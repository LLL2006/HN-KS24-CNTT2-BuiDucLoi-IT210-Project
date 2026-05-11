package com.re.it210project.service.impl;

import com.re.it210project.exception.NotFoundException;
import com.re.it210project.model.dto.AcademicHistoryResponse;
import com.re.it210project.model.entity.AcademicEvaluation;
import com.re.it210project.model.entity.BorrowingDetail;
import com.re.it210project.model.entity.BorrowingRecord;
import com.re.it210project.model.entity.MentoringSession;
import com.re.it210project.repository.AcademicEvaluationRepository;
import com.re.it210project.repository.BorrowingDetailRepository;
import com.re.it210project.repository.BorrowingRecordRepository;
import com.re.it210project.repository.MentoringSessionRepository;
import com.re.it210project.service.AcademicHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicHistoryServiceImpl implements AcademicHistoryService {

    private final MentoringSessionRepository mentoringSessionRepository;
    private final AcademicEvaluationRepository academicEvaluationRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;
    private final BorrowingDetailRepository borrowingDetailRepository;



    @Override
    public List<AcademicHistoryResponse> getStudentHistory(Long studentId) {
        List<MentoringSession> sessions =
                mentoringSessionRepository.findByStudentIdOrderByStartTimeDesc(studentId);

        return sessions.stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private AcademicHistoryResponse toHistoryResponse(MentoringSession session) {

        AcademicEvaluation evaluation =
                academicEvaluationRepository
                        .findBySessionId(session.getId())
                        .orElse(null);

        BorrowingRecord borrowingRecord =
                borrowingRecordRepository
                        .findBySessionId(session.getId())
                        .orElse(null);

        List<AcademicHistoryResponse.BorrowedEquipment> equipments =
                Collections.emptyList();

        if (borrowingRecord != null &&
                borrowingRecord.getDetails() != null) {

            equipments = borrowingRecord.getDetails()
                    .stream()
                    .map(this::toBorrowedEquipment)
                    .toList();
        }

        String lecturerName = session.getLecturer()
                .getUser()
                .getProfile()
                .getFullName();

        String departmentName = session.getLecturer()
                .getDepartment()
                .getName();

        return AcademicHistoryResponse.builder()

                .sessionId(session.getId())

                .lecturerName(lecturerName)

                .departmentName(departmentName)

                .topic(session.getTopic())

                .startTime(session.getStartTime())

                .status(session.getStatus())

                .evaluationContent(
                        evaluation != null
                                ? evaluation.getContent()
                                : null
                )

                .abilityLevel(
                        evaluation != null
                                ? evaluation.getAbilityLevel()
                                : null
                )

                .borrowedEquipments(equipments)

                .build();
    }

    private AcademicHistoryResponse.BorrowedEquipment toBorrowedEquipment(BorrowingDetail detail) {
        return AcademicHistoryResponse.BorrowedEquipment.builder()
                .equipmentName(detail.getEquipment().getName())
                .quantity(detail.getQuantity())
                .build();
    }

    @Override
    public AcademicEvaluation getEvaluationDetail(Long sessionId) {

        return academicEvaluationRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thông tin đánh giá cho buổi tư vấn này."));
    }
}