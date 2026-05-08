package com.re.it210project.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
}