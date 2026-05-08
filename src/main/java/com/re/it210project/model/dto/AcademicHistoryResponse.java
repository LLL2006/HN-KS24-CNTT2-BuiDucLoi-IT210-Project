package com.re.it210project.model.dto;

import com.re.it210project.model.enums.SessionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AcademicHistoryResponse {

    private Long sessionId;

    private String lecturerName;

    private String departmentName;

    private String topic;

    private LocalDateTime startTime;

    private SessionStatus status;

    private String evaluationContent;

    private String abilityLevel;

    private List<BorrowedEquipment> borrowedEquipments;

    @Data
    @Builder
    public static class BorrowedEquipment {

        private String equipmentName;

        private Integer quantity;
    }
}