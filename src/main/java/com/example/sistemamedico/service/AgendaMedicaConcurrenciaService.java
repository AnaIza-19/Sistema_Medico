
package com.example.sistemamedico.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AgendaMedicaConcurrenciaService {

    private final JdbcTemplate jdbcTemplate;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public AgendaMedicaConcurrenciaService(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // =====================================================
    // CU-16: BLOQUEAR MÉDICO DURANTE OPERACIONES DE AGENDA
    //
    // El bloqueo se aplica sobre la fila del médico
    // en public.usuarios utilizando SELECT FOR UPDATE.
    //
    // IMPORTANTE:
    // Debe ejecutarse dentro de una transacción existente.
    // =====================================================

    @Transactional(propagation = Propagation.MANDATORY)
    public void bloquearMedico(Long medicoId) {

        // =================================================
        // 1. VALIDAR IDENTIFICADOR DEL MÉDICO
        // =================================================

        if (medicoId == null || medicoId <= 0) {

            throw new IllegalArgumentException(
                    "No se identificó al médico."
            );
        }

        // =================================================
        // 2. BLOQUEAR FILA DEL MÉDICO EN POSTGRESQL
        // =================================================

        String sql = """
                SELECT id
                FROM public.usuarios
                WHERE id = ?
                FOR UPDATE
                """;

        List<Long> resultados =
                jdbcTemplate.queryForList(
                        sql,
                        Long.class,
                        medicoId
                );

        // =================================================
        // 3. VERIFICAR QUE EL MÉDICO EXISTA
        // =================================================

        if (resultados.isEmpty()) {

            throw new IllegalArgumentException(
                    "El médico no existe."
            );
        }

        // =================================================
        // 4. BLOQUEO ADQUIRIDO
        // =================================================

        // PostgreSQL mantendrá el bloqueo hasta que
        // termine la transacción que llamó este método.
    }
}
