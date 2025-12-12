package com.hpims.dto.request;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 更新药品请求DTO
 */
@Data
public class MedicineUpdateRequest {
    @NotBlank(message = "药品名称不能为空")
    private String name;

    private String genericName;

    private String specification;

    private String manufacturer;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.0", message = "价格不能为负数")
    private BigDecimal price;

    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存数量不能为负数")
    private Integer stockQuantity;

    @Min(value = 0, message = "最小库存不能为负数")
    private Integer minStock;

    private String category;

    private String approvalNumber;

    private String indication; // 适应症

    private String contraindication; // 禁忌症

    private String interactions; // 药物相互作用
}

