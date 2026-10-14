package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.Medicamento_Repositorio;
import com.example.sistemamedico.model.Medicamento;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MedicamentoAdminService {

    private final Medicamento_Repositorio repositorio;
    private final AuditoriaMedicamentoService auditoria;

    public MedicamentoAdminService(Medicamento_Repositorio repositorio,
                                   AuditoriaMedicamentoService auditoria) {
        this.repositorio = repositorio;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public Page<Medicamento> listar(String buscar, int pagina, int tamanio) {
        int limite = tamanio == 25 || tamanio == 50 ? tamanio : 10;
        Pageable paginacion = PageRequest.of(Math.max(0, pagina), limite,
                Sort.by("id").ascending());
        String texto = buscar == null ? "" : buscar.trim();
        return texto.isEmpty() ? repositorio.findAll(paginacion)
                : repositorio.findByNombreContainingIgnoreCase(texto, paginacion);
    }

    @Cacheable("medicamentosAdmin")
    @Transactional(readOnly = true)
    public List<Medicamento> obtenerActivos() {
        return repositorio.findByActivoTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Medicamento obtener(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicamento no encontrado."));
    }

    private String validarNombre(String nombre) {
        String limpio = nombre == null ? "" : nombre.trim();
        if (limpio.isBlank() || limpio.length() > 150)
            throw new IllegalArgumentException("El nombre es obligatorio (máximo 150 caracteres).");
        return limpio;
    }

    private String validarUnidad(String unidad) {
        String limpio = unidad == null ? "" : unidad.trim();
        if (limpio.isBlank() || limpio.length() > 50)
            throw new IllegalArgumentException("La unidad es obligatoria (máximo 50 caracteres).");
        return limpio;
    }

    private void validarPrecioYStock(BigDecimal precio, Integer stockMinimo) {
        if (precio == null || precio.signum() <= 0 || precio.scale() > 2
                || precio.compareTo(new BigDecimal("99999999.99")) > 0)
            throw new IllegalArgumentException("El precio debe ser mayor que cero, con máximo dos decimales.");
        if (stockMinimo == null || stockMinimo < 0)
            throw new IllegalArgumentException("El stock mínimo debe ser cero o un número positivo.");
    }

    private String validarActor(String ejecutadoPor) {
        if (ejecutadoPor == null || ejecutadoPor.isBlank() || ejecutadoPor.length() > 100)
            throw new IllegalArgumentException("No se identificó correctamente al administrador.");
        return ejecutadoPor.trim();
    }

    @Transactional
    @CacheEvict(cacheNames = "medicamentosAdmin", allEntries = true)
    public Medicamento crear(String nombre, BigDecimal precio, String unidad,
                             Integer stockMinimo, String ejecutadoPor) {
        String actor = validarActor(ejecutadoPor);
        String nombreLimpio = validarNombre(nombre);
        String unidadLimpia = validarUnidad(unidad);
        validarPrecioYStock(precio, stockMinimo);
        if (repositorio.existsByNombreIgnoreCase(nombreLimpio))
            throw new IllegalArgumentException("Ya existe un medicamento con ese nombre, incluso si está inactivo.");

        Medicamento m = new Medicamento();
        m.setNombre(nombreLimpio);
        m.setPrecio(precio);
        m.setUnidad(unidadLimpia);
        m.setStockMinimo(stockMinimo);
        m.setActivo(true);
        repositorio.saveAndFlush(m);
        auditoria.registrar("CREAR", m, actor, null, "Creación de medicamento");
        return m;
    }

    @Transactional
    @CacheEvict(cacheNames = "medicamentosAdmin", allEntries = true)
    public Medicamento actualizar(Long id, String nombre, BigDecimal precio,
                                  String unidad, Integer stockMinimo, String ejecutadoPor) {
        String actor = validarActor(ejecutadoPor);
        Medicamento m = obtener(id);
        String anteriores = auditoria.capturarDatos(m);
        String nombreLimpio = validarNombre(nombre);
        String unidadLimpia = validarUnidad(unidad);
        validarPrecioYStock(precio, stockMinimo);
        if (repositorio.existsByNombreIgnoreCaseAndIdNot(nombreLimpio, id))
            throw new IllegalArgumentException("Ya existe otro medicamento con ese nombre.");

        m.setNombre(nombreLimpio);
        m.setPrecio(precio);
        m.setUnidad(unidadLimpia);
        m.setStockMinimo(stockMinimo);
        repositorio.saveAndFlush(m);
        auditoria.registrar("ACTUALIZAR", m, actor, anteriores, "Actualización de medicamento");
        return m;
    }

    @Transactional
    @CacheEvict(cacheNames = "medicamentosAdmin", allEntries = true)
    public void desactivar(Long id, String ejecutadoPor) {
        String actor = validarActor(ejecutadoPor);
        Medicamento m = obtener(id);
        if (!Boolean.TRUE.equals(m.getActivo()))
            throw new IllegalArgumentException("El medicamento ya está inactivo.");
        String anteriores = auditoria.capturarDatos(m);
        m.setActivo(false);
        repositorio.saveAndFlush(m);
        auditoria.registrar("DESACTIVAR", m, actor, anteriores, "Desactivación lógica del medicamento");
    }

    @Transactional
    @CacheEvict(cacheNames = "medicamentosAdmin", allEntries = true)
    public void reactivar(Long id, String ejecutadoPor) {
        String actor = validarActor(ejecutadoPor);
        Medicamento m = obtener(id);
        if (Boolean.TRUE.equals(m.getActivo()))
            throw new IllegalArgumentException("El medicamento ya está activo.");
        // Respeta UNIQUE(nombre) en la tabla existente.
        if (repositorio.existsByNombreIgnoreCaseAndIdNot(m.getNombre(), id))
            throw new IllegalArgumentException("Existe otro medicamento con ese nombre.");
        String anteriores = auditoria.capturarDatos(m);
        m.setActivo(true);
        repositorio.saveAndFlush(m);
        auditoria.registrar("REACTIVAR", m, actor, anteriores, "Reactivación del medicamento");
    }
}
