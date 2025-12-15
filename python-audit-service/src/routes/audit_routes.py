"""
审核API路由

TODO: 田纹搭需要实现的其他路由类
=================================

新增资源类 (需要创建):
TODO: BatchAuditResource - 批量审核资源类
  - POST /api/prescription/batch-audit - 批量审核接口
  - 支持多处方同时提交
  - 返回批量审核结果和统计信息

TODO: AuditHistoryResource - 审核历史资源类
  - GET /api/audit/history/<prescription_id> - 单处方历史
  - GET /api/audit/history - 分页查询历史记录
  - 支持按时间范围、结果类型筛选

TODO: AuditRulesResource - 审核规则配置资源类
  - GET /api/audit/rules - 获取当前审核规则
  - PUT /api/audit/rules - 更新审核规则配置
  - POST /api/audit/rules/reset - 重置为默认规则

TODO: MedicineResource - 药品信息资源类
  - GET /api/medicines/<medicine_id> - 查询药品详情
  - GET /api/medicines/search - 药品搜索接口
  - GET /api/medicines/interactions - 相互作用查询

TODO: StatisticsResource - 统计分析资源类
  - GET /api/audit/statistics - 总体审核统计
  - GET /api/audit/statistics/daily - 每日统计
  - GET /api/audit/statistics/monthly - 每月统计
  - GET /api/audit/statistics/risk-trends - 风险趋势分析

TODO: HealthResource - 服务健康检查资源类
  - GET /api/health - 详细健康状态
  - GET /api/health/database - 数据库连接检查
  - GET /api/health/algorithm - 算法服务检查

工具类和中间件 (需要创建):
TODO: request_validators.py - 请求验证器
  - 处方数据格式验证
  - 参数校验装饰器
  - 错误处理统一格式

TODO: response_formatters.py - 响应格式化器
  - 统一API响应格式
  - 分页响应格式
  - 错误响应格式

TODO: rate_limit.py - 请求频率限制
  - 基于IP的频率限制
  - 基于用户的频率限制
  - 批量请求限制

数据库集成 (需要完善):
TODO: 集成数据库操作到路由中
  - 审核记录存储到数据库
  - 历史查询从数据库获取
  - 统计数据从数据库计算

性能优化 (需要实现):
TODO: 添加缓存机制
  - Redis缓存层 (如果需要)
  - 内存缓存频繁查询数据
  - 结果缓存减少重复计算

作者: 田纹搭 (Python审核服务负责人)
"""

import logging
from typing import Dict, Any, List, Optional

from flask import request, jsonify
from flask_restful import Resource, reqparse

from src.dao.repositories import AuditRecordDAO, PrescriptionDAO
from src.models import AuditIssueRecord, AuditRecord
from src.services.audit_service import AuditService, AuditResult, IssueSeverity

logger = logging.getLogger(__name__)


class AuditResource(Resource):
  """处方审核资源"""

  def __init__(self, audit_service: AuditService, audit_record_dao: AuditRecordDAO):
    self.audit_service = audit_service
    self.audit_record_dao = audit_record_dao

  def post(self):
    """审核处方"""
    try:
      data = request.get_json()
      if not data:
        return {'success': False, 'message': '请求数据不能为空'}, 400

      prescription_data = data.get('prescription')
      if not prescription_data:
        return {'success': False, 'message': '处方数据不能为空'}, 400

      audit_report = self.audit_service.audit_prescription(prescription_data)

      try:
        record_id = self._persist_audit_result(prescription_data, audit_report)
      except Exception as e:
        logger.error(f"审核结果持久化失败: {e}", exc_info=True)
        record_id = None

      result = {
        'success': True,
        'data': {
          'result': audit_report.result.value,
          'score': audit_report.score,
          'issues': [
            {
              'issue_type': issue.issue_type,
              'severity': issue.severity.value,
              'description': issue.description,
              'suggestion': issue.suggestion,
              'drug_name': issue.drug_name,
              'related_drugs': issue.related_drugs,
            }
            for issue in audit_report.issues
          ],
          'suggestions': audit_report.suggestions,
          'audit_time': audit_report.audit_time,
          'audit_record_id': record_id,
        },
      }

      logger.info(f"处方审核完成，结果: {audit_report.result.value}, 得分: {audit_report.score}")
      return result, 200

    except Exception as e:
      logger.error(f"审核处方时发生错误: {str(e)}")
      return {'success': False, 'message': f'审核失败: {str(e)}'}, 500

  def get(self):
    """获取审核服务状态"""
    return {
      'success': True,
      'data': {
        'service': 'audit-service',
        'status': 'running',
        'version': '1.0.0'
      }
    }, 200

  def _persist_audit_result(self, prescription_data: Dict[str, Any], audit_report):
    patient = prescription_data.get('patient', {}) or {}

    issues_dicts: List[Dict[str, Any]] = [
      {
        'issue_type': issue.issue_type,
        'severity': issue.severity.value,
        'description': issue.description,
        'suggestion': issue.suggestion,
        'drug_name': issue.drug_name,
        'related_drugs': issue.related_drugs,
      }
      for issue in audit_report.issues
    ]

    # MySQL/SQLite 兼容的时间格式（使用数据库默认值也可）
    audit_time_db = audit_report.audit_time.replace("T", " ").split(".")[0]

    record = AuditRecord(
      id=None,
      prescription_id=prescription_data.get('prescription_id') or prescription_data.get('id') or 0,
      audit_type='自动审核',
      audit_result=audit_report.result.value,
      audit_score=audit_report.score,
      issues_found=issues_dicts,
      suggestions=audit_report.suggestions,
      auditor='system',
      patient_age=patient.get('age'),
      patient_gender=patient.get('gender'),
      patient_conditions=patient.get('conditions', []),
      patient_allergies=patient.get('allergies', []),
      rule_version=None,
      engine_version='python-audit-service/1.0.0',
      audit_time=audit_time_db,
    )

    record_id = self.audit_record_dao.insert_record(record)

    issues_records = [
      AuditIssueRecord.from_service_issue(record_id, issue_dict)
      for issue_dict in issues_dicts
    ]
    self.audit_record_dao.bulk_insert_issues(issues_records)
    self.audit_record_dao.insert_snapshot(record_id, prescription_data)
    return record_id


class BatchAuditResource(Resource):
  """批量审核资源"""

  def __init__(self, audit_service: AuditService, audit_record_dao: AuditRecordDAO):
    self.audit_service = audit_service
    self.audit_record_dao = audit_record_dao

  def post(self):
    try:
      data = request.get_json()
      if not data:
        return {'success': False, 'message': '请求数据不能为空'}, 400

      prescriptions = data.get('prescriptions') or []
      if not isinstance(prescriptions, list) or not prescriptions:
        return {'success': False, 'message': 'prescriptions 列表不能为空'}, 400

      results = []
      for pres in prescriptions:
        audit_report = self.audit_service.audit_prescription(pres)
        try:
          record_id = self._persist_audit_result(pres, audit_report)
        except Exception as e:
          logger.error(f"批量审核持久化失败: {e}", exc_info=True)
          record_id = None

        results.append({
          'result': audit_report.result.value,
          'score': audit_report.score,
          'issues': [
            {
              'issue_type': issue.issue_type,
              'severity': issue.severity.value,
              'description': issue.description,
              'suggestion': issue.suggestion,
              'drug_name': issue.drug_name,
              'related_drugs': issue.related_drugs,
            }
            for issue in audit_report.issues
          ],
          'suggestions': audit_report.suggestions,
          'audit_time': audit_report.audit_time,
          'audit_record_id': record_id,
          'prescription_id': pres.get('prescription_id') or pres.get('id')
        })

      return {'success': True, 'data': {'results': results, 'total': len(results)}}, 200

    except Exception as e:
      logger.error(f"批量审核失败: {str(e)}", exc_info=True)
      return {'success': False, 'message': f'批量审核失败: {str(e)}'}, 500

  def _persist_audit_result(self, prescription_data: Dict[str, Any], audit_report):
    patient = prescription_data.get('patient', {}) or {}
    issues_dicts: List[Dict[str, Any]] = [
      {
        'issue_type': issue.issue_type,
        'severity': issue.severity.value,
        'description': issue.description,
        'suggestion': issue.suggestion,
        'drug_name': issue.drug_name,
        'related_drugs': issue.related_drugs,
      }
      for issue in audit_report.issues
    ]

    audit_time_db = audit_report.audit_time.replace("T", " ").split(".")[0]

    record = AuditRecord(
      id=None,
      prescription_id=prescription_data.get('prescription_id') or prescription_data.get('id') or 0,
      audit_type='自动审核',
      audit_result=audit_report.result.value,
      audit_score=audit_report.score,
      issues_found=issues_dicts,
      suggestions=audit_report.suggestions,
      auditor='system',
      patient_age=patient.get('age'),
      patient_gender=patient.get('gender'),
      patient_conditions=patient.get('conditions', []),
      patient_allergies=patient.get('allergies', []),
      rule_version=None,
      engine_version='python-audit-service/1.0.0',
      audit_time=audit_time_db,
    )

    record_id = self.audit_record_dao.insert_record(record)
    issues_records = [
      AuditIssueRecord.from_service_issue(record_id, issue_dict)
      for issue_dict in issues_dicts
    ]
    self.audit_record_dao.bulk_insert_issues(issues_records)
    self.audit_record_dao.insert_snapshot(record_id, prescription_data)
    return record_id


class AuditHistoryResource(Resource):
  """审核历史查询"""

  def __init__(self, audit_record_dao: AuditRecordDAO):
    self.audit_record_dao = audit_record_dao

  def get(self, prescription_id: Optional[int] = None):
    try:
      limit = request.args.get('limit', default=50, type=int)
      records = self.audit_record_dao.list_history(prescription_id=prescription_id, limit=limit)
      return {
        'success': True,
        'data': [
          {
            'id': r.id,
            'prescription_id': r.prescription_id,
            'audit_type': r.audit_type,
            'audit_result': r.audit_result,
            'audit_score': r.audit_score,
            'issues_found': r.issues_found,
            'suggestions': r.suggestions,
            'auditor': r.auditor,
            'patient_age': r.patient_age,
            'patient_gender': r.patient_gender,
            'patient_conditions': r.patient_conditions,
            'patient_allergies': r.patient_allergies,
            'rule_version': r.rule_version,
            'engine_version': r.engine_version,
            'audit_time': r.audit_time,
            'create_time': r.created_at,
          }
          for r in records
        ]
      }, 200
    except Exception as e:
      logger.error(f"查询审核历史失败: {str(e)}", exc_info=True)
      return {'success': False, 'message': f'查询审核历史失败: {str(e)}'}, 500


class PrescriptionQueryResource(Resource):
  """处方查询（供审核使用）"""

  def __init__(self, prescription_dao: PrescriptionDAO):
    self.prescription_dao = prescription_dao

  def get(self):
    try:
      args = request.args
      patient_name = args.get('patient_name')
      doctor_name = args.get('doctor_name')
      department = args.get('department')
      status = args.get('status')
      date_from = args.get('date_from')
      date_to = args.get('date_to')
      limit = args.get('limit', default=50, type=int)

      items = self.prescription_dao.search(
        patient_name=patient_name,
        doctor_name=doctor_name,
        department=department,
        status=status,
        date_from=date_from,
        date_to=date_to,
        limit=limit,
      )

      data = []
      for p in items:
        data.append({
          'prescription_id': p.prescription_id,
          'patient_name': getattr(p, 'patient_name', None),
          'patient': p.patient.to_dict(),
          'medicines': [m.to_dict() for m in p.medicines],
          'doctor_name': getattr(p, 'doctor_name', None),
          'department': getattr(p, 'department', None),
          'prescription_number': getattr(p, 'prescription_number', None),
          'create_date': getattr(p, 'create_date', None),
          'status': getattr(p, 'status', None),
        })

      return {'success': True, 'data': data, 'total': len(data)}, 200

    except Exception as e:
      logger.error(f"查询处方失败: {str(e)}", exc_info=True)
      return {'success': False, 'message': f'查询处方失败: {str(e)}'}, 500
