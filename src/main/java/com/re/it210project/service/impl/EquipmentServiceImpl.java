package com.re.it210project.service.impl;

import com.re.it210project.exception.NotFoundException;
import com.re.it210project.model.dto.EquipmentRequest;
import com.re.it210project.model.entity.Equipment;
import com.re.it210project.repository.EquipmentRepository;
import com.re.it210project.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;

    @Override
    public List<Equipment> findAll() {
        return equipmentRepository.findAll();
    }

    @Override
    public List<Equipment> findActive() {
        return equipmentRepository.findByActiveTrue();
    }

    @Override
    public Equipment create(EquipmentRequest request) {
        Equipment equipment = Equipment.builder()
                .name(request.getName())
                .description(request.getDescription())
                .quantity(request.getQuantity())
                .active(true)
                .build();

        return equipmentRepository.save(equipment);
    }

    @Override
    public Equipment update(Long id, EquipmentRequest request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thiết bị"));

        equipment.setName(request.getName());
        equipment.setDescription(request.getDescription());
        equipment.setQuantity(request.getQuantity());

        return equipmentRepository.save(equipment);
    }

    @Override
    public void delete(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thiết bị"));

        equipment.setActive(false);
        equipmentRepository.save(equipment);
    }

}