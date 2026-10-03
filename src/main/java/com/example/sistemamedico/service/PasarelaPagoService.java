package com.example.sistemamedico.service;

import org.springframework.stereotype.Service;

@Service
public class PasarelaPagoService {

    // =====================================================
    // RESULTADOS POSIBLES DE LA PASARELA
    // =====================================================

    public enum EstadoPasarela {

        APROBADO,
        RECHAZADO,
        ERROR_PROCESAMIENTO,
        ERROR_COMUNICACION
    }


    // =====================================================
    // PROCESAR TRANSACCIÓN SIMULADA
    // =====================================================

    public EstadoPasarela procesar(
            String numeroTarjeta
    ) {

        String numero =
                numeroTarjeta == null
                        ? ""
                        : numeroTarjeta.replaceAll(
                        "\\s+",
                        ""
                );


        // =================================================
        // RECHAZO BANCARIO
        // =================================================

        if (
                numero.equals(
                        "4000000000000002"
                )
        ) {

            return EstadoPasarela.RECHAZADO;
        }


        // =================================================
        // ERROR DE PROCESAMIENTO
        // =================================================

        if (
                numero.equals(
                        "4000000000000069"
                )
        ) {

            return EstadoPasarela.ERROR_PROCESAMIENTO;
        }


        // =================================================
        // ERROR DE COMUNICACIÓN
        // =================================================

        if (
                numero.equals(
                        "4000000000009995"
                )
        ) {

            return EstadoPasarela.ERROR_COMUNICACION;
        }


        // =================================================
        // PAGO APROBADO
        // =================================================

        return EstadoPasarela.APROBADO;
    }
}