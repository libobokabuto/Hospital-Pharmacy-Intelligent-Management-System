package com.hpims.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 入库记录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockInResponse {
    private Long id;
    private Long medicineId;
    private String medicineName; // 药品名称（关联查询）
    private String batchNumber;
    private Integer quantity;
    private String supplier;
    private LocalDate inDate;
    private String operator;
    private LocalDateTime createTime;
}

