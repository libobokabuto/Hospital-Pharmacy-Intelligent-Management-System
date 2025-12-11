package com.hpims.repository;

import com.hpims.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 药品数据访问接口
 */
@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    /**
     * 根据药品名称查找药品
     */
    Optional<Medicine> findByName(String name);

    /**
     * 根据分类查找药品列表
     */
    List<Medicine> findByCategory(String category);

    /**
     * 根据生产厂家查找药品列表
     */
    List<Medicine> findByManufacturer(String manufacturer);

    /**
     * 根据药品名称模糊查询药品列表
     */
    List<Medicine> findByNameContaining(String name);
}

