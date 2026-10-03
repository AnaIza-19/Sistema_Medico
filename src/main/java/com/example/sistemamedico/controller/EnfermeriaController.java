
package com.example.sistemamedico.controller;

import com.example.sistemamedico.Repositorio.Cita_Repositorio;
import com.example.sistemamedico.Repositorio.SignoVital_Repositorio;

import com.example.sistemamedico.model.Cita;
import com.example.sistemamedico.model.SignoVital;
import com.example.sistemamedico.model.Usuario;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Controller
public class EnfermeriaController {

    private final Cita_Repositorio citaRepository;

    private final SignoVital_Repositorio signoVitalRepository;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public EnfermeriaController(

            Cita_Repositorio citaRepository,

            SignoVital_Repositorio signoVitalRepository
    ) {

        this.citaRepository =
                citaRepository;

        this.signoVitalRepository =
                signoVitalRepository;
    }


    // =====================================================
    // PANEL PRINCIPAL DE ENFERMERÍA
    // CU-07
    // =====================================================

    @GetMapping("/enfermeria")
    public String panelEnfermeria(

            HttpSession session,

            Model model
    ) {

        Usuario enfermero =
                obtenerEnfermeroSesion(
                        session
                );


        if (enfermero == null) {

            return "redirect:/personal/login";
        }


        model.addAttribute(
                "enfermero",
                enfermero
        );


        List<Cita> pacientesPresentes;

        List<Cita> pacientesEnSignos;


        // =================================================
        // FILTRAR POR SUCURSAL
        // =================================================

        if (enfermero.getSucursal() != null) {

            Long sucursalId =
                    enfermero
                            .getSucursal()
                            .getId();


            pacientesPresentes =
                    citaRepository
                            .findByEstadoIgnoreCaseAndSucursalIdOrderByHoraLlegadaAsc(
                                    "PACIENTE_PRESENTE",
                                    sucursalId
                            );


            pacientesEnSignos =
                    citaRepository
                            .findByEstadoIgnoreCaseAndSucursalIdOrderByHoraLlegadaAsc(
                                    "SIGNOS_VITALES",
                                    sucursalId
                            );

        } else {

            pacientesPresentes =
                    Collections.emptyList();


            pacientesEnSignos =
                    Collections.emptyList();
        }


        model.addAttribute(
                "pacientesPresentes",
                pacientesPresentes
        );


        model.addAttribute(
                "pacientesEnSignos",
                pacientesEnSignos
        );


        return "enfermeria/index";
    }


    // =====================================================
    // LLAMAR PACIENTE
    // PACIENTE_PRESENTE -> SIGNOS_VITALES
    // =====================================================

    @PostMapping("/enfermeria/llamar")
    public String llamarPaciente(

            @RequestParam("citaId")
            Long citaId,

            HttpSession session,

            RedirectAttributes redirectAttributes
    ) {

        Usuario enfermero =
                obtenerEnfermeroSesion(
                        session
                );


        if (enfermero == null) {

            return "redirect:/personal/login";
        }


        Optional<Cita> citaOptional =
                citaRepository
                        .findById(
                                citaId
                        );


        if (citaOptional.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se encontró la cita seleccionada."
            );


            return "redirect:/enfermeria";
        }


        Cita cita =
                citaOptional.get();


        // =================================================
        // VALIDAR ESTADO
        // =================================================

        if (
                cita.getEstado() == null
                        ||
                        !"PACIENTE_PRESENTE"
                                .equalsIgnoreCase(
                                        cita.getEstado()
                                )
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La cita ya no se encuentra disponible para toma de signos vitales."
            );


            return "redirect:/enfermeria";
        }


        // =================================================
        // VALIDAR SUCURSAL
        // =================================================

        if (
                enfermero.getSucursal() == null
                        ||
                        cita.getSucursal() == null
                        ||
                        !enfermero
                                .getSucursal()
                                .getId()
                                .equals(
                                        cita
                                                .getSucursal()
                                                .getId()
                                )
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No tiene permiso para atender esta cita."
            );


            return "redirect:/enfermeria";
        }


        // =================================================
        // CAMBIAR ESTADO
        // =================================================

        cita.setEstado(
                "SIGNOS_VITALES"
        );


        citaRepository.save(
                cita
        );


        // =================================================
        // GENERAR ANUNCIO TTS
        // =================================================

        String nombrePaciente =
                cita
                        .getPaciente()
                        .getNombreCompleto();


        String anuncio =
                "Turno número "
                        +
                        cita.getId()
                        +
                        ". Paciente "
                        +
                        nombrePaciente
                        +
                        ", favor pasar a toma de signos vitales.";


        redirectAttributes.addFlashAttribute(
                "anuncioTts",
                anuncio
        );


        redirectAttributes.addFlashAttribute(
                "exito",
                "Paciente llamado correctamente. "
                        +
                        "La cita pasó a estado SIGNOS_VITALES."
        );


        return "redirect:/enfermeria";
    }


    // =====================================================
    // MOSTRAR FORMULARIO DE SIGNOS VITALES
    // =====================================================

    @GetMapping("/enfermeria/signos/{citaId}")
    public String mostrarFormularioSignos(

            @PathVariable
            Long citaId,

            HttpSession session,

            Model model,

            RedirectAttributes redirectAttributes
    ) {

        Usuario enfermero =
                obtenerEnfermeroSesion(
                        session
                );


        if (enfermero == null) {

            return "redirect:/personal/login";
        }


        Optional<Cita> citaOptional =
                citaRepository
                        .findById(
                                citaId
                        );


        if (citaOptional.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se encontró la cita seleccionada."
            );


            return "redirect:/enfermeria";
        }


        Cita cita =
                citaOptional.get();


        // =================================================
        // VALIDAR ESTADO
        // =================================================

        if (
                cita.getEstado() == null
                        ||
                        !"SIGNOS_VITALES"
                                .equalsIgnoreCase(
                                        cita.getEstado()
                                )
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La cita no se encuentra en proceso de toma de signos vitales."
            );


            return "redirect:/enfermeria";
        }


        // =================================================
        // VALIDAR SUCURSAL
        // =================================================

        if (
                enfermero.getSucursal() == null
                        ||
                        cita.getSucursal() == null
                        ||
                        !enfermero
                                .getSucursal()
                                .getId()
                                .equals(
                                        cita
                                                .getSucursal()
                                                .getId()
                                )
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No tiene permiso para registrar signos vitales de esta cita."
            );


            return "redirect:/enfermeria";
        }


        // =================================================
        // EVITAR DUPLICADOS
        // =================================================

        if (
                signoVitalRepository
                        .existsByCitaId(
                                citaId
                        )
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Los signos vitales de esta cita ya fueron registrados."
            );


            return "redirect:/enfermeria";
        }


        model.addAttribute(
                "cita",
                cita
        );


        model.addAttribute(
                "enfermero",
                enfermero
        );


        return "enfermeria/signos";
    }


    // =====================================================
    // REGISTRAR SIGNOS VITALES
    // CU-07
    // =====================================================

    @PostMapping("/enfermeria/signos/{citaId}")
    public String registrarSignosVitales(

            @PathVariable
            Long citaId,

            @RequestParam
            Integer presionSistolica,

            @RequestParam
            Integer presionDiastolica,

            @RequestParam
            BigDecimal temperatura,

            @RequestParam
            BigDecimal peso,

            @RequestParam
            BigDecimal talla,

            @RequestParam
            Integer frecuenciaCardiaca,

            @RequestParam(
                    required = false,
                    defaultValue = "false"
            )
            Boolean esEmergencia,

            HttpSession session,

            RedirectAttributes redirectAttributes
    ) {

        Usuario enfermero =
                obtenerEnfermeroSesion(
                        session
                );


        if (enfermero == null) {

            return "redirect:/personal/login";
        }


        Optional<Cita> citaOptional =
                citaRepository
                        .findById(
                                citaId
                        );


        if (citaOptional.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se encontró la cita seleccionada."
            );


            return "redirect:/enfermeria";
        }


        Cita cita =
                citaOptional.get();


        // =================================================
        // VALIDAR ESTADO
        // =================================================

        if (
                cita.getEstado() == null
                        ||
                        !"SIGNOS_VITALES"
                                .equalsIgnoreCase(
                                        cita.getEstado()
                                )
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La cita no está en proceso de toma de signos vitales."
            );


            return "redirect:/enfermeria";
        }


        // =================================================
        // VALIDAR SUCURSAL
        // =================================================

        if (
                enfermero.getSucursal() == null
                        ||
                        cita.getSucursal() == null
                        ||
                        !enfermero
                                .getSucursal()
                                .getId()
                                .equals(
                                        cita
                                                .getSucursal()
                                                .getId()
                                )
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No tiene permiso para registrar signos vitales de esta cita."
            );


            return "redirect:/enfermeria";
        }


        // =================================================
        // EVITAR DUPLICADO
        // =================================================

        if (
                signoVitalRepository
                        .existsByCitaId(
                                citaId
                        )
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Los signos vitales de esta cita ya fueron registrados."
            );


            return "redirect:/enfermeria";
        }


        // =================================================
        // FA02 - PRESIÓN SISTÓLICA
        // =================================================

        if (
                presionSistolica == null
                        ||
                        presionSistolica < 60
                        ||
                        presionSistolica > 250
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La presión sistólica debe estar entre 60 y 250 mmHg."
            );


            return "redirect:/enfermeria/signos/" + citaId;
        }


        // =================================================
        // FA02 - PRESIÓN DIASTÓLICA
        // =================================================

        if (
                presionDiastolica == null
                        ||
                        presionDiastolica < 40
                        ||
                        presionDiastolica > 150
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La presión diastólica debe estar entre 40 y 150 mmHg."
            );


            return "redirect:/enfermeria/signos/" + citaId;
        }


        // =================================================
        // FA02 - TEMPERATURA
        // =================================================

        if (
                temperatura == null
                        ||
                        temperatura.compareTo(
                                new BigDecimal(
                                        "34"
                                )
                        ) < 0
                        ||
                        temperatura.compareTo(
                                new BigDecimal(
                                        "42"
                                )
                        ) > 0
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La temperatura debe estar entre 34 y 42 °C."
            );


            return "redirect:/enfermeria/signos/" + citaId;
        }


        // =================================================
        // FA02 - PESO
        // =================================================

        if (
                peso == null
                        ||
                        peso.compareTo(
                                new BigDecimal(
                                        "0.5"
                                )
                        ) < 0
                        ||
                        peso.compareTo(
                                new BigDecimal(
                                        "300"
                                )
                        ) > 0
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El peso debe estar entre 0.5 y 300 kg."
            );


            return "redirect:/enfermeria/signos/" + citaId;
        }


        // =================================================
        // FA02 - TALLA
        // =================================================

        if (
                talla == null
                        ||
                        talla.compareTo(
                                new BigDecimal(
                                        "30"
                                )
                        ) < 0
                        ||
                        talla.compareTo(
                                new BigDecimal(
                                        "250"
                                )
                        ) > 0
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La talla debe estar entre 30 y 250 cm."
            );


            return "redirect:/enfermeria/signos/" + citaId;
        }


        // =================================================
        // FA02 - FRECUENCIA CARDÍACA
        // =================================================

        if (
                frecuenciaCardiaca == null
                        ||
                        frecuenciaCardiaca < 30
                        ||
                        frecuenciaCardiaca > 220
        ) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La frecuencia cardíaca debe estar entre 30 y 220 lpm."
            );


            return "redirect:/enfermeria/signos/" + citaId;
        }


        // =================================================
        // FA03 - ALERTAS CLÍNICAS
        // =================================================

        boolean tieneAlerta =
                false;


        StringBuilder detalleAlerta =
                new StringBuilder();


        if (
                presionSistolica < 90
                        ||
                        presionSistolica > 140
        ) {

            tieneAlerta =
                    true;


            detalleAlerta.append(
                    "Presión sistólica fuera del rango clínico normal. "
            );
        }


        if (
                presionDiastolica < 60
                        ||
                        presionDiastolica > 90
        ) {

            tieneAlerta =
                    true;


            detalleAlerta.append(
                    "Presión diastólica fuera del rango clínico normal. "
            );
        }


        if (
                temperatura.compareTo(
                        new BigDecimal(
                                "36"
                        )
                ) < 0
                        ||
                        temperatura.compareTo(
                                new BigDecimal(
                                        "37.5"
                                )
                        ) > 0
        ) {

            tieneAlerta =
                    true;


            detalleAlerta.append(
                    "Temperatura fuera del rango clínico normal. "
            );
        }


        if (
                frecuenciaCardiaca < 60
                        ||
                        frecuenciaCardiaca > 100
        ) {

            tieneAlerta =
                    true;


            detalleAlerta.append(
                    "Frecuencia cardíaca fuera del rango clínico normal. "
            );
        }


        // =================================================
        // CREAR REGISTRO
        // =================================================

        SignoVital signoVital =
                new SignoVital();


        signoVital.setCita(
                cita
        );


        signoVital.setEnfermero(
                enfermero
        );


        signoVital.setPresionSistolica(
                presionSistolica
        );


        signoVital.setPresionDiastolica(
                presionDiastolica
        );


        signoVital.setTemperatura(
                temperatura
        );


        signoVital.setPeso(
                peso
        );


        signoVital.setTalla(
                talla
        );


        signoVital.setFrecuenciaCardiaca(
                frecuenciaCardiaca
        );


        signoVital.setEsEmergencia(
                Boolean.TRUE.equals(
                        esEmergencia
                )
        );


        signoVital.setTieneAlertaClinica(
                tieneAlerta
        );


        signoVital.setDetalleAlerta(
                tieneAlerta
                        ?
                        detalleAlerta
                                .toString()
                                .trim()
                        :
                        null
        );


        // =================================================
        // GUARDAR SIGNOS VITALES
        // =================================================

        signoVitalRepository.save(
                signoVital
        );


        // =================================================
        // CAMBIAR ESTADO FINAL
        //
        // SIGNOS_VITALES -> LISTO_CONSULTA
        // =================================================

        cita.setEstado(
                "LISTO_CONSULTA"
        );


        citaRepository.save(
                cita
        );


        // =================================================
        // MENSAJE FINAL - EMERGENCIA
        // =================================================

        if (
                Boolean.TRUE.equals(
                        esEmergencia
                )
        ) {

            redirectAttributes.addFlashAttribute(
                    "exito",
                    "Signos vitales de emergencia registrados para paciente "
                            +
                            cita
                                    .getPaciente()
                                    .getNombreCompleto()
                            +
                            ". El paciente debe pasar directamente a consulta médica."
            );

        } else {

            // =================================================
            // MENSAJE FLUJO NORMAL
            // =================================================

            redirectAttributes.addFlashAttribute(
                    "exito",
                    "Signos vitales del paciente "
                            +
                            cita
                                    .getPaciente()
                                    .getNombreCompleto()
                            +
                            " registrados correctamente. "
                            +
                            "El paciente puede regresar a la sala de espera."
            );
        }


        return "redirect:/enfermeria";
    }


    // =====================================================
    // VALIDAR SESIÓN DE ENFERMERÍA
    // =====================================================

    private Usuario obtenerEnfermeroSesion(
            HttpSession session
    ) {

        Usuario usuario =
                (Usuario)
                        session.getAttribute(
                                "usuarioInterno"
                        );


        if (usuario == null) {

            return null;
        }


        if (
                usuario.getRol() == null
                        ||
                        usuario
                                .getRol()
                                .getNombre() == null
        ) {

            return null;
        }


        if (
                !"ENFERMERO"
                        .equalsIgnoreCase(
                                usuario
                                        .getRol()
                                        .getNombre()
                        )
        ) {

            return null;
        }


        return usuario;
    }
}