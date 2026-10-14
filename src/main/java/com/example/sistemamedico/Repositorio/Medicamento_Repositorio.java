package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.Medicamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Medicamento_Repositorio extends JpaRepository<Medicamento, Long> {

    // Método existente: necesario para Farmacia y otros casos de uso.
    List<Medicamento> findByActivoTrueOrderByNombreAsc();

    // CU-14: búsqueda con paginación, incluidos los inactivos.
    Page<Medicamento> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    // La BD actual exige UNIQUE(nombre) aun para registros inactivos.
    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}