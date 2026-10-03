package com.example.sistemamedico.controller;

import com.example.sistemamedico.model.Usuario;
import com.example.sistemamedico.service.AgendamientoCitaService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/citas")
public class AgendamientoCitaController {

    private final AgendamientoCitaService agendamientoCitaService;


    public AgendamientoCitaController(
            AgendamientoCitaService agendamientoCitaService
    ) {

        this.agendamientoCitaService =
                agendamientoCitaService;
    }


    // =====================================================
    // MÉTODO AUXILIAR
    // VERIFICAR SI EL USUARIO ES RECEPCIONISTA
    // =====================================================

    private boolean esRecepcionista(
            Object usuarioSesion
    ) {

        if (!(usuarioSesion instanceof Usuario usuario)) {

            return false;
        }


        return usuario.getRol() != null
                &&
                "RECEPCIONISTA".equalsIgnoreCase(
                        usuario.getRol().getNombre()
                );
    }


    // =====================================================
    // MÉTODO AUXILIAR
    // VERIFICAR SI EL USUARIO ES PACIENTE
    // =====================================================

    private boolean esPaciente(
            Usuario usuario
    ) {

        return usuario != null
                &&
                usuario.getRol() != null
                &&
                "PACIENTE".equalsIgnoreCase(
                        usuario.getRol().getNombre()
                );
    }


    // =====================================================
    // PASO 1 - SELECCIONAR SUCURSAL
    // =====================================================

    @GetMapping("/agendar")
    public String mostrarPasoSucursal(

            @RequestParam(
                    value = "pacienteId",
                    required = false
            )
            Long pacienteId,

            @RequestParam(
                    value = "origen",
                    required = false
            )
            String origen,

            Model model,

            HttpSession session
    ) {

        // =================================================
        // MODO RECEPCIÓN / WALK-IN
        // =================================================

        if ("recepcion".equalsIgnoreCase(origen)) {

            Object usuarioInterno =
                    session.getAttribute(
                            "usuarioInterno"
                    );


            if (!esRecepcionista(usuarioInterno)) {

                return "redirect:/personal/login";
            }


            if (pacienteId == null) {

                return "redirect:/recepcion";
            }


            Usuario paciente =
                    agendamientoCitaService
                            .buscarUsuario(
                                    pacienteId
                            )
                            .orElse(null);


            if (paciente == null) {

                return "redirect:/recepcion";
            }


            if (!esPaciente(paciente)) {

                return "redirect:/recepcion";
            }


            if (!Boolean.TRUE.equals(
                    paciente.getActivo()
            )) {

                return "redirect:/recepcion";
            }


            model.addAttribute(
                    "pacienteWalkIn",
                    paciente
            );


            model.addAttribute(
                    "pacienteId",
                    pacienteId
            );


            model.addAttribute(
                    "origen",
                    "recepcion"
            );

        } else {

            // =================================================
            // MODO NORMAL DEL PACIENTE
            // =================================================

            Object usuarioSesion =
                    session.getAttribute(
                            "usuarioPaciente"
                    );


            if (!(usuarioSesion instanceof Usuario)) {

                return "redirect:/login";
            }


            Usuario paciente =
                    (Usuario) usuarioSesion;


            if (!esPaciente(paciente)) {

                return "redirect:/login";
            }


            if (!Boolean.TRUE.equals(
                    paciente.getActivo()
            )) {

                return "redirect:/login";
            }
        }


        model.addAttribute(
                "sucursales",
                agendamientoCitaService
                        .obtenerSucursalesActivas()
        );


        model.addAttribute(
                "pasoActual",
                1
        );


        return "citas/agendar-paso1";
    }


    // =====================================================
    // PASO 2 - SELECCIONAR ESPECIALIDAD
    // =====================================================

    @GetMapping("/agendar/especialidad")
    public String mostrarPasoEspecialidad(

            @RequestParam Long sucursalId,

            @RequestParam(
                    value = "pacienteId",
                    required = false
            )
            Long pacienteId,

            @RequestParam(
                    value = "origen",
                    required = false
            )
            String origen,

            Model model,

            HttpSession session
    ) {

        if ("recepcion".equalsIgnoreCase(origen)) {

            Object usuarioInterno =
                    session.getAttribute(
                            "usuarioInterno"
                    );


            if (!esRecepcionista(usuarioInterno)) {

                return "redirect:/personal/login";
            }


            if (pacienteId == null) {

                return "redirect:/recepcion";
            }
        }


        var sucursal =
                agendamientoCitaService
                        .buscarSucursal(
                                sucursalId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Sucursal no encontrada."
                                )
                        );


        var especialidades =
                agendamientoCitaService
                        .obtenerEspecialidadesPorSucursal(
                                sucursalId
                        );


        model.addAttribute(
                "sucursalId",
                sucursalId
        );


        model.addAttribute(
                "especialidades",
                especialidades
        );


        model.addAttribute(
                "pasoActual",
                2
        );


        model.addAttribute(
                "pacienteId",
                pacienteId
        );


        model.addAttribute(
                "origen",
                origen
        );


        if (especialidades.isEmpty()) {

            model.addAttribute(
                    "mensajeError",
                    "No hay especialidades disponibles para la sucursal "
                            + sucursal.getNombre()
                            + ". Seleccione otra sucursal."
            );
        }


        return "citas/agendar-paso2";
    }


    // =====================================================
    // PASO 3 - SELECCIONAR MÉDICO
    // =====================================================

    @GetMapping("/agendar/medico")
    public String mostrarPasoMedico(

            @RequestParam Long sucursalId,

            @RequestParam Long especialidadId,

            @RequestParam(
                    value = "pacienteId",
                    required = false
            )
            Long pacienteId,

            @RequestParam(
                    value = "origen",
                    required = false
            )
            String origen,

            Model model,

            HttpSession session
    ) {

        if ("recepcion".equalsIgnoreCase(origen)) {

            Object usuarioInterno =
                    session.getAttribute(
                            "usuarioInterno"
                    );


            if (!esRecepcionista(usuarioInterno)) {

                return "redirect:/personal/login";
            }


            if (pacienteId == null) {

                return "redirect:/recepcion";
            }
        }


        var sucursal =
                agendamientoCitaService
                        .buscarSucursal(
                                sucursalId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Sucursal no encontrada."
                                )
                        );


        var especialidad =
                agendamientoCitaService
                        .buscarEspecialidad(
                                especialidadId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Especialidad no encontrada."
                                )
                        );


        var medicos =
                agendamientoCitaService
                        .obtenerMedicos(
                                sucursalId,
                                especialidadId
                        );


        model.addAttribute(
                "sucursalId",
                sucursalId
        );


        model.addAttribute(
                "especialidadId",
                especialidadId
        );


        model.addAttribute(
                "medicos",
                medicos
        );


        model.addAttribute(
                "pasoActual",
                3
        );


        model.addAttribute(
                "pacienteId",
                pacienteId
        );


        model.addAttribute(
                "origen",
                origen
        );


        if (medicos.isEmpty()) {

            model.addAttribute(
                    "mensajeError",
                    "No se encontraron horarios disponibles para la especialidad "
                            + especialidad.getNombre()
                            + " en la Sede "
                            + sucursal.getNombre()
                            + ". Por favor, seleccione otra especialidad o sede."
            );
        }


        return "citas/agendar-paso3";
    }


    // =====================================================
    // PASO 4 - SELECCIONAR FECHA Y HORA
    // =====================================================

    @GetMapping("/agendar/horario")
    public String mostrarPasoHorario(

            @RequestParam Long sucursalId,

            @RequestParam Long especialidadId,

            @RequestParam Long medicoId,

            @RequestParam(
                    value = "pacienteId",
                    required = false
            )
            Long pacienteId,

            @RequestParam(
                    value = "origen",
                    required = false
            )
            String origen,

            Model model,

            HttpSession session
    ) {

        if ("recepcion".equalsIgnoreCase(origen)) {

            Object usuarioInterno =
                    session.getAttribute(
                            "usuarioInterno"
                    );


            if (!esRecepcionista(usuarioInterno)) {

                return "redirect:/personal/login";
            }


            if (pacienteId == null) {

                return "redirect:/recepcion";
            }
        }


        var sucursal =
                agendamientoCitaService
                        .buscarSucursal(
                                sucursalId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Sucursal no encontrada."
                                )
                        );


        var especialidad =
                agendamientoCitaService
                        .buscarEspecialidad(
                                especialidadId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Especialidad no encontrada."
                                )
                        );


        var horarios =
                agendamientoCitaService
                        .obtenerHorariosDisponibles(
                                medicoId,
                                sucursalId,
                                especialidadId
                        );


        model.addAttribute(
                "sucursalId",
                sucursalId
        );


        model.addAttribute(
                "especialidadId",
                especialidadId
        );


        model.addAttribute(
                "medicoId",
                medicoId
        );


        model.addAttribute(
                "horarios",
                horarios
        );


        model.addAttribute(
                "pasoActual",
                4
        );


        model.addAttribute(
                "pacienteId",
                pacienteId
        );


        model.addAttribute(
                "origen",
                origen
        );


        if (horarios.isEmpty()) {

            model.addAttribute(
                    "mensajeError",
                    "No se encontraron horarios disponibles para la especialidad "
                            + especialidad.getNombre()
                            + " en la Sede "
                            + sucursal.getNombre()
                            + ". Por favor, seleccione otra especialidad o sede."
            );
        }


        return "citas/agendar-paso4";
    }


    // =====================================================
    // PASO 5 - MOSTRAR CONFIRMACIÓN
    // =====================================================

    @GetMapping("/agendar/confirmar")
    public String mostrarPasoConfirmar(

            @RequestParam Long sucursalId,

            @RequestParam Long especialidadId,

            @RequestParam Long medicoId,

            @RequestParam Long horarioId,

            @RequestParam(
                    value = "pacienteId",
                    required = false
            )
            Long pacienteId,

            @RequestParam(
                    value = "origen",
                    required = false
            )
            String origen,

            Model model,

            HttpSession session
    ) {

        if ("recepcion".equalsIgnoreCase(origen)) {

            Object usuarioInterno =
                    session.getAttribute(
                            "usuarioInterno"
                    );


            if (!esRecepcionista(usuarioInterno)) {

                return "redirect:/personal/login";
            }


            if (pacienteId == null) {

                return "redirect:/recepcion";
            }
        }


        var sucursal =
                agendamientoCitaService
                        .buscarSucursal(
                                sucursalId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Sucursal no encontrada."
                                )
                        );


        var especialidad =
                agendamientoCitaService
                        .buscarEspecialidad(
                                especialidadId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Especialidad no encontrada."
                                )
                        );


        var medico =
                agendamientoCitaService
                        .buscarUsuario(
                                medicoId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Médico no encontrado."
                                )
                        );


        var horario =
                agendamientoCitaService
                        .buscarHorario(
                                horarioId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Horario no encontrado."
                                )
                        );


        model.addAttribute(
                "sucursal",
                sucursal
        );


        model.addAttribute(
                "especialidad",
                especialidad
        );


        model.addAttribute(
                "medico",
                medico
        );


        model.addAttribute(
                "horario",
                horario
        );


        model.addAttribute(
                "sucursalId",
                sucursalId
        );


        model.addAttribute(
                "especialidadId",
                especialidadId
        );


        model.addAttribute(
                "medicoId",
                medicoId
        );


        model.addAttribute(
                "horarioId",
                horarioId
        );


        model.addAttribute(
                "pacienteId",
                pacienteId
        );


        model.addAttribute(
                "origen",
                origen
        );


        if (
                "recepcion".equalsIgnoreCase(origen)
                        &&
                        pacienteId != null
        ) {

            Usuario paciente =
                    agendamientoCitaService
                            .buscarUsuario(
                                    pacienteId
                            )
                            .orElseThrow(
                                    () -> new IllegalArgumentException(
                                            "Paciente no encontrado."
                                    )
                            );


            if (!esPaciente(paciente)) {

                return "redirect:/recepcion";
            }


            if (!Boolean.TRUE.equals(
                    paciente.getActivo()
            )) {

                return "redirect:/recepcion";
            }


            model.addAttribute(
                    "pacienteWalkIn",
                    paciente
            );
        }


        model.addAttribute(
                "pasoActual",
                5
        );


        return "citas/agendar-paso5";
    }


    // =====================================================
    // CONFIRMAR Y REGISTRAR CITA
    // =====================================================

    @PostMapping("/agendar/confirmar")
    public String confirmarCita(

            @RequestParam Long sucursalId,

            @RequestParam Long especialidadId,

            @RequestParam Long medicoId,

            @RequestParam Long horarioId,

            @RequestParam String motivoConsulta,

            @RequestParam(
                    value = "pacienteId",
                    required = false
            )
            Long pacienteId,

            @RequestParam(
                    value = "origen",
                    required = false
            )
            String origen,

            HttpSession session,

            RedirectAttributes redirectAttributes
    ) {

        Usuario paciente;

        String origenCita;


        // =================================================
        // WALK-IN DESDE RECEPCIÓN
        // =================================================

        if ("recepcion".equalsIgnoreCase(origen)) {

            Object usuarioInternoSesion =
                    session.getAttribute(
                            "usuarioInterno"
                    );


            if (!esRecepcionista(
                    usuarioInternoSesion
            )) {

                return "redirect:/personal/login";
            }


            if (pacienteId == null) {

                redirectAttributes.addFlashAttribute(
                        "mensajeError",
                        "No se pudo identificar al paciente."
                );


                return "redirect:/recepcion";
            }


            paciente =
                    agendamientoCitaService
                            .buscarUsuario(
                                    pacienteId
                            )
                            .orElse(null);


            if (paciente == null) {

                redirectAttributes.addFlashAttribute(
                        "mensajeError",
                        "Paciente no encontrado."
                );


                return "redirect:/recepcion";
            }


            if (!esPaciente(paciente)) {

                redirectAttributes.addFlashAttribute(
                        "mensajeError",
                        "El usuario seleccionado no corresponde a un paciente."
                );


                return "redirect:/recepcion";
            }


            if (!Boolean.TRUE.equals(
                    paciente.getActivo()
            )) {

                redirectAttributes.addFlashAttribute(
                        "mensajeError",
                        "El paciente seleccionado se encuentra inactivo."
                );


                return "redirect:/recepcion";
            }


            // =============================================
            // CITA CREADA POR PERSONAL INTERNO
            // =============================================

            origenCita =
                    "INTERNO";

        } else {

            // =================================================
            // AGENDAMIENTO NORMAL DEL PACIENTE
            // =================================================

            Object usuarioSesion =
                    session.getAttribute(
                            "usuarioPaciente"
                    );


            if (!(usuarioSesion instanceof Usuario)) {

                return "redirect:/login";
            }


            paciente =
                    (Usuario) usuarioSesion;


            if (!esPaciente(paciente)) {

                return "redirect:/login";
            }


            if (!Boolean.TRUE.equals(
                    paciente.getActivo()
            )) {

                return "redirect:/login";
            }


            // =============================================
            // CITA CREADA DESDE EL PORTAL
            // =============================================

            origenCita =
                    "PORTAL";
        }


        try {

            var cita =
                    agendamientoCitaService
                            .registrarCita(
                                    paciente.getId(),
                                    sucursalId,
                                    especialidadId,
                                    medicoId,
                                    horarioId,
                                    motivoConsulta,
                                    origenCita
                            );


            // =================================================
            // WALK-IN DESDE RECEPCIÓN
            // =================================================

            if ("INTERNO".equals(
                    origenCita
            )) {

                redirectAttributes.addFlashAttribute(
                        "mensajeExito",
                        "Cita creada correctamente para "
                                + paciente.getNombreCompleto()
                                + ". La cita queda pendiente de pago."
                );


                return "redirect:/recepcion/buscar"
                        + "?tipo=CITA"
                        + "&valor="
                        + cita.getId();
            }


            // =================================================
            // PORTAL DEL PACIENTE
            // =================================================

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Su cita ha sido registrada exitosamente. "
                            + "Será redirigido al proceso de pago "
                            + "para confirmar la reserva."
            );


            return "redirect:/citas/pago?citaId="
                    + cita.getId();


        } catch (
                IllegalArgumentException
                |
                IllegalStateException e
        ) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    e.getMessage()
            );


            String url =
                    "redirect:/citas/agendar/horario"
                            + "?sucursalId="
                            + sucursalId
                            + "&especialidadId="
                            + especialidadId
                            + "&medicoId="
                            + medicoId;


            if ("INTERNO".equals(
                    origenCita
            )) {

                url +=
                        "&pacienteId="
                                + paciente.getId()
                                + "&origen=recepcion";
            }


            return url;
        }
    }


    // =====================================================
    // FA03 - TIEMPO DE RESERVA EXPIRADO
    // =====================================================

    @GetMapping("/reserva-expirada")
    public String reservaExpirada(

            @RequestParam Long citaId,

            RedirectAttributes redirectAttributes
    ) {

        var cita =
                agendamientoCitaService
                        .liberarReservaExpirada(
                                citaId
                        );


        // =================================================
        // SOLO PORTAL PUEDE EXPIRAR
        // =================================================

        if (
                cita.getOrigen() != null
                        &&
                        "PORTAL".equalsIgnoreCase(
                                cita.getOrigen()
                        )
                        &&
                        "EXPIRADA".equalsIgnoreCase(
                                cita.getEstado()
                        )
        ) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    "El tiempo para confirmar su cita ha expirado. "
                            + "El horario seleccionado ha sido liberado. "
                            + "Por favor, seleccione un nuevo horario."
            );


            return "redirect:/citas/agendar/horario"
                    + "?sucursalId="
                    + cita.getSucursal().getId()
                    + "&especialidadId="
                    + cita.getEspecialidad().getId()
                    + "&medicoId="
                    + cita.getMedico().getId();
        }


        // =================================================
        // CITA INTERNA
        // NO DEBE EXPIRAR
        // =================================================

        redirectAttributes.addFlashAttribute(
                "mensajeError",
                "La cita no está sujeta a cancelación automática."
        );


        return "redirect:/citas/mis-citas";
    }
}