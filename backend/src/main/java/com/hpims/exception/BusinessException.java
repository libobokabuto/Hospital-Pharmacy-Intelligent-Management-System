package com.hpims.exception;

/**
 * 业务异常基类
 * 所有业务异常都应该继承此类
 */
public class BusinessException extends RuntimeException {
    
    private String code;
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
}

