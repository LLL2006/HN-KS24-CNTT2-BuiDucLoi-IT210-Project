package com.re.it210project.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BorrowingApprovalRequest {

    @NotNull(message = "Không tìm thấy phiếu mượn cần duyệt")
    private Long borrowingRecordId;
}