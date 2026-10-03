package com.example.sistemamedico.controller;

import com.example.sistemamedico.model.Cita;
import com.example.sistemamedico.model.Usuario;
import com.example.sistemamedico.service.MisCitasService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/citas")
public class MisCitasController {

    private final MisCitasService misCitasService;


    public MisCitasController(
            MisCitasService misCitasService
    ) {

        this.misCitasService = misCitasService;
    }


    // =====================================================
    // MOSTRAR MIS CITAS
    // =====================================================

    @GetMapping("/mis-citas")
    public String mostrarMisCitas(
            HttpSession session,
            Model model
    ) {

        // =================================================
        // VERIFICAR SESIÓN DEL PACIENTE
        // =================================================

        Usuario paciente =
                (Usuario) session.getAttribute(
                        "usuarioPaciente"
                );


        if (paciente == null) {

            return "redirect:/login";
        }


        // =================================================
        // CONSULTAR ÚNICAMENTE CITAS DEL PACIENTE
        // =================================================

        List<Cita> citas =
                misCitasService
                        .obtenerCitasPaciente(
                                paciente.getId()
                        );


        model.addAttribute(
                "paciente",
                paciente
        );


        model.addAttribute(
                "citas",
                citas
        );


        return "citas/mis-citas";
    }
}
