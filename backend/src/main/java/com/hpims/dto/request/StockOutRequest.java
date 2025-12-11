package com.hpims.dto.request;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 出库请求DTO
 */
@Data
public class StockOutRequest {
    @NotNull(message = "药品ID不能为空")
    private Long medicineId;

    private String batchNumber;

    @NotNull(message = "出库数量不能为空")
    @Min(value = 1, message = "出库数量必须大于0")
    private Integer quantity;

    @NotNull(message = "出库日期不能为空")
    private LocalDate outDate;

    private String operator;

    private String reason;
}

