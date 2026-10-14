package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.Laboratorio_Repositorio;
import com.example.sistemamedico.model.Laboratorio;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class LaboratorioAdminService {
    private final Laboratorio_Repositorio laboratorioRepository;
    private final AuditoriaLaboratorioService auditoriaService;

    public LaboratorioAdminService(Laboratorio_Repositorio laboratorioRepository,
                                   AuditoriaLaboratorioService auditoriaService) {
        this.laboratorioRepository = laboratorioRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public Page<Laboratorio> listar(String buscar, int pagina, int tamanio) {
        int cantidad = (tamanio == 25 || tamanio == 50) ? tamanio : 10;
        Pageable pageable = PageRequest.of(Math.max(0, pagina), cantidad,
                Sort.by("id").ascending());
        String criterio = buscar == null ? "" : buscar.trim();
        if (criterio.isEmpty()) return laboratorioRepository.findAll(pageable);
        return laboratorioRepository.findByNombreContainingIgnoreCase(criterio, pageable);
    }

    @Transactional(readOnly = true)
    public Laboratorio obtener(Long id) {
        return laboratorioRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Laboratorio no encontrado."));
    }

    @Cacheable("laboratoriosAdmin")
    @Transactional(readOnly = true)
    public List<Laboratorio> obtenerActivos() {
        return laboratorioRepository.findByActivoTrueOrderByIdAsc();
    }

    private String obligatorio(String valor, String etiqueta, int maximo) {
        String resultado = valor == null ? "" : valor.trim();
        if (resultado.isBlank() || resultado.length() > maximo) {
            throw new IllegalArgumentException(etiqueta + " es obligatorio y admite hasta "
                    + maximo + " caracteres.");
        }
        return resultado;
    }

    private String telefonoValido(String telefono) {
        String resultado = telefono == null ? "" : telefono.trim();
        if (!resultado.matches("[0-9]{8}")) {
            throw new IllegalArgumentException("El teléfono debe contener exactamente 8 dígitos.");
        }
        return resultado;
    }

    private String actorValido(String ejecutadoPor) {
        if (ejecutadoPor == null || ejecutadoPor.isBlank()) {
            throw new IllegalArgumentException("No se identificó al administrador.");
        }
        return ejecutadoPor.trim();
    }

    @Transactional
    @CacheEvict(cacheNames = "laboratoriosAdmin", allEntries = true)
    public Laboratorio crear(String nombre, String telefono, String direccion,
                             String descripcion, String ejecutadoPor) {
        String actor = actorValido(ejecutadoPor);
        String n = obligatorio(nombre, "El nombre", 150);
        String t = telefonoValido(telefono);
        String d = obligatorio(direccion, "La dirección", 255);
        String desc = obligatorio(descripcion, "La descripción", 500);
        if (laboratorioRepository.existsByNombreIgnoreCaseAndActivoTrue(n)) {
            throw new IllegalArgumentException("Ya existe un laboratorio activo con ese nombre.");
        }
        Laboratorio laboratorio = new Laboratorio();
        laboratorio.setNombre(n);
        laboratorio.setTelefono(t);
        laboratorio.setDireccion(d);
        laboratorio.setDescripcion(desc);
        laboratorio.setActivo(true);
        laboratorioRepository.saveAndFlush(laboratorio);
        auditoriaService.registrar("CREAR", laboratorio, actor, null, "Creación de laboratorio");
        return laboratorio;
    }

    @Transactional
    @CacheEvict(cacheNames = "laboratoriosAdmin", allEntries = true)
    public Laboratorio actualizar(Long id, String nombre, String telefono,
                                  String direccion, String descripcion, String ejecutadoPor) {
        String actor = actorValido(ejecutadoPor);
        Laboratorio laboratorio = obtener(id);
        String anteriores = auditoriaService.capturarDatos(laboratorio);
        String n = obligatorio(nombre, "El nombre", 150);
        String t = telefonoValido(telefono);
        String d = obligatorio(direccion, "La dirección", 255);
        String desc = obligatorio(descripcion, "La descripción", 500);
        if (Boolean.TRUE.equals(laboratorio.getActivo())
                && laboratorioRepository.existsByNombreIgnoreCaseAndActivoTrueAndIdNot(n, id)) {
            throw new IllegalArgumentException("Ya existe otro laboratorio activo con ese nombre.");
        }
        laboratorio.setNombre(n);
        laboratorio.setTelefono(t);
        laboratorio.setDireccion(d);
        laboratorio.setDescripcion(desc);
        laboratorioRepository.saveAndFlush(laboratorio);
        auditoriaService.registrar("ACTUALIZAR", laboratorio, actor, anteriores,
                "Actualización de laboratorio");
        return laboratorio;
    }

    @Transactional
    @CacheEvict(cacheNames = "laboratoriosAdmin", allEntries = true)
    public void desactivar(Long id, String ejecutadoPor) {
        String actor = actorValido(ejecutadoPor);
        Laboratorio laboratorio = obtener(id);
        if (!Boolean.TRUE.equals(laboratorio.getActivo())) {
            throw new IllegalArgumentException("El laboratorio ya está inactivo.");
        }
        String anteriores = auditoriaService.capturarDatos(laboratorio);
        laboratorio.setActivo(false);
        laboratorioRepository.saveAndFlush(laboratorio);
        auditoriaService.registrar("DESACTIVAR", laboratorio, actor, anteriores,
                "Desactivación lógica de laboratorio");
    }

    @Transactional
    @CacheEvict(cacheNames = "laboratoriosAdmin", allEntries = true)
    public void reactivar(Long id, String ejecutadoPor) {
        String actor = actorValido(ejecutadoPor);
        Laboratorio laboratorio = obtener(id);
        if (Boolean.TRUE.equals(laboratorio.getActivo())) {
            throw new IllegalArgumentException("El laboratorio ya está activo.");
        }
        if (laboratorioRepository.existsByNombreIgnoreCaseAndActivoTrue(laboratorio.getNombre())) {
            throw new IllegalArgumentException("Ya existe otro laboratorio activo con ese nombre.");
        }
        String anteriores = auditoriaService.capturarDatos(laboratorio);
        laboratorio.setActivo(true);
        laboratorioRepository.saveAndFlush(laboratorio);
        auditoriaService.registrar("REACTIVAR", laboratorio, actor, anteriores,
                "Reactivación de laboratorio");
    }
}
