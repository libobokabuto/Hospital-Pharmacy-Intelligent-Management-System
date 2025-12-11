package com.hpims.service;

import com.hpims.model.StockIn;
import com.hpims.repository.StockInRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 入库业务逻辑服务类
 */
@Service
public class StockInService {

    @Autowired
    private StockInRepository stockInRepository;

    @Autowired
    private MedicineService medicineService;

    /**
     * 创建入库记录
     */
    @Transactional
    public StockIn createStockIn(StockIn stockIn) {
        // 验证药品是否存在
        medicineService.findById(stockIn.getMedicineId());
        
        // 设置入库日期（如果未设置）
        if (stockIn.getInDate() == null) {
            stockIn.setInDate(LocalDate.now());
        }
        
        // 保存入库记录
        StockIn savedStockIn = stockInRepository.save(stockIn);
        
        // 自动更新药品库存
        processStockIn(savedStockIn);
        
        return savedStockIn;
    }

    /**
     * 获取所有入库记录
     */
    public List<StockIn> findAll() {
        return stockInRepository.findAll();
    }

    /**
     * 按药品ID查询入库记录
     */
    public List<StockIn> findByMedicineId(Long medicineId) {
        return stockInRepository.findByMedicineId(medicineId);
    }

    /**
     * 按日期范围查询入库记录
     */
    public List<StockIn> findByDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new RuntimeException("开始日期和结束日期不能为空");
        }
        if (start.isAfter(end)) {
            throw new RuntimeException("开始日期不能晚于结束日期");
        }
        return stockInRepository.findByInDateBetween(start, end);
    }

    /**
     * 处理入库（更新库存）
     */
    @Transactional
    public void processStockIn(StockIn stockIn) {
        if (stockIn == null || stockIn.getMedicineId() == null || stockIn.getQuantity() == null) {
            throw new RuntimeException("入库记录信息不完整");
        }
        
        // 更新药品库存（增加）
        medicineService.updateStock(stockIn.getMedicineId(), stockIn.getQuantity());
    }
}

