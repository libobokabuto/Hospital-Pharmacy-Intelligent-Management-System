package com.hpims.exception;

/**
 * 处方不存在异常
 */
public class PrescriptionNotFoundException extends BusinessException {
    
    public PrescriptionNotFoundException(String message) {
        super("PRESCRIPTION_NOT_FOUND", message);
    }
    
    public PrescriptionNotFoundException(Long id) {
        super("PRESCRIPTION_NOT_FOUND", "处方不存在，ID: " + id);
    }
    
    public PrescriptionNotFoundException(String field, String value) {
        super("PRESCRIPTION_NOT_FOUND", String.format("处方不存在，%s: %s", field, value));
    }
}

