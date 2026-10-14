
package com.example.sistemamedico;

import com.example.sistemamedico.service.AgendamientoCitaService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AgendaMedicaRegistroCitaTest {

    @Autowired
    private AgendamientoCitaService agendamientoService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // =====================================================
    // DATOS DE PRUEBA CU-16
    // =====================================================

    private static final Long MEDICO_ID = 14L;
    private static final Long SUCURSAL_ID = 2L;
    private static final Long ESPECIALIDAD_ID = 2L;
    private static final Long HORARIO_BLOQUEADO_ID = 1868L;
    private static final Long CONSULTA_PADRE_ID = 5L;

    // Paciente existente usado para cita normal.
    private static final Long PACIENTE_ID = 10L;

    // =====================================================
    // CONTAR CITAS QUE UTILIZAN EL HORARIO
    // =====================================================

    private Long contarCitasDelHorario() {

        return jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM public.citas
                WHERE horario_id = ?
                """,
                Long.class,
                HORARIO_BLOQUEADO_ID
        );
    }

    // =====================================================
    // CONSULTAR ESTADO ACTUAL DEL HORARIO
    // =====================================================

    private String obtenerEstadoHorario() {

        return jdbcTemplate.queryForObject(
                """
                SELECT estado
                FROM public.horarios_medicos
                WHERE id = ?
                """,
                String.class,
                HORARIO_BLOQUEADO_ID
        );
    }

    // =====================================================
    // TEST 1
    // RECHAZAR CITA NORMAL EN HORARIO BLOQUEADO
    // =====================================================

    @Test
    void debeRechazarCitaNormalEnHorarioBloqueado() {

        Long citasAntes = contarCitasDelHorario();

        String estadoAntes = obtenerEstadoHorario();

        assertEquals(
                "DISPONIBLE",
                estadoAntes
        );

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> agendamientoService.registrarCita(
                        PACIENTE_ID,
                        SUCURSAL_ID,
                        ESPECIALIDAD_ID,
                        MEDICO_ID,
                        HORARIO_BLOQUEADO_ID,
                        "Consulta médica de prueba para validar CU16",
                        "PORTAL"
                )
        );

        assertTrue(
                error.getMessage().contains(
                        "coincide con un evento"
                )
        );

        assertEquals(
                citasAntes,
                contarCitasDelHorario(),
                "No debe crearse una nueva cita."
        );

        assertEquals(
                estadoAntes,
                obtenerEstadoHorario(),
                "No debe cambiar el estado del horario."
        );
    }

    // =====================================================
    // TEST 2
    // RECHAZAR CITA DE SEGUIMIENTO BLOQUEADA
    // =====================================================

    @Test
    void debeRechazarSeguimientoEnHorarioBloqueado() {

        Long citasAntes = contarCitasDelHorario();

        String estadoAntes = obtenerEstadoHorario();

        assertEquals(
                "DISPONIBLE",
                estadoAntes
        );

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> agendamientoService.registrarCitaSeguimiento(
                        CONSULTA_PADRE_ID,
                        HORARIO_BLOQUEADO_ID,
                        "MONITOREO_TRATAMIENTO",
                        "Seguimiento de prueba para validar bloqueo CU16",
                        "NORMAL",
                        MEDICO_ID
                )
        );


        System.out.println(
                "CU16 - ERROR REAL DE SEGUIMIENTO: "
                        + error.getMessage()
        );

        assertTrue(
                error.getMessage() != null
                        && error.getMessage().contains(
                        "coincide con un evento"
                ),
                "Se esperaba un bloqueo de agenda, pero ocurrió: "
                        + error.getMessage()
        );


        assertEquals(
                citasAntes,
                contarCitasDelHorario(),
                "No debe crearse una cita de seguimiento."
        );

        assertEquals(
                estadoAntes,
                obtenerEstadoHorario(),
                "El horario debe permanecer disponible."
        );
    }
}
