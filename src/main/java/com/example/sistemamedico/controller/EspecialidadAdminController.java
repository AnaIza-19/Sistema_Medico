
package com.example.sistemamedico.controller;

import com.example.sistemamedico.model.Especialidad;
import com.example.sistemamedico.model.Usuario;

import com.example.sistemamedico.service.EspecialidadAdminService;
import com.example.sistemamedico.service.PermisoRolService;

import jakarta.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/especialidades")
public class EspecialidadAdminController {

    // ==========================================
    // SERVICIOS
    // ==========================================

    private final EspecialidadAdminService
            especialidadAdminService;

    private final PermisoRolService
            permisoRolService;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public EspecialidadAdminController(

            EspecialidadAdminService especialidadAdminService,

            PermisoRolService permisoRolService

    ) {

        this.especialidadAdminService =
                especialidadAdminService;

        this.permisoRolService =
                permisoRolService;
    }


    // ==========================================
    // OBTENER ADMINISTRADOR DE LA SESIÓN
    // ==========================================

    private Usuario obtenerAdministrador(
            HttpSession session
    ) {

        Object usuarioSesion =
                session.getAttribute("usuarioAdmin");

        if (usuarioSesion instanceof Usuario admin) {

            if (Boolean.TRUE.equals(admin.getActivo())
                    && admin.getRol() != null
                    && "ADMINISTRADOR".equalsIgnoreCase(
                    admin.getRol().getNombre()
            )) {

                return admin;
            }
        }

        return null;
    }


    // ==========================================
    // OBTENER NOMBRE DEL ADMINISTRADOR
    // ==========================================

    private String obtenerNombreAdministrador(
            HttpSession session
    ) {

        Usuario admin =
                obtenerAdministrador(session);

        if (admin != null) {

            return admin.getNombreUsuario();
        }

        return "Administrador";
    }


    // ==========================================
    // CU-14
    // VERIFICAR PERMISO POR OPERACIÓN
    // ==========================================

    private boolean tienePermiso(

            HttpSession session,
            String permiso

    ) {

        Usuario admin =
                obtenerAdministrador(session);

        if (admin == null) {
            return false;
        }

        return permisoRolService.tienePermiso(
                admin,
                permiso
        );
    }


    // ==========================================
    // REDIRECCIÓN POR ACCESO DENEGADO
    // ==========================================

    private String accesoDenegado(

            HttpSession session,
            RedirectAttributes redirectAttributes

    ) {

        if (obtenerAdministrador(session) == null) {

            return "redirect:/admin/login";
        }

        redirectAttributes.addFlashAttribute(
                "errorGeneral",
                "No tiene permisos para realizar esta operación."
        );

        return "redirect:/admin/usuarios";
    }


    // ==========================================
    // CU-14
    // LISTAR ESPECIALIDADES
    // PERMISO: specialty/read
    // ==========================================

    @GetMapping
    public String listarEspecialidades(

            @RequestParam(defaultValue = "")
            String buscar,

            @RequestParam(defaultValue = "0")
            int pagina,

            @RequestParam(defaultValue = "10")
            int tamanio,

            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes

    ) {

        if (!tienePermiso(
                session,
                "specialty/read"
        )) {

            return accesoDenegado(
                    session,
                    redirectAttributes
            );
        }


        Page<Especialidad> especialidades =
                especialidadAdminService
                        .listarEspecialidades(
                                buscar,
                                pagina,
                                tamanio
                        );


        model.addAttribute(
                "especialidades",
                especialidades
        );

        model.addAttribute("buscar", buscar);

        model.addAttribute("tamanio", tamanio);

        model.addAttribute(
                "usuarioActual",
                obtenerNombreAdministrador(session)
        );


        return "admin/especialidades/listar";
    }


    // ==========================================
    // MOSTRAR FORMULARIO DE CREACIÓN
    // PERMISO: specialty/create
    // ==========================================

    @GetMapping("/crear")
    public String mostrarCrear(

            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes

    ) {

        if (!tienePermiso(
                session,
                "specialty/create"
        )) {

            return accesoDenegado(
                    session,
                    redirectAttributes
            );
        }

        model.addAttribute(
                "usuarioActual",
                obtenerNombreAdministrador(session)
        );

        return "admin/especialidades/crear";
    }


    // ==========================================
    // CREAR ESPECIALIDAD
    // PERMISO: specialty/create
    // ==========================================

    @PostMapping("/crear")
    public String crearEspecialidad(

            @RequestParam String nombre,

            @RequestParam String descripcion,

            @RequestParam(required = false)
            BigDecimal precio,

            HttpSession session,
            RedirectAttributes redirectAttributes

    ) {

        if (!tienePermiso(
                session,
                "specialty/create"
        )) {

            return accesoDenegado(
                    session,
                    redirectAttributes
            );
        }


        try {


            Especialidad especialidad =
                    especialidadAdminService
                            .crearEspecialidad(
                                    nombre,
                                    descripcion,
                                    precio,
                                    obtenerNombreAdministrador(session)
                            );



            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "El registro "
                            + especialidad.getNombre()
                            + " ha sido creado exitosamente."
            );


            return "redirect:/admin/especialidades";

        } catch (
                IllegalArgumentException
                | IllegalStateException e
        ) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    e.getMessage()
            );

            redirectAttributes.addFlashAttribute(
                    "nombreAnterior",
                    nombre
            );

            redirectAttributes.addFlashAttribute(
                    "descripcionAnterior",
                    descripcion
            );

            redirectAttributes.addFlashAttribute(
                    "precioAnterior",
                    precio
            );

            return "redirect:/admin/especialidades/crear";
        }
    }


    // ==========================================
    // MOSTRAR FORMULARIO DE EDICIÓN
    // PERMISO: specialty/update
    // ==========================================

    @GetMapping("/editar/{id}")
    public String mostrarEditar(

            @PathVariable Long id,

            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes

    ) {

        if (!tienePermiso(
                session,
                "specialty/update"
        )) {

            return accesoDenegado(
                    session,
                    redirectAttributes
            );
        }


        try {

            Especialidad especialidad =
                    especialidadAdminService
                            .buscarPorId(id);

            model.addAttribute(
                    "especialidad",
                    especialidad
            );

            model.addAttribute(
                    "usuarioActual",
                    obtenerNombreAdministrador(session)
            );


            return "admin/especialidades/editar";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    e.getMessage()
            );

            return "redirect:/admin/especialidades";
        }
    }


    // ==========================================
    // ACTUALIZAR ESPECIALIDAD
    // PERMISO: specialty/update
    // ==========================================

    @PostMapping("/editar/{id}")
    public String actualizarEspecialidad(

            @PathVariable Long id,

            @RequestParam String nombre,

            @RequestParam String descripcion,

            @RequestParam(required = false)
            BigDecimal precio,

            HttpSession session,
            RedirectAttributes redirectAttributes

    ) {

        if (!tienePermiso(
                session,
                "specialty/update"
        )) {

            return accesoDenegado(
                    session,
                    redirectAttributes
            );
        }


        try {


            Especialidad especialidad =
                    especialidadAdminService
                            .editarEspecialidad(
                                    id,
                                    nombre,
                                    descripcion,
                                    precio,
                                    obtenerNombreAdministrador(session)
                            );



            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "El registro "
                            + especialidad.getNombre()
                            + " ha sido actualizado correctamente."
            );


            return "redirect:/admin/especialidades";

        } catch (
                IllegalArgumentException
                | IllegalStateException e
        ) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    e.getMessage()
            );

            return "redirect:/admin/especialidades/editar/"
                    + id;
        }
    }


    // ==========================================
    // DESACTIVAR ESPECIALIDAD
    // PERMISO: specialty/delete
    // ==========================================

    @PostMapping("/desactivar/{id}")
    public String desactivarEspecialidad(

            @PathVariable Long id,

            HttpSession session,
            RedirectAttributes redirectAttributes

    ) {

        if (!tienePermiso(
                session,
                "specialty/delete"
        )) {

            return accesoDenegado(
                    session,
                    redirectAttributes
            );
        }


        try {

            Especialidad especialidad =
                    especialidadAdminService
                            .buscarPorId(id);

            String nombre =
                    especialidad.getNombre();


            especialidadAdminService
                    .desactivarEspecialidad(
                            id,
                            obtenerNombreAdministrador(session)
                    );



            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "El registro "
                            + nombre
                            + " ha sido eliminado correctamente."
            );

        } catch (
                IllegalArgumentException
                | IllegalStateException e
        ) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    e.getMessage()
            );
        }


        return "redirect:/admin/especialidades";
    }


    // ==========================================
    // REACTIVAR ESPECIALIDAD
    // PERMISO: specialty/reactivate
    // ==========================================

    @PostMapping("/reactivar/{id}")
    public String reactivarEspecialidad(

            @PathVariable Long id,

            HttpSession session,
            RedirectAttributes redirectAttributes

    ) {

        if (!tienePermiso(
                session,
                "specialty/reactivate"
        )) {

            return accesoDenegado(
                    session,
                    redirectAttributes
            );
        }


        try {


            especialidadAdminService
                    .reactivarEspecialidad(
                            id,
                            obtenerNombreAdministrador(session)
                    );



            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Especialidad reactivada correctamente."
            );

        } catch (
                IllegalArgumentException
                | IllegalStateException e
        ) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    e.getMessage()
            );
        }


        return "redirect:/admin/especialidades";
    }
}
