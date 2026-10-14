
package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.AuditoriaInventarioMedicamento_Repositorio;
import com.example.sistemamedico.Repositorio.InventarioMedicamento_Repositorio;
import com.example.sistemamedico.Repositorio.Medicamento_Repositorio;
import com.example.sistemamedico.Repositorio.MovimientoInventario_Repositorio;
import com.example.sistemamedico.Repositorio.Sucursal_Repositorio;

import com.example.sistemamedico.model.AuditoriaInventarioMedicamento;
import com.example.sistemamedico.model.InventarioMedicamento;
import com.example.sistemamedico.model.Medicamento;
import com.example.sistemamedico.model.MovimientoInventario;
import com.example.sistemamedico.model.Sucursal;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventarioMedicamentoAdminService {

    // =====================================================
    // REPOSITORIOS
    // =====================================================

    private final InventarioMedicamento_Repositorio inventarios;
    private final Medicamento_Repositorio medicamentos;
    private final Sucursal_Repositorio sucursales;
    private final AuditoriaInventarioMedicamento_Repositorio auditoria;

    // CU-15: BITÁCORA DE MOVIMIENTOS
    private final MovimientoInventario_Repositorio movimientos;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public InventarioMedicamentoAdminService(
            InventarioMedicamento_Repositorio inventarios,
            Medicamento_Repositorio medicamentos,
            Sucursal_Repositorio sucursales,
            AuditoriaInventarioMedicamento_Repositorio auditoria,
            MovimientoInventario_Repositorio movimientos
    ) {

        this.inventarios = inventarios;
        this.medicamentos = medicamentos;
        this.sucursales = sucursales;
        this.auditoria = auditoria;
        this.movimientos = movimientos;
    }

    // =====================================================
    // LISTAR INVENTARIOS
    // =====================================================

    @Transactional(readOnly = true)
    public Page<InventarioMedicamento> listar(
            String buscar,
            int pagina,
            int tamanio
    ) {

        int tam = (tamanio == 25 || tamanio == 50)
                ? tamanio
                : 10;

        Pageable pag = PageRequest.of(
                Math.max(pagina, 0),
                tam,
                Sort.by("id").ascending()
        );

        return inventarios.buscarParaAdmin(
                buscar == null ? "" : buscar.trim(),
                pag
        );
    }

    // =====================================================
    // OBTENER INVENTARIO
    // =====================================================

    @Transactional(readOnly = true)
    public InventarioMedicamento obtener(Long id) {

        InventarioMedicamento i = inventarios.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Inventario no encontrado."
                        )
                );

        // Inicializar relaciones dentro de la transacción.
        i.getMedicamento().getNombre();
        i.getSucursal().getNombre();

        return i;
    }

    // =====================================================
    // OBTENER INVENTARIOS ACTIVOS
    // =====================================================

    @Cacheable("inventariosAdmin")
    @Transactional(readOnly = true)
    public List<InventarioMedicamento> obtenerActivos() {

        return inventarios.findByActivoTrueOrderByIdAsc();
    }

    // =====================================================
    // MEDICAMENTOS ACTIVOS
    // =====================================================

    @Transactional(readOnly = true)
    public List<Medicamento> medicamentosActivos() {

        return medicamentos.findByActivoTrueOrderByNombreAsc();
    }

    // =====================================================
    // SUCURSALES ACTIVAS
    // =====================================================

    @Transactional(readOnly = true)
    public List<Sucursal> sucursalesActivas() {

        return sucursales.findByActivoTrue();
    }

    // =====================================================
    // VALIDAR ADMINISTRADOR
    // =====================================================

    private String validarActor(String actor) {

        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException(
                    "No se identificó al administrador."
            );
        }

        String limpio = actor.trim();

        if (limpio.length() > 100) {
            throw new IllegalArgumentException(
                    "El usuario administrador es demasiado largo."
            );
        }

        return limpio;
    }

    // =====================================================
    // VALIDAR STOCK
    // =====================================================

    private int validarStock(Integer stock) {

        if (stock == null || stock < 0) {
            throw new IllegalArgumentException(
                    "El stock debe ser un número entero " +
                            "mayor o igual a cero."
            );
        }

        return stock;
    }

    // =====================================================
    // VALIDAR DETALLE DE AUDITORÍA CU-14
    // =====================================================

    private String validarDetalle(String detalle) {

        String d = detalle == null
                ? ""
                : detalle.trim();

        if (d.length() < 5 || d.length() > 500) {
            throw new IllegalArgumentException(
                    "Ingrese un motivo de 5 a 500 caracteres " +
                            "para auditar la operación."
            );
        }

        return d;
    }

    // =====================================================
    // REGISTRAR AUDITORÍA CU-14
    // =====================================================

    private void registrar(
            String accion,
            InventarioMedicamento i,
            String actor,
            Integer anterior,
            Integer nuevo,
            String motivo
    ) {

        AuditoriaInventarioMedicamento a =
                new AuditoriaInventarioMedicamento();

        a.setInventarioId(i.getId());

        a.setMedicamentoId(
                i.getMedicamento().getId()
        );

        a.setSucursalId(
                i.getSucursal().getId()
        );

        a.setAccion(accion);
        a.setEjecutadoPor(actor);
        a.setStockAnterior(anterior);
        a.setStockNuevo(nuevo);
        a.setDetalle(motivo);

        auditoria.saveAndFlush(a);
    }

    // =====================================================
    // BLOQUEAR INVENTARIO PARA MODIFICACIONES
    // =====================================================

    private InventarioMedicamento bloquear(Long id) {

        return inventarios.bloquearPorId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Inventario no encontrado."
                        )
                );
    }

    // =====================================================
    // CU-15: REGISTRAR MOVIMIENTO DE AJUSTE
    //
    // IMPORTANTE:
    // ESTE MÉTODO NO MODIFICA EL STOCK.
    // SOLO CREA LA BITÁCORA DEL AJUSTE.
    // =====================================================

    private void registrarMovimientoAjuste(
            InventarioMedicamento inventario,
            int stockAnterior,
            int stockNuevo,
            String usuario,
            String motivo
    ) {

        // Calcular diferencia sin desbordamiento.
        long diferencia =
                (long) stockNuevo - stockAnterior;

        if (diferencia == 0) {
            throw new IllegalArgumentException(
                    "El nuevo stock debe ser distinto del actual."
            );
        }

        long cantidadAbsoluta = Math.abs(diferencia);

        if (cantidadAbsoluta > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "La cantidad del ajuste supera " +
                            "el rango permitido."
            );
        }

        // Tipo 4: entrada.
        // Tipo 5: salida.

        short tipoMovimiento = diferencia > 0
                ? (short) 4
                : (short) 5;

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
                tipoMovimiento
        );

        movimiento.setCantidad(
                (int) cantidadAbsoluta
        );

        movimiento.setStockAnterior(
                stockAnterior
        );

        movimiento.setStockNuevo(
                stockNuevo
        );

        // Ajustes administrativos sin costo de compra.
        movimiento.setCostoUnitario(null);

        // Referencia para identificar el origen.
        movimiento.setNumeroReferencia(
                "AJUSTE-CU14-" + inventario.getId()
        );

        movimiento.setMotivo(motivo);

        movimiento.setRegistradoPor(usuario);

        movimiento.setActivo(true);

        // No establecer despachoDetalleId.
        // Solo corresponde a despachos CU-11.

        movimientos.saveAndFlush(movimiento);
    }

    // =====================================================
    // CREAR INVENTARIO CU-14
    // =====================================================

    @Transactional
    @CacheEvict(
            cacheNames = "inventariosAdmin",
            allEntries = true
    )
    public InventarioMedicamento crear(
            Long medicamentoId,
            Long sucursalId,
            Integer stock,
            String detalle,
            String actor
    ) {

        String usuario = validarActor(actor);
        int cantidad = validarStock(stock);
        String motivo = validarDetalle(detalle);

        if (medicamentoId == null || sucursalId == null) {
            throw new IllegalArgumentException(
                    "Seleccione medicamento y sucursal."
            );
        }

        Medicamento m = medicamentos.findById(medicamentoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Medicamento no encontrado."
                        )
                );

        Sucursal s = sucursales.findById(sucursalId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Sucursal no encontrada."
                        )
                );

        if (!Boolean.TRUE.equals(m.getActivo())
                || !Boolean.TRUE.equals(s.getActivo())) {
            throw new IllegalArgumentException(
                    "Seleccione un medicamento y una sucursal activos."
            );
        }

        if (inventarios
                .findByMedicamentoIdAndSucursalId(
                        medicamentoId,
                        sucursalId
                )
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Ya existe inventario para esa combinación. " +
                            "Si está inactivo, reactívelo."
            );
        }

        InventarioMedicamento i =
                new InventarioMedicamento();

        i.setMedicamento(m);
        i.setSucursal(s);
        i.setStockActual(cantidad);
        i.setActivo(true);

        inventarios.saveAndFlush(i);

        registrar(
                "CREAR",
                i,
                usuario,
                null,
                cantidad,
                motivo
        );

        return i;
    }

    // =====================================================
    // ACTUALIZAR STOCK
    //
    // INTEGRACIÓN CU-14 + CU-15
    //
    // REGISTRA:
    // 1. EL NUEVO STOCK
    // 2. AUDITORÍA CU-14
    // 3. MOVIMIENTO CU-15
    //
    // TODO DENTRO DE UNA SOLA TRANSACCIÓN.
    // =====================================================

    @Transactional
    @CacheEvict(
            cacheNames = "inventariosAdmin",
            allEntries = true
    )
    public void actualizarStock(
            Long id,
            Integer stockNuevo,
            String detalle,
            String actor
    ) {

        // =============================================
        // 1. VALIDAR DATOS
        // =============================================

        String usuario = validarActor(actor);

        int nuevo = validarStock(stockNuevo);

        String motivo = validarDetalle(detalle);

        // CU-15 requiere al menos 10 caracteres
        // para justificar un ajuste administrativo.

        if (motivo.length() < 10) {
            throw new IllegalArgumentException(
                    "La justificación del ajuste debe " +
                            "contener entre 10 y 500 caracteres."
            );
        }

        // =============================================
        // 2. BLOQUEAR INVENTARIO
        // =============================================

        InventarioMedicamento i = bloquear(id);

        if (!Boolean.TRUE.equals(i.getActivo())) {
            throw new IllegalArgumentException(
                    "No se puede ajustar un inventario inactivo."
            );
        }

        if (!Boolean.TRUE.equals(
                i.getMedicamento().getActivo()
        ) || !Boolean.TRUE.equals(
                i.getSucursal().getActivo()
        )) {

            throw new IllegalArgumentException(
                    "El medicamento y la sucursal deben estar activos."
            );
        }

        // =============================================
        // 3. OBTENER STOCK ANTERIOR
        // =============================================

        Integer stockGuardado = i.getStockActual();

        if (stockGuardado == null || stockGuardado < 0) {
            throw new IllegalArgumentException(
                    "El inventario presenta un stock inválido."
            );
        }

        int anterior = stockGuardado;

        if (anterior == nuevo) {
            throw new IllegalArgumentException(
                    "El nuevo stock debe ser distinto del actual."
            );
        }

        // =============================================
        // 4. VALIDAR DIFERENCIA
        // =============================================

        long diferencia =
                (long) nuevo - anterior;

        if (Math.abs(diferencia) > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "La cantidad del ajuste supera " +
                            "el rango permitido."
            );
        }

        // =============================================
        // 5. ACTUALIZAR STOCK UNA SOLA VEZ
        // =============================================

        i.setStockActual(nuevo);

        inventarios.saveAndFlush(i);

        // =============================================
        // 6. REGISTRAR AUDITORÍA CU-14
        // =============================================

        registrar(
                "AJUSTAR_STOCK",
                i,
                usuario,
                anterior,
                nuevo,
                motivo
        );

        // =============================================
        // 7. REGISTRAR MOVIMIENTO CU-15
        // =============================================

        registrarMovimientoAjuste(
                i,
                anterior,
                nuevo,
                usuario,
                motivo
        );

        // Si falla cualquiera de los registros,
        // Spring revierte toda la transacción.
    }

    // =====================================================
    // DESACTIVAR INVENTARIO CU-14
    // =====================================================

    @Transactional
    @CacheEvict(
            cacheNames = "inventariosAdmin",
            allEntries = true
    )
    public void desactivar(
            Long id,
            String detalle,
            String actor
    ) {

        String usuario = validarActor(actor);
        String motivo = validarDetalle(detalle);

        InventarioMedicamento i = bloquear(id);

        if (!Boolean.TRUE.equals(i.getActivo())) {
            throw new IllegalArgumentException(
                    "Este inventario ya está inactivo."
            );
        }

        if (i.getStockActual() != null
                && i.getStockActual() > 0) {

            throw new IllegalArgumentException(
                    "Para proteger existencias, " +
                            "ajuste primero el stock a 0."
            );
        }

        int stock = i.getStockActual();

        i.setActivo(false);

        inventarios.saveAndFlush(i);

        registrar(
                "DESACTIVAR",
                i,
                usuario,
                stock,
                stock,
                motivo
        );
    }

    // =====================================================
    // REACTIVAR INVENTARIO CU-14
    // =====================================================

    @Transactional
    @CacheEvict(
            cacheNames = "inventariosAdmin",
            allEntries = true
    )
    public void reactivar(
            Long id,
            String detalle,
            String actor
    ) {

        String usuario = validarActor(actor);
        String motivo = validarDetalle(detalle);

        InventarioMedicamento i = bloquear(id);

        if (Boolean.TRUE.equals(i.getActivo())) {
            throw new IllegalArgumentException(
                    "Este inventario ya está activo."
            );
        }

        if (!Boolean.TRUE.equals(
                i.getMedicamento().getActivo()
        ) || !Boolean.TRUE.equals(
                i.getSucursal().getActivo()
        )) {

            throw new IllegalArgumentException(
                    "El medicamento y la sucursal deben estar activos."
            );
        }

        int stock = i.getStockActual();

        i.setActivo(true);

        inventarios.saveAndFlush(i);

        registrar(
                "REACTIVAR",
                i,
                usuario,
                stock,
                stock,
                motivo
        );
    }
}
