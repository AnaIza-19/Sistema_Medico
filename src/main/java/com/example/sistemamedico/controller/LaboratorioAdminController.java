package com.example.sistemamedico.controller;

import com.example.sistemamedico.model.Usuario;
import com.example.sistemamedico.service.LaboratorioAdminService;
import com.example.sistemamedico.service.PermisoRolService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/laboratorios")
public class LaboratorioAdminController {
    private final LaboratorioAdminService laboratorioService;
    private final PermisoRolService permisos;

    public LaboratorioAdminController(LaboratorioAdminService laboratorioService,
                                      PermisoRolService permisos) {
        this.laboratorioService = laboratorioService;
        this.permisos = permisos;
    }

    private Usuario administrador(HttpSession session) {
        Object sesion = session.getAttribute("usuarioAdmin");
        if (!(sesion instanceof Usuario usuario)) return null;
        if (!Boolean.TRUE.equals(usuario.getActivo()) || usuario.getRol() == null
                || !"ADMINISTRADOR".equalsIgnoreCase(usuario.getRol().getNombre())) {
            return null;
        }
        return usuario;
    }

    private String validarAcceso(HttpSession session, RedirectAttributes flash, String codigo) {
        Usuario usuario = administrador(session);
        if (usuario == null) return "redirect:/admin/login";
        if (!permisos.tienePermiso(usuario, codigo)) {
            flash.addFlashAttribute("errorGeneral", "No tiene permisos para realizar esta operación.");
            return "redirect:/admin/usuarios";
        }
        return null;
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "") String buscar,
                         @RequestParam(defaultValue = "0") int pagina,
                         @RequestParam(defaultValue = "10") int tamanio,
                         HttpSession session, Model model, RedirectAttributes flash) {
        String redireccion = validarAcceso(session, flash, "laboratory/read");
        if (redireccion != null) return redireccion;
        model.addAttribute("laboratorios", laboratorioService.listar(buscar, pagina, tamanio));
        model.addAttribute("buscar", buscar);
        model.addAttribute("tamanio", (tamanio == 25 || tamanio == 50) ? tamanio : 10);
        model.addAttribute("usuarioActual", administrador(session).getNombreUsuario());
        return "admin/laboratorios/listar";
    }

    @GetMapping("/crear")
    public String crearFormulario(HttpSession session, Model model, RedirectAttributes flash) {
        String redireccion = validarAcceso(session, flash, "laboratory/create");
        if (redireccion != null) return redireccion;
        model.addAttribute("usuarioActual", administrador(session).getNombreUsuario());
        return "admin/laboratorios/crear";
    }

    @PostMapping("/crear")
    public String crear(@RequestParam String nombre, @RequestParam String telefono,
                        @RequestParam String direccion, @RequestParam String descripcion,
                        HttpSession session, RedirectAttributes flash) {
        String redireccion = validarAcceso(session, flash, "laboratory/create");
        if (redireccion != null) return redireccion;
        try {
            laboratorioService.crear(nombre, telefono, direccion, descripcion,
                    administrador(session).getNombreUsuario());
            flash.addFlashAttribute("mensajeExito", "Laboratorio creado correctamente.");
            return "redirect:/admin/laboratorios";
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/admin/laboratorios/crear";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarFormulario(@PathVariable Long id, HttpSession session,
                                   Model model, RedirectAttributes flash) {
        String redireccion = validarAcceso(session, flash, "laboratory/update");
        if (redireccion != null) return redireccion;
        try {
            model.addAttribute("laboratorio", laboratorioService.obtener(id));
            model.addAttribute("usuarioActual", administrador(session).getNombreUsuario());
            return "admin/laboratorios/editar";
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/admin/laboratorios";
        }
    }

    @PostMapping("/editar/{id}")
    public String actualizar(@PathVariable Long id,
                             @RequestParam String nombre, @RequestParam String telefono,
                             @RequestParam String direccion, @RequestParam String descripcion,
                             HttpSession session, RedirectAttributes flash) {
        String redireccion = validarAcceso(session, flash, "laboratory/update");
        if (redireccion != null) return redireccion;
        try {
            laboratorioService.actualizar(id, nombre, telefono, direccion, descripcion,
                    administrador(session).getNombreUsuario());
            flash.addFlashAttribute("mensajeExito", "Laboratorio actualizado correctamente.");
            return "redirect:/admin/laboratorios";
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/admin/laboratorios/editar/" + id;
        }
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        String redireccion = validarAcceso(session, flash, "laboratory/delete");
        if (redireccion != null) return redireccion;
        try {
            laboratorioService.desactivar(id, administrador(session).getNombreUsuario());
            flash.addFlashAttribute("mensajeExito", "Laboratorio desactivado correctamente.");
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/admin/laboratorios";
    }

    @PostMapping("/reactivar/{id}")
    public String reactivar(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        String redireccion = validarAcceso(session, flash, "laboratory/reactivate");
        if (redireccion != null) return redireccion;
        try {
            laboratorioService.reactivar(id, administrador(session).getNombreUsuario());
            flash.addFlashAttribute("mensajeExito", "Laboratorio reactivado correctamente.");
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/admin/laboratorios";
    }
}
