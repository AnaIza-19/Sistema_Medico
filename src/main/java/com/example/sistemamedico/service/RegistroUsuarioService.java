package com.example.sistemamedico.service;

import com.example.sistemamedico.model.Rol;
import com.example.sistemamedico.model.Usuario;

import com.example.sistemamedico.Repositorio.Rol_Repositorio;
import com.example.sistemamedico.Repositorio.Usuario_Repositorio;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Map;
import java.util.Optional;

@Service
public class RegistroUsuarioService {

    private final Usuario_Repositorio usuarioRepository;
    private final Rol_Repositorio rolRepository;
    private final CorreoService correoService;


    public RegistroUsuarioService(
            Usuario_Repositorio usuarioRepository,
            Rol_Repositorio rolRepository,
            CorreoService correoService
    ) {

        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.correoService = correoService;
    }


    // =====================================================
    // REGISTRAR PACIENTE
    // =====================================================

    public boolean registrarPaciente(
            Map<String, String> datos,
            Model model
    ) {

        String nombre =
                limpiar(datos.get("nombre"));

        String apellido =
                limpiar(datos.get("apellido"));

        String dpi =
                limpiar(datos.get("dpi"));

        String nit =
                opcional(datos.get("nit"));

        String telefono =
                limpiar(datos.get("telefono"));

        String numeroSeguro =
                opcional(datos.get("numeroSeguro"));

        String correo =
                limpiar(datos.get("correo"));

        String nombreUsuario =
                limpiar(datos.get("nombreUsuario"));

        String contrasena =
                limpiar(datos.get("contrasena"));


        boolean errores = false;


        // =====================================================
        // VALIDAR NOMBRE
        // =====================================================

        if (nombre.isBlank()) {

            model.addAttribute(
                    "errorNombre",
                    "El nombre es obligatorio."
            );

            errores = true;
        }


        // =====================================================
        // VALIDAR APELLIDO
        // =====================================================

        if (apellido.isBlank()) {

            model.addAttribute(
                    "errorApellido",
                    "El apellido es obligatorio."
            );

            errores = true;
        }


        // =====================================================
        // VALIDAR DPI
        // =====================================================

        if (!dpi.matches("\\d{13}")) {

            model.addAttribute(
                    "errorDpi",
                    "El DPI debe contener exactamente 13 dígitos."
            );

            errores = true;

        } else if (
                usuarioRepository.existsByDpi(dpi)
        ) {

            model.addAttribute(
                    "errorDpi",
                    "Ya existe una cuenta registrada con este número de DPI. "
                            + "Si ya tiene cuenta, inicie sesión."
            );

            model.addAttribute(
                    "dpiDuplicado",
                    true
            );

            errores = true;
        }

        // =====================================================
        // VALIDAR NIT
        // =====================================================

        if (nit == null) {

            model.addAttribute(
                    "errorNit",
                    "El NIT es obligatorio."
            );

            errores = true;
        }


        // =====================================================
        // VALIDAR TELÉFONO
        // =====================================================

        if (telefono.isBlank()) {

            model.addAttribute(
                    "errorTelefono",
                    "El número de teléfono es obligatorio."
            );

            errores = true;
        }


        // =====================================================
        // VALIDAR CORREO
        // =====================================================

        if (!correoValido(correo)) {

            model.addAttribute(
                    "errorCorreo",
                    "Ingrese un correo electrónico válido."
            );

            errores = true;

        } else if (
                usuarioRepository
                        .existsByCorreoIgnoreCase(
                                correo
                        )
        ) {

            model.addAttribute(
                    "errorCorreo",
                    "Ya existe una cuenta registrada con este correo electrónico."
            );

            errores = true;
        }


        // =====================================================
        // VALIDAR NOMBRE DE USUARIO
        // ENTRE 8 Y 9 CARACTERES
        // =====================================================

        if (
                nombreUsuario.length() < 8
                        ||
                        nombreUsuario.length() > 9
        ) {

            model.addAttribute(
                    "errorNombreUsuario",
                    "El nombre de usuario debe contener entre 8 y 9 caracteres."
            );

            errores = true;

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

            errores = true;
        }


        // =====================================================
        // VALIDAR CONTRASEÑA
        // MÍNIMO 12 CARACTERES
        // =====================================================

        if (contrasena.length() < 12) {

            model.addAttribute(
                    "errorContrasena",
                    "La contraseña debe contener como mínimo 12 caracteres."
            );

            errores = true;
        }


        // =====================================================
        // SI HAY ERRORES
        // =====================================================

        if (errores) {

            conservarDatos(
                    datos,
                    model
            );

            return false;
        }


        // =====================================================
        // OBTENER ROL PACIENTE
        // =====================================================

        Optional<Rol> rolPaciente =
                rolRepository.findByNombre(
                        "PACIENTE"
                );


        if (rolPaciente.isEmpty()) {

            model.addAttribute(
                    "errorGeneral",
                    "No se encontró el rol PACIENTE en el sistema."
            );

            conservarDatos(
                    datos,
                    model
            );

            return false;
        }


        // =====================================================
        // CREAR USUARIO
        // =====================================================

        Usuario usuario =
                new Usuario();


        usuario.setNombre(
                nombre
        );

        usuario.setApellido(
                apellido
        );

        usuario.setDpi(
                dpi
        );

        usuario.setNit(
                nit
        );

        usuario.setTelefono(
                telefono
        );

        usuario.setNumeroSeguro(
                numeroSeguro
        );

        usuario.setCorreo(
                correo
        );

        usuario.setNombreUsuario(
                nombreUsuario
        );

        usuario.setContrasena(
                contrasena
        );


        usuario.setRol(
                rolPaciente.get()
        );


        // El CU-02 indica que el paciente
        // debe quedar registrado como activo.

        usuario.setActivo(
                true
        );


        usuario.setIntentosFallidos(
                0
        );

        usuario.setBloqueadoHasta(
                null
        );


        // Un paciente registrado desde el portal
        // no necesita sucursal ni especialidad.

        usuario.setSucursal(
                null
        );

        usuario.setEspecialidad(
                null
        );


        // =====================================================
        // GUARDAR EN BASE DE DATOS
        // =====================================================

        usuarioRepository.save(
                usuario
        );


        // =====================================================
        // ENVIAR CORREO DE BIENVENIDA
        // =====================================================

        correoService.enviarCorreoBienvenida(
                correo,
                nombre + " " + apellido
        );


        return true;
    }


    // =====================================================
    // CONSERVAR DATOS SI EXISTE UN ERROR
    // =====================================================

    private void conservarDatos(
            Map<String, String> datos,
            Model model
    ) {

        model.addAttribute(
                "nombre",
                datos.get("nombre")
        );

        model.addAttribute(
                "apellido",
                datos.get("apellido")
        );

        model.addAttribute(
                "dpi",
                datos.get("dpi")
        );

        model.addAttribute(
                "nit",
                datos.get("nit")
        );

        model.addAttribute(
                "telefono",
                datos.get("telefono")
        );

        model.addAttribute(
                "numeroSeguro",
                datos.get("numeroSeguro")
        );

        model.addAttribute(
                "correo",
                datos.get("correo")
        );

        model.addAttribute(
                "nombreUsuario",
                datos.get("nombreUsuario")
        );
    }


    // =====================================================
    // LIMPIAR TEXTO
    // =====================================================

    private String limpiar(
            String texto
    ) {

        return texto == null
                ? ""
                : texto.trim();
    }


    // =====================================================
    // CAMPO OPCIONAL
    // =====================================================

    private String opcional(
            String texto
    ) {

        String valor =
                limpiar(
                        texto
                );


        return valor.isEmpty()
                ? null
                : valor;
    }


    // =====================================================
    // VALIDAR CORREO
    // =====================================================

    private boolean correoValido(
            String correo
    ) {

        return correo != null
                &&
                correo.matches(
                        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
                );
    }
}