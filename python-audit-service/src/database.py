"""SQLite database helpers for the audit service."""
import json
import os
import sqlite3
from contextlib import contextmanager
from datetime import datetime
from typing import Iterator, Optional

import pymysql

DB_ENGINE = os.getenv("AUDIT_DB_ENGINE", "mysql").lower()

# SQLite 路径（仅当选择 sqlite 时使用）
DEFAULT_DB_PATH = os.getenv(
    "AUDIT_DB_PATH",
    os.path.join(os.path.dirname(__file__), "..", "data", "audit.db"),
)

# MySQL 连接配置（与 Java 服务保持一致的默认值，可通过环境变量覆盖）
MYSQL_CONFIG = {
    "host": os.getenv("AUDIT_DB_HOST", "localhost"),
    "port": int(os.getenv("AUDIT_DB_PORT", "3306")),
    "user": os.getenv("AUDIT_DB_USER", "root"),
    "password": os.getenv("AUDIT_DB_PASSWORD", "040918"),
    "database": os.getenv("AUDIT_DB_NAME", "hpims"),
    "charset": "utf8mb4",
    "cursorclass": pymysql.cursors.DictCursor,
    "autocommit": False,
}

# DDL statements for audit service tables
DDL_STATEMENTS = """
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS medicine (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    generic_name TEXT,
    specification TEXT,
    manufacturer TEXT,
    category TEXT,
    approval_number TEXT,
    price REAL DEFAULT 0.0,
    stock_quantity INTEGER DEFAULT 0,
    created_at TEXT DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS medicine_indication (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    medicine_id INTEGER NOT NULL,
    indication TEXT NOT NULL,
    created_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (medicine_id) REFERENCES medicine(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS medicine_contraindication (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    medicine_id INTEGER NOT NULL,
    contraindication TEXT NOT NULL,
    created_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (medicine_id) REFERENCES medicine(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS medicine_adverse_reaction (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    medicine_id INTEGER NOT NULL,
    reaction TEXT NOT NULL,
    severity TEXT,
    created_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (medicine_id) REFERENCES medicine(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS medicine_interaction (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    medicine_id INTEGER NOT NULL,
    other_medicine_name TEXT NOT NULL,
    risk_level TEXT,
    description TEXT,
    suggestion TEXT,
    created_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (medicine_id) REFERENCES medicine(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS audit_rule (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT,
    rule_type TEXT NOT NULL,
    severity TEXT NOT NULL,
    enabled INTEGER DEFAULT 1,
    version TEXT,
    updated_at TEXT DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS audit_rule_condition (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    rule_id INTEGER NOT NULL,
    condition_type TEXT NOT NULL,
    left_value TEXT,
    operator TEXT,
    right_value TEXT,
    metadata TEXT,
    FOREIGN KEY (rule_id) REFERENCES audit_rule(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS audit_rule_action (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    rule_id INTEGER NOT NULL,
    action_type TEXT NOT NULL,
    message TEXT,
    suggestion TEXT,
    FOREIGN KEY (rule_id) REFERENCES audit_rule(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS audit_record (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    prescription_id INTEGER NOT NULL,
    audit_type TEXT DEFAULT '自动审核',
    audit_result TEXT NOT NULL,
    audit_score REAL,
    issues_found TEXT,
    suggestions TEXT,
    auditor TEXT,
    patient_age INTEGER,
    patient_gender TEXT,
    patient_conditions TEXT,
    patient_allergies TEXT,
    rule_version TEXT,
    engine_version TEXT,
    audit_time TEXT DEFAULT (datetime('now')),
    created_at TEXT DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS audit_issue (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    audit_record_id INTEGER NOT NULL,
    issue_type TEXT NOT NULL,
    severity TEXT NOT NULL,
    description TEXT,
    suggestion TEXT,
    drug_name TEXT,
    related_drugs TEXT,
    created_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (audit_record_id) REFERENCES audit_record(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS audit_snapshot (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    audit_record_id INTEGER NOT NULL,
    prescription_payload TEXT NOT NULL,
    created_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (audit_record_id) REFERENCES audit_record(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS audit_statistics (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    period_date TEXT NOT NULL,
    period_type TEXT NOT NULL,
    total_count INTEGER DEFAULT 0,
    pass_count INTEGER DEFAULT 0,
    warning_count INTEGER DEFAULT 0,
    reject_count INTEGER DEFAULT 0,
    top_risk_drugs TEXT,
    created_at TEXT DEFAULT (datetime('now')),
    UNIQUE (period_date, period_type)
);
"""


@contextmanager
def get_connection(db_path: str = DEFAULT_DB_PATH):
    """Yield a DB connection for the configured engine."""

    if DB_ENGINE == "mysql":
        conn = pymysql.connect(**MYSQL_CONFIG)
        try:
            yield conn
        finally:
            conn.commit()
            conn.close()
    else:
        os.makedirs(os.path.dirname(db_path), exist_ok=True)
        conn = sqlite3.connect(db_path)
        conn.row_factory = sqlite3.Row
        try:
            yield conn
        finally:
            conn.commit()
            conn.close()


def init_db(db_path: str = DEFAULT_DB_PATH) -> None:
    """Create tables if they do not exist (only for sqlite)."""
    if DB_ENGINE == "mysql":
        # MySQL 由外部迁移脚本管理（与 Java 模块共用），这里不做 DDL
        return
    with get_connection(db_path) as conn:
        conn.executescript(DDL_STATEMENTS)


def backup_db(db_path: str = DEFAULT_DB_PATH, backup_dir: Optional[str] = None) -> str:
    """Create a timestamped backup (sqlite only)."""
    if DB_ENGINE == "mysql":
        raise NotImplementedError("MySQL backup is managed externally (e.g., mysqldump)")

    backup_dir = backup_dir or os.path.join(os.path.dirname(db_path), "backups")
    os.makedirs(backup_dir, exist_ok=True)
    timestamp = datetime.now().strftime("%Y%m%d%H%M%S")
    backup_path = os.path.join(backup_dir, f"audit_{timestamp}.db")
    with get_connection(db_path) as conn:
        with sqlite3.connect(backup_path) as backup:
            conn.backup(backup)
    return backup_path


def json_dumps(data) -> str:
    return json.dumps(data, ensure_ascii=False) if data is not None else None


def json_loads(data: str):
    if data is None:
        return None
    try:
        return json.loads(data)
    except json.JSONDecodeError:
        return None
