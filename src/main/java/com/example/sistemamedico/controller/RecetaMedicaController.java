package com.example.sistemamedico.controller;

import com.example.sistemamedico.model.Cita;
import com.example.sistemamedico.model.Medicamento;
import com.example.sistemamedico.model.RecetaMedica;
import com.example.sistemamedico.model.RecetaMedicaDetalle;
import com.example.sistemamedico.model.Usuario;

import com.example.sistemamedico.service.RecetaMedicaService;

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
@RequestMapping("/medico/receta")
public class RecetaMedicaController {


    private final RecetaMedicaService
            recetaMedicaService;


    public RecetaMedicaController(
            RecetaMedicaService recetaMedicaService
    ) {

        this.recetaMedicaService =
                recetaMedicaService;
    }


    // =====================================================
    // MOSTRAR FORMULARIO DE RECETA
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
                    recetaMedicaService
                            .obtenerCita(
                                    citaId
                            );


            // =========================================
            // VALIDAR QUE LA CITA PERTENEZCA
            // AL MEDICO AUTENTICADO
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
            // VALIDAR ESTADO DE LA CITA
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
                        "La receta médica solamente puede "
                                + "generarse cuando la consulta está "
                                + "evaluada y pendiente de cierre."
                );
            }


            // =========================================
            // CATALOGO DE MEDICAMENTOS
            // =========================================

            List<Medicamento> medicamentos =
                    recetaMedicaService
                            .obtenerMedicamentosActivos();


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
                    "medicamentos",
                    medicamentos
            );


            return "medico/receta-medica";


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
    // GUARDAR RECETA MEDICA
    // =====================================================

    @PostMapping("/{citaId}")
    public String guardarReceta(

            @PathVariable Long citaId,

            @RequestParam(
                    name = "medicamentoIds",
                    required = false
            )
            List<Long> medicamentoIds,

            @RequestParam(
                    name = "dosis",
                    required = false
            )
            List<String> dosis,

            @RequestParam(
                    name = "frecuencias",
                    required = false
            )
            List<String> frecuencias,

            @RequestParam(
                    name = "duraciones",
                    required = false
            )
            List<String> duraciones,

            @RequestParam(
                    name = "indicaciones",
                    required = false
            )
            List<String> indicaciones,

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
            // GENERAR RECETA
            // =========================================

            RecetaMedica receta =
                    recetaMedicaService
                            .generarReceta(
                                    citaId,
                                    medico,
                                    medicamentoIds,
                                    dosis,
                                    frecuencias,
                                    duraciones,
                                    indicaciones
                            );


            // =========================================
            // OBTENER MEDICAMENTOS GUARDADOS
            // =========================================

            List<RecetaMedicaDetalle> detalles =
                    recetaMedicaService
                            .obtenerDetallesReceta(
                                    receta.getId()
                            );


            // =========================================
            // CREAR LISTA PARA EL MENSAJE
            // =========================================

            String listaMedicamentos =
                    detalles
                            .stream()

                            .map(
                                    detalle ->
                                            detalle
                                                    .getMedicamento()
                                                    .getNombre()
                            )

                            .collect(
                                    Collectors.joining(
                                            ", "
                                    )
                            );


            // =========================================
            // MENSAJE FA04
            // =========================================

            String mensaje =
                    "Receta médica generada exitosamente. "
                            + "Número de receta: "
                            + receta.getId()
                            + ". Medicamentos: "
                            + listaMedicamentos
                            + ". El paciente puede adquirirlos "
                            + "en la farmacia de la clínica.";


            redirect.addFlashAttribute(
                    "mensaje",
                    mensaje
            );


            return "redirect:/medico";


        } catch (
                IllegalArgumentException e
        ) {


            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );


            return "redirect:/medico/receta/"
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
        // VALIDAR SESION
        // =============================================

        if (
                !(usuarioSesion instanceof Usuario)
        ) {

            return null;
        }


        Usuario usuario =
                (Usuario) usuarioSesion;


        // =============================================
        // VALIDAR ROL MEDICO
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
        // VALIDAR USUARIO ACTIVO
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
