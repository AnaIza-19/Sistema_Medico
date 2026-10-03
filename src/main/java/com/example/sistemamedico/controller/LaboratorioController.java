package com.example.sistemamedico.controller;

import com.example.sistemamedico.Repositorio.OrdenLaboratorioDetalle_Repositorio;
import com.example.sistemamedico.Repositorio.OrdenLaboratorio_Repositorio;

import com.example.sistemamedico.model.OrdenLaboratorio;
import com.example.sistemamedico.model.OrdenLaboratorioDetalle;
import com.example.sistemamedico.model.Usuario;

import jakarta.servlet.http.HttpSession;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;


@Controller
@RequestMapping("/laboratorio")
public class LaboratorioController {


    // =====================================================
    // REPOSITORIOS
    // =====================================================

    private final OrdenLaboratorio_Repositorio
            ordenLaboratorioRepository;

    private final OrdenLaboratorioDetalle_Repositorio
            ordenLaboratorioDetalleRepository;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public LaboratorioController(

            OrdenLaboratorio_Repositorio
                    ordenLaboratorioRepository,

            OrdenLaboratorioDetalle_Repositorio
                    ordenLaboratorioDetalleRepository
    ) {

        this.ordenLaboratorioRepository =
                ordenLaboratorioRepository;

        this.ordenLaboratorioDetalleRepository =
                ordenLaboratorioDetalleRepository;
    }


    // =====================================================
    // PANTALLA PRINCIPAL
    // =====================================================

    @GetMapping
    public String mostrarOrdenes(

            HttpSession session,

            Model model
    ) {

        Usuario personalLaboratorio =
                obtenerPersonalLaboratorio(
                        session
                );


        if (personalLaboratorio == null) {

            return "redirect:/personal/login";
        }


        List<OrdenLaboratorio> ordenes =
                ordenLaboratorioRepository
                        .findAll();


        model.addAttribute(
                "personalLaboratorio",
                personalLaboratorio
        );


        model.addAttribute(
                "ordenes",
                ordenes
        );


        return "laboratorio/ordenes";
    }


    // =====================================================
    // VER DETALLE DE UNA ORDEN
    // =====================================================

    @GetMapping("/orden/{ordenId}")
    public String verDetalleOrden(

            @PathVariable Long ordenId,

            HttpSession session,

            Model model
    ) {

        Usuario personalLaboratorio =
                obtenerPersonalLaboratorio(
                        session
                );


        if (personalLaboratorio == null) {

            return "redirect:/personal/login";
        }


        OrdenLaboratorio orden =
                ordenLaboratorioRepository
                        .findById(
                                ordenId
                        )
                        .orElse(null);


        if (orden == null) {

            return "redirect:/laboratorio";
        }


        List<OrdenLaboratorioDetalle> detalles =
                ordenLaboratorioDetalleRepository
                        .findByOrdenIdOrderByIdAsc(
                                ordenId
                        );


        model.addAttribute(
                "personalLaboratorio",
                personalLaboratorio
        );


        model.addAttribute(
                "orden",
                orden
        );


        model.addAttribute(
                "detalles",
                detalles
        );


        return "laboratorio/detalle-orden";
    }


    // =====================================================
    // CU-09
    // GUARDAR RESULTADO DE UN EXAMEN
    // =====================================================

    @PostMapping("/resultado/guardar")
    public String guardarResultado(

            @RequestParam Long detalleId,

            @RequestParam String valorResultado,

            @RequestParam String unidad,

            @RequestParam
            @DateTimeFormat(
                    pattern = "yyyy-MM-dd'T'HH:mm"
            )
            LocalDateTime fechaResultado,

            @RequestParam(
                    required = false,
                    defaultValue = "false"
            )
            boolean fueraRango,

            @RequestParam(
                    required = false
            )
            String notasResultado,

            HttpSession session,

            RedirectAttributes redirectAttributes
    ) {

        // =================================================
        // VALIDAR PERSONAL DE LABORATORIO
        // =================================================

        Usuario personalLaboratorio =
                obtenerPersonalLaboratorio(
                        session
                );


        if (personalLaboratorio == null) {

            return "redirect:/personal/login";
        }


        // =================================================
        // BUSCAR DETALLE
        // =================================================

        OrdenLaboratorioDetalle detalle =
                ordenLaboratorioDetalleRepository
                        .findById(
                                detalleId
                        )
                        .orElse(null);


        if (
                detalle == null
                        ||
                        detalle.getOrden() == null
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "mensajeError",
                            "El examen seleccionado no existe."
                    );


            return "redirect:/laboratorio";
        }


        OrdenLaboratorio orden =
                detalle.getOrden();


        // =================================================
        // SOLO ORDENES EN PROCESO
        // =================================================

        if (
                orden.getEstado() == null
                        ||
                        !"EN_PROCESO"
                                .equalsIgnoreCase(
                                        orden.getEstado()
                                )
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "mensajeError",
                            "La orden debe estar en estado En proceso para registrar resultados."
                    );


            return "redirect:/laboratorio/orden/"
                    + orden.getId();
        }


        // =================================================
        // ORDEN EXTERNA
        // =================================================

        if (
                Boolean.TRUE.equals(
                        orden.getOrdenExterna()
                )
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "mensajeError",
                            "No se pueden registrar resultados en una orden externa."
                    );


            return "redirect:/laboratorio/orden/"
                    + orden.getId();
        }


        // =================================================
        // RESULTADO YA PUBLICADO
        // =================================================

        if (
                Boolean.TRUE.equals(
                        detalle.getPublicado()
                )
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "mensajeError",
                            "El resultado ya fue publicado."
                    );


            return "redirect:/laboratorio/orden/"
                    + orden.getId();
        }


        // =================================================
        // VALIDAR VALOR
        // =================================================

        String valor =
                valorResultado == null
                        ? ""
                        : valorResultado.trim();


        if (valor.isBlank()) {

            redirectAttributes
                    .addFlashAttribute(
                            "mensajeError",
                            "Debe ingresar el valor del resultado."
                    );


            return "redirect:/laboratorio/orden/"
                    + orden.getId();
        }


        // =================================================
        // VALIDAR UNIDAD
        // =================================================

        String unidadLimpia =
                unidad == null
                        ? ""
                        : unidad.trim();


        if (unidadLimpia.isBlank()) {

            redirectAttributes
                    .addFlashAttribute(
                            "mensajeError",
                            "Debe ingresar la unidad del resultado."
                    );


            return "redirect:/laboratorio/orden/"
                    + orden.getId();
        }


        // =================================================
        // VALIDAR FECHA
        // =================================================

        if (fechaResultado == null) {

            redirectAttributes
                    .addFlashAttribute(
                            "mensajeError",
                            "Debe ingresar la fecha del resultado."
                    );


            return "redirect:/laboratorio/orden/"
                    + orden.getId();
        }


        // =================================================
        // GUARDAR RESULTADO
        // =================================================

        detalle.setValorResultado(
                valor
        );


        detalle.setUnidad(
                unidadLimpia
        );


        detalle.setFechaResultado(
                fechaResultado
        );


        detalle.setFueraRango(
                fueraRango
        );


        detalle.setNotasResultado(
                notasResultado == null
                        ? null
                        : notasResultado.trim()
        );


        ordenLaboratorioDetalleRepository
                .save(
                        detalle
                );


        redirectAttributes
                .addFlashAttribute(
                        "mensajeExito",
                        "Resultado guardado exitosamente."
                );


        return "redirect:/laboratorio/orden/"
                + orden.getId();
    }


    // =====================================================
    // CU-09
    // PUBLICAR RESULTADO INDIVIDUAL
    // =====================================================

    @PostMapping("/resultado/publicar")
    public String publicarResultado(

            @RequestParam Long detalleId,

            HttpSession session,

            RedirectAttributes redirectAttributes
    ) {

        // =================================================
        // VALIDAR PERSONAL
        // =================================================

        Usuario personalLaboratorio =
                obtenerPersonalLaboratorio(
                        session
                );


        if (personalLaboratorio == null) {

            return "redirect:/personal/login";
        }


        // =================================================
        // BUSCAR DETALLE
        // =================================================

        OrdenLaboratorioDetalle detalle =
                ordenLaboratorioDetalleRepository
                        .findById(
                                detalleId
                        )
                        .orElse(null);


        if (
                detalle == null
                        ||
                        detalle.getOrden() == null
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "mensajeError",
                            "El examen seleccionado no existe."
                    );


            return "redirect:/laboratorio";
        }


        OrdenLaboratorio orden =
                detalle.getOrden();


        // =================================================
        // VALIDAR ESTADO
        // =================================================

        if (
                !"EN_PROCESO"
                        .equalsIgnoreCase(
                                orden.getEstado()
                        )
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "mensajeError",
                            "La orden debe estar En proceso."
                    );


            return "redirect:/laboratorio/orden/"
                    + orden.getId();
        }


        // =================================================
        // VERIFICAR QUE TENGA RESULTADO GUARDADO
        // =================================================

        if (
                detalle.getValorResultado() == null
                        ||
                        detalle.getValorResultado()
                                .isBlank()
                        ||
                        detalle.getFechaResultado() == null
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "mensajeError",
                            "Primero debe guardar el resultado del examen."
                    );


            return "redirect:/laboratorio/orden/"
                    + orden.getId();
        }


        // =================================================
        // PUBLICAR
        // =================================================

        detalle.setPublicado(
                true
        );


        ordenLaboratorioDetalleRepository
                .save(
                        detalle
                );


        // =================================================
        // VERIFICAR SI TODOS LOS RESULTADOS
        // YA ESTAN PUBLICADOS
        // =================================================

        List<OrdenLaboratorioDetalle> detalles =
                ordenLaboratorioDetalleRepository
                        .findByOrdenIdOrderByIdAsc(
                                orden.getId()
                        );


        boolean todosPublicados =
                !detalles.isEmpty()
                        &&
                        detalles.stream()
                                .allMatch(
                                        item ->
                                                Boolean.TRUE.equals(
                                                        item.getPublicado()
                                                )
                                );


        // =================================================
        // TODOS PUBLICADOS -> COMPLETADA
        // =================================================

        if (todosPublicados) {

            orden.setEstado(
                    "COMPLETADA"
            );


            ordenLaboratorioRepository
                    .save(
                            orden
                    );
        }


        redirectAttributes
                .addFlashAttribute(
                        "mensajeExito",
                        "Resultado publicado exitosamente."
                );


        return "redirect:/laboratorio/orden/"
                + orden.getId();
    }


    // =====================================================
    // OBTENER PERSONAL DE LABORATORIO
    // =====================================================

    private Usuario obtenerPersonalLaboratorio(
            HttpSession session
    ) {

        Object usuarioSesion =
                session.getAttribute(
                        "usuarioInterno"
                );


        if (
                !(usuarioSesion instanceof Usuario)
        ) {

            return null;
        }


        Usuario usuario =
                (Usuario) usuarioSesion;


        if (
                usuario.getRol() == null
                        ||
                        usuario.getRol()
                                .getNombre() == null
                        ||
                        !"LABORATORIO"
                                .equalsIgnoreCase(
                                        usuario.getRol()
                                                .getNombre()
                                )
        ) {

            return null;
        }


        if (
                usuario.getActivo() == null
                        ||
                        !usuario.getActivo()
        ) {

            return null;
        }


        return usuario;
    }
}
