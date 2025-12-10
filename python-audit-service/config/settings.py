"""
配置文件
"""

import os
from typing import List, Dict, Any


class Config:
    """基础配置类"""

    # Flask配置
    SECRET_KEY = os.getenv('SECRET_KEY', 'dev-secret-key-change-in-production')
    DEBUG = os.getenv('FLASK_DEBUG', 'False').lower() == 'true'

    # 服务配置
    SERVICE_NAME = 'hpims-audit-service'
    SERVICE_VERSION = '1.0.0'

    # 审核规则配置
    AUDIT_RULES = {
        'max_daily_dosage': {
            'enabled': True,
            'warning_threshold': 0.8,  # 80% of max dosage
            'error_threshold': 1.0     # 100% of max dosage
        },
        'drug_interaction': {
            'enabled': True,
            'severity_levels': ['minor', 'moderate', 'major', 'contraindicated']
        },
        'age_restrictions': {
            'enabled': True,
            'check_pediatric': True,
            'check_geriatric': True
        },
        'allergy_check': {
            'enabled': True,
            'known_allergens': ['penicillin', 'sulfa', 'aspirin']
        },
        'duplicate_therapy': {
            'enabled': True,
            'max_same_class_drugs': 2
        }
    }

    # 评分权重配置
    SCORING_WEIGHTS = {
        'compatibility': 0.3,
        'dosage_safety': 0.25,
        'drug_interactions': 0.25,
        'patient_safety': 0.2
    }

    # 审核结果阈值
    AUDIT_THRESHOLDS = {
        'pass': 0.8,      # >= 80% 通过
        'warning': 0.6,   # 60-79% 警告
        'reject': 0.6     # < 60% 驳回
    }

    # 药品数据库路径（如果使用本地文件）
    DRUG_DATABASE_PATH = os.path.join(os.path.dirname(__file__), '..', 'data', 'drugs.csv')
    INTERACTION_DATABASE_PATH = os.path.join(os.path.dirname(__file__), '..', 'data', 'interactions.csv')


class DevelopmentConfig(Config):
    """开发环境配置"""
    DEBUG = True
    LOG_LEVEL = 'DEBUG'


class ProductionConfig(Config):
    """生产环境配置"""
    DEBUG = False
    LOG_LEVEL = 'INFO'


# 配置映射
config = {
    'development': DevelopmentConfig,
    'production': ProductionConfig,
    'default': DevelopmentConfig
}


def get_config(config_name: str = None) -> Config:
    """获取配置实例"""
    if config_name is None:
        config_name = os.getenv('FLASK_ENV', 'default')

    return config.get(config_name, config['default'])()
