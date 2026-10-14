package com.example.sistemamedico.controller;

import com.example.sistemamedico.model.ExamenLaboratorio;
import com.example.sistemamedico.model.Usuario;
import com.example.sistemamedico.service.ExamenLaboratorioAdminService;
import com.example.sistemamedico.service.LaboratorioAdminService;
import com.example.sistemamedico.service.PermisoRolService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/examenes-laboratorio")
public class ExamenLaboratorioAdminController {
    private final ExamenLaboratorioAdminService examenService;
    private final LaboratorioAdminService laboratorioService;
    private final PermisoRolService permisos;

    public ExamenLaboratorioAdminController(ExamenLaboratorioAdminService examenService,
                                            LaboratorioAdminService laboratorioService,
                                            PermisoRolService permisos) {
        this.examenService = examenService;
        this.laboratorioService = laboratorioService;
        this.permisos = permisos;
    }

    private Usuario administrador(HttpSession session) {
        Object sesion = session.getAttribute("usuarioAdmin");
        if (!(sesion instanceof Usuario usuario)) return null;
        if (!Boolean.TRUE.equals(usuario.getActivo()) || usuario.getRol() == null
                || !"ADMINISTRADOR".equalsIgnoreCase(usuario.getRol().getNombre())) return null;
        return usuario;
    }

    private String validarAcceso(HttpSession session, RedirectAttributes flash, String permiso) {
        Usuario admin = administrador(session);
        if (admin == null) return "redirect:/admin/login";
        if (!permisos.tienePermiso(admin, permiso)) {
            flash.addFlashAttribute("errorGeneral", "No tiene permisos para realizar esta operación.");
            return "redirect:/admin/usuarios";
        }
        return null;
    }

    private void datosFormulario(Model model, HttpSession session) {
        model.addAttribute("usuarioActual", administrador(session).getNombreUsuario());
        model.addAttribute("laboratorios", laboratorioService.obtenerActivos());
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "") String buscar,
                         @RequestParam(defaultValue = "0") int pagina,
                         @RequestParam(defaultValue = "10") int tamanio,
                         HttpSession session, Model model, RedirectAttributes flash) {
        String acceso = validarAcceso(session, flash, "lab-exam/read");
        if (acceso != null) return acceso;
        model.addAttribute("examenes", examenService.listar(buscar, pagina, tamanio));
        model.addAttribute("buscar", buscar);
        model.addAttribute("tamanio", (tamanio == 25 || tamanio == 50) ? tamanio : 10);
        model.addAttribute("usuarioActual", administrador(session).getNombreUsuario());
        return "admin/examenes-laboratorio/listar";
    }

    @GetMapping("/crear")
    public String crearFormulario(HttpSession session, Model model, RedirectAttributes flash) {
        String acceso = validarAcceso(session, flash, "lab-exam/create");
        if (acceso != null) return acceso;
        datosFormulario(model, session);
        return "admin/examenes-laboratorio/crear";
    }

    @PostMapping("/crear")
    public String crear(@RequestParam String nombre,
                        @RequestParam(required = false) String descripcion,
                        @RequestParam BigDecimal precio,
                        @RequestParam(required = false) String rangoReferencia,
                        @RequestParam(required = false) String unidad,
                        @RequestParam(required = false) Long laboratorioId,
                        HttpSession session, RedirectAttributes flash) {
        String acceso = validarAcceso(session, flash, "lab-exam/create");
        if (acceso != null) return acceso;
        try {
            ExamenLaboratorio examen = examenService.crear(nombre, descripcion, precio,
                    rangoReferencia, unidad, laboratorioId, administrador(session).getNombreUsuario());
            flash.addFlashAttribute("mensajeExito", "El examen " + examen.getNombre() + " ha sido creado exitosamente.");
            return "redirect:/admin/examenes-laboratorio";
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/admin/examenes-laboratorio/crear";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarFormulario(@PathVariable Long id,
                                   HttpSession session, Model model, RedirectAttributes flash) {
        String acceso = validarAcceso(session, flash, "lab-exam/update");
        if (acceso != null) return acceso;
        try {
            model.addAttribute("examen", examenService.obtener(id));
            datosFormulario(model, session);
            return "admin/examenes-laboratorio/editar";
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/admin/examenes-laboratorio";
        }
    }

    @PostMapping("/editar/{id}")
    public String editar(@PathVariable Long id,
                         @RequestParam String nombre,
                         @RequestParam(required = false) String descripcion,
                         @RequestParam BigDecimal precio,
                         @RequestParam(required = false) String rangoReferencia,
                         @RequestParam(required = false) String unidad,
                         @RequestParam(required = false) Long laboratorioId,
                         HttpSession session, RedirectAttributes flash) {
        String acceso = validarAcceso(session, flash, "lab-exam/update");
        if (acceso != null) return acceso;
        try {
            ExamenLaboratorio examen = examenService.actualizar(id, nombre, descripcion,
                    precio, rangoReferencia, unidad, laboratorioId,
                    administrador(session).getNombreUsuario());
            flash.addFlashAttribute("mensajeExito", "El examen " + examen.getNombre() + " ha sido actualizado correctamente.");
            return "redirect:/admin/examenes-laboratorio";
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/admin/examenes-laboratorio/editar/" + id;
        }
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        String acceso = validarAcceso(session, flash, "lab-exam/delete");
        if (acceso != null) return acceso;
        try {
            examenService.desactivar(id, administrador(session).getNombreUsuario());
            flash.addFlashAttribute("mensajeExito", "El examen ha sido desactivado correctamente.");
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/admin/examenes-laboratorio";
    }

    @PostMapping("/reactivar/{id}")
    public String reactivar(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        String acceso = validarAcceso(session, flash, "lab-exam/reactivate");
        if (acceso != null) return acceso;
        try {
            examenService.reactivar(id, administrador(session).getNombreUsuario());
            flash.addFlashAttribute("mensajeExito", "El examen ha sido reactivado correctamente.");
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/admin/examenes-laboratorio";
    }
}
