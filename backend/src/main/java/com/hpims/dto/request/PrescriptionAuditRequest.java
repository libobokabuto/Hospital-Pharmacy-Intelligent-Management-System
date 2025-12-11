package com.hpims.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 处方审核请求DTO
 */
@Data
public class PrescriptionAuditRequest {
    @NotBlank(message = "审核结果不能为空")
    private String auditResult; // 通过、拒绝、待审核

    private BigDecimal auditScore; // 审核得分，0-100

    private String issuesFound; // 发现的问题

    private String suggestions; // 建议

    private String auditor; // 审核人
}

