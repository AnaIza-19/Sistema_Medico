package com.example.sistemamedico.controller;

import com.example.sistemamedico.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(
            HttpSession session,
            Model model
    ) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuarioPaciente");

        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute(
                "nombre",
                usuario.getNombre()
        );

        return "dashboard";
    }
}