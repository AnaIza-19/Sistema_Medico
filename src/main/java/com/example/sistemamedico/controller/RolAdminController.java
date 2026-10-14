package com.example.sistemamedico.controller;

import com.example.sistemamedico.model.Rol;
import com.example.sistemamedico.model.Usuario;
import com.example.sistemamedico.service.PermisoRolService;
import com.example.sistemamedico.service.RolAdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/roles")
public class RolAdminController {
    private final RolAdminService service;
    private final PermisoRolService permisos;

    public RolAdminController(RolAdminService service, PermisoRolService permisos) {
        this.service = service;
        this.permisos = permisos;
    }

    private Usuario administrador(HttpSession session) {
        Object actual = session.getAttribute("usuarioAdmin");
        if (!(actual instanceof Usuario usuario)) return null;
        if (!Boolean.TRUE.equals(usuario.getActivo())) return null;
        if (usuario.getRol() == null ||
                !"ADMINISTRADOR".equalsIgnoreCase(usuario.getRol().getNombre())) return null;
        return usuario;
    }

    private boolean autorizado(HttpSession session, String codigo) {
        Usuario admin = administrador(session);
        return admin != null && permisos.tienePermiso(admin, codigo);
    }

    private String denegado(HttpSession session, RedirectAttributes flash) {
        if (administrador(session) == null) return "redirect:/admin/login";
        flash.addFlashAttribute("errorGeneral", "No tiene permisos para esta operación.");
        return "redirect:/admin/usuarios";
    }

    private void agregarAdmin(Model model, HttpSession session) {
        model.addAttribute("adminName", administrador(session).getNombreUsuario());
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "") String buscar,
                         @RequestParam(defaultValue = "0") int pagina,
                         @RequestParam(defaultValue = "10") int tamanio,
                         Model model, HttpSession session, RedirectAttributes flash) {
        if (!autorizado(session, "role/read")) return denegado(session, flash);
        Page<Rol> paginaRoles = service.listar(buscar, pagina, tamanio);
        Map<Long, Long> usuariosPorRol = new HashMap<>();
        Map<Long, Long> permisosPorRol = new HashMap<>();
        for (Rol r : paginaRoles.getContent()) {
            usuariosPorRol.put(r.getId(), service.contarUsuarios(r.getId()));
            permisosPorRol.put(r.getId(), service.contarPermisos(r.getId()));
        }
        model.addAttribute("roles", paginaRoles);
        model.addAttribute("usuariosPorRol", usuariosPorRol);
        model.addAttribute("permisosPorRol", permisosPorRol);
        model.addAttribute("buscar", buscar);
        model.addAttribute("tamanio", (tamanio == 25 || tamanio == 50) ? tamanio : 10);
        agregarAdmin(model, session);
        return "admin/roles/listar";
    }

    @GetMapping("/crear")
    public String formularioCrear(Model model, HttpSession session, RedirectAttributes flash) {
        if (!autorizado(session, "role/create")) return denegado(session, flash);
        agregarAdmin(model, session);
        return "admin/roles/crear";
    }

    @PostMapping("/crear")
    public String crear(@RequestParam String nombre,
                        @RequestParam(required = false) String descripcion,
                        HttpSession session, RedirectAttributes flash) {
        if (!autorizado(session, "role/create")) return denegado(session, flash);
        try {
            service.crear(nombre, descripcion, administrador(session).getNombreUsuario());
            flash.addFlashAttribute("mensajeExito", "Rol creado correctamente.");
            return "redirect:/admin/roles";
        } catch (RuntimeException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/admin/roles/crear";
        }
    }

    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable Long id,
                                   Model model, HttpSession session, RedirectAttributes flash) {
        if (!autorizado(session, "role/update")) return denegado(session, flash);
        try {
            Rol rol = service.obtener(id);
            if (Boolean.TRUE.equals(rol.getEsSistema())) {
                flash.addFlashAttribute("mensajeError", "Los roles internos están protegidos.");
                return "redirect:/admin/roles";
            }
            model.addAttribute("rol", rol);
            model.addAttribute("usuariosAsignados", service.contarUsuarios(id));
            agregarAdmin(model, session);
            return "admin/roles/editar";
        } catch (RuntimeException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/admin/roles";
        }
    }

    @PostMapping("/editar/{id}")
    public String editar(@PathVariable Long id, @RequestParam String nombre,
                         @RequestParam(required = false) String descripcion,
                         HttpSession session, RedirectAttributes flash) {
        if (!autorizado(session, "role/update")) return denegado(session, flash);
        try {
            service.actualizar(id, nombre, descripcion, administrador(session).getNombreUsuario());
            flash.addFlashAttribute("mensajeExito", "Rol actualizado correctamente.");
            return "redirect:/admin/roles";
        } catch (RuntimeException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/admin/roles/editar/" + id;
        }
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        if (!autorizado(session, "role/delete")) return denegado(session, flash);
        try {
            service.desactivar(id, administrador(session).getNombreUsuario());
            flash.addFlashAttribute("mensajeExito", "Rol desactivado correctamente.");
        } catch (RuntimeException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/admin/roles";
    }

    @PostMapping("/reactivar/{id}")
    public String reactivar(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        if (!autorizado(session, "role/reactivate")) return denegado(session, flash);
        try {
            service.reactivar(id, administrador(session).getNombreUsuario());
            flash.addFlashAttribute("mensajeExito", "Rol reactivado correctamente.");
        } catch (RuntimeException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/admin/roles";
    }

    @GetMapping("/permisos/{id}")
    public String formularioPermisos(@PathVariable Long id, Model model,
                                     HttpSession session, RedirectAttributes flash) {
        if (!autorizado(session, "role/update")) return denegado(session, flash);
        try {
            Rol rol = service.obtener(id);
            if (Boolean.TRUE.equals(rol.getEsSistema()) || !Boolean.TRUE.equals(rol.getActivo())) {
                flash.addFlashAttribute("mensajeError", "Los permisos de este rol no pueden modificarse.");
                return "redirect:/admin/roles";
            }
            model.addAttribute("rol", rol);
            model.addAttribute("disponibles", service.permisosDisponibles());
            model.addAttribute("asignados", service.permisosDelRol(id));
            agregarAdmin(model, session);
            return "admin/roles/permisos";
        } catch (RuntimeException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/admin/roles";
        }
    }

    @PostMapping("/permisos/{id}")
    public String guardarPermisos(@PathVariable Long id,
                                  @RequestParam(required = false) List<String> codigos,
                                  HttpSession session, RedirectAttributes flash) {
        if (!autorizado(session, "role/update")) return denegado(session, flash);
        try {
            service.guardarPermisos(id, codigos, administrador(session).getNombreUsuario());
            flash.addFlashAttribute("mensajeExito", "Permisos actualizados correctamente.");
            return "redirect:/admin/roles";
        } catch (RuntimeException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/admin/roles/permisos/" + id;
        }
    }
}
