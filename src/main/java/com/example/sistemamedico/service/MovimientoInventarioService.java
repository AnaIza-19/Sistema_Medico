
package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.InventarioMedicamento_Repositorio;
import com.example.sistemamedico.Repositorio.MovimientoInventario_Repositorio;

import com.example.sistemamedico.model.InventarioMedicamento;
import com.example.sistemamedico.model.MovimientoInventario;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class MovimientoInventarioService {

    // =====================================================
    // TIPOS DE MOVIMIENTOS
    // =====================================================

    private static final int COMPRA = 0;
    private static final int DEVOLUCION = 1;
    private static final int VENTA = 2;
    private static final int RECLAMO = 3;
    private static final int AJUSTE_POSITIVO = 4;
    private static final int AJUSTE_NEGATIVO = 5;

    // EL TIPO 6 (DESPACHO) SERA AUTOMATICO.
    // NO SE PERMITE EN EL REGISTRO MANUAL.


    // =====================================================
    // REPOSITORIOS
    // =====================================================

    private final InventarioMedicamento_Repositorio
            inventarioRepositorio;

    private final MovimientoInventario_Repositorio
            movimientoRepositorio;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public MovimientoInventarioService(
            InventarioMedicamento_Repositorio inventarioRepositorio,
            MovimientoInventario_Repositorio movimientoRepositorio
    ) {

        this.inventarioRepositorio = inventarioRepositorio;
        this.movimientoRepositorio = movimientoRepositorio;
    }


    // =====================================================
    // CU-15: REGISTRAR MOVIMIENTO MANUAL
    // =====================================================

    @Transactional
    @CacheEvict(
            cacheNames = "inventariosAdmin",
            allEntries = true
    )
    public MovimientoInventario registrarMovimientoManual(

            Long inventarioId,
            Integer tipoMovimiento,
            Integer cantidad,
            BigDecimal costoUnitario,
            String numeroReferencia,
            String motivo,
            String nombreUsuario,
            String rolUsuario

    ) {

        // =================================================
        // 1. VALIDAR IDENTIFICADOR DEL INVENTARIO
        // =================================================

        if (inventarioId == null || inventarioId <= 0) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un medicamento y una sucursal."
            );
        }


        // =================================================
        // 2. VALIDAR USUARIO Y ROL
        // =================================================

        if (nombreUsuario == null ||
                nombreUsuario.isBlank()) {

            throw new IllegalArgumentException(
                    "No se identificó al usuario responsable."
            );
        }

        String usuarioLimpio = nombreUsuario.trim();

        if (usuarioLimpio.length() > 100) {

            throw new IllegalArgumentException(
                    "El nombre del usuario supera la longitud permitida."
            );
        }

        boolean esAdministrador =
                "ADMINISTRADOR".equalsIgnoreCase(rolUsuario);

        boolean esFarmacia =
                "FARMACIA".equalsIgnoreCase(rolUsuario);

        if (!esAdministrador && !esFarmacia) {

            throw new IllegalArgumentException(
                    "No tiene permisos para registrar movimientos de inventario."
            );
        }


        // =================================================
        // 3. VALIDAR TIPO DE MOVIMIENTO
        // =================================================

        if (tipoMovimiento == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar el tipo de movimiento."
            );
        }

        if (tipoMovimiento < COMPRA ||
                tipoMovimiento > AJUSTE_NEGATIVO) {

            throw new IllegalArgumentException(
                    "Tipo de movimiento no permitido para registro manual."
            );
        }


        // =================================================
        // 4. VALIDAR PERMISOS PARA AJUSTES
        // =================================================

        if (
                (tipoMovimiento == AJUSTE_POSITIVO ||
                        tipoMovimiento == AJUSTE_NEGATIVO)
                        && !esAdministrador
        ) {

            throw new IllegalArgumentException(
                    "Los ajustes de inventario requieren permisos de administración."
            );
        }


        // =================================================
        // 5. VALIDAR CANTIDAD
        // =================================================

        if (cantidad == null || cantidad <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad debe ser un número entero positivo."
            );
        }


        // =================================================
        // 6. VALIDAR COSTO UNITARIO
        // =================================================

        if (tipoMovimiento == COMPRA) {

            if (costoUnitario == null) {

                throw new IllegalArgumentException(
                        "El costo unitario es obligatorio para compras."
                );
            }

            if (costoUnitario.compareTo(BigDecimal.ZERO) <= 0) {

                throw new IllegalArgumentException(
                        "El costo unitario debe ser mayor a 0."
                );
            }

            if (costoUnitario.scale() > 2) {

                throw new IllegalArgumentException(
                        "El costo unitario debe tener máximo 2 decimales."
                );
            }

            if (costoUnitario.precision() -
                    costoUnitario.scale() > 10) {

                throw new IllegalArgumentException(
                        "El costo unitario supera el valor permitido."
                );
            }

        } else {

            // EL COSTO UNITARIO SOLO SE SOLICITA
            // PARA MOVIMIENTOS DE COMPRA.

            costoUnitario = null;
        }


        // =================================================
        // 7. VALIDAR NÚMERO DE REFERENCIA
        // =================================================

        String referenciaLimpia = null;

        if (numeroReferencia != null &&
                !numeroReferencia.isBlank()) {

            referenciaLimpia = numeroReferencia.trim();

            if (referenciaLimpia.length() > 120) {

                throw new IllegalArgumentException(
                        "El número de referencia no debe superar 120 caracteres."
                );
            }
        }


        // =================================================
        // 8. VALIDAR MOTIVO O NOTAS
        // =================================================

        String motivoLimpio =
                motivo == null ? "" : motivo.trim();

        boolean requiereMotivo =
                tipoMovimiento == DEVOLUCION ||
                        tipoMovimiento == RECLAMO ||
                        tipoMovimiento == AJUSTE_POSITIVO ||
                        tipoMovimiento == AJUSTE_NEGATIVO;

        if (requiereMotivo) {

            if (motivoLimpio.length() < 10 ||
                    motivoLimpio.length() > 500) {

                throw new IllegalArgumentException(
                        "El motivo debe contener entre 10 y 500 caracteres."
                );
            }
        }

        if (motivoLimpio.length() > 500) {

            throw new IllegalArgumentException(
                    "Las notas no deben superar 500 caracteres."
            );
        }

        if (tipoMovimiento == VENTA) {

            // EL FORMULARIO DE VENTA NO SOLICITA NOTAS.

            motivoLimpio = "";
        }


        // =================================================
        // 9. BUSCAR INVENTARIO EN POSTGRESQL
        // =================================================

        InventarioMedicamento inventario =
                inventarioRepositorio.findById(inventarioId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No se encontró el inventario seleccionado."
                                )
                        );


        // =================================================
        // 10. VALIDAR QUE EL INVENTARIO ESTÉ ACTIVO
        // =================================================

        if (!Boolean.TRUE.equals(inventario.getActivo())) {

            throw new IllegalArgumentException(
                    "No se pueden registrar movimientos en un inventario inactivo."
            );
        }


        // =================================================
        // 11. OBTENER EL STOCK ACTUAL
        // =================================================

        Integer stockAnterior =
                inventario.getStockActual();

        if (stockAnterior == null || stockAnterior < 0) {

            throw new IllegalArgumentException(
                    "El inventario presenta un stock inválido."
            );
        }


        // =================================================
        // 12. DETERMINAR ENTRADA O SALIDA
        // =================================================

        boolean esEntrada =
                tipoMovimiento == COMPRA ||
                        tipoMovimiento == DEVOLUCION ||
                        tipoMovimiento == AJUSTE_POSITIVO;


        // =================================================
        // 13. CALCULAR STOCK PROYECTADO
        // =================================================

        long stockProyectado;

        if (esEntrada) {

            stockProyectado =
                    (long) stockAnterior + cantidad;

        } else {

            // VENTA, RECLAMO Y AJUSTE NEGATIVO

            if (cantidad > stockAnterior) {

                throw new IllegalArgumentException(
                        "Stock insuficiente. Stock actual: " +
                                stockAnterior +
                                ". No se puede registrar una salida de " +
                                cantidad +
                                " unidades."
                );
            }

            stockProyectado =
                    (long) stockAnterior - cantidad;
        }


        // =================================================
        // 14. IMPEDIR DESBORDAMIENTO DE ENTEROS
        // =================================================

        if (stockProyectado < 0 ||
                stockProyectado > Integer.MAX_VALUE) {

            throw new IllegalArgumentException(
                    "El stock resultante está fuera del rango permitido."
            );
        }

        int stockNuevo = (int) stockProyectado;


        // =================================================
        // 15. ACTUALIZAR EXISTENCIAS
        //
        // @VERSION EN InventarioMedicamento
        // PROTEGE LAS ACTUALIZACIONES JPA.
        // =================================================

        inventario.setStockActual(stockNuevo);

        inventarioRepositorio.saveAndFlush(inventario);


        // =================================================
        // 16. CREAR REGISTRO DE BITÁCORA
        // =================================================

        MovimientoInventario movimiento =
                new MovimientoInventario();

        movimiento.setInventarioId(
                inventario.getId()
        );

        movimiento.setMedicamentoId(
                inventario.getMedicamento().getId()
        );

        movimiento.setSucursalId(
                inventario.getSucursal().getId()
        );

        movimiento.setTipoMovimiento(
                tipoMovimiento.shortValue()
        );

        movimiento.setCantidad(cantidad);

        movimiento.setStockAnterior(stockAnterior);

        movimiento.setStockNuevo(stockNuevo);

        movimiento.setCostoUnitario(costoUnitario);

        movimiento.setNumeroReferencia(
                referenciaLimpia
        );

        movimiento.setMotivo(
                motivoLimpio.isBlank() ? null : motivoLimpio
        );

        movimiento.setRegistradoPor(usuarioLimpio);

        movimiento.setActivo(true);


        // =================================================
        // 17. GUARDAR MOVIMIENTO
        //
        // SI FALLA LA BITÁCORA,
        // SE REVIERTE TAMBIÉN EL CAMBIO DE STOCK.
        // =================================================

        return movimientoRepositorio.saveAndFlush(movimiento);

    }

}
