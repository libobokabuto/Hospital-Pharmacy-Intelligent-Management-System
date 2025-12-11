package com.hpims.exception;

/**
 * 库存不足异常
 */
public class StockInsufficientException extends BusinessException {
    
    private Long medicineId;
    private String medicineName;
    private Integer currentStock;
    private Integer requiredStock;
    
    public StockInsufficientException(String message) {
        super("STOCK_INSUFFICIENT", message);
    }
    
    public StockInsufficientException(Long medicineId, Integer currentStock, Integer requiredStock) {
        super("STOCK_INSUFFICIENT", 
            String.format("库存不足，药品ID: %d，当前库存: %d，需要: %d", 
                medicineId, currentStock, requiredStock));
        this.medicineId = medicineId;
        this.currentStock = currentStock;
        this.requiredStock = requiredStock;
    }
    
    public StockInsufficientException(String medicineName, Integer currentStock, Integer requiredStock) {
        super("STOCK_INSUFFICIENT", 
            String.format("库存不足，药品: %s，当前库存: %d，需要: %d", 
                medicineName, currentStock, requiredStock));
        this.medicineName = medicineName;
        this.currentStock = currentStock;
        this.requiredStock = requiredStock;
    }
    
    public Long getMedicineId() {
        return medicineId;
    }
    
    public String getMedicineName() {
        return medicineName;
    }
    
    public Integer getCurrentStock() {
        return currentStock;
    }
    
    public Integer getRequiredStock() {
        return requiredStock;
    }
}

