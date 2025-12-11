package com.hpims.service;

import com.hpims.model.PrescriptionDetail;
import com.hpims.repository.PrescriptionDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 处方明细业务逻辑服务类
 */
@Service
public class PrescriptionDetailService {

    @Autowired
    private PrescriptionDetailRepository prescriptionDetailRepository;

    /**
     * 根据处方ID查询处方明细
     */
    public List<PrescriptionDetail> findByPrescriptionId(Long prescriptionId) {
        return prescriptionDetailRepository.findByPrescriptionId(prescriptionId);
    }

    /**
     * 保存处方明细
     */
    @Transactional
    public PrescriptionDetail save(PrescriptionDetail detail) {
        return prescriptionDetailRepository.save(detail);
    }

    /**
     * 批量保存处方明细
     */
    @Transactional
    public List<PrescriptionDetail> saveAll(List<PrescriptionDetail> details) {
        return prescriptionDetailRepository.saveAll(details);
    }

    /**
     * 根据处方ID删除处方明细
     */
    @Transactional
    public void deleteByPrescriptionId(Long prescriptionId) {
        List<PrescriptionDetail> details = findByPrescriptionId(prescriptionId);
        prescriptionDetailRepository.deleteAll(details);
    }
}

