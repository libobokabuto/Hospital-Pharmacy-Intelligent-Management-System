package com.hpims.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 处方响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionResponse {
    private Long id;
    private String prescriptionNumber;
    private String patientName;
    private Integer patientAge;
    private String patientGender;
    private String doctorName;
    private String department;
    private LocalDate createDate;
    private String status; // 未审核、审核中、已通过、已拒绝、已发药、已取消
    private String auditResult;
    private LocalDateTime auditTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<PrescriptionDetailResponse> details; // 处方明细列表
}

