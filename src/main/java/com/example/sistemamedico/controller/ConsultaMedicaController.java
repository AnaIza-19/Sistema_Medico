package com.example.sistemamedico.controller;

import com.example.sistemamedico.Repositorio.CodigoCie10_Repositorio;
import com.example.sistemamedico.Repositorio.SignoVital_Repositorio;

import com.example.sistemamedico.model.Cita;
import com.example.sistemamedico.model.CodigoCie10;
import com.example.sistemamedico.model.ConsultaMedica;
import com.example.sistemamedico.model.SignoVital;
import com.example.sistemamedico.model.Usuario;

import com.example.sistemamedico.service.ConsultaMedicaService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Controller
@RequestMapping("/medico")
public class ConsultaMedicaController {


    // ==========================================================
    // DEPENDENCIAS
    // ==========================================================

    private final ConsultaMedicaService consultaMedicaService;

    private final SignoVital_Repositorio signoVitalRepository;

    private final CodigoCie10_Repositorio codigoCie10Repository;


    // ==========================================================
    // CONSTRUCTOR
    // ==========================================================

    public ConsultaMedicaController(
            ConsultaMedicaService consultaMedicaService,
            SignoVital_Repositorio signoVitalRepository,
            CodigoCie10_Repositorio codigoCie10Repository
    ) {

        this.consultaMedicaService =
                consultaMedicaService;

        this.signoVitalRepository =
                signoVitalRepository;

        this.codigoCie10Repository =
                codigoCie10Repository;
    }


    // ==========================================================
    // PANEL PRINCIPAL DEL MEDICO
    // ==========================================================

    @GetMapping
    public String panelMedico(
            HttpSession session,
            Model model
    ) {

        Usuario medico =
                obtenerMedicoSesion(
                        session
                );


        if (medico == null) {

            return "redirect:/personal/login";
        }


        model.addAttribute(
                "medico",
                medico
        );


        model.addAttribute(
                "citasEnEspera",
                consultaMedicaService
                        .obtenerCitasEnEspera(
                                medico.getId()
                        )
        );


        model.addAttribute(
                "citasEnConsulta",
                consultaMedicaService
                        .obtenerCitasEnConsulta(
                                medico.getId()
                        )
        );


        model.addAttribute(
                "citasEvaluadas",
                consultaMedicaService
                        .obtenerCitasEvaluadas(
                                medico.getId()
                        )
        );


        return "medico/panel";
    }


    // ==========================================================
    // INICIAR CONSULTA
    // ==========================================================

    @PostMapping("/iniciar/{citaId}")
    public String iniciarConsulta(
            @PathVariable Long citaId,
            HttpSession session,
            RedirectAttributes redirect
    ) {

        Usuario medico =
                obtenerMedicoSesion(
                        session
                );


        if (medico == null) {

            return "redirect:/personal/login";
        }


        try {

            Cita cita =
                    consultaMedicaService
                            .iniciarConsulta(
                                    citaId,
                                    medico.getId()
                            );


            String nombrePaciente =
                    cita.getPaciente()
                            .getNombreCompleto();


            String anuncio =
                    "Turno número "
                            + cita.getId()
                            + ". Paciente "
                            + nombrePaciente
                            + ", favor pasar a consulta médica.";


            redirect.addFlashAttribute(
                    "mensaje",
                    "Consulta iniciada correctamente."
            );


            redirect.addFlashAttribute(
                    "anuncioTts",
                    anuncio
            );


        } catch (
                IllegalArgumentException
                |
                IllegalStateException e
        ) {

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }


        return "redirect:/medico";
    }


    // ==========================================================
    // ABRIR / COMPLETAR CONSULTA
    // ==========================================================

    @GetMapping("/consulta/{citaId}")
    public String abrirConsulta(
            @PathVariable Long citaId,
            HttpSession session,
            Model model,
            RedirectAttributes redirect
    ) {

        Usuario medico =
                obtenerMedicoSesion(
                        session
                );


        if (medico == null) {

            return "redirect:/personal/login";
        }


        try {

            Cita cita =
                    consultaMedicaService
                            .obtenerCita(
                                    citaId
                            );


            // ==================================================
            // VALIDAR QUE LA CITA PERTENEZCA AL MEDICO
            // ==================================================

            if (
                    cita.getMedico() == null
                            ||
                            cita.getMedico()
                                    .getId() == null
                            ||
                            !cita.getMedico()
                                    .getId()
                                    .equals(
                                            medico.getId()
                                    )
            ) {

                throw new IllegalStateException(
                        "La cita no pertenece al médico autenticado."
                );
            }


            ConsultaMedica consulta =
                    consultaMedicaService
                            .obtenerOCrearConsulta(
                                    citaId,
                                    medico
                            );


            Optional<SignoVital> signos =
                    signoVitalRepository
                            .findByCitaId(
                                    citaId
                            );


            model.addAttribute(
                    "medico",
                    medico
            );


            model.addAttribute(
                    "cita",
                    cita
            );


            model.addAttribute(
                    "consulta",
                    consulta
            );


            model.addAttribute(
                    "signosVitales",
                    signos.orElse(null)
            );


            return "medico/consulta";


        } catch (
                IllegalArgumentException
                |
                IllegalStateException e
        ) {

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );


            return "redirect:/medico";
        }
    }


    // ==========================================================
    // GUARDAR CONSULTA
    // ==========================================================

    @PostMapping("/consulta/{citaId}/guardar")
    public String guardarConsulta(

            @PathVariable Long citaId,

            @RequestParam String motivoVisita,

            @RequestParam(
                    required = false
            )
            String hallazgosClinicos,

            @RequestParam(
                    required = false
            )
            String codigoCie10,

            @RequestParam(
                    required = false
            )
            String diagnostico,

            @RequestParam(
                    required = false
            )
            String planTratamiento,

            @RequestParam(
                    required = false
            )
            String notasAdicionales,

            @RequestParam String estado,

            HttpSession session,

            RedirectAttributes redirect
    ) {

        Usuario medico =
                obtenerMedicoSesion(
                        session
                );


        if (medico == null) {

            return "redirect:/personal/login";
        }


        try {

            ConsultaMedica consulta =
                    consultaMedicaService
                            .guardarConsulta(
                                    citaId,
                                    medico.getId(),
                                    motivoVisita,
                                    hallazgosClinicos,
                                    codigoCie10,
                                    diagnostico,
                                    planTratamiento,
                                    notasAdicionales,
                                    estado
                            );


            if (
                    "FINALIZADA"
                            .equalsIgnoreCase(
                                    consulta.getEstado()
                            )
            ) {

                redirect.addFlashAttribute(
                        "mensaje",
                        "La consulta ha sido finalizada exitosamente. "
                                +
                                "El paciente puede proceder a las siguientes indicaciones médicas."
                );


                return "redirect:/medico";
            }


            redirect.addFlashAttribute(
                    "mensaje",
                    "Consulta guardada correctamente."
            );


            return "redirect:/medico/consulta/"
                    + citaId;


        } catch (
                IllegalArgumentException
                |
                IllegalStateException e
        ) {

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );


            return "redirect:/medico/consulta/"
                    + citaId;
        }
    }


    // ==========================================================
    // FA06 - PACIENTE NO ASISTIO
    // ==========================================================

    @PostMapping("/no-asistio/{citaId}")
    public String marcarNoAsistio(
            @PathVariable Long citaId,
            HttpSession session,
            RedirectAttributes redirect
    ) {

        Usuario medico =
                obtenerMedicoSesion(
                        session
                );


        if (medico == null) {

            return "redirect:/personal/login";
        }


        try {

            Cita cita =
                    consultaMedicaService
                            .marcarNoAsistio(
                                    citaId,
                                    medico.getId()
                            );


            redirect.addFlashAttribute(
                    "mensaje",
                    "Cita #"
                            + cita.getId()
                            + " marcada como No Asistió."
            );


        } catch (
                IllegalArgumentException
                |
                IllegalStateException e
        ) {

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }


        return "redirect:/medico";
    }


    // ==========================================================
    // FINALIZAR ATENCION
    // ==========================================================

    @PostMapping("/finalizar-atencion/{citaId}")
    public String finalizarAtencion(
            @PathVariable Long citaId,
            HttpSession session,
            RedirectAttributes redirect
    ) {

        Usuario medico =
                obtenerMedicoSesion(
                        session
                );


        if (medico == null) {

            return "redirect:/personal/login";
        }


        try {

            Cita cita =
                    consultaMedicaService
                            .finalizarAtencion(
                                    citaId,
                                    medico.getId()
                            );


            redirect.addFlashAttribute(
                    "mensaje",
                    "Atención finalizada para cita #"
                            + cita.getId()
                            + "."
            );


        } catch (
                IllegalArgumentException
                |
                IllegalStateException e
        ) {

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }


        return "redirect:/medico";
    }


    // ==========================================================
    // BUSCAR CODIGOS CIE-10
    // ==========================================================

    @GetMapping("/api/cie10")
    @ResponseBody
    public List<Map<String, String>> buscarCie10(
            @RequestParam String buscar
    ) {

        String criterio =
                buscar == null
                        ? ""
                        : buscar.trim();


        if (criterio.isEmpty()) {

            return List.of();
        }


        // ======================================================
        // BUSCAR POR CODIGO
        // ======================================================

        List<CodigoCie10> porCodigo =
                codigoCie10Repository
                        .findTop10ByCodigoContainingIgnoreCaseAndActivoTrueOrderByCodigoAsc(
                                criterio
                        );


        // ======================================================
        // BUSCAR POR DESCRIPCION
        // ======================================================

        List<CodigoCie10> porDescripcion =
                codigoCie10Repository
                        .findTop10ByDescripcionContainingIgnoreCaseAndActivoTrueOrderByCodigoAsc(
                                criterio
                        );


        // ======================================================
        // EVITAR RESULTADOS DUPLICADOS
        // ======================================================

        Map<String, CodigoCie10> unicos =
                new LinkedHashMap<>();


        for (CodigoCie10 cie : porCodigo) {

            unicos.put(
                    cie.getCodigo(),
                    cie
            );
        }


        for (CodigoCie10 cie : porDescripcion) {

            unicos.putIfAbsent(
                    cie.getCodigo(),
                    cie
            );
        }


        // ======================================================
        // PREPARAR RESPUESTA JSON
        // ======================================================

        List<Map<String, String>> respuesta =
                new ArrayList<>();


        for (CodigoCie10 cie : unicos.values()) {

            if (respuesta.size() >= 10) {

                break;
            }


            Map<String, String> dato =
                    new LinkedHashMap<>();


            dato.put(
                    "codigo",
                    cie.getCodigo()
            );


            dato.put(
                    "descripcion",
                    cie.getDescripcion()
            );


            respuesta.add(
                    dato
            );
        }


        return respuesta;
    }


    // ==========================================================
    // OBTENER MEDICO DE LA SESION
    // ==========================================================

    private Usuario obtenerMedicoSesion(
            HttpSession session
    ) {

        Object sesion =
                session.getAttribute(
                        "usuarioInterno"
                );


        if (!(sesion instanceof Usuario usuario)) {

            return null;
        }


        if (
                usuario.getRol() == null
                        ||
                        usuario.getRol()
                                .getNombre() == null
                        ||
                        !"MEDICO".equalsIgnoreCase(
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
