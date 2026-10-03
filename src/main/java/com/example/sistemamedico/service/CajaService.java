package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.Cita_Repositorio;
import com.example.sistemamedico.Repositorio.HorarioMedico_Repositorio;
import com.example.sistemamedico.Repositorio.OrdenLaboratorioDetalle_Repositorio;
import com.example.sistemamedico.Repositorio.OrdenLaboratorio_Repositorio;
import com.example.sistemamedico.Repositorio.PagoLaboratorio_Repositorio;
import com.example.sistemamedico.Repositorio.Pago_Repositorio;
import com.example.sistemamedico.Repositorio.Usuario_Repositorio;

import com.example.sistemamedico.model.Cita;
import com.example.sistemamedico.model.HorarioMedico;
import com.example.sistemamedico.model.OrdenLaboratorio;
import com.example.sistemamedico.model.Pago;
import com.example.sistemamedico.model.PagoLaboratorio;
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


    // =====================================================
    // REPOSITORIOS CU-06
    // =====================================================

    private final Cita_Repositorio citaRepository;

    private final Usuario_Repositorio usuarioRepository;

    private final Pago_Repositorio pagoRepository;

    private final HorarioMedico_Repositorio
            horarioMedicoRepository;


    // =====================================================
    // REPOSITORIOS LABORATORIO
    // =====================================================

    private final OrdenLaboratorio_Repositorio
            ordenLaboratorioRepository;

    private final OrdenLaboratorioDetalle_Repositorio
            ordenLaboratorioDetalleRepository;

    private final PagoLaboratorio_Repositorio
            pagoLaboratorioRepository;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public CajaService(

            Cita_Repositorio citaRepository,

            Usuario_Repositorio usuarioRepository,

            Pago_Repositorio pagoRepository,

            HorarioMedico_Repositorio
                    horarioMedicoRepository,

            OrdenLaboratorio_Repositorio
                    ordenLaboratorioRepository,

            OrdenLaboratorioDetalle_Repositorio
                    ordenLaboratorioDetalleRepository,

            PagoLaboratorio_Repositorio
                    pagoLaboratorioRepository
    ) {

        this.citaRepository =
                citaRepository;

        this.usuarioRepository =
                usuarioRepository;

        this.pagoRepository =
                pagoRepository;

        this.horarioMedicoRepository =
                horarioMedicoRepository;

        this.ordenLaboratorioRepository =
                ordenLaboratorioRepository;

        this.ordenLaboratorioDetalleRepository =
                ordenLaboratorioDetalleRepository;

        this.pagoLaboratorioRepository =
                pagoLaboratorioRepository;
    }


    // =====================================================
    // CU-06
    // BUSCAR CITA PENDIENTE POR NÚMERO
    // =====================================================

    public Optional<Cita>
    buscarPendientePorNumeroCita(
            Long citaId
    ) {

        if (citaId == null) {

            return Optional.empty();
        }


        return citaRepository
                .findById(
                        citaId
                )
                .filter(
                        cita ->
                                cita.getEstado() != null
                                        &&
                                        "PENDIENTE_PAGO"
                                                .equalsIgnoreCase(
                                                        cita.getEstado()
                                                )
                );
    }


    // =====================================================
    // CU-06
    // BUSCAR CITAS PENDIENTES POR DPI
    // =====================================================

    public List<Cita>
    buscarPendientesPorDpi(
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
                        !"PACIENTE"
                                .equalsIgnoreCase(
                                        paciente
                                                .getRol()
                                                .getNombre()
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
                                        "PENDIENTE_PAGO"
                                                .equalsIgnoreCase(
                                                        cita.getEstado()
                                                )
                )
                .toList();
    }


    // =====================================================
    // CU-06
    // OBTENER MONTO DE LA CONSULTA
    // =====================================================

    public BigDecimal obtenerMonto(
            Cita cita
    ) {

        if (
                cita == null
                        ||
                        cita.getEspecialidad() == null
                        ||
                        cita.getEspecialidad()
                                .getPrecio() == null
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
    // CU-06
    // PROCESAR PAGO EN EFECTIVO
    // =====================================================

    @Transactional
    public Pago procesarPagoEfectivo(

            Long citaId,

            BigDecimal montoRecibido
    ) {

        // =================================================
        // BUSCAR CITA
        // =================================================

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


        // =================================================
        // VALIDAR ESTADO
        // =================================================

        if (
                cita.getEstado() == null
                        ||
                        !"PENDIENTE_PAGO"
                                .equalsIgnoreCase(
                                        cita.getEstado()
                                )
        ) {

            throw new IllegalStateException(
                    "La cita ya no se encuentra pendiente de pago."
            );
        }


        // =================================================
        // EVITAR PAGO DUPLICADO
        // =================================================

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


        // =================================================
        // OBTENER MONTO
        // =================================================

        BigDecimal montoTotal =
                obtenerMonto(
                        cita
                );


        // =================================================
        // VALIDAR MONTO RECIBIDO
        // =================================================

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
                            +
                            montoRecibido.setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            )
                            +
                            ") es menor al monto a cobrar (Q"
                            +
                            montoTotal.setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            )
                            +
                            ")."
            );
        }


        // =================================================
        // CALCULAR CAMBIO
        // =================================================

        BigDecimal cambio =
                montoRecibido.subtract(
                        montoTotal
                );


        // =================================================
        // CREAR PAGO
        // =================================================

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


        // =================================================
        // CONFIRMAR CITA
        // =================================================

        confirmarCitaYOcuparHorario(
                cita
        );


        // =================================================
        // GUARDAR
        // =================================================

        return pagoRepository.save(
                pago
        );
    }


    // =====================================================
    // CU-06
    // PROCESAR PAGO CON TARJETA
    // VISA / MASTERCARD / DÉBITO
    // =====================================================

    @Transactional
    public Pago procesarPagoTarjeta(

            Long citaId,

            String metodoPago,

            String ultimosCuatro
    ) {

        // =================================================
        // BUSCAR CITA
        // =================================================

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


        // =================================================
        // VALIDAR ESTADO
        // =================================================

        if (
                cita.getEstado() == null
                        ||
                        !"PENDIENTE_PAGO"
                                .equalsIgnoreCase(
                                        cita.getEstado()
                                )
        ) {

            throw new IllegalStateException(
                    "La cita ya no se encuentra pendiente de pago."
            );
        }


        // =================================================
        // EVITAR PAGO DUPLICADO
        // =================================================

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


        // =================================================
        // VALIDAR MÉTODO
        // =================================================

        String metodo =
                metodoPago == null
                        ? ""
                        : metodoPago
                        .trim()
                        .toUpperCase();


        if (
                !"VISA".equals(
                        metodo
                )
                        &&
                        !"MASTERCARD".equals(
                                metodo
                        )
                        &&
                        !"DEBITO".equals(
                                metodo
                        )
        ) {

            throw new IllegalArgumentException(
                    "El método de pago con tarjeta no es válido."
            );
        }


        // =================================================
        // VALIDAR ÚLTIMOS 4 DÍGITOS
        // =================================================

        String referencia =
                ultimosCuatro == null
                        ? ""
                        : ultimosCuatro.trim();


        if (
                !referencia.matches(
                        "\\d{4}"
                )
        ) {

            throw new IllegalArgumentException(
                    "Debe ingresar exactamente los últimos 4 dígitos de la tarjeta."
            );
        }


        // =================================================
        // SIMULACIÓN TARJETA RECHAZADA
        // =================================================

        if (
                "0002".equals(
                        referencia
                )
        ) {

            throw new IllegalStateException(
                    "La transacción con tarjeta fue rechazada por el banco. "
                            + "Solicite al paciente otro método de pago."
            );
        }


        // =================================================
        // OBTENER MONTO
        // =================================================

        BigDecimal montoTotal =
                obtenerMonto(
                        cita
                );


        // =================================================
        // CREAR PAGO
        // =================================================

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


        // =================================================
        // CONFIRMAR CITA
        // =================================================

        confirmarCitaYOcuparHorario(
                cita
        );


        // =================================================
        // GUARDAR PAGO
        // =================================================

        return pagoRepository.save(
                pago
        );
    }


    // =====================================================
    // CU-06
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
    // CU-06
    // GENERAR NUMERO DE TRANSACCION
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
    // CU-06
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


    // =====================================================
    // CU-10
    // BUSCAR ORDEN DE LABORATORIO PENDIENTE
    // POR NÚMERO DE ORDEN
    // =====================================================

    public Optional<OrdenLaboratorio>
    buscarOrdenLaboratorioPendientePorNumero(
            Long ordenId
    ) {

        if (ordenId == null) {

            return Optional.empty();
        }


        return ordenLaboratorioRepository
                .findById(
                        ordenId
                )
                .filter(
                        orden ->
                                orden.getEstado() != null
                                        &&
                                        "PENDIENTE"
                                                .equalsIgnoreCase(
                                                        orden.getEstado()
                                                )
                                        &&
                                        !Boolean.TRUE.equals(
                                                orden.getOrdenExterna()
                                        )
                );
    }


    // =====================================================
    // CU-10
    // BUSCAR ORDENES PENDIENTES POR DPI
    // =====================================================

    public List<OrdenLaboratorio>
    buscarOrdenesLaboratorioPendientesPorDpi(
            String dpi
    ) {

        String dpiLimpio =
                dpi == null
                        ? ""
                        : dpi.trim();


        // =================================================
        // VALIDAR DPI
        // =================================================

        if (
                dpiLimpio.isBlank()
                        ||
                        !dpiLimpio.matches(
                                "\\d{13}"
                        )
        ) {

            return List.of();
        }


        // =================================================
        // BUSCAR PACIENTE
        // =================================================

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
                        !"PACIENTE"
                                .equalsIgnoreCase(
                                        paciente
                                                .getRol()
                                                .getNombre()
                                )
        ) {

            return List.of();
        }


        // =================================================
        // BUSCAR ORDENES PENDIENTES
        // =================================================

        return ordenLaboratorioRepository
                .findAll()
                .stream()

                .filter(
                        orden ->
                                orden.getEstado() != null
                                        &&
                                        "PENDIENTE"
                                                .equalsIgnoreCase(
                                                        orden.getEstado()
                                                )
                )

                .filter(
                        orden ->
                                !Boolean.TRUE.equals(
                                        orden.getOrdenExterna()
                                )
                )

                .filter(
                        orden ->
                                orden.getCita() != null
                                        &&
                                        orden.getCita()
                                                .getPaciente() != null
                                        &&
                                        orden.getCita()
                                                .getPaciente()
                                                .getId() != null
                                        &&
                                        orden.getCita()
                                                .getPaciente()
                                                .getId()
                                                .equals(
                                                        paciente.getId()
                                                )
                )

                .toList();
    }


    // =====================================================
    // CU-10
    // CONTAR EXAMENES
    // =====================================================

    public int contarExamenesLaboratorio(
            Long ordenId
    ) {

        if (ordenId == null) {

            return 0;
        }


        return ordenLaboratorioDetalleRepository
                .findByOrdenIdOrderByIdAsc(
                        ordenId
                )
                .size();
    }


    // =====================================================
    // CU-10
    // BUSCAR ORDEN POR ID
    // =====================================================

    public OrdenLaboratorio
    buscarOrdenLaboratorio(
            Long ordenId
    ) {

        return ordenLaboratorioRepository
                .findById(
                        ordenId
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Orden de laboratorio no encontrada."
                                )
                );
    }


    // =====================================================
    // CU-10
    // PROCESAR PAGO DE LABORATORIO
    // =====================================================

    @Transactional
    public PagoLaboratorio
    procesarPagoLaboratorio(

            Long ordenId,

            Usuario cajero,

            String metodoPago,

            BigDecimal montoRecibido,

            String ultimosCuatro
    ) {

        // =================================================
        // VALIDAR CAJERO
        // =================================================

        if (
                cajero == null
                        ||
                        cajero.getId() == null
        ) {

            throw new IllegalArgumentException(
                    "No se encontró el cajero autenticado."
            );
        }


        if (
                cajero.getRol() == null
                        ||
                        cajero.getRol()
                                .getNombre() == null
                        ||
                        !"CAJERO"
                                .equalsIgnoreCase(
                                        cajero.getRol()
                                                .getNombre()
                                )
        ) {

            throw new IllegalArgumentException(
                    "El usuario no tiene permisos para realizar el cobro."
            );
        }


        if (
                cajero.getActivo() == null
                        ||
                        !cajero.getActivo()
        ) {

            throw new IllegalArgumentException(
                    "La cuenta del cajero se encuentra inactiva."
            );
        }


        // =================================================
        // BUSCAR ORDEN
        // =================================================

        OrdenLaboratorio orden =
                ordenLaboratorioRepository
                        .findById(
                                ordenId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Orden de laboratorio no encontrada."
                                        )
                        );


        // =================================================
        // NO COBRAR ORDEN EXTERNA
        // =================================================

        if (
                Boolean.TRUE.equals(
                        orden.getOrdenExterna()
                )
        ) {

            throw new IllegalStateException(
                    "Las órdenes externas no se cobran en la caja de la clínica."
            );
        }


        // =================================================
        // VALIDAR ESTADO
        // =================================================

        if (
                orden.getEstado() == null
                        ||
                        !"PENDIENTE"
                                .equalsIgnoreCase(
                                        orden.getEstado()
                                )
        ) {

            throw new IllegalStateException(
                    "La orden de laboratorio ya no se encuentra pendiente de pago."
            );
        }


        // =================================================
        // EVITAR DOBLE PAGO
        // =================================================

        if (
                pagoLaboratorioRepository
                        .existsByOrdenId(
                                ordenId
                        )
        ) {

            throw new IllegalStateException(
                    "Esta orden de laboratorio ya tiene un pago registrado."
            );
        }


        // =================================================
        // OBTENER MONTO
        // =================================================

        BigDecimal total =
                orden.getMontoTotal();


        if (
                total == null
                        ||
                        total.compareTo(
                                BigDecimal.ZERO
                        ) <= 0
        ) {

            throw new IllegalStateException(
                    "La orden no tiene un monto válido para realizar el cobro."
            );
        }


        // =================================================
        // VALIDAR MÉTODO DE PAGO
        // =================================================

        String metodo =
                metodoPago == null
                        ? ""
                        : metodoPago
                        .trim()
                        .toUpperCase();


        if (
                !"EFECTIVO".equals(
                        metodo
                )
                        &&
                        !"VISA".equals(
                                metodo
                        )
                        &&
                        !"MASTERCARD".equals(
                                metodo
                        )
                        &&
                        !"DEBITO".equals(
                                metodo
                        )
        ) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un método de pago válido."
            );
        }


        BigDecimal cambio = null;

        String referenciaTarjeta = null;


        // =================================================
        // EFECTIVO
        // =================================================

        if (
                "EFECTIVO".equals(
                        metodo
                )
        ) {

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
                            total
                    ) < 0
            ) {

                throw new IllegalArgumentException(

                        "El monto recibido (Q"
                                +
                                montoRecibido.setScale(
                                        2,
                                        RoundingMode.HALF_UP
                                )
                                +
                                ") es menor al monto a cobrar (Q"
                                +
                                total.setScale(
                                        2,
                                        RoundingMode.HALF_UP
                                )
                                +
                                ")."
                );
            }


            cambio =
                    montoRecibido.subtract(
                            total
                    );
        }


        // =================================================
        // TARJETA
        // =================================================

        else {

            referenciaTarjeta =
                    ultimosCuatro == null
                            ? ""
                            : ultimosCuatro.trim();


            if (
                    !referenciaTarjeta.matches(
                            "\\d{4}"
                    )
            ) {

                throw new IllegalArgumentException(
                        "Debe ingresar los últimos 4 dígitos de la tarjeta."
                );
            }


            // =============================================
            // FA04
            // SIMULACIÓN DE TARJETA RECHAZADA
            // =============================================

            if (
                    "0002".equals(
                            referenciaTarjeta
                    )
            ) {

                throw new IllegalStateException(
                        "La transacción con tarjeta fue rechazada por el banco. "
                                + "Solicite al paciente otro método de pago."
                );
            }


            montoRecibido = null;

            cambio = null;
        }


        // =================================================
        // CREAR PAGO DE LABORATORIO
        // =================================================

        PagoLaboratorio pago =
                new PagoLaboratorio();


        pago.setOrden(
                orden
        );


        pago.setCajero(
                cajero
        );


        pago.setNumeroTransaccion(
                generarNumeroTransaccionLaboratorio()
        );


        pago.setMonto(
                total
        );


        pago.setMetodoPago(
                metodo
        );


        pago.setMontoRecibido(
                montoRecibido
        );


        pago.setCambio(
                cambio
        );


        pago.setUltimosCuatro(
                referenciaTarjeta
        );


        pago.setFechaPago(
                LocalDateTime.now()
        );


        // =================================================
        // GUARDAR PAGO
        // =================================================

        PagoLaboratorio pagoGuardado =
                pagoLaboratorioRepository
                        .save(
                                pago
                        );


        // =================================================
        // ACTUALIZAR ORDEN
        // PENDIENTE -> EN_PROCESO
        // =================================================

        orden.setEstado(
                "EN_PROCESO"
        );


        ordenLaboratorioRepository
                .save(
                        orden
                );


        return pagoGuardado;
    }


    // =====================================================
    // CU-10
    // BUSCAR PAGO DE LABORATORIO
    // =====================================================

    public PagoLaboratorio
    buscarPagoLaboratorio(
            Long pagoId
    ) {

        return pagoLaboratorioRepository
                .findById(
                        pagoId
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Pago de laboratorio no encontrado."
                                )
                );
    }


    // =====================================================
    // CU-10
    // GENERAR NUMERO DE TRANSACCION LABORATORIO
    // =====================================================

    private String
    generarNumeroTransaccionLaboratorio() {

        return "LAB-"
                +
                UUID.randomUUID()
                        .toString()
                        .substring(
                                0,
                                8
                        )
                        .toUpperCase();
    }
}