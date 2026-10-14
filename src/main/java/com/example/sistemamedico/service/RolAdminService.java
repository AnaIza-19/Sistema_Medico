package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.AuditoriaRol_Repositorio;
import com.example.sistemamedico.Repositorio.Rol_Repositorio;
import com.example.sistemamedico.model.AuditoriaRol;
import com.example.sistemamedico.model.Rol;
import jakarta.persistence.EntityManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class RolAdminService {

    private final Rol_Repositorio roles;
    private final AuditoriaRol_Repositorio auditoria;
    private final EntityManager em;

    public RolAdminService(Rol_Repositorio roles,
                           AuditoriaRol_Repositorio auditoria,
                           EntityManager em) {
        this.roles = roles;
        this.auditoria = auditoria;
        this.em = em;
    }

    @Transactional(readOnly = true)
    public Page<Rol> listar(String buscar, int pagina, int tamanio) {
        int tam = (tamanio == 25 || tamanio == 50) ? tamanio : 10;
        Pageable pag = PageRequest.of(Math.max(0, pagina), tam, Sort.by("id").ascending());
        String filtro = buscar == null ? "" : buscar.trim();
        return filtro.isEmpty() ? roles.findAll(pag) :
                roles.findByNombreContainingIgnoreCase(filtro, pag);
    }

    @Transactional(readOnly = true)
    public Rol obtener(Long id) {
        return roles.findById(id).orElseThrow(
                () -> new IllegalArgumentException("El rol no existe."));
    }

    // Se consulta la tabla existente, sin cambiar Usuario.java ni su repositorio.
    @Transactional(readOnly = true)
    public long contarUsuarios(Long rolId) {
        Number n = (Number) em.createNativeQuery(
                        "SELECT COUNT(*) FROM public.usuarios WHERE rol_id = :id")
                .setParameter("id", rolId).getSingleResult();
        return n.longValue();
    }

    @Transactional(readOnly = true)
    public long contarPermisos(Long rolId) {
        Number n = (Number) em.createNativeQuery(
                        "SELECT COUNT(*) FROM public.permisos_rol WHERE rol_id = :id")
                .setParameter("id", rolId).getSingleResult();
        return n.longValue();
    }

    @Transactional(readOnly = true)
    public List<String> permisosDelRol(Long rolId) {
        @SuppressWarnings("unchecked")
        List<Object> resultados = em.createNativeQuery(
                        "SELECT codigo FROM public.permisos_rol WHERE rol_id = :id ORDER BY codigo")
                .setParameter("id", rolId).getResultList();
        List<String> lista = new ArrayList<>();
        for (Object codigo : resultados) lista.add(codigo.toString());
        return lista;
    }

    @Transactional(readOnly = true)
    public List<String> permisosDisponibles() {
        @SuppressWarnings("unchecked")
        List<Object> resultados = em.createNativeQuery(
                        "SELECT DISTINCT codigo FROM public.permisos_rol ORDER BY codigo")
                .getResultList();
        List<String> lista = new ArrayList<>();
        for (Object codigo : resultados) lista.add(codigo.toString());
        return lista;
    }

    private String nombreValido(String nombre) {
        String n = nombre == null ? "" : nombre.trim().toUpperCase(Locale.ROOT);
        if (!n.matches("[A-Z][A-Z0-9_]{2,29}"))
            throw new IllegalArgumentException(
                    "El nombre debe tener de 3 a 30 caracteres (A-Z, 0-9 y guion bajo), iniciando con letra.");
        return n;
    }

    private String descripcionValida(String descripcion) {
        String d = descripcion == null ? "" : descripcion.trim();
        if (d.length() > 255)
            throw new IllegalArgumentException("La descripción supera los 255 caracteres.");
        return d.isEmpty() ? null : d;
    }

    private String actorValido(String actor) {
        if (actor == null || actor.isBlank())
            throw new IllegalArgumentException("No se identificó al administrador.");
        return actor.trim();
    }

    private void exigirPersonalizado(Rol rol) {
        if (Boolean.TRUE.equals(rol.getEsSistema()))
            throw new IllegalArgumentException("Este rol interno del hospital está protegido.");
    }

    private String capturar(Rol r) {
        return "ID=" + r.getId() + " | nombre=" + r.getNombre()
                + " | descripcion=" + r.getDescripcion()
                + " | activo=" + r.getActivo()
                + " | esSistema=" + r.getEsSistema();
    }

    private void registrar(String accion, Rol rol, String actor,
                           String anteriores, String detalle, String nuevos) {
        AuditoriaRol a = new AuditoriaRol();
        a.setRolId(rol.getId());
        a.setNombreRol(rol.getNombre());
        a.setAccion(accion);
        a.setEjecutadoPor(actor);
        a.setDatosAnteriores(anteriores);
        a.setDatosNuevos(nuevos);
        a.setDetalle(detalle);
        auditoria.saveAndFlush(a);
    }

    @Transactional
    @CacheEvict(cacheNames = "rolesAdmin", allEntries = true)
    public Rol crear(String nombre, String descripcion, String ejecutadoPor) {
        String actor = actorValido(ejecutadoPor);
        String n = nombreValido(nombre);
        if (roles.existsByNombreIgnoreCase(n))
            throw new IllegalArgumentException("Ya existe un rol con ese nombre.");
        Rol rol = new Rol(n);
        rol.setDescripcion(descripcionValida(descripcion));
        rol.setActivo(true);
        rol.setEsSistema(false);
        roles.saveAndFlush(rol);
        registrar("CREAR", rol, actor, null, "Creación de rol personalizado", capturar(rol));
        return rol;
    }

    @Transactional
    @CacheEvict(cacheNames = "rolesAdmin", allEntries = true)
    public void actualizar(Long id, String nombre, String descripcion, String ejecutadoPor) {
        String actor = actorValido(ejecutadoPor);
        Rol rol = obtener(id);
        exigirPersonalizado(rol);
        String nuevoNombre = nombreValido(nombre);
        if (!rol.getNombre().equalsIgnoreCase(nuevoNombre) && contarUsuarios(id) > 0)
            throw new IllegalArgumentException(
                    "No se puede renombrar un rol asignado a usuarios.");
        if (roles.existsByNombreIgnoreCaseAndIdNot(nuevoNombre, id))
            throw new IllegalArgumentException("Ya existe otro rol con ese nombre.");
        String antes = capturar(rol);
        rol.setNombre(nuevoNombre);
        rol.setDescripcion(descripcionValida(descripcion));
        roles.saveAndFlush(rol);
        registrar("ACTUALIZAR", rol, actor, antes, "Actualización de rol", capturar(rol));
    }

    @Transactional
    @CacheEvict(cacheNames = "rolesAdmin", allEntries = true)
    public void desactivar(Long id, String ejecutadoPor) {
        String actor = actorValido(ejecutadoPor);
        Rol rol = obtener(id);
        exigirPersonalizado(rol);
        if (!Boolean.TRUE.equals(rol.getActivo()))
            throw new IllegalArgumentException("El rol ya está inactivo.");
        if (contarUsuarios(id) > 0)
            throw new IllegalArgumentException("No se puede desactivar: hay usuarios asignados a este rol.");
        String antes = capturar(rol);
        rol.setActivo(false);
        roles.saveAndFlush(rol);
        registrar("DESACTIVAR", rol, actor, antes, "Desactivación lógica", capturar(rol));
    }

    @Transactional
    @CacheEvict(cacheNames = "rolesAdmin", allEntries = true)
    public void reactivar(Long id, String ejecutadoPor) {
        String actor = actorValido(ejecutadoPor);
        Rol rol = obtener(id);
        exigirPersonalizado(rol);
        if (Boolean.TRUE.equals(rol.getActivo()))
            throw new IllegalArgumentException("El rol ya está activo.");
        String antes = capturar(rol);
        rol.setActivo(true);
        roles.saveAndFlush(rol);
        registrar("REACTIVAR", rol, actor, antes, "Reactivación de rol", capturar(rol));
    }

    @Transactional
    @CacheEvict(cacheNames = "rolesAdmin", allEntries = true)
    public void guardarPermisos(Long id, List<String> elegidos, String ejecutadoPor) {
        String actor = actorValido(ejecutadoPor);
        Rol rol = obtener(id);
        exigirPersonalizado(rol);
        if (!Boolean.TRUE.equals(rol.getActivo()))
            throw new IllegalArgumentException("No se pueden asignar permisos a un rol inactivo.");

        Set<String> permitidos = new LinkedHashSet<>(permisosDisponibles());
        Set<String> nuevos = new LinkedHashSet<>(elegidos == null ? List.of() : elegidos);
        if (!permitidos.containsAll(nuevos))
            throw new IllegalArgumentException("Se detectó un código de permiso no permitido.");

        List<String> anteriores = permisosDelRol(id);
        if (new LinkedHashSet<>(anteriores).equals(nuevos)) return;

        em.createNativeQuery("DELETE FROM public.permisos_rol WHERE rol_id = :id")
                .setParameter("id", id).executeUpdate();
        for (String codigo : nuevos) {
            em.createNativeQuery(
                            "INSERT INTO public.permisos_rol (rol_id,codigo) VALUES (:id,:codigo) "
                                    + "ON CONFLICT (rol_id,codigo) DO NOTHING")
                    .setParameter("id", id).setParameter("codigo", codigo).executeUpdate();
        }
        registrar("PERMISOS", rol, actor, anteriores.toString(),
                "Actualización de permisos del rol personalizado", nuevos.toString());
    }
}
