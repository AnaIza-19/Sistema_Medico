package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.Cita_Repositorio;
import com.example.sistemamedico.Repositorio.Especialidad_Repositorio;
import com.example.sistemamedico.Repositorio.HorarioMedico_Repositorio;
import com.example.sistemamedico.Repositorio.Sucursal_Repositorio;
import com.example.sistemamedico.Repositorio.Usuario_Repositorio;

import com.example.sistemamedico.model.Cita;
import com.example.sistemamedico.model.Especialidad;
import com.example.sistemamedico.model.HorarioMedico;
import com.example.sistemamedico.model.Sucursal;
import com.example.sistemamedico.model.Usuario;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
public class AgendamientoCitaService {

    // =====================================================
    // REPOSITORIOS
    // =====================================================

    private final Sucursal_Repositorio sucursalRepository;

    private final Especialidad_Repositorio especialidadRepository;

    private final Usuario_Repositorio usuarioRepository;

    private final HorarioMedico_Repositorio horarioMedicoRepository;

    private final Cita_Repositorio citaRepository;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public AgendamientoCitaService(

            Sucursal_Repositorio sucursalRepository,

            Especialidad_Repositorio especialidadRepository,

            Usuario_Repositorio usuarioRepository,

            HorarioMedico_Repositorio horarioMedicoRepository,

            Cita_Repositorio citaRepository
    ) {

        this.sucursalRepository =
                sucursalRepository;

        this.especialidadRepository =
                especialidadRepository;

        this.usuarioRepository =
                usuarioRepository;

        this.horarioMedicoRepository =
                horarioMedicoRepository;

        this.citaRepository =
                citaRepository;
    }


    // =====================================================
    // PASO 1
    // SUCURSALES ACTIVAS
    // =====================================================

    @Cacheable("sucursalesCita")
    public List<Sucursal> obtenerSucursalesActivas() {

        System.out.println(
                "Cargando sucursales para citas desde BD..."
        );


        return sucursalRepository
                .findByActivoTrue();
    }


    // =====================================================
    // PASO 2
    // ESPECIALIDADES POR SUCURSAL
    // =====================================================

    @Cacheable(
            value = "especialidadesPorSucursal",
            key = "#sucursalId"
    )
    public List<Especialidad> obtenerEspecialidadesPorSucursal(
            Long sucursalId
    ) {

        System.out.println(
                "Cargando especialidades de la sucursal "
                        + sucursalId
                        + " desde BD..."
        );


        return especialidadRepository
                .findEspecialidadesActivasPorSucursal(
                        sucursalId
                );
    }


    // =====================================================
    // PASO 3
    // MÉDICOS
    // =====================================================

    public List<Usuario> obtenerMedicos(

            Long sucursalId,

            Long especialidadId
    ) {

        return usuarioRepository
                .findByRolNombreAndSucursalIdAndEspecialidadIdAndActivoTrueOrderByNombreAsc(
                        "MEDICO",
                        sucursalId,
                        especialidadId
                );
    }


    // =====================================================
    // PASO 4
    // HORARIOS DISPONIBLES
    // =====================================================

    public List<HorarioMedico> obtenerHorariosDisponibles(

            Long medicoId,

            Long sucursalId,

            Long especialidadId
    ) {

        return horarioMedicoRepository
                .findByMedicoIdAndSucursalIdAndEspecialidadIdAndEstadoOrderByFechaAscHoraInicioAsc(
                        medicoId,
                        sucursalId,
                        especialidadId,
                        "DISPONIBLE"
                );
    }


    // =====================================================
    // BUSCAR SUCURSAL
    // =====================================================

    public Optional<Sucursal> buscarSucursal(
            Long id
    ) {

        return sucursalRepository
                .findById(
                        id
                );
    }


    // =====================================================
    // BUSCAR ESPECIALIDAD
    // =====================================================

    public Optional<Especialidad> buscarEspecialidad(
            Long id
    ) {

        return especialidadRepository
                .findById(
                        id
                );
    }


    // =====================================================
    // BUSCAR USUARIO
    // =====================================================

    public Optional<Usuario> buscarUsuario(
            Long id
    ) {

        return usuarioRepository
                .findById(
                        id
                );
    }


    // =====================================================
    // BUSCAR HORARIO
    // =====================================================

    public Optional<HorarioMedico> buscarHorario(
            Long id
    ) {

        return horarioMedicoRepository
                .findById(
                        id
                );
    }


    // =====================================================
    // REGISTRAR CITA
    //
    // CU-03
    // CU-05 WALK-IN
    // CU-06 FA03
    //
    // ORIGEN:
    // PORTAL  = creada por paciente
    // INTERNO = creada por personal interno
    // =====================================================

    @Transactional
    public Cita registrarCita(

            Long pacienteId,

            Long sucursalId,

            Long especialidadId,

            Long medicoId,

            Long horarioId,

            String motivoConsulta,

            String origen
    ) {

        // =================================================
        // BUSCAR PACIENTE
        // =================================================

        Usuario paciente =
                usuarioRepository
                        .findById(
                                pacienteId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Paciente no encontrado."
                                        )
                        );


        // =================================================
        // VALIDAR PACIENTE
        // =================================================

        if (
                paciente.getRol() == null
                        ||
                        !"PACIENTE".equalsIgnoreCase(
                                paciente
                                        .getRol()
                                        .getNombre()
                        )
        ) {

            throw new IllegalArgumentException(
                    "El usuario seleccionado no corresponde a un paciente."
            );
        }


        if (!Boolean.TRUE.equals(
                paciente.getActivo()
        )) {

            throw new IllegalStateException(
                    "El paciente seleccionado se encuentra inactivo."
            );
        }


        // =================================================
        // BUSCAR MÉDICO
        // =================================================

        Usuario medico =
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
        // VALIDAR MÉDICO
        // =================================================

        if (
                medico.getRol() == null
                        ||
                        !"MEDICO".equalsIgnoreCase(
                                medico
                                        .getRol()
                                        .getNombre()
                        )
        ) {

            throw new IllegalArgumentException(
                    "El usuario seleccionado no corresponde a un médico."
            );
        }


        if (!Boolean.TRUE.equals(
                medico.getActivo()
        )) {

            throw new IllegalStateException(
                    "El médico seleccionado se encuentra inactivo."
            );
        }


        // =================================================
        // BUSCAR SUCURSAL
        // =================================================

        Sucursal sucursal =
                sucursalRepository
                        .findById(
                                sucursalId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Sucursal no encontrada."
                                        )
                        );


        // =================================================
        // BUSCAR ESPECIALIDAD
        // =================================================

        Especialidad especialidad =
                especialidadRepository
                        .findById(
                                especialidadId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Especialidad no encontrada."
                                        )
                        );


        // =================================================
        // BUSCAR HORARIO
        // =================================================

        HorarioMedico horario =
                horarioMedicoRepository
                        .findById(
                                horarioId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Horario no encontrado."
                                        )
                        );


        // =================================================
        // VALIDAR DISPONIBILIDAD DEL HORARIO
        // =================================================

        if (
                horario.getEstado() == null
                        ||
                        !"DISPONIBLE".equalsIgnoreCase(
                                horario.getEstado()
                        )
        ) {

            throw new IllegalStateException(
                    "El horario seleccionado ya no está disponible."
            );
        }


        // =================================================
        // VALIDAR QUE EL HORARIO CORRESPONDA AL MÉDICO
        // =================================================

        if (
                horario.getMedico() == null
                        ||
                        horario.getMedico().getId() == null
                        ||
                        !horario.getMedico()
                                .getId()
                                .equals(
                                        medico.getId()
                                )
        ) {

            throw new IllegalStateException(
                    "El horario seleccionado no corresponde al médico seleccionado."
            );
        }


        // =================================================
        // VALIDAR SUCURSAL DEL HORARIO
        // =================================================

        if (
                horario.getSucursal() == null
                        ||
                        horario.getSucursal().getId() == null
                        ||
                        !horario.getSucursal()
                                .getId()
                                .equals(
                                        sucursal.getId()
                                )
        ) {

            throw new IllegalStateException(
                    "El horario seleccionado no corresponde a la sucursal seleccionada."
            );
        }


        // =================================================
        // VALIDAR ESPECIALIDAD DEL HORARIO
        // =================================================

        if (
                horario.getEspecialidad() == null
                        ||
                        horario.getEspecialidad().getId() == null
                        ||
                        !horario.getEspecialidad()
                                .getId()
                                .equals(
                                        especialidad.getId()
                                )
        ) {

            throw new IllegalStateException(
                    "El horario seleccionado no corresponde a la especialidad seleccionada."
            );
        }


        // =================================================
        // VALIDAR MOTIVO DE CONSULTA
        // =================================================

        String motivo =
                motivoConsulta == null
                        ? ""
                        : motivoConsulta.trim();


        if (
                motivo.length() < 10
                        ||
                        motivo.length() > 2000
        ) {

            throw new IllegalArgumentException(
                    "El motivo de consulta debe contener entre 10 y 2000 caracteres."
            );
        }


        // =================================================
        // VALIDAR ORIGEN
        // =================================================

        String origenLimpio =
                origen == null
                        ? "PORTAL"
                        : origen
                        .trim()
                        .toUpperCase();


        if (
                !"PORTAL".equals(
                        origenLimpio
                )
                        &&
                        !"INTERNO".equals(
                                origenLimpio
                        )
        ) {

            throw new IllegalArgumentException(
                    "El origen de la cita no es válido."
            );
        }


        // =================================================
        // CREAR CITA
        // =================================================

        Cita cita =
                new Cita();


        cita.setPaciente(
                paciente
        );


        cita.setMedico(
                medico
        );


        cita.setSucursal(
                sucursal
        );


        cita.setEspecialidad(
                especialidad
        );


        cita.setHorario(
                horario
        );


        cita.setMotivoConsulta(
                motivo
        );


        cita.setEstado(
                "PENDIENTE_PAGO"
        );


        cita.setPrioridad(
                "NORMAL"
        );


        cita.setOrigen(
                origenLimpio
        );


        // =================================================
        // RESERVAR HORARIO
        // =================================================

        horario.setEstado(
                "RESERVADO"
        );


        // =================================================
        // PORTAL
        //
        // El paciente tiene 10 minutos para realizar
        // el pago.
        // =================================================

        if (
                "PORTAL".equals(
                        origenLimpio
                )
        ) {

            horario.setReservadoHasta(
                    LocalDateTime.now()
                            .plusMinutes(
                                    5
                            )
            );
        }


        // =================================================
        // INTERNO
        //
        // Las citas creadas por personal interno
        // NO se cancelan automáticamente.
        // =================================================

        else {

            horario.setReservadoHasta(
                    null
            );
        }


        horarioMedicoRepository.save(
                horario
        );


        // =================================================
        // GUARDAR CITA
        // =================================================

        return citaRepository.save(
                cita
        );
    }


    // =====================================================
    // CU-06 FA03
    // LIBERAR RESERVA EXPIRADA
    //
    // SOLO APLICA A CITAS:
    // - PORTAL
    // - PENDIENTE_PAGO
    // - CON TIEMPO DE RESERVA VENCIDO
    // =====================================================

    @Transactional
    public Cita liberarReservaExpirada(
            Long citaId
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
        // SOLO CITAS DEL PORTAL
        // =================================================

        if (
                cita.getOrigen() == null
                        ||
                        !"PORTAL".equalsIgnoreCase(
                                cita.getOrigen()
                        )
        ) {

            return cita;
        }


        // =================================================
        // SOLO SI ESTÁ PENDIENTE DE PAGO
        // =================================================

        if (
                cita.getEstado() == null
                        ||
                        !"PENDIENTE_PAGO".equalsIgnoreCase(
                                cita.getEstado()
                        )
        ) {

            return cita;
        }


        // =================================================
        // OBTENER HORARIO
        // =================================================

        HorarioMedico horario =
                cita.getHorario();


        if (horario == null) {

            return cita;
        }


        // =================================================
        // COMPROBAR VENCIMIENTO
        // =================================================

        if (
                horario.getReservadoHasta() != null
                        &&
                        LocalDateTime.now()
                                .isAfter(
                                        horario.getReservadoHasta()
                                )
        ) {

            // =================================================
            // LIBERAR HORARIO
            // =================================================

            horario.setEstado(
                    "DISPONIBLE"
            );


            horario.setReservadoHasta(
                    null
            );


            horarioMedicoRepository.save(
                    horario
            );


            // =================================================
            // CAMBIAR ESTADO DE CITA
            // =================================================

            cita.setEstado(
                    "EXPIRADA"
            );


            citaRepository.save(
                    cita
            );
        }


        return cita;
    }
}