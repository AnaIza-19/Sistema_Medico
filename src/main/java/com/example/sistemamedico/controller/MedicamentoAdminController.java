package com.example.sistemamedico.controller;

import com.example.sistemamedico.model.Medicamento;
import com.example.sistemamedico.model.Usuario;
import com.example.sistemamedico.service.MedicamentoAdminService;
import com.example.sistemamedico.service.PermisoRolService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/medicamentos")
public class MedicamentoAdminController {

    private final MedicamentoAdminService servicio;
    private final PermisoRolService permisoRolService;

    public MedicamentoAdminController(MedicamentoAdminService servicio,
                                      PermisoRolService permisoRolService) {
        this.servicio = servicio;
        this.permisoRolService = permisoRolService;
    }

    private Usuario administrador(HttpSession session) {
        Object usuario = session.getAttribute("usuarioAdmin");
        if (!(usuario instanceof Usuario admin)) return null;
        if (!Boolean.TRUE.equals(admin.getActivo()) || admin.getRol() == null) return null;
        if (!"ADMINISTRADOR".equalsIgnoreCase(admin.getRol().getNombre())) return null;
        return admin;
    }

    private String comprobarAcceso(HttpSession session, String permiso,
                                   RedirectAttributes redirect) {
        Usuario admin = administrador(session);
        if (admin == null) return "redirect:/admin/login";
        if (!permisoRolService.tienePermiso(admin, permiso)) {
            redirect.addFlashAttribute("errorGeneral", "No tiene permisos para realizar esta operación.");
            return "redirect:/admin/usuarios";
        }
        return null;
    }

    private void datosComunes(Model model, Usuario admin) {
        model.addAttribute("usuarioActual", admin.getNombreUsuario());
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "") String buscar,
                         @RequestParam(defaultValue = "0") int pagina,
                         @RequestParam(defaultValue = "10") int tamanio,
                         HttpSession session, RedirectAttributes redirect, Model model) {
        String rechazo = comprobarAcceso(session, "medicine/read", redirect);
        if (rechazo != null) return rechazo;
        Page<Medicamento> medicamentos = servicio.listar(buscar, pagina, tamanio);
        datosComunes(model, administrador(session));
        model.addAttribute("medicamentos", medicamentos);
        model.addAttribute("buscar", buscar);
        model.addAttribute("tamanio", tamanio == 25 || tamanio == 50 ? tamanio : 10);
        return "admin/medicamentos/listar";
    }

    @GetMapping("/crear")
    public String crearFormulario(HttpSession session, RedirectAttributes redirect, Model model) {
        String rechazo = comprobarAcceso(session, "medicine/create", redirect);
        if (rechazo != null) return rechazo;
        datosComunes(model, administrador(session));
        model.addAttribute("medicamento", new Medicamento());
        return "admin/medicamentos/crear";
    }

    @PostMapping("/crear")
    public String crear(@RequestParam String nombre,
                        @RequestParam(required = false) BigDecimal precio,
                        @RequestParam String unidad,
                        @RequestParam(required = false) Integer stockMinimo,
                        HttpSession session, RedirectAttributes redirect, Model model) {
        String rechazo = comprobarAcceso(session, "medicine/create", redirect);
        if (rechazo != null) return rechazo;
        try {
            servicio.crear(nombre, precio, unidad, stockMinimo,
                    administrador(session).getNombreUsuario());
            redirect.addFlashAttribute("mensajeExito", "Medicamento creado correctamente.");
            return "redirect:/admin/medicamentos";
        } catch (IllegalArgumentException ex) {
            Medicamento m = new Medicamento();
            m.setNombre(nombre);
            m.setPrecio(precio);
            m.setUnidad(unidad);
            m.setStockMinimo(stockMinimo);
            datosComunes(model, administrador(session));
            model.addAttribute("medicamento", m);
            model.addAttribute("mensajeError", ex.getMessage());
            return "admin/medicamentos/crear";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarFormulario(@PathVariable Long id,
                                   HttpSession session, RedirectAttributes redirect, Model model) {
        String rechazo = comprobarAcceso(session, "medicine/update", redirect);
        if (rechazo != null) return rechazo;
        try {
            datosComunes(model, administrador(session));
            model.addAttribute("medicamento", servicio.obtener(id));
            return "admin/medicamentos/editar";
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/admin/medicamentos";
        }
    }

    @PostMapping("/editar/{id}")
    public String actualizar(@PathVariable Long id,
                             @RequestParam String nombre,
                             @RequestParam(required = false) BigDecimal precio,
                             @RequestParam String unidad,
                             @RequestParam(required = false) Integer stockMinimo,
                             HttpSession session, RedirectAttributes redirect, Model model) {
        String rechazo = comprobarAcceso(session, "medicine/update", redirect);
        if (rechazo != null) return rechazo;
        try {
            servicio.actualizar(id, nombre, precio, unidad, stockMinimo,
                    administrador(session).getNombreUsuario());
            redirect.addFlashAttribute("mensajeExito", "Medicamento actualizado correctamente.");
            return "redirect:/admin/medicamentos";
        } catch (IllegalArgumentException ex) {
            Medicamento m = new Medicamento();
            m.setId(id);
            m.setNombre(nombre);
            m.setPrecio(precio);
            m.setUnidad(unidad);
            m.setStockMinimo(stockMinimo);
            datosComunes(model, administrador(session));
            model.addAttribute("medicamento", m);
            model.addAttribute("mensajeError", ex.getMessage());
            return "admin/medicamentos/editar";
        }
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        String rechazo = comprobarAcceso(session, "medicine/delete", redirect);
        if (rechazo != null) return rechazo;
        try {
            servicio.desactivar(id, administrador(session).getNombreUsuario());
            redirect.addFlashAttribute("mensajeExito", "Medicamento desactivado correctamente.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/admin/medicamentos";
    }

    @PostMapping("/reactivar/{id}")
    public String reactivar(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        String rechazo = comprobarAcceso(session, "medicine/reactivate", redirect);
        if (rechazo != null) return rechazo;
        try {
            servicio.reactivar(id, administrador(session).getNombreUsuario());
            redirect.addFlashAttribute("mensajeExito", "Medicamento reactivado correctamente.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/admin/medicamentos";
    }
}
