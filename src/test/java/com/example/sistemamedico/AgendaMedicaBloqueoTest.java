
package com.example.sistemamedico;

import com.example.sistemamedico.service.AgendaMedicaBloqueoService;
import com.example.sistemamedico.service.AgendamientoCitaService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AgendaMedicaBloqueoTest {

    @Autowired
    private AgendaMedicaBloqueoService bloqueoService;

    @Autowired
    private AgendamientoCitaService agendamientoService;

    @Test
    void debeDetectarHorarioBloqueado() {

        boolean bloqueado =
                bloqueoService.existeConflicto(
                        14L,
                        java.time.LocalDate.of(2026, 10, 15),
                        java.time.LocalTime.of(9, 0),
                        java.time.LocalTime.of(9, 30)
                );

        assertTrue(
                bloqueado,
                "El horario 09:00-09:30 debe estar bloqueado."
        );
    }

    @Test
    void debeRechazarHorarioBloqueado() {

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () -> bloqueoService.validarHorario(
                                14L,
                                java.time.LocalDate.of(2026, 10, 15),
                                java.time.LocalTime.of(9, 0),
                                java.time.LocalTime.of(9, 30)
                        )
                );

        assertTrue(
                error.getMessage().contains(
                        "coincide con un evento"
                )
        );
    }

    @Test
    void debePermitirHorarioSinConflicto() {

        assertDoesNotThrow(
                () -> bloqueoService.validarHorario(
                        14L,
                        java.time.LocalDate.of(2026, 10, 15),
                        java.time.LocalTime.of(8, 0),
                        java.time.LocalTime.of(8, 30)
                )
        );
    }
}
