package com.hpims.dto.request;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 入库请求DTO
 */
@Data
public class StockInRequest {
    @NotNull(message = "药品ID不能为空")
    private Long medicineId;

    private String batchNumber;

    @NotNull(message = "入库数量不能为空")
    @Min(value = 1, message = "入库数量必须大于0")
    private Integer quantity;

    private String supplier;

    @NotNull(message = "入库日期不能为空")
    private LocalDate inDate;

    private String operator;
}

