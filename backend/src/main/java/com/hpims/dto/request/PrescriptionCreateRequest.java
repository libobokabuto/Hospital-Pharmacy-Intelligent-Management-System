package com.hpims.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * 创建处方请求DTO
 */
@Data
public class PrescriptionCreateRequest {
    @NotBlank(message = "患者姓名不能为空")
    private String patientName;

    private Integer patientAge;

    private String patientGender;

    private String doctorName;

    private String department;

    private String status; // 处方状态，默认"未审核"

    @NotNull(message = "处方日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createDate;

    @NotNull(message = "处方明细不能为空")
    @Size(min = 1, message = "至少需要一条处方明细")
    private List<@Valid PrescriptionDetailRequest> details;
}

