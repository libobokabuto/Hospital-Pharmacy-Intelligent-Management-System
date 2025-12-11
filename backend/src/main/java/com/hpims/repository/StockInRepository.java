package com.hpims.repository;

import com.hpims.model.StockIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 入库记录数据访问接口
 */
@Repository
public interface StockInRepository extends JpaRepository<StockIn, Long> {

    /**
     * 根据药品ID查找入库记录列表
     */
    List<StockIn> findByMedicineId(Long medicineId);

    /**
     * 根据入库日期范围查找入库记录列表
     */
    List<StockIn> findByInDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 根据操作员查找入库记录列表
     */
    List<StockIn> findByOperator(String operator);
}

