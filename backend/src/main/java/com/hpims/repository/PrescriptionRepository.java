package com.hpims.repository;

import com.hpims.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 处方数据访问接口
 */
@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    /**
     * 根据处方号查找处方
     */
    Optional<Prescription> findByPrescriptionNumber(String prescriptionNumber);

    /**
     * 根据状态查找处方列表
     */
    List<Prescription> findByStatus(String status);

    /**
     * 根据患者姓名查找处方列表
     */
    List<Prescription> findByPatientName(String patientName);

    /**
     * 根据医生姓名查找处方列表
     */
    List<Prescription> findByDoctorName(String doctorName);

    /**
     * 根据创建日期范围查找处方列表
     */
    List<Prescription> findByCreateDateBetween(LocalDate startDate, LocalDate endDate);
}

