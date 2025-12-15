# Python审核服务 (Python Audit Service)

## 概述

Python审核服务是HPIMS系统的核心智能审核模块，基于Flask框架开发，提供处方智能审核算法，包括药品兼容性检查、用量合理性验证、药品相互作用检测和患者安全性评估。

## 技术栈

- **框架**: Flask 3.1+
- **Python版本**: Python 3.8+
- **数据库**: MySQL 8.0 (与Java后端共享) / SQLite (本地调试)
- **数据库驱动**: PyMySQL 1.1+
- **CORS支持**: flask-cors 6.0+
- **RESTful API**: Flask-RESTful 0.3+

## 项目结构

```
python-audit-service/
├── src/
│   ├── app.py                      # Flask应用入口
│   ├── models.py                   # 数据模型定义
│   ├── database.py                 # 数据库连接和操作
│   ├── database_test.py            # 数据库测试脚本
│   ├── routes/                     # API路由
│   │   ├── __init__.py
│   │   └── audit_routes.py         # 审核相关路由
│   ├── services/                   # 业务逻辑层
│   │   ├── __init__.py
│   │   └── audit_service.py       # 审核算法核心实现
│   └── dao/                        # 数据访问层
│       ├── __init__.py
│       └── repositories.py         # 数据访问对象
├── config/                         # 配置文件
│   ├── __init__.py
│   └── settings.py                 # 应用配置
├── requirements.txt                # Python依赖
├── test-client.html                # 测试客户端页面
└── README.md                       # 本文档
```

## 核心功能

### 1. 处方智能审核

审核服务对处方进行多维度智能分析：

#### 1.1 药品兼容性检查

- **重复药品检测**: 检查处方中是否存在重复药品
- **分类过度使用**: 检测同一分类药品是否使用过多
- **药品冲突**: 识别可能产生冲突的药品组合

**评分机制**: 基础分25分，发现问题扣分

#### 1.2 用量合理性验证

- **单次剂量检查**: 验证单次用药剂量是否在推荐范围内
- **日总剂量检查**: 验证每日总剂量是否超标
- **规格折算**: 根据药品规格自动折算有效剂量
- **剂量单位处理**: 支持mg、g、ml等单位的智能解析

**评分机制**: 基础分25分，超量扣分（超量50%扣25分，超量20%扣10分）

#### 1.3 药品相互作用检测

- **数据库驱动**: 基于 `medicine_interaction` 表的相互作用知识库
- **风险等级评估**: 根据风险等级（1-3级）进行扣分
  - 风险等级3: 扣25分（高风险）
  - 风险等级2: 扣13分（中风险）
  - 风险等级1: 扣9分（低风险）

**评分机制**: 基础分25分，发现相互作用按风险等级扣分

#### 1.4 患者安全性检查

- **疾病禁忌检查**: 检查患者疾病是否与药品禁忌症冲突
- **适应症匹配**: 验证药品适应症是否与患者疾病匹配
- **过敏史检查**: 检查患者过敏史（预留功能）

**评分机制**: 基础分25分，发现禁忌症扣25分

### 2. 审核结果

审核服务返回以下结果：

- **审核结果**: `pass`（通过）、`warning`（警告）、`reject`（拒绝）
- **审核评分**: 0-100分
- **问题列表**: 发现的所有问题及严重程度
- **建议**: 针对问题的处理建议

**评分阈值**:
- `pass`: 得分 = 100分（无问题）
- `warning`: 得分 > 75分（存在风险，需人工审核）
- `reject`: 得分 ≤ 75分（存在严重问题，建议驳回）

### 3. 数据持久化

- **审核记录存储**: 将审核结果保存到 `audit_record` 表
- **问题明细存储**: 将发现的问题保存到 `audit_issue` 表
- **审核快照**: 保存处方原始数据到 `audit_snapshot` 表
- **处方状态同步**: 自动更新处方表的审核状态和结果

## API接口

### 1. 单个处方审核

**接口**: `POST /api/prescription/audit`

**请求体**:
```json
{
  "prescription": {
    "prescription_id": 1,
    "patient": {
      "name": "张三",
      "age": 35,
      "gender": "男",
      "conditions": ["高血压", "糖尿病"],
      "allergies": []
    },
    "medicines": [
      {
        "medicine_id": 1,
        "name": "阿莫西林胶囊",
        "dosage": "每次2粒",
        "frequency": "每日3次",
        "days": 7
      }
    ]
  }
}
```

**响应**:
```json
{
  "success": true,
  "data": {
    "result": "pass",
    "score": 95.5,
    "issues": [],
    "suggestions": ["建议按时服药"],
    "audit_time": "2024-12-09T12:00:00",
    "audit_record_id": 1
  }
}
```

### 2. 批量审核

**接口**: `POST /api/prescription/batch-audit`

**请求体**:
```json
{
  "prescriptions": [
    { "prescription": {...} },
    { "prescription": {...} }
  ]
}
```

**响应**: 返回批量审核结果数组

### 3. 审核历史查询

**接口**: `GET /api/audit/history?prescription_id=1&limit=50`

**响应**: 返回审核历史记录列表

### 4. 处方查询

**接口**: `GET /api/prescriptions?patient_name=张三&status=未审核`

**响应**: 返回符合条件的处方列表

### 5. 健康检查

**接口**: `GET /health`

**响应**:
```json
{
  "status": "healthy",
  "service": "audit-service"
}
```

## 数据库集成

### 数据库配置

服务支持两种数据库模式：

1. **MySQL模式**（生产环境）: 与Java后端共享数据库
2. **SQLite模式**（本地调试）: 使用本地SQLite数据库

### 环境变量配置

```bash
# 数据库引擎 (mysql 或 sqlite)
AUDIT_DB_ENGINE=mysql

# MySQL配置
AUDIT_DB_HOST=localhost
AUDIT_DB_PORT=3306
AUDIT_DB_USER=root
AUDIT_DB_PASSWORD=your_password
AUDIT_DB_NAME=hpims

# SQLite配置（仅sqlite模式）
AUDIT_DB_PATH=./data/audit.db
```

### 数据访问层

服务通过DAO层访问数据库：

- `MedicineDAO`: 药品信息查询
- `AuditRecordDAO`: 审核记录操作
- `PrescriptionDAO`: 处方数据操作

## 审核算法详解

### 剂量计算逻辑

1. **解析原始剂量**: 从字符串中提取数值（支持"每次2粒"、"10% 500ml"等格式）
2. **规格折算**: 根据药品规格（如"0.25g*24粒"）计算有效含量
3. **单位统一**: 将质量单位统一为g，体积单位统一为ml
4. **日总剂量**: 单次剂量 × 频次 × 天数

### 相互作用检测

1. 从 `medicine_interaction` 表查询药品相互作用
2. 检查处方中是否存在相互作用的药品组合
3. 根据风险等级进行评分和警告

### 禁忌症检查

1. 从 `medicine_contraindication` 表查询药品禁忌症
2. 检查患者疾病是否与禁忌症匹配
3. 发现匹配则标记为高风险

## 启动步骤

### 1. 环境要求

- Python 3.8+
- pip

### 2. 安装依赖

```bash
cd python-audit-service
pip install -r requirements.txt
```

### 3. 配置数据库

编辑 `config/settings.py` 或设置环境变量配置数据库连接。

### 4. 运行服务

```bash
# 开发模式
python src/app.py

# 或使用Flask CLI
export FLASK_APP=src/app.py
flask run --host=0.0.0.0 --port=5000
```

### 5. 验证启动

访问 http://localhost:5000/health 检查服务状态。

## 配置说明

### settings.py 配置

```python
class Config:
    # Flask配置
    SECRET_KEY = 'your-secret-key'
    DEBUG = True
    
    # 审核规则配置
    AUDIT_RULES = {
        'max_daily_dosage': {
            'enabled': True,
            'warning_threshold': 0.8,
            'error_threshold': 1.0
        },
        'drug_interaction': {
            'enabled': True,
            'severity_levels': ['minor', 'moderate', 'major', 'contraindicated']
        }
    }
    
    # 评分权重
    SCORING_WEIGHTS = {
        'compatibility': 0.3,
        'dosage_safety': 0.25,
        'drug_interactions': 0.25,
        'patient_safety': 0.2
    }
```

## 测试

### 使用测试客户端

打开 `test-client.html` 在浏览器中进行API测试。

### 命令行测试

```bash
# 测试健康检查
curl http://localhost:5000/health

# 测试审核接口
curl -X POST http://localhost:5000/api/prescription/audit \
  -H "Content-Type: application/json" \
  -d @test_prescription.json
```

## 与Java后端集成

Java后端通过 `AuditServiceClient` 调用Python服务：

```java
// 提交处方审核
String url = auditServiceUrl + "/api/prescription/audit";
RestTemplate restTemplate = new RestTemplate();
ResponseEntity<AuditResultDto> response = restTemplate.postForEntity(
    url, request, AuditResultDto.class
);
```

## 算法优化方向

### 已实现

- ✅ 基础审核算法
- ✅ 剂量合理性检查
- ✅ 药品相互作用检测
- ✅ 患者安全性检查
- ✅ 数据持久化

### 待优化

- ⏳ 机器学习模型集成
- ⏳ 更复杂的剂量计算（考虑体重、肾功能）
- ⏳ 药品-疾病相互作用扩展
- ⏳ 审核规则配置化
- ⏳ 性能优化和缓存机制

## 开发规范

### 代码规范

- 遵循PEP 8规范
- 使用类型提示（Type Hints）
- 函数和类添加文档字符串
- 异常处理完善

### 日志规范

使用Python标准logging模块：

```python
import logging
logger = logging.getLogger(__name__)
logger.info("审核完成")
logger.error("审核失败", exc_info=True)
```

## 常见问题

### 1. 数据库连接失败

检查：
- MySQL服务是否运行
- 数据库配置是否正确
- 网络连接是否正常

### 2. 审核结果不准确

- 检查药品知识库数据是否完整
- 验证剂量计算逻辑
- 查看日志了解详细处理过程

### 3. 性能问题

- 考虑添加缓存机制
- 优化数据库查询
- 使用异步处理批量审核

## 开发计划

- ✅ Flask应用框架搭建
- ✅ 基础审核算法实现
- ✅ 数据库集成
- ✅ API接口开发
- ✅ 与Java后端集成
- ⏳ 算法准确性优化
- ⏳ 机器学习模型集成
- ⏳ 性能优化

## 联系方式

如有问题，请联系Python审核服务开发负责人。

