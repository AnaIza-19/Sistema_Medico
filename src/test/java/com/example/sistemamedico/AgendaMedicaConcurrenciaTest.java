
package com.example.sistemamedico;

import com.example.sistemamedico.service.AgendaMedicaConcurrenciaService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AgendaMedicaConcurrenciaTest {

    @Autowired
    private AgendaMedicaConcurrenciaService concurrenciaService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private static final Long MEDICO_ID = 14L;

    // =====================================================
    // CU-16: DOS TRANSACCIONES INTENTAN BLOQUEAR
    // AL MISMO MÉDICO
    // =====================================================

    @Test
    @Timeout(30)
    void debeSerializarOperacionesDelMismoMedico()
            throws Exception {

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch primerBloqueoAdquirido =
                new CountDownLatch(1);

        CountDownLatch liberarPrimerBloqueo =
                new CountDownLatch(1);

        CountDownLatch segundaOperacionIniciada =
                new CountDownLatch(1);

        TransactionTemplate transacciones =
                new TransactionTemplate(transactionManager);

        Future<Boolean> primera = null;
        Future<Boolean> segunda = null;

        try {

            // =============================================
            // TRANSACCIÓN 1
            // OBTIENE Y MANTIENE EL BLOQUEO
            // =============================================

            primera = executor.submit(() ->

                    transacciones.execute(status -> {

                        concurrenciaService.bloquearMedico(
                                MEDICO_ID
                        );

                        primerBloqueoAdquirido.countDown();

                        try {

                            if (!liberarPrimerBloqueo.await(
                                    10,
                                    TimeUnit.SECONDS
                            )) {

                                throw new IllegalStateException(
                                        "La primera operación "
                                                + "no fue liberada."
                                );
                            }

                        } catch (InterruptedException e) {

                            Thread.currentThread().interrupt();

                            throw new IllegalStateException(
                                    "Prueba interrumpida.",
                                    e
                            );
                        }

                        return true;
                    })
            );

            // =============================================
            // COMPROBAR PRIMER BLOQUEO
            // =============================================

            assertTrue(
                    primerBloqueoAdquirido.await(
                            5,
                            TimeUnit.SECONDS
                    ),
                    "La primera transacción "
                            + "no adquirió el bloqueo."
            );

            // =============================================
            // TRANSACCIÓN 2
            // INTENTA BLOQUEAR AL MISMO MÉDICO
            // =============================================

            segunda = executor.submit(() ->

                    transacciones.execute(status -> {

                        segundaOperacionIniciada.countDown();

                        concurrenciaService.bloquearMedico(
                                MEDICO_ID
                        );

                        return true;
                    })
            );

            assertTrue(
                    segundaOperacionIniciada.await(
                            5,
                            TimeUnit.SECONDS
                    ),
                    "La segunda transacción no inició."
            );

            // =============================================
            // VERIFICAR QUE LA SEGUNDA NO TERMINE
            // MIENTRAS LA PRIMERA MANTIENE EL BLOQUEO
            // =============================================

            Thread.sleep(500);

            assertFalse(
                    segunda.isDone(),
                    "La segunda operación terminó "
                            + "antes de liberar el bloqueo."
            );

            // =============================================
            // LIBERAR LA PRIMERA TRANSACCIÓN
            // =============================================

            liberarPrimerBloqueo.countDown();

            assertEquals(
                    Boolean.TRUE,
                    primera.get(
                            8,
                            TimeUnit.SECONDS
                    ),
                    "La primera transacción debe terminar."
            );

            assertEquals(
                    Boolean.TRUE,
                    segunda.get(
                            8,
                            TimeUnit.SECONDS
                    ),
                    "La segunda transacción debe continuar "
                            + "después de liberar el bloqueo."
            );

            System.out.println(
                    "CU16: BLOQUEO COMPARTIDO CORRECTO."
            );

            System.out.println(
                    "CU16: DOS TRANSACCIONES SERIALIZADAS."
            );

        } finally {

            // Siempre liberar el bloqueo, incluso
            // si alguna comprobación falla.

            liberarPrimerBloqueo.countDown();

            if (primera != null) {
                primera.cancel(true);
            }

            if (segunda != null) {
                segunda.cancel(true);
            }

            executor.shutdownNow();
        }
    }
}
