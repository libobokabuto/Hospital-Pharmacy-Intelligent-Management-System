package com.hpims.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 审核记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "audit_record")
public class AuditRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "处方ID不能为空")
    @Column(name = "prescription_id", nullable = false)
    private Long prescriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", insertable = false, updatable = false)
    private Prescription prescription;

    @Column(name = "audit_type", length = 20)
    private String auditType; // 自动审核、人工审核

    @NotBlank(message = "审核结果不能为空")
    @Column(name = "audit_result", nullable = false, length = 20)
    private String auditResult; // 通过、拒绝、待审核

    @Column(name = "audit_score", precision = 5, scale = 2)
    private BigDecimal auditScore; // 审核得分，0-100

    @Column(name = "issues_found", columnDefinition = "TEXT")
    private String issuesFound; // 发现的问题

    @Column(columnDefinition = "TEXT")
    private String suggestions; // 建议

    @Column(length = 50)
    private String auditor; // 审核人

    @Column(name = "audit_time")
    private LocalDateTime auditTime;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (auditTime == null) {
            auditTime = LocalDateTime.now();
        }
        if (auditType == null || auditType.isEmpty()) {
            auditType = "自动审核";
        }
    }
}

