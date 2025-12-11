package com.hpims.service;

import com.hpims.model.AuditRecord;
import com.hpims.repository.AuditRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审核记录业务逻辑服务类
 */
@Service
public class AuditRecordService {

    @Autowired
    private AuditRecordRepository auditRecordRepository;

    /**
     * 保存审核记录
     */
    @Transactional
    public AuditRecord save(AuditRecord record) {
        return auditRecordRepository.save(record);
    }

    /**
     * 根据处方ID查询审核历史
     */
    public List<AuditRecord> findByPrescriptionId(Long prescriptionId) {
        return auditRecordRepository.findByPrescriptionId(prescriptionId)
                .stream()
                .sorted(Comparator.comparing(AuditRecord::getAuditTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 查询最新审核记录
     */
    public AuditRecord findLatestByPrescriptionId(Long prescriptionId) {
        List<AuditRecord> records = findByPrescriptionId(prescriptionId);
        return records.isEmpty() ? null : records.get(0);
    }

    /**
     * 根据ID查询审核记录
     */
    public AuditRecord findById(Long id) {
        return auditRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审核记录不存在，ID: " + id));
    }

    /**
     * 获取所有审核记录
     */
    public List<AuditRecord> findAll() {
        return auditRecordRepository.findAll();
    }

    /**
     * 根据审核结果查询审核记录
     */
    public List<AuditRecord> findByAuditResult(String auditResult) {
        return auditRecordRepository.findByAuditResult(auditResult);
    }
}

