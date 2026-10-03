package com.example.sistemamedico.controller;

import com.example.sistemamedico.model.Cita;
import com.example.sistemamedico.model.ExamenLaboratorio;
import com.example.sistemamedico.model.OrdenLaboratorio;
import com.example.sistemamedico.model.Usuario;

import com.example.sistemamedico.service.OrdenLaboratorioService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/medico/laboratorio")
public class OrdenLaboratorioController {


    private final OrdenLaboratorioService
            ordenLaboratorioService;


    public OrdenLaboratorioController(
            OrdenLaboratorioService ordenLaboratorioService
    ) {

        this.ordenLaboratorioService =
                ordenLaboratorioService;
    }


    // =====================================================
    // MOSTRAR FORMULARIO DE ORDEN DE LABORATORIO
    // =====================================================

    @GetMapping("/{citaId}")
    public String mostrarFormulario(

            @PathVariable Long citaId,

            HttpSession session,

            Model model,

            RedirectAttributes redirect
    ) {


        // =============================================
        // VALIDAR MEDICO EN SESION
        // =============================================

        Usuario medico =
                obtenerMedicoSesion(
                        session
                );


        if (medico == null) {

            return "redirect:/personal/login";
        }


        try {


            // =========================================
            // BUSCAR CITA
            // =========================================

            Cita cita =
                    ordenLaboratorioService
                            .obtenerCita(
                                    citaId
                            );


            // =========================================
            // VALIDAR QUE LA CITA SEA DEL MEDICO
            // =========================================

            if (
                    cita.getMedico() == null
                            ||
                            cita.getMedico().getId() == null
                            ||
                            !cita.getMedico()
                                    .getId()
                                    .equals(
                                            medico.getId()
                                    )
            ) {

                throw new IllegalArgumentException(
                        "La cita no pertenece al médico autenticado."
                );
            }


            // =========================================
            // VALIDAR ESTADO
            // =========================================

            if (
                    cita.getEstado() == null
                            ||
                            !"EVALUADO_PENDIENTE_CIERRE"
                                    .equalsIgnoreCase(
                                            cita.getEstado()
                                    )
            ) {

                throw new IllegalArgumentException(
                        "La orden de laboratorio solamente puede "
                                + "generarse cuando la consulta está "
                                + "evaluada y pendiente de cierre."
                );
            }


            // =========================================
            // OBTENER CATALOGO DE EXAMENES
            // =========================================

            List<ExamenLaboratorio> examenes =
                    ordenLaboratorioService
                            .obtenerExamenesActivos();


            // =========================================
            // ENVIAR DATOS AL HTML
            // =========================================

            model.addAttribute(
                    "medico",
                    medico
            );

            model.addAttribute(
                    "cita",
                    cita
            );

            model.addAttribute(
                    "examenes",
                    examenes
            );


            return "medico/orden-laboratorio";


        } catch (
                IllegalArgumentException e
        ) {


            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );


            return "redirect:/medico";
        }
    }


    // =====================================================
    // GUARDAR ORDEN DE LABORATORIO
    // =====================================================

    @PostMapping("/{citaId}")
    public String guardarOrden(

            @PathVariable Long citaId,

            @RequestParam(
                    name = "examenIds",
                    required = false
            )
            List<Long> examenIds,

            @RequestParam(
                    name = "observaciones",
                    required = false
            )
            String observaciones,

            HttpSession session,

            RedirectAttributes redirect
    ) {


        // =============================================
        // VALIDAR MEDICO EN SESION
        // =============================================

        Usuario medico =
                obtenerMedicoSesion(
                        session
                );


        if (medico == null) {

            return "redirect:/personal/login";
        }


        try {


            // =========================================
            // GENERAR ORDEN
            // =========================================

            OrdenLaboratorio orden =
                    ordenLaboratorioService
                            .generarOrden(
                                    citaId,
                                    medico,
                                    examenIds,
                                    observaciones
                            );


            // =========================================
            // OBTENER NOMBRES DE LOS EXAMENES
            // PARA MOSTRARLOS EN EL MENSAJE
            // =========================================

            List<ExamenLaboratorio> catalogo =
                    ordenLaboratorioService
                            .obtenerExamenesActivos();


            String listaExamenes =
                    catalogo
                            .stream()

                            .filter(
                                    examen ->
                                            examenIds != null
                                                    &&
                                                    examenIds.contains(
                                                            examen.getId()
                                                    )
                            )

                            .map(
                                    ExamenLaboratorio::getNombre
                            )

                            .collect(
                                    Collectors.joining(
                                            ", "
                                    )
                            );


            // =========================================
            // MENSAJE SEGUN FA01
            // =========================================

            String mensaje =
                    "Orden de laboratorio generada exitosamente. "
                            + "Número de orden: "
                            + orden.getId()
                            + ". Exámenes: "
                            + listaExamenes
                            + ". El paciente debe dirigirse "
                            + "al área de laboratorio.";


            redirect.addFlashAttribute(
                    "mensaje",
                    mensaje
            );


            return "redirect:/medico";


        } catch (
                IllegalArgumentException e
        ) {


            // =========================================
            // ERROR DE VALIDACION
            // =========================================

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );


            return "redirect:/medico/laboratorio/"
                    + citaId;
        }
    }


    // =====================================================
    // OBTENER MEDICO DE LA SESION
    // =====================================================

    private Usuario obtenerMedicoSesion(
            HttpSession session
    ) {


        Object usuarioSesion =
                session.getAttribute(
                        "usuarioInterno"
                );


        // =============================================
        // VERIFICAR QUE EXISTA USUARIO EN SESION
        // =============================================

        if (
                !(usuarioSesion instanceof Usuario)
        ) {

            return null;
        }


        Usuario usuario =
                (Usuario) usuarioSesion;


        // =============================================
        // VALIDAR ROL
        // =============================================

        if (
                usuario.getRol() == null
                        ||
                        usuario.getRol().getNombre() == null
                        ||
                        !"MEDICO"
                                .equalsIgnoreCase(
                                        usuario.getRol()
                                                .getNombre()
                                )
        ) {

            return null;
        }


        // =============================================
        // VALIDAR QUE ESTE ACTIVO
        // =============================================

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
