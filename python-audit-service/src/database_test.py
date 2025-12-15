"""简单的集成测试脚本，用于验证 DAO 功能与数据库连通性。

运行方式（默认 MySQL，需确保 database/init.sql 已执行）：
    python src/database_test.py

可通过环境变量覆盖连接参数，例如：
    AUDIT_DB_ENGINE=mysql AUDIT_DB_HOST=127.0.0.1 AUDIT_DB_USER=root AUDIT_DB_PASSWORD=xxx python src/database_test.py
"""

import os
import sys
from datetime import date, datetime
from pathlib import Path

# 保证能 import src.*
ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

from src import database
from src.dao.repositories import (
    AuditRecordDAO,
    AuditRuleDAO,
    AuditStatisticsDAO,
    MedicineDAO,
    PrescriptionDAO,
)
from src.models import AuditIssueRecord, AuditRecord, AuditStatistics


def make_config():
    return {
        "AUDIT_DB_ENGINE": os.getenv("AUDIT_DB_ENGINE", "mysql"),
        "AUDIT_DB_HOST": os.getenv("AUDIT_DB_HOST", "localhost"),
        "AUDIT_DB_PORT": int(os.getenv("AUDIT_DB_PORT", "3306")),
        "AUDIT_DB_USER": os.getenv("AUDIT_DB_USER", "root"),
        "AUDIT_DB_PASSWORD": os.getenv("AUDIT_DB_PASSWORD", "040918"),
        "AUDIT_DB_NAME": os.getenv("AUDIT_DB_NAME", "hpims"),
    }


def test_medicine_and_prescription():
    med_dao = MedicineDAO()
    pres_dao = PrescriptionDAO()

    med = med_dao.get_by_id(1)
    assert med, "未找到 id=1 的药品，确认数据库已初始化"
    print("Medicine #1:", med.to_dict())

    payload = pres_dao.get_with_details(1)
    assert payload, "未找到 id=1 的处方，确认数据库已初始化"
    print("Prescription #1 patient:", payload.patient.to_dict())
    print("Prescription #1 medicines:", [m.to_dict() for m in payload.medicines])


def test_audit_record_roundtrip():
    audit_dao = AuditRecordDAO()

    record = AuditRecord(
        id=None,
        prescription_id=1,
        audit_type="自动审核",
        audit_result="pass",
        audit_score=98.5,
        issues_found=[],
        suggestions=["按时服药"],
        auditor="test-script",
        patient_age=30,
        patient_gender="男",
        patient_conditions=["无"],
        patient_allergies=[],
        rule_version="v1",
        engine_version="test",
        audit_time=datetime.now().isoformat(timespec="seconds"),
    )

    record_id = audit_dao.insert_record(record)
    issues = [
        AuditIssueRecord(
            id=None,
            audit_record_id=record_id,
            issue_type="sample_issue",
            severity="low",
            description="示例问题",
            suggestion="示例建议",
            drug_name="阿莫西林",
            related_drugs=["布洛芬"],
        )
    ]
    audit_dao.bulk_insert_issues(issues)
    audit_dao.insert_snapshot(record_id, {"prescription_id": 1, "note": "snapshot"})

    history = audit_dao.list_history(prescription_id=1, limit=5)
    assert any(r.id == record_id for r in history), "历史记录未包含新插入的审核记录"
    print(f"Audit record inserted id={record_id}, history size={len(history)}")

    # 清理插入的数据，避免污染
    with database.get_connection() as conn:
        cur = conn.cursor()
        cur.execute("DELETE FROM audit_issue WHERE audit_record_id = %s" if database.DB_ENGINE == "mysql" else "DELETE FROM audit_issue WHERE audit_record_id = ?", (record_id,))
        cur.execute("DELETE FROM audit_snapshot WHERE audit_record_id = %s" if database.DB_ENGINE == "mysql" else "DELETE FROM audit_snapshot WHERE audit_record_id = ?", (record_id,))
        cur.execute("DELETE FROM audit_record WHERE id = %s" if database.DB_ENGINE == "mysql" else "DELETE FROM audit_record WHERE id = ?", (record_id,))
    print("Cleanup done for audit_record")


def test_statistics_upsert():
    stats_dao = AuditStatisticsDAO()
    today = date.today().isoformat()

    stats = AuditStatistics(
        id=None,
        period_date=today,
        period_type="daily",
        total_count=10,
        pass_count=8,
        warning_count=1,
        reject_count=1,
        top_risk_drugs=[{"name": "阿莫西林", "count": 3}],
    )

    stats_dao.upsert_period(stats)
    fetched = stats_dao.get_period(today, "daily")
    assert fetched and fetched.total_count == 10, "统计 upsert 失败"
    print("Statistics upsert ok:", fetched.to_dict())

    # 清理
    with database.get_connection() as conn:
        cur = conn.cursor()
        cur.execute(
            "DELETE FROM audit_statistics WHERE period_date = %s AND period_type = %s"
            if database.DB_ENGINE == "mysql"
            else "DELETE FROM audit_statistics WHERE period_date = ? AND period_type = ?",
            (today, "daily"),
        )
    print("Cleanup done for audit_statistics")


def main():
    cfg = make_config()
    print("Using config:", cfg)
    os.environ.update({k: str(v) for k, v in cfg.items()})

    if database.DB_ENGINE == "sqlite":
        database.init_db()

    test_medicine_and_prescription()
    test_audit_record_roundtrip()
    test_statistics_upsert()
    print("All DAO smoke tests passed.")

    print("\nExpected key outputs (参考)：")
    print("- Medicine #1: 字典包含药品基础信息，indications/contraindications/adverse_reactions/interactions 不为空则展示")
    print("- Prescription #1 patient: 显示 age/gender/conditions/allergies")
    print("- Prescription #1 medicines: 列出处方明细（medicine_id/name/dosage/frequency/days/category）")
    print("- Audit record inserted id=..., history size=...: 表示审核记录写入并能被历史查询读取")
    print("- Cleanup done for audit_record: 表示插入的审核记录/issue/snapshot 已清理")
    print("- Statistics upsert ok: ... created_at ...: 表示统计 upsert + 读取成功")
    print("- Cleanup done for audit_statistics: 表示统计数据已清理")
    print("- All DAO smoke tests passed.: 全流程无异常")


if __name__ == "__main__":
    main()