"""Domain models for the audit service."""
from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any, Dict, List, Optional


@dataclass
class PatientInfo:
    """患者基本信息（用于审核输入和存档）"""

    age: Optional[int] = None
    gender: Optional[str] = None
    weight: Optional[float] = None
    conditions: List[str] = field(default_factory=list)
    allergies: List[str] = field(default_factory=list)

    def to_dict(self) -> Dict[str, Any]:
        return {
            "age": self.age,
            "gender": self.gender,
            "weight": self.weight,
            "conditions": self.conditions,
            "allergies": self.allergies,
        }


@dataclass
class PrescriptionDetail:
    """处方明细（单个药品条目）"""

    medicine_id: Optional[int]
    name: str
    quantity: Optional[int] = None
    dosage: Optional[str] = None
    frequency: Optional[str] = None
    days: Optional[int] = None
    category: Optional[str] = None

    def to_dict(self) -> Dict[str, Any]:
        return {
            "medicine_id": self.medicine_id,
            "name": self.name,
            "quantity": self.quantity,
            "dosage": self.dosage,
            "frequency": self.frequency,
            "days": self.days,
            "category": self.category,
        }


@dataclass
class PrescriptionPayload:
    """处方审核输入/快照结构"""

    prescription_id: Optional[int]
    patient: PatientInfo
    medicines: List[PrescriptionDetail]

    def to_dict(self) -> Dict[str, Any]:
        return {
            "prescription_id": self.prescription_id,
            "patient": self.patient.to_dict(),
            "medicines": [m.to_dict() for m in self.medicines],
        }


@dataclass
class Medicine:
    id: Optional[int]
    name: str
    generic_name: Optional[str] = None
    specification: Optional[str] = None
    manufacturer: Optional[str] = None
    category: Optional[str] = None
    approval_number: Optional[str] = None
    price: float = 0.0
    stock_quantity: int = 0
    indications: List[str] = field(default_factory=list)
    contraindications: List[str] = field(default_factory=list)
    adverse_reactions: List[Dict[str, Any]] = field(default_factory=list)
    interactions: List[Dict[str, Any]] = field(default_factory=list)
    created_at: Optional[str] = None

    def to_dict(self) -> Dict[str, Any]:
        return {
            "id": self.id,
            "name": self.name,
            "generic_name": self.generic_name,
            "specification": self.specification,
            "manufacturer": self.manufacturer,
            "category": self.category,
            "approval_number": self.approval_number,
            "price": self.price,
            "stock_quantity": self.stock_quantity,
            "indications": self.indications,
            "contraindications": self.contraindications,
            "adverse_reactions": self.adverse_reactions,
            "interactions": self.interactions,
            "created_at": self.created_at,
        }


@dataclass
class AuditRule:
    id: Optional[int]
    name: str
    description: Optional[str]
    rule_type: str
    severity: str
    enabled: bool = True
    version: Optional[str] = None
    conditions: List[Dict[str, Any]] = field(default_factory=list)
    actions: List[Dict[str, Any]] = field(default_factory=list)
    updated_at: Optional[str] = None

    def to_dict(self) -> Dict[str, Any]:
        return {
            "id": self.id,
            "name": self.name,
            "description": self.description,
            "rule_type": self.rule_type,
            "severity": self.severity,
            "enabled": self.enabled,
            "version": self.version,
            "conditions": self.conditions,
            "actions": self.actions,
            "updated_at": self.updated_at,
        }


@dataclass
class AuditRecord:
    id: Optional[int]
    prescription_id: int
    audit_type: str
    audit_result: str
    audit_score: Optional[float]
    issues_found: List[Dict[str, Any]] = field(default_factory=list)
    suggestions: List[str] = field(default_factory=list)
    auditor: Optional[str] = None
    patient_age: Optional[int] = None
    patient_gender: Optional[str] = None
    patient_conditions: List[str] = field(default_factory=list)
    patient_allergies: List[str] = field(default_factory=list)
    rule_version: Optional[str] = None
    engine_version: Optional[str] = None
    audit_time: Optional[str] = None
    created_at: Optional[str] = None

    def to_dict(self) -> Dict[str, Any]:
        return {
            "id": self.id,
            "prescription_id": self.prescription_id,
            "audit_type": self.audit_type,
            "audit_result": self.audit_result,
            "audit_score": self.audit_score,
            "issues_found": self.issues_found,
            "suggestions": self.suggestions,
            "auditor": self.auditor,
            "patient_age": self.patient_age,
            "patient_gender": self.patient_gender,
            "patient_conditions": self.patient_conditions,
            "patient_allergies": self.patient_allergies,
            "rule_version": self.rule_version,
            "engine_version": self.engine_version,
            "audit_time": self.audit_time,
            "created_at": self.created_at,
        }


@dataclass
class AuditStatistics:
    id: Optional[int]
    period_date: str
    period_type: str
    total_count: int
    pass_count: int
    warning_count: int
    reject_count: int
    top_risk_drugs: List[Dict[str, Any]] = field(default_factory=list)
    created_at: Optional[str] = None

    def to_dict(self) -> Dict[str, Any]:
        return {
            "id": self.id,
            "period_date": self.period_date,
            "period_type": self.period_type,
            "total_count": self.total_count,
            "pass_count": self.pass_count,
            "warning_count": self.warning_count,
            "reject_count": self.reject_count,
            "top_risk_drugs": self.top_risk_drugs,
            "created_at": self.created_at,
        }


@dataclass
class AuditIssueRecord:
    """与 audit_issue 表对应的结构，用于落库/读取"""

    id: Optional[int]
    audit_record_id: int
    issue_type: str
    severity: str
    description: Optional[str]
    suggestion: Optional[str]
    drug_name: Optional[str]
    related_drugs: List[str] = field(default_factory=list)
    created_at: Optional[str] = None

    @classmethod
    def from_service_issue(cls, audit_record_id: int, issue: Dict[str, Any]) -> "AuditIssueRecord":
        """构建用于持久化的 Issue 记录；issue 来自审核服务的字典表示。"""

        return cls(
            id=None,
            audit_record_id=audit_record_id,
            issue_type=issue.get("issue_type", "unknown"),
            severity=issue.get("severity", "unknown"),
            description=issue.get("description"),
            suggestion=issue.get("suggestion"),
            drug_name=issue.get("drug_name"),
            related_drugs=issue.get("related_drugs") or [],
        )

    def to_dict(self) -> Dict[str, Any]:
        return {
            "id": self.id,
            "audit_record_id": self.audit_record_id,
            "issue_type": self.issue_type,
            "severity": self.severity,
            "description": self.description,
            "suggestion": self.suggestion,
            "drug_name": self.drug_name,
            "related_drugs": self.related_drugs,
            "created_at": self.created_at,
        }
