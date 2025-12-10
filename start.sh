#!/bin/bash

echo "========================================"
echo "  医院药房智能管理系统 - 启动脚本"
echo "========================================"
echo

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "[1/4] 检查环境..."

# 检查JDK
if ! java -version >/dev/null 2>&1; then
    echo "❌ JDK未安装，请先安装JDK 17+"
    echo "下载地址: https://adoptium.net/"
    exit 1
fi

# 检查Maven
if ! mvn -version >/dev/null 2>&1; then
    echo "❌ Maven未安装，请先安装Maven 3.6+"
    echo "下载地址: https://maven.apache.org/download.cgi"
    exit 1
fi

# 检查Python
if ! python3 --version >/dev/null 2>&1 && ! python --version >/dev/null 2>&1; then
    echo "❌ Python未安装，请先安装Python 3.8+"
    echo "下载地址: https://www.python.org/downloads/"
    exit 1
fi

# 检查Node.js
if ! npm --version >/dev/null 2>&1; then
    echo "❌ Node.js未安装，请先安装Node.js 16+"
    echo "下载地址: https://nodejs.org/"
    exit 1
fi

echo "✅ 环境检查通过"
echo

echo "[2/4] 初始化数据库..."
if [ ! -f "database/init.sql" ]; then
    echo "❌ 数据库初始化文件不存在"
    exit 1
fi

echo "请确保MySQL服务已启动，然后按回车键继续..."
read -r

# 尝试连接数据库并执行初始化
if mysql -u root -p123456 < database/init.sql 2>/dev/null; then
    echo "✅ 数据库初始化完成"
else
    echo "❌ 数据库初始化失败"
    echo "提示：确保MySQL服务启动，root密码为123456"
    echo "或者手动执行: mysql -u root -p < database/init.sql"
    exit 1
fi

echo

echo "[3/4] 安装项目依赖..."

echo "安装后端依赖..."
cd backend
if mvn clean install -q; then
    echo "✅ 后端依赖安装完成"
else
    echo "❌ 后端依赖安装失败"
    cd ..
    exit 1
fi

echo "安装前端依赖..."
cd ../frontend
if npm install --silent; then
    echo "✅ 前端依赖安装完成"
else
    echo "❌ 前端依赖安装失败"
    cd ..
    exit 1
fi

echo "安装Python依赖..."
cd ../python-audit-service
if pip3 install -r requirements.txt --quiet 2>/dev/null || pip install -r requirements.txt --quiet 2>/dev/null; then
    echo "✅ Python依赖安装完成"
else
    echo "❌ Python依赖安装失败"
    cd ..
    exit 1
fi

cd ..
echo

echo "[4/4] 启动服务..."
echo

echo "启动后端服务 (端口8080)..."
cd backend
mvn spring-boot:run &
BACKEND_PID=$!
cd ..

echo "等待10秒..."
sleep 10

echo "启动前端服务 (端口5173)..."
cd frontend
npm run dev &
FRONTEND_PID=$!
cd ..

echo "等待5秒..."
sleep 5

echo "启动Python审计服务 (端口5000)..."
cd python-audit-service
python3 src/app.py &
PYTHON_PID=$!
cd ..

echo
echo "========================================"
echo "🎉 项目启动完成！"
echo "========================================"
echo
echo "访问地址："
echo "  前端界面: http://localhost:5173"
echo "  后端API:   http://localhost:8080"
echo "  Python服务: http://localhost:5000"
echo
echo "默认账号："
echo "  用户名: admin"
echo "  密码: 123456"
echo

# 检查系统类型并打开浏览器
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    open http://localhost:5173
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux
    if command -v xdg-open >/dev/null 2>&1; then
        xdg-open http://localhost:5173
    elif command -v firefox >/dev/null 2>&1; then
        firefox http://localhost:5173
    fi
fi

echo "服务进程ID："
echo "  后端: $BACKEND_PID"
echo "  前端: $FRONTEND_PID"
echo "  Python: $PYTHON_PID"
echo
echo "提示：按 Ctrl+C 停止所有服务"
echo "配置文件位置：dep/环境搭建指南.md"
echo

# 等待用户中断
trap 'echo ""; echo "正在停止服务..."; kill $BACKEND_PID $FRONTEND_PID $PYTHON_PID 2>/dev/null; exit 0' INT
wait



