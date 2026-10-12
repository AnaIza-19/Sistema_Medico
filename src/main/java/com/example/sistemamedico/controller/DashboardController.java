package com.example.sistemamedico.controller;

import com.example.sistemamedico.Repositorio.DespachoMedicamentoDetalle_Repositorio;
import com.example.sistemamedico.Repositorio.DespachoMedicamento_Repositorio;

import com.example.sistemamedico.model.DespachoMedicamento;
import com.example.sistemamedico.model.DespachoMedicamentoDetalle;
import com.example.sistemamedico.model.Usuario;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Controller
public class DashboardController {


    // =====================================================
    // REPOSITORIOS
    // =====================================================

    private final DespachoMedicamento_Repositorio despachoRepositorio;

    private final DespachoMedicamentoDetalle_Repositorio despachoDetalleRepositorio;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public DashboardController(
            DespachoMedicamento_Repositorio despachoRepositorio,
            DespachoMedicamentoDetalle_Repositorio despachoDetalleRepositorio
    ) {

        this.despachoRepositorio =
                despachoRepositorio;

        this.despachoDetalleRepositorio =
                despachoDetalleRepositorio;
    }


    // =====================================================
    // PANEL PRINCIPAL DEL PACIENTE
    // =====================================================

    @GetMapping("/dashboard")
    public String dashboard(
            HttpSession session,
            Model model
    ) {

        Usuario usuario =
                obtenerPaciente(
                        session
                );


        if (usuario == null) {

            return "redirect:/login";
        }


        model.addAttribute(
                "nombre",
                usuario.getNombre()
        );


        return "dashboard";
    }


    // =====================================================
    // CU-11
    // HISTORIAL DE MEDICAMENTOS DEL PACIENTE
    // =====================================================

    @GetMapping("/paciente/historial-medicamentos")
    public String historialMedicamentos(
            HttpSession session,
            Model model
    ) {

        Usuario paciente =
                obtenerPaciente(
                        session
                );


        if (paciente == null) {

            return "redirect:/login";
        }


        // =================================================
        // BUSCAR SOLO DESPACHOS CONFIRMADOS
        // =================================================

        List<DespachoMedicamento> despachos =
                despachoRepositorio
                        .buscarHistorialPaciente(
                                paciente.getId()
                        );


        // =================================================
        // MAPA:
        // ID DESPACHO -> MEDICAMENTOS DESPACHADOS
        // =================================================

        Map<Long, List<DespachoMedicamentoDetalle>>
                detallesPorDespacho =
                new LinkedHashMap<>();


        // Crear primero las posiciones
        // manteniendo el orden de los despachos

        for (
                DespachoMedicamento despacho :
                despachos
        ) {

            detallesPorDespacho.put(
                    despacho.getId(),
                    new ArrayList<>()
            );
        }


        // =================================================
        // BUSCAR DETALLES
        // =================================================

        if (!despachos.isEmpty()) {


            List<Long> despachoIds =
                    despachos
                            .stream()
                            .map(
                                    DespachoMedicamento::getId
                            )
                            .toList();


            List<DespachoMedicamentoDetalle> detalles =
                    despachoDetalleRepositorio
                            .findByDespachoIdInOrderByDespachoFechaDespachoDescIdAsc(
                                    despachoIds
                            );


            // =============================================
            // AGRUPAR DETALLES POR DESPACHO
            // =============================================

            for (
                    DespachoMedicamentoDetalle detalle :
                    detalles
            ) {

                Long despachoId =
                        detalle
                                .getDespacho()
                                .getId();


                detallesPorDespacho
                        .computeIfAbsent(
                                despachoId,
                                id -> new ArrayList<>()
                        )
                        .add(
                                detalle
                        );
            }
        }


        // =================================================
        // ENVIAR INFORMACIÓN A LA VISTA
        // =================================================

        model.addAttribute(
                "paciente",
                paciente
        );


        model.addAttribute(
                "despachos",
                despachos
        );


        model.addAttribute(
                "detallesPorDespacho",
                detallesPorDespacho
        );


        return "paciente/historial-medicamentos";
    }


    // =====================================================
    // OBTENER PACIENTE DE LA SESIÓN
    // =====================================================

    private Usuario obtenerPaciente(
            HttpSession session
    ) {

        Object usuarioSesion =
                session.getAttribute(
                        "usuarioPaciente"
                );


        if (!(usuarioSesion instanceof Usuario)) {

            return null;
        }


        return (Usuario) usuarioSesion;
    }
}