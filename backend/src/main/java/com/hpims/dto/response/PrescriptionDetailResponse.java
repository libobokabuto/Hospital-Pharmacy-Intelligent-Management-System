package com.hpims.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 处方明细响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDetailResponse {
    private Long id;
    private Long prescriptionId;
    private Long medicineId;
    private String medicineName; // 药品名称（关联查询）
    private Integer quantity;
    private String dosage; // 用法用量
    private String frequency; // 频次
    private Integer days; // 用药天数
    private LocalDateTime createTime;
}

