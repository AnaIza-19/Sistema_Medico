
package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.Especialidad_Repositorio;
import com.example.sistemamedico.Repositorio.SucursalEspecialidad_Repositorio;
import com.example.sistemamedico.Repositorio.Sucursal_Repositorio;

import com.example.sistemamedico.model.Especialidad;
import com.example.sistemamedico.model.Sucursal;
import com.example.sistemamedico.model.SucursalEspecialidad;

import org.springframework.cache.annotation.CacheEvict;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
@Service
public class SucursalEspecialidadService {

    // ==========================================
    // REPOSITORIOS Y AUDITORÍA
    // ==========================================

    private final SucursalEspecialidad_Repositorio
            sucursalEspecialidadRepository;

    private final Sucursal_Repositorio
            sucursalRepository;

    private final Especialidad_Repositorio
            especialidadRepository;

    private final AuditoriaSucursalEspecialidadService
            auditoriaService;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public SucursalEspecialidadService(

            SucursalEspecialidad_Repositorio
                    sucursalEspecialidadRepository,

            Sucursal_Repositorio
                    sucursalRepository,

            Especialidad_Repositorio
                    especialidadRepository,

            AuditoriaSucursalEspecialidadService
                    auditoriaService

    ) {

        this.sucursalEspecialidadRepository =
                sucursalEspecialidadRepository;

        this.sucursalRepository =
                sucursalRepository;

        this.especialidadRepository =
                especialidadRepository;

        this.auditoriaService =
                auditoriaService;
    }


    // ==========================================
    // CU-13
    // LISTADO CON PAGINACIÓN Y BÚSQUEDA POR ID
    // ==========================================

    public Page<SucursalEspecialidad> obtenerAsignaciones(

            String buscar,
            int pagina,
            int tamanio

    ) {


        if (
                tamanio != 3
                        &&
                        tamanio != 10
                        &&
                        tamanio != 25
                        &&
                        tamanio != 50
        ) {
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

        String criterio =
                buscar == null
                        ? ""
                        : buscar.trim();


        // SIN FILTRO

        if (criterio.isEmpty()) {

            return sucursalEspecialidadRepository
                    .findAll(pageable);
        }


        // BÚSQUEDA POR ID

        try {

            Long id =
                    Long.parseLong(criterio);

            Optional<SucursalEspecialidad> asignacion =
                    sucursalEspecialidadRepository
                            .findById(id);

            if (asignacion.isPresent()) {

                return new PageImpl<>(
                        List.of(asignacion.get()),
                        pageable,
                        1
                );
            }

            return Page.empty(pageable);

        } catch (NumberFormatException e) {

            return Page.empty(pageable);
        }
    }


    // ==========================================
    // OBTENER SUCURSALES ACTIVAS
    // ==========================================

    public List<Sucursal> obtenerSucursalesActivas() {

        return sucursalRepository
                .findAll()
                .stream()
                .filter(sucursal ->
                        Boolean.TRUE.equals(
                                sucursal.getActivo()
                        )
                )
                .sorted(
                        Comparator.comparing(
                                Sucursal::getNombre,
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
                .toList();
    }


    // ==========================================
    // OBTENER ESPECIALIDADES ACTIVAS
    // ==========================================

    public List<Especialidad> obtenerEspecialidadesActivas() {

        return especialidadRepository
                .findAll()
                .stream()
                .filter(especialidad ->
                        Boolean.TRUE.equals(
                                especialidad.getActivo()
                        )
                )
                .sorted(
                        Comparator.comparing(
                                Especialidad::getNombre,
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
                .toList();
    }


    // ==========================================
    // CU-13
    // CREAR ASIGNACIÓN CON AUDITORÍA
    // ==========================================

    @Transactional
    @CacheEvict(
            value = "especialidadesPorSucursal",
            key = "#sucursalId"
    )
    public SucursalEspecialidad asignarEspecialidad(

            Long sucursalId,
            Long especialidadId,
            String ejecutadoPor

    ) {

        // VALIDAR ADMINISTRADOR

        if (ejecutadoPor == null ||
                ejecutadoPor.isBlank()) {

            throw new IllegalArgumentException(
                    "Debe identificarse el administrador."
            );
        }


        // VALIDAR SEDE

        if (sucursalId == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar una sede."
            );
        }


        // VALIDAR ESPECIALIDAD

        if (especialidadId == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar una especialidad."
            );
        }


        // BUSCAR SUCURSAL

        Sucursal sucursal =
                sucursalRepository
                        .findById(sucursalId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "La sede seleccionada no existe."
                                )
                        );


        // VALIDAR SUCURSAL ACTIVA

        if (!Boolean.TRUE.equals(
                sucursal.getActivo()
        )) {

            throw new IllegalArgumentException(
                    "La sede seleccionada no está activa."
            );
        }


        // BUSCAR ESPECIALIDAD

        Especialidad especialidad =
                especialidadRepository
                        .findById(especialidadId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "La especialidad seleccionada no existe."
                                )
                        );


        // VALIDAR ESPECIALIDAD ACTIVA

        if (!Boolean.TRUE.equals(
                especialidad.getActivo()
        )) {

            throw new IllegalArgumentException(
                    "La especialidad seleccionada no está activa."
            );
        }


        // VALIDAR ASIGNACIÓN DUPLICADA

        boolean existe =
                sucursalEspecialidadRepository
                        .existsBySucursalIdAndEspecialidadId(
                                sucursalId,
                                especialidadId
                        );

        if (existe) {

            throw new IllegalStateException(
                    "La asignación ya existe."
            );
        }


        // CREAR ASIGNACIÓN

        SucursalEspecialidad asignacion =
                new SucursalEspecialidad();

        asignacion.setSucursal(sucursal);

        asignacion.setEspecialidad(especialidad);

        asignacion.setActivo(true);


        // GUARDAR EN POSTGRESQL

        SucursalEspecialidad guardada =
                sucursalEspecialidadRepository
                        .save(asignacion);


        // ======================================
        // REGISTRAR AUDITORÍA
        // ======================================

        auditoriaService.registrarAsignacion(
                guardada,
                ejecutadoPor
        );


        return guardada;
    }


    // ==========================================
    // CU-13
    // ELIMINAR ASIGNACIÓN CON AUDITORÍA
    // ==========================================

    @Transactional
    @CacheEvict(
            value = "especialidadesPorSucursal",
            allEntries = true
    )
    public void eliminarAsignacion(

            Long id,
            String ejecutadoPor

    ) {

        // VALIDAR ADMINISTRADOR

        if (ejecutadoPor == null ||
                ejecutadoPor.isBlank()) {

            throw new IllegalArgumentException(
                    "Debe identificarse el administrador."
            );
        }


        // BUSCAR ASIGNACIÓN

        SucursalEspecialidad asignacion =
                sucursalEspecialidadRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "La asignación no existe."
                                )
                        );


        // ======================================
        // REGISTRAR AUDITORÍA ANTES DE ELIMINAR
        // ======================================

        auditoriaService.registrarEliminacion(
                asignacion,
                ejecutadoPor
        );


        // ELIMINAR ASIGNACIÓN

        sucursalEspecialidadRepository
                .delete(asignacion);
    }


    // ==========================================
    // BUSCAR ASIGNACIÓN POR ID
    // ==========================================

    public Optional<SucursalEspecialidad>
    buscarPorId(Long id) {

        return sucursalEspecialidadRepository
                .findById(id);
    }
}
