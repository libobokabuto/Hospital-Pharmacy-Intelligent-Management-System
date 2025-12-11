package com.hpims.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 审核记录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditRecordResponse {
    private Long id;
    private Long prescriptionId;
    private String prescriptionNumber; // 处方号（关联查询）
    private String auditType; // 自动审核、人工审核
    private String auditResult; // 通过、拒绝、待审核
    private BigDecimal auditScore; // 审核得分，0-100
    private String issuesFound; // 发现的问题
    private String suggestions; // 建议
    private String auditor; // 审核人
    private LocalDateTime auditTime;
    private LocalDateTime createTime;
}

