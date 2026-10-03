package com.example.sistemamedico.controller;

import com.example.sistemamedico.model.Cita;
import com.example.sistemamedico.model.RecepcionEmergencia;
import com.example.sistemamedico.model.Usuario;

import com.example.sistemamedico.service.RecepcionService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/recepcion")
public class RecepcionController {

    private final RecepcionService recepcionService;


    public RecepcionController(
            RecepcionService recepcionService
    ) {

        this.recepcionService =
                recepcionService;
    }


    // =====================================================
    // MÉTODO AUXILIAR - VALIDAR RECEPCIONISTA
    // =====================================================

    private boolean esRecepcionista(
            Usuario usuario
    ) {

        return usuario != null
                &&
                usuario.getRol() != null
                &&
                "RECEPCIONISTA".equalsIgnoreCase(
                        usuario
                                .getRol()
                                .getNombre()
                );
    }


    // =====================================================
    // OBTENER RECEPCIONISTA DE SESIÓN
    // =====================================================

    private Usuario obtenerRecepcionista(
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


        if (!esRecepcionista(usuario)) {

            return null;
        }


        return usuario;
    }


    // =====================================================
    // PANTALLA PRINCIPAL
    // =====================================================

    @GetMapping
    public String mostrarRecepcion(
            HttpSession session
    ) {

        Usuario usuario =
                obtenerRecepcionista(
                        session
                );


        if (usuario == null) {

            return "redirect:/personal/login";
        }


        return "recepcion/index";
    }


    // =====================================================
    // BUSCAR
    // =====================================================

    @GetMapping("/buscar")
    public String buscar(

            @RequestParam String tipo,

            @RequestParam String valor,

            HttpSession session,

            Model model
    ) {

        Usuario usuario =
                obtenerRecepcionista(
                        session
                );


        if (usuario == null) {

            return "redirect:/personal/login";
        }


        String tipoLimpio =
                tipo == null
                        ? ""
                        : tipo.trim()
                        .toUpperCase();


        String valorLimpio =
                valor == null
                        ? ""
                        : valor.trim();


        // =================================================
        // BUSCAR POR NÚMERO DE CITA
        // =================================================

        if ("CITA".equals(
                tipoLimpio
        )) {

            try {

                Long citaId =
                        Long.parseLong(
                                valorLimpio
                        );


                Optional<Cita> cita =
                        recepcionService
                                .buscarPorNumeroCita(
                                        citaId
                                );


                if (cita.isPresent()) {

                    model.addAttribute(
                            "cita",
                            cita.get()
                    );

                } else {

                    model.addAttribute(
                            "mensajeError",
                            "No se encontró ninguna cita con ese número."
                    );
                }


            } catch (
                    NumberFormatException e
            ) {

                model.addAttribute(
                        "mensajeError",
                        "El número de cita no es válido."
                );
            }
        }


        // =================================================
        // BUSCAR POR DPI
        // =================================================

        else if ("DPI".equals(
                tipoLimpio
        )) {

            Optional<Usuario> paciente =
                    recepcionService
                            .buscarPacientePorDpi(
                                    valorLimpio
                            );


            // =================================================
            // NO EXISTE PACIENTE CON ESE DPI
            // =================================================

            if (paciente.isEmpty()) {

                model.addAttribute(
                        "pacienteNoExiste",
                        true
                );


                model.addAttribute(
                        "mensajeError",
                        "No se encontró ningún paciente con ese DPI."
                );

            } else {

                // =================================================
                // BUSCAR SOLO CITAS ACTIVAS
                // =================================================

                List<Cita> citasActivas =
                        recepcionService
                                .buscarCitasPaciente(
                                        paciente
                                                .get()
                                                .getId()
                                );


                // =================================================
                // FA04 - SIN CITAS ACTIVAS
                // =================================================

                if (citasActivas.isEmpty()) {

                    model.addAttribute(
                            "pacienteSinCitas",
                            paciente.get()
                    );

                } else {

                    // La lista viene ordenada por
                    // fechaCreacion DESC.
                    model.addAttribute(
                            "cita",
                            citasActivas.get(
                                    0
                            )
                    );
                }
            }
        }


        // =================================================
        // TIPO DE BÚSQUEDA NO VÁLIDO
        // =================================================

        else {

            model.addAttribute(
                    "mensajeError",
                    "Tipo de búsqueda no válido."
            );
        }


        model.addAttribute(
                "tipoSeleccionado",
                tipoLimpio
        );


        return "recepcion/index";
    }


    // =====================================================
    // REGISTRAR LLEGADA
    // =====================================================

    @PostMapping("/registrar-llegada")
    public String registrarLlegada(

            @RequestParam Long citaId,

            HttpSession session,

            RedirectAttributes redirectAttributes
    ) {

        Usuario usuario =
                obtenerRecepcionista(
                        session
                );


        if (usuario == null) {

            return "redirect:/personal/login";
        }


        try {

            Cita cita =
                    recepcionService
                            .registrarLlegada(
                                    citaId
                            );


            String nombrePaciente =
                    cita
                            .getPaciente()
                            .getNombre()
                            + " "
                            + cita
                            .getPaciente()
                            .getApellido();


            // =================================================
            // FA08 - EMERGENCIA
            // =================================================

            if ("EMERGENCIA".equalsIgnoreCase(
                    cita.getPrioridad()
            )) {

                redirectAttributes.addFlashAttribute(
                        "mensajeExito",
                        "Paciente "
                                + nombrePaciente
                                + " registrado con prioridad de EMERGENCIA. "
                                + "El paciente debe pasar directamente "
                                + "a toma de signos vitales."
                );

            } else {

                redirectAttributes.addFlashAttribute(
                        "mensajeExito",
                        "La llegada del paciente "
                                + nombrePaciente
                                + " ha sido registrada exitosamente. "
                                + "El paciente debe pasar a la sala de espera."
                );
            }


        } catch (
                IllegalStateException e
        ) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    e.getMessage()
            );


        } catch (
                Exception e
        ) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    "Error al registrar la llegada."
            );
        }


        return "redirect:/recepcion/buscar"
                + "?tipo=CITA"
                + "&valor="
                + citaId;
    }


    // =====================================================
    // FA08 - SIGNOS VITALES URGENTE
    // =====================================================

    @GetMapping("/signos-vitales-urgente")
    public String signosVitalesUrgente(

            @RequestParam Long citaId,

            HttpSession session,

            RedirectAttributes redirectAttributes
    ) {

        Usuario usuario =
                obtenerRecepcionista(
                        session
                );


        if (usuario == null) {

            return "redirect:/personal/login";
        }


        Cita cita =
                recepcionService
                        .buscarPorNumeroCita(
                                citaId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Cita no encontrada."
                                )
                        );


        if (!"EMERGENCIA".equalsIgnoreCase(
                cita.getPrioridad()
        )) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    "La cita seleccionada no tiene "
                            + "prioridad de emergencia."
            );


            return "redirect:/recepcion/buscar"
                    + "?tipo=CITA"
                    + "&valor="
                    + citaId;
        }


        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "Paciente derivado a toma de signos vitales "
                        + "con prioridad de EMERGENCIA. "
                        + "La atención continuará en el CU-07."
        );


        return "redirect:/recepcion/buscar"
                + "?tipo=CITA"
                + "&valor="
                + citaId;
    }


    // =====================================================
    // FA07 - MOSTRAR REASIGNACIÓN
    // =====================================================

    @GetMapping("/reasignar-medico")
    public String mostrarReasignacion(

            @RequestParam Long citaId,

            HttpSession session,

            Model model
    ) {

        Usuario usuario =
                obtenerRecepcionista(
                        session
                );


        if (usuario == null) {

            return "redirect:/personal/login";
        }


        Cita cita =
                recepcionService
                        .buscarPorNumeroCita(
                                citaId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Cita no encontrada."
                                )
                        );


        if (
                !"CONFIRMADA".equalsIgnoreCase(
                        cita.getEstado()
                )
                        &&
                        !"PACIENTE_PRESENTE".equalsIgnoreCase(
                                cita.getEstado()
                        )
        ) {

            return "redirect:/recepcion/buscar"
                    + "?tipo=CITA"
                    + "&valor="
                    + citaId;
        }


        List<Usuario> medicos =
                recepcionService
                        .obtenerMedicosDisponibles(
                                cita
                        );


        model.addAttribute(
                "cita",
                cita
        );


        model.addAttribute(
                "medicos",
                medicos
        );


        return "recepcion/reasignar-medico";
    }


    // =====================================================
    // FA07 - CONFIRMAR REASIGNACIÓN
    // =====================================================

    @PostMapping("/reasignar-medico")
    public String confirmarReasignacion(

            @RequestParam Long citaId,

            @RequestParam Long medicoId,

            @RequestParam(
                    required = false
            )
            String nota,

            HttpSession session,

            RedirectAttributes redirectAttributes
    ) {

        Usuario usuario =
                obtenerRecepcionista(
                        session
                );


        if (usuario == null) {

            return "redirect:/personal/login";
        }


        try {

            recepcionService
                    .reasignarMedico(
                            citaId,
                            medicoId,
                            nota
                    );


            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Médico reasignado correctamente"
            );


        } catch (
                Exception e
        ) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    e.getMessage()
            );
        }


        return "redirect:/recepcion/buscar"
                + "?tipo=CITA"
                + "&valor="
                + citaId;
    }


    // =====================================================
    // FA01 - MOSTRAR EMERGENCIA
    // =====================================================

    @GetMapping("/emergencia")
    public String mostrarEmergencia(
            HttpSession session
    ) {

        Usuario usuario =
                obtenerRecepcionista(
                        session
                );


        if (usuario == null) {

            return "redirect:/personal/login";
        }


        return "recepcion/emergencia";
    }


    // =====================================================
    // FA01 - REGISTRAR EMERGENCIA
    // =====================================================

    @PostMapping("/emergencia")
    public String registrarEmergencia(

            @RequestParam String dpi,

            @RequestParam String nombre,

            @RequestParam String apellido,

            HttpSession session,

            RedirectAttributes redirectAttributes
    ) {

        Usuario usuario =
                obtenerRecepcionista(
                        session
                );


        if (usuario == null) {

            return "redirect:/personal/login";
        }


        try {

            RecepcionEmergencia emergencia =
                    recepcionService
                            .registrarEmergencia(
                                    dpi,
                                    nombre,
                                    apellido
                            );


            String nombreCompleto =
                    emergencia
                            .getPaciente()
                            .getNombreCompleto();


            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Paciente "
                            + nombreCompleto
                            + " registrado con prioridad de EMERGENCIA. "
                            + "El paciente debe pasar directamente "
                            + "a toma de signos vitales."
            );


            return "redirect:/recepcion/emergencia"
                    + "?registroId="
                    + emergencia.getId();


        } catch (
                IllegalArgumentException e
        ) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    e.getMessage()
            );


            return "redirect:/recepcion/emergencia";
        }
    }
}
