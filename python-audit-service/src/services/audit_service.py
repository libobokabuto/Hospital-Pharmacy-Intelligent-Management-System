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
from typing import Dict, List, Any, Optional, Tuple
from dataclasses import dataclass
from enum import Enum

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

            issues = []
            total_score = 0.0
            max_score = 100.0

            # 1. 药品兼容性检查
            compatibility_score, compatibility_issues = self._check_drug_compatibility(medicines)
            issues.extend(compatibility_issues)
            total_score += compatibility_score

            # 2. 用量合理性检查
            dosage_score, dosage_issues = self._check_dosage_safety(medicines, patient_info)
            issues.extend(dosage_issues)
            total_score += dosage_score

            # 3. 药品相互作用检查
            interaction_score, interaction_issues = self._check_drug_interactions(medicines)
            issues.extend(interaction_issues)
            total_score += interaction_score

            # 4. 患者安全性检查
            safety_score, safety_issues = self._check_patient_safety(medicines, patient_info)
            issues.extend(safety_issues)
            total_score += safety_score

            # 计算最终得分
            final_score = min(total_score / max_score, 1.0)

            # 确定审核结果
            if final_score >= 0.8:
                result = AuditResult.PASS
            elif final_score >= 0.6:
                result = AuditResult.WARNING
            else:
                result = AuditResult.REJECT

            # 生成建议
            suggestions = self._generate_suggestions(issues, result)

            import datetime
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

    def _check_dosage_safety(self, medicines: List[Dict[str, Any]], patient_info: Dict[str, Any]) -> Tuple[float, List[AuditIssue]]:
        """检查用量安全性"""
        issues = []
        score = 25.0  # 基础分

        age = patient_info.get('age', 0)
        weight = patient_info.get('weight', 70)  # 默认体重70kg

        for med in medicines:
            name = med.get('name', '')
            dosage = med.get('dosage', 0)
            frequency = med.get('frequency', '')
            days = med.get('days', 1)

            # 简单的剂量检查（实际应该有更复杂的规则）
            if dosage <= 0:
                issues.append(AuditIssue(
                    issue_type="invalid_dosage",
                    severity=IssueSeverity.HIGH,
                    description=f"药品 {name} 的剂量无效: {dosage}",
                    suggestion="请重新确认药品剂量",
                    drug_name=name
                ))
                score -= 15
                continue

            # 年龄相关的剂量检查
            if age < 12 and dosage > 0:  # 儿童剂量警告
                issues.append(AuditIssue(
                    issue_type="pediatric_dosage",
                    severity=IssueSeverity.MEDIUM,
                    description=f"儿童患者使用 {name}，请确认剂量是否合适",
                    suggestion="建议咨询儿科医生确认剂量",
                    drug_name=name
                ))
                score -= 5

            if age > 65 and dosage > 0:  # 老人剂量警告
                issues.append(AuditIssue(
                    issue_type="geriatric_dosage",
                    severity=IssueSeverity.MEDIUM,
                    description=f"老年患者使用 {name}，请确认剂量是否合适",
                    suggestion="建议咨询老年病科医生确认剂量",
                    drug_name=name
                ))
                score -= 5

        return max(score, 0), issues

    def _check_drug_interactions(self, medicines: List[Dict[str, Any]]) -> Tuple[float, List[AuditIssue]]:
        """检查药品相互作用"""
        issues = []
        score = 25.0  # 基础分

        drug_names = [med.get('name', '') for med in medicines]

        # 检查已知的药品相互作用（简化版）
        known_interactions = [
            (['阿司匹林', '华法林'], '严重', '增加出血风险'),
            (['西咪替丁', '茶碱'], '中度', '增加茶碱浓度'),
            (['红霉素', '他汀类药物'], '中度', '增加肌肉毒性风险')
        ]

        for interaction in known_interactions:
            drugs, severity, risk = interaction
            if all(drug in drug_names for drug in drugs):
                severity_level = IssueSeverity.HIGH if severity == '严重' else IssueSeverity.MEDIUM
                issues.append(AuditIssue(
                    issue_type="drug_interaction",
                    severity=severity_level,
                    description=f"药品相互作用: {' + '.join(drugs)} - {risk}",
                    suggestion="建议调整用药方案或密切监测",
                    related_drugs=drugs
                ))
                score -= 15 if severity == '严重' else 10

        return max(score, 0), issues

    def _check_patient_safety(self, medicines: List[Dict[str, Any]], patient_info: Dict[str, Any]) -> Tuple[float, List[AuditIssue]]:
        """检查患者安全性"""
        issues = []
        score = 25.0  # 基础分

        allergies = patient_info.get('allergies', [])
        conditions = patient_info.get('conditions', [])

        # 检查过敏史
        for med in medicines:
            name = med.get('name', '')
            # 简单的过敏检查（实际应该更复杂）
            for allergy in allergies:
                if allergy.lower() in name.lower():
                    issues.append(AuditIssue(
                        issue_type="allergy_alert",
                        severity=IssueSeverity.CRITICAL,
                        description=f"患者对 {allergy} 过敏，但处方包含 {name}",
                        suggestion="立即停止使用此类药品，寻求替代方案",
                        drug_name=name
                    ))
                    score -= 20

        # 检查疾病禁忌
        disease_contraindications = {
            '哮喘': ['β受体阻滞剂'],
            '癫痫': ['氯霉素'],
            '肝病': ['对乙酰氨基酚']
        }

        for condition in conditions:
            contraindicated_drugs = disease_contraindications.get(condition, [])
            for med in medicines:
                name = med.get('name', '')
                if any(drug in name for drug in contraindicated_drugs):
                    issues.append(AuditIssue(
                        issue_type="disease_contraindication",
                        severity=IssueSeverity.HIGH,
                        description=f"患者患有{condition}，不适宜使用 {name}",
                        suggestion="请更换其他治疗方案",
                        drug_name=name
                    ))
                    score -= 15

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
