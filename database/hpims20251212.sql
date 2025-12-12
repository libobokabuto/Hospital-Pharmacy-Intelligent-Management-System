/*
 Navicat Premium Dump SQL

 Source Server         : himps_connection
 Source Server Type    : MySQL
 Source Server Version : 80020 (8.0.20)
 Source Host           : localhost:3306
 Source Schema         : hpims

 Target Server Type    : MySQL
 Target Server Version : 80020 (8.0.20)
 File Encoding         : 65001

 Date: 12/12/2025 23:16:04
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for audit_record
-- ----------------------------
DROP TABLE IF EXISTS `audit_record`;
CREATE TABLE `audit_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `prescription_id` bigint NOT NULL COMMENT '处方ID',
  `audit_type` enum('auto','manual') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'auto' COMMENT '审核类型',
  `audit_result` enum('pass','warning','reject') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '审核结果',
  `audit_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '审核评分',
  `issues_found` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '发现的问题',
  `suggestions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '建议',
  `auditor` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核员',
  `audit_time` datetime NOT NULL COMMENT '审核时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_prescription_id`(`prescription_id` ASC) USING BTREE,
  INDEX `idx_audit_type`(`audit_type` ASC) USING BTREE,
  INDEX `idx_audit_result`(`audit_result` ASC) USING BTREE,
  INDEX `idx_audit_time`(`audit_time` ASC) USING BTREE,
  CONSTRAINT `audit_record_ibfk_1` FOREIGN KEY (`prescription_id`) REFERENCES `prescription` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '审核记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of audit_record
-- ----------------------------
INSERT INTO `audit_record` VALUES (1, 1, '', '', 95.50, '无明显问题', '建议按时服药', '系统审核', '0000-00-00 00:00:00', '2025-12-10 18:24:11');
INSERT INTO `audit_record` VALUES (2, 2, '', '', 98.00, '剂量合理', '继续治疗', '王药师', '0000-00-00 00:00:00', '2025-12-10 18:24:11');
INSERT INTO `audit_record` VALUES (3, 3, '', '', NULL, '正在审核中', NULL, '系统审核', '0000-00-00 00:00:00', '2025-12-10 18:24:11');
INSERT INTO `audit_record` VALUES (4, 1, '', '', 95.50, '无明显问题', '建议按时服药', '系统审核', '0000-00-00 00:00:00', '2025-12-12 00:21:31');
INSERT INTO `audit_record` VALUES (5, 2, '', '', 98.00, '剂量合理', '继续治疗', '王药师', '0000-00-00 00:00:00', '2025-12-12 00:21:31');
INSERT INTO `audit_record` VALUES (6, 3, '', '', NULL, '正在审核中', NULL, '系统审核', '0000-00-00 00:00:00', '2025-12-12 00:21:31');
INSERT INTO `audit_record` VALUES (7, 12, 'manual', 'pass', 100.00, '', '', 'pharmacist', '2025-12-12 22:14:18', '2025-12-12 22:14:18');

-- ----------------------------
-- Table structure for inventory
-- ----------------------------
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `medicine_id` bigint NOT NULL COMMENT '药品ID',
  `batch_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '批号',
  `quantity` int NOT NULL COMMENT '库存数量',
  `expiry_date` date NULL DEFAULT NULL COMMENT '有效期',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '存放位置',
  `status` enum('normal','expired','depleted') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'normal' COMMENT '状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_medicine_id`(`medicine_id` ASC) USING BTREE,
  INDEX `idx_batch_number`(`batch_number` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_expiry_date`(`expiry_date` ASC) USING BTREE,
  CONSTRAINT `inventory_ibfk_1` FOREIGN KEY (`medicine_id`) REFERENCES `medicine` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '库存表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inventory
-- ----------------------------
INSERT INTO `inventory` VALUES (1, 1, 'ASP20241201', 100, '2026-12-01', 'A01-01', 'normal', '2025-12-10 01:17:41');
INSERT INTO `inventory` VALUES (2, 1, 'ASP20241202', 100, '2026-12-01', 'A01-02', 'normal', '2025-12-10 01:17:41');
INSERT INTO `inventory` VALUES (3, 2, 'IBU20241201', 150, '2026-11-15', 'A02-01', 'normal', '2025-12-10 01:17:41');
INSERT INTO `inventory` VALUES (4, 3, 'AMX20241201', 300, '2026-10-20', 'B01-01', 'normal', '2025-12-10 01:17:41');
INSERT INTO `inventory` VALUES (5, 4, 'DSP20241201', 100, '2026-09-30', 'C01-01', 'normal', '2025-12-10 01:17:41');
INSERT INTO `inventory` VALUES (6, 5, 'VIT20241201', 180, '2027-01-15', 'D01-01', 'normal', '2025-12-10 01:17:41');
INSERT INTO `inventory` VALUES (7, 6, 'GAN20241201', 120, '2026-08-25', 'E01-01', 'normal', '2025-12-10 01:17:41');

-- ----------------------------
-- Table structure for medicine
-- ----------------------------
DROP TABLE IF EXISTS `medicine`;
CREATE TABLE `medicine`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '药品名称',
  `generic_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '通用名',
  `specification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '规格',
  `manufacturer` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '厂家',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '价格',
  `stock_quantity` int NULL DEFAULT 0 COMMENT '库存数量',
  `min_stock` int NULL DEFAULT 0 COMMENT '最低库存预警',
  `category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '药品分类',
  `approval_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '批准文号',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_name`(`name` ASC) USING BTREE,
  INDEX `idx_generic_name`(`generic_name` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '药品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of medicine
-- ----------------------------
INSERT INTO `medicine` VALUES (1, '阿司匹林', '乙酰水杨酸', '100mg*30片', '拜耳医药', 15.50, 199, 20, '解热镇痛药', '国药准字H12345678', '2025-12-10 01:17:41', '2025-12-12 22:22:50');
INSERT INTO `medicine` VALUES (2, '布洛芬', '布洛芬', '200mg*20片', '中美史克', 14.80, 150, 15, '解热镇痛药', '国药准字H23456789', '2025-12-10 01:17:41', '2025-12-12 16:40:51');
INSERT INTO `medicine` VALUES (3, '阿莫西林', '阿莫西林', '250mg*24粒', '哈药集团', 8.90, 300, 30, '抗生素', '国药准字H34567890', '2025-12-10 01:17:41', '2025-12-10 01:17:41');
INSERT INTO `medicine` VALUES (4, '复方丹参片', '丹参等', '24片/盒', '天士力', 18.60, 100, 10, '心血管药', '国药准字H45678901', '2025-12-10 01:17:41', '2025-12-10 01:17:41');
INSERT INTO `medicine` VALUES (5, '维生素C', '抗坏血酸', '100mg*100片', '罗氏制药', 25.00, 180, 18, '维生素', '国药准字H56789012', '2025-12-10 01:17:41', '2025-12-10 01:17:41');
INSERT INTO `medicine` VALUES (6, '感冒灵颗粒', '对乙酰氨基酚等', '10g*12袋', '白云山制药', 16.50, 120, 12, '感冒药', '国药准字H67890123', '2025-12-10 01:17:41', '2025-12-10 01:17:41');
INSERT INTO `medicine` VALUES (7, '阿莫西林胶囊', '阿莫西林', '0.25g*24粒', '哈药集团', 15.50, 100, 20, '抗生素', 'H20051234', '2025-12-10 18:24:11', '2025-12-10 18:24:11');
INSERT INTO `medicine` VALUES (8, '布洛芬缓释胶囊', '布洛芬', '0.3g*20粒', '拜耳医药', 28.80, 50, 15, '解热镇痛', 'H20059876', '2025-12-10 18:24:11', '2025-12-10 18:24:11');
INSERT INTO `medicine` VALUES (9, '维生素C片', '维生素C', '100mg*100片', '施尔康', 12.90, 200, 30, '维生素', 'H20051111', '2025-12-10 18:24:11', '2025-12-10 18:24:11');
INSERT INTO `medicine` VALUES (10, '感冒灵颗粒', '感冒灵', '10g*12袋', '白云山制药', 8.50, 80, 25, '感冒药', 'H20052222', '2025-12-10 18:24:11', '2025-12-10 18:24:11');
INSERT INTO `medicine` VALUES (11, '葡萄糖注射液', '葡萄糖', '10% 500ml', '华润双鹤', 5.20, 150, 40, '注射剂', 'H20053333', '2025-12-10 18:24:11', '2025-12-10 18:24:11');
INSERT INTO `medicine` VALUES (12, '头孢拉定胶囊', '头孢拉定', '0.25g*12粒', '石药集团', 22.00, 75, 20, '抗生素', 'H20054444', '2025-12-10 18:24:11', '2025-12-10 18:24:11');
INSERT INTO `medicine` VALUES (13, '阿莫西林胶囊', '阿莫西林', '0.25g*24粒', '哈药集团', 15.50, 100, 20, '抗生素', 'H20051234', '2025-12-12 00:21:31', '2025-12-12 00:21:31');
INSERT INTO `medicine` VALUES (14, '布洛芬缓释胶囊', '布洛芬', '0.3g*20粒', '拜耳医药', 28.80, 50, 15, '解热镇痛', 'H20059876', '2025-12-12 00:21:31', '2025-12-12 00:21:31');
INSERT INTO `medicine` VALUES (15, '维生素C片', '维生素C', '100mg*100片', '施尔康', 12.90, 200, 30, '维生素', 'H20051111', '2025-12-12 00:21:31', '2025-12-12 00:21:31');
INSERT INTO `medicine` VALUES (16, '感冒灵颗粒', '感冒灵', '10g*12袋', '白云山制药', 8.50, 80, 25, '感冒药', 'H20052222', '2025-12-12 00:21:31', '2025-12-12 00:21:31');
INSERT INTO `medicine` VALUES (17, '葡萄糖注射液', '葡萄糖', '10% 500ml', '华润双鹤', 5.20, 150, 40, '注射剂', 'H20053333', '2025-12-12 00:21:31', '2025-12-12 00:21:31');
INSERT INTO `medicine` VALUES (18, '头孢拉定胶囊', '头孢拉定', '0.25g*12粒', '石药集团', 22.00, 75, 20, '抗生素', 'H20054444', '2025-12-12 00:21:31', '2025-12-12 00:21:31');

-- ----------------------------
-- Table structure for prescription
-- ----------------------------
DROP TABLE IF EXISTS `prescription`;
CREATE TABLE `prescription`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `prescription_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '处方编号',
  `patient_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `patient_age` int NULL DEFAULT NULL COMMENT '患者年龄',
  `patient_gender` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `doctor_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `department` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `create_date` date NOT NULL COMMENT '开方日期',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `audit_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `prescription_number`(`prescription_number` ASC) USING BTREE,
  INDEX `idx_prescription_number`(`prescription_number` ASC) USING BTREE,
  INDEX `idx_patient_name`(`patient_name` ASC) USING BTREE,
  INDEX `idx_doctor_name`(`doctor_name` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_date`(`create_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '处方表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of prescription
-- ----------------------------
INSERT INTO `prescription` VALUES (1, 'RX20241209001', '张三', 35, '', '李医生', '内科', '2024-12-09', '', NULL, NULL, '2025-12-10 18:24:11', '2025-12-10 18:24:11');
INSERT INTO `prescription` VALUES (2, 'RX20241209002', '李四', 28, '', '王医生', '外科', '2024-12-09', '', NULL, NULL, '2025-12-10 18:24:11', '2025-12-10 18:24:11');
INSERT INTO `prescription` VALUES (3, 'RX20241209003', '王五', 42, '', '赵医生', '儿科', '2024-12-09', '', NULL, NULL, '2025-12-10 18:24:11', '2025-12-10 18:24:11');
INSERT INTO `prescription` VALUES (5, 'RX20251212001', '123', 12, '男', 'doctor', '内科', '2025-12-12', '已取消', NULL, NULL, '2025-12-12 19:17:41', '2025-12-12 21:54:17');
INSERT INTO `prescription` VALUES (6, 'RX20251212002', '321', 13, '男', 'doctor', '内科', '2025-12-12', '已取消', '通过', '2025-12-12 19:35:20', '2025-12-12 19:33:20', '2025-12-12 21:54:18');
INSERT INTO `prescription` VALUES (9, 'RX20251212003', '111', 12, '女', 'doctor', '12', '2025-12-12', '未审核', NULL, NULL, '2025-12-12 21:37:57', '2025-12-12 21:37:57');
INSERT INTO `prescription` VALUES (10, 'RX20251212004', 'ljb', 20, '男', 'doctor', '内科', '2025-12-12', '未审核', NULL, NULL, '2025-12-12 21:39:55', '2025-12-12 21:39:55');
INSERT INTO `prescription` VALUES (11, 'RX20251212005', '000', 12, '男', 'doctor', '内科', '2025-12-12', '未审核', NULL, NULL, '2025-12-12 21:44:56', '2025-12-12 21:44:56');
INSERT INTO `prescription` VALUES (12, 'RX20251212006', '000', 22, '男', 'doctor', '内科', '2025-12-12', '已发药', '通过', '2025-12-12 22:14:18', '2025-12-12 21:46:25', '2025-12-12 22:22:50');
INSERT INTO `prescription` VALUES (13, 'RX20251212007', 'ljb', 22, '男', 'doctor', '内科', '2025-12-12', '已取消', '通过', '2025-12-12 22:03:22', '2025-12-12 21:49:21', '2025-12-12 22:14:00');

-- ----------------------------
-- Table structure for prescription_detail
-- ----------------------------
DROP TABLE IF EXISTS `prescription_detail`;
CREATE TABLE `prescription_detail`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `prescription_id` bigint NOT NULL COMMENT '处方ID',
  `medicine_id` bigint NOT NULL COMMENT '药品ID',
  `quantity` int NOT NULL COMMENT '数量',
  `dosage` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用法用量',
  `frequency` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '频次',
  `days` int NULL DEFAULT NULL COMMENT '天数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_prescription_id`(`prescription_id` ASC) USING BTREE,
  INDEX `idx_medicine_id`(`medicine_id` ASC) USING BTREE,
  CONSTRAINT `prescription_detail_ibfk_1` FOREIGN KEY (`prescription_id`) REFERENCES `prescription` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `prescription_detail_ibfk_2` FOREIGN KEY (`medicine_id`) REFERENCES `medicine` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '处方明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of prescription_detail
-- ----------------------------
INSERT INTO `prescription_detail` VALUES (1, 1, 1, 2, '每次2粒', '每日3次', 7, '2025-12-10 18:24:11');
INSERT INTO `prescription_detail` VALUES (2, 1, 3, 1, '每次1片', '每日2次', 7, '2025-12-10 18:24:11');
INSERT INTO `prescription_detail` VALUES (3, 2, 2, 1, '每次1粒', '每日2次', 5, '2025-12-10 18:24:11');
INSERT INTO `prescription_detail` VALUES (4, 2, 4, 2, '每次1袋', '每日3次', 3, '2025-12-10 18:24:11');
INSERT INTO `prescription_detail` VALUES (5, 3, 5, 1, '每次1瓶', '每日1次', 3, '2025-12-10 18:24:11');
INSERT INTO `prescription_detail` VALUES (6, 3, 6, 1, '每次1粒', '每日3次', 7, '2025-12-10 18:24:11');
INSERT INTO `prescription_detail` VALUES (7, 1, 1, 2, '每次2粒', '每日3次', 7, '2025-12-12 00:21:31');
INSERT INTO `prescription_detail` VALUES (8, 1, 3, 1, '每次1片', '每日2次', 7, '2025-12-12 00:21:31');
INSERT INTO `prescription_detail` VALUES (9, 2, 2, 1, '每次1粒', '每日2次', 5, '2025-12-12 00:21:31');
INSERT INTO `prescription_detail` VALUES (10, 2, 4, 2, '每次1袋', '每日3次', 3, '2025-12-12 00:21:31');
INSERT INTO `prescription_detail` VALUES (11, 3, 5, 1, '每次1瓶', '每日1次', 3, '2025-12-12 00:21:31');
INSERT INTO `prescription_detail` VALUES (12, 3, 6, 1, '每次1粒', '每日3次', 7, '2025-12-12 00:21:31');
INSERT INTO `prescription_detail` VALUES (13, 9, 1, 1, '1', '1', 1, '2025-12-12 21:37:57');
INSERT INTO `prescription_detail` VALUES (14, 10, 11, 1, '1', '1', 1, '2025-12-12 21:39:55');
INSERT INTO `prescription_detail` VALUES (15, 11, 2, 1, '1', '1', 1, '2025-12-12 21:44:56');
INSERT INTO `prescription_detail` VALUES (16, 12, 1, 1, '2', '2', 1, '2025-12-12 21:46:25');
INSERT INTO `prescription_detail` VALUES (17, 13, 1, 1, '2', '2', 1, '2025-12-12 21:49:21');

-- ----------------------------
-- Table structure for stock_in
-- ----------------------------
DROP TABLE IF EXISTS `stock_in`;
CREATE TABLE `stock_in`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `medicine_id` bigint NOT NULL COMMENT '药品ID',
  `batch_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '批号',
  `quantity` int NOT NULL COMMENT '入库数量',
  `supplier` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '供应商',
  `in_date` date NOT NULL COMMENT '入库日期',
  `operator` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作员',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_medicine_id`(`medicine_id` ASC) USING BTREE,
  INDEX `idx_in_date`(`in_date` ASC) USING BTREE,
  INDEX `idx_operator`(`operator` ASC) USING BTREE,
  CONSTRAINT `stock_in_ibfk_1` FOREIGN KEY (`medicine_id`) REFERENCES `medicine` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '入库记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of stock_in
-- ----------------------------
INSERT INTO `stock_in` VALUES (1, 1, 'BATCH20241201', 100, '医药公司A', '2024-12-01', '张库管', '2025-12-10 18:24:11');
INSERT INTO `stock_in` VALUES (2, 2, 'BATCH20241202', 50, '医药公司B', '2024-12-02', '李库管', '2025-12-10 18:24:11');
INSERT INTO `stock_in` VALUES (3, 3, 'BATCH20241203', 200, '医药公司C', '2024-12-03', '王库管', '2025-12-10 18:24:11');
INSERT INTO `stock_in` VALUES (4, 1, 'BATCH20241201', 100, '医药公司A', '2024-12-01', '张库管', '2025-12-12 00:21:31');
INSERT INTO `stock_in` VALUES (5, 2, 'BATCH20241202', 50, '医药公司B', '2024-12-02', '李库管', '2025-12-12 00:21:31');
INSERT INTO `stock_in` VALUES (6, 3, 'BATCH20241203', 200, '医药公司C', '2024-12-03', '王库管', '2025-12-12 00:21:31');
INSERT INTO `stock_in` VALUES (7, 1, '', 1, '', '2025-12-12', 'admin', '2025-12-12 21:30:33');

-- ----------------------------
-- Table structure for stock_out
-- ----------------------------
DROP TABLE IF EXISTS `stock_out`;
CREATE TABLE `stock_out`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `medicine_id` bigint NOT NULL COMMENT '药品ID',
  `batch_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '批号',
  `quantity` int NOT NULL COMMENT '出库数量',
  `out_date` date NOT NULL COMMENT '出库日期',
  `operator` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作员',
  `reason` enum('prescription','loss','expired','other') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'prescription' COMMENT '出库原因',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_medicine_id`(`medicine_id` ASC) USING BTREE,
  INDEX `idx_out_date`(`out_date` ASC) USING BTREE,
  INDEX `idx_operator`(`operator` ASC) USING BTREE,
  INDEX `idx_reason`(`reason` ASC) USING BTREE,
  CONSTRAINT `stock_out_ibfk_1` FOREIGN KEY (`medicine_id`) REFERENCES `medicine` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '出库记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of stock_out
-- ----------------------------
INSERT INTO `stock_out` VALUES (1, 1, 'BATCH20241201', 20, '2024-12-08', '药房管理员', '', '2025-12-10 18:24:11');
INSERT INTO `stock_out` VALUES (2, 2, 'BATCH20241202', 10, '2024-12-08', '药房管理员', '', '2025-12-10 18:24:11');
INSERT INTO `stock_out` VALUES (3, 1, 'BATCH20241201', 20, '2024-12-08', '药房管理员', '', '2025-12-12 00:21:31');
INSERT INTO `stock_out` VALUES (4, 2, 'BATCH20241202', 10, '2024-12-08', '药房管理员', '', '2025-12-12 00:21:31');
INSERT INTO `stock_out` VALUES (5, 1, NULL, 1, '2025-12-12', '系统', 'prescription', '2025-12-12 22:22:50');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（加密存储）',
  `role` enum('admin','doctor','nurse','pharmacist') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色',
  `real_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '科室',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$azjYo0obaJciGF3SlKGs2u5RQ4LCg5j7FnUh5oh3bMPBxyIcNbxQO', 'admin', '系统管理员', '信息科', '2025-12-10 01:17:41', '2025-12-12 00:07:01');
INSERT INTO `sys_user` VALUES (2, 'doctor1', '$2a$10$azjYo0obaJciGF3SlKGs2u5RQ4LCg5j7FnUh5oh3bMPBxyIcNbxQO', 'doctor', '张医生', '内科', '2025-12-10 01:17:41', '2025-12-12 00:07:05');
INSERT INTO `sys_user` VALUES (3, 'doctor2', '$2a$10$azjYo0obaJciGF3SlKGs2u5RQ4LCg5j7FnUh5oh3bMPBxyIcNbxQO', 'doctor', '李医生', '外科', '2025-12-10 01:17:41', '2025-12-12 00:07:07');
INSERT INTO `sys_user` VALUES (4, 'nurse1', '$2a$10$azjYo0obaJciGF3SlKGs2u5RQ4LCg5j7FnUh5oh3bMPBxyIcNbxQO', 'nurse', '王护士', '内科病房', '2025-12-10 01:17:41', '2025-12-12 00:07:10');
INSERT INTO `sys_user` VALUES (5, 'nurse2', '$2a$10$azjYo0obaJciGF3SlKGs2u5RQ4LCg5j7FnUh5oh3bMPBxyIcNbxQO', 'nurse', '赵护士', '外科病房', '2025-12-10 01:17:41', '2025-12-12 00:07:14');
INSERT INTO `sys_user` VALUES (6, 'pharmacist1', '$2a$10$azjYo0obaJciGF3SlKGs2u5RQ4LCg5j7FnUh5oh3bMPBxyIcNbxQO', 'pharmacist', '刘药师', '药剂科', '2025-12-10 01:17:41', '2025-12-12 00:07:17');
INSERT INTO `sys_user` VALUES (7, 'pharmacist2', '$2a$10$azjYo0obaJciGF3SlKGs2u5RQ4LCg5j7FnUh5oh3bMPBxyIcNbxQO', 'pharmacist', '陈药师', '药剂科', '2025-12-10 01:17:41', '2025-12-12 00:07:19');
INSERT INTO `sys_user` VALUES (11, 'doctor', '$2a$10$azjYo0obaJciGF3SlKGs2u5RQ4LCg5j7FnUh5oh3bMPBxyIcNbxQO', 'doctor', '李医生', '内科', '2025-12-10 18:24:11', '2025-12-12 00:07:21');
INSERT INTO `sys_user` VALUES (12, 'pharmacist', '$2a$10$azjYo0obaJciGF3SlKGs2u5RQ4LCg5j7FnUh5oh3bMPBxyIcNbxQO', 'pharmacist', '王药师', '药房', '2025-12-10 18:24:11', '2025-12-12 00:07:26');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'administrator',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `department` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', '$2a$10$43UylYuLKdHabur94Pm0K.FAZeRk8DnFeRRkxaWMrk1cDsfQhAXCm', 'admin', '系统管理员', '信息部', '2025-12-12 00:21:31', '2025-12-12 00:53:19');
INSERT INTO `users` VALUES (2, 'doctor', '$2a$10$43UylYuLKdHabur94Pm0K.FAZeRk8DnFeRRkxaWMrk1cDsfQhAXCm', 'doctor', '李医生', '内科', '2025-12-12 00:21:31', '2025-12-12 00:53:19');
INSERT INTO `users` VALUES (3, 'pharmacist', '$2a$10$43UylYuLKdHabur94Pm0K.FAZeRk8DnFeRRkxaWMrk1cDsfQhAXCm', 'pharmacist', '王药师', '药房', '2025-12-12 00:21:31', '2025-12-12 00:53:19');
INSERT INTO `users` VALUES (4, 'nurse', '$2a$10$IwPYlp/t6seniIfej7dWHuDR8PCvF4x10IR.1pldSGRIWs7YHr.7O', 'nurse', '张护士', '1', '2025-12-12 16:51:13', '2025-12-12 16:51:13');

SET FOREIGN_KEY_CHECKS = 1;
