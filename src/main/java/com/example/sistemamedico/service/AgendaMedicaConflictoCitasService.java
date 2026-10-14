
package com.example.sistemamedico.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AgendaMedicaConflictoCitasService {

    private final JdbcTemplate jdbcTemplate;

    public AgendaMedicaConflictoCitasService(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // =====================================================
    // CU-16: DETECTAR CITAS QUE COINCIDEN CON UN EVENTO
    // =====================================================

    @Transactional(readOnly = true)
    public boolean existeConflicto(
            Long medicoId,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    ) {

        if (medicoId == null
                || fechaInicio == null
                || fechaFin == null
                || !fechaFin.isAfter(fechaInicio)) {

            throw new IllegalArgumentException(
                    "Los datos del evento no son válidos."
            );
        }

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM public.citas c
                    INNER JOIN public.horarios_medicos h
                        ON h.id = c.horario_id
                    WHERE c.medico_id = ?
                      AND h.medico_id = c.medico_id
                      AND UPPER(TRIM(c.estado)) NOT IN (
                          'PENDIENTE',
                          'NO_ASISTIO',
                          'CANCELADA',
                          'EXPIRADA'
                      )
                      AND (h.fecha + h.hora_inicio) < ?
                      AND (h.fecha + h.hora_fin) > ?
                )
                """;

        Boolean resultado = jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                medicoId,
                fechaFin,
                fechaInicio
        );

        return Boolean.TRUE.equals(resultado);
    }

    // =====================================================
    // CU-16: VALIDAR ANTES DE GUARDAR EVENTO
    // =====================================================

    @Transactional(readOnly = true)
    public void validarSinConflicto(
            Long medicoId,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    ) {

        if (existeConflicto(
                medicoId,
                fechaInicio,
                fechaFin
        )) {

            throw new IllegalStateException(
                    "No se puede guardar el evento porque "
                            + "coincide con una cita médica programada. "
                            + "Seleccione otro horario."
            );
        }
    }
}
