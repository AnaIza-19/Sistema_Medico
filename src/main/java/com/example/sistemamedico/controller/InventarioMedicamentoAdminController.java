package com.example.sistemamedico.controller;

import com.example.sistemamedico.model.Usuario;
import com.example.sistemamedico.service.InventarioMedicamentoAdminService;
import com.example.sistemamedico.service.PermisoRolService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/inventario-medicamentos")
public class InventarioMedicamentoAdminController {
    private final InventarioMedicamentoAdminService servicio;
    private final PermisoRolService permisoRolService;

    public InventarioMedicamentoAdminController(InventarioMedicamentoAdminService servicio,
                                                PermisoRolService permisoRolService) {
        this.servicio = servicio;
        this.permisoRolService = permisoRolService;
    }

    private Usuario administrador(HttpSession session) {
        Object valor = session.getAttribute("usuarioAdmin");
        if (!(valor instanceof Usuario)) return null;
        Usuario usuario = (Usuario) valor;
        if (!Boolean.TRUE.equals(usuario.getActivo()) || usuario.getRol() == null
                || !"ADMINISTRADOR".equalsIgnoreCase(usuario.getRol().getNombre())) return null;
        return usuario;
    }

    private boolean autorizado(HttpSession session, String codigo) {
        Usuario usuario = administrador(session);
        return usuario != null && permisoRolService.tienePermiso(usuario, codigo);
    }

    private String denegado(HttpSession session, RedirectAttributes redir) {
        if (administrador(session) == null) return "redirect:/admin/login";
        redir.addFlashAttribute("errorGeneral", "No tiene permisos para realizar esta operación.");
        return "redirect:/admin/usuarios";
    }

    private void comunes(Model model, HttpSession session) {
        model.addAttribute("adminName", administrador(session).getNombreUsuario());
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "") String buscar,
                         @RequestParam(defaultValue = "0") int pagina,
                         @RequestParam(defaultValue = "10") int tamanio,
                         Model model, HttpSession session, RedirectAttributes redir) {
        if (!autorizado(session, "inventory/read")) return denegado(session, redir);
        comunes(model, session);
        var inventarios = servicio.listar(buscar, pagina, tamanio);
        // Reencauzar las páginas que dejan de existir al cambiar un filtro.
        if (inventarios.getTotalPages() > 0 && pagina >= inventarios.getTotalPages())
            inventarios = servicio.listar(buscar, inventarios.getTotalPages() - 1, tamanio);
        model.addAttribute("inventarios", inventarios);
        model.addAttribute("buscar", buscar);
        model.addAttribute("tamanio", tamanio);
        return "admin/inventario-medicamentos/listar";
    }

    @GetMapping("/crear")
    public String crearFormulario(Model model, HttpSession session, RedirectAttributes redir) {
        if (!autorizado(session, "inventory/create")) return denegado(session, redir);
        comunes(model, session);
        model.addAttribute("medicamentos", servicio.medicamentosActivos());
        model.addAttribute("sucursales", servicio.sucursalesActivas());
        return "admin/inventario-medicamentos/crear";
    }

    @PostMapping("/crear")
    public String crear(@RequestParam(required = false) Long medicamentoId,
                        @RequestParam(required = false) Long sucursalId,
                        @RequestParam(required = false) Integer stockActual,
                        @RequestParam(defaultValue = "") String detalle,
                        HttpSession session, RedirectAttributes redir) {
        if (!autorizado(session, "inventory/create")) return denegado(session, redir);
        try {
            servicio.crear(medicamentoId, sucursalId, stockActual, detalle,
                    administrador(session).getNombreUsuario());
            redir.addFlashAttribute("mensajeExito", "Inventario creado correctamente.");
        } catch (IllegalArgumentException ex) {
            redir.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/admin/inventario-medicamentos/crear";
        }
        return "redirect:/admin/inventario-medicamentos";
    }

    @GetMapping("/editar/{id}")
    public String editarFormulario(@PathVariable Long id, Model model,
                                   HttpSession session, RedirectAttributes redir) {
        if (!autorizado(session, "inventory/update")) return denegado(session, redir);
        try {
            comunes(model, session);
            model.addAttribute("inventario", servicio.obtener(id));
            return "admin/inventario-medicamentos/editar";
        } catch (IllegalArgumentException ex) {
            redir.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/admin/inventario-medicamentos";
        }
    }

    @PostMapping("/editar/{id}")
    public String ajustarStock(@PathVariable Long id,
                               @RequestParam(required = false) Integer stockNuevo,
                               @RequestParam(defaultValue = "") String detalle,
                               HttpSession session, RedirectAttributes redir) {
        if (!autorizado(session, "inventory/update")) return denegado(session, redir);
        try {
            servicio.actualizarStock(id, stockNuevo, detalle, administrador(session).getNombreUsuario());
            redir.addFlashAttribute("mensajeExito", "Stock actualizado y auditado correctamente.");
            return "redirect:/admin/inventario-medicamentos";
        } catch (IllegalArgumentException ex) {
            redir.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/admin/inventario-medicamentos/editar/" + id;
        }
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id,
                             @RequestParam(defaultValue = "") String detalle,
                             HttpSession session, RedirectAttributes redir) {
        if (!autorizado(session, "inventory/delete")) return denegado(session, redir);
        try {
            servicio.desactivar(id, detalle, administrador(session).getNombreUsuario());
            redir.addFlashAttribute("mensajeExito", "Inventario desactivado correctamente.");
        } catch (IllegalArgumentException ex) {
            redir.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/admin/inventario-medicamentos";
    }

    @PostMapping("/reactivar/{id}")
    public String reactivar(@PathVariable Long id,
                            @RequestParam(defaultValue = "") String detalle,
                            HttpSession session, RedirectAttributes redir) {
        if (!autorizado(session, "inventory/reactivate")) return denegado(session, redir);
        try {
            servicio.reactivar(id, detalle, administrador(session).getNombreUsuario());
            redir.addFlashAttribute("mensajeExito", "Inventario reactivado correctamente.");
        } catch (IllegalArgumentException ex) {
            redir.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/admin/inventario-medicamentos";
    }
}
