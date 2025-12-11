package com.hpims.exception;

/**
 * 药品不存在异常
 */
public class MedicineNotFoundException extends BusinessException {
    
    public MedicineNotFoundException(String message) {
        super("MEDICINE_NOT_FOUND", message);
    }
    
    public MedicineNotFoundException(Long id) {
        super("MEDICINE_NOT_FOUND", "药品不存在，ID: " + id);
    }
    
    public MedicineNotFoundException(String field, String value) {
        super("MEDICINE_NOT_FOUND", String.format("药品不存在，%s: %s", field, value));
    }
}

