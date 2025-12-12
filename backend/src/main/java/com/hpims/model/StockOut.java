package com.hpims.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonGetter;
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
 * 出库记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "stock_out")
public class StockOut {
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

    @NotNull(message = "出库数量不能为空")
    @Min(value = 1, message = "出库数量必须大于0")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "出库日期不能为空")
    @Column(name = "out_date", nullable = false)
    private LocalDate outDate;

    @Size(max = 50, message = "操作员姓名长度不能超过50个字符")
    @Column(length = 50)
    private String operator;

    @Column(name = "reason", columnDefinition = "ENUM('prescription', 'loss', 'expired', 'other')")
    private String reason; // prescription=处方发药, loss=盘亏, expired=过期, other=其他

    /**
     * JSON序列化时，将ENUM值转换为中文
     */
    @JsonGetter("reason")
    public String getReasonForJson() {
        if (reason == null) {
            return null;
        }
        switch (reason) {
            case "prescription":
                return "处方发药";
            case "loss":
                return "盘亏";
            case "expired":
                return "过期";
            case "other":
                return "其他";
            default:
                return reason;
        }
    }

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}

