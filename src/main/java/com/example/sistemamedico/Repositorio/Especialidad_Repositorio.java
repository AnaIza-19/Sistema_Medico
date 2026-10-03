package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Especialidad_Repositorio extends JpaRepository<Especialidad, Long> {
    @org.springframework.data.jpa.repository.Query("""
        SELECT e
        FROM Especialidad e
        JOIN SucursalEspecialidad se
            ON se.especialidad.id = e.id
        WHERE se.sucursal.id = :sucursalId
        AND se.activo = true
        AND e.activo = true
        ORDER BY e.nombre ASC
        """)
    List<Especialidad> findEspecialidadesActivasPorSucursal(
            @org.springframework.data.repository.query.Param("sucursalId")
            Long sucursalId
    );

    List<Especialidad> findByActivoTrue();

}