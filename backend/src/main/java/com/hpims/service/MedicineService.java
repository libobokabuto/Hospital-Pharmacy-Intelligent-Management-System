package com.hpims.service;

import com.hpims.exception.MedicineNotFoundException;
import com.hpims.exception.StockInsufficientException;
import com.hpims.model.Medicine;
import com.hpims.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 药品业务逻辑服务类
 */
@Service
public class MedicineService {

    @Autowired
    private MedicineRepository medicineRepository;

    /**
     * 获取所有药品
     */
    public List<Medicine> findAll() {
        return medicineRepository.findAll();
    }

    /**
     * 根据ID查询药品
     */
    public Medicine findById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException(id));
    }

    /**
     * 保存药品
     */
    @Transactional
    public Medicine save(Medicine medicine) {
        return medicineRepository.save(medicine);
    }

    /**
     * 更新药品
     */
    @Transactional
    public Medicine update(Long id, Medicine medicine) {
        Medicine existingMedicine = findById(id);
        
        // 更新字段
        existingMedicine.setName(medicine.getName());
        existingMedicine.setGenericName(medicine.getGenericName());
        existingMedicine.setSpecification(medicine.getSpecification());
        existingMedicine.setManufacturer(medicine.getManufacturer());
        existingMedicine.setPrice(medicine.getPrice());
        existingMedicine.setCategory(medicine.getCategory());
        existingMedicine.setApprovalNumber(medicine.getApprovalNumber());
        existingMedicine.setIndication(medicine.getIndication());
        existingMedicine.setContraindication(medicine.getContraindication());
        existingMedicine.setInteractions(medicine.getInteractions());
        existingMedicine.setMinStock(medicine.getMinStock());
        
        // 注意：更新时不修改库存数量，库存数量应通过updateStock方法更新
        
        return medicineRepository.save(existingMedicine);
    }

    /**
     * 删除药品
     */
    @Transactional
    public void delete(Long id) {
        if (!medicineRepository.existsById(id)) {
            throw new MedicineNotFoundException(id);
        }
        medicineRepository.deleteById(id);
    }

    /**
     * 搜索药品（根据名称模糊查询）
     */
    public List<Medicine> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        return medicineRepository.findByNameContaining(keyword.trim());
    }

    /**
     * 按分类查询药品
     */
    public List<Medicine> findByCategory(String category) {
        return medicineRepository.findByCategory(category);
    }

    /**
     * 更新库存
     */
    @Transactional
    public void updateStock(Long id, Integer quantity) {
        Medicine medicine = findById(id);
        int newStock = medicine.getStockQuantity() + quantity;
        
        if (newStock < 0) {
            throw new StockInsufficientException(
                medicine.getId(),
                medicine.getStockQuantity(),
                Math.abs(quantity)
            );
        }
        
        medicine.setStockQuantity(newStock);
        medicineRepository.save(medicine);
    }
}

