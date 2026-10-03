package com.example.sistemamedico.controller;

import com.example.sistemamedico.model.Usuario;
import com.example.sistemamedico.Repositorio.Usuario_Repositorio;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Controller
public class LoginController {

    private final Usuario_Repositorio usuarioRepository;


    public LoginController(
            Usuario_Repositorio usuarioRepository
    ) {

        this.usuarioRepository =
                usuarioRepository;
    }


    // =====================================================
    // MOSTRAR LOGIN
    // =====================================================

    @GetMapping("/login")
    public String mostrarLogin() {

        return "login";
    }


    // =====================================================
    // LOGIN DE PACIENTE
    // =====================================================

    @PostMapping("/login-paciente")
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


        // =====================================================
        // USUARIO NO ENCONTRADO
        // =====================================================

        if (encontrado.isEmpty()) {

            model.addAttribute(
                    "error",
                    "Usuario o contraseña incorrectos."
            );

            return "login";
        }


        Usuario u =
                encontrado.get();


        // =====================================================
        // CUENTA BLOQUEADA
        // =====================================================

        if (
                u.getBloqueadoHasta() != null
                        &&
                        LocalDateTime.now()
                                .isBefore(
                                        u.getBloqueadoHasta()
                                )
        ) {

            long minutos =
                    ChronoUnit.MINUTES.between(
                            LocalDateTime.now(),
                            u.getBloqueadoHasta()
                    );


            model.addAttribute(
                    "error",
                    "Cuenta bloqueada temporalmente. "
                            + "Intente de nuevo en "
                            + (minutos + 1)
                            + " minutos."
            );


            model.addAttribute(
                    "bloqueado",
                    true
            );


            return "login";
        }


        // =====================================================
        // DESBLOQUEO AUTOMÁTICO
        // =====================================================

        if (
                u.getBloqueadoHasta() != null
                        &&
                        !LocalDateTime.now()
                                .isBefore(
                                        u.getBloqueadoHasta()
                                )
        ) {

            u.setIntentosFallidos(
                    0
            );

            u.setBloqueadoHasta(
                    null
            );


            usuarioRepository.save(
                    u
            );
        }


        // =====================================================
        // CONTRASEÑA INCORRECTA
        // =====================================================

        if (
                !u.getContrasena()
                        .equals(
                                contrasena
                        )
        ) {

            int intentosActuales =
                    u.getIntentosFallidos() == null
                            ? 0
                            : u.getIntentosFallidos();


            int intentos =
                    intentosActuales + 1;


            u.setIntentosFallidos(
                    intentos
            );


            // =================================================
            // BLOQUEO DESPUÉS DE 5 INTENTOS
            // =================================================

            if (intentos >= 5) {

                u.setBloqueadoHasta(
                        LocalDateTime.now()
                                .plusMinutes(
                                        15
                                )
                );


                usuarioRepository.save(
                        u
                );


                model.addAttribute(
                        "error",
                        "Cuenta bloqueada temporalmente. "
                                + "Intente de nuevo en 15 minutos."
                );


                model.addAttribute(
                        "bloqueado",
                        true
                );


                return "login";
            }


            usuarioRepository.save(
                    u
            );


            int restantes =
                    5 - intentos;


            model.addAttribute(
                    "error",
                    "Usuario o contraseña incorrectos. "
                            + "Intentos restantes: "
                            + restantes
            );


            return "login";
        }


        // =====================================================
        // CUENTA INACTIVA
        // =====================================================

        if (
                u.getActivo() == null
                        ||
                        !u.getActivo()
        ) {

            model.addAttribute(
                    "error",
                    "La cuenta se encuentra inactiva."
            );


            return "login";
        }


        // =====================================================
        // SOLO PACIENTES
        // =====================================================

        if (
                u.getRol() == null
                        ||
                        !"PACIENTE".equalsIgnoreCase(
                                u.getRol()
                                        .getNombre()
                        )
        ) {

            model.addAttribute(
                    "error",
                    "Este acceso es exclusivo para pacientes. "
                            + "Si es personal del hospital, "
                            + "use el panel administrativo."
            );


            model.addAttribute(
                    "usuarioInterno",
                    true
            );


            return "login";
        }


        // =====================================================
        // LOGIN CORRECTO
        // =====================================================

        u.setIntentosFallidos(
                0
        );

        u.setBloqueadoHasta(
                null
        );


        usuarioRepository.save(
                u
        );


        // =====================================================
        // GUARDAR PACIENTE EN SESIÓN
        // IMPORTANTE PARA CU-03
        // =====================================================

        session.setAttribute(
                "usuarioPaciente",
                u
        );


        return "redirect:/dashboard";
    }


    // =====================================================
    // CERRAR SESIÓN
    // =====================================================

    @GetMapping("/cerrar-sesion")
    public String cerrarSesion(
            HttpSession session
    ) {

        session.invalidate();


        return "redirect:/";
    }
}