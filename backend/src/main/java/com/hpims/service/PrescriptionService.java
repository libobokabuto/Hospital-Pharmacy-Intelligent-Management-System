package com.hpims.service;

import com.hpims.dto.response.AuditResultDto;
import com.hpims.exception.BusinessException;
import com.hpims.exception.PrescriptionNotFoundException;
import com.hpims.model.Medicine;
import com.hpims.model.Prescription;
import com.hpims.model.PrescriptionDetail;
import com.hpims.model.AuditRecord;
import com.hpims.repository.PrescriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 处方业务逻辑服务类
 */
@Service
public class PrescriptionService {

    private static final Logger logger = LoggerFactory.getLogger(PrescriptionService.class);

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PrescriptionDetailService prescriptionDetailService;

    @Autowired
    private AuditRecordService auditRecordService;

    @Autowired
    private StockOutService stockOutService;

    @Autowired
    private AuditServiceClient auditServiceClient;

    @Autowired
    private MedicineService medicineService;

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
                throw new BusinessException("PRESCRIPTION_NUMBER_EXISTS", "处方号已存在: " + prescription.getPrescriptionNumber());
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
        logger.info("处方主表保存成功，ID: {}, 处方号: {}", savedPrescription.getId(), savedPrescription.getPrescriptionNumber());

        // 保存处方明细
        if (details != null && !details.isEmpty()) {
            logger.info("开始保存处方明细，数量: {}", details.size());
            for (PrescriptionDetail detail : details) {
                // 验证明细的有效性
                if (detail.getMedicineId() == null) {
                    throw new BusinessException("INVALID_PRESCRIPTION_DETAIL", 
                            "处方明细无效：药品ID不能为空");
                }
                if (detail.getQuantity() == null || detail.getQuantity() <= 0) {
                    throw new BusinessException("INVALID_PRESCRIPTION_DETAIL", 
                            "处方明细无效：药品数量必须大于0");
                }
                detail.setPrescriptionId(savedPrescription.getId());
                logger.debug("设置处方明细处方ID: detail.medicineId={}, prescriptionId={}", 
                        detail.getMedicineId(), savedPrescription.getId());
            }
            List<PrescriptionDetail> savedDetails = prescriptionDetailService.saveAll(details);
            if (savedDetails == null || savedDetails.isEmpty()) {
                throw new BusinessException("PRESCRIPTION_DETAILS_SAVE_FAILED", 
                        "处方明细保存失败，请检查数据是否有效");
            }
            logger.info("处方明细保存成功，实际保存数量: {}", savedDetails.size());
        } else {
            logger.warn("处方明细为空，未保存明细");
            throw new BusinessException("PRESCRIPTION_DETAILS_EMPTY", 
                    "处方明细不能为空，请至少添加一个药品明细");
        }

        return savedPrescription;
    }

    /**
     * 根据ID查询处方
     */
    public Prescription findById(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new PrescriptionNotFoundException(id));
    }

    /**
     * 根据处方号查询处方
     */
    public Prescription findByPrescriptionNumber(String prescriptionNumber) {
        return prescriptionRepository.findByPrescriptionNumber(prescriptionNumber)
                .orElseThrow(() -> new PrescriptionNotFoundException("prescriptionNumber", prescriptionNumber));
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
     * 集成Python审核服务，完成以下功能：
     * 1. 调用AuditServiceClient调用Python服务
     * 2. 将处方数据转换为Python服务需要的格式
     * 3. 处理Python服务返回的审核结果
     * 4. 保存审核记录并更新处方状态
     */
    @Transactional
    public void submitForAudit(Long prescriptionId) {
        Prescription prescription = findById(prescriptionId);

        // 检查处方状态
        if (!"未审核".equals(prescription.getStatus())) {
            throw new BusinessException("INVALID_PRESCRIPTION_STATUS", "只能提交未审核状态的处方进行审核，当前状态: " + prescription.getStatus());
        }

        // 检查Python服务是否可用
        if (!auditServiceClient.isServiceAvailable()) {
            logger.warn("Python审核服务不可用，无法进行自动审核");
            throw new BusinessException("AUDIT_SERVICE_UNAVAILABLE", "Python审核服务不可用，请稍后重试");
        }

        // 更新处方状态为"审核中"
        prescription.setStatus("审核中");
        prescription.setUpdateTime(LocalDateTime.now());
        prescriptionRepository.save(prescription);

        try {
            // 获取处方明细
            List<PrescriptionDetail> details = prescriptionDetailService.findByPrescriptionId(prescriptionId);
            if (details == null || details.isEmpty()) {
                throw new BusinessException("PRESCRIPTION_DETAILS_EMPTY", "处方明细为空，无法进行审核");
            }

            // 获取药品信息
            List<Medicine> medicines = new ArrayList<>();
            for (PrescriptionDetail detail : details) {
                try {
                    Medicine medicine = medicineService.findById(detail.getMedicineId());
                    medicines.add(medicine);
                } catch (Exception e) {
                    logger.warn("药品ID {} 不存在，跳过: {}", detail.getMedicineId(), e.getMessage());
                    medicines.add(null); // 保持索引对应
                }
            }

            // 调用Python审核服务
            logger.info("开始调用Python审核服务，处方ID: {}", prescriptionId);
            AuditResultDto auditResult = auditServiceClient.auditPrescription(prescription, details, medicines);

            // 将Python返回的结果转换为中文状态
            String auditResultStatus = convertAuditResultToStatus(auditResult.getResult());
            String prescriptionStatus = convertAuditResultToPrescriptionStatus(auditResult.getResult());

            // 构建问题描述
            String issuesFound = buildIssuesDescription(auditResult.getIssues());

            // 保存审核记录
            AuditRecord auditRecord = AuditRecord.builder()
                    .prescriptionId(prescriptionId)
                    .auditType("自动审核")
                    .auditResult(auditResultStatus)
                    .auditScore(auditResult.getScore() != null 
                            ? BigDecimal.valueOf(auditResult.getScore()) 
                            : null)
                    .issuesFound(issuesFound)
                    .suggestions(auditResult.getSuggestions())
                    .auditor("Python审核服务")
                    .auditTime(LocalDateTime.now())
                    .build();
            auditRecordService.save(auditRecord);

            // 更新处方状态和审核结果
            prescription.setStatus(prescriptionStatus);
            prescription.setAuditResult(auditResultStatus);
            prescription.setAuditTime(LocalDateTime.now());
            prescription.setUpdateTime(LocalDateTime.now());
            prescriptionRepository.save(prescription);

            logger.info("审核完成，处方ID: {}, 结果: {}, 得分: {}", 
                    prescriptionId, auditResultStatus, auditResult.getScore());

        } catch (BusinessException e) {
            // 如果审核失败，将状态回退为"未审核"
            prescription.setStatus("未审核");
            prescription.setUpdateTime(LocalDateTime.now());
            prescriptionRepository.save(prescription);
            throw e;
        } catch (RuntimeException e) {
            // 如果审核失败，将状态回退为"未审核"
            prescription.setStatus("未审核");
            prescription.setUpdateTime(LocalDateTime.now());
            prescriptionRepository.save(prescription);
            throw new BusinessException("AUDIT_FAILED", "审核失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将Python返回的审核结果转换为中文状态
     * PASS -> 通过, WARNING -> 警告, REJECT -> 拒绝
     */
    private String convertAuditResultToStatus(String result) {
        if (result == null) {
            return "待审核";
        }
        switch (result.toUpperCase()) {
            case "PASS":
                return "通过";
            case "WARNING":
                return "警告";
            case "REJECT":
                return "拒绝";
            default:
                return "待审核";
        }
    }

    /**
     * 将审核结果转换为处方状态
     * PASS -> 已通过, WARNING -> 已通过（警告）, REJECT -> 已拒绝
     */
    private String convertAuditResultToPrescriptionStatus(String result) {
        if (result == null) {
            return "审核中";
        }
        switch (result.toUpperCase()) {
            case "PASS":
            case "WARNING":
                return "已通过";
            case "REJECT":
                return "已拒绝";
            default:
                return "审核中";
        }
    }

    /**
     * 构建问题描述字符串
     */
    private String buildIssuesDescription(List<AuditResultDto.AuditIssueDto> issues) {
        if (issues == null || issues.isEmpty()) {
            return "未发现问题";
        }

        return issues.stream()
                .map(issue -> String.format("[%s] %s: %s", 
                        issue.getSeverity(), 
                        issue.getIssueType(), 
                        issue.getDescription()))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 发药（更新状态，扣减库存）
     */
    @Transactional
    public void dispense(Long prescriptionId) {
        Prescription prescription = findById(prescriptionId);
        logger.info("开始发药，处方ID: {}, 处方号: {}, 当前状态: {}", 
                prescriptionId, prescription.getPrescriptionNumber(), prescription.getStatus());

        // 检查处方状态
        if (!"已通过".equals(prescription.getStatus())) {
            throw new BusinessException("INVALID_PRESCRIPTION_STATUS", "只能对已通过审核的处方进行发药，当前状态: " + prescription.getStatus());
        }

        // 获取处方明细
        List<PrescriptionDetail> details = prescriptionDetailService.findByPrescriptionId(prescriptionId);
        logger.info("查询到处方明细数量: {}", details != null ? details.size() : 0);
        
        if (details == null || details.isEmpty()) {
            logger.error("处方ID {} 的明细为空，无法发药", prescriptionId);
            throw new BusinessException("PRESCRIPTION_DETAILS_EMPTY", 
                    "处方明细为空，无法发药。请检查处方ID: " + prescriptionId + " 是否有对应的处方明细记录。");
        }

        // 扣减库存（通过出库记录）
        for (PrescriptionDetail detail : details) {
            // 创建出库记录
            // 注意：reason 字段在数据库中为 ENUM('prescription', 'loss', 'expired', 'other')
            // 处方发药应使用 'prescription'
            com.hpims.model.StockOut stockOut = com.hpims.model.StockOut.builder()
                    .medicineId(detail.getMedicineId())
                    .quantity(detail.getQuantity())
                    .outDate(java.time.LocalDate.now())
                    .reason("prescription") // 使用ENUM值
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
     * 格式：RX + 8位日期(yyyyMMdd) + 3位序号
     * 示例：RX20250101001
     */
    private String generatePrescriptionNumber() {
        // 格式: RX + 日期(yyyyMMdd) + 3位序号
        String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "RX" + dateStr;
        
        // 查询当天已生成的处方号
        List<Prescription> todayPrescriptions = prescriptionRepository.findByPrescriptionNumberStartingWith(prefix);
        
        // 找到最大的序号
        int maxSeq = 0;
        for (Prescription p : todayPrescriptions) {
            String num = p.getPrescriptionNumber();
            if (num != null && num.length() == 13 && num.startsWith(prefix)) {
                try {
                    int seq = Integer.parseInt(num.substring(10)); // 提取最后3位序号
                    if (seq > maxSeq) {
                        maxSeq = seq;
                    }
                } catch (NumberFormatException e) {
                    // 忽略格式不正确的处方号
                }
            }
        }
        
        // 生成下一个序号（3位数字，从001开始）
        int nextSeq = maxSeq + 1;
        String seqStr = String.format("%03d", nextSeq);
        
        return prefix + seqStr;
    }
}

