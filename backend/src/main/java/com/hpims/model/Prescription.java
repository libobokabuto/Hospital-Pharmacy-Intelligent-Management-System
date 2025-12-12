package com.hpims.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.hpims.validation.PrescriptionNumber;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 处方主表实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "prescription")
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 处方号：创建时可以为空（后端会自动生成），更新时不能为空
    @PrescriptionNumber
    @Column(name = "prescription_number", unique = true, nullable = false, length = 50)
    private String prescriptionNumber;

    @NotBlank(message = "患者姓名不能为空")
    @Size(max = 50, message = "患者姓名长度不能超过50个字符")
    @Column(name = "patient_name", nullable = false, length = 50)
    private String patientName;

    @Column(name = "patient_age")
    private Integer patientAge;

    @Size(max = 20, message = "患者性别长度不能超过20个字符")
    @Column(name = "patient_gender", length = 20)
    private String patientGender;

    @Size(max = 50, message = "医生姓名长度不能超过50个字符")
    @Column(name = "doctor_name", length = 50)
    private String doctorName;

    @Size(max = 50, message = "科室名称长度不能超过50个字符")
    @Column(length = 50)
    private String department;

    @NotNull(message = "处方日期不能为空")
    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @NotBlank(message = "处方状态不能为空")
    @Size(max = 20, message = "处方状态长度不能超过20个字符")
    @Column(nullable = false, length = 20)
    private String status; // 未审核、审核中、已通过、已拒绝、已发药、已取消

    @Column(name = "audit_result", columnDefinition = "TEXT")
    private String auditResult;

    @Column(name = "audit_time")
    private LocalDateTime auditTime;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("prescription")
    private List<PrescriptionDetail> details;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (status == null || status.isEmpty()) {
            status = "未审核";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}

