import os
import sys
from pathlib import Path

# 保证能 import src.database
ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

from src import database

# 可通过环境变量覆盖，默认与 backend application.yml 一致
cfg = {
    "AUDIT_DB_ENGINE": os.getenv("AUDIT_DB_ENGINE", "mysql"),
    "AUDIT_DB_HOST": os.getenv("AUDIT_DB_HOST", "localhost"),
    "AUDIT_DB_PORT": int(os.getenv("AUDIT_DB_PORT", "3306")),
    "AUDIT_DB_USER": os.getenv("AUDIT_DB_USER", "root"),
    "AUDIT_DB_PASSWORD": os.getenv("AUDIT_DB_PASSWORD", "040918"),
    "AUDIT_DB_NAME": os.getenv("AUDIT_DB_NAME", "hpims"),
}

print("Using config:", cfg)

os.environ.update({k: str(v) for k, v in cfg.items()})

with database.get_connection() as conn:
    with conn.cursor() as cur:
        # 1) 简单连通性
        cur.execute("SELECT 1 AS ok")
        print("Ping:", cur.fetchone())

        # 2) 查询处方数量
        cur.execute("SELECT COUNT(*) AS cnt FROM prescription;")
        print("Prescription count:", cur.fetchone())

        # 3) 插入一条审核记录并回滚以免污染
        cur.execute(
            """
            INSERT INTO audit_record (prescription_id, audit_type, audit_result)
            VALUES (1, '自动审核', '测试脚本');
            """
        )
    conn.rollback()  # 不保留测试数据
    print("Insert/rollback test passed")