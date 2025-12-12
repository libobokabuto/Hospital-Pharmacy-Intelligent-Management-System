package com.hpims.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 处方明细实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "prescription_detail")
public class PrescriptionDetail {
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

    @NotNull(message = "药品ID不能为空")
    @Column(name = "medicine_id", nullable = false)
    private Long medicineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", insertable = false, updatable = false)
    @JsonIgnore
    private Medicine medicine;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    @Column(nullable = false)
    private Integer quantity;

    @Size(max = 100, message = "用法用量长度不能超过100个字符")
    @Column(length = 100)
    private String dosage; // 用法用量，如：每次2粒

    @Size(max = 50, message = "频次长度不能超过50个字符")
    @Column(length = 50)
    private String frequency; // 频次，如：每日3次

    @Min(value = 1, message = "天数必须大于0")
    @Column
    private Integer days; // 用药天数

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}

