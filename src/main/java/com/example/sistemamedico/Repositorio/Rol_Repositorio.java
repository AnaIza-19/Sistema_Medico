package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface Rol_Repositorio extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(String nombre);
}