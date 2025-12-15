"""
药品审核服务

TODO: 田纹搭需要完善和扩展的审核功能
=====================================

核心算法优化 (高优先级):
TODO: 完善药品数据库加载机制
  - 从外部文件或数据库加载药品数据
  - 支持药品相互作用数据库
  - 添加药品禁忌症规则库
  - 实现药品分类体系

TODO: 升级审核算法准确性
  - 改进剂量计算逻辑 (考虑体重、年龄、肾功能)
  - 添加药品浓度监测
  - 实现药物基因组学检查
  - 支持特殊人群剂量调整 (儿童、老人、孕妇)

TODO: 增强相互作用检测
  - 实现更复杂的相互作用规则引擎
  - 添加药品-疾病相互作用检测
  - 支持营养素补充剂相互作用
  - 实现时间相关相互作用检测

机器学习增强 (中优先级):
TODO: 实现异常检测模型
  - 使用机器学习检测异常处方模式
  - 训练历史数据识别潜在风险
  - 实现处方相似度分析
  - 添加预测性风险评估

TODO: 智能推荐系统
  - 基于患者历史推荐替代药品
  - 智能剂量优化建议
  - 用药方案个性化推荐
  - 药物经济学分析

数据持久化 (基础功能):
TODO: 实现审核记录存储
  - SQLite数据库表结构设计
  - 审核历史数据持久化
  - 查询历史审核记录
  - 数据备份和恢复功能

TODO: 药品数据管理
  - 药品信息本地缓存
  - 数据更新同步机制
  - 药品数据版本控制
  - 数据质量校验

API接口扩展 (高优先级):
TODO: 实现批量审核接口
  - 支持多处方同时审核
  - 批量处理优化
  - 进度跟踪和状态报告
  - 错误处理和回滚机制

TODO: 统计分析接口
  - 审核结果统计报表
  - 风险趋势分析
  - 药品使用模式分析
  - 审核效率指标

性能优化 (中优先级):
TODO: 缓存机制实现
  - 药品数据缓存
  - 审核规则缓存
  - 结果缓存优化

TODO: 并发处理优化
  - 多线程审核处理
  - 异步任务队列
  - 资源池管理

测试和验证 (持续任务):
TODO: 算法准确性测试
  - 单元测试覆盖所有审核逻辑
  - 集成测试验证端到端流程
  - 性能测试和压力测试

TODO: 数据质量保证
  - 药品数据库准确性验证
  - 审核规则正确性检查
  - 边界条件测试

作者: 田纹搭 (Python审核服务负责人)
"""

import logging
import datetime
from typing import Dict, List, Any, Optional, Tuple
from dataclasses import dataclass
from enum import Enum

from src.dao.repositories import MedicineDAO

logger = logging.getLogger(__name__)


class AuditResult(Enum):
    """审核结果枚举"""
    PASS = "pass"
    WARNING = "warning"
    REJECT = "reject"


class IssueSeverity(Enum):
    """问题严重程度枚举"""
    LOW = "low"
    MEDIUM = "medium"
    HIGH = "high"
    CRITICAL = "critical"


@dataclass
class AuditIssue:
    """审核问题"""
    issue_type: str
    severity: IssueSeverity
    description: str
    suggestion: str
    drug_name: Optional[str] = None
    related_drugs: Optional[List[str]] = None


@dataclass
class AuditReport:
    """审核报告"""
    result: AuditResult
    score: float
    issues: List[AuditIssue]
    suggestions: List[str]
    audit_time: str


class AuditService:
    """药品审核服务类"""

    def __init__(self):
        # 初始化审核规则
        self.drug_database = self._load_drug_database()
        self.interaction_rules = self._load_interaction_rules()
        self.medicine_dao = MedicineDAO()
        logger.info("审核服务初始化完成")

    def audit_prescription(self, prescription_data: Dict[str, Any]) -> AuditReport:
        """
        审核处方

        Args:
            prescription_data: 处方数据

        Returns:
            AuditReport: 审核报告
        """
        try:
            patient_info = prescription_data.get('patient', {})
            medicines = prescription_data.get('medicines', [])

            # 加载药品知识（适应症/禁忌/相互作用）供规则使用
            med_id_list = [m.get('medicine_id') for m in medicines if m.get('medicine_id')]
            knowledge_map = self.medicine_dao.get_knowledge_bundle(med_id_list) if med_id_list else {}
            recommended_map = self.medicine_dao.get_recommended_limits(med_id_list) if med_id_list else {}
            spec_map = self.medicine_dao.get_specifications(med_id_list) if med_id_list else {}

            issues = []
            total_score = 0.0
            max_score = 100.0

            # 1. 药品兼容性检查
            compatibility_score, compatibility_issues = self._check_drug_compatibility(medicines)
            issues.extend(compatibility_issues)
            total_score += compatibility_score

            # 2. 用量合理性检查
            dosage_score, dosage_issues = self._check_dosage_safety(
                medicines,
                patient_info,
                recommended_map,
                spec_map,
            )
            issues.extend(dosage_issues)
            total_score += dosage_score

            # 3. 药品相互作用检查
            interaction_score, interaction_issues = self._check_drug_interactions(medicines, knowledge_map)
            issues.extend(interaction_issues)
            total_score += interaction_score

            # 4. 患者安全性检查
            safety_score, safety_issues = self._check_patient_safety(medicines, patient_info, knowledge_map)
            issues.extend(safety_issues)
            total_score += safety_score

            # 计算最终得分
            final_score = min(total_score / max_score, 1.0)

            # 确定审核结果：1 直接通过；>0.75 待人工审核；<=0.75 拒绝
            if final_score == 1.0:
                result = AuditResult.PASS
            elif final_score > 0.75:
                result = AuditResult.WARNING
            else:
                result = AuditResult.REJECT

            # 生成建议
            suggestions = self._generate_suggestions(issues, result)

            audit_time = datetime.datetime.now().isoformat()

            return AuditReport(
                result=result,
                score=round(final_score * 100, 2),
                issues=issues,
                suggestions=suggestions,
                audit_time=audit_time
            )

        except Exception as e:
            logger.error(f"审核处方时发生错误: {str(e)}")
            # 返回失败的审核报告
            return AuditReport(
                result=AuditResult.REJECT,
                score=0.0,
                issues=[AuditIssue(
                    issue_type="system_error",
                    severity=IssueSeverity.CRITICAL,
                    description=f"系统审核过程中发生错误: {str(e)}",
                    suggestion="请联系技术支持或人工审核"
                )],
                suggestions=["建议进行人工审核"],
                audit_time=datetime.datetime.now().isoformat()
            )

    def _check_drug_compatibility(self, medicines: List[Dict[str, Any]]) -> Tuple[float, List[AuditIssue]]:
        """检查药品兼容性"""
        issues = []
        score = 25.0  # 基础分

        # 检查重复药品
        drug_names = [med.get('name', '') for med in medicines]
        duplicates = set([name for name in drug_names if drug_names.count(name) > 1])

        if duplicates:
            issues.append(AuditIssue(
                issue_type="duplicate_drugs",
                severity=IssueSeverity.HIGH,
                description=f"处方中存在重复药品: {', '.join(duplicates)}",
                suggestion="请确认是否需要重复用药，或合并相同药品的剂量"
            ))
            score -= 10

        # 检查药品分类重复
        drug_categories = {}
        for med in medicines:
            category = med.get('category', 'unknown')
            drug_name = med.get('name', '')
            if category not in drug_categories:
                drug_categories[category] = []
            drug_categories[category].append(drug_name)

        for category, drugs in drug_categories.items():
            if len(drugs) > 2:  # 同一分类药品超过2种
                issues.append(AuditIssue(
                    issue_type="category_overuse",
                    severity=IssueSeverity.MEDIUM,
                    description=f"同一分类({category})药品使用过多: {', '.join(drugs)}",
                    suggestion="建议评估是否需要同时使用多种同类药品"
                ))
                score -= 5

        return max(score, 0), issues

    def _check_dosage_safety(
        self,
        medicines: List[Dict[str, Any]],
        patient_info: Dict[str, Any],
        recommended_map: Dict[int, Dict[str, Any]],
        spec_map: Dict[int, Optional[str]],
    ) -> Tuple[float, List[AuditIssue]]:
        """检查用量安全性"""
        issues = []
        score = 25.0  # 基础分

        weight = self._to_number(patient_info.get('weight'), default=70)  # 默认体重70kg
        id_to_name = {med.get('medicine_id'): med.get('name', '') for med in medicines if med.get('medicine_id')}

        for med in medicines:
            name = med.get('name', '')
            dosage_raw = med.get('dosage', 0)
            dosage = self._parse_dosage_value(dosage_raw)
            frequency = med.get('frequency', '')
            times_per_day = self._to_number(frequency, default=1)
            total_daily = dosage * max(times_per_day, 1)
            days = med.get('days', 1)

            med_id = med.get('medicine_id')
            spec_text = spec_map.get(med_id) if med_id else None
            effective_single, effective_daily = self._compute_effective_amounts(
                dosage_raw, dosage, times_per_day, spec_text
            )
            used_single = effective_single if effective_single is not None else dosage
            used_daily = effective_daily if effective_daily is not None else total_daily

            # 剂量无效直接扣 25 分
            if dosage <= 0:
                issues.append(AuditIssue(
                    issue_type="invalid_dosage",
                    severity=IssueSeverity.HIGH,
                    description=f"药品 {name} 的剂量无效: {dosage}",
                    suggestion="请重新确认药品剂量",
                    drug_name=name
                ))
                score -= 25
                continue

            # 推荐剂量检查
            rec = recommended_map.get(med_id) if med_id else None
            if rec:
                rec_single = rec.get('single')
                rec_daily = rec.get('daily')
                if rec_single is not None and float(rec_single) > 0 and used_single > float(rec_single):
                    ratio = used_single / float(rec_single)
                    deduct = 25 if ratio > 1.5 else 10
                    issues.append(AuditIssue(
                        issue_type="dose_over_single",
                        severity=IssueSeverity.HIGH if deduct == 25 else IssueSeverity.MEDIUM,
                        description=self._fmt_amount_message(
                            name=name,
                            measured=used_single,
                            recommended=rec_single,
                            spec_text=spec_text,
                            scope="单次",
                        ),
                        suggestion="请调整单次剂量",
                        drug_name=name
                    ))
                    score -= deduct
                if rec_daily is not None and float(rec_daily) > 0 and used_daily > float(rec_daily):
                    ratio = used_daily / float(rec_daily)
                    deduct = 25 if ratio > 1.2 else 10
                    issues.append(AuditIssue(
                        issue_type="dose_over_daily",
                        severity=IssueSeverity.HIGH if deduct == 25 else IssueSeverity.MEDIUM,
                        description=self._fmt_amount_message(
                            name=name,
                            measured=used_daily,
                            recommended=rec_daily,
                            spec_text=spec_text,
                            scope="日总",
                        ),
                        suggestion="请降低频次或每次剂量",
                        drug_name=name
                    ))
                    score -= deduct

        return max(score, 0), issues

    @staticmethod
    def _to_number(val: Any, default: float = 0) -> float:
        import re

        # 直接数值
        try:
            return float(val)
        except (TypeError, ValueError):
            pass

        # 字符串中提取第一个数字（支持整数/小数）
        if isinstance(val, str):
            m = re.search(r"[-+]?\d*\.?\d+", val)
            if m:
                try:
                    return float(m.group())
                except ValueError:
                    pass

        return default

    @staticmethod
    def _parse_dosage_value(val: Any, default: float = 0) -> float:
        """提取剂量，优先取含 ml 的数字；若有浓度和体积如“10% 500ml”，取体积 500."""
        import re
        if val is None:
            return default
        if isinstance(val, (int, float)):
            return float(val)
        if not isinstance(val, str):
            return default

        # 提取所有数字
        nums = re.findall(r"[-+]?\d*\.?\d+", val)
        if not nums:
            return default

        # 如果包含 ml 且有数字，优先取带 ml 的数字
        if 'ml' in val.lower():
            ml_match = re.findall(r"([-+]?\d*\.?\d+)\s*ml", val.lower())
            if ml_match:
                try:
                    return float(ml_match[-1])
                except ValueError:
                    pass

        # 否则返回最后一个数字（如“10% 500ml”会取 500）
        try:
            return float(nums[-1])
        except ValueError:
            return default

    @staticmethod
    def _fmt_amount(val: Any) -> str:
        try:
            num = float(val)
            return f"{num:.2f}".rstrip('0').rstrip('.')
        except (TypeError, ValueError):
            return str(val)

    def _fmt_amount_message(
        self,
        name: str,
        measured: Any,
        recommended: Any,
        spec_text: Optional[str],
        scope: str,
    ) -> str:
        measured_s = self._fmt_amount(measured)
        rec_s = self._fmt_amount(recommended)
        if spec_text:
            return f"{name} {scope}剂量折算 {measured_s} 超出推荐 {rec_s}（规格: {spec_text}）"
        return f"{name} {scope}剂量 {measured_s} 超出推荐 {rec_s}"

    def _compute_effective_amounts(
        self,
        dosage_raw: Any,
        parsed_dosage: float,
        times_per_day: float,
        specification: Optional[str],
    ) -> Tuple[Optional[float], Optional[float]]:
        """根据规格折算有效剂量，返回单次与日总剂量；无法解析时返回 None."""
        per_unit, _ = self._parse_spec_amount(specification) if specification else (None, None)
        if per_unit is None:
            return None, None

        raw_str = str(dosage_raw).lower() if isinstance(dosage_raw, str) else ''
        contains_mass_or_volume = any(u in raw_str for u in ['mg', 'g', 'µg', 'ug', 'kg', 'ml'])

        # 若原始剂量已带质量/体积单位，则认为已是有效剂量；否则按规格折算
        if contains_mass_or_volume:
            single = parsed_dosage
        else:
            single = parsed_dosage * per_unit

        daily = single * max(times_per_day, 1)
        return single, daily

    @staticmethod
    def _parse_spec_amount(specification: str) -> Tuple[Optional[float], Optional[str]]:
        """从规格中提取每单位有效含量，质量统一转成 g，体积转 ml."""
        import re

        if not specification:
            return None, None

        # 质量以 g 为基准；体积以 ml 为基准
        mass_units = {'kg': 1000.0, 'g': 1.0, 'mg': 0.001, 'ug': 0.000001, 'µg': 0.000001}
        volume_units = {'ml': 1.0}

        spec = specification.lower()
        m = re.search(r"(\d+(?:\.\d+)?)\s*(kg|g|mg|µg|ug|ml)", spec)
        if not m:
            return None, None

        try:
            amount = float(m.group(1))
        except ValueError:
            return None, None

        unit = m.group(2)
        if unit in mass_units:
            return amount * mass_units[unit], 'mass'
        if unit in volume_units:
            return amount * volume_units[unit], 'volume'

        return None, None

    def _check_drug_interactions(self, medicines: List[Dict[str, Any]], knowledge_map: Dict[int, Dict[str, Any]]) -> Tuple[float, List[AuditIssue]]:
        """检查药品相互作用"""
        issues = []
        score = 25.0  # 基础分

        drug_names = [med.get('name', '') for med in medicines]
        id_to_name = {med.get('medicine_id'): med.get('name', '') for med in medicines if med.get('medicine_id')}

        # 基于知识库的相互作用（medicine_interaction 表），按 risk_level 计分
        for med_id, bundle in knowledge_map.items():
            this_name = id_to_name.get(med_id, '')
            for inter in bundle.get('interactions', []):
                other_name = inter.get('other_medicine_name') or ''
                risk_raw = inter.get('risk_level')
                try:
                    risk_num = int(risk_raw) if risk_raw is not None else None
                except (TypeError, ValueError):
                    risk_num = None
                if other_name in drug_names:
                    # risklevel: 3->25, 2->13, 1->9
                    deduct = 25 if risk_num == 3 else 13 if risk_num == 2 else 9 if risk_num == 1 else 9
                    severity_level = IssueSeverity.HIGH if deduct >= 13 else IssueSeverity.MEDIUM
                    issues.append(AuditIssue(
                        issue_type="drug_interaction_db",
                        severity=severity_level,
                        description=f"药品相互作用: {this_name} + {other_name} - {inter.get('description') or '相互作用风险'}",
                        suggestion=inter.get('suggestion') or "建议调整用药或监测",
                        related_drugs=[this_name, other_name]
                    ))
                    score -= deduct

        return max(score, 0), issues

    def _check_patient_safety(self, medicines: List[Dict[str, Any]], patient_info: Dict[str, Any], knowledge_map: Dict[int, Dict[str, Any]]) -> Tuple[float, List[AuditIssue]]:
        """检查患者安全性"""
        issues = []
        score = 25.0  # 基础分

        conditions = patient_info.get('conditions', [])
        id_to_name = {med.get('medicine_id'): med.get('name', '') for med in medicines if med.get('medicine_id')}

        # 检查疾病禁忌
        # 基于知识库的疾病禁忌
        for med_id, bundle in knowledge_map.items():
            med_name = id_to_name.get(med_id, '')
            for condition in conditions:
                for contra in bundle.get('contraindications', []):
                    if condition and condition in contra:
                        issues.append(AuditIssue(
                            issue_type="disease_contraindication",
                            severity=IssueSeverity.HIGH,
                            description=f"患者患有{condition}，不适宜使用 {med_name}",
                            suggestion="请更换或调整方案",
                            drug_name=med_name
                        ))
                        score -= 25

        # 患者疾病与药品适应症的简单匹配：缺少适应症时给出提醒（非致命）
        for med_id, bundle in knowledge_map.items():
            med_name = id_to_name.get(med_id, '')
            indications = bundle.get('indications', [])
            if indications and conditions:
                matched = any(cond in indications for cond in conditions)
                if not matched:
                    issues.append(AuditIssue(
                        issue_type="indication_mismatch",
                        severity=IssueSeverity.MEDIUM,
                        description=f"{med_name} 的适应症与患者疾病未匹配，请确认用药适应性",
                        suggestion="请确认处方适应症是否合理",
                        drug_name=med_name
                    ))
                    score -= 25

        return max(score, 0), issues

    def _generate_suggestions(self, issues: List[AuditIssue], result: AuditResult) -> List[str]:
        """生成审核建议"""
        suggestions = []

        if result == AuditResult.REJECT:
            suggestions.append("该处方存在严重问题，建议驳回并重新开方")
        elif result == AuditResult.WARNING:
            suggestions.append("该处方存在一定风险，建议在医生指导下使用")

        # 根据问题类型生成具体建议
        for issue in issues:
            if issue.suggestion and issue.suggestion not in suggestions:
                suggestions.append(issue.suggestion)

        return suggestions

    def _load_drug_database(self) -> Dict[str, Any]:
        """加载药品数据库（简化版）"""
        # 实际应该从数据库或文件中加载
        return {
            '阿司匹林': {'max_dosage': 2000, 'category': 'NSAIDs'},
            '布洛芬': {'max_dosage': 2400, 'category': 'NSAIDs'},
            '阿莫西林': {'max_dosage': 3000, 'category': '抗生素'}
        }

    def _load_interaction_rules(self) -> Dict[str, Any]:
        """加载药品相互作用规则（简化版）"""
        # 实际应该从数据库或文件中加载
        return {
            '阿司匹林+华法林': {'severity': 'major', 'description': '增加出血风险'}
        }
