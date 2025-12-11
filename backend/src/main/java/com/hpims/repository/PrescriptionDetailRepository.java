package com.hpims.repository;

import com.hpims.model.PrescriptionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 处方明细数据访问接口
 */
@Repository
public interface PrescriptionDetailRepository extends JpaRepository<PrescriptionDetail, Long> {

    /**
     * 根据处方ID查找处方明细列表
     */
    List<PrescriptionDetail> findByPrescriptionId(Long prescriptionId);

    /**
     * 根据药品ID查找处方明细列表
     */
    List<PrescriptionDetail> findByMedicineId(Long medicineId);
}

