
package com.example.sistemamedico.controller;

import com.example.sistemamedico.service.AgendaMedicaSeguridadService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/medico/agenda/api")
public class AgendaMedicaCitasController {

    // =====================================================
    // CU-16: DEPENDENCIAS
    // =====================================================

    private final JdbcTemplate jdbcTemplate;

    private final AgendaMedicaSeguridadService seguridadService;

    public AgendaMedicaCitasController(
            JdbcTemplate jdbcTemplate,
            AgendaMedicaSeguridadService seguridadService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.seguridadService = seguridadService;
    }

    // =====================================================
    // CU-16: CONSULTAR CITAS DEL MÉDICO
    // PERMISO: medical-agenda/read
    // =====================================================

    @GetMapping("/citas")
    public List<CitaAgendaRespuesta> consultarCitas(

            @RequestParam LocalDate inicio,

            @RequestParam LocalDate fin,

            HttpSession session
    ) {

        // 1. VALIDAR SESIÓN, ROL Y PERMISO

        Long medicoId =
                seguridadService.obtenerMedicoId(
                        session,
                        "read"
                );

        // 2. VALIDAR RANGO DE FECHAS

        if (inicio == null
                || fin == null
                || !fin.isAfter(inicio)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El período de consulta es inválido."
            );
        }

        if (fin.isAfter(inicio.plusYears(1))) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El período no puede superar un año."
            );
        }

        // =================================================
        // 3. CONSULTAR CITAS Y HORARIOS
        //
        // - Solo citas del médico autenticado.
        // - Se excluyen estados no visibles.
        // - Se conservan citas con horarios iguales.
        // - No se modifica ningún registro.
        // =================================================

        String sql = """
                SELECT
                    c.id AS cita_id,
                    c.paciente_id,
                    p.nombre AS paciente_nombre,
                    p.apellido AS paciente_apellido,
                    c.estado,
                    c.motivo_consulta,
                    h.fecha,
                    h.hora_inicio,
                    h.hora_fin
                FROM public.citas c
                INNER JOIN public.horarios_medicos h
                    ON h.id = c.horario_id
                INNER JOIN public.usuarios p
                    ON p.id = c.paciente_id
                WHERE c.medico_id = ?
                  AND h.medico_id = c.medico_id
                  AND h.fecha >= ?
                  AND h.fecha < ?
                  AND UPPER(TRIM(c.estado)) NOT IN (
                      'PENDIENTE',
                      'PENDIENTE_PAGO',
                      'NO_ASISTIO',
                      'CANCELADA'
                  )
                ORDER BY
                    h.fecha ASC,
                    h.hora_inicio ASC,
                    c.id ASC
                """;

        return jdbcTemplate.query(
                sql,

                (rs, numeroFila) -> {

                    LocalDate fecha =
                            rs.getObject(
                                    "fecha",
                                    LocalDate.class
                            );

                    LocalTime horaInicio =
                            rs.getObject(
                                    "hora_inicio",
                                    LocalTime.class
                            );

                    LocalTime horaFin =
                            rs.getObject(
                                    "hora_fin",
                                    LocalTime.class
                            );

                    String estado =
                            rs.getString("estado");

                    String nombrePaciente =
                            rs.getString("paciente_nombre")
                                    + " "
                                    + rs.getString("paciente_apellido");

                    return new CitaAgendaRespuesta(
                            rs.getLong("cita_id"),
                            rs.getLong("paciente_id"),
                            nombrePaciente.trim(),
                            estado,
                            rs.getString("motivo_consulta"),
                            LocalDateTime.of(
                                    fecha,
                                    horaInicio
                            ),
                            LocalDateTime.of(
                                    fecha,
                                    horaFin
                            ),
                            colorPorEstado(estado)
                    );
                },

                medicoId,
                Date.valueOf(inicio),
                Date.valueOf(fin)
        );
    }

    // =====================================================
    // CU-16: COLORES DE CITAS SEGÚN SU ESTADO
    // =====================================================

    private String colorPorEstado(String estado) {

        if (estado == null) {
            return "#64748b";
        }

        String codigo =
                estado.trim().toUpperCase(Locale.ROOT);

        return switch (codigo) {

            case "CONFIRMADA", "PAGADA" ->
                    "#2563eb";

            case "PACIENTE_PRESENTE",
                 "SIGNOS_VITALES",
                 "LISTO_CONSULTA",
                 "EN_ESPERA" ->
                    "#0891b2";

            case "CONSULTA_MEDICA" ->
                    "#d97706";

            case "EVALUADO",
                 "EVALUADO_PENDIENTE_CIERRE" ->
                    "#7c3aed";

            case "ATENCION_FINALIZADA" ->
                    "#16a34a";

            case "EXPIRADA" ->
                    "#64748b";

            default ->
                    "#475569";
        };
    }

    // =====================================================
    // CU-16: RESPUESTA PARA EL CALENDARIO
    // =====================================================

    public record CitaAgendaRespuesta(
            Long id,
            Long pacienteId,
            String paciente,
            String estado,
            String motivoConsulta,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            String color
    ) {
    }
}
