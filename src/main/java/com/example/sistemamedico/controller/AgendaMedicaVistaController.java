
package com.example.sistemamedico.controller;

import com.example.sistemamedico.model.Usuario;
import com.example.sistemamedico.service.AgendaMedicaSeguridadService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class AgendaMedicaVistaController {

    // =====================================================
    // CU-16: SERVICIO DE SEGURIDAD
    // =====================================================

    private final AgendaMedicaSeguridadService seguridadService;

    public AgendaMedicaVistaController(
            AgendaMedicaSeguridadService seguridadService
    ) {
        this.seguridadService = seguridadService;
    }

    // =====================================================
    // CU-16: MOSTRAR AGENDA MÉDICA
    // =====================================================

    @GetMapping("/medico/agenda")
    public String mostrarAgenda(
            HttpSession session,
            Model model
    ) {

        // ================================================
        // 1. COMPROBAR SI EXISTE SESIÓN
        // ================================================

        if (session.getAttribute("usuarioInterno") == null) {
            return "redirect:/personal/login";
        }

        // ================================================
        // 2. VALIDAR ROL MÉDICO Y PERMISO DE LECTURA
        // ================================================

        Usuario medico;

        try {

            medico = seguridadService.exigirPermiso(
                    session,
                    "read"
            );

        } catch (ResponseStatusException ex) {

            if (ex.getStatusCode().value()
                    == HttpStatus.UNAUTHORIZED.value()) {

                return "redirect:/personal/login";
            }

            throw ex;
        }

        // ================================================
        // 3. PREPARAR DATOS DEL MÉDICO
        // ================================================

        model.addAttribute(
                "medicoNombre",
                medico.getNombre() + " " + medico.getApellido()
        );

        model.addAttribute(
                "medicoId",
                medico.getId()
        );

        model.addAttribute(
                "usuarioActual",
                medico.getNombreUsuario()
        );

        // ================================================
        // 4. MOSTRAR CALENDARIO
        // ================================================

        return "medico/agenda";
    }
}
