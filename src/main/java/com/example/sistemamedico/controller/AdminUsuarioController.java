package com.example.sistemamedico.controller;

import com.example.sistemamedico.model.Usuario;
import com.example.sistemamedico.service.AdminUsuarioService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {

    private final AdminUsuarioService usuarioService;

    public AdminUsuarioController(
            AdminUsuarioService usuarioService
    ) {
        this.usuarioService = usuarioService;
    }


    // ==========================================
    // LISTAR USUARIOS
    // ==========================================

    @GetMapping
    public String listar(

            @RequestParam(defaultValue = "nombre")
            String campo,

            @RequestParam(defaultValue = "")
            String buscar,

            @RequestParam(defaultValue = "0")
            int pagina,

            @RequestParam(defaultValue = "10")
            int tamanio,

            Model model
    ) {

        usuarioService.prepararListado(
                campo,
                buscar,
                pagina,
                tamanio,
                model
        );

        return "admin/usuarios/listado";
    }


    // ==========================================
    // MOSTRAR CREAR
    // ==========================================

    @GetMapping("/crear")
    public String mostrarCrear(
            Model model
    ) {

        usuarioService.cargarCatalogos(model);

        return "admin/usuarios/crear";
    }


    // ==========================================
    // CREAR USUARIO
    // ==========================================

    @PostMapping("/crear")
    public String crear(

            @RequestParam
            Map<String, String> datos,

            Model model,

            RedirectAttributes redirect,

            HttpSession session
    ) {

        String ejecutadoPor =
                obtenerAdministrador(session);


        boolean creado =
                usuarioService.crearUsuario(
                        datos,
                        model,
                        ejecutadoPor
                );


        if (!creado) {

            usuarioService.cargarCatalogos(model);

            return "admin/usuarios/crear";
        }


        redirect.addFlashAttribute(
                "exito",
                "Usuario creado correctamente."
        );


        return "redirect:/admin/usuarios";
    }


    // ==========================================
    // MOSTRAR EDITAR
    // ==========================================

    @GetMapping("/editar/{id}")
    public String mostrarEditar(

            @PathVariable Long id,

            Model model,

            RedirectAttributes redirect
    ) {

        boolean encontrado =
                usuarioService.prepararEdicion(
                        id,
                        model
                );


        if (!encontrado) {

            redirect.addFlashAttribute(
                    "errorGeneral",
                    "El usuario seleccionado no existe."
            );

            return "redirect:/admin/usuarios";
        }


        return "admin/usuarios/editar";
    }


    // ==========================================
    // ACTUALIZAR USUARIO
    // ==========================================

    @PostMapping("/editar/{id}")
    public String actualizar(

            @PathVariable Long id,

            @RequestParam
            Map<String, String> datos,

            Model model,

            RedirectAttributes redirect,

            HttpSession session
    ) {

        String ejecutadoPor =
                obtenerAdministrador(session);


        boolean actualizado =
                usuarioService.actualizarUsuario(
                        id,
                        datos,
                        model,
                        ejecutadoPor
                );


        if (!actualizado) {

            usuarioService.cargarCatalogos(model);

            return "admin/usuarios/editar";
        }


        redirect.addFlashAttribute(
                "exito",
                "Usuario actualizado correctamente."
        );


        return "redirect:/admin/usuarios";
    }


    // ==========================================
    // ELIMINAR USUARIO
    // ==========================================

    @PostMapping("/eliminar/{id}")
    public String eliminar(

            @PathVariable Long id,

            RedirectAttributes redirect,

            HttpSession session
    ) {

        String ejecutadoPor =
                obtenerAdministrador(session);


        String nombreUsuario =
                usuarioService.eliminarUsuario(
                        id,
                        ejecutadoPor
                );


        if (nombreUsuario == null) {

            redirect.addFlashAttribute(
                    "errorGeneral",
                    "El usuario seleccionado no existe."
            );

        } else {

            redirect.addFlashAttribute(
                    "exito",
                    "El usuario "
                            + nombreUsuario
                            + " ha sido eliminado correctamente."
            );
        }


        return "redirect:/admin/usuarios";
    }


    // ==========================================
    // OBTENER ADMINISTRADOR DE LA SESIÓN
    // ==========================================

    private String obtenerAdministrador(
            HttpSession session
    ) {

        Usuario admin =
                (Usuario) session.getAttribute(
                        "usuarioAdmin"
                );


        if (admin == null) {
            return "DESCONOCIDO";
        }


        return admin.getNombreUsuario();
    }
}


