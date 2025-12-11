package com.hpims.service;

import com.hpims.exception.BusinessException;
import com.hpims.exception.StockInsufficientException;
import com.hpims.model.Medicine;
import com.hpims.model.StockOut;
import com.hpims.repository.StockOutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 出库业务逻辑服务类
 */
@Service
public class StockOutService {

    @Autowired
    private StockOutRepository stockOutRepository;

    @Autowired
    private MedicineService medicineService;

    /**
     * 创建出库记录
     */
    @Transactional
    public StockOut createStockOut(StockOut stockOut) {
        // 验证药品是否存在
        medicineService.findById(stockOut.getMedicineId());
        
        // 设置出库日期（如果未设置）
        if (stockOut.getOutDate() == null) {
            stockOut.setOutDate(LocalDate.now());
        }
        
        // 保存出库记录
        StockOut savedStockOut = stockOutRepository.save(stockOut);
        
        // 自动处理出库（更新库存，检查库存是否充足）
        processStockOut(savedStockOut);
        
        return savedStockOut;
    }

    /**
     * 获取所有出库记录
     */
    public List<StockOut> findAll() {
        return stockOutRepository.findAll();
    }

    /**
     * 按药品ID查询出库记录
     */
    public List<StockOut> findByMedicineId(Long medicineId) {
        return stockOutRepository.findByMedicineId(medicineId);
    }

    /**
     * 按日期范围查询出库记录
     */
    public List<StockOut> findByDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new BusinessException("INVALID_DATE_RANGE", "开始日期和结束日期不能为空");
        }
        if (start.isAfter(end)) {
            throw new BusinessException("INVALID_DATE_RANGE", "开始日期不能晚于结束日期");
        }
        return stockOutRepository.findByOutDateBetween(start, end);
    }

    /**
     * 处理出库（更新库存，检查库存是否充足）
     */
    @Transactional
    public void processStockOut(StockOut stockOut) {
        if (stockOut == null || stockOut.getMedicineId() == null || stockOut.getQuantity() == null) {
            throw new BusinessException("INCOMPLETE_STOCK_OUT_RECORD", "出库记录信息不完整");
        }
        
        // 检查库存是否充足
        Medicine medicine = medicineService.findById(stockOut.getMedicineId());
        if (medicine.getStockQuantity() < stockOut.getQuantity()) {
            throw new StockInsufficientException(
                medicine.getName(),
                medicine.getStockQuantity(),
                stockOut.getQuantity()
            );
        }
        
        // 更新药品库存（减少）
        medicineService.updateStock(stockOut.getMedicineId(), -stockOut.getQuantity());
    }
}

