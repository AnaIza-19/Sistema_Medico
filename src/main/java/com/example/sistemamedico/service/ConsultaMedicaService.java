package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.Cita_Repositorio;
import com.example.sistemamedico.Repositorio.ConsultaMedica_Repositorio;
import com.example.sistemamedico.model.Cita;
import com.example.sistemamedico.model.ConsultaMedica;
import com.example.sistemamedico.model.Usuario;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsultaMedicaService {

    private final Cita_Repositorio citaRepository;
    private final ConsultaMedica_Repositorio consultaRepository;


    public ConsultaMedicaService(
            Cita_Repositorio citaRepository,
            ConsultaMedica_Repositorio consultaRepository
    ) {

        this.citaRepository = citaRepository;
        this.consultaRepository = consultaRepository;
    }


    // ==========================================================
    // CITAS EN ESPERA DEL MEDICO
    // ==========================================================

    public List<Cita> obtenerCitasEnEspera(Long medicoId) {

        return citaRepository
                .findByMedicoIdAndEstadoIgnoreCaseOrderByHoraLlegadaAsc(
                        medicoId,
                        "LISTO_CONSULTA"
                );
    }


    // ==========================================================
    // CITAS EN CONSULTA MEDICA
    // ==========================================================

    public List<Cita> obtenerCitasEnConsulta(Long medicoId) {

        return citaRepository
                .findByMedicoIdAndEstadoIgnoreCaseOrderByHoraLlegadaAsc(
                        medicoId,
                        "CONSULTA_MEDICA"
                );
    }


    // ==========================================================
    // CITAS EVALUADAS PENDIENTES DE CIERRE
    // ==========================================================

    public List<Cita> obtenerCitasEvaluadas(Long medicoId) {

        return citaRepository
                .findByMedicoIdAndEstadoIgnoreCaseOrderByHoraLlegadaAsc(
                        medicoId,
                        "EVALUADO_PENDIENTE_CIERRE"
                );
    }


    // ==========================================================
    // BUSCAR CITA
    // ==========================================================

    public Cita obtenerCita(Long citaId) {

        return citaRepository
                .findById(citaId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "La cita no existe."
                        )
                );
    }


    // ==========================================================
    // INICIAR CONSULTA
    // ==========================================================

    @Transactional
    public Cita iniciarConsulta(
            Long citaId,
            Long medicoId
    ) {

        Cita cita = obtenerCita(citaId);

        validarMedicoDeCita(
                cita,
                medicoId
        );


        if (!"LISTO_CONSULTA".equalsIgnoreCase(
                cita.getEstado()
        )) {

            throw new IllegalStateException(
                    "La cita no se encuentra en espera de consulta."
            );
        }


        cita.setEstado(
                "CONSULTA_MEDICA"
        );


        return citaRepository.save(
                cita
        );
    }


    // ==========================================================
    // OBTENER O CREAR CONSULTA MEDICA
    // ==========================================================

    @Transactional
    public ConsultaMedica obtenerOCrearConsulta(
            Long citaId,
            Usuario medico
    ) {

        Cita cita = obtenerCita(citaId);

        validarMedicoDeCita(
                cita,
                medico.getId()
        );


        if (!"CONSULTA_MEDICA".equalsIgnoreCase(
                cita.getEstado()
        )) {

            throw new IllegalStateException(
                    "La cita no se encuentra en consulta médica."
            );
        }


        return consultaRepository
                .findByCitaId(citaId)
                .orElseGet(
                        () -> {

                            ConsultaMedica consulta =
                                    new ConsultaMedica();

                            consulta.setCita(
                                    cita
                            );

                            consulta.setMedico(
                                    medico
                            );

                            consulta.setEstado(
                                    "EN_CURSO"
                            );

                            consulta.setFechaInicio(
                                    LocalDateTime.now()
                            );

                            /*
                             * motivo_visita es NOT NULL en la BD.
                             *
                             * No guardamos todavía la consulta,
                             * porque el médico aún no ha escrito
                             * el motivo de visita.
                             */

                            return consulta;
                        }
                );
    }


    // ==========================================================
    // BUSCAR CONSULTA EXISTENTE
    // ==========================================================

    public ConsultaMedica obtenerConsultaPorCita(
            Long citaId
    ) {

        return consultaRepository
                .findByCitaId(citaId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "No existe una consulta médica para esta cita."
                        )
                );
    }


    // ==========================================================
    // GUARDAR CONSULTA
    // ==========================================================

    @Transactional
    public ConsultaMedica guardarConsulta(
            Long citaId,
            Long medicoId,
            String motivoVisita,
            String hallazgosClinicos,
            String codigoCie10,
            String diagnostico,
            String planTratamiento,
            String notasAdicionales,
            String estado
    ) {

        Cita cita = obtenerCita(citaId);

        validarMedicoDeCita(
                cita,
                medicoId
        );


        if (!"CONSULTA_MEDICA".equalsIgnoreCase(
                cita.getEstado()
        )) {

            throw new IllegalStateException(
                    "La cita no se encuentra en consulta médica."
            );
        }


        String motivo =
                limpiarObligatorio(
                        motivoVisita,
                        "El motivo de visita es obligatorio."
                );


        String estadoConsulta =
                estado == null
                        ? "EN_CURSO"
                        : estado.trim().toUpperCase();


        if (
                !"EN_CURSO".equals(estadoConsulta)
                        &&
                        !"FINALIZADA".equals(estadoConsulta)
        ) {

            throw new IllegalArgumentException(
                    "El estado de la consulta no es válido."
            );
        }


        /*
         * FA05
         *
         * No permitir finalizar una consulta
         * sin diagnóstico.
         */

        if (
                "FINALIZADA".equals(estadoConsulta)
                        &&
                        (
                                diagnostico == null
                                        ||
                                        diagnostico.trim().isEmpty()
                        )
        ) {

            throw new IllegalArgumentException(
                    "No es posible finalizar la consulta sin registrar un diagnóstico. "
                            +
                            "El campo Diagnóstico es obligatorio."
            );
        }


        ConsultaMedica consulta =
                consultaRepository
                        .findByCitaId(citaId)
                        .orElseGet(
                                () -> {

                                    ConsultaMedica nueva =
                                            new ConsultaMedica();

                                    nueva.setCita(
                                            cita
                                    );

                                    nueva.setMedico(
                                            cita.getMedico()
                                    );

                                    nueva.setFechaInicio(
                                            LocalDateTime.now()
                                    );

                                    return nueva;
                                }
                        );


        consulta.setMotivoVisita(
                motivo
        );

        consulta.setHallazgosClinicos(
                limpiarOpcional(
                        hallazgosClinicos
                )
        );

        consulta.setCodigoCie10(
                limpiarOpcional(
                        codigoCie10
                )
        );

        consulta.setDiagnostico(
                limpiarOpcional(
                        diagnostico
                )
        );

        consulta.setPlanTratamiento(
                limpiarOpcional(
                        planTratamiento
                )
        );

        consulta.setNotasAdicionales(
                limpiarOpcional(
                        notasAdicionales
                )
        );

        consulta.setEstado(
                estadoConsulta
        );


        // ==========================================
        // SI FINALIZA LA CONSULTA
        // ==========================================

        if ("FINALIZADA".equals(
                estadoConsulta
        )) {

            consulta.setFechaFinalizacion(
                    LocalDateTime.now()
            );


            cita.setEstado(
                    "EVALUADO_PENDIENTE_CIERRE"
            );

            citaRepository.save(
                    cita
            );

        } else {

            consulta.setFechaFinalizacion(
                    null
            );
        }


        return consultaRepository.save(
                consulta
        );
    }


    // ==========================================================
    // PACIENTE NO ASISTIO
    // ==========================================================

    @Transactional
    public Cita marcarNoAsistio(
            Long citaId,
            Long medicoId
    ) {

        Cita cita = obtenerCita(citaId);

        validarMedicoDeCita(
                cita,
                medicoId
        );


        if (!"LISTO_CONSULTA".equalsIgnoreCase(
                cita.getEstado()
        )) {

            throw new IllegalStateException(
                    "Solamente una cita en espera puede marcarse como No Asistió."
            );
        }


        cita.setEstado(
                "NO_ASISTIO"
        );


        return citaRepository.save(
                cita
        );
    }


    // ==========================================================
    // FINALIZAR ATENCION
    // ==========================================================

    @Transactional
    public Cita finalizarAtencion(
            Long citaId,
            Long medicoId
    ) {

        Cita cita = obtenerCita(citaId);

        validarMedicoDeCita(
                cita,
                medicoId
        );


        if (!"EVALUADO_PENDIENTE_CIERRE"
                .equalsIgnoreCase(
                        cita.getEstado()
                )) {

            throw new IllegalStateException(
                    "La cita todavía no está lista para finalizar la atención."
            );
        }


        ConsultaMedica consulta =
                obtenerConsultaPorCita(
                        citaId
                );


        if (!"FINALIZADA".equalsIgnoreCase(
                consulta.getEstado()
        )) {

            throw new IllegalStateException(
                    "La consulta médica todavía no ha sido finalizada."
            );
        }


        cita.setEstado(
                "ATENCION_FINALIZADA"
        );


        return citaRepository.save(
                cita
        );
    }


    // ==========================================================
    // VALIDAR QUE EL MEDICO SEA EL ASIGNADO
    // ==========================================================

    private void validarMedicoDeCita(
            Cita cita,
            Long medicoId
    ) {

        if (
                cita.getMedico() == null
                        ||
                        cita.getMedico().getId() == null
                        ||
                        !cita.getMedico()
                                .getId()
                                .equals(medicoId)
        ) {

            throw new IllegalStateException(
                    "La cita no pertenece al médico autenticado."
            );
        }
    }


    // ==========================================================
    // LIMPIAR CAMPO OBLIGATORIO
    // ==========================================================

    private String limpiarObligatorio(
            String texto,
            String mensaje
    ) {

        if (
                texto == null
                        ||
                        texto.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    mensaje
            );
        }

        return texto.trim();
    }


    // ==========================================================
    // LIMPIAR CAMPO OPCIONAL
    // ==========================================================

    private String limpiarOpcional(
            String texto
    ) {

        if (
                texto == null
                        ||
                        texto.trim().isEmpty()
        ) {

            return null;
        }

        return texto.trim();
    }
}
