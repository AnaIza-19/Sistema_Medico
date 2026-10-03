package com.example.sistemamedico.service;

import com.example.sistemamedico.model.Usuario;
import com.example.sistemamedico.model.Rol;
import com.example.sistemamedico.model.Sucursal;
import com.example.sistemamedico.model.Especialidad;

import com.example.sistemamedico.Repositorio.Usuario_Repositorio;
import com.example.sistemamedico.Repositorio.Rol_Repositorio;
import com.example.sistemamedico.Repositorio.Sucursal_Repositorio;
import com.example.sistemamedico.Repositorio.Especialidad_Repositorio;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service

public class AdminUsuarioService {

    private final CatalogoAdminService catalogoAdminService;
    private final Usuario_Repositorio usuarioRepository;
    private final Rol_Repositorio rolRepository;
    private final Sucursal_Repositorio sucursalRepository;
    private final Especialidad_Repositorio especialidadRepository;

    private final UsuarioValidacionService validacionService;
    private final AuditoriaUsuarioService auditoriaService;


    public AdminUsuarioService(
            CatalogoAdminService catalogoAdminService,
            Usuario_Repositorio usuarioRepository,
            Rol_Repositorio rolRepository,
            Sucursal_Repositorio sucursalRepository,
            Especialidad_Repositorio especialidadRepository,
            UsuarioValidacionService validacionService,
            AuditoriaUsuarioService auditoriaService
    ) {
        this.catalogoAdminService = catalogoAdminService;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.sucursalRepository = sucursalRepository;
        this.especialidadRepository = especialidadRepository;
        this.validacionService = validacionService;
        this.auditoriaService = auditoriaService;
    }

    public void prepararListado(
            String campo,
            String buscar,
            int pagina,
            int tamanio,
            Model model
    ) {

        if (tamanio != 10 &&
                tamanio != 25 &&
                tamanio != 50) {

            tamanio = 10;
        }

        if (pagina < 0) {
            pagina = 0;
        }

        Pageable pageable =
                PageRequest.of(
                        pagina,
                        tamanio
                );

        String criterio =
                limpiar(buscar);

        Page<Usuario> usuarios;

        if (criterio.isEmpty()) {

            usuarios =
                    usuarioRepository.findAll(
                            pageable
                    );

        } else {

            usuarios =
                    buscarUsuarios(
                            campo,
                            criterio,
                            pageable
                    );
        }

        model.addAttribute(
                "usuarios",
                usuarios
        );

        model.addAttribute(
                "campo",
                campo
        );

        model.addAttribute(
                "buscar",
                criterio
        );

        model.addAttribute(
                "tamanio",
                tamanio
        );

        model.addAttribute(
                "paginaActual",
                pagina
        );
    }


    private Page<Usuario> buscarUsuarios(
            String campo,
            String criterio,
            Pageable pageable
    ) {

        switch (campo) {

            case "id":

                try {

                    Long id =
                            Long.parseLong(
                                    criterio
                            );

                    Optional<Usuario> usuario =
                            usuarioRepository.findById(id);

                    if (usuario.isPresent()) {

                        return new PageImpl<>(
                                List.of(
                                        usuario.get()
                                ),
                                pageable,
                                1
                        );
                    }

                    return Page.empty(
                            pageable
                    );

                } catch (NumberFormatException e) {

                    return Page.empty(
                            pageable
                    );
                }


            case "correo":

                return usuarioRepository
                        .findByCorreoContainingIgnoreCase(
                                criterio,
                                pageable
                        );


            case "rol":

                return usuarioRepository
                        .findByRolNombreContainingIgnoreCase(
                                criterio,
                                pageable
                        );


            case "usuario":

                return usuarioRepository
                        .findByNombreUsuarioContainingIgnoreCase(
                                criterio,
                                pageable
                        );


            case "dpi":

                return usuarioRepository
                        .findByDpiContaining(
                                criterio,
                                pageable
                        );


            default:

                return usuarioRepository
                        .findByNombreContainingIgnoreCase(
                                criterio,
                                pageable
                        );
        }
    }

    public void cargarCatalogos(
            Model model
    ) {

        model.addAttribute(
                "roles",
                catalogoAdminService.obtenerRoles()
        );

        model.addAttribute(
                "sucursales",
                catalogoAdminService.obtenerSucursales()
        );

        model.addAttribute(
                "especialidades",
                catalogoAdminService.obtenerEspecialidades()
        );
    }

    @Transactional
    public boolean crearUsuario(
            Map<String, String> datos,
            Model model,
            String ejecutadoPor
    ) {

        String nombre =
                limpiar(datos.get("nombre"));

        String apellido =
                limpiar(datos.get("apellido"));

        String correo =
                limpiar(datos.get("correo"));

        String nombreUsuario =
                limpiar(datos.get("nombreUsuario"));

        String contrasena =
                limpiar(datos.get("contrasena"));

        String dpi =
                opcional(datos.get("dpi"));

        String telefono =
                opcional(datos.get("telefono"));

        String nit =
                opcional(datos.get("nit"));

        String numeroSeguro =
                opcional(datos.get("numeroSeguro"));

        Long rolId =
                convertirLong(
                        datos.get("rolId")
                );

        Long sucursalId =
                convertirLong(
                        datos.get("sucursalId")
                );

        Long especialidadId =
                convertirLong(
                        datos.get("especialidadId")
                );

        boolean activo =
                Boolean.parseBoolean(
                        datos.getOrDefault(
                                "activo",
                                "true"
                        )
                );

        boolean errores =
                validacionService.validarNuevo(
                        nombre,
                        apellido,
                        correo,
                        nombreUsuario,
                        contrasena,
                        dpi,
                        rolId,
                        model
                );

        Rol rol =
                obtenerRol(
                        rolId,
                        model
                );

        Sucursal sucursal =
                obtenerSucursal(
                        sucursalId,
                        model
                );

        Especialidad especialidad =
                obtenerEspecialidad(
                        especialidadId,
                        model
                );

        if (
                rol != null
                        &&
                        !"MEDICO".equalsIgnoreCase(
                                rol.getNombre()
                        )
        ) {

            especialidad = null;
        }

        if (rol == null) {

            errores = true;
        }


        if (
                sucursalId != null
                        &&
                        sucursal == null
        ) {

            errores = true;
        }


        /*
         * La especialidad es opcional.
         * Solo se valida si:
         *
         * 1. El usuario es MEDICO
         * 2. Se envió una especialidad
         * 3. Esa especialidad no existe o está inactiva
         */

        if (
                rol != null
                        &&
                        "MEDICO".equalsIgnoreCase(
                                rol.getNombre()
                        )
                        &&
                        especialidadId != null
                        &&
                        especialidad == null
        ) {

            errores = true;
        }

        if (errores) {

            conservarDatosCrear(
                    datos,
                    model
            );

            return false;
        }

        Usuario usuario =
                new Usuario();


        usuario.setNombre(
                nombre
        );

        usuario.setApellido(
                apellido
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

        usuario.setDpi(
                dpi
        );

        usuario.setTelefono(
                telefono
        );

        usuario.setNit(
                nit
        );

        usuario.setNumeroSeguro(
                numeroSeguro
        );

        usuario.setRol(
                rol
        );

        usuario.setSucursal(
                sucursal
        );

        usuario.setEspecialidad(
                especialidad
        );

        usuario.setActivo(
                activo
        );

        usuario.setIntentosFallidos(
                0
        );

        usuario.setBloqueadoHasta(
                null
        );

        usuarioRepository.save(
                usuario
        );

        auditoriaService.registrar(
                "CREAR",
                usuario,
                ejecutadoPor
        );


        return true;
    }

    public boolean prepararEdicion(
            Long id,
            Model model
    ) {

        Optional<Usuario> usuario =
                usuarioRepository.findById(
                        id
                );


        if (usuario.isEmpty()) {

            return false;
        }


        model.addAttribute(
                "usuarioEditar",
                usuario.get()
        );


        cargarCatalogos(
                model
        );


        return true;
    }

    @Transactional
    public boolean actualizarUsuario(
            Long id,
            Map<String, String> datos,
            Model model,
            String ejecutadoPor
    ) {

        Optional<Usuario> encontrado =
                usuarioRepository.findById(
                        id
                );


        if (encontrado.isEmpty()) {

            model.addAttribute(
                    "errorGeneral",
                    "El usuario seleccionado no existe."
            );

            return false;
        }


        Usuario usuario =
                encontrado.get();


        String nombre =
                limpiar(datos.get("nombre"));

        String apellido =
                limpiar(datos.get("apellido"));

        String correo =
                limpiar(datos.get("correo"));

        String nombreUsuario =
                limpiar(datos.get("nombreUsuario"));

        String nuevaContrasena =
                opcional(
                        datos.get("nuevaContrasena")
                );

        String dpi =
                opcional(datos.get("dpi"));

        String telefono =
                opcional(datos.get("telefono"));

        String nit =
                opcional(datos.get("nit"));

        String numeroSeguro =
                opcional(datos.get("numeroSeguro"));

        Long rolId =
                convertirLong(
                        datos.get("rolId")
                );

        Long sucursalId =
                convertirLong(
                        datos.get("sucursalId")
                );

        Long especialidadId =
                convertirLong(
                        datos.get("especialidadId")
                );

        boolean activo =
                Boolean.parseBoolean(
                        datos.getOrDefault(
                                "activo",
                                "true"
                        )
                );


        boolean errores =
                validacionService.validarEdicion(
                        usuario,
                        nombre,
                        apellido,
                        correo,
                        nombreUsuario,
                        dpi,
                        rolId,
                        model
                );


        Rol rol =
                obtenerRol(
                        rolId,
                        model
                );

        Sucursal sucursal =
                obtenerSucursal(
                        sucursalId,
                        model
                );

        Especialidad especialidad =
                obtenerEspecialidad(
                        especialidadId,
                        model
                );
        if (
                rol != null
                        &&
                        !"MEDICO".equalsIgnoreCase(
                                rol.getNombre()
                        )
        ) {
            especialidad = null;
        }

        if (rol == null) {
            errores = true;
        }

        if (sucursalId != null &&
                sucursal == null) {

            errores = true;
        }

        if (
                rol != null
                        &&
                        "MEDICO".equalsIgnoreCase(
                                rol.getNombre()
                        )
                        &&
                        especialidadId != null
                        &&
                        especialidad == null
        ) {
            errores = true;
        }

        if (errores) {

            model.addAttribute(
                    "usuarioEditar",
                    usuario
            );

            return false;
        }


        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setCorreo(correo);
        usuario.setNombreUsuario(nombreUsuario);
        usuario.setDpi(dpi);
        usuario.setTelefono(telefono);
        usuario.setNit(nit);
        usuario.setNumeroSeguro(numeroSeguro);
        usuario.setRol(rol);
        usuario.setSucursal(sucursal);
        usuario.setEspecialidad(especialidad);
        usuario.setActivo(activo);


        if (nuevaContrasena != null) {

            usuario.setContrasena(
                    nuevaContrasena
            );
        }


        usuarioRepository.save(
                usuario
        );


        auditoriaService.registrar(
                "ACTUALIZAR",
                usuario,
                ejecutadoPor
        );


        return true;
    }
    @Transactional
    public String eliminarUsuario(
            Long id,
            String ejecutadoPor
    ) {

        Optional<Usuario> encontrado =
                usuarioRepository.findById(
                        id
                );


        if (encontrado.isEmpty()) {

            return null;
        }


        Usuario usuario =
                encontrado.get();


        String nombreUsuario =
                usuario.getNombreUsuario();


        // ==========================================
        // ELIMINACIÓN LÓGICA
        // ==========================================

        usuario.setActivo(false);


        usuarioRepository.save(
                usuario
        );


        // ==========================================
        // REGISTRAR AUDITORÍA
        // ==========================================

        auditoriaService.registrarEliminacion(
                nombreUsuario,
                ejecutadoPor
        );


        return nombreUsuario;
    }

    private Rol obtenerRol(
            Long id,
            Model model
    ) {

        if (id == null) {
            return null;
        }


        return rolRepository
                .findById(id)
                .orElseGet(() -> {

                    model.addAttribute(
                            "errorRol",
                            "Debe seleccionar un rol válido."
                    );

                    return null;
                });
    }

    private Sucursal obtenerSucursal(
            Long id,
            Model model
    ) {

        if (id == null) {
            return null;
        }


        Optional<Sucursal> encontrada =
                sucursalRepository.findById(id);


        if (encontrada.isPresent() &&
                Boolean.TRUE.equals(
                        encontrada.get().getActivo()
                )) {

            return encontrada.get();
        }


        model.addAttribute(
                "errorSucursal",
                "La sucursal seleccionada no es válida."
        );


        return null;
    }

    private Especialidad obtenerEspecialidad(
            Long id,
            Model model
    ) {

        if (id == null) {
            return null;
        }


        Optional<Especialidad> encontrada =
                especialidadRepository.findById(
                        id
                );


        if (encontrada.isPresent() &&
                Boolean.TRUE.equals(
                        encontrada.get().getActivo()
                )) {

            return encontrada.get();
        }


        model.addAttribute(
                "errorEspecialidad",
                "La especialidad seleccionada no es válida."
        );


        return null;
    }

    private void conservarDatosCrear(
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
                "correo",
                datos.get("correo")
        );

        model.addAttribute(
                "nombreUsuario",
                datos.get("nombreUsuario")
        );

        model.addAttribute(
                "dpi",
                datos.get("dpi")
        );

        model.addAttribute(
                "telefono",
                datos.get("telefono")
        );

        model.addAttribute(
                "nit",
                datos.get("nit")
        );

        model.addAttribute(
                "numeroSeguro",
                datos.get("numeroSeguro")
        );

        model.addAttribute(
                "rolId",
                convertirLong(
                        datos.get("rolId")
                )
        );

        model.addAttribute(
                "sucursalId",
                convertirLong(
                        datos.get("sucursalId")
                )
        );

        model.addAttribute(
                "especialidadId",
                convertirLong(
                        datos.get("especialidadId")
                )
        );

        model.addAttribute(
                "activoSeleccionado",
                Boolean.parseBoolean(
                        datos.getOrDefault(
                                "activo",
                                "true"
                        )
                )
        );
    }

    private String limpiar(
            String texto
    ) {

        return texto == null
                ? ""
                : texto.trim();
    }


    private String opcional(
            String texto
    ) {

        String valor =
                limpiar(texto);

        return valor.isEmpty()
                ? null
                : valor;
    }


    private Long convertirLong(
            String valor
    ) {

        if (valor == null ||
                valor.isBlank()) {

            return null;
        }


        try {

            return Long.parseLong(
                    valor
            );

        } catch (NumberFormatException e) {

            return null;
        }
    }
}