package com.example.sistemamedico.controller;

import com.example.sistemamedico.model.Usuario;
import com.example.sistemamedico.Repositorio.Usuario_Repositorio;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class AdminLoginController {

    private final Usuario_Repositorio usuarioRepository;

    public AdminLoginController(
            Usuario_Repositorio usuarioRepository
    ) {
        this.usuarioRepository = usuarioRepository;
    }


    // ==========================================
    // MOSTRAR LOGIN ADMINISTRATIVO
    // ==========================================

    @GetMapping("/admin/login")
    public String mostrarLogin() {
        return "admin/login";
    }


    // ==========================================
    // INICIAR SESIÓN ADMINISTRATIVA
    // ==========================================

    @PostMapping("/admin/login")
    public String iniciarSesion(

            @RequestParam String usuario,
            @RequestParam String contrasena,

            Model model,
            HttpSession session
    ) {

        String usuarioLimpio =
                usuario.trim();


        Optional<Usuario> encontrado =
                usuarioRepository
                        .findByNombreUsuarioIgnoreCase(
                                usuarioLimpio
                        );


        if (encontrado.isEmpty()) {

            model.addAttribute(
                    "error",
                    "Usuario o contraseña incorrectos."
            );

            return "admin/login";
        }


        Usuario u =
                encontrado.get();


        if (!u.getContrasena().equals(contrasena)) {

            model.addAttribute(
                    "error",
                    "Usuario o contraseña incorrectos."
            );

            return "admin/login";
        }


        if (u.getActivo() == null ||
                !u.getActivo()) {

            model.addAttribute(
                    "error",
                    "La cuenta se encuentra inactiva."
            );

            return "admin/login";
        }


        if (u.getRol() == null ||
                !"ADMINISTRADOR".equalsIgnoreCase(
                        u.getRol().getNombre()
                )) {

            model.addAttribute(
                    "error",
                    "No tiene permisos de administración."
            );

            return "admin/login";
        }


        // Guardar administrador en sesión
        session.setAttribute(
                "usuarioAdmin",
                u
        );


        return "redirect:/admin/usuarios";
    }


    // ==========================================
    // CERRAR SESIÓN ADMINISTRATIVA
    // ==========================================

    @GetMapping("/admin/cerrar-sesion")
    public String cerrarSesion(
            HttpSession session
    ) {

        session.removeAttribute(
                "usuarioAdmin"
        );

        return "redirect:/admin/login";
    }
}
