
package com.example.sistemamedico.controller;

import com.example.sistemamedico.Repositorio.InventarioMedicamento_Repositorio;

import com.example.sistemamedico.model.InventarioMedicamento;
import com.example.sistemamedico.model.MovimientoInventario;
import com.example.sistemamedico.model.Usuario;

import com.example.sistemamedico.service.MovimientoInventarioService;
import com.example.sistemamedico.service.PermisoRolService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/inventario-medicamentos/ajustes")
public class AjusteInventarioAdminController {

    // =====================================================
    // DEPENDENCIAS
    // =====================================================

    private final InventarioMedicamento_Repositorio inventarioRepositorio;

    private final MovimientoInventarioService movimientoService;

    private final PermisoRolService permisoRolService;

    // =====================================================
    // TOKEN DE UN SOLO USO PARA AJUSTES ADMINISTRATIVOS
    // =====================================================

    private static final String TOKEN_FORMULARIO =
            "CU15_TOKEN_AJUSTE_ADMIN";

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public AjusteInventarioAdminController(
            InventarioMedicamento_Repositorio inventarioRepositorio,
            MovimientoInventarioService movimientoService,
            PermisoRolService permisoRolService
    ) {
        this.inventarioRepositorio = inventarioRepositorio;
        this.movimientoService = movimientoService;
        this.permisoRolService = permisoRolService;
    }

    // =====================================================
    // VALIDAR SESIÓN ADMINISTRATIVA
    // =====================================================

    private Usuario obtenerAdministrador(HttpSession session) {

        Object valor = session.getAttribute("usuarioAdmin");

        if (!(valor instanceof Usuario usuario)) {
            return null;
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            return null;
        }

        if (usuario.getRol() == null) {
            return null;
        }

        if (!"ADMINISTRADOR".equalsIgnoreCase(
                usuario.getRol().getNombre()
        )) {
            return null;
        }

        return usuario;
    }

    // =====================================================
    // VALIDAR PERMISOS ADMINISTRATIVOS
    // =====================================================

    private boolean autorizado(Usuario usuario) {

        return usuario != null
                && permisoRolService.tienePermiso(
                usuario,
                "inventory/read"
        )
                && permisoRolService.tienePermiso(
                usuario,
                "inventory/update"
        );
    }

    // =====================================================
    // REDIRECCIÓN POR ACCESO DENEGADO
    // =====================================================

    private String accesoDenegado(
            Usuario usuario,
            RedirectAttributes redir
    ) {

        if (usuario == null) {
            return "redirect:/admin/login";
        }

        redir.addFlashAttribute(
                "errorGeneral",
                "No tiene permisos para registrar ajustes de inventario."
        );

        return "redirect:/admin/usuarios";
    }

    // =====================================================
    // CU-15: MOSTRAR FORMULARIO DE AJUSTES
    // =====================================================

    @GetMapping
    @Transactional(readOnly = true)
    public String mostrarFormulario(
            Model model,
            HttpSession session,
            RedirectAttributes redir
    ) {

        Usuario administrador =
                obtenerAdministrador(session);

        if (!autorizado(administrador)) {
            return accesoDenegado(administrador, redir);
        }

        // =================================================
        // GENERAR TOKEN DE UN SOLO USO
        //
        // El token pertenece a esta sesión administrativa.
        // Un nuevo GET reemplaza el token anterior.
        // =================================================

        String token = UUID.randomUUID().toString();

        synchronized (session) {
            session.setAttribute(
                    TOKEN_FORMULARIO,
                    token
            );
        }

        model.addAttribute(
                "tokenFormulario",
                token
        );

        // =================================================
        // CONSULTAR INVENTARIOS ACTIVOS
        // =================================================

        List<InventarioOpcion> inventarios =
                inventarioRepositorio.findAll()
                        .stream()
                        .filter(i ->
                                Boolean.TRUE.equals(i.getActivo())
                        )
                        .filter(i ->
                                i.getMedicamento() != null
                                        && i.getSucursal() != null
                        )
                        .map(i -> new InventarioOpcion(
                                i.getId(),
                                i.getMedicamento().getNombre(),
                                i.getSucursal().getNombre(),
                                i.getStockActual(),
                                i.getMedicamento().getStockMinimo()
                        ))
                        .sorted(
                                Comparator.comparing(
                                        InventarioOpcion::medicamento,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                        )
                        .toList();

        model.addAttribute(
                "adminName",
                administrador.getNombreUsuario()
        );

        model.addAttribute(
                "inventarios",
                inventarios
        );

        return "admin/inventario-medicamentos/ajustes";
    }

    // =====================================================
    // CU-15: REGISTRAR AJUSTE ADMINISTRATIVO
    // =====================================================

    @PostMapping
    public String registrarAjuste(

            @RequestParam(required = false)
            Long inventarioId,

            @RequestParam(required = false)
            Integer tipoMovimiento,

            @RequestParam(required = false)
            Integer cantidad,

            @RequestParam(defaultValue = "")
            String numeroReferencia,

            @RequestParam(defaultValue = "")
            String motivo,

            @RequestParam(required = false)
            String tokenFormulario,

            HttpSession session,

            RedirectAttributes redir
    ) {

        // =================================================
        // 1. VALIDAR ADMINISTRADOR
        // =================================================

        Usuario administrador =
                obtenerAdministrador(session);

        if (!autorizado(administrador)) {
            return accesoDenegado(administrador, redir);
        }

        // =================================================
        // 2. VALIDAR TIPO ADMINISTRATIVO
        //
        // SOLO AJUSTE POSITIVO 4 O NEGATIVO 5.
        // =================================================

        if (tipoMovimiento == null
                || (tipoMovimiento != 4
                && tipoMovimiento != 5)) {

            redir.addFlashAttribute(
                    "mensajeError",
                    "Seleccione un ajuste positivo o negativo."
            );

            return "redirect:/admin/inventario-medicamentos/ajustes";
        }

        // =================================================
        // 3. VALIDAR Y CONSUMIR TOKEN
        //
        // synchronized evita que dos solicitudes de esta
        // misma sesión consuman el mismo token.
        // =================================================

        synchronized (session) {

            Object tokenGuardado =
                    session.getAttribute(TOKEN_FORMULARIO);

            if (tokenFormulario == null
                    || tokenFormulario.isBlank()
                    || !(tokenGuardado instanceof String)
                    || !tokenFormulario.equals(tokenGuardado)) {

                redir.addFlashAttribute(
                        "mensajeError",
                        "El formulario ya fue enviado o ha caducado. "
                                + "Abra un formulario nuevo."
                );

                return "redirect:/admin/inventario-medicamentos/ajustes";
            }

            // Se invalida antes de modificar existencias.

            session.removeAttribute(TOKEN_FORMULARIO);

            // =================================================
            // 4. REGISTRAR AJUSTE EN LA MISMA TRANSACCIÓN
            //    QUE MANEJA MovimientoInventarioService
            // =================================================

            try {

                MovimientoInventario movimiento =
                        movimientoService.registrarMovimientoManual(
                                inventarioId,
                                tipoMovimiento,
                                cantidad,
                                null,
                                numeroReferencia,
                                motivo,
                                administrador.getNombreUsuario(),
                                administrador.getRol().getNombre()
                        );

                String nombreTipo =
                        tipoMovimiento == 4
                                ? "positivo"
                                : "negativo";

                redir.addFlashAttribute(
                        "mensajeExito",
                        "Ajuste " + nombreTipo
                                + " registrado correctamente. "
                                + "Movimiento ID: "
                                + movimiento.getId()
                                + ". Stock anterior: "
                                + movimiento.getStockAnterior()
                                + ". Stock nuevo: "
                                + movimiento.getStockNuevo()
                                + "."
                );

                return "redirect:/admin/inventario-medicamentos/ajustes";

            } catch (IllegalArgumentException ex) {

                redir.addFlashAttribute(
                        "mensajeError",
                        ex.getMessage()
                );

                return "redirect:/admin/inventario-medicamentos/ajustes";
            }
        }
    }

    // =====================================================
    // DTO PARA OPCIONES DEL INVENTARIO
    // =====================================================

    public record InventarioOpcion(
            Long id,
            String medicamento,
            String sucursal,
            Integer stockActual,
            Integer stockMinimo
    ) {
    }
}
