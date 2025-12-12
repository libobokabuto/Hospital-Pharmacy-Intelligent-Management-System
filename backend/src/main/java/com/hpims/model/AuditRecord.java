package com.hpims.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
    @JsonIgnore
    private Prescription prescription;

    @Size(max = 20, message = "审核类型长度不能超过20个字符")
    @Column(name = "audit_type", columnDefinition = "ENUM('auto','manual')")
    private String auditType; // auto=自动审核, manual=人工审核

    @NotBlank(message = "审核结果不能为空")
    @Column(name = "audit_result", nullable = false, columnDefinition = "ENUM('pass','warning','reject')")
    private String auditResult; // pass=通过, warning=警告, reject=拒绝

    /**
     * JSON序列化时，将ENUM值转换为中文
     */
    @JsonGetter("auditType")
    public String getAuditTypeForJson() {
        if (auditType == null) {
            return null;
        }
        switch (auditType) {
            case "auto":
                return "自动审核";
            case "manual":
                return "人工审核";
            default:
                return auditType;
        }
    }

    /**
     * JSON序列化时，将ENUM值转换为中文
     */
    @JsonGetter("auditResult")
    public String getAuditResultForJson() {
        if (auditResult == null) {
            return null;
        }
        switch (auditResult) {
            case "pass":
                return "通过";
            case "warning":
                return "警告";
            case "reject":
                return "拒绝";
            default:
                return auditResult;
        }
    }

    @DecimalMin(value = "0.0", message = "审核得分不能小于0")
    @DecimalMax(value = "100.0", message = "审核得分不能大于100")
    @Column(name = "audit_score", precision = 5, scale = 2)
    private BigDecimal auditScore; // 审核得分，0-100

    @Column(name = "issues_found", columnDefinition = "TEXT")
    private String issuesFound; // 发现的问题

    @Column(columnDefinition = "TEXT")
    private String suggestions; // 建议

    @Size(max = 50, message = "审核人姓名长度不能超过50个字符")
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
            auditType = "auto"; // 使用ENUM值
        }
    }
}

