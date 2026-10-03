package com.example.sistemamedico.controller;

import com.example.sistemamedico.model.Cita;
import com.example.sistemamedico.model.Pago;
import com.example.sistemamedico.model.Usuario;
import com.example.sistemamedico.service.PagoService;

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
public class PagoController {

    private final PagoService pagoService;


    public PagoController(
            PagoService pagoService
    ) {

        this.pagoService = pagoService;
    }


    // =====================================================
    // MOSTRAR PANTALLA DE PAGO
    // =====================================================

    @GetMapping("/pago")
    public String mostrarPago(
            @RequestParam Long citaId,
            HttpSession session,
            Model model
    ) {

        // =================================================
        // VALIDAR SESIÓN
        // =================================================

        Usuario paciente =
                (Usuario) session.getAttribute(
                        "usuarioPaciente"
                );


        if (paciente == null) {

            return "redirect:/login";
        }


        // =================================================
        // BUSCAR CITA
        // =================================================

        Cita cita =
                pagoService.buscarCita(
                        citaId
                );


        // =================================================
        // VALIDAR QUE LA CITA PERTENEZCA AL PACIENTE
        // =================================================

        if (
                cita.getPaciente() == null
                        ||
                        cita.getPaciente().getId() == null
                        ||
                        !cita.getPaciente()
                                .getId()
                                .equals(
                                        paciente.getId()
                                )
        ) {

            return "redirect:/citas/mis-citas";
        }


        // =================================================
        // SI LA CITA YA ESTÁ PAGADA
        // =================================================

        if (
                "PAGADA".equals(
                        cita.getEstado()
                )
        ) {

            return "redirect:/citas/mis-citas";
        }


        // =================================================
        // VALIDAR RESERVA
        // =================================================

        if (
                !pagoService.reservaVigente(
                        cita
                )
        ) {

            return "redirect:/citas/reserva-expirada?citaId="
                    + cita.getId();
        }


        // =================================================
        // DATOS PARA LA PANTALLA
        // =================================================

        model.addAttribute(
                "cita",
                cita
        );


        model.addAttribute(
                "monto",
                pagoService.obtenerMonto(
                        cita
                )
        );


        model.addAttribute(
                "idempotencyKey",
                pagoService.generarIdempotencyKey()
        );


        model.addAttribute(
                "reservadoHasta",
                cita.getHorario()
                        .getReservadoHasta()
                        .toString()
        );


        return "citas/pago";
    }


    // =====================================================
    // PROCESAR PAGO
    // =====================================================

    @PostMapping("/pago/procesar")
    public String procesarPago(
            @RequestParam Long citaId,
            @RequestParam String numeroTarjeta,
            @RequestParam String titular,
            @RequestParam String vencimiento,
            @RequestParam String cvv,
            @RequestParam String idempotencyKey,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        // =================================================
        // VALIDAR SESIÓN
        // =================================================

        Usuario paciente =
                (Usuario) session.getAttribute(
                        "usuarioPaciente"
                );


        if (paciente == null) {

            return "redirect:/login";
        }


        // =================================================
        // BUSCAR CITA
        // =================================================

        Cita cita =
                pagoService.buscarCita(
                        citaId
                );


        // =================================================
        // VALIDAR PROPIEDAD DE LA CITA
        // =================================================

        if (
                cita.getPaciente() == null
                        ||
                        cita.getPaciente().getId() == null
                        ||
                        !cita.getPaciente()
                                .getId()
                                .equals(
                                        paciente.getId()
                                )
        ) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    "No tiene autorización para realizar el pago de esta cita."
            );


            return "redirect:/citas/mis-citas";
        }


        // =================================================
        // VALIDACIONES
        // =================================================

        String errorTarjeta =
                pagoService.validarNumeroTarjeta(
                        numeroTarjeta
                );


        String errorTitular =
                pagoService.validarTitular(
                        titular
                );


        String errorVencimiento =
                pagoService.validarVencimiento(
                        vencimiento
                );


        String errorCvv =
                pagoService.validarCvv(
                        cvv
                );


        // =================================================
        // SI HAY ERRORES DE VALIDACIÓN
        // =================================================

        if (
                errorTarjeta != null
                        ||
                        errorTitular != null
                        ||
                        errorVencimiento != null
                        ||
                        errorCvv != null
        ) {

            redirectAttributes.addFlashAttribute(
                    "errorTarjeta",
                    errorTarjeta
            );


            redirectAttributes.addFlashAttribute(
                    "errorTitular",
                    errorTitular
            );


            redirectAttributes.addFlashAttribute(
                    "errorVencimiento",
                    errorVencimiento
            );


            redirectAttributes.addFlashAttribute(
                    "errorCvv",
                    errorCvv
            );


            return "redirect:/citas/pago?citaId="
                    + citaId;
        }


        // =================================================
        // PROCESAR PAGO
        // =================================================

        try {

            Pago pago =
                    pagoService.procesarPago(
                            citaId,
                            numeroTarjeta,
                            titular,
                            vencimiento,
                            cvv,
                            idempotencyKey
                    );


            return "redirect:/citas/pago/exitoso?pagoId="
                    + pago.getId();


        } catch (
                IllegalStateException e
        ) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    e.getMessage()
            );


            return "redirect:/citas/pago?citaId="
                    + citaId;
        }
    }


    // =====================================================
    // PANTALLA PAGO EXITOSO
    // =====================================================

    @GetMapping("/pago/exitoso")
    public String mostrarPagoExitoso(
            @RequestParam Long pagoId,
            HttpSession session,
            Model model
    ) {

        // =================================================
        // VALIDAR SESIÓN
        // =================================================

        Usuario paciente =
                (Usuario) session.getAttribute(
                        "usuarioPaciente"
                );


        if (paciente == null) {

            return "redirect:/login";
        }


        // =================================================
        // BUSCAR PAGO
        // =================================================

        Pago pago =
                pagoService.buscarPago(
                        pagoId
                );


        // =================================================
        // VALIDAR QUE EL PAGO PERTENEZCA AL PACIENTE
        // =================================================

        if (
                pago.getCita() == null
                        ||
                        pago.getCita().getPaciente() == null
                        ||
                        pago.getCita().getPaciente().getId() == null
                        ||
                        !pago.getCita()
                                .getPaciente()
                                .getId()
                                .equals(
                                        paciente.getId()
                                )
        ) {

            return "redirect:/citas/mis-citas";
        }


        // =================================================
        // DATOS PARA LA VISTA
        // =================================================

        model.addAttribute(
                "pago",
                pago
        );


        return "citas/pago-exitoso";
    }
}