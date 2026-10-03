package com.example.sistemamedico.controller;

import com.example.sistemamedico.model.Cita;
import com.example.sistemamedico.model.Pago;
import com.example.sistemamedico.model.Usuario;

import com.example.sistemamedico.service.CajaService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Controller
@RequestMapping("/caja")
public class CajaController {

    private final CajaService cajaService;


    public CajaController(
            CajaService cajaService
    ) {

        this.cajaService =
                cajaService;
    }


    // =====================================================
    // VERIFICAR SI ES CAJERO
    // =====================================================

    private boolean esCajero(
            Usuario usuario
    ) {

        return usuario != null
                &&
                usuario.getRol() != null
                &&
                "CAJERO".equalsIgnoreCase(
                        usuario
                                .getRol()
                                .getNombre()
                );
    }


    // =====================================================
    // OBTENER CAJERO DE SESIÓN
    // =====================================================

    private Usuario obtenerCajero(
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


        if (!esCajero(
                usuario
        )) {

            return null;
        }


        return usuario;
    }


    // =====================================================
    // PANTALLA PRINCIPAL
    // =====================================================

    @GetMapping
    public String mostrarCaja(

            HttpSession session,

            Model model
    ) {

        Usuario cajero =
                obtenerCajero(
                        session
                );


        if (cajero == null) {

            return "redirect:/personal/login";
        }


        model.addAttribute(
                "cajero",
                cajero
        );


        return "caja/index";
    }


    // =====================================================
    // BUSCAR CITA
    //
    // PERMITE:
    // - NÚMERO DE CITA
    // - DPI
    //
    // SI SE BUSCA POR DPI Y EXISTEN VARIAS CITAS
    // PENDIENTES, SE MUESTRAN TODAS.
    // =====================================================

    @GetMapping("/buscar")
    public String buscar(

            @RequestParam String tipo,

            @RequestParam String valor,

            HttpSession session,

            Model model
    ) {

        Usuario cajero =
                obtenerCajero(
                        session
                );


        if (cajero == null) {

            return "redirect:/personal/login";
        }


        model.addAttribute(
                "cajero",
                cajero
        );


        String tipoLimpio =
                tipo == null
                        ? ""
                        : tipo
                        .trim()
                        .toUpperCase();


        String valorLimpio =
                valor == null
                        ? ""
                        : valor.trim();


        // =================================================
        // CONSERVAR DATOS DE BÚSQUEDA
        // =================================================

        model.addAttribute(
                "tipoSeleccionado",
                tipoLimpio
        );


        model.addAttribute(
                "valorBuscado",
                valorLimpio
        );


        // =================================================
        // VALIDAR CRITERIO VACÍO
        // =================================================

        if (valorLimpio.isBlank()) {

            model.addAttribute(
                    "mensajeError",
                    "Debe ingresar un criterio de búsqueda."
            );


            return "caja/index";
        }


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


                Optional<Cita> resultado =
                        cajaService
                                .buscarPendientePorNumeroCita(
                                        citaId
                                );


                if (resultado.isPresent()) {

                    Cita cita =
                            resultado.get();


                    model.addAttribute(
                            "cita",
                            cita
                    );


                    model.addAttribute(
                            "monto",
                            cajaService.obtenerMonto(
                                    cita
                            )
                    );

                } else {

                    model.addAttribute(
                            "mensajeError",
                            "No se encontraron citas pendientes de pago "
                                    + "para el criterio ingresado."
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


            return "caja/index";
        }


        // =================================================
        // BUSCAR POR DPI
        // =================================================

        if ("DPI".equals(
                tipoLimpio
        )) {

            // =================================================
            // VALIDAR DPI
            // =================================================

            if (!valorLimpio.matches(
                    "\\d{13}"
            )) {

                model.addAttribute(
                        "mensajeError",
                        "El DPI debe contener exactamente 13 dígitos."
                );


                return "caja/index";
            }


            // =================================================
            // BUSCAR TODAS LAS CITAS PENDIENTES
            // =================================================

            List<Cita> citas =
                    cajaService
                            .buscarPendientesPorDpi(
                                    valorLimpio
                            );


            if (citas.isEmpty()) {

                model.addAttribute(
                        "mensajeError",
                        "No se encontraron citas pendientes de pago "
                                + "para el criterio ingresado."
                );


                return "caja/index";
            }


            // =================================================
            // CALCULAR MONTO DE CADA CITA
            // =================================================

            Map<Long, BigDecimal> montos =
                    new LinkedHashMap<>();


            for (Cita cita : citas) {

                montos.put(
                        cita.getId(),
                        cajaService.obtenerMonto(
                                cita
                        )
                );
            }


            // =================================================
            // ENVIAR TODAS LAS CITAS A LA VISTA
            // =================================================

            model.addAttribute(
                    "citas",
                    citas
            );


            model.addAttribute(
                    "montos",
                    montos
            );


            model.addAttribute(
                    "cantidadCitas",
                    citas.size()
            );


            return "caja/index";
        }


        // =================================================
        // TIPO DE BÚSQUEDA INVÁLIDO
        // =================================================

        model.addAttribute(
                "mensajeError",
                "Tipo de búsqueda no válido."
        );


        return "caja/index";
    }


    // =====================================================
    // MOSTRAR PANTALLA DE COBRO
    // =====================================================

    @GetMapping("/cobro")
    public String mostrarCobro(

            @RequestParam Long citaId,

            HttpSession session,

            Model model
    ) {

        Usuario cajero =
                obtenerCajero(
                        session
                );


        if (cajero == null) {

            return "redirect:/personal/login";
        }


        Cita cita =
                cajaService
                        .buscarPendientePorNumeroCita(
                                citaId
                        )
                        .orElse(null);


        // =================================================
        // SOLO SE PUEDEN COBRAR CITAS PENDIENTES
        // =================================================

        if (cita == null) {

            return "redirect:/caja";
        }


        model.addAttribute(
                "cajero",
                cajero
        );


        model.addAttribute(
                "cita",
                cita
        );


        model.addAttribute(
                "monto",
                cajaService.obtenerMonto(
                        cita
                )
        );


        return "caja/cobro";
    }


    // =====================================================
    // PROCESAR PAGO
    // =====================================================

    @PostMapping("/procesar")
    public String procesarPago(

            @RequestParam Long citaId,

            @RequestParam String metodoPago,

            @RequestParam(
                    required = false
            )
            BigDecimal montoRecibido,

            @RequestParam(
                    required = false
            )
            String ultimosCuatro,

            HttpSession session,

            RedirectAttributes redirectAttributes
    ) {

        Usuario cajero =
                obtenerCajero(
                        session
                );


        if (cajero == null) {

            return "redirect:/personal/login";
        }


        String metodo =
                metodoPago == null
                        ? ""
                        : metodoPago
                        .trim()
                        .toUpperCase();


        try {

            Pago pago;


            // =================================================
            // EFECTIVO
            // =================================================

            if ("EFECTIVO".equals(
                    metodo
            )) {

                pago =
                        cajaService
                                .procesarPagoEfectivo(
                                        citaId,
                                        montoRecibido
                                );
            }


            // =================================================
            // TARJETAS
            // =================================================

            else if (
                    "VISA".equals(
                            metodo
                    )
                            ||
                            "MASTERCARD".equals(
                                    metodo
                            )
                            ||
                            "DEBITO".equals(
                                    metodo
                            )
            ) {

                pago =
                        cajaService
                                .procesarPagoTarjeta(
                                        citaId,
                                        metodo,
                                        ultimosCuatro
                                );
            }


            // =================================================
            // MÉTODO INVÁLIDO
            // =================================================

            else {

                throw new IllegalArgumentException(
                        "Debe seleccionar un método de pago válido."
                );
            }


            // =================================================
            // MENSAJE DE ÉXITO
            // =================================================

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "¡Pago registrado exitosamente! Paciente: "
                            + pago
                            .getCita()
                            .getPaciente()
                            .getNombreCompleto()
                            + ". La cita ha sido actualizada a estado Confirmada."
            );


            return "redirect:/caja/comprobante?pagoId="
                    + pago.getId();


        } catch (
                IllegalArgumentException
                |
                IllegalStateException e
        ) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    e.getMessage()
            );


            return "redirect:/caja/cobro?citaId="
                    + citaId;
        }
    }


    // =====================================================
    // MOSTRAR COMPROBANTE
    // =====================================================

    @GetMapping("/comprobante")
    public String mostrarComprobante(

            @RequestParam Long pagoId,

            HttpSession session,

            Model model
    ) {

        Usuario cajero =
                obtenerCajero(
                        session
                );


        if (cajero == null) {

            return "redirect:/personal/login";
        }


        Pago pago =
                cajaService.buscarPago(
                        pagoId
                );


        model.addAttribute(
                "cajero",
                cajero
        );


        model.addAttribute(
                "pago",
                pago
        );


        return "caja/comprobante";
    }
}