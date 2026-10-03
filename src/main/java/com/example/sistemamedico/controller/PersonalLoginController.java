package com.example.sistemamedico.controller;

import com.example.sistemamedico.Repositorio.Usuario_Repositorio;
import com.example.sistemamedico.model.Usuario;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;


@Controller
public class PersonalLoginController {

    private final Usuario_Repositorio usuarioRepository;


    public PersonalLoginController(
            Usuario_Repositorio usuarioRepository
    ) {

        this.usuarioRepository =
                usuarioRepository;
    }


    // =====================================================
    // MOSTRAR LOGIN DEL PERSONAL INTERNO
    // CU-05 / CU-06 / CU-07 / CU-08 / CU-09
    // =====================================================

    @GetMapping("/personal/login")
    public String mostrarLoginPersonal() {

        return "personal/login";
    }


    // =====================================================
    // INICIAR SESIÓN DEL PERSONAL INTERNO
    // =====================================================

    @PostMapping("/personal/login")
    public String iniciarSesionPersonal(

            @RequestParam String usuario,

            @RequestParam String contrasena,

            Model model,

            HttpSession session
    ) {

        // =================================================
        // LIMPIAR USUARIO
        // =================================================

        String usuarioLimpio =
                usuario == null
                        ? ""
                        : usuario.trim();


        // =================================================
        // BUSCAR USUARIO
        // =================================================

        Optional<Usuario> encontrado =
                usuarioRepository
                        .findByNombreUsuarioIgnoreCase(
                                usuarioLimpio
                        );


        // =================================================
        // USUARIO NO EXISTE
        // =================================================

        if (encontrado.isEmpty()) {

            model.addAttribute(
                    "error",
                    "Usuario o contraseña incorrectos."
            );

            return "personal/login";
        }


        Usuario u =
                encontrado.get();


        // =================================================
        // CONTRASEÑA INCORRECTA
        // =================================================

        if (
                u.getContrasena() == null
                        ||
                        !u.getContrasena()
                                .equals(
                                        contrasena
                                )
        ) {

            model.addAttribute(
                    "error",
                    "Usuario o contraseña incorrectos."
            );

            return "personal/login";
        }


        // =================================================
        // CUENTA INACTIVA
        // =================================================

        if (
                u.getActivo() == null
                        ||
                        !u.getActivo()
        ) {

            model.addAttribute(
                    "error",
                    "La cuenta se encuentra inactiva."
            );

            return "personal/login";
        }


        // =================================================
        // VALIDAR QUE TENGA ROL
        // =================================================

        if (u.getRol() == null) {

            model.addAttribute(
                    "error",
                    "El usuario no tiene un rol asignado."
            );

            return "personal/login";
        }


        String rol =
                u.getRol()
                        .getNombre();


        if (
                rol == null
                        ||
                        rol.isBlank()
        ) {

            model.addAttribute(
                    "error",
                    "El usuario no tiene un rol válido."
            );

            return "personal/login";
        }


        // =================================================
        // GUARDAR USUARIO INTERNO EN SESIÓN
        // =================================================

        session.setAttribute(
                "usuarioInterno",
                u
        );


        // =================================================
        // RECEPCIONISTA
        // CU-05
        // =================================================

        if (
                "RECEPCIONISTA".equalsIgnoreCase(
                        rol
                )
        ) {

            return "redirect:/recepcion";
        }


        // =================================================
        // CAJERO
        // CU-06
        // =================================================

        if (
                "CAJERO".equalsIgnoreCase(
                        rol
                )
        ) {

            return "redirect:/caja";
        }


        // =================================================
        // ENFERMERO
        // CU-07
        // =================================================

        if (
                "ENFERMERO".equalsIgnoreCase(
                        rol
                )
        ) {

            return "redirect:/enfermeria";
        }


        // =================================================
        // MEDICO
        // CU-08
        // =================================================

        if (
                "MEDICO".equalsIgnoreCase(
                        rol
                )
        ) {

            return "redirect:/medico";
        }


        // =================================================
        // LABORATORIO
        // CU-09
        // =================================================

        if (
                "LABORATORIO".equalsIgnoreCase(
                        rol
                )
        ) {

            return "redirect:/laboratorio";
        }


        // =================================================
        // ROL NO AUTORIZADO
        // =================================================

        session.removeAttribute(
                "usuarioInterno"
        );


        model.addAttribute(
                "error",
                "No tiene permisos para acceder a este módulo."
        );


        return "personal/login";
    }


    // =====================================================
    // CERRAR SESIÓN DEL PERSONAL INTERNO
    // =====================================================

    @GetMapping("/personal/cerrar-sesion")
    public String cerrarSesionPersonal(
            HttpSession session
    ) {

        session.removeAttribute(
                "usuarioInterno"
        );


        return "redirect:/personal/login";
    }
}