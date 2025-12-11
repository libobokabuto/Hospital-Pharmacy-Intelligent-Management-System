package com.hpims.dto.request;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 处方明细请求DTO
 */
@Data
public class PrescriptionDetailRequest {
    @NotNull(message = "药品ID不能为空")
    private Long medicineId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;

    private String dosage; // 用法用量，如：每次2粒

    private String frequency; // 频次，如：每日3次

    @Min(value = 1, message = "天数必须大于0")
    private Integer days; // 用药天数
}

