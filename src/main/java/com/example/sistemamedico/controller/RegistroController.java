package com.example.sistemamedico.controller;

import com.example.sistemamedico.service.RegistroUsuarioService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class RegistroController {

    private final RegistroUsuarioService registroUsuarioService;


    public RegistroController(
            RegistroUsuarioService registroUsuarioService
    ) {

        this.registroUsuarioService =
                registroUsuarioService;
    }


    // ==========================================
    // MOSTRAR FORMULARIO DE REGISTRO
    // ==========================================

    @GetMapping("/registro")
    public String mostrarRegistro(

            @RequestParam(
                    value = "origen",
                    required = false
            )
            String origen,

            Model model
    ) {

        // Guardamos el origen en el formulario
        // para saber a dónde regresar después.

        model.addAttribute(
                "origen",
                origen
        );


        return "registro";
    }


    // ==========================================
    // REGISTRAR PACIENTE
    // ==========================================

    @PostMapping("/registro")
    public String registrarPaciente(

            @RequestParam
            Map<String, String> datos,

            @RequestParam(
                    value = "origen",
                    required = false
            )
            String origen,

            Model model,

            RedirectAttributes redirect
    ) {

        boolean registrado =
                registroUsuarioService.registrarPaciente(
                        datos,
                        model
                );


        // ==========================================
        // SI HUBO ERROR
        // ==========================================

        if (!registrado) {

            model.addAttribute(
                    "origen",
                    origen
            );


            return "registro";
        }


        // ==========================================
        // SI VIENE DESDE RECEPCIÓN
        // ==========================================

        if ("recepcion".equalsIgnoreCase(origen)) {

            redirect.addFlashAttribute(
                    "mensajeExito",
                    "Paciente registrado correctamente. "
                            + "Puede continuar con la atención en recepción."
            );


            return "redirect:/recepcion";
        }


        // ==========================================
        // REGISTRO NORMAL DEL PACIENTE
        // ==========================================

        redirect.addFlashAttribute(
                "registroExitoso",
                "¡Registro exitoso! Su cuenta ha sido creada. "
                        + "Ahora puede iniciar sesión con sus credenciales."
        );


        return "redirect:/login";
    }
}