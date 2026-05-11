package com.re.it210project.repository;

import com.re.it210project.model.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findByActiveTrue();
    @Query("SELECT e FROM Equipment e WHERE e.active = true AND e.quantity < 5")
    List<Equipment> findLowStockEquipments();
}