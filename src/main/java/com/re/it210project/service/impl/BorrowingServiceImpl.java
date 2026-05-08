package com.re.it210project.service.impl;

import com.re.it210project.exception.BadRequestException;
import com.re.it210project.exception.NotFoundException;
import com.re.it210project.model.entity.BorrowingDetail;
import com.re.it210project.model.entity.BorrowingRecord;
import com.re.it210project.model.entity.Equipment;
import com.re.it210project.model.enums.BorrowingStatus;
import com.re.it210project.repository.BorrowingRecordRepository;
import com.re.it210project.repository.EquipmentRepository;
import com.re.it210project.service.BorrowingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowingServiceImpl implements BorrowingService {

    private final BorrowingRecordRepository borrowingRecordRepository;
    private final EquipmentRepository equipmentRepository;

    @Override
    public List<BorrowingRecord> findAllPending() {
        return borrowingRecordRepository.findByStatus(
                BorrowingStatus.PENDING_ALLOCATION
        );
    }

    @Override
    @Transactional
    public void approveExport(Long recordId) {

        BorrowingRecord record = borrowingRecordRepository.findById(recordId)
                .orElseThrow(() ->
                        new NotFoundException("Không tìm thấy phiếu mượn"));

        if (record.getStatus() != BorrowingStatus.PENDING_ALLOCATION) {
            throw new BadRequestException(
                    "Phiếu này đã được xử lý trước đó"
            );
        }

        for (BorrowingDetail detail : record.getDetails()) {

            Equipment equipment = detail.getEquipment();

            if (equipment.getQuantity() < detail.getQuantity()) {
                throw new BadRequestException(
                        "Thiết bị [" + equipment.getName() + "] không đủ tồn kho"
                );
            }
        }

        for (BorrowingDetail detail : record.getDetails()) {

            Equipment equipment = detail.getEquipment();

            equipment.setQuantity(
                    equipment.getQuantity() - detail.getQuantity()
            );

            equipmentRepository.save(equipment);
        }

        record.setStatus(BorrowingStatus.EXPORTED);
        record.setExportedAt(LocalDateTime.now());

        borrowingRecordRepository.save(record);
    }

    @Override
    public BorrowingRecord findById(Long id) {
        return borrowingRecordRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Không tìm thấy phiếu mượn"));
    }
}