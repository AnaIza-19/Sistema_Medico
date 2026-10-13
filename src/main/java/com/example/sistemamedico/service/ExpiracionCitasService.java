package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.Cita_Repositorio;
import com.example.sistemamedico.Repositorio.HorarioMedico_Repositorio;
import com.example.sistemamedico.model.Cita;
import com.example.sistemamedico.model.HorarioMedico;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class ExpiracionCitasService {

    private final Cita_Repositorio citaRepository;

    private final HorarioMedico_Repositorio horarioMedicoRepository;
    private final CorreoService correoService;

    public ExpiracionCitasService(
            Cita_Repositorio citaRepository,
            HorarioMedico_Repositorio horarioMedicoRepository,
            CorreoService correoService
    ) {

        this.citaRepository =
                citaRepository;

        this.horarioMedicoRepository =
                horarioMedicoRepository;

        this.correoService =
                correoService;
    }

    // =====================================================
    // CU-06 FA03
    //
    // REVISAR AUTOMÁTICAMENTE LAS CITAS DEL PORTAL
    // QUE YA SUPERARON LOS 10 MINUTOS SIN PAGO
    //
    // Se ejecuta cada 30 segundos.
    // =====================================================

    @Scheduled(
            fixedRate = 30000
    )
    @Transactional
    public void cancelarCitasPortalVencidas() {

        LocalDateTime ahora =
                LocalDateTime.now();


        List<Cita> citasVencidas =
                citaRepository
                        .buscarCitasPortalVencidas(
                                ahora
                        );


        if (citasVencidas.isEmpty()) {

            return;
        }


        for (Cita cita : citasVencidas) {

            // =================================================
            // VALIDACIÓN EXTRA
            // =================================================

            if (
                    cita.getOrigen() == null
                            ||
                            !"PORTAL".equalsIgnoreCase(
                                    cita.getOrigen()
                            )
            ) {

                continue;
            }


            if (
                    cita.getEstado() == null
                            ||
                            !"PENDIENTE_PAGO".equalsIgnoreCase(
                                    cita.getEstado()
                            )
            ) {

                continue;
            }


            HorarioMedico horario =
                    cita.getHorario();


            if (horario == null) {

                continue;
            }


            if (
                    horario.getReservadoHasta() == null
                            ||
                            !ahora.isAfter(
                                    horario.getReservadoHasta()
                            )
            ) {

                continue;
            }


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
            // EXPIRAR CITA
            // =================================================

            cita.setEstado(
                    "EXPIRADA"
            );


            citaRepository.save(
                    cita
            );


            System.out.println(
                    "CU-06 FA03 - Cita "
                            + cita.getId()
                            + " expirada automáticamente."
            );
        }
    }
    // =====================================================
// CU-12
// ENVIAR RECORDATORIOS DE CITAS DE SEGUIMIENTO
//
// Se ejecuta cada 60 segundos.
//
// - Busca recordatorios cuya fecha ya llegó.
// - Solo procesa citas CONFIRMADAS.
// - Envía el correo.
// - Marca recordatorio_enviado = true.
// - Si falla el correo, queda en false para reintentar.
// =====================================================

    @Scheduled(
            fixedRate = 60000
    )
    @Transactional
    public void enviarRecordatoriosSeguimiento() {

        LocalDateTime ahora =
                LocalDateTime.now();


        List<Cita> recordatoriosPendientes =
                citaRepository
                        .buscarRecordatoriosSeguimientoPendientes(
                                ahora
                        );


        if (recordatoriosPendientes.isEmpty()) {

            return;
        }


        for (Cita cita : recordatoriosPendientes) {

            // =================================================
            // VALIDACIÓN EXTRA
            // NO ENVIAR SI LA CITA FUE CANCELADA
            // =================================================

            if (
                    cita.getEstado() == null
                            ||
                            !"CONFIRMADA".equalsIgnoreCase(
                                    cita.getEstado()
                            )
            ) {

                continue;
            }


            // =================================================
            // EVITAR ENVÍO DUPLICADO
            // =================================================

            if (
                    Boolean.TRUE.equals(
                            cita.getRecordatorioEnviado()
                    )
            ) {

                continue;
            }


            try {

                // =============================================
                // ENVIAR CORREO
                // =============================================

                correoService
                        .enviarRecordatorioSeguimiento(
                                cita
                        );


                // =============================================
                // MARCAR COMO ENVIADO
                // =============================================

                cita.setRecordatorioEnviado(
                        true
                );


                citaRepository.save(
                        cita
                );


                System.out.println(
                        "CU-12 - Recordatorio enviado para la cita "
                                + cita.getId()
                                + "."
                );

            } catch (Exception e) {

                // =============================================
                // SI FALLA, NO SE MARCA COMO ENVIADO
                // PARA QUE PUEDA REINTENTARSE
                // =============================================

                System.err.println(
                        "CU-12 - Error al enviar recordatorio "
                                + "de la cita "
                                + cita.getId()
                                + "."
                );


                System.err.println(
                        "Detalle: "
                                + e.getMessage()
                );
            }
        }
    }
}