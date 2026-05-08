package com.re.it210project.model.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class MentoringBookingRequest {

    @NotNull(message = "Vui lòng chọn giảng viên")
    private Long lecturerId;

    @NotNull(message = "Vui lòng chọn ngày học")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "Ngày tư vấn không được là ngày quá khứ")
    private LocalDate sessionDate;

    @NotNull(message = "Vui lòng chọn giờ học")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime sessionTime;

    @NotBlank(message = "Vui lòng nhập chủ đề cần hỗ trợ")
    @Size(max = 255, message = "Chủ đề tối đa 255 ký tự")
    private String topic;
}