package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.DespachoMedicamentoDetalle_Repositorio;
import com.example.sistemamedico.Repositorio.DespachoMedicamento_Repositorio;
import com.example.sistemamedico.Repositorio.InventarioMedicamento_Repositorio;
import com.example.sistemamedico.Repositorio.Medicamento_Repositorio;
import com.example.sistemamedico.Repositorio.RecetaMedicaDetalle_Repositorio;
import com.example.sistemamedico.Repositorio.RecetaMedica_Repositorio;

import com.example.sistemamedico.model.DespachoMedicamento;
import com.example.sistemamedico.model.DespachoMedicamentoDetalle;
import com.example.sistemamedico.model.InventarioMedicamento;
import com.example.sistemamedico.model.Medicamento;
import com.example.sistemamedico.model.RecetaMedica;
import com.example.sistemamedico.model.RecetaMedicaDetalle;
import com.example.sistemamedico.model.Usuario;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class FarmaciaDespachoService {


    // =====================================================
    // REPOSITORIOS
    // =====================================================

    private final RecetaMedica_Repositorio recetaRepositorio;

    private final RecetaMedicaDetalle_Repositorio recetaDetalleRepositorio;

    private final Medicamento_Repositorio medicamentoRepositorio;

    private final InventarioMedicamento_Repositorio inventarioRepositorio;

    private final DespachoMedicamento_Repositorio despachoRepositorio;

    private final DespachoMedicamentoDetalle_Repositorio despachoDetalleRepositorio;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public FarmaciaDespachoService(
            RecetaMedica_Repositorio recetaRepositorio,
            RecetaMedicaDetalle_Repositorio recetaDetalleRepositorio,
            Medicamento_Repositorio medicamentoRepositorio,
            InventarioMedicamento_Repositorio inventarioRepositorio,
            DespachoMedicamento_Repositorio despachoRepositorio,
            DespachoMedicamentoDetalle_Repositorio despachoDetalleRepositorio
    ) {

        this.recetaRepositorio =
                recetaRepositorio;

        this.recetaDetalleRepositorio =
                recetaDetalleRepositorio;

        this.medicamentoRepositorio =
                medicamentoRepositorio;

        this.inventarioRepositorio =
                inventarioRepositorio;

        this.despachoRepositorio =
                despachoRepositorio;

        this.despachoDetalleRepositorio =
                despachoDetalleRepositorio;
    }


    // =====================================================
    // CU-11
    // CONFIRMAR DESPACHO
    // =====================================================

    @Transactional
    public DespachoMedicamento confirmarDespacho(
            Long recetaId,
            Usuario personalFarmacia,
            Map<Long, Integer> cantidades,
            Map<Long, Long> medicamentosAlternativos,
            Map<Long, String> razonesSustitucion
    ) {


        // =================================================
        // VALIDAR PERSONAL DE FARMACIA
        // =================================================

        if (personalFarmacia == null) {

            throw new IllegalArgumentException(
                    "No se encontró el usuario de farmacia."
            );
        }


        if (
                personalFarmacia.getRol() == null
                        ||
                        !"FARMACIA".equalsIgnoreCase(
                                personalFarmacia
                                        .getRol()
                                        .getNombre()
                        )
        ) {

            throw new IllegalArgumentException(
                    "El usuario no tiene autorización para realizar despachos."
            );
        }


        if (!Boolean.TRUE.equals(
                personalFarmacia.getActivo()
        )) {

            throw new IllegalArgumentException(
                    "El usuario de farmacia se encuentra inactivo."
            );
        }


        if (personalFarmacia.getSucursal() == null) {

            throw new IllegalArgumentException(
                    "El usuario de farmacia no tiene una sucursal asignada."
            );
        }


        Long sucursalId =
                personalFarmacia
                        .getSucursal()
                        .getId();


        // =================================================
        // BUSCAR RECETA ACTIVA
        // =================================================

        RecetaMedica receta =
                recetaRepositorio
                        .findByIdAndActivoTrue(
                                recetaId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "No se encontró una receta activa con ID "
                                                        + recetaId
                                                        + "."
                                        )
                        );


        // =================================================
        // VALIDAR VIGENCIA MÁXIMA DE 7 DÍAS
        // =================================================

        long diasTranscurridos =
                ChronoUnit.DAYS.between(

                        receta.getFechaCreacion()
                                .toLocalDate(),

                        LocalDate.now()
                );


        if (diasTranscurridos > 7) {

            throw new IllegalArgumentException(

                    "Receta Vencida. La receta #"
                            + receta.getId()
                            + " fue emitida hace "
                            + diasTranscurridos
                            + " días y ya no es válida para despacho."
            );
        }


        // =================================================
        // EVITAR DOBLE DESPACHO
        // =================================================

        boolean yaDespachada =
                despachoRepositorio
                        .existsByRecetaIdAndEstado(
                                recetaId,
                                "CONFIRMADO"
                        );


        if (yaDespachada) {

            throw new IllegalArgumentException(
                    "La receta #"
                            + recetaId
                            + " ya posee un despacho confirmado."
            );
        }


        // =================================================
        // OBTENER MEDICAMENTOS RECETADOS
        // =================================================

        List<RecetaMedicaDetalle> detallesReceta =
                recetaDetalleRepositorio
                        .findByRecetaIdOrderByIdAsc(
                                recetaId
                        );


        if (detallesReceta.isEmpty()) {

            throw new IllegalArgumentException(
                    "La receta no contiene medicamentos registrados."
            );
        }


        // =================================================
        // PREPARAR ÍTEMS ANTES DE GUARDAR
        // =================================================

        List<ItemPreparado> itemsPreparados =
                new ArrayList<>();


        BigDecimal montoTotal =
                BigDecimal.ZERO;


        for (
                RecetaMedicaDetalle detalle :
                detallesReceta
        ) {


            // =============================================
            // CANTIDAD SELECCIONADA
            // =============================================

            Integer cantidad =
                    cantidades.get(
                            detalle.getId()
                    );


            /*
             * Si el campo no fue enviado, significa que ese
             * medicamento no se despachará.
             *
             * Esto permite continuar con los medicamentos
             * disponibles cuando otro no tiene inventario.
             */

            if (cantidad == null) {

                continue;
            }


            if (cantidad <= 0) {

                throw new IllegalArgumentException(
                        "La cantidad a despachar debe ser mayor que cero."
                );
            }


            Medicamento medicamentoOriginal =
                    detalle.getMedicamento();


            Medicamento medicamentoDespachado =
                    medicamentoOriginal;


            boolean sustituido =
                    false;


            String razonSustitucion =
                    null;


            // =============================================
            // REVISAR SUSTITUCIÓN
            // =============================================

            Long medicamentoAlternativoId =
                    medicamentosAlternativos.get(
                            detalle.getId()
                    );


            if (
                    medicamentoAlternativoId != null
                            &&
                            !medicamentoAlternativoId.equals(
                                    medicamentoOriginal.getId()
                            )
            ) {


                medicamentoDespachado =
                        medicamentoRepositorio
                                .findById(
                                        medicamentoAlternativoId
                                )
                                .orElseThrow(
                                        () ->
                                                new IllegalArgumentException(
                                                        "El medicamento alternativo seleccionado no existe."
                                                )
                                );


                if (!Boolean.TRUE.equals(
                        medicamentoDespachado.getActivo()
                )) {

                    throw new IllegalArgumentException(
                            "El medicamento alternativo seleccionado se encuentra inactivo."
                    );
                }


                razonSustitucion =
                        razonesSustitucion.get(
                                detalle.getId()
                        );


                if (
                        razonSustitucion == null
                                ||
                                razonSustitucion.isBlank()
                ) {

                    throw new IllegalArgumentException(
                            "Debe ingresar la razón de sustitución para "
                                    + medicamentoOriginal.getNombre()
                                    + "."
                    );
                }


                razonSustitucion =
                        razonSustitucion.trim();


                sustituido =
                        true;
            }


            // =============================================
            // BUSCAR INVENTARIO DEL MEDICAMENTO
            // REALMENTE DESPACHADO
            // =============================================

            Optional<InventarioMedicamento> inventarioOptional =
                    inventarioRepositorio
                            .findByMedicamentoIdAndSucursalIdAndActivoTrue(
                                    medicamentoDespachado.getId(),
                                    sucursalId
                            );


            if (inventarioOptional.isEmpty()) {

                throw new IllegalArgumentException(
                        "Sin inventario registrado para "
                                + medicamentoDespachado.getNombre()
                                + "."
                );
            }


            InventarioMedicamento inventario =
                    inventarioOptional.get();


            Integer stockActual =
                    inventario.getStockActual();


            if (
                    stockActual == null
                            ||
                            stockActual <= 0
            ) {

                throw new IllegalArgumentException(
                        "No hay existencias disponibles de "
                                + medicamentoDespachado.getNombre()
                                + "."
                );
            }


            // =============================================
            // VALIDAR CANTIDAD CONTRA INVENTARIO
            // =============================================

            if (cantidad > stockActual) {

                throw new IllegalArgumentException(
                        "Stock insuficiente para "
                                + medicamentoDespachado.getNombre()
                                + ". Disponible: "
                                + stockActual
                                + "."
                );
            }


            // =============================================
            // PRECIO
            // =============================================

            BigDecimal precioUnitario =
                    medicamentoDespachado.getPrecio();


            if (precioUnitario == null) {

                precioUnitario =
                        BigDecimal.ZERO;
            }


            BigDecimal subtotal =
                    precioUnitario.multiply(
                            BigDecimal.valueOf(
                                    cantidad
                            )
                    );


            montoTotal =
                    montoTotal.add(
                            subtotal
                    );


            // =============================================
            // PREPARAR ÍTEM
            // =============================================

            ItemPreparado item =
                    new ItemPreparado();


            item.detalleReceta =
                    detalle;

            item.medicamentoOriginal =
                    medicamentoOriginal;

            item.medicamentoDespachado =
                    medicamentoDespachado;

            item.inventario =
                    inventario;

            item.cantidad =
                    cantidad;

            item.precioUnitario =
                    precioUnitario;

            item.subtotal =
                    subtotal;

            item.sustituido =
                    sustituido;

            item.razonSustitucion =
                    razonSustitucion;


            itemsPreparados.add(
                    item
            );
        }


        // =================================================
        // DEBE EXISTIR AL MENOS UN MEDICAMENTO
        // =================================================

        if (itemsPreparados.isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar al menos un medicamento para el despacho."
            );
        }


        // =================================================
        // CREAR ENCABEZADO DEL DESPACHO
        // =================================================

        DespachoMedicamento despacho =
                new DespachoMedicamento();


        despacho.setReceta(
                receta
        );


        despacho.setFarmacia(
                personalFarmacia
        );


        despacho.setSucursal(
                personalFarmacia.getSucursal()
        );


        despacho.setMontoTotal(
                montoTotal
        );


        despacho.setEstado(
                "CONFIRMADO"
        );


        despacho =
                despachoRepositorio.save(
                        despacho
                );


        // =================================================
        // GUARDAR DETALLES Y DESCONTAR INVENTARIO
        // =================================================

        for (
                ItemPreparado item :
                itemsPreparados
        ) {


            DespachoMedicamentoDetalle detalleDespacho =
                    new DespachoMedicamentoDetalle();


            detalleDespacho.setDespacho(
                    despacho
            );


            detalleDespacho.setMedicamentoOriginal(
                    item.medicamentoOriginal
            );


            detalleDespacho.setMedicamentoDespachado(
                    item.medicamentoDespachado
            );


            detalleDespacho.setCantidad(
                    item.cantidad
            );


            detalleDespacho.setPrecioUnitario(
                    item.precioUnitario
            );


            detalleDespacho.setSubtotal(
                    item.subtotal
            );


            detalleDespacho.setSustituido(
                    item.sustituido
            );


            detalleDespacho.setRazonSustitucion(
                    item.razonSustitucion
            );


            despachoDetalleRepositorio.save(
                    detalleDespacho
            );


            // =============================================
            // DESCONTAR INVENTARIO
            // =============================================

            int nuevoStock =
                    item.inventario
                            .getStockActual()
                            -
                            item.cantidad;


            item.inventario.setStockActual(
                    nuevoStock
            );


            inventarioRepositorio.save(
                    item.inventario
            );
        }


        // =================================================
        // LA RECETA YA FUE DESPACHADA
        // =================================================

        receta.setActivo(
                false
        );


        recetaRepositorio.save(
                receta
        );


        return despacho;
    }


    // =====================================================
    // CU-11 - FA03
    // PACIENTE NO ADQUIERE LOS MEDICAMENTOS
    // =====================================================

    @Transactional
    public DespachoMedicamento cancelarPorNoAdquisicion(
            Long recetaId,
            Usuario personalFarmacia
    ) {


        // =================================================
        // VALIDAR PERSONAL DE FARMACIA
        // =================================================

        if (personalFarmacia == null) {

            throw new IllegalArgumentException(
                    "No se encontró el usuario de farmacia."
            );
        }


        if (
                personalFarmacia.getRol() == null
                        ||
                        !"FARMACIA".equalsIgnoreCase(
                                personalFarmacia
                                        .getRol()
                                        .getNombre()
                        )
        ) {

            throw new IllegalArgumentException(
                    "El usuario no tiene autorización para realizar esta operación."
            );
        }


        if (!Boolean.TRUE.equals(
                personalFarmacia.getActivo()
        )) {

            throw new IllegalArgumentException(
                    "El usuario de farmacia se encuentra inactivo."
            );
        }


        if (personalFarmacia.getSucursal() == null) {

            throw new IllegalArgumentException(
                    "El usuario de farmacia no tiene una sucursal asignada."
            );
        }


        // =================================================
        // BUSCAR RECETA ACTIVA
        // =================================================

        RecetaMedica receta =
                recetaRepositorio
                        .findByIdAndActivoTrue(
                                recetaId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "No se encontró una receta activa con ID "
                                                        + recetaId
                                                        + "."
                                        )
                        );


        // =================================================
        // VALIDAR QUE NO EXISTA DESPACHO CONFIRMADO
        // =================================================

        boolean yaDespachada =
                despachoRepositorio
                        .existsByRecetaIdAndEstado(
                                recetaId,
                                "CONFIRMADO"
                        );


        if (yaDespachada) {

            throw new IllegalArgumentException(
                    "La receta #"
                            + recetaId
                            + " ya posee un despacho confirmado."
            );
        }


        // =================================================
        // EVITAR REGISTRAR DOS VECES LA CANCELACIÓN
        // =================================================

        boolean cancelacionRegistrada =
                despachoRepositorio
                        .existsByRecetaIdAndEstado(
                                recetaId,
                                "CANCELADO"
                        );


        if (cancelacionRegistrada) {

            throw new IllegalArgumentException(
                    "Ya se registró anteriormente que el paciente no adquirió los medicamentos de la receta #"
                            + recetaId
                            + "."
            );
        }


        // =================================================
        // OBTENER PACIENTE
        // =================================================

        if (
                receta.getCita() == null
                        ||
                        receta.getCita().getPaciente() == null
        ) {

            throw new IllegalArgumentException(
                    "No se encontró el paciente asociado a la receta."
            );
        }


        Usuario paciente =
                receta.getCita()
                        .getPaciente();


        String nombrePaciente =
                paciente.getNombre();


        if (
                paciente.getApellido() != null
                        &&
                        !paciente.getApellido().isBlank()
        ) {

            nombrePaciente =
                    nombrePaciente
                            + " "
                            + paciente.getApellido();
        }


        // =================================================
        // MENSAJE FA03
        // =================================================

        String mensaje =
                "Se ha registrado que el paciente "
                        + nombrePaciente
                        + " no adquirió los medicamentos recetados "
                        + "en farmacia interna. Receta: "
                        + receta.getId()
                        + ".";


        // =================================================
        // REGISTRAR DESPACHO CANCELADO
        // =================================================

        DespachoMedicamento despacho =
                new DespachoMedicamento();


        despacho.setReceta(
                receta
        );


        despacho.setFarmacia(
                personalFarmacia
        );


        despacho.setSucursal(
                personalFarmacia.getSucursal()
        );


        despacho.setMontoTotal(
                BigDecimal.ZERO
        );


        despacho.setEstado(
                "CANCELADO"
        );


        despacho.setObservaciones(
                mensaje
        );


        despacho =
                despachoRepositorio.save(
                        despacho
                );


        /*
         * IMPORTANTE:
         *
         * NO se descuenta inventario.
         * NO se crean detalles de despacho.
         * NO se desactiva la receta.
         */


        return despacho;
    }


    // =====================================================
    // CLASE INTERNA PARA PREPARAR LOS ÍTEMS
    // ANTES DE MODIFICAR LA BASE DE DATOS
    // =====================================================

    private static class ItemPreparado {


        private RecetaMedicaDetalle detalleReceta;


        private Medicamento medicamentoOriginal;


        private Medicamento medicamentoDespachado;


        private InventarioMedicamento inventario;


        private Integer cantidad;


        private BigDecimal precioUnitario;


        private BigDecimal subtotal;


        private boolean sustituido;


        private String razonSustitucion;
    }
}