package com.hpims.repository;

import com.hpims.model.StockOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 出库记录数据访问接口
 */
@Repository
public interface StockOutRepository extends JpaRepository<StockOut, Long> {

    /**
     * 根据药品ID查找出库记录列表
     */
    List<StockOut> findByMedicineId(Long medicineId);

    /**
     * 根据出库日期范围查找出库记录列表
     */
    List<StockOut> findByOutDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 根据出库原因查找出库记录列表
     */
    List<StockOut> findByReason(String reason);
}

