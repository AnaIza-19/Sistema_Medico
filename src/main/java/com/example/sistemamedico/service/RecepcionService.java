package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.Cita_Repositorio;
import com.example.sistemamedico.Repositorio.HorarioMedico_Repositorio;
import com.example.sistemamedico.Repositorio.RecepcionEmergencia_Repositorio;
import com.example.sistemamedico.Repositorio.Usuario_Repositorio;

import com.example.sistemamedico.model.Cita;
import com.example.sistemamedico.model.HorarioMedico;
import com.example.sistemamedico.model.RecepcionEmergencia;
import com.example.sistemamedico.model.Rol;
import com.example.sistemamedico.model.Usuario;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class RecepcionService {

    // =====================================================
    // REPOSITORIOS
    // =====================================================

    private final Cita_Repositorio citaRepository;

    private final Usuario_Repositorio usuarioRepository;

    private final RecepcionEmergencia_Repositorio emergenciaRepository;

    private final HorarioMedico_Repositorio horarioMedicoRepository;

    private final EntityManager entityManager;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public RecepcionService(

            Cita_Repositorio citaRepository,

            Usuario_Repositorio usuarioRepository,

            RecepcionEmergencia_Repositorio emergenciaRepository,

            HorarioMedico_Repositorio horarioMedicoRepository,

            EntityManager entityManager
    ) {

        this.citaRepository =
                citaRepository;

        this.usuarioRepository =
                usuarioRepository;

        this.emergenciaRepository =
                emergenciaRepository;

        this.horarioMedicoRepository =
                horarioMedicoRepository;

        this.entityManager =
                entityManager;
    }


    // =====================================================
    // MÉTODO AUXILIAR
    // VERIFICAR SI UN USUARIO ES PACIENTE
    // =====================================================

    private boolean esPaciente(
            Usuario usuario
    ) {

        return usuario != null
                &&
                usuario.getRol() != null
                &&
                "PACIENTE".equalsIgnoreCase(
                        usuario.getRol().getNombre()
                );
    }


    // =====================================================
    // MÉTODO AUXILIAR
    // VERIFICAR SI UNA CITA ESTÁ ACTIVA
    // =====================================================

    private boolean esCitaActiva(
            Cita cita
    ) {

        if (
                cita == null
                        ||
                        cita.getEstado() == null
        ) {

            return false;
        }


        return switch (
                cita.getEstado().toUpperCase()
                ) {

            case "PENDIENTE_PAGO",
                 "PAGADA",
                 "CONFIRMADA",
                 "PACIENTE_PRESENTE"
                    -> true;

            default
                    -> false;
        };
    }


    // =====================================================
    // BUSCAR CITA POR NÚMERO DE CITA
    // =====================================================

    public Optional<Cita> buscarPorNumeroCita(
            Long citaId
    ) {

        if (citaId == null) {

            return Optional.empty();
        }


        return citaRepository.findById(
                citaId
        );
    }


    // =====================================================
    // BUSCAR PACIENTE POR DPI
    // SOLO PACIENTES ACTIVOS
    // =====================================================

    public Optional<Usuario> buscarPacientePorDpi(
            String dpi
    ) {

        String dpiLimpio =
                dpi == null
                        ? ""
                        : dpi.trim();


        if (dpiLimpio.isBlank()) {

            return Optional.empty();
        }


        return usuarioRepository
                .findByDpi(
                        dpiLimpio
                )
                .filter(
                        this::esPaciente
                )
                .filter(
                        usuario ->
                                Boolean.TRUE.equals(
                                        usuario.getActivo()
                                )
                );
    }


    // =====================================================
    // BUSCAR CITAS ACTIVAS DEL PACIENTE
    // =====================================================

    public List<Cita> buscarCitasPaciente(
            Long pacienteId
    ) {

        if (pacienteId == null) {

            return List.of();
        }


        return citaRepository
                .findByPacienteIdOrderByFechaCreacionDesc(
                        pacienteId
                )
                .stream()
                .filter(
                        this::esCitaActiva
                )
                .toList();
    }


    // =====================================================
    // REGISTRAR LLEGADA DEL PACIENTE
    // =====================================================

    @Transactional
    public Cita registrarLlegada(
            Long citaId
    ) {

        Cita cita =
                citaRepository
                        .findById(
                                citaId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Cita no encontrada."
                                        )
                        );


        // =================================================
        // SOLO CITAS CONFIRMADAS PUEDEN REGISTRAR LLEGADA
        // =================================================

        if (
                cita.getEstado() == null
                        ||
                        !"CONFIRMADA".equalsIgnoreCase(
                                cita.getEstado()
                        )
        ) {

            throw new IllegalStateException(
                    "Operación no permitida."
            );
        }


        // =================================================
        // CAMBIAR ESTADO
        // =================================================

        cita.setEstado(
                "PACIENTE_PRESENTE"
        );


        // =================================================
        // GUARDAR HORA DE LLEGADA
        // =================================================

        cita.setHoraLlegada(
                LocalDateTime.now()
        );


        return citaRepository.save(
                cita
        );
    }


    // =====================================================
    // FA07
    // OBTENER SOLO MÉDICOS REALMENTE DISPONIBLES
    // PARA LA MISMA FECHA Y HORA
    // =====================================================

    public List<Usuario> obtenerMedicosDisponibles(
            Cita cita
    ) {

        if (
                cita == null
                        ||
                        cita.getSucursal() == null
                        ||
                        cita.getEspecialidad() == null
                        ||
                        cita.getHorario() == null
        ) {

            return List.of();
        }


        HorarioMedico horarioActual =
                cita.getHorario();


        // =================================================
        // MÉDICOS ACTIVOS DE LA MISMA SEDE
        // Y ESPECIALIDAD
        // =================================================

        List<Usuario> candidatos =
                usuarioRepository
                        .findByRolNombreAndSucursalIdAndEspecialidadIdAndActivoTrueOrderByNombreAsc(
                                "MEDICO",
                                cita.getSucursal().getId(),
                                cita.getEspecialidad().getId()
                        );


        // =================================================
        // DEJAR SOLO LOS QUE TENGAN
        // EXACTAMENTE LA MISMA FRANJA DISPONIBLE
        // =================================================

        return candidatos
                .stream()

                // No mostrar al médico actual
                .filter(
                        medico ->
                                cita.getMedico() == null
                                        ||
                                        !medico.getId()
                                                .equals(
                                                        cita.getMedico().getId()
                                                )
                )

                // Debe existir horario DISPONIBLE
                // en misma fecha y hora
                .filter(
                        medico ->
                                horarioMedicoRepository
                                        .findByMedicoIdAndSucursalIdAndEspecialidadIdAndFechaAndHoraInicioAndHoraFinAndEstado(

                                                medico.getId(),

                                                cita.getSucursal()
                                                        .getId(),

                                                cita.getEspecialidad()
                                                        .getId(),

                                                horarioActual
                                                        .getFecha(),

                                                horarioActual
                                                        .getHoraInicio(),

                                                horarioActual
                                                        .getHoraFin(),

                                                "DISPONIBLE"
                                        )
                                        .isPresent()
                )

                .toList();
    }


    // =====================================================
    // FA07
    // REASIGNAR MÉDICO
    // =====================================================

    @Transactional
    public Cita reasignarMedico(
            Long citaId,
            Long medicoId,
            String nota
    ) {

        // =================================================
        // BUSCAR CITA
        // =================================================

        Cita cita =
                citaRepository
                        .findById(
                                citaId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Cita no encontrada."
                                        )
                        );


        // =================================================
        // VALIDAR ESTADO DE LA CITA
        // =================================================

        if (
                cita.getEstado() == null
                        ||
                        (
                                !"CONFIRMADA".equalsIgnoreCase(
                                        cita.getEstado()
                                )
                                        &&
                                        !"PACIENTE_PRESENTE".equalsIgnoreCase(
                                                cita.getEstado()
                                        )
                        )
        ) {

            throw new IllegalStateException(
                    "Operación no permitida."
            );
        }


        // =================================================
        // BUSCAR NUEVO MÉDICO
        // =================================================

        Usuario nuevoMedico =
                usuarioRepository
                        .findById(
                                medicoId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Médico no encontrado."
                                        )
                        );


        // =================================================
        // VALIDAR QUE REALMENTE SEA MÉDICO
        // =================================================

        if (
                nuevoMedico.getRol() == null
                        ||
                        !"MEDICO".equalsIgnoreCase(
                                nuevoMedico
                                        .getRol()
                                        .getNombre()
                        )
        ) {

            throw new IllegalStateException(
                    "El usuario seleccionado no es médico."
            );
        }


        // =================================================
        // VALIDAR QUE EL MÉDICO ESTÉ ACTIVO
        // =================================================

        if (!Boolean.TRUE.equals(
                nuevoMedico.getActivo()
        )) {

            throw new IllegalStateException(
                    "El médico seleccionado se encuentra inactivo."
            );
        }


        // =================================================
        // VALIDAR QUE NO SEA EL MISMO MÉDICO
        // =================================================

        if (
                cita.getMedico() != null
                        &&
                        cita.getMedico().getId() != null
                        &&
                        cita.getMedico()
                                .getId()
                                .equals(
                                        nuevoMedico.getId()
                                )
        ) {

            throw new IllegalStateException(
                    "La cita ya está asignada a este médico."
            );
        }


        // =================================================
        // VALIDAR SUCURSAL DEL MÉDICO
        // =================================================

        if (
                nuevoMedico.getSucursal() == null
                        ||
                        cita.getSucursal() == null
                        ||
                        !nuevoMedico
                                .getSucursal()
                                .getId()
                                .equals(
                                        cita.getSucursal().getId()
                                )
        ) {

            throw new IllegalStateException(
                    "El médico seleccionado no pertenece "
                            + "a la misma sede de la cita."
            );
        }


        // =================================================
        // VALIDAR ESPECIALIDAD DEL MÉDICO
        // =================================================

        if (
                nuevoMedico.getEspecialidad() == null
                        ||
                        cita.getEspecialidad() == null
                        ||
                        !nuevoMedico
                                .getEspecialidad()
                                .getId()
                                .equals(
                                        cita.getEspecialidad().getId()
                                )
        ) {

            throw new IllegalStateException(
                    "El médico seleccionado no pertenece "
                            + "a la misma especialidad de la cita."
            );
        }


        // =================================================
        // OBTENER HORARIO ACTUAL DE LA CITA
        // =================================================

        HorarioMedico horarioAnterior =
                cita.getHorario();


        if (horarioAnterior == null) {

            throw new IllegalStateException(
                    "La cita no tiene un horario asociado."
            );
        }


        // =================================================
        // BUSCAR LA MISMA FECHA Y HORA
        // PARA EL NUEVO MÉDICO
        // =================================================

        HorarioMedico nuevoHorario =
                horarioMedicoRepository
                        .findByMedicoIdAndSucursalIdAndEspecialidadIdAndFechaAndHoraInicioAndHoraFinAndEstado(

                                nuevoMedico.getId(),

                                cita.getSucursal()
                                        .getId(),

                                cita.getEspecialidad()
                                        .getId(),

                                horarioAnterior
                                        .getFecha(),

                                horarioAnterior
                                        .getHoraInicio(),

                                horarioAnterior
                                        .getHoraFin(),

                                "DISPONIBLE"
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "El médico seleccionado no tiene disponibilidad "
                                                        + "para la fecha y hora de esta cita."
                                        )
                        );


        // =================================================
        // VALIDAR NOTA OPCIONAL
        // =================================================

        String notaLimpia =
                nota == null
                        ? ""
                        : nota.trim();


        if (notaLimpia.length() > 1000) {

            throw new IllegalArgumentException(
                    "La nota de reasignación no puede superar los 1000 caracteres."
            );
        }


        // =================================================
        // LIBERAR HORARIO DEL MÉDICO ANTERIOR
        // =================================================

        horarioAnterior.setEstado(
                "DISPONIBLE"
        );


        horarioAnterior.setReservadoHasta(
                null
        );


        horarioMedicoRepository.save(
                horarioAnterior
        );


        // =================================================
        // RESERVAR HORARIO DEL NUEVO MÉDICO
        // =================================================

        nuevoHorario.setEstado(
                "RESERVADO"
        );


        /*
         * Como la cita ya está CONFIRMADA
         * o PACIENTE_PRESENTE,
         * esta reserva ya no debe expirar
         * a los 5 minutos.
         */
        nuevoHorario.setReservadoHasta(
                null
        );


        horarioMedicoRepository.save(
                nuevoHorario
        );


        // =================================================
        // GUARDAR NOTA DE REASIGNACIÓN
        // =================================================

        if (notaLimpia.isBlank()) {

            cita.setNotaReasignacion(
                    null
            );

        } else {

            cita.setNotaReasignacion(
                    notaLimpia
            );
        }


        // =================================================
        // CAMBIAR MÉDICO Y HORARIO EN LA CITA
        // =================================================

        cita.setMedico(
                nuevoMedico
        );


        cita.setHorario(
                nuevoHorario
        );


        // =================================================
        // GUARDAR CITA
        // =================================================

        return citaRepository.save(
                cita
        );
    }


    // =====================================================
    // FA01
    // REGISTRAR EMERGENCIA
    // =====================================================

    @Transactional
    public RecepcionEmergencia registrarEmergencia(

            String dpi,

            String nombre,

            String apellido
    ) {

        // =================================================
        // LIMPIAR DPI
        // =================================================

        String dpiLimpio =
                dpi == null
                        ? ""
                        : dpi.trim();


        // =================================================
        // VALIDAR DPI
        // =================================================

        if (!dpiLimpio.matches("\\d{13}")) {

            throw new IllegalArgumentException(
                    "El DPI debe contener exactamente 13 dígitos."
            );
        }


        // =================================================
        // LIMPIAR NOMBRE Y APELLIDO
        // =================================================

        String nombreLimpio =
                nombre == null
                        ? ""
                        : nombre.trim();


        String apellidoLimpio =
                apellido == null
                        ? ""
                        : apellido.trim();


        // =================================================
        // VALIDAR NOMBRE
        // =================================================

        if (nombreLimpio.isBlank()) {

            throw new IllegalArgumentException(
                    "Debe ingresar el nombre del paciente."
            );
        }


        // =================================================
        // VALIDAR APELLIDO
        // =================================================

        if (apellidoLimpio.isBlank()) {

            throw new IllegalArgumentException(
                    "Debe ingresar el apellido del paciente."
            );
        }


        // =================================================
        // BUSCAR USUARIO EXISTENTE POR DPI
        // =================================================

        Usuario paciente =
                usuarioRepository
                        .findByDpi(
                                dpiLimpio
                        )
                        .orElse(null);


        // =================================================
        // SI EL DPI YA EXISTE
        // VALIDAR QUE SEA PACIENTE
        // =================================================

        if (paciente != null) {

            if (!esPaciente(paciente)) {

                throw new IllegalArgumentException(
                        "El DPI ingresado pertenece a un usuario "
                                + "que no está registrado como paciente."
                );
            }


            if (!Boolean.TRUE.equals(
                    paciente.getActivo()
            )) {

                throw new IllegalArgumentException(
                        "El paciente se encuentra inactivo."
                );
            }
        }


        // =================================================
        // SI NO EXISTE
        // CREAR PACIENTE BÁSICO DE EMERGENCIA
        // =================================================

        if (paciente == null) {

            paciente =
                    new Usuario();


            paciente.setDpi(
                    dpiLimpio
            );


            paciente.setNombre(
                    nombreLimpio
            );


            paciente.setApellido(
                    apellidoLimpio
            );


            // =================================================
            // CREAR NOMBRE DE USUARIO PROVISIONAL
            // =================================================

            String identificador =
                    UUID.randomUUID()
                            .toString()
                            .replace(
                                    "-",
                                    ""
                            )
                            .substring(
                                    0,
                                    10
                            );


            paciente.setNombreUsuario(
                    "emg"
                            + identificador
            );


            // =================================================
            // CONTRASEÑA PROVISIONAL
            // =================================================

            paciente.setContrasena(
                    "Emergencia2026#"
                            + identificador.substring(
                            0,
                            2
                    )
            );


            // =================================================
            // ASIGNAR ROL PACIENTE
            // ACTUALMENTE rol_id = 1
            // =================================================

            Rol rolPaciente =
                    entityManager
                            .getReference(
                                    Rol.class,
                                    1L
                            );


            paciente.setRol(
                    rolPaciente
            );


            // =================================================
            // ACTIVAR PACIENTE
            // =================================================

            paciente.setActivo(
                    true
            );


            paciente.setIntentosFallidos(
                    0
            );


            // =================================================
            // GUARDAR PACIENTE
            // =================================================

            paciente =
                    usuarioRepository.save(
                            paciente
                    );
        }


        // =================================================
        // CREAR REGISTRO DE RECEPCIÓN DE EMERGENCIA
        // =================================================

        RecepcionEmergencia emergencia =
                new RecepcionEmergencia();


        emergencia.setPaciente(
                paciente
        );


        emergencia.setPrioridad(
                "EMERGENCIA"
        );


        emergencia.setEstado(
                "PENDIENTE_SIGNOS_VITALES"
        );


        emergencia.setHoraLlegada(
                LocalDateTime.now()
        );


        // =================================================
        // GUARDAR EMERGENCIA
        // =================================================

        return emergenciaRepository.save(
                emergencia
        );
    }
}