package com.hpims.service;

import com.hpims.dto.response.AuditResultDto;
import com.hpims.model.Medicine;
import com.hpims.model.Prescription;
import com.hpims.model.PrescriptionDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Python审核服务HTTP客户端
 * 负责调用Python审核服务的API接口
 */
@Service
public class AuditServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AuditServiceClient.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${audit.service.url:http://localhost:5000}")
    private String auditServiceUrl;

    /**
     * 调用Python审核服务审核处方
     *
     * @param prescription 处方对象
     * @param details 处方明细列表
     * @param medicines 药品信息列表（与details对应）
     * @return 审核结果DTO
     * @throws RestClientException 如果调用服务失败
     */
    public AuditResultDto auditPrescription(Prescription prescription, 
                                           List<PrescriptionDetail> details,
                                           List<Medicine> medicines) {
        try {
            // 构建请求URL
            String url = auditServiceUrl + "/api/prescription/audit";
            logger.info("调用Python审核服务: {}", url);

            // 构建请求体
            Map<String, Object> requestBody = buildRequestPayload(prescription, details, medicines);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // 发送POST请求
            ResponseEntity<AuditResultDto.AuditServiceResponse> response = restTemplate.postForEntity(
                    url, 
                    request, 
                    AuditResultDto.AuditServiceResponse.class
            );

            // 检查响应
            AuditResultDto.AuditServiceResponse serviceResponse = response.getBody();
            if (serviceResponse == null) {
                throw new RuntimeException("Python审核服务返回空响应");
            }

            if (!Boolean.TRUE.equals(serviceResponse.getSuccess())) {
                throw new RuntimeException("Python审核服务返回错误: " + serviceResponse.getMessage());
            }

            AuditResultDto result = serviceResponse.getData();
            logger.info("审核完成，结果: {}, 得分: {}", result.getResult(), result.getScore());
            return result;

        } catch (RestClientException e) {
            logger.error("调用Python审核服务失败: {}", e.getMessage(), e);
            throw new RuntimeException("调用Python审核服务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查Python审核服务是否可用
     *
     * @return true如果服务可用，false否则
     */
    public boolean isServiceAvailable() {
        try {
            String healthUrl = auditServiceUrl + "/health";
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.getForEntity(healthUrl, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            logger.warn("Python审核服务健康检查失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 构建发送给Python服务的请求体
     * 将Java对象转换为Python服务期望的JSON格式
     *
     * @param prescription 处方对象
     * @param details 处方明细列表
     * @param medicines 药品信息列表
     * @return 请求体Map
     */
    private Map<String, Object> buildRequestPayload(Prescription prescription,
                                                   List<PrescriptionDetail> details,
                                                   List<Medicine> medicines) {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> prescriptionData = new HashMap<>();

        // 构建患者信息
        Map<String, Object> patient = new HashMap<>();
        patient.put("name", prescription.getPatientName());
        patient.put("age", prescription.getPatientAge());
        patient.put("gender", prescription.getPatientGender());
        prescriptionData.put("patient", patient);

        // 构建药品列表
        List<Map<String, Object>> medicinesList = new java.util.ArrayList<>();
        for (int i = 0; i < details.size(); i++) {
            PrescriptionDetail detail = details.get(i);
            Medicine medicine = i < medicines.size() ? medicines.get(i) : null;

            Map<String, Object> medicineData = new HashMap<>();
            medicineData.put("name", medicine != null ? medicine.getName() : "未知药品");
            medicineData.put("generic_name", medicine != null ? medicine.getGenericName() : null);
            medicineData.put("quantity", detail.getQuantity());
            medicineData.put("dosage", detail.getDosage());
            medicineData.put("frequency", detail.getFrequency());
            medicineData.put("days", detail.getDays());
            medicineData.put("specification", medicine != null ? medicine.getSpecification() : null);
            medicineData.put("category", medicine != null ? medicine.getCategory() : null);

            medicinesList.add(medicineData);
        }
        prescriptionData.put("medicines", medicinesList);

        // 添加处方其他信息
        prescriptionData.put("prescription_number", prescription.getPrescriptionNumber());
        prescriptionData.put("doctor_name", prescription.getDoctorName());
        prescriptionData.put("department", prescription.getDepartment());
        prescriptionData.put("create_date", prescription.getCreateDate() != null 
                ? prescription.getCreateDate().toString() : null);

        payload.put("prescription", prescriptionData);
        return payload;
    }
}

