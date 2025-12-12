package com.hpims.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品信息实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "medicine")
public class Medicine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "药品名称不能为空")
    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "generic_name", length = 100)
    private String genericName;

    @Column(length = 50)
    private String specification;

    @Column(length = 100)
    private String manufacturer;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.0", message = "价格不能为负数")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存数量不能为负数")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Min(value = 0, message = "最小库存不能为负数")
    @Column(name = "min_stock")
    private Integer minStock;

    @Column(length = 50)
    private String category;

    @Column(name = "approval_number", length = 50)
    private String approvalNumber;

    @Column(columnDefinition = "TEXT")
    private String indication; // 适应症

    @Column(columnDefinition = "TEXT")
    private String contraindication; // 禁忌症

    @Column(columnDefinition = "TEXT")
    private String interactions; // 药物相互作用

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (stockQuantity == null) {
            stockQuantity = 0;
        }
        if (minStock == null) {
            minStock = 10;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}

