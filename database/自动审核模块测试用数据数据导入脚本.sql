-- 全量测试数据重置与插入脚本（自动审核模块）
-- 适用于 MySQL；执行前请确认已连接目标测试库。

SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM audit_issue;
DELETE FROM audit_snapshot;
DELETE FROM audit_record;
DELETE FROM prescription_detail;
DELETE FROM prescription;
DELETE FROM medicine_interaction;
DELETE FROM medicine_contraindication;
DELETE FROM medicine_indication;
DELETE FROM medicine;
SET FOREIGN_KEY_CHECKS = 1;

-- 药品：含规格与推荐剂量（单位：g；体积 ml）
INSERT INTO medicine (id, name, generic_name, specification, manufacturer, price, stock_quantity, min_stock, category, approval_number, recommended_single_dose, recommended_daily_dose) VALUES
  (201, '阿莫西林胶囊', '阿莫西林', '0.25g*24粒', '测试厂家A', 10.00, 100, 10, '抗生素', 'T201', 0.50, 1.50),
  (202, '布洛芬缓释胶囊', '布洛芬', '0.3g*20粒', '测试厂家B', 12.00, 80, 10, '解热镇痛', 'T202', 0.30, 0.90),
  (203, '维生素C片', '维生素C', '100mg*100片', '测试厂家C', 8.00, 150, 20, '维生素', 'T203', 0.10, 0.30),
  (204, '葡萄糖注射液', '葡萄糖', '10% 500ml', '测试厂家D', 5.00, 200, 20, '注射剂', 'T204', NULL, NULL),
  (205, '华法林片', '华法林', '2.5mg*60片', '测试厂家E', 25.00, 50, 10, '抗凝', 'T205', 0.0025, 0.0050),
  (206, '阿司匹林片', '阿司匹林', '100mg*30片', '测试厂家F', 15.00, 120, 15, '解热镇痛', 'T206', 0.10, 0.30),
  (207, '铁剂片', '铁剂', '300mg*30片', '测试厂家G', 18.00, 90, 10, '补血', 'T207', 0.30, 0.90),
  (208, '头孢拉定胶囊', '头孢拉定', '0.25g*12粒', '测试厂家H', 22.00, 70, 10, '抗生素', 'T208', 0.50, 1.50);

-- 适应症（用于适应性匹配）
INSERT INTO medicine_indication (medicine_id, indication) VALUES
  (201, '上呼吸道感染'),
  (202, '发热'),
  (203, '维生素C缺乏'),
  (204, '低血糖'),
  (205, '血栓'),
  (206, '心血管预防'),
  (207, '缺铁性贫血'),
  (208, '细菌感染');

-- 禁忌（患者病症命中扣 25）
INSERT INTO medicine_contraindication (medicine_id, contraindication) VALUES
  (202, '胃溃疡'),
  (202, '哮喘'),
  (205, '妊娠'),
  (206, '胃溃疡'),
  (207, '血色素沉着症'),
  (208, '肾功能不全');

-- 相互作用（risk_level 3/2/1 -> 扣分 25/13/9）
INSERT INTO medicine_interaction (medicine_id, other_medicine_name, risk_level, description, suggestion) VALUES
  (205, '阿莫西林胶囊', 3, '增加出血风险', '避免合用或严密监测'),
  (205, '布洛芬缓释胶囊', 3, '增加出血风险', '避免合用'),
  (206, '布洛芬缓释胶囊', 2, '增加胃肠不良反应风险', '必要时监测或调整'),
  (203, '铁剂片', 1, '可能影响吸收', '分开服用'),
  (208, '华法林片', 2, '可能影响抗凝效果', '监测 INR');

-- 处方头（含患者病症 JSON）
INSERT INTO prescription (id, prescription_number, patient_name, patient_age, patient_gender, patient_conditions, doctor_name, department, create_date, status) VALUES
  (301, 'RX-T-301', '张三', 35, '男', JSON_ARRAY('胃溃疡'), '李医生', '内科', CURDATE(), '未审核'),
  (302, 'RX-T-302', '李四', 28, '女', JSON_ARRAY('维生素C缺乏'), '王医生', '内科', CURDATE(), '未审核'),
  (303, 'RX-T-303', '王五', 60, '男', JSON_ARRAY('上呼吸道感染'), '赵医生', '心内科', CURDATE(), '未审核'),
  (304, 'RX-T-304', '赵六', 32, '女', JSON_ARRAY('妊娠'), '周医生', '产科', CURDATE(), '未审核'),
  (305, 'RX-T-305', '钱七', 45, '男', JSON_ARRAY('感冒'), '郑医生', '全科', CURDATE(), '未审核'),
  (306, 'RX-T-306', '孙八', 30, '女', JSON_ARRAY('维生素C缺乏'), '吴医生', '全科', CURDATE(), '未审核'),
  (307, 'RX-T-307', '周九', 50, '男', JSON_ARRAY('上呼吸道感染'), '钱医生', '内科', CURDATE(), '未审核'),
  (308, 'RX-T-308', '吴十', 40, '女', JSON_ARRAY('维生素C缺乏', '缺铁性贫血'), '孙医生', '内科', CURDATE(), '未审核'),
  (309, 'RX-T-309', '郑十一', 55, '男', JSON_ARRAY('胃溃疡', '哮喘'), '周医生', '内科', CURDATE(), '未审核'),
  (310, 'RX-T-310', '钱十二', 48, '女', JSON_ARRAY('上呼吸道感染', '胃溃疡'), '李医生', '呼吸科', CURDATE(), '未审核');

-- 处方明细
-- 301：布洛芬 2粒 q8h，禁忌(胃溃疡) + 超单次/日总（0.6g 单次>1.5倍，日总1.8g>1.2倍）
INSERT INTO prescription_detail (prescription_id, medicine_id, quantity, dosage, frequency, days) VALUES
  (301, 202, 1, '每次2粒', '每日3次', 5);

-- 302：维C 正常剂量，适应症匹配，期望通过
INSERT INTO prescription_detail (prescription_id, medicine_id, quantity, dosage, frequency, days) VALUES
  (302, 203, 1, '每次1片', '每日1次', 7);

-- 303：阿莫西林 + 华法林，相互作用 risk=3（扣25），剂量在推荐内
INSERT INTO prescription_detail (prescription_id, medicine_id, quantity, dosage, frequency, days) VALUES
  (303, 201, 1, '每次2粒', '每日2次', 7),
  (303, 205, 1, '每次1片', '每日1次', 7);

-- 304：妊娠 + 华法林（禁忌扣25）
INSERT INTO prescription_detail (prescription_id, medicine_id, quantity, dosage, frequency, days) VALUES
  (304, 205, 1, '每次1片', '每日1次', 5);

-- 305：阿司匹林用于“感冒”，适应症不匹配（扣25）
INSERT INTO prescription_detail (prescription_id, medicine_id, quantity, dosage, frequency, days) VALUES
  (305, 206, 1, '每次1片', '每日1次', 5);

-- 306：维C 大剂量（每次3片，每日2次）超单次>1.5倍且超日总>1.2倍
INSERT INTO prescription_detail (prescription_id, medicine_id, quantity, dosage, frequency, days) VALUES
  (306, 203, 1, '每次3片', '每日2次', 5);

-- 307：头孢拉定轻度超单次（3粒=0.75g，推荐0.5，比例=1.5，扣10），日总=1.5g 等于推荐日总，无日总扣分
INSERT INTO prescription_detail (prescription_id, medicine_id, quantity, dosage, frequency, days) VALUES
  (307, 208, 1, '每次3粒', '每日2次', 5);

-- 308：维C + 铁剂，相互作用 risk=1（扣9），其余剂量正常
INSERT INTO prescription_detail (prescription_id, medicine_id, quantity, dosage, frequency, days) VALUES
  (308, 203, 1, '每次1片', '每日1次', 7),
  (308, 207, 1, '每次1片', '每日1次', 7);

-- 309：布洛芬 + 多病症（胃溃疡、哮喘）均为禁忌，验证多病症叠加检查
INSERT INTO prescription_detail (prescription_id, medicine_id, quantity, dosage, frequency, days) VALUES
  (309, 202, 1, '每次1粒', '每日2次', 5);

-- 310：阿莫西林（适应症匹配） + 阿司匹林（胃溃疡禁忌），验证多病症下部分药匹配部分药禁忌
INSERT INTO prescription_detail (prescription_id, medicine_id, quantity, dosage, frequency, days) VALUES
  (310, 201, 1, '每次2粒', '每日2次', 5),
  (310, 206, 1, '每次1片', '每日1次', 5);

-- 可按需追加：
-- 1) 添加更多患者病症以验证适应症匹配与禁忌。
-- 2) 在请求中加入“华法林”或“阿司匹林”等名字以外的同义名，验证相互作用匹配依赖名称一致性。
