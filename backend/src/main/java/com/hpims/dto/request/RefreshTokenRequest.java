package com.hpims.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 刷新令牌请求DTO
 */
@Data
public class RefreshTokenRequest {
    @NotBlank(message = "令牌不能为空")
    private String token;
}

