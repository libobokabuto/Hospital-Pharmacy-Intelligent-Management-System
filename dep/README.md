# 🚀 环境配置和启动指南

## 📁 文件说明

本目录包含了完整的环境配置和项目启动指南：

### 📄 文档文件
- **[环境搭建指南.md](环境搭建指南.md)** - 详细的环境安装步骤
- **[数据库配置.md](数据库配置.md)** - 数据库架构和配置说明
- **[启动指南.md](启动指南.md)** - 项目启动和运行指南

### 🛠 工具文件
- **[check_env.py](check_env.py)** - 环境检查脚本（推荐先运行）

### 🔧 启动脚本
- **start.bat** (根目录) - Windows一键启动脚本
- **start.sh** (根目录) - Linux/Mac启动脚本

---

## 🎯 快速开始

### 第1步：检查环境
```bash
python dep/check_env.py
```

### 第2步：一键启动
**Windows用户**：
```
双击运行 start.bat
```

**Linux/Mac用户**：
```bash
chmod +x start.sh
./start.sh
```

### 第3步：访问系统
打开浏览器访问：http://localhost:5173

**默认账号**：
- 用户名：admin
- 密码：123456

---

## 📋 环境要求

| 组件 | 版本要求 | 下载地址 |
|------|----------|----------|
| JDK | 17+ | https://adoptium.net/ |
| MySQL | 8.0 | https://dev.mysql.com/downloads/mysql/ |
| Maven | 3.6+ | https://maven.apache.org/download.cgi |
| Python | 3.8+ | https://www.python.org/downloads/ |
| Node.js | 16+ | https://nodejs.org/ |

---

## 🏗 项目架构

```
Hospital-Pharmacy-Intelligent-Management-System/
├── backend/                    # Java Spring Boot后端
├── frontend/                   # Vue.js前端
├── python-audit-service/       # Python审计服务
├── database/                   # 数据库脚本
├── dep/                       # 环境配置文档
│   ├── 环境搭建指南.md
│   ├── 数据库配置.md
│   ├── 启动指南.md
│   └── check_env.py
├── start.bat                  # Windows启动脚本
├── start.sh                   # Linux/Mac启动脚本
└── README.md
```

---

## 🔧 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 | 5173 | Vue开发服务器 |
| 后端 | 8080 | Spring Boot API |
| Python审计 | 5000 | Flask API服务 |
| MySQL | 3306 | 数据库 |

---

## 🚨 常见问题

### Q: 环境检查失败怎么办？
A: 按照 `环境搭建指南.md` 逐步安装缺失的组件

### Q: 数据库连接失败？
A: 检查MySQL服务是否启动，密码是否正确

### Q: 项目启动失败？
A: 查看对应服务的控制台错误信息

### Q: 端口被占用？
A: 修改配置文件中的端口号，或关闭占用进程

---

## 📞 获取帮助

1. 先运行环境检查脚本定位问题
2. 查看对应的详细文档
3. 检查控制台错误信息
4. 如需帮助，请提供具体的错误信息

---

## 🎉 祝你成功！

按照这个指南操作，你很快就能在本地运行完整的医院药房管理系统了！

**文档版本**: v1.0
**最后更新**: 2024年12月9日

