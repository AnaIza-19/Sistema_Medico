package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.Rol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface Rol_Repositorio extends JpaRepository<Rol, Long> {
    // Método preexistente: NO eliminar, otros casos de uso lo necesitan.
    Optional<Rol> findByNombre(String nombre);

    List<Rol> findByActivoTrue();
    Page<Rol> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
