"""DAO layer skeletons for audit service entities.

MySQL is the primary backend (shared with Java services). SQLite remains an
optional path for local debugging/testing. All JSON-ish columns should go
through json_dumps/json_loads to keep consistency across engines.
"""

from __future__ import annotations

from typing import Any, Callable, ContextManager, Dict, List, Optional, Tuple
from decimal import Decimal
from datetime import datetime, date

try:
    # Preferred path when imported as a package
    from ..database import DB_ENGINE, get_connection, json_dumps, json_loads
    from ..models import (
        AuditIssueRecord,
        AuditRecord,
        AuditRule,
        AuditStatistics,
        Medicine,
        PatientInfo,
        PrescriptionDetail,
        PrescriptionPayload,
    )
except ImportError:
    # Fallback for direct execution (python src/dao/repositories.py)
    import sys
    from pathlib import Path

    ROOT = Path(__file__).resolve().parents[2]
    sys.path.insert(0, str(ROOT))

    from src.database import DB_ENGINE, get_connection, json_dumps, json_loads
    from src.models import (
        AuditIssueRecord,
        AuditRecord,
        AuditRule,
        AuditStatistics,
        Medicine,
        PatientInfo,
        PrescriptionDetail,
        PrescriptionPayload,
    )

ConnectionFactory = Callable[[], ContextManager[Any]]


class BaseDAO:
    """Base DAO that wraps a connection factory."""

    def __init__(self, conn_factory: ConnectionFactory = get_connection):
        self.conn_factory = conn_factory


class MedicineDAO(BaseDAO):
    """CRUD for medicine and related knowledge tables (indication/contra/etc.)."""

    @property
    def _ph(self) -> str:
        return "%s" if DB_ENGINE == "mysql" else "?"

    def _in_clause(self, values: List[Any]) -> str:
        return ",".join([self._ph] * len(values))

    def _row_to_medicine(self, row: Dict[str, Any]) -> Medicine:
        return Medicine(
            id=row["id"],
            name=row["name"],
            generic_name=row.get("generic_name"),
            specification=row.get("specification"),
            manufacturer=row.get("manufacturer"),
            category=row.get("category"),
            approval_number=row.get("approval_number"),
            price=row.get("price", 0.0),
            stock_quantity=row.get("stock_quantity", 0),
            recommended_single_dose=row.get("recommended_single_dose"),
            recommended_daily_dose=row.get("recommended_daily_dose"),
            created_at=row.get("create_time") or row.get("created_at"),
        )

    def get_by_id(self, medicine_id: int) -> Optional[Medicine]:
        sql = (
            "SELECT id, name, generic_name, specification, manufacturer, category, "
            "approval_number, price, stock_quantity, recommended_single_dose, recommended_daily_dose, create_time FROM medicine WHERE id = "
            f"{self._ph}"
        )
        with self.conn_factory() as conn:
            cur = conn.cursor()
            cur.execute(sql, (medicine_id,))
            row = cur.fetchone()
            if not row:
                return None

            med = self._row_to_medicine(row)
            bundle = self.get_knowledge_bundle([medicine_id]).get(medicine_id, {})
            med.indications = bundle.get("indications", [])
            med.contraindications = bundle.get("contraindications", [])
            med.adverse_reactions = bundle.get("adverse_reactions", [])
            med.interactions = bundle.get("interactions", [])
            return med

    def search(self, keyword: str, limit: int = 20) -> List[Medicine]:
        pattern = f"%{keyword}%"
        sql = (
            "SELECT id, name, generic_name, specification, manufacturer, category, "
            "approval_number, price, stock_quantity, recommended_single_dose, recommended_daily_dose, create_time "
            "FROM medicine WHERE name LIKE {ph} OR generic_name LIKE {ph} LIMIT {limit}"
        ).format(ph=self._ph, limit=limit)

        with self.conn_factory() as conn:
            cur = conn.cursor()
            cur.execute(sql, (pattern, pattern))
            rows = cur.fetchall() or []
            meds = [self._row_to_medicine(r) for r in rows]

        # Attach knowledge bundles in one shot
        bundles = self.get_knowledge_bundle([m.id for m in meds if m.id is not None])
        for m in meds:
            if m.id in bundles:
                b = bundles[m.id]
                m.indications = b.get("indications", [])
                m.contraindications = b.get("contraindications", [])
                m.adverse_reactions = b.get("adverse_reactions", [])
                m.interactions = b.get("interactions", [])
        return meds

    def get_interactions(self, medicine_ids: List[int]) -> List[Dict[str, Any]]:
        if not medicine_ids:
            return []
        placeholders = self._in_clause(medicine_ids)
        sql = (
            f"SELECT medicine_id, other_medicine_name, risk_level, description, suggestion "
            f"FROM medicine_interaction WHERE medicine_id IN ({placeholders})"
        )
        with self.conn_factory() as conn:
            cur = conn.cursor()
            cur.execute(sql, medicine_ids)
            rows = cur.fetchall() or []
            return [dict(r) for r in rows]

    def get_knowledge_bundle(self, medicine_ids: List[int]) -> Dict[int, Dict[str, Any]]:
        if not medicine_ids:
            return {}

        placeholders = self._in_clause(medicine_ids)
        bundle: Dict[int, Dict[str, Any]] = {mid: {
            "indications": [],
            "contraindications": [],
            "adverse_reactions": [],
            "interactions": [],
        } for mid in medicine_ids}

        with self.conn_factory() as conn:
            cur = conn.cursor()

            cur.execute(
                f"SELECT medicine_id, indication FROM medicine_indication WHERE medicine_id IN ({placeholders})",
                medicine_ids,
            )
            for row in cur.fetchall() or []:
                bundle[row["medicine_id"]]["indications"].append(row["indication"])

            cur.execute(
                f"SELECT medicine_id, contraindication FROM medicine_contraindication WHERE medicine_id IN ({placeholders})",
                medicine_ids,
            )
            for row in cur.fetchall() or []:
                bundle[row["medicine_id"]]["contraindications"].append(row["contraindication"])

            cur.execute(
                f"SELECT medicine_id, reaction, severity FROM medicine_adverse_reaction WHERE medicine_id IN ({placeholders})",
                medicine_ids,
            )
            for row in cur.fetchall() or []:
                bundle[row["medicine_id"]]["adverse_reactions"].append(
                    {"reaction": row["reaction"], "severity": row.get("severity")}
                )

            cur.execute(
                f"SELECT medicine_id, other_medicine_name, risk_level, description, suggestion "
                f"FROM medicine_interaction WHERE medicine_id IN ({placeholders})",
                medicine_ids,
            )
            for row in cur.fetchall() or []:
                bundle[row["medicine_id"]]["interactions"].append(
                    {
                        "other_medicine_name": row["other_medicine_name"],
                        "risk_level": row.get("risk_level"),
                        "description": row.get("description"),
                        "suggestion": row.get("suggestion"),
                    }
                )

        return bundle

    def get_recommended_limits(self, medicine_ids: List[int]) -> Dict[int, Dict[str, Optional[float]]]:
        if not medicine_ids:
            return {}
        placeholders = self._in_clause(medicine_ids)
        sql = (
            f"SELECT id, recommended_single_dose, recommended_daily_dose FROM medicine WHERE id IN ({placeholders})"
        )
        with self.conn_factory() as conn:
            cur = conn.cursor()
            cur.execute(sql, medicine_ids)
            rows = cur.fetchall() or []
        return {
            row["id"]: {
                "single": row.get("recommended_single_dose"),
                "daily": row.get("recommended_daily_dose"),
            }
            for row in rows
        }

    def get_specifications(self, medicine_ids: List[int]) -> Dict[int, str]:
        if not medicine_ids:
            return {}
        placeholders = self._in_clause(medicine_ids)
        sql = f"SELECT id, specification FROM medicine WHERE id IN ({placeholders})"
        with self.conn_factory() as conn:
            cur = conn.cursor()
            cur.execute(sql, medicine_ids)
            rows = cur.fetchall() or []
        return {row["id"]: row.get("specification") for row in rows}


class PrescriptionDAO(BaseDAO):
    """Read prescription header/detail from shared DB for auditing."""

    @property
    def _ph(self) -> str:
        return "%s" if DB_ENGINE == "mysql" else "?"

    def get_with_details(self, prescription_id: int) -> Optional[PrescriptionPayload]:
        sql_pres = (
            "SELECT id, patient_age, patient_gender, patient_conditions, patient_name FROM prescription WHERE id = "
            f"{self._ph}"
        )
        with self.conn_factory() as conn:
            cur = conn.cursor()
            cur.execute(sql_pres, (prescription_id,))
            header = cur.fetchone()
            if not header:
                return None

            patient = PatientInfo(
                age=header.get("patient_age"),
                gender=header.get("patient_gender"),
                weight=None,
                conditions=json_loads(header.get("patient_conditions")) or [],
                allergies=[],
            )

            details = self.list_details(prescription_id)

        return PrescriptionPayload(
            prescription_id=prescription_id,
            patient=patient,
            medicines=details,
        )

    def list_details(self, prescription_id: int) -> List[PrescriptionDetail]:
        sql = (
            "SELECT pd.medicine_id, m.name, pd.quantity, pd.dosage, pd.frequency, pd.days, m.category "
            "FROM prescription_detail pd JOIN medicine m ON pd.medicine_id = m.id "
            f"WHERE pd.prescription_id = {self._ph}"
        )
        with self.conn_factory() as conn:
            cur = conn.cursor()
            cur.execute(sql, (prescription_id,))
            rows = cur.fetchall() or []
            return [
                PrescriptionDetail(
                    medicine_id=row["medicine_id"],
                    name=row["name"],
                    quantity=row.get("quantity"),
                    dosage=row.get("dosage"),
                    frequency=row.get("frequency"),
                    days=row.get("days"),
                    category=row.get("category"),
                )
                for row in rows
            ]

    def search(self,
            patient_name: Optional[str] = None,
               doctor_name: Optional[str] = None,
               department: Optional[str] = None,
               status: Optional[str] = None,
               date_from: Optional[str] = None,
               date_to: Optional[str] = None,
               limit: int = 50) -> List[PrescriptionPayload]:
        """按患者名/医生名/科室/状态/日期范围筛选处方并附带明细."""
        where = ["1=1"]
        params: List[Any] = []

        def like_param(val: str) -> str:
            return f"%{val}%"

        if patient_name:
            where.append(f"patient_name LIKE {self._ph}")
            params.append(like_param(patient_name))
        if doctor_name:
            where.append(f"doctor_name LIKE {self._ph}")
            params.append(like_param(doctor_name))
        if department:
            where.append(f"department LIKE {self._ph}")
            params.append(like_param(department))
        if status:
            where.append(f"status = {self._ph}")
            params.append(status)
        if date_from:
            where.append(f"create_date >= {self._ph}")
            params.append(date_from)
        if date_to:
            where.append(f"create_date <= {self._ph}")
            params.append(date_to)

        sql = (
            "SELECT id, prescription_number, patient_name, patient_age, patient_gender, patient_conditions, doctor_name, department, create_date, status "
            f"FROM prescription WHERE {' AND '.join(where)} ORDER BY create_date DESC, id DESC LIMIT {limit}"
        )

        with self.conn_factory() as conn:
            cur = conn.cursor()
            cur.execute(sql, tuple(params))
            rows = cur.fetchall() or []

        results: List[PrescriptionPayload] = []
        for row in rows:
            patient = PatientInfo(
                age=self._to_primitive(row.get("patient_age")),
                gender=row.get("patient_gender"),
                weight=None,
                conditions=json_loads(row.get("patient_conditions")) or [],
                allergies=[],
            )
            payload = PrescriptionPayload(
                prescription_id=row["id"],
                patient=patient,
                medicines=self.list_details(row["id"]),
            )
            # 附加头信息
            payload.prescription_number = row.get("prescription_number")
            payload.patient_name = row.get("patient_name")
            payload.doctor_name = row.get("doctor_name")
            payload.department = row.get("department")
            payload.create_date = self._to_primitive(row.get("create_date"))
            payload.status = row.get("status")
            results.append(payload)

        return results

    def update_audit_result(self, prescription_id: int, result: str, audit_time: str, status: Optional[str] = None, audit_result_text: Optional[str] = None):
        """更新处方表的审核状态与结果。"""
        if not prescription_id:
            return
        status_val = status or '已审核'
        audit_text = audit_result_text or result
        sql = (
            f"UPDATE prescription SET status = {self._ph}, audit_result = {self._ph}, audit_time = {self._ph} "
            "WHERE id = " + self._ph
        )
        with self.conn_factory() as conn:
            cur = conn.cursor()
            cur.execute(sql, (status_val, audit_text, audit_time, prescription_id))
            conn.commit()

    @staticmethod
    def _to_primitive(obj: Any) -> Any:
        from decimal import Decimal
        from datetime import date, datetime

        if isinstance(obj, Decimal):
            return float(obj)
        if isinstance(obj, (datetime, date)):
            return obj.isoformat()
        return obj


class AuditRecordDAO(BaseDAO):
    """Persist audit record + issues + snapshot."""

    @property
    def _ph(self) -> str:
        return "%s" if DB_ENGINE == "mysql" else "?"

    def insert_record(self, record: AuditRecord) -> int:
        sql = (
            "INSERT INTO audit_record (prescription_id, audit_type, audit_result, audit_score, "
            "issues_found, suggestions, auditor, patient_age, patient_gender, patient_conditions, "
            "patient_allergies, rule_version, engine_version, audit_time) "
            f"VALUES ({','.join([self._ph]*14)})"
        )

        payload = (
            record.prescription_id,
            record.audit_type,
            record.audit_result,
            record.audit_score,
            json_dumps(record.issues_found),
            json_dumps(record.suggestions),
            record.auditor,
            record.patient_age,
            record.patient_gender,
            json_dumps(record.patient_conditions),
            json_dumps(record.patient_allergies),
            record.rule_version,
            record.engine_version,
            record.audit_time,
        )

        with self.conn_factory() as conn:
            cur = conn.cursor()
            cur.execute(sql, payload)
            return cur.lastrowid

    def bulk_insert_issues(self, issues: List[AuditIssueRecord]) -> None:
        if not issues:
            return
        sql = (
            "INSERT INTO audit_issue (audit_record_id, issue_type, severity, description, suggestion, "
            "drug_name, related_drugs) VALUES ("
            f"{','.join([self._ph]*7)})"
        )
        payloads = [
            (
                issue.audit_record_id,
                issue.issue_type,
                issue.severity,
                issue.description,
                issue.suggestion,
                issue.drug_name,
                json_dumps(issue.related_drugs),
            )
            for issue in issues
        ]
        with self.conn_factory() as conn:
            cur = conn.cursor()
            cur.executemany(sql, payloads)

    def insert_snapshot(self, audit_record_id: int, payload: Dict[str, Any]) -> None:
        sql = f"INSERT INTO audit_snapshot (audit_record_id, prescription_payload) VALUES ({self._ph}, {self._ph})"
        with self.conn_factory() as conn:
            cur = conn.cursor()
            cur.execute(sql, (audit_record_id, json_dumps(payload)))

    def list_history(self, prescription_id: Optional[int] = None, limit: int = 50) -> List[AuditRecord]:
        base_sql = (
            "SELECT id, prescription_id, audit_type, audit_result, audit_score, issues_found, suggestions, "
            "auditor, patient_age, patient_gender, patient_conditions, patient_allergies, rule_version, "
            "engine_version, audit_time, create_time FROM audit_record"
        )

        params: Tuple[Any, ...] = tuple()
        if prescription_id is not None:
            base_sql += f" WHERE prescription_id = {self._ph}"
            params = (prescription_id,)

        base_sql += " ORDER BY audit_time DESC LIMIT " + str(limit)

        with self.conn_factory() as conn:
            cur = conn.cursor()
            cur.execute(base_sql, params)
            rows = cur.fetchall() or []

        history: List[AuditRecord] = []
        for row in rows:
            issues_found = self._to_primitive(json_loads(row.get("issues_found")))
            suggestions = self._to_primitive(json_loads(row.get("suggestions")) or [])
            patient_conditions = self._to_primitive(json_loads(row.get("patient_conditions")) or [])
            patient_allergies = self._to_primitive(json_loads(row.get("patient_allergies")) or [])
            history.append(
                AuditRecord(
                    id=row["id"],
                    prescription_id=row["prescription_id"],
                    audit_type=row.get("audit_type"),
                    audit_result=row.get("audit_result"),
                    audit_score=self._to_primitive(row.get("audit_score")),
                    issues_found=issues_found,
                    suggestions=suggestions,
                    auditor=row.get("auditor"),
                    patient_age=self._to_primitive(row.get("patient_age")),
                    patient_gender=row.get("patient_gender"),
                    patient_conditions=patient_conditions,
                    patient_allergies=patient_allergies,
                    rule_version=row.get("rule_version"),
                    engine_version=row.get("engine_version"),
                    audit_time=self._to_primitive(row.get("audit_time")),
                    created_at=self._to_primitive(row.get("create_time")),
                )
            )
        return history

    @staticmethod
    def _to_primitive(obj: Any) -> Any:
        """Recursively convert Decimal/datetime/date to JSON-friendly primitives."""
        if isinstance(obj, Decimal):
            return float(obj)
        if isinstance(obj, (datetime, date)):
            return obj.isoformat(sep=" ") if isinstance(obj, datetime) else obj.isoformat()
        if isinstance(obj, list):
            return [AuditRecordDAO._to_primitive(i) for i in obj]
        if isinstance(obj, dict):
            return {k: AuditRecordDAO._to_primitive(v) for k, v in obj.items()}
        return obj


class AuditStatisticsDAO(BaseDAO):
    """Upsert and query audit_statistics table."""

    @property
    def _ph(self) -> str:
        return "%s" if DB_ENGINE == "mysql" else "?"

    def upsert_period(self, stats: AuditStatistics) -> None:
        if DB_ENGINE == "mysql":
            sql = (
                "INSERT INTO audit_statistics (period_date, period_type, total_count, pass_count, "
                "warning_count, reject_count, top_risk_drugs) VALUES ("
                f"{','.join([self._ph]*7)}) "
                "ON DUPLICATE KEY UPDATE total_count=VALUES(total_count), pass_count=VALUES(pass_count), "
                "warning_count=VALUES(warning_count), reject_count=VALUES(reject_count), "
                "top_risk_drugs=VALUES(top_risk_drugs)"
            )
        else:
            sql = (
                "INSERT INTO audit_statistics (period_date, period_type, total_count, pass_count, warning_count, "
                "reject_count, top_risk_drugs) VALUES ("
                f"{','.join([self._ph]*7)}) "
                "ON CONFLICT(period_date, period_type) DO UPDATE SET "
                "total_count=excluded.total_count, pass_count=excluded.pass_count, "
                "warning_count=excluded.warning_count, reject_count=excluded.reject_count, "
                "top_risk_drugs=excluded.top_risk_drugs"
            )

        payload = (
            stats.period_date,
            stats.period_type,
            stats.total_count,
            stats.pass_count,
            stats.warning_count,
            stats.reject_count,
            json_dumps(stats.top_risk_drugs),
        )

        with self.conn_factory() as conn:
            cur = conn.cursor()
            cur.execute(sql, payload)

    def get_period(self, period_date: str, period_type: str) -> Optional[AuditStatistics]:
        sql = (
            "SELECT id, period_date, period_type, total_count, pass_count, warning_count, reject_count, "
            f"top_risk_drugs, create_time FROM audit_statistics WHERE period_date = {self._ph} AND period_type = {self._ph}"
        )
        with self.conn_factory() as conn:
            cur = conn.cursor()
            cur.execute(sql, (period_date, period_type))
            row = cur.fetchone()
            if not row:
                return None
            return AuditStatistics(
                id=row.get("id"),
                period_date=row["period_date"],
                period_type=row["period_type"],
                total_count=row.get("total_count", 0),
                pass_count=row.get("pass_count", 0),
                warning_count=row.get("warning_count", 0),
                reject_count=row.get("reject_count", 0),
                top_risk_drugs=json_loads(row.get("top_risk_drugs")) or [],
                created_at=row.get("create_time"),
            )

    def list_recent(self, period_type: str, limit: int = 30) -> List[AuditStatistics]:
        sql = (
            "SELECT id, period_date, period_type, total_count, pass_count, warning_count, reject_count, top_risk_drugs, create_time "
            f"FROM audit_statistics WHERE period_type = {self._ph} ORDER BY period_date DESC LIMIT {limit}"
        )
        with self.conn_factory() as conn:
            cur = conn.cursor()
            cur.execute(sql, (period_type,))
            rows = cur.fetchall() or []

        return [
            AuditStatistics(
                id=row.get("id"),
                period_date=row["period_date"],
                period_type=row["period_type"],
                total_count=row.get("total_count", 0),
                pass_count=row.get("pass_count", 0),
                warning_count=row.get("warning_count", 0),
                reject_count=row.get("reject_count", 0),
                top_risk_drugs=json_loads(row.get("top_risk_drugs")) or [],
                created_at=row.get("create_time"),
            )
            for row in rows
        ]


class AuditRuleDAO(BaseDAO):
    """CRUD for audit rules and their conditions/actions."""

    @property
    def _ph(self) -> str:
        return "%s" if DB_ENGINE == "mysql" else "?"

    def list_rules(self, enabled_only: bool = True) -> List[AuditRule]:
        sql = "SELECT id, name, description, rule_type, severity, enabled, version, updated_at FROM audit_rule"
        params: Tuple[Any, ...] = tuple()
        if enabled_only:
            sql += f" WHERE enabled = {self._ph}"
            params = (1,)

        with self.conn_factory() as conn:
            cur = conn.cursor()
            cur.execute(sql, params)
            rule_rows = cur.fetchall() or []

            # Fetch conditions/actions in bulk
            rule_ids = [r["id"] for r in rule_rows]
            cond_map: Dict[int, List[Dict[str, Any]]] = {rid: [] for rid in rule_ids}
            act_map: Dict[int, List[Dict[str, Any]]] = {rid: [] for rid in rule_ids}

            if rule_ids:
                placeholders = ",".join([self._ph] * len(rule_ids))

                cur.execute(
                    f"SELECT rule_id, condition_type, left_value, operator, right_value, metadata "
                    f"FROM audit_rule_condition WHERE rule_id IN ({placeholders})",
                    rule_ids,
                )
                for row in cur.fetchall() or []:
                    cond_map[row["rule_id"]].append(
                        {
                            "condition_type": row["condition_type"],
                            "left_value": row.get("left_value"),
                            "operator": row.get("operator"),
                            "right_value": row.get("right_value"),
                            "metadata": json_loads(row.get("metadata")),
                        }
                    )

                cur.execute(
                    f"SELECT rule_id, action_type, message, suggestion FROM audit_rule_action WHERE rule_id IN ({placeholders})",
                    rule_ids,
                )
                for row in cur.fetchall() or []:
                    act_map[row["rule_id"]].append(
                        {
                            "action_type": row["action_type"],
                            "message": row.get("message"),
                            "suggestion": row.get("suggestion"),
                        }
                    )

        rules: List[AuditRule] = []
        for r in rule_rows:
            rules.append(
                AuditRule(
                    id=r["id"],
                    name=r["name"],
                    description=r.get("description"),
                    rule_type=r["rule_type"],
                    severity=r["severity"],
                    enabled=bool(r.get("enabled", 1)),
                    version=r.get("version"),
                    conditions=cond_map.get(r["id"], []),
                    actions=act_map.get(r["id"], []),
                    updated_at=r.get("updated_at"),
                )
            )
        return rules

    def update_rules(self, rules: List[AuditRule]) -> None:
        # Simple strategy: clear and re-insert to keep parity between rules/conditions/actions
        with self.conn_factory() as conn:
            cur = conn.cursor()
            cur.execute("DELETE FROM audit_rule_action")
            cur.execute("DELETE FROM audit_rule_condition")
            cur.execute("DELETE FROM audit_rule")

            for rule in rules:
                cur.execute(
                    "INSERT INTO audit_rule (name, description, rule_type, severity, enabled, version) "
                    f"VALUES ({','.join([self._ph]*6)})",
                    (
                        rule.name,
                        rule.description,
                        rule.rule_type,
                        rule.severity,
                        1 if rule.enabled else 0,
                        rule.version,
                    ),
                )
                rule_id = cur.lastrowid

                # conditions
                if rule.conditions:
                    cur.executemany(
                        "INSERT INTO audit_rule_condition (rule_id, condition_type, left_value, operator, right_value, metadata) "
                        f"VALUES ({','.join([self._ph]*6)})",
                        [
                            (
                                rule_id,
                                c.get("condition_type"),
                                c.get("left_value"),
                                c.get("operator"),
                                c.get("right_value"),
                                json_dumps(c.get("metadata")),
                            )
                            for c in rule.conditions
                        ],
                    )

                # actions
                if rule.actions:
                    cur.executemany(
                        "INSERT INTO audit_rule_action (rule_id, action_type, message, suggestion) "
                        f"VALUES ({','.join([self._ph]*4)})",
                        [
                            (
                                rule_id,
                                a.get("action_type"),
                                a.get("message"),
                                a.get("suggestion"),
                            )
                            for a in rule.actions
                        ],
                    )

    def reset_rules(self) -> None:
        with self.conn_factory() as conn:
            cur = conn.cursor()
            cur.execute("DELETE FROM audit_rule_action")
            cur.execute("DELETE FROM audit_rule_condition")
            cur.execute("DELETE FROM audit_rule")


__all__ = [
    "BaseDAO",
    "MedicineDAO",
    "PrescriptionDAO",
    "AuditRecordDAO",
    "AuditStatisticsDAO",
    "AuditRuleDAO",
]
