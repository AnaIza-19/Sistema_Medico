package com.example.sistemamedico.controller;

import com.example.sistemamedico.Repositorio.ConsultaMedica_Repositorio;
import com.example.sistemamedico.Repositorio.InventarioMedicamento_Repositorio;
import com.example.sistemamedico.Repositorio.RecetaMedicaDetalle_Repositorio;
import com.example.sistemamedico.Repositorio.RecetaMedica_Repositorio;

import com.example.sistemamedico.dto.MedicamentoDespachoVista;
import com.example.sistemamedico.dto.RecetaDespachoResultado;

import com.example.sistemamedico.model.ConsultaMedica;
import com.example.sistemamedico.model.InventarioMedicamento;
import com.example.sistemamedico.model.RecetaMedica;
import com.example.sistemamedico.model.RecetaMedicaDetalle;
import com.example.sistemamedico.model.Usuario;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.example.sistemamedico.Repositorio.DespachoMedicamentoDetalle_Repositorio;
import com.example.sistemamedico.Repositorio.DespachoMedicamento_Repositorio;

import com.example.sistemamedico.model.DespachoMedicamento;
import com.example.sistemamedico.model.DespachoMedicamentoDetalle;

import com.example.sistemamedico.service.FarmaciaDespachoService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import com.example.sistemamedico.model.Medicamento;
@Controller
@RequestMapping("/farmacia")
public class FarmaciaController {


    // =====================================================
    // REPOSITORIOS
    // =====================================================

    private final RecetaMedica_Repositorio recetaRepositorio;

    private final ConsultaMedica_Repositorio consultaRepositorio;

    private final RecetaMedicaDetalle_Repositorio recetaDetalleRepositorio;

    private final InventarioMedicamento_Repositorio inventarioRepositorio;

    private final DespachoMedicamento_Repositorio despachoRepositorio;

    private final DespachoMedicamentoDetalle_Repositorio despachoDetalleRepositorio;

    private final FarmaciaDespachoService farmaciaDespachoService;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public FarmaciaController(
            RecetaMedica_Repositorio recetaRepositorio,
            ConsultaMedica_Repositorio consultaRepositorio,
            RecetaMedicaDetalle_Repositorio recetaDetalleRepositorio,
            InventarioMedicamento_Repositorio inventarioRepositorio,
            DespachoMedicamento_Repositorio despachoRepositorio,
            DespachoMedicamentoDetalle_Repositorio despachoDetalleRepositorio,
            FarmaciaDespachoService farmaciaDespachoService
    ) {

        this.recetaRepositorio =
                recetaRepositorio;

        this.consultaRepositorio =
                consultaRepositorio;

        this.recetaDetalleRepositorio =
                recetaDetalleRepositorio;

        this.inventarioRepositorio =
                inventarioRepositorio;

        this.despachoRepositorio =
                despachoRepositorio;

        this.despachoDetalleRepositorio =
                despachoDetalleRepositorio;

        this.farmaciaDespachoService =
                farmaciaDespachoService;
    }

    // =====================================================
    // PANEL PRINCIPAL DE FARMACIA
    // =====================================================

    @GetMapping
    public String mostrarPanel(
            HttpSession session,
            Model model
    ) {

        Usuario personalFarmacia =
                obtenerPersonalFarmacia(
                        session
                );


        if (personalFarmacia == null) {

            return "redirect:/personal/login";
        }


        model.addAttribute(
                "personalFarmacia",
                personalFarmacia
        );


        return "farmacia/index";
    }


    // =====================================================
    // CU-11
    // BUSCAR RECETAS
    // =====================================================

    @GetMapping("/despacho")
    public String mostrarNuevoDespacho(
            @RequestParam(required = false)
            Long recetaId,

            @RequestParam(required = false)
            Long consultaId,

            HttpSession session,
            Model model
    ) {

        Usuario personalFarmacia =
                obtenerPersonalFarmacia(
                        session
                );


        if (personalFarmacia == null) {

            return "redirect:/personal/login";
        }


        model.addAttribute(
                "personalFarmacia",
                personalFarmacia
        );


        model.addAttribute(
                "recetaId",
                recetaId
        );


        model.addAttribute(
                "consultaId",
                consultaId
        );


        // =================================================
        // TODAVÍA NO SE REALIZÓ UNA BÚSQUEDA
        // =================================================

        if (
                recetaId == null
                        &&
                        consultaId == null
        ) {

            return "farmacia/despacho";
        }


        // =================================================
        // NO UTILIZAR LOS DOS FILTROS AL MISMO TIEMPO
        // =================================================

        if (
                recetaId != null
                        &&
                        consultaId != null
        ) {

            model.addAttribute(
                    "error",
                    "Utilice únicamente un filtro: ID de Receta o ID de Consulta."
            );


            return "farmacia/despacho";
        }


        List<RecetaMedica> recetas =
                new ArrayList<>();


        // =================================================
        // BUSCAR POR ID DE RECETA
        // =================================================

        if (recetaId != null) {

            recetaRepositorio
                    .findByIdAndActivoTrue(
                            recetaId
                    )
                    .ifPresent(
                            recetas::add
                    );
        }


        // =================================================
        // BUSCAR POR ID DE CONSULTA
        // =================================================

        if (consultaId != null) {

            Optional<ConsultaMedica> consulta =
                    consultaRepositorio
                            .findById(
                                    consultaId
                            );


            if (consulta.isPresent()) {

                Long citaId =
                        consulta.get()
                                .getCita()
                                .getId();


                recetas =
                        recetaRepositorio
                                .findByCitaIdAndActivoTrueOrderByFechaCreacionDesc(
                                        citaId
                                );
            }
        }


        // =================================================
        // NO SE ENCONTRARON RECETAS
        // =================================================

        if (recetas.isEmpty()) {

            model.addAttribute(
                    "mensaje",
                    "No se encontraron recetas activas con el criterio indicado."
            );


            model.addAttribute(
                    "resultados",
                    List.of()
            );


            return "farmacia/despacho";
        }


        // =================================================
        // PREPARAR RESULTADOS
        // =================================================

        List<RecetaDespachoResultado> resultados =
                new ArrayList<>();


        for (RecetaMedica receta : recetas) {

            Long citaId =
                    receta.getCita()
                            .getId();


            Long idConsulta =
                    consultaRepositorio
                            .findByCitaId(
                                    citaId
                            )
                            .map(
                                    ConsultaMedica::getId
                            )
                            .orElse(
                                    null
                            );


            long diasTranscurridos =
                    calcularDiasReceta(
                            receta
                    );


            boolean vigente =
                    diasTranscurridos <= 7;


            resultados.add(
                    new RecetaDespachoResultado(
                            receta,
                            idConsulta,
                            diasTranscurridos,
                            vigente
                    )
            );
        }


        model.addAttribute(
                "resultados",
                resultados
        );


        return "farmacia/despacho";
    }


    // =====================================================
    // CU-11
    // DETALLE DE LA RECETA PARA DESPACHO
    // =====================================================

    @GetMapping("/despacho/{recetaId}")
    public String mostrarDetalleDespacho(
            @PathVariable Long recetaId,
            HttpSession session,
            Model model
    ) {

        Usuario personalFarmacia =
                obtenerPersonalFarmacia(
                        session
                );


        if (personalFarmacia == null) {

            return "redirect:/personal/login";
        }


        model.addAttribute(
                "personalFarmacia",
                personalFarmacia
        );


        // =================================================
        // BUSCAR RECETA ACTIVA
        // =================================================

        Optional<RecetaMedica> recetaOptional =
                recetaRepositorio
                        .findByIdAndActivoTrue(
                                recetaId
                        );


        if (recetaOptional.isEmpty()) {

            model.addAttribute(
                    "error",
                    "No se encontró una receta activa con ID "
                            + recetaId
                            + "."
            );


            return "farmacia/despacho";
        }


        RecetaMedica receta =
                recetaOptional.get();


        // =================================================
        // VALIDAR VIGENCIA EN BACKEND
        // =================================================

        long diasTranscurridos =
                calcularDiasReceta(
                        receta
                );


        if (diasTranscurridos > 7) {

            model.addAttribute(
                    "error",
                    "Receta Vencida. La receta #"
                            + receta.getId()
                            + " fue emitida hace "
                            + diasTranscurridos
                            + " días y ya no es válida para despacho."
            );


            return "farmacia/despacho";
        }


        // =================================================
        // VALIDAR SUCURSAL DEL PERSONAL
        // =================================================

        if (personalFarmacia.getSucursal() == null) {

            model.addAttribute(
                    "error",
                    "El usuario de farmacia no tiene una sucursal asignada."
            );


            return "farmacia/despacho";
        }


        Long sucursalId =
                personalFarmacia
                        .getSucursal()
                        .getId();


        // =================================================
        // OBTENER CONSULTA ASOCIADA
        // =================================================

        Long consultaId =
                consultaRepositorio
                        .findByCitaId(
                                receta.getCita()
                                        .getId()
                        )
                        .map(
                                ConsultaMedica::getId
                        )
                        .orElse(
                                null
                        );


        // =================================================
        // OBTENER MEDICAMENTOS DE LA RECETA
        // =================================================

        List<RecetaMedicaDetalle> detalles =
                recetaDetalleRepositorio
                        .findByRecetaIdOrderByIdAsc(
                                recetaId
                        );


        if (detalles.isEmpty()) {

            model.addAttribute(
                    "error",
                    "La receta no contiene medicamentos registrados."
            );


            return "farmacia/despacho";
        }


        // =================================================
        // PREPARAR MEDICAMENTOS E INVENTARIO
        // =================================================

        List<MedicamentoDespachoVista> medicamentosVista =
                new ArrayList<>();


        for (RecetaMedicaDetalle detalle : detalles) {


            Long medicamentoId =
                    detalle
                            .getMedicamento()
                            .getId();


            Optional<InventarioMedicamento> inventarioOptional =
                    inventarioRepositorio
                            .findByMedicamentoIdAndSucursalIdAndActivoTrue(
                                    medicamentoId,
                                    sucursalId
                            );


            // =============================================
            // SIN INVENTARIO REGISTRADO
            // =============================================

            if (inventarioOptional.isEmpty()) {

                medicamentosVista.add(
                        new MedicamentoDespachoVista(
                                detalle,
                                null,
                                detalle.getMedicamento()
                                        .getStockMinimo(),
                                false,
                                false,
                                "Sin inventario registrado"
                        )
                );


                continue;
            }


            // =============================================
            // INVENTARIO ENCONTRADO
            // =============================================

            InventarioMedicamento inventario =
                    inventarioOptional.get();


            Integer stockActual =
                    inventario.getStockActual();


            Integer stockMinimo =
                    detalle.getMedicamento()
                            .getStockMinimo();


            boolean stockBajo =
                    stockActual != null
                            &&
                            stockMinimo != null
                            &&
                            stockActual <= stockMinimo;


            String mensajeInventario;


            // =============================================
            // STOCK BAJO
            // =============================================

            if (stockBajo) {

                mensajeInventario =
                        detalle.getMedicamento()
                                .getNombre()
                                +
                                ": Stock bajo — disponible: "
                                +
                                stockActual
                                +
                                " (mínimo: "
                                +
                                stockMinimo
                                +
                                ").";

            } else {

                mensajeInventario =
                        "Disponible: "
                                +
                                stockActual;
            }


            // =============================================
            // AGREGAR A LA VISTA
            // =============================================

            medicamentosVista.add(
                    new MedicamentoDespachoVista(
                            detalle,
                            stockActual,
                            stockMinimo,
                            true,
                            stockBajo,
                            mensajeInventario
                    )
            );
        }


        // =================================================
        // MEDICAMENTOS DISPONIBLES PARA SUSTITUCIÓN
        // =================================================

        List<InventarioMedicamento> alternativas =
                inventarioRepositorio
                        .findBySucursalIdAndActivoTrueOrderByMedicamentoNombreAsc(
                                sucursalId
                        )
                        .stream()
                        .filter(
                                inventario ->
                                        inventario.getStockActual() != null
                                                &&
                                                inventario.getStockActual() > 0
                        )
                        .toList();


        // =================================================
        // ENVIAR DATOS A LA VISTA
        // =================================================

        model.addAttribute(
                "alternativas",
                alternativas
        );


        model.addAttribute(
                "receta",
                receta
        );


        model.addAttribute(
                "consultaId",
                consultaId
        );


        model.addAttribute(
                "diasTranscurridos",
                diasTranscurridos
        );


        model.addAttribute(
                "medicamentos",
                medicamentosVista
        );


        return "farmacia/detalle-despacho";
    }


    // =====================================================
    // CALCULAR DÍAS TRANSCURRIDOS DE LA RECETA
    // =====================================================
// =====================================================
// CU-11
// CONFIRMAR DESPACHO
// =====================================================

    @PostMapping("/despacho/{recetaId}/confirmar")
    public String confirmarDespacho(
            @PathVariable Long recetaId,

            @RequestParam Map<String, String> parametros,

            HttpSession session,

            RedirectAttributes redirectAttributes
    ) {

        Usuario personalFarmacia =
                obtenerPersonalFarmacia(
                        session
                );


        if (personalFarmacia == null) {

            return "redirect:/personal/login";
        }


        Map<Long, Integer> cantidades =
                new HashMap<>();


        Map<Long, Long> medicamentosAlternativos =
                new HashMap<>();


        Map<Long, String> razonesSustitucion =
                new HashMap<>();


        try {


            // =============================================
            // LEER CAMPOS DEL FORMULARIO
            // =============================================

            for (
                    Map.Entry<String, String> parametro :
                    parametros.entrySet()
            ) {


                String nombre =
                        parametro.getKey();


                String valor =
                        parametro.getValue();


                // =========================================
                // CANTIDAD
                // =========================================

                if (
                        nombre.startsWith(
                                "cantidad_"
                        )
                                &&
                                valor != null
                                &&
                                !valor.isBlank()
                ) {

                    Long detalleId =
                            Long.valueOf(
                                    nombre.substring(
                                            "cantidad_".length()
                                    )
                            );


                    Integer cantidad =
                            Integer.valueOf(
                                    valor
                            );


                    cantidades.put(
                            detalleId,
                            cantidad
                    );
                }


                // =========================================
                // MEDICAMENTO ALTERNATIVO
                // =========================================

                if (
                        nombre.startsWith(
                                "alternativo_"
                        )
                                &&
                                valor != null
                                &&
                                !valor.isBlank()
                ) {

                    Long detalleId =
                            Long.valueOf(
                                    nombre.substring(
                                            "alternativo_".length()
                                    )
                            );


                    Long medicamentoId =
                            Long.valueOf(
                                    valor
                            );


                    medicamentosAlternativos.put(
                            detalleId,
                            medicamentoId
                    );
                }


                // =========================================
                // RAZÓN DE SUSTITUCIÓN
                // =========================================

                if (
                        nombre.startsWith(
                                "razon_"
                        )
                                &&
                                valor != null
                                &&
                                !valor.isBlank()
                ) {

                    Long detalleId =
                            Long.valueOf(
                                    nombre.substring(
                                            "razon_".length()
                                    )
                            );


                    razonesSustitucion.put(
                            detalleId,
                            valor.trim()
                    );
                }
            }


            // =============================================
            // GUARDAR DESPACHO
            // =============================================

            DespachoMedicamento despacho =
                    farmaciaDespachoService
                            .confirmarDespacho(
                                    recetaId,
                                    personalFarmacia,
                                    cantidades,
                                    medicamentosAlternativos,
                                    razonesSustitucion
                            );


            return "redirect:/farmacia/despacho/resultado/"
                    + despacho.getId();


        } catch (Exception e) {


            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );


            return "redirect:/farmacia/despacho/"
                    + recetaId;
        }
    }


// =====================================================
// CU-11
// RESULTADO DEL DESPACHO
// =====================================================

    @GetMapping("/despacho/resultado/{despachoId}")
    public String mostrarResultadoDespacho(
            @PathVariable Long despachoId,
            HttpSession session,
            Model model
    ) {

        Usuario personalFarmacia =
                obtenerPersonalFarmacia(
                        session
                );


        if (personalFarmacia == null) {

            return "redirect:/personal/login";
        }


        DespachoMedicamento despacho =
                despachoRepositorio
                        .findById(
                                despachoId
                        )
                        .orElse(null);


        if (despacho == null) {

            return "redirect:/farmacia";
        }


        // Seguridad adicional:
        // solo personal de la misma sucursal puede verlo

        if (
                personalFarmacia.getSucursal() == null
                        ||
                        !despacho.getSucursal()
                                .getId()
                                .equals(
                                        personalFarmacia
                                                .getSucursal()
                                                .getId()
                                )
        ) {

            return "redirect:/farmacia";
        }


        List<DespachoMedicamentoDetalle> detalles =
                despachoDetalleRepositorio
                        .findByDespachoIdOrderByIdAsc(
                                despachoId
                        );


        // =============================================
        // ALERTAS DE STOCK MÍNIMO
        // =============================================

        List<String> alertas =
                new ArrayList<>();


        for (
                DespachoMedicamentoDetalle detalle :
                detalles
        ) {

            Medicamento medicamento =
                    detalle.getMedicamentoDespachado();


            inventarioRepositorio
                    .findByMedicamentoIdAndSucursalIdAndActivoTrue(
                            medicamento.getId(),
                            despacho.getSucursal()
                                    .getId()
                    )
                    .ifPresent(
                            inventario -> {

                                Integer stockActual =
                                        inventario.getStockActual();


                                Integer stockMinimo =
                                        medicamento.getStockMinimo();


                                if (
                                        stockActual != null
                                                &&
                                                stockMinimo != null
                                                &&
                                                stockActual <= stockMinimo
                                ) {

                                    alertas.add(

                                            "ALERTA: El medicamento "
                                                    + medicamento.getNombre()
                                                    + " ha alcanzado el nivel de stock mínimo ("
                                                    + stockActual
                                                    + " unidades restantes). "
                                                    + "Se recomienda generar orden de reabastecimiento."
                                    );
                                }
                            }
                    );
        }


        model.addAttribute(
                "personalFarmacia",
                personalFarmacia
        );


        model.addAttribute(
                "despacho",
                despacho
        );


        model.addAttribute(
                "detalles",
                detalles
        );


        model.addAttribute(
                "alertas",
                alertas
        );


        return "farmacia/resultado-despacho";
    }
    private long calcularDiasReceta(
            RecetaMedica receta
    ) {

        return ChronoUnit.DAYS.between(

                receta.getFechaCreacion()
                        .toLocalDate(),

                LocalDate.now()
        );
    }


    // =====================================================
    // VALIDAR USUARIO DE FARMACIA
    // =====================================================

    private Usuario obtenerPersonalFarmacia(
            HttpSession session
    ) {

        Object usuarioSesion =
                session.getAttribute(
                        "usuarioInterno"
                );


        if (!(usuarioSesion instanceof Usuario)) {

            return null;
        }


        Usuario usuario =
                (Usuario) usuarioSesion;


        if (
                usuario.getRol() == null
                        ||
                        !"FARMACIA".equalsIgnoreCase(
                                usuario.getRol()
                                        .getNombre()
                        )
        ) {

            return null;
        }


        if (!usuario.getActivo()) {

            return null;
        }


        return usuario;
    }
    // =====================================================
// CU-11 - FA03
// PACIENTE NO ADQUIERE LOS MEDICAMENTOS
// =====================================================

    @PostMapping("/despacho/{recetaId}/cancelar")
    public String cancelarNoAdquisicion(
            @PathVariable Long recetaId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        Usuario personalFarmacia =
                obtenerPersonalFarmacia(session);


        if (personalFarmacia == null) {

            return "redirect:/personal/login";
        }


        try {

            DespachoMedicamento despacho =
                    farmaciaDespachoService
                            .cancelarPorNoAdquisicion(
                                    recetaId,
                                    personalFarmacia
                            );


            return "redirect:/farmacia/despacho/cancelado/"
                    + despacho.getId();


        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );


            return "redirect:/farmacia/despacho/"
                    + recetaId;
        }
    }


// =====================================================
// CU-11 - FA03
// RESULTADO DE NO ADQUISICIÓN
// =====================================================

    @GetMapping("/despacho/cancelado/{despachoId}")
    public String mostrarCancelacion(
            @PathVariable Long despachoId,
            HttpSession session,
            Model model
    ) {

        Usuario personalFarmacia =
                obtenerPersonalFarmacia(session);


        if (personalFarmacia == null) {

            return "redirect:/personal/login";
        }


        DespachoMedicamento despacho =
                despachoRepositorio
                        .findById(
                                despachoId
                        )
                        .orElse(null);


        if (
                despacho == null
                        ||
                        !"CANCELADO".equalsIgnoreCase(
                                despacho.getEstado()
                        )
        ) {

            return "redirect:/farmacia";
        }


        if (
                personalFarmacia.getSucursal() == null
                        ||
                        !despacho.getSucursal()
                                .getId()
                                .equals(
                                        personalFarmacia
                                                .getSucursal()
                                                .getId()
                                )
        ) {

            return "redirect:/farmacia";
        }


        model.addAttribute(
                "personalFarmacia",
                personalFarmacia
        );


        model.addAttribute(
                "despacho",
                despacho
        );


        return "farmacia/despacho-cancelado";
    }
}