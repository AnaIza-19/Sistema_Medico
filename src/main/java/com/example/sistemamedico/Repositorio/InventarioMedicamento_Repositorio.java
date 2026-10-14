package com.example.sistemamedico.Repositorio;

import com.example.sistemamedico.model.InventarioMedicamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioMedicamento_Repositorio extends JpaRepository<InventarioMedicamento, Long> {
    // CU-11: conservar exactamente las consultas existentes.
    @EntityGraph(attributePaths = "medicamento")
    Optional<InventarioMedicamento> findByMedicamentoIdAndSucursalIdAndActivoTrue(
            Long medicamentoId, Long sucursalId);

    @EntityGraph(attributePaths = "medicamento")
    List<InventarioMedicamento> findBySucursalIdAndActivoTrueOrderByMedicamentoNombreAsc(
            Long sucursalId);

    // CU-14: catálogo por sucursal y medicamento; paginado sin acceso LAZY en Thymeleaf.
    @Query(value = "SELECT i FROM InventarioMedicamento i " +
            "JOIN FETCH i.medicamento m JOIN FETCH i.sucursal s " +
            "WHERE (:buscar = '' OR LOWER(m.nombre) LIKE LOWER(CONCAT('%', :buscar, '%')) " +
            "OR LOWER(s.nombre) LIKE LOWER(CONCAT('%', :buscar, '%')))",
            countQuery = "SELECT COUNT(i) FROM InventarioMedicamento i " +
                    "JOIN i.medicamento m JOIN i.sucursal s " +
                    "WHERE (:buscar = '' OR LOWER(m.nombre) LIKE LOWER(CONCAT('%', :buscar, '%')) " +
                    "OR LOWER(s.nombre) LIKE LOWER(CONCAT('%', :buscar, '%')))" )
    Page<InventarioMedicamento> buscarParaAdmin(@Param("buscar") String buscar, Pageable pageable);

    Optional<InventarioMedicamento> findByMedicamentoIdAndSucursalId(Long medicamentoId, Long sucursalId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventarioMedicamento i WHERE i.id = :id")
    Optional<InventarioMedicamento> bloquearPorId(@Param("id") Long id);

    @EntityGraph(attributePaths = {"medicamento", "sucursal"})
    List<InventarioMedicamento> findByActivoTrueOrderByIdAsc();
}
