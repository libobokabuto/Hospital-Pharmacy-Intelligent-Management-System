package com.hpims.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 入库记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "stock_in")
public class StockIn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "药品ID不能为空")
    @Column(name = "medicine_id", nullable = false)
    private Long medicineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", insertable = false, updatable = false)
    @JsonIgnore
    private Medicine medicine;

    @Size(max = 50, message = "批次号长度不能超过50个字符")
    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @NotNull(message = "入库数量不能为空")
    @Min(value = 1, message = "入库数量必须大于0")
    @Column(nullable = false)
    private Integer quantity;

    @Size(max = 100, message = "供应商名称长度不能超过100个字符")
    @Column(length = 100)
    private String supplier;

    @NotNull(message = "入库日期不能为空")
    @Column(name = "in_date", nullable = false)
    private LocalDate inDate;

    @Size(max = 50, message = "操作员姓名长度不能超过50个字符")
    @Column(length = 50)
    private String operator;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}

