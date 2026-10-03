package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.Usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface Usuario_Repositorio
        extends JpaRepository<Usuario, Long> {

    // =========================
    // MÉTODOS QUE YA USAMOS
    // =========================

    Optional<Usuario> findByDpi(String dpi);

    Optional<Usuario> findByNombreUsuarioIgnoreCase(String nombreUsuario);

    boolean existsByDpi(String dpi);


    // =========================
    // BÚSQUEDAS PARA CU-01
    // =========================

    // Buscar por nombre
    Page<Usuario> findByNombreContainingIgnoreCase(
            String nombre,
            Pageable pageable
    );

    // Buscar por correo
    Page<Usuario> findByCorreoContainingIgnoreCase(
            String correo,
            Pageable pageable
    );

    // Buscar por nombre de usuario
    Page<Usuario> findByNombreUsuarioContainingIgnoreCase(
            String nombreUsuario,
            Pageable pageable
    );

    // Buscar por DPI
    Page<Usuario> findByDpiContaining(
            String dpi,
            Pageable pageable
    );

    // Buscar por rol
    Page<Usuario> findByRolNombreContainingIgnoreCase(
            String rol,
            Pageable pageable
    );


    boolean existsByNombreUsuarioIgnoreCase(
            String nombreUsuario
    );

    boolean existsByCorreoIgnoreCase(
            String correo
    );


    // =========================
    // CU-03
    // MÉDICOS POR SUCURSAL
    // Y ESPECIALIDAD
    // =========================

    List<Usuario>
    findByRolNombreAndSucursalIdAndEspecialidadIdAndActivoTrueOrderByNombreAsc(
            String rolNombre,
            Long sucursalId,
            Long especialidadId
    );

}







