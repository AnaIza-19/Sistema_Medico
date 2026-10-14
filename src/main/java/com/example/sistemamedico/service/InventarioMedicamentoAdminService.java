package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.AuditoriaInventarioMedicamento_Repositorio;
import com.example.sistemamedico.Repositorio.InventarioMedicamento_Repositorio;
import com.example.sistemamedico.Repositorio.Medicamento_Repositorio;
import com.example.sistemamedico.Repositorio.Sucursal_Repositorio;
import com.example.sistemamedico.model.AuditoriaInventarioMedicamento;
import com.example.sistemamedico.model.InventarioMedicamento;
import com.example.sistemamedico.model.Medicamento;
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
    private final InventarioMedicamento_Repositorio inventarios;
    private final Medicamento_Repositorio medicamentos;
    private final Sucursal_Repositorio sucursales;
    private final AuditoriaInventarioMedicamento_Repositorio auditoria;

    public InventarioMedicamentoAdminService(
            InventarioMedicamento_Repositorio inventarios,
            Medicamento_Repositorio medicamentos,
            Sucursal_Repositorio sucursales,
            AuditoriaInventarioMedicamento_Repositorio auditoria) {
        this.inventarios = inventarios;
        this.medicamentos = medicamentos;
        this.sucursales = sucursales;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public Page<InventarioMedicamento> listar(String buscar, int pagina, int tamanio) {
        int tam = (tamanio == 25 || tamanio == 50) ? tamanio : 10;
        Pageable pag = PageRequest.of(Math.max(pagina, 0), tam, Sort.by("id").ascending());
        return inventarios.buscarParaAdmin(buscar == null ? "" : buscar.trim(), pag);
    }

    @Transactional(readOnly = true)
    public InventarioMedicamento obtener(Long id) {
        InventarioMedicamento i = inventarios.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado."));
        // Inicializar relaciones durante la transacción; open-in-view está desactivado.
        i.getMedicamento().getNombre();
        i.getSucursal().getNombre();
        return i;
    }

    @Cacheable("inventariosAdmin")
    @Transactional(readOnly = true)
    public List<InventarioMedicamento> obtenerActivos() {
        return inventarios.findByActivoTrueOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public List<Medicamento> medicamentosActivos() {
        return medicamentos.findByActivoTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Sucursal> sucursalesActivas() {
        return sucursales.findByActivoTrue();
    }

    private String validarActor(String actor) {
        if (actor == null || actor.isBlank())
            throw new IllegalArgumentException("No se identificó al administrador.");
        String limpio = actor.trim();
        if (limpio.length() > 100)
            throw new IllegalArgumentException("El usuario administrador es demasiado largo.");
        return limpio;
    }

    private int validarStock(Integer stock) {
        if (stock == null || stock < 0)
            throw new IllegalArgumentException("El stock debe ser un número entero mayor o igual a cero.");
        return stock;
    }

    private String validarDetalle(String detalle) {
        String d = detalle == null ? "" : detalle.trim();
        if (d.length() < 5 || d.length() > 500)
            throw new IllegalArgumentException("Ingrese un motivo de 5 a 500 caracteres para auditar la operación.");
        return d;
    }

    private void registrar(String accion, InventarioMedicamento i, String actor,
                           Integer anterior, Integer nuevo, String motivo) {
        AuditoriaInventarioMedicamento a = new AuditoriaInventarioMedicamento();
        a.setInventarioId(i.getId());
        a.setMedicamentoId(i.getMedicamento().getId());
        a.setSucursalId(i.getSucursal().getId());
        a.setAccion(accion);
        a.setEjecutadoPor(actor);
        a.setStockAnterior(anterior);
        a.setStockNuevo(nuevo);
        a.setDetalle(motivo);
        auditoria.saveAndFlush(a);
    }

    private InventarioMedicamento bloquear(Long id) {
        return inventarios.bloquearPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado."));
    }

    @Transactional
    @CacheEvict(cacheNames = "inventariosAdmin", allEntries = true)
    public InventarioMedicamento crear(Long medicamentoId, Long sucursalId, Integer stock,
                                       String detalle, String actor) {
        String usuario = validarActor(actor);
        int cantidad = validarStock(stock);
        String motivo = validarDetalle(detalle);
        if (medicamentoId == null || sucursalId == null)
            throw new IllegalArgumentException("Seleccione medicamento y sucursal.");
        Medicamento m = medicamentos.findById(medicamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Medicamento no encontrado."));
        Sucursal s = sucursales.findById(sucursalId)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada."));
        if (!Boolean.TRUE.equals(m.getActivo()) || !Boolean.TRUE.equals(s.getActivo()))
            throw new IllegalArgumentException("Seleccione un medicamento y una sucursal activos.");
        if (inventarios.findByMedicamentoIdAndSucursalId(medicamentoId, sucursalId).isPresent())
            throw new IllegalArgumentException("Ya existe inventario para esa combinación. Si está inactivo, reactívelo.");
        InventarioMedicamento i = new InventarioMedicamento();
        i.setMedicamento(m);
        i.setSucursal(s);
        i.setStockActual(cantidad);
        i.setActivo(true);
        inventarios.saveAndFlush(i);
        registrar("CREAR", i, usuario, null, cantidad, motivo);
        return i;
    }

    @Transactional
    @CacheEvict(cacheNames = "inventariosAdmin", allEntries = true)
    public void actualizarStock(Long id, Integer stockNuevo, String detalle, String actor) {
        String usuario = validarActor(actor);
        int nuevo = validarStock(stockNuevo);
        String motivo = validarDetalle(detalle);
        InventarioMedicamento i = bloquear(id);
        if (!Boolean.TRUE.equals(i.getActivo()))
            throw new IllegalArgumentException("No se puede ajustar un inventario inactivo.");
        if (!Boolean.TRUE.equals(i.getMedicamento().getActivo())
                || !Boolean.TRUE.equals(i.getSucursal().getActivo()))
            throw new IllegalArgumentException("El medicamento y la sucursal deben estar activos.");
        int anterior = i.getStockActual() == null ? 0 : i.getStockActual();
        if (anterior == nuevo)
            throw new IllegalArgumentException("El nuevo stock debe ser distinto del actual.");
        i.setStockActual(nuevo);
        inventarios.saveAndFlush(i);
        registrar("AJUSTAR_STOCK", i, usuario, anterior, nuevo, motivo);
    }

    @Transactional
    @CacheEvict(cacheNames = "inventariosAdmin", allEntries = true)
    public void desactivar(Long id, String detalle, String actor) {
        String usuario = validarActor(actor);
        String motivo = validarDetalle(detalle);
        InventarioMedicamento i = bloquear(id);
        if (!Boolean.TRUE.equals(i.getActivo()))
            throw new IllegalArgumentException("Este inventario ya está inactivo.");
        if (i.getStockActual() != null && i.getStockActual() > 0)
            throw new IllegalArgumentException("Para proteger existencias, ajuste primero el stock a 0.");
        int stock = i.getStockActual();
        i.setActivo(false);
        inventarios.saveAndFlush(i);
        registrar("DESACTIVAR", i, usuario, stock, stock, motivo);
    }

    @Transactional
    @CacheEvict(cacheNames = "inventariosAdmin", allEntries = true)
    public void reactivar(Long id, String detalle, String actor) {
        String usuario = validarActor(actor);
        String motivo = validarDetalle(detalle);
        InventarioMedicamento i = bloquear(id);
        if (Boolean.TRUE.equals(i.getActivo()))
            throw new IllegalArgumentException("Este inventario ya está activo.");
        if (!Boolean.TRUE.equals(i.getMedicamento().getActivo())
                || !Boolean.TRUE.equals(i.getSucursal().getActivo()))
            throw new IllegalArgumentException("El medicamento y la sucursal deben estar activos.");
        int stock = i.getStockActual();
        i.setActivo(true);
        inventarios.saveAndFlush(i);
        registrar("REACTIVAR", i, usuario, stock, stock, motivo);
    }
}
