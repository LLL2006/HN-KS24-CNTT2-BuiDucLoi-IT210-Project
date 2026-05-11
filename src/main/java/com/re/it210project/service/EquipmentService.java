package com.re.it210project.service;

import com.re.it210project.model.dto.EquipmentRequest;
import com.re.it210project.model.entity.Equipment;

import java.util.List;

public interface EquipmentService {

    List<Equipment> findAll();

    List<Equipment> findActive();

    Equipment create(EquipmentRequest request);

    Equipment update(Long id, EquipmentRequest request);

    void delete(Long id);

}