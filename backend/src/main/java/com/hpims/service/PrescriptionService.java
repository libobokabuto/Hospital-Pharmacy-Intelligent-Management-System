package com.hpims.service;

import com.hpims.model.Prescription;
import com.hpims.model.PrescriptionDetail;
import com.hpims.model.AuditRecord;
import com.hpims.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 处方业务逻辑服务类
 */
@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PrescriptionDetailService prescriptionDetailService;

    @Autowired
    private AuditRecordService auditRecordService;

    @Autowired
    private StockOutService stockOutService;

    /**
     * 创建处方（包含明细）
     */
    @Transactional
    public Prescription createPrescription(Prescription prescription, List<PrescriptionDetail> details) {
        // 生成处方号（如果未提供）
        if (prescription.getPrescriptionNumber() == null || prescription.getPrescriptionNumber().trim().isEmpty()) {
            prescription.setPrescriptionNumber(generatePrescriptionNumber());
        } else {
            // 检查处方号是否已存在
            if (prescriptionRepository.findByPrescriptionNumber(prescription.getPrescriptionNumber()).isPresent()) {
                throw new RuntimeException("处方号已存在: " + prescription.getPrescriptionNumber());
            }
        }

        // 设置创建日期（如果未设置）
        if (prescription.getCreateDate() == null) {
            prescription.setCreateDate(java.time.LocalDate.now());
        }

        // 设置初始状态（如果未设置）
        if (prescription.getStatus() == null || prescription.getStatus().trim().isEmpty()) {
            prescription.setStatus("未审核");
        }

        // 保存处方主表
        Prescription savedPrescription = prescriptionRepository.save(prescription);

        // 保存处方明细
        if (details != null && !details.isEmpty()) {
            for (PrescriptionDetail detail : details) {
                detail.setPrescriptionId(savedPrescription.getId());
            }
            prescriptionDetailService.saveAll(details);
        }

        return savedPrescription;
    }

    /**
     * 根据ID查询处方
     */
    public Prescription findById(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("处方不存在，ID: " + id));
    }

    /**
     * 根据处方号查询处方
     */
    public Prescription findByPrescriptionNumber(String prescriptionNumber) {
        return prescriptionRepository.findByPrescriptionNumber(prescriptionNumber)
                .orElseThrow(() -> new RuntimeException("处方不存在，处方号: " + prescriptionNumber));
    }

    /**
     * 获取所有处方
     */
    public List<Prescription> findAll() {
        return prescriptionRepository.findAll();
    }

    /**
     * 按状态查询处方
     */
    public List<Prescription> findByStatus(String status) {
        return prescriptionRepository.findByStatus(status);
    }

    /**
     * 更新处方状态
     */
    @Transactional
    public Prescription updateStatus(Long id, String status) {
        Prescription prescription = findById(id);
        prescription.setStatus(status);
        prescription.setUpdateTime(LocalDateTime.now());
        return prescriptionRepository.save(prescription);
    }

    /**
     * 提交审核（调用Python服务）
     * 
     * TODO: 集成Python审核服务
     * 当前实现为占位符，后续需要：
     * 1. 创建AuditServiceClient调用Python服务
     * 2. 将处方数据转换为Python服务需要的格式
     * 3. 处理Python服务返回的审核结果
     * 4. 保存审核记录
     */
    @Transactional
    public void submitForAudit(Long prescriptionId) {
        Prescription prescription = findById(prescriptionId);

        // 检查处方状态
        if (!"未审核".equals(prescription.getStatus())) {
            throw new RuntimeException("只能提交未审核状态的处方进行审核，当前状态: " + prescription.getStatus());
        }

        // 更新处方状态为"审核中"
        prescription.setStatus("审核中");
        prescription.setUpdateTime(LocalDateTime.now());
        prescriptionRepository.save(prescription);

        // TODO: 调用Python审核服务
        // AuditServiceClient client = ...;
        // AuditResult result = client.auditPrescription(prescription);
        
        // 模拟审核结果（临时实现）
        AuditRecord auditRecord = AuditRecord.builder()
                .prescriptionId(prescriptionId)
                .auditType("自动审核")
                .auditResult("待审核") // 实际应从Python服务获取
                .auditTime(LocalDateTime.now())
                .build();
        
        auditRecordService.save(auditRecord);

        // TODO: 根据Python服务返回的结果更新处方状态
        // if (result.isPassed()) {
        //     updateStatus(prescriptionId, "已通过");
        //     prescription.setAuditResult(result.getResult());
        //     prescription.setAuditTime(LocalDateTime.now());
        // } else {
        //     updateStatus(prescriptionId, "已拒绝");
        //     prescription.setAuditResult(result.getResult());
        //     prescription.setAuditTime(LocalDateTime.now());
        // }
    }

    /**
     * 发药（更新状态，扣减库存）
     */
    @Transactional
    public void dispense(Long prescriptionId) {
        Prescription prescription = findById(prescriptionId);

        // 检查处方状态
        if (!"已通过".equals(prescription.getStatus())) {
            throw new RuntimeException("只能对已通过审核的处方进行发药，当前状态: " + prescription.getStatus());
        }

        // 获取处方明细
        List<PrescriptionDetail> details = prescriptionDetailService.findByPrescriptionId(prescriptionId);
        
        if (details == null || details.isEmpty()) {
            throw new RuntimeException("处方明细为空，无法发药");
        }

        // 扣减库存（通过出库记录）
        for (PrescriptionDetail detail : details) {
            // 创建出库记录
            com.hpims.model.StockOut stockOut = com.hpims.model.StockOut.builder()
                    .medicineId(detail.getMedicineId())
                    .quantity(detail.getQuantity())
                    .outDate(java.time.LocalDate.now())
                    .reason("处方发药 - " + prescription.getPrescriptionNumber())
                    .operator("系统") // 实际应从当前登录用户获取
                    .build();
            
            // 处理出库（会自动检查库存并扣减）
            stockOutService.processStockOut(stockOut);
            
            // 保存出库记录
            stockOutService.createStockOut(stockOut);
        }

        // 更新处方状态为"已发药"
        updateStatus(prescriptionId, "已发药");
    }

    /**
     * 生成处方号
     */
    private String generatePrescriptionNumber() {
        // 格式: P + 日期(yyyyMMdd) + 随机字符串
        String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "P" + dateStr + randomStr;
    }
}

