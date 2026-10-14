
package com.example.sistemamedico.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class AgendaMedicaBloqueoService {

    private final JdbcTemplate jdbcTemplate;

    public AgendaMedicaBloqueoService(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // =====================================================
    // CU-16: COMPROBAR CONFLICTO CON EVENTOS MÉDICOS
    // =====================================================

    @Transactional(readOnly = true)
    public boolean existeConflicto(
            Long medicoId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin
    ) {

        if (medicoId == null
                || fecha == null
                || horaInicio == null
                || horaFin == null) {

            throw new IllegalArgumentException(
                    "Los datos del horario son obligatorios."
            );
        }

        LocalDateTime inicio =
                LocalDateTime.of(fecha, horaInicio);

        LocalDateTime fin =
                LocalDateTime.of(fecha, horaFin);

        if (!fin.isAfter(inicio)) {
            throw new IllegalArgumentException(
                    "La hora de fin debe ser posterior "
                            + "a la hora de inicio."
            );
        }

        // Se utiliza el intervalo [inicio, fin).
        // Si un evento termina exactamente cuando
        // comienza la cita, no existe conflicto.

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM public.agenda_medica_eventos e
                    WHERE e.medico_id = ?
                      AND e.fecha_inicio < ?
                      AND e.fecha_fin > ?
                )
                """;

        Boolean resultado = jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                medicoId,
                fin,
                inicio
        );

        return Boolean.TRUE.equals(resultado);
    }

    // =====================================================
    // CU-16: IMPEDIR RESERVAR HORARIO BLOQUEADO
    // =====================================================

    @Transactional(readOnly = true)
    public void validarHorario(
            Long medicoId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin
    ) {

        if (existeConflicto(
                medicoId,
                fecha,
                horaInicio,
                horaFin
        )) {

            throw new IllegalStateException(
                    "El horario seleccionado coincide "
                            + "con un evento de la agenda médica. "
                            + "Seleccione otro horario."
            );
        }
    }
}
