
package com.example.sistemamedico.controller;

import com.example.sistemamedico.Repositorio.InventarioMedicamento_Repositorio;
import com.example.sistemamedico.Repositorio.MovimientoInventario_Repositorio;

import com.example.sistemamedico.model.InventarioMedicamento;
import com.example.sistemamedico.model.MovimientoInventario;
import com.example.sistemamedico.model.Usuario;

import com.example.sistemamedico.service.MovimientoInventarioService;

import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;


@Controller
@RequestMapping("/farmacia/bitacora")
public class MovimientoInventarioController {

    // =====================================================
    // REPOSITORIOS Y SERVICIO
    // =====================================================

    private final MovimientoInventario_Repositorio
            movimientoRepositorio;

    private final InventarioMedicamento_Repositorio
            inventarioRepositorio;

    private final MovimientoInventarioService
            movimientoService;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public MovimientoInventarioController(

            MovimientoInventario_Repositorio movimientoRepositorio,

            InventarioMedicamento_Repositorio inventarioRepositorio,

            MovimientoInventarioService movimientoService
    ) {

        this.movimientoRepositorio = movimientoRepositorio;
        this.inventarioRepositorio = inventarioRepositorio;
        this.movimientoService = movimientoService;
    }


    // =====================================================
    // VALIDAR SESIÓN DEL PERSONAL DE FARMACIA
    // =====================================================

    private Usuario obtenerUsuarioFarmacia(HttpSession session) {

        Object sesion =
                session.getAttribute("usuarioInterno");

        if (!(sesion instanceof Usuario usuario)) {
            return null;
        }

        if (usuario.getRol() == null) {
            return null;
        }

        String rol = usuario.getRol().getNombre();

        if (!"FARMACIA".equalsIgnoreCase(rol)) {
            return null;
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            return null;
        }

        return usuario;
    }


    // =====================================================
    // CU-15: LISTADO CON FILTROS DINÁMICOS
    // =====================================================

    @GetMapping
    @Transactional(readOnly = true)
    public String listarMovimientos(

            @RequestParam(defaultValue = "0")
            int pagina,

            @RequestParam(required = false)
            Integer tipo,

            @RequestParam(required = false)
            Long medicamentoId,

            @RequestParam(required = false)
            Long sucursalId,

            @RequestParam(required = false)
            String referencia,

            @RequestParam(required = false)
            String usuario,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaHasta,

            Model model,

            HttpSession session
    ) {

        // =================================================
        // 1. VALIDAR SESIÓN
        // =================================================

        Usuario usuarioSesion =
                obtenerUsuarioFarmacia(session);

        if (usuarioSesion == null) {
            return "redirect:/personal/login";
        }


        // =================================================
        // 2. VALIDAR TIPO DE MOVIMIENTO
        // =================================================

        if (tipo != null && (tipo < 0 || tipo > 6)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de movimiento no válido."
            );
        }


        // =================================================
        // 3. VALIDAR RANGO DE FECHAS
        // =================================================

        if (fechaDesde != null
                && fechaHasta != null
                && fechaDesde.isAfter(fechaHasta)) {

            model.addAttribute(
                    "mensajeError",
                    "La fecha inicial no puede ser posterior a la final."
            );

            // EVITA EJECUTAR UN RANGO INVERTIDO

            fechaDesde = null;
            fechaHasta = null;
        }


        // =================================================
        // 4. LIMPIAR FILTROS DE TEXTO
        // =================================================

        String referenciaFiltro =
                referencia == null || referencia.isBlank()
                        ? null
                        : referencia.trim();

        String usuarioFiltro =
                usuario == null || usuario.isBlank()
                        ? null
                        : usuario.trim();


        // =================================================
        // 5. CONVERTIR RANGO DE FECHAS
        //
        // FECHA DESDE: 00:00 DEL DÍA INICIAL
        // FECHA HASTA: ANTES DE 00:00 DEL DÍA SIGUIENTE
        // =================================================

        LocalDateTime desde =
                fechaDesde == null
                        ? null
                        : fechaDesde.atStartOfDay();

        LocalDateTime hastaExclusiva =
                fechaHasta == null
                        ? null
                        : fechaHasta.plusDays(1).atStartOfDay();


        // =================================================
        // 6. CONSTRUIR FILTROS DINÁMICAMENTE
        //
        // SOLO SE AGREGAN LOS FILTROS QUE TENGAN VALOR.
        // ESTO EVITA EL ERROR lower(bytea).
        // =================================================

        Specification<MovimientoInventario> filtros =
                (root, query, cb) -> {

                    List<Predicate> condiciones =
                            new ArrayList<>();


                    // =====================================
                    // TIPO DE MOVIMIENTO
                    // =====================================

                    if (tipo != null) {

                        condiciones.add(
                                cb.equal(
                                        root.get("tipoMovimiento"),
                                        tipo.shortValue()
                                )
                        );
                    }


                    // =====================================
                    // MEDICAMENTO
                    // =====================================

                    if (medicamentoId != null) {

                        condiciones.add(
                                cb.equal(
                                        root.get("medicamentoId"),
                                        medicamentoId
                                )
                        );
                    }


                    // =====================================
                    // SUCURSAL
                    // =====================================

                    if (sucursalId != null) {

                        condiciones.add(
                                cb.equal(
                                        root.get("sucursalId"),
                                        sucursalId
                                )
                        );
                    }


                    // =====================================
                    // NÚMERO DE REFERENCIA
                    // =====================================

                    if (referenciaFiltro != null) {

                        condiciones.add(
                                cb.like(
                                        cb.lower(
                                                root.get("numeroReferencia")
                                        ),
                                        "%" + referenciaFiltro
                                                .toLowerCase(Locale.ROOT) + "%"
                                )
                        );
                    }


                    // =====================================
                    // USUARIO RESPONSABLE
                    // =====================================

                    if (usuarioFiltro != null) {

                        condiciones.add(
                                cb.like(
                                        cb.lower(
                                                root.get("registradoPor")
                                        ),
                                        "%" + usuarioFiltro
                                                .toLowerCase(Locale.ROOT) + "%"
                                )
                        );
                    }


                    // =====================================
                    // FECHA INICIAL
                    // =====================================

                    if (desde != null) {

                        condiciones.add(
                                cb.greaterThanOrEqualTo(
                                        root.get("fechaHora"),
                                        desde
                                )
                        );
                    }


                    // =====================================
                    // FECHA FINAL
                    // =====================================

                    if (hastaExclusiva != null) {

                        condiciones.add(
                                cb.lessThan(
                                        root.get("fechaHora"),
                                        hastaExclusiva
                                )
                        );
                    }


                    // =====================================
                    // COMBINAR FILTROS
                    // =====================================

                    return cb.and(
                            condiciones.toArray(new Predicate[0])
                    );
                };


        // =================================================
        // 7. PAGINACIÓN Y ORDENAMIENTO
        // =================================================

        int paginaSegura = Math.max(0, pagina);

        Page<MovimientoInventario> movimientos =
                movimientoRepositorio.findAll(

                        filtros,

                        PageRequest.of(
                                paginaSegura,
                                10,
                                Sort.by(
                                        Sort.Order.desc("fechaHora"),
                                        Sort.Order.desc("id")
                                )
                        )
                );


        // =================================================
        // 8. OBTENER NOMBRES DE MEDICAMENTOS Y SUCURSALES
        // =================================================

        List<InventarioMedicamento> inventarios =
                inventarioRepositorio.findAll();

        Map<Long, String> nombresMedicamentos =
                new HashMap<>();

        Map<Long, String> nombresSucursales =
                new HashMap<>();

        Map<Long, String> opcionesMedicamentos =
                new TreeMap<>();

        Map<Long, String> opcionesSucursales =
                new TreeMap<>();


        for (InventarioMedicamento inventario : inventarios) {

            if (inventario.getMedicamento() == null
                    || inventario.getSucursal() == null) {
                continue;
            }

            Long idMedicamento =
                    inventario.getMedicamento().getId();

            Long idSucursal =
                    inventario.getSucursal().getId();

            String nombreMedicamento =
                    inventario.getMedicamento().getNombre();

            String nombreSucursal =
                    inventario.getSucursal().getNombre();


            nombresMedicamentos.put(
                    idMedicamento,
                    nombreMedicamento
            );

            nombresSucursales.put(
                    idSucursal,
                    nombreSucursal
            );

            opcionesMedicamentos.put(
                    idMedicamento,
                    nombreMedicamento
            );

            opcionesSucursales.put(
                    idSucursal,
                    nombreSucursal
            );
        }


        // =================================================
        // 9. ENVIAR DATOS A THYMELEAF
        // =================================================

        model.addAttribute(
                "movimientos",
                movimientos
        );

        model.addAttribute(
                "usuarioActual",
                usuarioSesion.getNombreUsuario()
        );

        model.addAttribute(
                "nombresMedicamentos",
                nombresMedicamentos
        );

        model.addAttribute(
                "nombresSucursales",
                nombresSucursales
        );

        model.addAttribute(
                "opcionesMedicamentos",
                opcionesMedicamentos
        );

        model.addAttribute(
                "opcionesSucursales",
                opcionesSucursales
        );


        // =================================================
        // 10. CONSERVAR FILTROS EN LA PANTALLA
        // =================================================

        model.addAttribute("tipo", tipo);

        model.addAttribute(
                "medicamentoId",
                medicamentoId
        );

        model.addAttribute(
                "sucursalId",
                sucursalId
        );

        model.addAttribute(
                "referencia",
                referenciaFiltro
        );

        model.addAttribute(
                "usuarioFiltro",
                usuarioFiltro
        );

        model.addAttribute(
                "fechaDesde",
                fechaDesde
        );

        model.addAttribute(
                "fechaHasta",
                fechaHasta
        );


        return "farmacia/bitacora/listar";
    }


    // =====================================================
    // CU-15: MOSTRAR FORMULARIO NUEVO MOVIMIENTO
    // =====================================================

    @GetMapping("/nuevo")
    @Transactional(readOnly = true)
    public String mostrarFormulario(

            Model model,

            HttpSession session
    ) {

        // =================================================
        // VALIDAR SESIÓN
        // =================================================

        Usuario usuario =
                obtenerUsuarioFarmacia(session);

        if (usuario == null) {
            return "redirect:/personal/login";
        }


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


        // =================================================
        // TIPOS MANUALES PARA FARMACIA
        //
        // 4 Y 5 REQUIEREN ADMINISTRADOR.
        // 6 ES AUTOMÁTICO.
        // =================================================

        List<TipoOpcion> tiposMovimiento = List.of(

                new TipoOpcion(0, "Compra"),

                new TipoOpcion(1, "Devolución"),

                new TipoOpcion(2, "Venta"),

                new TipoOpcion(3, "Reclamo")

        );


        model.addAttribute(
                "inventarios",
                inventarios
        );

        model.addAttribute(
                "tiposMovimiento",
                tiposMovimiento
        );

        model.addAttribute(
                "usuarioActual",
                usuario.getNombreUsuario()
        );


        return "farmacia/bitacora/crear";
    }


    // =====================================================
    // CU-15: CONSULTAR EXISTENCIAS
    //
    // NO MODIFICA EL INVENTARIO.
    // =====================================================

    @GetMapping("/stock")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<?> consultarStock(

            @RequestParam Long inventarioId,

            HttpSession session
    ) {

        Usuario usuario =
                obtenerUsuarioFarmacia(session);

        if (usuario == null) {

            return ResponseEntity.status(
                    HttpStatus.FORBIDDEN
            ).body(
                    Map.of(
                            "error",
                            "Acceso no autorizado."
                    )
            );
        }


        InventarioMedicamento inventario =
                inventarioRepositorio
                        .findById(inventarioId)
                        .orElse(null);


        if (inventario == null
                || !Boolean.TRUE.equals(inventario.getActivo())) {

            return ResponseEntity.status(
                    HttpStatus.NOT_FOUND
            ).body(
                    Map.of(
                            "error",
                            "Inventario no encontrado o inactivo."
                    )
            );
        }


        // =================================================
        // RESPUESTA JSON PARA EL FORMULARIO
        // =================================================

        return ResponseEntity.ok(
                new StockRespuesta(

                        inventario.getId(),

                        inventario.getMedicamento().getNombre(),

                        inventario.getSucursal().getNombre(),

                        inventario.getStockActual(),

                        inventario.getMedicamento().getStockMinimo()

                )
        );
    }


    // =====================================================
    // CU-15: REGISTRAR MOVIMIENTO MANUAL
    // =====================================================

    @PostMapping("/nuevo")
    public String registrarMovimiento(

            @RequestParam(required = false)
            Long inventarioId,

            @RequestParam(required = false)
            Integer tipoMovimiento,

            @RequestParam(required = false)
            Integer cantidad,

            @RequestParam(required = false)
            BigDecimal costoUnitario,

            @RequestParam(required = false)
            String numeroReferencia,

            @RequestParam(required = false)
            String motivo,

            HttpSession session,

            RedirectAttributes redirectAttributes
    ) {

        // =================================================
        // VALIDAR IDENTIDAD DEL USUARIO
        // =================================================

        Usuario usuario =
                obtenerUsuarioFarmacia(session);

        if (usuario == null) {
            return "redirect:/personal/login";
        }


        // =================================================
        // REGISTRAR MOVIMIENTO MEDIANTE EL SERVICIO
        // =================================================

        try {

            MovimientoInventario movimiento =
                    movimientoService.registrarMovimientoManual(

                            inventarioId,

                            tipoMovimiento,

                            cantidad,

                            costoUnitario,

                            numeroReferencia,

                            motivo,

                            usuario.getNombreUsuario(),

                            usuario.getRol().getNombre()

                    );


            // =================================================
            // MENSAJE DE REGISTRO EXITOSO
            // =================================================

            String nombreTipo =
                    obtenerNombreTipo(tipoMovimiento);

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",

                    "Movimiento registrado exitosamente. " +
                            "Tipo: " + nombreTipo +
                            ". Cantidad: " + cantidad +
                            ". Stock actualizado: " +
                            movimiento.getStockNuevo()
            );


            return "redirect:/farmacia/bitacora";

        } catch (IllegalArgumentException ex) {

            // =================================================
            // MOSTRAR ERROR DE VALIDACIÓN
            // =================================================

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    ex.getMessage()
            );

            return "redirect:/farmacia/bitacora/nuevo";
        }
    }


    // =====================================================
    // CU-15: VER DETALLE DE UN MOVIMIENTO
    // =====================================================

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String verDetalle(

            @PathVariable Long id,

            Model model,

            HttpSession session
    ) {

        // =================================================
        // VALIDAR SESIÓN
        // =================================================

        Usuario usuario =
                obtenerUsuarioFarmacia(session);

        if (usuario == null) {
            return "redirect:/personal/login";
        }


        // =================================================
        // BUSCAR MOVIMIENTO
        // =================================================

        MovimientoInventario movimiento =
                movimientoRepositorio
                        .findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Movimiento no encontrado."
                                )
                        );


        model.addAttribute(
                "movimiento",
                movimiento
        );

        model.addAttribute(
                "usuarioActual",
                usuario.getNombreUsuario()
        );


        return "farmacia/bitacora/detalle";
    }


    // =====================================================
    // NOMBRE DEL TIPO DE MOVIMIENTO
    // =====================================================

    private String obtenerNombreTipo(Integer tipo) {

        if (tipo == null) {
            return "Desconocido";
        }

        return switch (tipo) {

            case 0 -> "Compra";

            case 1 -> "Devolución";

            case 2 -> "Venta";

            case 3 -> "Reclamo";

            case 4 -> "Ajuste+";

            case 5 -> "Ajuste-";

            case 6 -> "Despacho";

            default -> "Desconocido";
        };
    }


    // =====================================================
    // OPCIONES DEL INVENTARIO PARA THYMELEAF
    // =====================================================

    public record InventarioOpcion(

            Long id,

            String medicamento,

            String sucursal,

            Integer stockActual,

            Integer stockMinimo

    ) {
    }


    // =====================================================
    // OPCIONES DE LOS TIPOS DE MOVIMIENTO
    // =====================================================

    public record TipoOpcion(

            Integer codigo,

            String nombre

    ) {
    }


    // =====================================================
    // RESPUESTA JSON DE EXISTENCIAS
    // =====================================================

    public record StockRespuesta(

            Long inventarioId,

            String medicamento,

            String sucursal,

            Integer stockActual,

            Integer stockMinimo

    ) {
    }

}
