package com.example.sistemamedico.controller;

import com.example.sistemamedico.model.EstadoCita;
import com.example.sistemamedico.model.Usuario;
import com.example.sistemamedico.service.EstadoCitaAdminService;
import com.example.sistemamedico.service.PermisoRolService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/estados-cita")
public class EstadoCitaAdminController {

    private final EstadoCitaAdminService estadoService;
    private final PermisoRolService permisoRolService;

    public EstadoCitaAdminController(EstadoCitaAdminService estadoService,
                                     PermisoRolService permisoRolService) {
        this.estadoService = estadoService;
        this.permisoRolService = permisoRolService;
    }

    private Usuario obtenerAdmin(HttpSession session) {
        Object guardado = session.getAttribute("usuarioAdmin");
        if (!(guardado instanceof Usuario usuario)) return null;
        if (!Boolean.TRUE.equals(usuario.getActivo()) || usuario.getRol() == null
                || !"ADMINISTRADOR".equalsIgnoreCase(usuario.getRol().getNombre())) return null;
        return usuario;
    }

    private boolean autorizado(HttpSession session, String permiso) {
        Usuario admin = obtenerAdmin(session);
        return admin != null && permisoRolService.tienePermiso(admin, permiso);
    }

    private String denegar(HttpSession session, RedirectAttributes flash) {
        if (obtenerAdmin(session) == null) return "redirect:/admin/login";
        flash.addFlashAttribute("errorGeneral", "No tiene permisos para realizar esta operación.");
        return "redirect:/admin/usuarios";
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "") String buscar,
                         @RequestParam(defaultValue = "0") int pagina,
                         @RequestParam(defaultValue = "10") int tamanio,
                         HttpSession session, Model model, RedirectAttributes flash) {
        if (!autorizado(session, "appointment-status/read")) return denegar(session, flash);
        Page<EstadoCita> estados = estadoService.listar(buscar, pagina, tamanio);
        model.addAttribute("estados", estados);
        model.addAttribute("buscar", buscar);
        model.addAttribute("tamanio", (tamanio == 25 || tamanio == 50) ? tamanio : 10);
        model.addAttribute("usuarioActual", obtenerAdmin(session).getNombreUsuario());
        return "admin/estados-cita/listar";
    }

    @GetMapping("/crear")
    public String formularioCrear(HttpSession session, Model model, RedirectAttributes flash) {
        if (!autorizado(session, "appointment-status/create")) return denegar(session, flash);
        model.addAttribute("usuarioActual", obtenerAdmin(session).getNombreUsuario());
        return "admin/estados-cita/crear";
    }

    @PostMapping("/crear")
    public String crear(@RequestParam String codigo,
                        @RequestParam String nombre,
                        @RequestParam(required = false) String descripcion,
                        HttpSession session, RedirectAttributes flash) {
        if (!autorizado(session, "appointment-status/create")) return denegar(session, flash);
        try {
            estadoService.crear(codigo, nombre, descripcion, obtenerAdmin(session).getNombreUsuario());
            flash.addFlashAttribute("mensajeExito", "Estado de cita creado correctamente.");
            return "redirect:/admin/estados-cita";
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
            flash.addFlashAttribute("codigoAnterior", codigo);
            flash.addFlashAttribute("nombreAnterior", nombre);
            flash.addFlashAttribute("descripcionAnterior", descripcion);
            return "redirect:/admin/estados-cita/crear";
        }
    }

    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable Long id, HttpSession session,
                                   Model model, RedirectAttributes flash) {
        if (!autorizado(session, "appointment-status/update")) return denegar(session, flash);
        try {
            model.addAttribute("estado", estadoService.obtener(id));
            model.addAttribute("usuarioActual", obtenerAdmin(session).getNombreUsuario());
            return "admin/estados-cita/editar";
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/admin/estados-cita";
        }
    }

    @PostMapping("/editar/{id}")
    public String editar(@PathVariable Long id, @RequestParam String nombre,
                         @RequestParam(required = false) String descripcion,
                         HttpSession session, RedirectAttributes flash) {
        if (!autorizado(session, "appointment-status/update")) return denegar(session, flash);
        try {
            estadoService.actualizar(id, nombre, descripcion, obtenerAdmin(session).getNombreUsuario());
            flash.addFlashAttribute("mensajeExito", "Estado de cita actualizado correctamente.");
            return "redirect:/admin/estados-cita";
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/admin/estados-cita/editar/" + id;
        }
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        if (!autorizado(session, "appointment-status/delete")) return denegar(session, flash);
        try {
            estadoService.desactivar(id, obtenerAdmin(session).getNombreUsuario());
            flash.addFlashAttribute("mensajeExito", "Estado desactivado correctamente.");
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/admin/estados-cita";
    }

    @PostMapping("/reactivar/{id}")
    public String reactivar(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        if (!autorizado(session, "appointment-status/reactivate")) return denegar(session, flash);
        try {
            estadoService.reactivar(id, obtenerAdmin(session).getNombreUsuario());
            flash.addFlashAttribute("mensajeExito", "Estado reactivado correctamente.");
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/admin/estados-cita";
    }
}
