package com.hpims.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 出库记录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockOutResponse {
    private Long id;
    private Long medicineId;
    private String medicineName; // 药品名称（关联查询）
    private String batchNumber;
    private Integer quantity;
    private LocalDate outDate;
    private String operator;
    private String reason;
    private LocalDateTime createTime;
}

