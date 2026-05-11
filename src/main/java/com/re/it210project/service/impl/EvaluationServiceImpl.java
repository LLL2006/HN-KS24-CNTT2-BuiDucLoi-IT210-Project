package com.re.it210project.service.impl;

import com.re.it210project.exception.BadRequestException;
import com.re.it210project.exception.NotFoundException;
import com.re.it210project.model.dto.EvaluationRequest;
import com.re.it210project.model.entity.*;
import com.re.it210project.model.enums.BorrowingStatus;
import com.re.it210project.model.enums.SessionStatus;
import com.re.it210project.repository.*;
import com.re.it210project.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl implements EvaluationService {
    private final MentoringSessionRepository mentoringSessionRepository;
    private final AcademicEvaluationRepository academicEvaluationRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;
    private final EquipmentRepository equipmentRepository;

    @Override
    @Transactional
    public void completeEvaluation(EvaluationRequest request, Long lecturerId) {
        MentoringSession session = mentoringSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy lịch tư vấn"));

        if (!session.getLecturer().getUserId().equals(lecturerId)) {
            throw new BadRequestException("Bạn không có quyền đánh giá lịch này");
        }

        if (session.getStatus() != SessionStatus.CONFIRMED) {
            throw new BadRequestException("Chỉ có thể đánh giá lịch đã được tiếp nhận");
        }

        session.setStatus(SessionStatus.COMPLETED);
        mentoringSessionRepository.save(session);

        AcademicEvaluation evaluation = AcademicEvaluation.builder()
                .session(session)
                .content(request.getContent())
                .abilityLevel(request.getAbilityLevel())
                .createdAt(LocalDateTime.now())
                .build();
        academicEvaluationRepository.save(evaluation);

        if (request.getEquipments() != null && !request.getEquipments().isEmpty()) {
            double totalDeposit = 0;
            BorrowingRecord borrowingRecord = BorrowingRecord.builder()
                    .session(session)
                    .status(BorrowingStatus.PENDING_ALLOCATION)
                    .createdAt(LocalDateTime.now())
                    .details(new ArrayList<>())
                    .build();

            for (EvaluationRequest.EquipmentItem item : request.getEquipments()) {
                if (item.getQuantity() == null || item.getQuantity() <= 0) continue;

                Equipment equipment = equipmentRepository.findById(item.getEquipmentId())
                        .orElseThrow(() -> new NotFoundException("Không tìm thấy thiết bị"));

                if (equipment.getDepositAmount() != null) {
                    totalDeposit += (equipment.getDepositAmount() * item.getQuantity());
                }

                BorrowingDetail detail = BorrowingDetail.builder()
                        .equipment(equipment)
                        .quantity(item.getQuantity())
                        .borrowingRecord(borrowingRecord)
                        .build();
                borrowingRecord.getDetails().add(detail);
            }

            borrowingRecord.setDepositAmount(totalDeposit);
            borrowingRecordRepository.save(borrowingRecord);
        }
    }
}