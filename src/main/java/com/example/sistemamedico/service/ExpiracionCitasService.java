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


    public ExpiracionCitasService(
            Cita_Repositorio citaRepository,
            HorarioMedico_Repositorio horarioMedicoRepository
    ) {

        this.citaRepository =
                citaRepository;

        this.horarioMedicoRepository =
                horarioMedicoRepository;
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
}