package com.re.it210project.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class EquipmentRequest {

    @NotBlank(message = "Tên thiết bị không được để trống")
    @Size(max = 100, message = "Tên thiết bị tối đa 100 ký tự")
    private String name;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng không thể nhỏ hơn 0")
    private Integer quantity;


    private Boolean requiresDeposit;

    @DecimalMin(
            value = "0.0",
            message = "Tiền cọc phải lớn hơn hoặc bằng 0"
    )
    private Double depositAmount;
}