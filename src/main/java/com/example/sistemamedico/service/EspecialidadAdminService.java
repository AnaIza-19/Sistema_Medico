
package com.example.sistemamedico.service;

import com.example.sistemamedico.model.Especialidad;
import com.example.sistemamedico.Repositorio.Especialidad_Repositorio;

import org.springframework.cache.annotation.CacheEvict;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class EspecialidadAdminService {

    // ==========================================
    // REPOSITORIO Y AUDITORÍA
    // ==========================================

    private final Especialidad_Repositorio
            especialidadRepository;

    private final AuditoriaEspecialidadService
            auditoriaService;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public EspecialidadAdminService(

            Especialidad_Repositorio especialidadRepository,

            AuditoriaEspecialidadService auditoriaService

    ) {

        this.especialidadRepository =
                especialidadRepository;

        this.auditoriaService =
                auditoriaService;
    }


    // ==========================================
    // CU-14
    // LISTAR Y BUSCAR ESPECIALIDADES
    // ==========================================

    @Transactional(readOnly = true)
    public Page<Especialidad> listarEspecialidades(

            String buscar,
            int pagina,
            int tamanio

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
                        tamanio,
                        Sort.by("id").ascending()
                );

        if (buscar == null || buscar.isBlank()) {

            return especialidadRepository
                    .findAll(pageable);
        }

        return especialidadRepository
                .findByNombreContainingIgnoreCase(
                        buscar.trim(),
                        pageable
                );
    }


    // ==========================================
    // BUSCAR ESPECIALIDAD POR ID
    // ==========================================

    @Transactional(readOnly = true)
    public Especialidad buscarPorId(Long id) {

        return especialidadRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La especialidad no existe."
                        )
                );
    }


    // ==========================================
    // VALIDAR ADMINISTRADOR
    // ==========================================

    private String validarAdministrador(
            String ejecutadoPor
    ) {

        if (ejecutadoPor == null ||
                ejecutadoPor.isBlank()) {

            throw new IllegalArgumentException(
                    "Debe identificarse el administrador."
            );
        }

        return ejecutadoPor.trim();
    }


    // ==========================================
    // VALIDAR NOMBRE
    // ==========================================

    private String validarNombre(String nombre) {

        if (nombre == null || nombre.isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre es obligatorio."
            );
        }

        String nombreLimpio = nombre.trim();

        if (nombreLimpio.length() > 200) {

            throw new IllegalArgumentException(
                    "El nombre no puede superar 200 caracteres."
            );
        }

        return nombreLimpio;
    }


    // ==========================================
    // VALIDAR DESCRIPCIÓN
    // ==========================================

    private String validarDescripcion(
            String descripcion
    ) {

        if (descripcion == null ||
                descripcion.isBlank()) {

            throw new IllegalArgumentException(
                    "La descripción es obligatoria."
            );
        }

        String descripcionLimpia =
                descripcion.trim();

        if (descripcionLimpia.length() > 500) {

            throw new IllegalArgumentException(
                    "La descripción no puede superar 500 caracteres."
            );
        }

        return descripcionLimpia;
    }


    // ==========================================
    // VALIDAR PRECIO
    // ==========================================

    private void validarPrecio(BigDecimal precio) {

        if (precio != null &&
                precio.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "El precio debe ser mayor a cero."
            );
        }
    }


    // ==========================================
    // CU-14
    // CREAR ESPECIALIDAD CON AUDITORÍA
    // ==========================================

    @Transactional
    @CacheEvict(
            cacheNames = {
                    "especialidades",
                    "especialidadesAdmin",
                    "especialidadesPorSucursal"
            },
            allEntries = true
    )
    public Especialidad crearEspecialidad(

            String nombre,
            String descripcion,
            BigDecimal precio,
            String ejecutadoPor

    ) {

        String administrador =
                validarAdministrador(ejecutadoPor);

        String nombreLimpio =
                validarNombre(nombre);

        String descripcionLimpia =
                validarDescripcion(descripcion);

        validarPrecio(precio);


        // ======================================
        // VALIDAR NOMBRE DUPLICADO
        // ======================================

        boolean existe =
                especialidadRepository
                        .existsByNombreIgnoreCaseAndActivoTrue(
                                nombreLimpio
                        );

        if (existe) {

            throw new IllegalStateException(
                    "Ya existe un registro con el nombre "
                            + nombreLimpio + "."
            );
        }


        // ======================================
        // CREAR ESPECIALIDAD
        // ======================================

        Especialidad especialidad =
                new Especialidad();

        especialidad.setNombre(nombreLimpio);

        especialidad.setDescripcion(
                descripcionLimpia
        );

        especialidad.setPrecio(precio);

        especialidad.setActivo(true);


        // ======================================
        // GUARDAR EN POSTGRESQL
        // ======================================

        Especialidad guardada =
                especialidadRepository
                        .saveAndFlush(especialidad);


        // ======================================
        // REGISTRAR AUDITORÍA
        // ======================================

        auditoriaService.registrarCreacion(
                guardada,
                administrador
        );


        return guardada;
    }


    // ==========================================
    // CU-14
    // EDITAR ESPECIALIDAD CON AUDITORÍA
    // ==========================================

    @Transactional
    @CacheEvict(
            cacheNames = {
                    "especialidades",
                    "especialidadesAdmin",
                    "especialidadesPorSucursal"
            },
            allEntries = true
    )
    public Especialidad editarEspecialidad(

            Long id,
            String nombre,
            String descripcion,
            BigDecimal precio,
            String ejecutadoPor

    ) {

        String administrador =
                validarAdministrador(ejecutadoPor);

        Especialidad especialidad =
                buscarPorId(id);


        // ======================================
        // CAPTURAR DATOS ANTERIORES
        // ======================================

        String datosAnteriores =
                auditoriaService.capturarDatos(
                        especialidad
                );


        // ======================================
        // VALIDACIONES
        // ======================================

        String nombreLimpio =
                validarNombre(nombre);

        String descripcionLimpia =
                validarDescripcion(descripcion);

        validarPrecio(precio);


        // ======================================
        // VALIDAR DUPLICADOS ACTIVOS
        // ======================================

        if (Boolean.TRUE.equals(
                especialidad.getActivo()
        )) {

            boolean existe =
                    especialidadRepository
                            .existsByNombreIgnoreCaseAndActivoTrueAndIdNot(
                                    nombreLimpio,
                                    id
                            );

            if (existe) {

                throw new IllegalStateException(
                        "Ya existe un registro con el nombre "
                                + nombreLimpio + "."
                );
            }
        }


        // ======================================
        // ACTUALIZAR DATOS
        // ======================================

        especialidad.setNombre(nombreLimpio);

        especialidad.setDescripcion(
                descripcionLimpia
        );


        // CONSERVAR PRECIO SI NO SE ENVÍA

        if (precio != null) {

            especialidad.setPrecio(precio);
        }


        // ======================================
        // GUARDAR MODIFICACIONES
        // ======================================

        Especialidad actualizada =
                especialidadRepository
                        .saveAndFlush(especialidad);


        // ======================================
        // REGISTRAR AUDITORÍA
        // ======================================

        auditoriaService.registrarActualizacion(
                actualizada,
                administrador,
                datosAnteriores
        );


        return actualizada;
    }


    // ==========================================
    // CU-14
    // DESACTIVACIÓN LÓGICA CON AUDITORÍA
    // ==========================================

    @Transactional
    @CacheEvict(
            cacheNames = {
                    "especialidades",
                    "especialidadesAdmin",
                    "especialidadesPorSucursal"
            },
            allEntries = true
    )
    public void desactivarEspecialidad(

            Long id,
            String ejecutadoPor

    ) {

        String administrador =
                validarAdministrador(ejecutadoPor);

        Especialidad especialidad =
                buscarPorId(id);


        // ======================================
        // VERIFICAR ESTADO ACTUAL
        // ======================================

        if (!Boolean.TRUE.equals(
                especialidad.getActivo()
        )) {

            throw new IllegalStateException(
                    "La especialidad ya está inactiva."
            );
        }


        // ======================================
        // CAPTURAR DATOS ANTERIORES
        // ======================================

        String datosAnteriores =
                auditoriaService.capturarDatos(
                        especialidad
                );


        // ======================================
        // ELIMINACIÓN LÓGICA
        // ======================================

        especialidad.setActivo(false);


        // ======================================
        // GUARDAR CAMBIO
        // ======================================

        Especialidad actualizada =
                especialidadRepository
                        .saveAndFlush(especialidad);


        // ======================================
        // REGISTRAR AUDITORÍA
        // ======================================

        auditoriaService.registrarDesactivacion(
                actualizada,
                administrador,
                datosAnteriores
        );
    }


    // ==========================================
    // CU-14
    // REACTIVAR ESPECIALIDAD CON AUDITORÍA
    // ==========================================

    @Transactional
    @CacheEvict(
            cacheNames = {
                    "especialidades",
                    "especialidadesAdmin",
                    "especialidadesPorSucursal"
            },
            allEntries = true
    )
    public void reactivarEspecialidad(

            Long id,
            String ejecutadoPor

    ) {

        String administrador =
                validarAdministrador(ejecutadoPor);

        Especialidad especialidad =
                buscarPorId(id);


        // ======================================
        // VERIFICAR SI YA ESTÁ ACTIVA
        // ======================================

        if (Boolean.TRUE.equals(
                especialidad.getActivo()
        )) {

            return;
        }


        // ======================================
        // CAPTURAR DATOS ANTERIORES
        // ======================================

        String datosAnteriores =
                auditoriaService.capturarDatos(
                        especialidad
                );


        // ======================================
        // VALIDAR NOMBRE DUPLICADO ACTIVO
        // ======================================

        boolean existe =
                especialidadRepository
                        .existsByNombreIgnoreCaseAndActivoTrueAndIdNot(
                                especialidad.getNombre(),
                                id
                        );

        if (existe) {

            throw new IllegalStateException(
                    "Ya existe una especialidad activa con este nombre."
            );
        }


        // ======================================
        // REACTIVAR
        // ======================================

        especialidad.setActivo(true);


        // ======================================
        // GUARDAR EN POSTGRESQL
        // ======================================

        Especialidad actualizada =
                especialidadRepository
                        .saveAndFlush(especialidad);


        // ======================================
        // REGISTRAR AUDITORÍA
        // ======================================

        auditoriaService.registrarReactivacion(
                actualizada,
                administrador,
                datosAnteriores
        );
    }
}
