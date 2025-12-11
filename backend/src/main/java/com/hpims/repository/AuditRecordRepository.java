package com.hpims.repository;

import com.hpims.model.AuditRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审核记录数据访问接口
 */
@Repository
public interface AuditRecordRepository extends JpaRepository<AuditRecord, Long> {

    /**
     * 根据处方ID查找审核记录列表
     */
    List<AuditRecord> findByPrescriptionId(Long prescriptionId);

    /**
     * 根据审核结果查找审核记录列表
     */
    List<AuditRecord> findByAuditResult(String auditResult);

    /**
     * 根据审核时间范围查找审核记录列表
     */
    List<AuditRecord> findByAuditTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
}

