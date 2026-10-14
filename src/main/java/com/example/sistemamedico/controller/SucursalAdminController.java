package com.example.sistemamedico.controller;

import com.example.sistemamedico.model.Sucursal;
import com.example.sistemamedico.model.Usuario;
import com.example.sistemamedico.service.PermisoRolService;
import com.example.sistemamedico.service.SucursalAdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/sucursales")
public class SucursalAdminController {

    private final SucursalAdminService servicio;
    private final PermisoRolService permisos;

    public SucursalAdminController(SucursalAdminService servicio,
                                   PermisoRolService permisos) {
        this.servicio = servicio;
        this.permisos = permisos;
    }

    private Usuario admin(HttpSession session) {
        Object obj = session.getAttribute("usuarioAdmin");
        if (!(obj instanceof Usuario u)) return null;
        if (!Boolean.TRUE.equals(u.getActivo()) || u.getRol() == null ||
                !"ADMINISTRADOR".equalsIgnoreCase(u.getRol().getNombre())) return null;
        return u;
    }

    private boolean autorizado(HttpSession session, String permiso) {
        Usuario u = admin(session);
        return u != null && permisos.tienePermiso(u, permiso);
    }

    private String denegado(HttpSession session, RedirectAttributes flash) {
        if (admin(session) == null) return "redirect:/admin/login";
        flash.addFlashAttribute("errorGeneral", "No tiene permisos para realizar esta operación.");
        return "redirect:/admin/usuarios";
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "") String buscar,
                         @RequestParam(defaultValue = "0") int pagina,
                         @RequestParam(defaultValue = "10") int tamanio,
                         Model model, HttpSession session, RedirectAttributes flash) {
        if (!autorizado(session, "branch/read")) return denegado(session, flash);
        Page<Sucursal> datos = servicio.listar(buscar, pagina, tamanio);
        model.addAttribute("sucursales", datos);
        model.addAttribute("buscar", buscar);
        model.addAttribute("tamanio", (tamanio == 25 || tamanio == 50) ? tamanio : 10);
        model.addAttribute("usuarioActual", admin(session).getNombreUsuario());
        return "admin/sucursales/listar";
    }

    @GetMapping("/crear")
    public String formularioCrear(HttpSession session, Model model, RedirectAttributes flash) {
        if (!autorizado(session, "branch/create")) return denegado(session, flash);
        model.addAttribute("usuarioActual", admin(session).getNombreUsuario());
        return "admin/sucursales/crear";
    }

    @PostMapping("/crear")
    public String crear(@RequestParam String nombre, @RequestParam String telefono,
                        @RequestParam String direccion, @RequestParam String descripcion,
                        HttpSession session, RedirectAttributes flash) {
        if (!autorizado(session, "branch/create")) return denegado(session, flash);
        try {
            servicio.crear(nombre, telefono, direccion, descripcion, admin(session).getNombreUsuario());
            flash.addFlashAttribute("mensajeExito", "Sucursal creada correctamente.");
            return "redirect:/admin/sucursales";
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/admin/sucursales/crear";
        }
    }

    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable Long id, HttpSession session,
                                   Model model, RedirectAttributes flash) {
        if (!autorizado(session, "branch/update")) return denegado(session, flash);
        try {
            model.addAttribute("sucursal", servicio.buscarPorId(id));
            model.addAttribute("usuarioActual", admin(session).getNombreUsuario());
            return "admin/sucursales/editar";
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/admin/sucursales";
        }
    }

    @PostMapping("/editar/{id}")
    public String editar(@PathVariable Long id, @RequestParam String nombre,
                         @RequestParam String telefono, @RequestParam String direccion,
                         @RequestParam String descripcion,
                         HttpSession session, RedirectAttributes flash) {
        if (!autorizado(session, "branch/update")) return denegado(session, flash);
        try {
            servicio.actualizar(id, nombre, telefono, direccion, descripcion, admin(session).getNombreUsuario());
            flash.addFlashAttribute("mensajeExito", "Sucursal actualizada correctamente.");
            return "redirect:/admin/sucursales";
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/admin/sucursales/editar/" + id;
        }
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        if (!autorizado(session, "branch/delete")) return denegado(session, flash);
        try {
            servicio.desactivar(id, admin(session).getNombreUsuario());
            flash.addFlashAttribute("mensajeExito", "Sucursal desactivada correctamente.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/admin/sucursales";
    }

    @PostMapping("/reactivar/{id}")
    public String reactivar(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        if (!autorizado(session, "branch/reactivate")) return denegado(session, flash);
        try {
            servicio.reactivar(id, admin(session).getNombreUsuario());
            flash.addFlashAttribute("mensajeExito", "Sucursal reactivada correctamente.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/admin/sucursales";
    }
}

