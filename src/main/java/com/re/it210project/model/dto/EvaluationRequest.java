package com.re.it210project.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class EvaluationRequest {

    @NotNull(message = "Không tìm thấy buổi cố vấn")
    private Long sessionId;

    @NotBlank(message = "Nội dung đánh giá không được để trống")
    private String content;

    @NotBlank(message = "Vui lòng chọn mức đánh giá năng lực")
    private String abilityLevel;

    @Valid
    private List<EquipmentItem> equipments;

    @Data
    public static class EquipmentItem {

        @NotNull(message = "Thiết bị không hợp lệ")
        private Long equipmentId;

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng mượn tối thiểu là 1")
        private Integer quantity;
    }
}