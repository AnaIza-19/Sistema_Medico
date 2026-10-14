package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.Cita_Repositorio;
import com.example.sistemamedico.Repositorio.ConsultaMedica_Repositorio;
import com.example.sistemamedico.Repositorio.Especialidad_Repositorio;
import com.example.sistemamedico.Repositorio.HorarioMedico_Repositorio;
import com.example.sistemamedico.Repositorio.Sucursal_Repositorio;
import com.example.sistemamedico.Repositorio.Usuario_Repositorio;

import com.example.sistemamedico.model.Cita;
import com.example.sistemamedico.model.ConsultaMedica;
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

    private final ConsultaMedica_Repositorio consultaMedicaRepository;

    private final CorreoService correoService;

    // CU-16: detectar eventos que bloquean la agenda médica.
    private final AgendaMedicaBloqueoService agendaMedicaBloqueoService;

    // CU-16: coordinar citas y eventos del mismo médico.
    private final AgendaMedicaConcurrenciaService agendaMedicaConcurrenciaService;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public AgendamientoCitaService(

            Sucursal_Repositorio sucursalRepository,
            Especialidad_Repositorio especialidadRepository,
            Usuario_Repositorio usuarioRepository,
            HorarioMedico_Repositorio horarioMedicoRepository,
            Cita_Repositorio citaRepository,
            ConsultaMedica_Repositorio consultaMedicaRepository,
            CorreoService correoService,
            AgendaMedicaBloqueoService agendaMedicaBloqueoService,
            AgendaMedicaConcurrenciaService agendaMedicaConcurrenciaService
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

        this.consultaMedicaRepository =
                consultaMedicaRepository;

        this.correoService =
                correoService;

        this.agendaMedicaBloqueoService =
                agendaMedicaBloqueoService;

        this.agendaMedicaConcurrenciaService =
                agendaMedicaConcurrenciaService;
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
                )
                .stream()
                .filter(horario ->
                        !agendaMedicaBloqueoService.existeConflicto(
                                medicoId,
                                horario.getFecha(),
                                horario.getHoraInicio(),
                                horario.getHoraFin()
                        )
                )
                .toList();
    }

// =====================================================
// CU-12
// HORARIOS FUTUROS DISPONIBLES PARA SEGUIMIENTO
//
// Solo:
// - mismo médico
// - misma sucursal
// - misma especialidad
// - estado DISPONIBLE
// - fecha y hora futuras
// =====================================================

    public List<HorarioMedico> obtenerHorariosSeguimientoDisponibles(

            Long medicoId,

            Long sucursalId,

            Long especialidadId
    ) {

        LocalDateTime ahora =
                LocalDateTime.now();


        return obtenerHorariosDisponibles(
                medicoId,
                sucursalId,
                especialidadId
        )
                .stream()

                .filter(
                        horario ->
                                horario.getFecha() != null
                                        &&
                                        horario.getHoraInicio() != null
                )

                .filter(
                        horario -> {

                            LocalDateTime fechaHora =
                                    LocalDateTime.of(
                                            horario.getFecha(),
                                            horario.getHoraInicio()
                                    );


                            return fechaHora.isAfter(
                                    ahora
                            );
                        }
                )

                .toList();
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
    // CU-12
    // BUSCAR CONSULTA MÉDICA
    // =====================================================

    public Optional<ConsultaMedica> buscarConsultaMedica(
            Long id
    ) {

        return consultaMedicaRepository
                .findById(
                        id
                );
    }
// =====================================================
// CU-12
// BUSCAR CONSULTA MÉDICA POR CITA
// =====================================================

    public Optional<ConsultaMedica> buscarConsultaMedicaPorCita(
            Long citaId
    ) {

        return consultaMedicaRepository
                .findByCitaId(
                        citaId
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


        // CU-16: adquirir bloqueo antes de consultar el horario.
        agendaMedicaConcurrenciaService.bloquearMedico(medicoId);

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
                        .bloquearPorId(
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
        // CU-16: NO RESERVAR CITAS SOBRE EVENTOS DEL MÉDICO
        // =================================================

        agendaMedicaBloqueoService.validarHorario(
                medico.getId(),
                horario.getFecha(),
                horario.getHoraInicio(),
                horario.getHoraFin()
        );

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
    // CU-12
    // REGISTRAR CITA DE SEGUIMIENTO
    //
    // Los datos de paciente, médico, sucursal y especialidad
    // se obtienen automáticamente desde la consulta padre.
    // =====================================================

    @Transactional
    public Cita registrarCitaSeguimiento(

            Long consultaPadreId,

            Long horarioId,

            String tipoSeguimiento,

            String motivoSeguimiento,

            String prioridad,

            Long medicoAutenticadoId
    ) {

        // =================================================
        // BUSCAR CONSULTA PADRE
        // =================================================

        ConsultaMedica consultaPadre =
                consultaMedicaRepository
                        .findById(
                                consultaPadreId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Consulta médica no encontrada."
                                        )
                        );


        // =================================================
        // VALIDAR ESTADO DE LA CONSULTA
        // =================================================

        String estadoConsulta =
                consultaPadre.getEstado() == null
                        ? ""
                        : consultaPadre
                        .getEstado()
                        .trim()
                        .toUpperCase();


        if (
                !"EN_CURSO".equals(
                        estadoConsulta
                )
                        &&
                        !"FINALIZADA".equals(
                                estadoConsulta
                        )
        ) {

            throw new IllegalStateException(
                    "La consulta médica no está disponible para agendar seguimiento."
            );
        }


        // =================================================
        // OBTENER CITA PADRE
        // =================================================

        Cita citaPadre =
                consultaPadre.getCita();


        if (citaPadre == null) {

            throw new IllegalStateException(
                    "La consulta médica no tiene una cita asociada."
            );
        }


        // =================================================
        // OBTENER DATOS PRECARGADOS
        // =================================================

        Usuario paciente =
                citaPadre.getPaciente();

        Usuario medico =
                citaPadre.getMedico();

        Sucursal sucursal =
                citaPadre.getSucursal();

        Especialidad especialidad =
                citaPadre.getEspecialidad();


        if (
                paciente == null
                        ||
                        medico == null
                        ||
                        sucursal == null
                        ||
                        especialidad == null
        ) {

            throw new IllegalStateException(
                    "No fue posible obtener los datos necesarios de la consulta padre."
            );
        }


        // =================================================
        // VALIDAR MÉDICO AUTENTICADO
        // =================================================

        if (
                medicoAutenticadoId == null
                        ||
                        medico.getId() == null
                        ||
                        !medico.getId()
                                .equals(
                                        medicoAutenticadoId
                                )
        ) {

            throw new IllegalStateException(
                    "La consulta no pertenece al médico autenticado."
            );
        }


        // CU-16: coordinar el seguimiento con eventos y citas.
        agendaMedicaConcurrenciaService.bloquearMedico(medico.getId());

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

            throw new IllegalStateException(
                    "La consulta no tiene un paciente válido."
            );
        }


        if (!Boolean.TRUE.equals(
                paciente.getActivo()
        )) {

            throw new IllegalStateException(
                    "El paciente se encuentra inactivo."
            );
        }


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

            throw new IllegalStateException(
                    "La consulta no tiene un médico válido."
            );
        }


        if (!Boolean.TRUE.equals(
                medico.getActivo()
        )) {

            throw new IllegalStateException(
                    "El médico se encuentra inactivo."
            );
        }


        // =================================================
        // VALIDAR TIPO DE SEGUIMIENTO
        // =================================================

        String tipoLimpio =
                tipoSeguimiento == null
                        ? ""
                        : tipoSeguimiento
                        .trim()
                        .toUpperCase();


        if (
                !"MONITOREO_TRATAMIENTO".equals(
                        tipoLimpio
                )
                        &&
                        !"REVISION_RESULTADOS_LABORATORIO".equals(
                                tipoLimpio
                        )
        ) {

            throw new IllegalArgumentException(
                    "Debe seleccionar el tipo de seguimiento."
            );
        }


        // =================================================
        // VALIDAR MOTIVO / OBSERVACIONES
        // =================================================

        String motivo =
                motivoSeguimiento == null
                        ? ""
                        : motivoSeguimiento.trim();


        if (
                motivo.length() < 10
                        ||
                        motivo.length() > 2000
        ) {

            throw new IllegalArgumentException(
                    "Las observaciones son obligatorias. Deben contener entre 10 y 2000 caracteres."
            );
        }


        // =================================================
        // VALIDAR PRIORIDAD
        // =================================================

        String prioridadLimpia =
                prioridad == null
                        ? ""
                        : prioridad
                        .trim()
                        .toUpperCase();


        if (prioridadLimpia.isBlank()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar la prioridad del seguimiento."
            );
        }


        if (prioridadLimpia.length() > 20) {

            throw new IllegalArgumentException(
                    "La prioridad seleccionada no es válida."
            );
        }


        // =================================================
        // BUSCAR HORARIO
        // =================================================

        HorarioMedico horario =
                horarioMedicoRepository
                        .bloquearPorId(
                                horarioId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Horario no encontrado."
                                        )
                        );


        // =================================================
        // FA01
        // VOLVER A VALIDAR DISPONIBILIDAD
        // =================================================

        if (
                horario.getEstado() == null
                        ||
                        !"DISPONIBLE".equalsIgnoreCase(
                                horario.getEstado()
                        )
        ) {

            throw new IllegalStateException(
                    "El horario seleccionado ya no está disponible. Por favor, elija otro horario."
            );
        }

        // =================================================
// CU-12
// VALIDAR QUE LA FECHA Y HORA SEAN FUTURAS
// =================================================

        if (
                horario.getFecha() == null
                        ||
                        horario.getHoraInicio() == null
        ) {

            throw new IllegalArgumentException(
                    "Seleccione una fecha futura dentro de los horarios disponibles del médico."
            );
        }


        LocalDateTime fechaHoraSeguimiento =
                LocalDateTime.of(
                        horario.getFecha(),
                        horario.getHoraInicio()
                );


        if (
                !fechaHoraSeguimiento.isAfter(
                        LocalDateTime.now()
                )
        ) {

            throw new IllegalArgumentException(
                    "Seleccione una fecha futura dentro de los horarios disponibles del médico."
            );
        }

        // =================================================
        // EL HORARIO DEBE SER DEL MISMO MÉDICO
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
                    "El horario seleccionado no corresponde al médico de la consulta."
            );
        }


        // =================================================
        // VALIDAR SUCURSAL
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
                    "El horario seleccionado no corresponde a la sucursal de la consulta."
            );
        }


        // =================================================
        // VALIDAR ESPECIALIDAD
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
                    "El horario seleccionado no corresponde a la especialidad de la consulta."
            );
        }


        // =================================================
        // CU-16: NO AGENDAR SEGUIMIENTOS SOBRE EVENTOS
        // =================================================

        agendaMedicaBloqueoService.validarHorario(
                medico.getId(),
                horario.getFecha(),
                horario.getHoraInicio(),
                horario.getHoraFin()
        );

        // =================================================
        // CREAR CITA DE SEGUIMIENTO
        // =================================================

        Cita citaSeguimiento =
                new Cita();


        citaSeguimiento.setPaciente(
                paciente
        );


        citaSeguimiento.setMedico(
                medico
        );


        citaSeguimiento.setSucursal(
                sucursal
        );


        citaSeguimiento.setEspecialidad(
                especialidad
        );


        citaSeguimiento.setHorario(
                horario
        );


        citaSeguimiento.setMotivoConsulta(
                motivo
        );


        citaSeguimiento.setPrioridad(
                prioridadLimpia
        );


        citaSeguimiento.setOrigen(
                "INTERNO"
        );


        citaSeguimiento.setTipoSeguimiento(
                tipoLimpio
        );


        citaSeguimiento.setConsultaPadre(
                consultaPadre
        );

// =================================================
// PROGRAMAR RECORDATORIO
// UN DÍA ANTES DE LA CITA
// =================================================

        citaSeguimiento.setRecordatorioProgramadoPara(
                fechaHoraSeguimiento.minusDays(1)
        );

        citaSeguimiento.setRecordatorioEnviado(
                false
        );
        /*
         * El CU-12 no incluye un paso de pago.
         * La cita es agendada directamente por el médico.
         */
        citaSeguimiento.setEstado(
                "CONFIRMADA"
        );


        // =================================================
        // OCUPAR HORARIO
        // =================================================

        horario.setEstado(
                "OCUPADO"
        );


        horario.setReservadoHasta(
                null
        );


        // =================================================
// GUARDAR HORARIO
// =================================================

        horarioMedicoRepository.save(
                horario
        );


// =================================================
// GUARDAR CITA DE SEGUIMIENTO
// =================================================

        Cita citaGuardada =
                citaRepository.save(
                        citaSeguimiento
                );



// =================================================
// CU-12
// ENVIAR CORREO DE CONFIRMACIÓN
//
// Si el correo falla, NO se cancela
// el registro de la cita.
// =================================================

        try {

            correoService
                    .enviarConfirmacionSeguimiento(
                            citaGuardada
                    );

        } catch (Exception e) {

            System.err.println(
                    "Error al enviar notificación por correo "
                            + "electrónico al paciente "
                            + citaGuardada
                            .getPaciente()
                            .getNombreCompleto()
                            + "."
            );


            System.err.println(
                    "Detalle: "
                            + e.getMessage()
            );
        }


// =================================================
// DEVOLVER CITA REGISTRADA
// =================================================

        return citaGuardada;
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
