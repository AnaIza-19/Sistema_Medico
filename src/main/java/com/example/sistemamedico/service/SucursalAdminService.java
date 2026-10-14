package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.Sucursal_Repositorio;
import com.example.sistemamedico.model.Sucursal;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SucursalAdminService {

    private final Sucursal_Repositorio repositorio;
    private final AuditoriaSucursalService auditoria;

    public SucursalAdminService(Sucursal_Repositorio repositorio,
                                AuditoriaSucursalService auditoria) {
        this.repositorio = repositorio;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public Page<Sucursal> listar(String buscar, int pagina, int tamanio) {
        int tam = (tamanio == 25 || tamanio == 50) ? tamanio : 10;
        int num = Math.max(0, pagina);
        String filtro = buscar == null ? "" : buscar.trim();
        if (filtro.length() > 100) filtro = filtro.substring(0, 100);
        Page<Sucursal> resultado = repositorio.findByNombreContainingIgnoreCase(
                filtro, PageRequest.of(num, tam, Sort.by("id").ascending()));
        if (resultado.getTotalPages() > 0 && num >= resultado.getTotalPages()) {
            resultado = repositorio.findByNombreContainingIgnoreCase(filtro,
                    PageRequest.of(resultado.getTotalPages() - 1, tam, Sort.by("id").ascending()));
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public Sucursal buscarPorId(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La sucursal no existe."));
    }

    private static String requerido(String texto, String campo, int max) {
        String dato = texto == null ? "" : texto.trim();
        if (dato.isEmpty()) throw new IllegalArgumentException("El campo " + campo + " es obligatorio.");
        if (dato.length() > max) throw new IllegalArgumentException(
                "El campo " + campo + " permite como máximo " + max + " caracteres.");
        return dato;
    }

    private static String telefonoValido(String t) {
        String tel = requerido(t, "teléfono", 8);
        if (!tel.matches("[0-9]{8}")) {
            throw new IllegalArgumentException("El teléfono debe contener exactamente 8 dígitos numéricos.");
        }
        return tel;
    }

    private static void validarAdministrador(String admin) {
        if (admin == null || admin.isBlank()) {
            throw new IllegalArgumentException("No se identificó al administrador.");
        }
    }

    @Transactional
    @CacheEvict(cacheNames = {"sucursalesAdmin", "sucursalesCita", "especialidadesPorSucursal"}, allEntries = true)
    public Sucursal crear(String nombre, String telefono, String direccion,
                          String descripcion, String administrador) {
        validarAdministrador(administrador);
        String n = requerido(nombre, "nombre", 100);
        String t = telefonoValido(telefono);
        String d = requerido(direccion, "dirección", 255);
        String desc = requerido(descripcion, "descripción", 500);
        if (repositorio.existsByNombreIgnoreCaseAndActivoTrue(n)) {
            throw new IllegalArgumentException("Ya existe una sucursal activa con ese nombre.");
        }
        Sucursal s = new Sucursal();
        s.setNombre(n); s.setTelefono(t); s.setDireccion(d); s.setDescripcion(desc); s.setActivo(true);
        Sucursal guardada = repositorio.saveAndFlush(s);
        auditoria.registrar("CREAR", guardada, administrador, null, "Creación de sucursal");
        return guardada;
    }

    @Transactional
    @CacheEvict(cacheNames = {"sucursalesAdmin", "sucursalesCita", "especialidadesPorSucursal"}, allEntries = true)
    public Sucursal actualizar(Long id, String nombre, String telefono, String direccion,
                               String descripcion, String administrador) {
        validarAdministrador(administrador);
        Sucursal s = buscarPorId(id);
        String n = requerido(nombre, "nombre", 100);
        String t = telefonoValido(telefono);
        String d = requerido(direccion, "dirección", 255);
        String desc = requerido(descripcion, "descripción", 500);
        if (Boolean.TRUE.equals(s.getActivo()) &&
                repositorio.existsByNombreIgnoreCaseAndActivoTrueAndIdNot(n, id)) {
            throw new IllegalArgumentException("Ya existe otra sucursal activa con ese nombre.");
        }
        String antes = auditoria.capturarDatos(s);
        s.setNombre(n); s.setTelefono(t); s.setDireccion(d); s.setDescripcion(desc);
        Sucursal guardada = repositorio.saveAndFlush(s);
        auditoria.registrar("ACTUALIZAR", guardada, administrador, antes, "Actualización de sucursal");
        return guardada;
    }

    @Transactional
    @CacheEvict(cacheNames = {"sucursalesAdmin", "sucursalesCita", "especialidadesPorSucursal"}, allEntries = true)
    public void desactivar(Long id, String administrador) {
        validarAdministrador(administrador);
        Sucursal s = buscarPorId(id);
        if (!Boolean.TRUE.equals(s.getActivo())) {
            throw new IllegalArgumentException("La sucursal ya está inactiva.");
        }
        String antes = auditoria.capturarDatos(s);
        s.setActivo(false);
        repositorio.saveAndFlush(s);
        auditoria.registrar("DESACTIVAR", s, administrador, antes, "Desactivación lógica de sucursal");
    }

    @Transactional
    @CacheEvict(cacheNames = {"sucursalesAdmin", "sucursalesCita", "especialidadesPorSucursal"}, allEntries = true)
    public void reactivar(Long id, String administrador) {
        validarAdministrador(administrador);
        Sucursal s = buscarPorId(id);
        if (Boolean.TRUE.equals(s.getActivo())) {
            throw new IllegalArgumentException("La sucursal ya está activa.");
        }
        if (repositorio.existsByNombreIgnoreCaseAndActivoTrueAndIdNot(s.getNombre(), id)) {
            throw new IllegalArgumentException("No se puede reactivar: ya existe otra sucursal activa con ese nombre.");
        }
        String antes = auditoria.capturarDatos(s);
        s.setActivo(true);
        repositorio.saveAndFlush(s);
        auditoria.registrar("REACTIVAR", s, administrador, antes, "Reactivación de sucursal");
    }
}
