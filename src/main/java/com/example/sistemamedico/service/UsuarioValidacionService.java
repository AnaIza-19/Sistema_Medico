package com.example.sistemamedico.service;

import com.example.sistemamedico.model.Usuario;
import com.example.sistemamedico.Repositorio.Usuario_Repositorio;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class UsuarioValidacionService {

    private final Usuario_Repositorio usuarioRepository;

    public UsuarioValidacionService(
            Usuario_Repositorio usuarioRepository
    ) {
        this.usuarioRepository = usuarioRepository;
    }

    public boolean validarNuevo(
            String nombre,
            String apellido,
            String correo,
            String nombreUsuario,
            String contrasena,
            String dpi,
            Long rolId,
            Model model
    ) {

        boolean error = false;

        if (nombre.isBlank()) {
            model.addAttribute(
                    "errorNombre",
                    "El nombre es obligatorio."
            );
            error = true;
        }

        if (apellido.isBlank()) {
            model.addAttribute(
                    "errorApellido",
                    "El apellido es obligatorio."
            );
            error = true;
        }

        if (!correoValido(correo)) {
            model.addAttribute(
                    "errorCorreo",
                    "Ingrese un correo electrónico válido."
            );
            error = true;

        } else if (
                usuarioRepository
                        .existsByCorreoIgnoreCase(correo)
        ) {

            model.addAttribute(
                    "errorCorreo",
                    "El correo electrónico ya se encuentra registrado."
            );
            error = true;
        }

        if (nombreUsuario.isBlank()) {
            model.addAttribute(
                    "errorNombreUsuario",
                    "El nombre de usuario es obligatorio."
            );
            error = true;

        } else if (
                usuarioRepository
                        .existsByNombreUsuarioIgnoreCase(
                                nombreUsuario
                        )
        ) {

            model.addAttribute(
                    "errorNombreUsuario",
                    "El nombre de usuario ya se encuentra registrado."
            );
            error = true;
        }

        if (contrasena.isBlank()) {
            model.addAttribute(
                    "errorContrasena",
                    "La contraseña es obligatoria."
            );
            error = true;
        }

        if (!dpiValido(dpi)) {
            model.addAttribute(
                    "errorDpi",
                    "El DPI debe contener exactamente 13 dígitos."
            );
            error = true;

        } else if (
                dpi != null
                        && usuarioRepository.existsByDpi(dpi)
        ) {

            model.addAttribute(
                    "errorDpi",
                    "El DPI ya se encuentra registrado."
            );
            error = true;
        }

        if (rolId == null) {
            model.addAttribute(
                    "errorRol",
                    "Debe seleccionar un rol válido."
            );
            error = true;
        }

        return error;
    }

    public boolean validarEdicion(
            Usuario actual,
            String nombre,
            String apellido,
            String correo,
            String nombreUsuario,
            String dpi,
            Long rolId,
            Model model
    ) {

        boolean error = false;

        if (nombre.isBlank()) {
            model.addAttribute(
                    "errorNombre",
                    "El nombre es obligatorio."
            );
            error = true;
        }

        if (apellido.isBlank()) {
            model.addAttribute(
                    "errorApellido",
                    "El apellido es obligatorio."
            );
            error = true;
        }

        if (!correoValido(correo)) {
            model.addAttribute(
                    "errorCorreo",
                    "Ingrese un correo electrónico válido."
            );
            error = true;

        } else if (
                usuarioRepository
                        .existsByCorreoIgnoreCase(correo)
                        &&
                        (
                                actual.getCorreo() == null
                                        ||
                                        !actual.getCorreo()
                                                .equalsIgnoreCase(correo)
                        )
        ) {

            model.addAttribute(
                    "errorCorreo",
                    "El correo electrónico ya se encuentra registrado."
            );
            error = true;
        }

        if (nombreUsuario.isBlank()) {
            model.addAttribute(
                    "errorNombreUsuario",
                    "El nombre de usuario es obligatorio."
            );
            error = true;

        } else if (
                usuarioRepository
                        .existsByNombreUsuarioIgnoreCase(
                                nombreUsuario
                        )
                        &&
                        !actual.getNombreUsuario()
                                .equalsIgnoreCase(nombreUsuario)
        ) {

            model.addAttribute(
                    "errorNombreUsuario",
                    "El nombre de usuario ya se encuentra registrado."
            );
            error = true;
        }

        if (!dpiValido(dpi)) {
            model.addAttribute(
                    "errorDpi",
                    "El DPI debe contener exactamente 13 dígitos."
            );
            error = true;

        } else if (
                dpi != null
                        &&
                        usuarioRepository.existsByDpi(dpi)
                        &&
                        (
                                actual.getDpi() == null
                                        ||
                                        !actual.getDpi().equals(dpi)
                        )
        ) {

            model.addAttribute(
                    "errorDpi",
                    "El DPI ya se encuentra registrado."
            );
            error = true;
        }

        if (rolId == null) {
            model.addAttribute(
                    "errorRol",
                    "Debe seleccionar un rol válido."
            );
            error = true;
        }

        return error;
    }

    private boolean correoValido(
            String correo
    ) {
        return correo != null
                &&
                correo.matches(
                        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
                );
    }

    private boolean dpiValido(
            String dpi
    ) {
        return dpi == null
                ||
                dpi.matches("\\d{13}");
    }
}
