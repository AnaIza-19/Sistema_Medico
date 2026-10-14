
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

        this.usuarioRepository = usuarioRepository;
    }


    // =====================================================
    // MOSTRAR LOGIN DEL PERSONAL HOSPITALARIO
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
        // LIMPIAR NOMBRE DE USUARIO
        // =================================================

        String usuarioLimpio =
                usuario == null
                        ? ""
                        : usuario.trim();


        // =================================================
        // BUSCAR USUARIO EN POSTGRESQL
        // =================================================

        Optional<Usuario> encontrado =
                usuarioRepository
                        .findByNombreUsuarioIgnoreCase(
                                usuarioLimpio
                        );


        // =================================================
        // VALIDAR USUARIO EXISTENTE
        // =================================================

        if (encontrado.isEmpty()) {

            model.addAttribute(
                    "error",
                    "Usuario o contraseña incorrectos."
            );

            return "personal/login";
        }


        Usuario u = encontrado.get();


        // =================================================
        // VALIDAR CONTRASEÑA
        // SE CONSERVA EL MECANISMO ACTUAL DEL PROYECTO
        // =================================================

        if (
                u.getContrasena() == null
                        ||
                        !u.getContrasena().equals(contrasena)
        ) {

            model.addAttribute(
                    "error",
                    "Usuario o contraseña incorrectos."
            );

            return "personal/login";
        }


        // =================================================
        // VALIDAR CUENTA ACTIVA
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
        // VALIDAR ROL ASIGNADO
        // =================================================

        if (u.getRol() == null) {

            model.addAttribute(
                    "error",
                    "El usuario no tiene un rol asignado."
            );

            return "personal/login";
        }


        String rol = u.getRol().getNombre();


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
        // DEFINIR DESTINO SEGÚN EL ROL
        // =================================================

        String destino;


        // =================================================
        // RECEPCIONISTA - CU-05
        // =================================================

        if ("RECEPCIONISTA".equalsIgnoreCase(rol)) {

            destino = "/recepcion";
        }


        // =================================================
        // CAJERO - CU-06
        // =================================================

        else if ("CAJERO".equalsIgnoreCase(rol)) {

            destino = "/caja";
        }


        // =================================================
        // ENFERMERO - CU-07
        // =================================================

        else if ("ENFERMERO".equalsIgnoreCase(rol)) {

            destino = "/enfermeria";
        }


        // =================================================
        // MÉDICO - CU-08
        // =================================================

        else if ("MEDICO".equalsIgnoreCase(rol)) {

            destino = "/medico";
        }


        // =================================================
        // LABORATORIO - CU-09
        // CORRECCIÓN AGREGADA
        // =================================================

        else if ("LABORATORIO".equalsIgnoreCase(rol)) {

            destino = "/laboratorio";
        }


        // =================================================
        // FARMACIA
        // CORRECCIÓN AGREGADA
        // =================================================

        else if ("FARMACIA".equalsIgnoreCase(rol)) {

            destino = "/farmacia";
        }


        // =================================================
        // ROL NO AUTORIZADO
        // =================================================

        else {

            session.removeAttribute("usuarioInterno");

            model.addAttribute(
                    "error",
                    "No tiene permisos para acceder a este módulo."
            );

            return "personal/login";
        }


        // =================================================
        // INICIAR SESIÓN AUTORIZADA
        // =================================================

        session.setAttribute(
                "usuarioInterno",
                u
        );


        // =================================================
        // REDIRIGIR AL MÓDULO CORRESPONDIENTE
        // =================================================

        return "redirect:" + destino;
    }


    // =====================================================
    // CERRAR SESIÓN DEL PERSONAL HOSPITALARIO
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
