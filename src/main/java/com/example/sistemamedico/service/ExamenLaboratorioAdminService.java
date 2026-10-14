package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.ExamenLaboratorio_Repositorio;
import com.example.sistemamedico.Repositorio.Laboratorio_Repositorio;
import com.example.sistemamedico.model.ExamenLaboratorio;
import com.example.sistemamedico.model.Laboratorio;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ExamenLaboratorioAdminService {
    private final ExamenLaboratorio_Repositorio examenRepository;
    private final Laboratorio_Repositorio laboratorioRepository;
    private final AuditoriaExamenLaboratorioService auditoria;

    public ExamenLaboratorioAdminService(ExamenLaboratorio_Repositorio examenRepository,
                                         Laboratorio_Repositorio laboratorioRepository,
                                         AuditoriaExamenLaboratorioService auditoria) {
        this.examenRepository = examenRepository;
        this.laboratorioRepository = laboratorioRepository;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public Page<ExamenLaboratorio> listar(String buscar, int pagina, int tamanio) {
        int cantidad = (tamanio == 25 || tamanio == 50) ? tamanio : 10;
        Pageable pageable = PageRequest.of(Math.max(pagina, 0), cantidad,
                Sort.by("id").ascending());
        String criterio = buscar == null ? "" : buscar.trim();
        return criterio.isBlank() ? examenRepository.findAll(pageable)
                : examenRepository.findByNombreContainingIgnoreCase(criterio, pageable);
    }

    @Transactional(readOnly = true)
    public ExamenLaboratorio obtener(Long id) {
        return examenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Examen de laboratorio no encontrado."));
    }

    @Cacheable("examenesLaboratorio")
    @Transactional(readOnly = true)
    public List<ExamenLaboratorio> obtenerActivos() {
        return examenRepository.findByActivoTrueOrderByNombreAsc();
    }

    private String nombreValido(String nombre) {
        String valor = nombre == null ? "" : nombre.trim();
        if (valor.isBlank()) throw new IllegalArgumentException("El nombre es obligatorio.");
        if (valor.length() > 200) throw new IllegalArgumentException("El nombre admite hasta 200 caracteres.");
        return valor;
    }

    private String opcional(String texto, String campo, int longitudMaxima) {
        String valor = texto == null ? "" : texto.trim();
        if (valor.length() > longitudMaxima)
            throw new IllegalArgumentException(campo + " admite hasta " + longitudMaxima + " caracteres.");
        return valor.isEmpty() ? null : valor;
    }

    private BigDecimal precioValido(BigDecimal precio) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("El precio base debe ser mayor a 0.");
        if (precio.scale() > 2 || precio.compareTo(new BigDecimal("99999999.99")) > 0)
            throw new IllegalArgumentException("El precio debe tener hasta 2 decimales y caber en 10,2.");
        return precio;
    }

    private Laboratorio laboratorioValido(Long id) {
        if (id == null) throw new IllegalArgumentException("Debe seleccionar un laboratorio.");
        Laboratorio laboratorio = laboratorioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El laboratorio seleccionado no existe."));
        if (!Boolean.TRUE.equals(laboratorio.getActivo()))
            throw new IllegalArgumentException("Debe seleccionar un laboratorio activo.");
        return laboratorio;
    }

    private String actorValido(String actor) {
        if (actor == null || actor.isBlank()) throw new IllegalArgumentException("No se identificó al administrador.");
        return actor.trim();
    }

    @Transactional
    @CacheEvict(cacheNames = "examenesLaboratorio", allEntries = true)
    public ExamenLaboratorio crear(String nombre, String descripcion, BigDecimal precio,
                                   String rangoReferencia, String unidad, Long laboratorioId,
                                   String ejecutadoPor) {
        String actor = actorValido(ejecutadoPor);
        String nombreLimpio = nombreValido(nombre);
        BigDecimal precioLimpio = precioValido(precio);
        String desc = opcional(descripcion, "La descripción", 500);
        String rango = opcional(rangoReferencia, "El rango de referencia", 150);
        String unidadLimpia = opcional(unidad, "La unidad", 100);
        Laboratorio lab = laboratorioValido(laboratorioId);

        // La tabla original sigue teniendo UNIQUE(nombre); protegemos su restricción global.
        if (examenRepository.existsByNombreIgnoreCase(nombreLimpio))
            throw new IllegalArgumentException("Ya existe un examen con ese nombre.");

        ExamenLaboratorio examen = new ExamenLaboratorio();
        examen.setNombre(nombreLimpio);
        examen.setPrecio(precioLimpio);
        examen.setDescripcion(desc);
        examen.setRangoReferencia(rango);
        examen.setUnidad(unidadLimpia);
        examen.setLaboratorio(lab);
        examen.setActivo(true);
        examenRepository.saveAndFlush(examen);
        auditoria.registrar("CREAR", examen, actor, null, "Creación de examen de laboratorio");
        return examen;
    }

    @Transactional
    @CacheEvict(cacheNames = "examenesLaboratorio", allEntries = true)
    public ExamenLaboratorio actualizar(Long id, String nombre, String descripcion,
                                        BigDecimal precio, String rangoReferencia,
                                        String unidad, Long laboratorioId, String ejecutadoPor) {
        String actor = actorValido(ejecutadoPor);
        ExamenLaboratorio examen = obtener(id);
        String anteriores = auditoria.capturarDatos(examen);
        String nombreLimpio = nombreValido(nombre);
        BigDecimal precioLimpio = precioValido(precio);
        String desc = opcional(descripcion, "La descripción", 500);
        String rango = opcional(rangoReferencia, "El rango de referencia", 150);
        String unidadLimpia = opcional(unidad, "La unidad", 100);
        Laboratorio lab = laboratorioValido(laboratorioId);

        if (examenRepository.existsByNombreIgnoreCaseAndIdNot(nombreLimpio, id))
            throw new IllegalArgumentException("Ya existe otro examen con ese nombre.");
        examen.setNombre(nombreLimpio);
        examen.setPrecio(precioLimpio);
        examen.setDescripcion(desc);
        examen.setRangoReferencia(rango);
        examen.setUnidad(unidadLimpia);
        examen.setLaboratorio(lab);
        examenRepository.saveAndFlush(examen);
        auditoria.registrar("ACTUALIZAR", examen, actor, anteriores, "Actualización del examen de laboratorio");
        return examen;
    }

    @Transactional
    @CacheEvict(cacheNames = "examenesLaboratorio", allEntries = true)
    public void desactivar(Long id, String ejecutadoPor) {
        String actor = actorValido(ejecutadoPor);
        ExamenLaboratorio examen = obtener(id);
        if (!Boolean.TRUE.equals(examen.getActivo()))
            throw new IllegalArgumentException("El examen ya está inactivo.");
        String anteriores = auditoria.capturarDatos(examen);
        examen.setActivo(false);
        examenRepository.saveAndFlush(examen);
        auditoria.registrar("DESACTIVAR", examen, actor, anteriores,
                "Desactivación lógica del examen de laboratorio");
    }

    @Transactional
    @CacheEvict(cacheNames = "examenesLaboratorio", allEntries = true)
    public void reactivar(Long id, String ejecutadoPor) {
        String actor = actorValido(ejecutadoPor);
        ExamenLaboratorio examen = obtener(id);
        if (Boolean.TRUE.equals(examen.getActivo()))
            throw new IllegalArgumentException("El examen ya está activo.");
        if (examen.getLaboratorio() == null || !Boolean.TRUE.equals(examen.getLaboratorio().getActivo()))
            throw new IllegalArgumentException("Debe asignar un laboratorio activo antes de reactivar.");
        String anteriores = auditoria.capturarDatos(examen);
        examen.setActivo(true);
        examenRepository.saveAndFlush(examen);
        auditoria.registrar("REACTIVAR", examen, actor, anteriores,
                "Reactivación del examen de laboratorio");
    }
}
