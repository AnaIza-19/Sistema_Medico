package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.Cita_Repositorio;
import com.example.sistemamedico.Repositorio.HorarioMedico_Repositorio;
import com.example.sistemamedico.Repositorio.Pago_Repositorio;
import com.example.sistemamedico.Repositorio.Usuario_Repositorio;

import com.example.sistemamedico.model.Cita;
import com.example.sistemamedico.model.HorarioMedico;
import com.example.sistemamedico.model.Pago;
import com.example.sistemamedico.model.Usuario;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class CajaService {

    private final Cita_Repositorio citaRepository;
    private final Usuario_Repositorio usuarioRepository;
    private final Pago_Repositorio pagoRepository;
    private final HorarioMedico_Repositorio horarioMedicoRepository;


    public CajaService(
            Cita_Repositorio citaRepository,
            Usuario_Repositorio usuarioRepository,
            Pago_Repositorio pagoRepository,
            HorarioMedico_Repositorio horarioMedicoRepository
    ) {

        this.citaRepository =
                citaRepository;

        this.usuarioRepository =
                usuarioRepository;

        this.pagoRepository =
                pagoRepository;

        this.horarioMedicoRepository =
                horarioMedicoRepository;
    }


    // =====================================================
    // BUSCAR CITA PENDIENTE POR NÚMERO
    // =====================================================

    public Optional<Cita> buscarPendientePorNumeroCita(
            Long citaId
    ) {

        if (citaId == null) {
            return Optional.empty();
        }


        return citaRepository
                .findById(citaId)
                .filter(
                        cita ->
                                cita.getEstado() != null
                                        &&
                                        "PENDIENTE_PAGO".equalsIgnoreCase(
                                                cita.getEstado()
                                        )
                );
    }


    // =====================================================
    // BUSCAR CITAS PENDIENTES POR DPI
    // =====================================================

    public List<Cita> buscarPendientesPorDpi(
            String dpi
    ) {

        String dpiLimpio =
                dpi == null
                        ? ""
                        : dpi.trim();


        if (dpiLimpio.isBlank()) {
            return List.of();
        }


        Usuario paciente =
                usuarioRepository
                        .findByDpi(
                                dpiLimpio
                        )
                        .orElse(null);


        if (paciente == null) {
            return List.of();
        }


        if (
                paciente.getRol() == null
                        ||
                        !"PACIENTE".equalsIgnoreCase(
                                paciente.getRol().getNombre()
                        )
        ) {

            return List.of();
        }


        return citaRepository
                .findByPacienteIdOrderByFechaCreacionDesc(
                        paciente.getId()
                )
                .stream()
                .filter(
                        cita ->
                                cita.getEstado() != null
                                        &&
                                        "PENDIENTE_PAGO".equalsIgnoreCase(
                                                cita.getEstado()
                                        )
                )
                .toList();
    }


    // =====================================================
    // OBTENER MONTO
    // =====================================================

    public BigDecimal obtenerMonto(
            Cita cita
    ) {

        if (
                cita == null
                        ||
                        cita.getEspecialidad() == null
                        ||
                        cita.getEspecialidad().getPrecio() == null
        ) {

            throw new IllegalStateException(
                    "La especialidad no tiene un precio configurado."
            );
        }


        return cita
                .getEspecialidad()
                .getPrecio();
    }


    // =====================================================
    // PROCESAR PAGO EN EFECTIVO
    // =====================================================

    @Transactional
    public Pago procesarPagoEfectivo(
            Long citaId,
            BigDecimal montoRecibido
    ) {

        Cita cita =
                citaRepository
                        .findById(
                                citaId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Cita no encontrada."
                                        )
                        );


        if (
                cita.getEstado() == null
                        ||
                        !"PENDIENTE_PAGO".equalsIgnoreCase(
                                cita.getEstado()
                        )
        ) {

            throw new IllegalStateException(
                    "La cita ya no se encuentra pendiente de pago."
            );
        }


        if (
                pagoRepository
                        .findByCitaId(
                                citaId
                        )
                        .isPresent()
        ) {

            throw new IllegalStateException(
                    "Ya existe un pago registrado para esta cita."
            );
        }


        BigDecimal montoTotal =
                obtenerMonto(
                        cita
                );


        if (montoRecibido == null) {

            throw new IllegalArgumentException(
                    "Debe ingresar el monto recibido."
            );
        }


        if (
                montoRecibido.compareTo(
                        BigDecimal.ZERO
                ) <= 0
        ) {

            throw new IllegalArgumentException(
                    "El monto recibido debe ser mayor a cero."
            );
        }


        if (
                montoRecibido.compareTo(
                        montoTotal
                ) < 0
        ) {

            throw new IllegalArgumentException(
                    "El monto recibido (Q"
                            + montoRecibido
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            )
                            + ") es menor al monto a cobrar (Q"
                            + montoTotal
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            )
                            + ")."
            );
        }


        BigDecimal cambio =
                montoRecibido.subtract(
                        montoTotal
                );


        Pago pago =
                new Pago();


        pago.setCita(
                cita
        );


        pago.setNumeroTransaccion(
                generarNumeroTransaccion()
        );


        pago.setMonto(
                montoTotal
        );


        pago.setMetodoPago(
                "EFECTIVO"
        );


        pago.setMontoRecibido(
                montoRecibido
        );


        pago.setCambio(
                cambio
        );


        pago.setEstado(
                "PAGADO"
        );


        pago.setFechaPago(
                LocalDateTime.now()
        );


        pago.setIdempotencyKey(
                UUID.randomUUID()
                        .toString()
        );


        pago.setUltimosCuatro(
                null
        );


        pago.setMarcaTarjeta(
                null
        );


        confirmarCitaYOcuparHorario(
                cita
        );


        return pagoRepository.save(
                pago
        );
    }


    // =====================================================
    // PROCESAR PAGO CON TARJETA
    // VISA / MASTERCARD / DÉBITO
    // =====================================================

    @Transactional
    public Pago procesarPagoTarjeta(
            Long citaId,
            String metodoPago,
            String ultimosCuatro
    ) {

        Cita cita =
                citaRepository
                        .findById(
                                citaId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Cita no encontrada."
                                        )
                        );


        if (
                cita.getEstado() == null
                        ||
                        !"PENDIENTE_PAGO".equalsIgnoreCase(
                                cita.getEstado()
                        )
        ) {

            throw new IllegalStateException(
                    "La cita ya no se encuentra pendiente de pago."
            );
        }


        if (
                pagoRepository
                        .findByCitaId(
                                citaId
                        )
                        .isPresent()
        ) {

            throw new IllegalStateException(
                    "Ya existe un pago registrado para esta cita."
            );
        }


        String metodo =
                metodoPago == null
                        ? ""
                        : metodoPago.trim()
                        .toUpperCase();


        if (
                !"VISA".equals(metodo)
                        &&
                        !"MASTERCARD".equals(metodo)
                        &&
                        !"DEBITO".equals(metodo)
        ) {

            throw new IllegalArgumentException(
                    "El método de pago con tarjeta no es válido."
            );
        }


        String referencia =
                ultimosCuatro == null
                        ? ""
                        : ultimosCuatro.trim();


        if (!referencia.matches("\\d{4}")) {

            throw new IllegalArgumentException(
                    "Debe ingresar exactamente los últimos 4 dígitos de la tarjeta."
            );
        }


        // =================================================
        // FA04
        // SIMULACIÓN DE TARJETA RECHAZADA
        // 0002 = RECHAZADA
        // =================================================

        if ("0002".equals(
                referencia
        )) {

            throw new IllegalStateException(
                    "La transacción con tarjeta fue rechazada por el banco. "
                            + "Solicite al paciente otro método de pago."
            );
        }


        BigDecimal montoTotal =
                obtenerMonto(
                        cita
                );


        Pago pago =
                new Pago();


        pago.setCita(
                cita
        );


        pago.setNumeroTransaccion(
                generarNumeroTransaccion()
        );


        pago.setMonto(
                montoTotal
        );


        pago.setMetodoPago(
                metodo
        );


        pago.setUltimosCuatro(
                referencia
        );


        pago.setMarcaTarjeta(
                metodo
        );


        pago.setMontoRecibido(
                null
        );


        pago.setCambio(
                null
        );


        pago.setEstado(
                "PAGADO"
        );


        pago.setFechaPago(
                LocalDateTime.now()
        );


        pago.setIdempotencyKey(
                UUID.randomUUID()
                        .toString()
        );


        confirmarCitaYOcuparHorario(
                cita
        );


        return pagoRepository.save(
                pago
        );
    }


    // =====================================================
    // BUSCAR PAGO
    // =====================================================

    public Pago buscarPago(
            Long pagoId
    ) {

        return pagoRepository
                .findById(
                        pagoId
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Pago no encontrado."
                                )
                );
    }


    // =====================================================
    // GENERAR NÚMERO DE TRANSACCIÓN
    // =====================================================

    private String generarNumeroTransaccion() {

        return "CAJA-"
                +
                UUID.randomUUID()
                        .toString()
                        .substring(
                                0,
                                8
                        )
                        .toUpperCase();
    }


    // =====================================================
    // CONFIRMAR CITA Y OCUPAR HORARIO
    // =====================================================

    private void confirmarCitaYOcuparHorario(
            Cita cita
    ) {

        cita.setEstado(
                "CONFIRMADA"
        );


        citaRepository.save(
                cita
        );


        HorarioMedico horario =
                cita.getHorario();


        if (horario != null) {

            horario.setEstado(
                    "OCUPADO"
            );


            horario.setReservadoHasta(
                    null
            );


            horarioMedicoRepository.save(
                    horario
            );
        }
    }
}