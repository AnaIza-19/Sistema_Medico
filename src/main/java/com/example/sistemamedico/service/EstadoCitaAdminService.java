
package com.example.sistemamedico.service;

import com.example.sistemamedico.Repositorio.EstadoCita_Repositorio;
import com.example.sistemamedico.model.EstadoCita;

import jakarta.persistence.EntityManager;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class EstadoCitaAdminService {

    // ==========================================
    // REPOSITORIO, AUDITORÍA Y ENTITY MANAGER
    // ==========================================

    private final EstadoCita_Repositorio estadoRepository;

    private final AuditoriaEstadoCitaService auditoriaService;

    private final EntityManager entityManager;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public EstadoCitaAdminService(
            EstadoCita_Repositorio estadoRepository,
            AuditoriaEstadoCitaService auditoriaService,
            EntityManager entityManager
    ) {

        this.estadoRepository = estadoRepository;
        this.auditoriaService = auditoriaService;
        this.entityManager = entityManager;
    }


    // ==========================================
    // CU-14: LISTAR ESTADOS CON PAGINACIÓN
    // ==========================================

    @Transactional(readOnly = true)
    public Page<EstadoCita> listar(
            String buscar,
            int pagina,
            int tamanio
    ) {

        int cantidad =
                (tamanio == 25 || tamanio == 50)
                        ? tamanio
                        : 10;

        Pageable paginacion = PageRequest.of(
                Math.max(pagina, 0),
                cantidad,
                Sort.by("id").ascending()
        );

        String busqueda =
                buscar == null ? "" : buscar.trim();

        if (busqueda.isBlank()) {

            return estadoRepository.findAll(paginacion);
        }

        return estadoRepository
                .findByNombreContainingIgnoreCase(
                        busqueda,
                        paginacion
                );
    }


    // ==========================================
    // CU-14: OBTENER ESTADO POR ID
    // ==========================================

    @Transactional(readOnly = true)
    public EstadoCita obtener(Long id) {

        return estadoRepository
                .findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Estado de cita no encontrado."
                        )
                );
    }


    // ==========================================
    // CU-14: OBTENER ESTADOS ACTIVOS
    // CON CACHÉ
    // ==========================================

    @Cacheable("estadosCita")
    @Transactional(readOnly = true)
    public List<EstadoCita> obtenerActivos() {

        return estadoRepository
                .findByActivoTrueOrderByIdAsc();
    }


    // ==========================================
    // VALIDAR CÓDIGO
    // ==========================================

    private String validarCodigo(String codigo) {

        String resultado =
                codigo == null
                        ? ""
                        : codigo.trim().toUpperCase(Locale.ROOT);

        if (!resultado.matches("[A-Z][A-Z0-9_]{1,29}")) {

            throw new IllegalArgumentException(
                    "El código debe tener entre 2 y 30 caracteres. " +
                            "Solo se permiten letras A-Z, números y guion bajo. " +
                            "Debe comenzar por una letra."
            );
        }

        return resultado;
    }


    // ==========================================
    // VALIDAR NOMBRE
    // ==========================================

    private String validarNombre(String nombre) {

        String resultado =
                nombre == null ? "" : nombre.trim();

        if (resultado.isBlank() || resultado.length() > 100) {

            throw new IllegalArgumentException(
                    "El nombre es obligatorio y admite hasta 100 caracteres."
            );
        }

        return resultado;
    }


    // ==========================================
    // VALIDAR DESCRIPCIÓN
    // ==========================================

    private String validarDescripcion(String descripcion) {

        String resultado =
                descripcion == null ? "" : descripcion.trim();

        if (resultado.length() > 500) {

            throw new IllegalArgumentException(
                    "La descripción no puede superar 500 caracteres."
            );
        }

        return resultado.isEmpty() ? null : resultado;
    }


    // ==========================================
    // VALIDAR ADMINISTRADOR
    // ==========================================

    private String validarAdministrador(String ejecutadoPor) {

        if (ejecutadoPor == null || ejecutadoPor.isBlank()) {

            throw new IllegalArgumentException(
                    "No se identificó al administrador."
            );
        }

        return ejecutadoPor.trim();
    }


    // ==========================================
    // CU-14: CONTAR CITAS QUE UTILIZAN UN ESTADO
    //
    // CORREGIDO: UTILIZA JPQL
    //
    // NO DEPENDE DEL ESQUEMA public
    // NO MODIFICA Cita.java
    // NO MODIFICA CITAS EXISTENTES
    // ==========================================

    private long contarCitasConEstado(String codigo) {

        Long cantidad = entityManager
                .createQuery(
                        "SELECT COUNT(c) FROM Cita c " +
                                "WHERE UPPER(TRIM(c.estado)) = :codigo",
                        Long.class
                )
                .setParameter(
                        "codigo",
                        codigo.trim().toUpperCase(Locale.ROOT)
                )
                .getSingleResult();

        return cantidad;
    }


    // ==========================================
    // CU-14: CREAR ESTADO DE CITA
    // ==========================================

    @Transactional
    @CacheEvict(
            cacheNames = "estadosCita",
            allEntries = true
    )
    public EstadoCita crear(
            String codigo,
            String nombre,
            String descripcion,
            String ejecutadoPor
    ) {

        String actor =
                validarAdministrador(ejecutadoPor);

        String codigoLimpio =
                validarCodigo(codigo);

        String nombreLimpio =
                validarNombre(nombre);

        String descripcionLimpia =
                validarDescripcion(descripcion);


        // ======================================
        // VALIDAR CÓDIGO DUPLICADO
        // ======================================

        if (estadoRepository
                .existsByCodigoIgnoreCase(codigoLimpio)) {

            throw new IllegalArgumentException(
                    "Ya existe un estado con ese código."
            );
        }


        // ======================================
        // VALIDAR NOMBRE DUPLICADO
        // ======================================

        if (estadoRepository
                .existsByNombreIgnoreCaseAndActivoTrue(nombreLimpio)) {

            throw new IllegalArgumentException(
                    "Ya existe un estado activo con ese nombre."
            );
        }


        // ======================================
        // CREAR NUEVO ESTADO
        // ======================================

        EstadoCita estado = new EstadoCita();

        estado.setCodigo(codigoLimpio);

        estado.setNombre(nombreLimpio);

        estado.setDescripcion(descripcionLimpia);

        estado.setActivo(true);

        // LOS NUEVOS ESTADOS SON PERSONALIZADOS

        estado.setEsSistema(false);


        // ======================================
        // GUARDAR EN POSTGRESQL
        // ======================================

        estadoRepository.saveAndFlush(estado);


        // ======================================
        // REGISTRAR AUDITORÍA
        // ======================================

        auditoriaService.registrar(
                "CREAR",
                estado,
                actor,
                null,
                "Creación de estado de cita"
        );

        return estado;
    }


    // ==========================================
    // CU-14: ACTUALIZAR ESTADO DE CITA
    // ==========================================

    @Transactional
    @CacheEvict(
            cacheNames = "estadosCita",
            allEntries = true
    )
    public EstadoCita actualizar(
            Long id,
            String nombre,
            String descripcion,
            String ejecutadoPor
    ) {

        String actor =
                validarAdministrador(ejecutadoPor);

        EstadoCita estado =
                obtener(id);


        // ======================================
        // CAPTURAR DATOS ANTERIORES
        // ======================================

        String anteriores =
                auditoriaService.capturarDatos(estado);


        // ======================================
        // VALIDAR NUEVOS DATOS
        // ======================================

        String nombreLimpio =
                validarNombre(nombre);

        String descripcionLimpia =
                validarDescripcion(descripcion);


        // ======================================
        // EVITAR DUPLICADOS ENTRE ACTIVOS
        // ======================================

        if (
                Boolean.TRUE.equals(estado.getActivo())
                        &&
                        estadoRepository
                                .existsByNombreIgnoreCaseAndActivoTrueAndIdNot(
                                        nombreLimpio,
                                        id
                                )
        ) {

            throw new IllegalArgumentException(
                    "Ya existe otro estado activo con ese nombre."
            );
        }


        // ======================================
        // ACTUALIZAR NOMBRE Y DESCRIPCIÓN
        //
        // EL CÓDIGO NO SE MODIFICA
        // ======================================

        estado.setNombre(nombreLimpio);

        estado.setDescripcion(descripcionLimpia);


        // ======================================
        // GUARDAR CAMBIOS
        // ======================================

        estadoRepository.saveAndFlush(estado);


        // ======================================
        // REGISTRAR AUDITORÍA
        // ======================================

        auditoriaService.registrar(
                "ACTUALIZAR",
                estado,
                actor,
                anteriores,
                "Actualización de nombre y descripción"
        );

        return estado;
    }


    // ==========================================
    // CU-14: DESACTIVAR ESTADO DE CITA
    //
    // ELIMINACIÓN LÓGICA
    // ==========================================

    @Transactional
    @CacheEvict(
            cacheNames = "estadosCita",
            allEntries = true
    )
    public void desactivar(
            Long id,
            String ejecutadoPor
    ) {

        String actor =
                validarAdministrador(ejecutadoPor);

        EstadoCita estado =
                obtener(id);


        // ======================================
        // PROTEGER ESTADOS INTERNOS DEL SISTEMA
        // ======================================

        if (Boolean.TRUE.equals(estado.getEsSistema())) {

            throw new IllegalArgumentException(
                    "No se puede desactivar un estado interno del sistema."
            );
        }


        // ======================================
        // VALIDAR ESTADO ACTUAL
        // ======================================

        if (!Boolean.TRUE.equals(estado.getActivo())) {

            throw new IllegalArgumentException(
                    "El estado ya está inactivo."
            );
        }


        // ======================================
        // NO DESACTIVAR ESTADOS UTILIZADOS
        // POR CITAS EXISTENTES
        // ======================================

        if (contarCitasConEstado(estado.getCodigo()) > 0) {

            throw new IllegalArgumentException(
                    "No se puede desactivar: existen citas " +
                            "que utilizan este estado."
            );
        }


        // ======================================
        // CAPTURAR DATOS ANTERIORES
        // ======================================

        String anteriores =
                auditoriaService.capturarDatos(estado);


        // ======================================
        // DESACTIVAR REGISTRO
        // ======================================

        estado.setActivo(false);

        estadoRepository.saveAndFlush(estado);


        // ======================================
        // REGISTRAR AUDITORÍA
        // ======================================

        auditoriaService.registrar(
                "DESACTIVAR",
                estado,
                actor,
                anteriores,
                "Desactivación lógica del estado de cita"
        );
    }


    // ==========================================
    // CU-14: REACTIVAR ESTADO DE CITA
    // ==========================================

    @Transactional
    @CacheEvict(
            cacheNames = "estadosCita",
            allEntries = true
    )
    public void reactivar(
            Long id,
            String ejecutadoPor
    ) {

        String actor =
                validarAdministrador(ejecutadoPor);

        EstadoCita estado =
                obtener(id);


        // ======================================
        // VALIDAR ESTADO ACTUAL
        // ======================================

        if (Boolean.TRUE.equals(estado.getActivo())) {

            throw new IllegalArgumentException(
                    "El estado ya está activo."
            );
        }


        // ======================================
        // VALIDAR NOMBRE DUPLICADO
        // ======================================

        if (estadoRepository
                .existsByNombreIgnoreCaseAndActivoTrue(
                        estado.getNombre()
                )) {

            throw new IllegalArgumentException(
                    "Ya existe otro estado activo con ese nombre."
            );
        }


        // ======================================
        // CAPTURAR DATOS ANTERIORES
        // ======================================

        String anteriores =
                auditoriaService.capturarDatos(estado);


        // ======================================
        // REACTIVAR
        // ======================================

        estado.setActivo(true);

        estadoRepository.saveAndFlush(estado);


        // ======================================
        // REGISTRAR AUDITORÍA
        // ======================================

        auditoriaService.registrar(
                "REACTIVAR",
                estado,
                actor,
                anteriores,
                "Reactivación del estado de cita"
        );
    }
}
