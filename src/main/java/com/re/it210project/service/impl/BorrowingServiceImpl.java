package com.re.it210project.service.impl;

import com.re.it210project.exception.BadRequestException;
import com.re.it210project.exception.NotFoundException;
import com.re.it210project.model.entity.*;
import com.re.it210project.model.enums.BorrowingStatus;
import com.re.it210project.repository.BorrowingRecordRepository;
import com.re.it210project.repository.EquipmentRepository;
import com.re.it210project.repository.PaymentTransactionRepository;
import com.re.it210project.service.BorrowingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BorrowingServiceImpl implements BorrowingService {
    private final BorrowingRecordRepository borrowingRecordRepository;
    private final EquipmentRepository equipmentRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Override
    public List<BorrowingRecord> findAllPending() {
        return borrowingRecordRepository.findByStatus(BorrowingStatus.PENDING_ALLOCATION);
    }

    @Override
    @Transactional
    public void approveExport(Long recordId) {
        BorrowingRecord record = borrowingRecordRepository.findById(recordId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu mượn"));

        if (record.getStatus() != BorrowingStatus.PENDING_ALLOCATION) {
            throw new BadRequestException("Phiếu này đã được xử lý trước đó");
        }

        for (BorrowingDetail detail : record.getDetails()) {
            Equipment equipment = detail.getEquipment();
            if (equipment.getQuantity() < detail.getQuantity()) {
                throw new BadRequestException("Thiết bị [" + equipment.getName() + "] không đủ tồn kho");
            }
        }

        for (BorrowingDetail detail : record.getDetails()) {
            Equipment equipment = detail.getEquipment();
            equipment.setQuantity(equipment.getQuantity() - detail.getQuantity());
            equipmentRepository.save(equipment);
        }

        record.setStatus(BorrowingStatus.EXPORTED);
        record.setExportedAt(LocalDateTime.now());
        borrowingRecordRepository.save(record);
    }

    @Override
    @Transactional
    public void returnEquipment(Long recordId) {
        BorrowingRecord record = borrowingRecordRepository.findById(recordId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu mượn"));

        if (record.getStatus() != BorrowingStatus.EXPORTED) {
            throw new BadRequestException("Phiếu chưa xuất kho nên không thể trả");
        }

        for (BorrowingDetail detail : record.getDetails()) {
            Equipment equipment = detail.getEquipment();
            equipment.setQuantity(equipment.getQuantity() + detail.getQuantity());
            equipmentRepository.save(equipment);
        }

        record.setStatus(BorrowingStatus.RETURNED);
        record.setActualReturnTime(LocalDateTime.now());
        borrowingRecordRepository.save(record);
    }

    @Override
    @Transactional
    public BorrowingRecord createBorrowing(MentoringSession session, List<BorrowingDetail> details) {
        boolean requiresDeposit = details.stream()
                .anyMatch(detail -> Boolean.TRUE.equals(detail.getEquipment().getRequiresDeposit()));

        BorrowingRecord borrowing = new BorrowingRecord();
        borrowing.setSession(session);
        borrowing.setCreatedAt(LocalDateTime.now());
        borrowing.setDetails(details);
        borrowing.setDueReturnTime(LocalDate.now().atTime(23, 59));

        if (requiresDeposit) {
            double totalDeposit = details.stream()
                    .mapToDouble(detail -> detail.getEquipment().getDepositAmount()).sum();
            borrowing.setDepositAmount(totalDeposit);
            borrowing.setStatus(BorrowingStatus.PENDING_PAYMENT);
        } else {
            borrowing.setStatus(BorrowingStatus.PENDING_ALLOCATION);
        }

        BorrowingRecord savedBorrowing = borrowingRecordRepository.save(borrowing);

        if (requiresDeposit) {
            PaymentTransaction transaction = PaymentTransaction.builder()
                    .transactionRef(UUID.randomUUID().toString())
                    .amount(savedBorrowing.getDepositAmount())
                    .paymentProvider("VNPAY")
                    .createdAt(LocalDateTime.now())
                    .borrowingRecord(savedBorrowing)
                    .build();
            paymentTransactionRepository.save(transaction);
        }
        return savedBorrowing;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processOverdueBorrowings() {
        List<BorrowingRecord> borrowings = borrowingRecordRepository.findAll();
        for (BorrowingRecord borrowing : borrowings) {
            boolean overdue = borrowing.getStatus() == BorrowingStatus.EXPORTED
                    && borrowing.getActualReturnTime() == null
                    && borrowing.getDueReturnTime() != null
                    && borrowing.getDueReturnTime().isBefore(LocalDateTime.now());

            if (overdue) {
                borrowing.setStatus(BorrowingStatus.OVERDUE);
                borrowing.setPenaltyAmount(borrowing.getDepositAmount());
                borrowingRecordRepository.save(borrowing);
            }
        }
    }

    @Override
    public BorrowingRecord findById(Long id) {
        return borrowingRecordRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu mượn"));
    }

    @Override
    @Transactional
    public void rejectExport(Long recordId) {
        BorrowingRecord record = borrowingRecordRepository.findById(recordId).orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu mượn"));
        if (record.getStatus() != BorrowingStatus.PENDING_ALLOCATION) {
            throw new BadRequestException("Phiếu này đã được xử lý trước đó");
        }
        record.setStatus(BorrowingStatus.REJECTED);
        borrowingRecordRepository.save(record);
    }

    @Override
    public List<BorrowingRecord> findAll() {
        return borrowingRecordRepository.findAllByOrderByCreatedAtDesc();
    }
}