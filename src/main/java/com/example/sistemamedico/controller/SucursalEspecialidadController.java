package com.example.sistemamedico.controller;

import com.example.sistemamedico.model.Usuario;
import com.example.sistemamedico.service.SucursalEspecialidadService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/branch-specialty")
public class SucursalEspecialidadController {


    // =====================================================
    // SERVICIO
    // =====================================================

    private final SucursalEspecialidadService
            sucursalEspecialidadService;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public SucursalEspecialidadController(
            SucursalEspecialidadService
                    sucursalEspecialidadService
    ) {

        this.sucursalEspecialidadService =
                sucursalEspecialidadService;
    }


    // =====================================================
    // CU-13
    // VERIFICAR ADMINISTRADOR
    //
    // Se aceptan las dos formas de sesión que utiliza
    // actualmente el proyecto:
    //
    // 1. usuarioInterno -> objeto Usuario
    // 2. usuario + rol  -> String
    // =====================================================

    private boolean esAdministrador(
            HttpSession session
    ) {

        Object usuarioAdmin = session.getAttribute("usuarioAdmin");

        if (usuarioAdmin instanceof Usuario admin) {

            if (admin.getRol() != null &&
                    "ADMINISTRADOR".equalsIgnoreCase(
                            admin.getRol().getNombre()
                    )) {

                return true;
            }
        }



        // =================================================
        // FORMA 1
        // SESIÓN COMO OBJETO USUARIO
        // =================================================

        Object usuarioInterno =
                session.getAttribute(
                        "usuarioInterno"
                );


        if (usuarioInterno instanceof Usuario usuario) {

            if (
                    usuario.getRol() != null
                            &&
                            usuario.getRol().getNombre() != null
            ) {

                String nombreRol =
                        usuario
                                .getRol()
                                .getNombre();


                if (
                        "ADMIN".equalsIgnoreCase(
                                nombreRol
                        )
                                ||
                                "ADMINISTRADOR".equalsIgnoreCase(
                                        nombreRol
                                )
                ) {

                    return true;
                }
            }
        }


        // =================================================
        // FORMA 2
        // SESIÓN ADMINISTRATIVA TRADICIONAL
        // =================================================

        Object usuarioSesion =
                session.getAttribute(
                        "usuario"
                );

        Object rolSesion =
                session.getAttribute(
                        "rol"
                );


        if (
                usuarioSesion instanceof String
                        &&
                        rolSesion instanceof String
        ) {

            String usuario =
                    ((String) usuarioSesion)
                            .trim();

            String rol =
                    ((String) rolSesion)
                            .trim();


            return !usuario.isEmpty()
                    &&
                    (
                            "ADMIN".equalsIgnoreCase(
                                    rol
                            )
                                    ||
                                    "ADMINISTRADOR".equalsIgnoreCase(
                                            rol
                                    )
                    );
        }


        return false;
    }


    // =====================================================
    // OBTENER NOMBRE DEL ADMINISTRADOR
    // PARA MOSTRAR EN LA VISTA
    // =====================================================

    private String obtenerNombreAdministrador(
            HttpSession session
    ) {

        // ==========================================
        // OBTENER ADMINISTRADOR DE LA SESIÓN
        // ==========================================

        Object usuarioAdmin =
                session.getAttribute("usuarioAdmin");

        if (usuarioAdmin instanceof Usuario admin) {

            if (admin.getNombreUsuario() != null
                    && !admin.getNombreUsuario().isBlank()) {

                return admin.getNombreUsuario();
            }

            if (admin.getNombreCompleto() != null
                    && !admin.getNombreCompleto().isBlank()) {

                return admin.getNombreCompleto();
            }
        }


        // =================================================
        // SESIÓN usuarioInterno
        // =================================================

        Object usuarioInterno =
                session.getAttribute(
                        "usuarioInterno"
                );


        if (usuarioInterno instanceof Usuario usuario) {

            if (
                    usuario.getNombreUsuario() != null
                            &&
                            !usuario.getNombreUsuario()
                                    .isBlank()
            ) {

                return usuario
                        .getNombreUsuario();
            }


            if (
                    usuario.getNombreCompleto() != null
                            &&
                            !usuario.getNombreCompleto()
                                    .isBlank()
            ) {

                return usuario
                        .getNombreCompleto();
            }
        }


        // =================================================
        // SESIÓN usuario
        // =================================================

        Object usuario =
                session.getAttribute(
                        "usuario"
                );


        if (usuario instanceof String) {

            return (String) usuario;
        }


        return "Administrador";
    }


    // =====================================================
    // CU-13
    // LISTADO DE ESPECIALIDADES POR SEDE
    // =====================================================

    @GetMapping
    public String listarAsignaciones(

            @RequestParam(
                    defaultValue = ""
            )
            String buscar,

            @RequestParam(
                    defaultValue = "0"
            )
            int pagina,

            @RequestParam(
                    defaultValue = "10"
            )
            int tamanio,

            HttpSession session,

            Model model

    ) {


        // =================================================
        // VALIDAR ADMINISTRADOR
        // =================================================

        if (!esAdministrador(session)) {

            return "redirect:/admin/login";
        }


        // =================================================
        // OBTENER ASIGNACIONES
        // =================================================

        var asignaciones =
                sucursalEspecialidadService
                        .obtenerAsignaciones(
                                buscar,
                                pagina,
                                tamanio
                        );


        // =================================================
        // DATOS PARA LA VISTA
        // =================================================

        model.addAttribute(
                "asignaciones",
                asignaciones
        );


        model.addAttribute(
                "buscar",
                buscar
        );


        model.addAttribute(
                "tamanio",
                tamanio
        );


        model.addAttribute(
                "paginaActual",
                pagina
        );


        model.addAttribute(
                "usuarioActual",
                obtenerNombreAdministrador(
                        session
                )
        );


        return "admin/branch-specialty/listar";
    }


    // =====================================================
    // CU-13
    // MOSTRAR FORMULARIO
    // ASIGNAR ESPECIALIDAD
    // =====================================================

    @GetMapping("/create")
    public String mostrarFormulario(

            HttpSession session,

            Model model

    ) {


        // =================================================
        // VALIDAR ADMINISTRADOR
        // =================================================

        if (!esAdministrador(session)) {

            return "redirect:/admin/login";
        }


        // =================================================
        // SOLO SUCURSALES ACTIVAS
        // =================================================

        model.addAttribute(
                "sucursales",
                sucursalEspecialidadService
                        .obtenerSucursalesActivas()
        );


        // =================================================
        // SOLO ESPECIALIDADES ACTIVAS
        // =================================================

        model.addAttribute(
                "especialidades",
                sucursalEspecialidadService
                        .obtenerEspecialidadesActivas()
        );


        model.addAttribute(
                "usuarioActual",
                obtenerNombreAdministrador(
                        session
                )
        );


        return "admin/branch-specialty/crear";
    }


    // =====================================================
    // CU-13
    // REGISTRAR ASIGNACIÓN
    // =====================================================

    @PostMapping("/create")
    public String crearAsignacion(

            @RequestParam(
                    value = "sucursalId",
                    required = false
            )
            Long sucursalId,

            @RequestParam(
                    value = "especialidadId",
                    required = false
            )
            Long especialidadId,

            HttpSession session,

            Model model,

            RedirectAttributes redirectAttributes

    ) {


        // =================================================
        // VALIDAR ADMINISTRADOR
        // =================================================

        if (!esAdministrador(session)) {

            return "redirect:/admin/login";
        }


        try {


            // =============================================
            // GUARDAR ASIGNACIÓN
            // =============================================


            sucursalEspecialidadService
                    .asignarEspecialidad(
                            sucursalId,
                            especialidadId,
                            obtenerNombreAdministrador(session)
                    );


            // =============================================
            // MENSAJE DE ÉXITO
            // =============================================

            redirectAttributes
                    .addFlashAttribute(
                            "mensajeExito",
                            "Especialidad asignada a la sede correctamente"
                    );


            return "redirect:/branch-specialty";


        } catch (
                IllegalArgumentException
                |
                IllegalStateException e
        ) {


            // =============================================
            // FA03 / FA05
            // MOSTRAR ERROR
            // =============================================

            model.addAttribute(
                    "mensajeError",
                    e.getMessage()
            );


            // =============================================
            // MANTENER SELECCIONES
            // =============================================

            model.addAttribute(
                    "sucursalSeleccionada",
                    sucursalId
            );


            model.addAttribute(
                    "especialidadSeleccionada",
                    especialidadId
            );


            // =============================================
            // RECARGAR SUCURSALES ACTIVAS
            // =============================================

            model.addAttribute(
                    "sucursales",
                    sucursalEspecialidadService
                            .obtenerSucursalesActivas()
            );


            // =============================================
            // RECARGAR ESPECIALIDADES ACTIVAS
            // =============================================

            model.addAttribute(
                    "especialidades",
                    sucursalEspecialidadService
                            .obtenerEspecialidadesActivas()
            );


            model.addAttribute(
                    "usuarioActual",
                    obtenerNombreAdministrador(
                            session
                    )
            );


            return "admin/branch-specialty/crear";
        }
    }


    // =====================================================
    // CU-13
    // FA02 - ELIMINAR ASIGNACIÓN
    // =====================================================

    @PostMapping("/delete")
    public String eliminarAsignacion(

            @RequestParam Long id,

            HttpSession session,

            RedirectAttributes redirectAttributes

    ) {


        // =================================================
        // VALIDAR ADMINISTRADOR
        // =================================================

        if (!esAdministrador(session)) {

            return "redirect:/admin/login";
        }


        try {


            // =============================================
            // ELIMINAR ASIGNACIÓN
            // =============================================


            sucursalEspecialidadService
                    .eliminarAsignacion(
                            id,
                            obtenerNombreAdministrador(session)
                    );



            // =============================================
            // MENSAJE DE ÉXITO
            // =============================================

            redirectAttributes
                    .addFlashAttribute(
                            "mensajeExito",
                            "Asignación eliminada correctamente."
                    );


        } catch (IllegalArgumentException e) {


            // =============================================
            // MENSAJE DE ERROR
            // =============================================

            redirectAttributes
                    .addFlashAttribute(
                            "mensajeError",
                            e.getMessage()
                    );
        }


        return "redirect:/branch-specialty";
    }
}
