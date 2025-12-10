@echo off
echo ========================================
echo   医院药房智能管理系统 - 启动脚本
echo ========================================
echo.

cd /d %~dp0

echo [1/4] 检查环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ JDK未安装，请先安装JDK 17+
    pause
    exit /b 1
)

mvn -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Maven未安装，请先安装Maven 3.6+
    pause
    exit /b 1
)

python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Python未安装，请先安装Python 3.8+
    pause
    exit /b 1
)

npm --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Node.js未安装，请先安装Node.js 16+
    pause
    exit /b 1
)

echo ✅ 环境检查通过
echo.

echo [2/4] 初始化数据库...
if not exist "database\init.sql" (
    echo ❌ 数据库初始化文件不存在
    pause
    exit /b 1
)

echo 请确保MySQL服务已启动，然后按任意键继续...
pause >nul

mysql -u root -p123456 < database\init.sql 2>nul
if errorlevel 1 (
    echo ❌ 数据库初始化失败，请检查MySQL连接
    echo 提示：确保MySQL服务启动，root密码为123456
    pause
    exit /b 1
)

echo ✅ 数据库初始化完成
echo.

echo [3/4] 安装项目依赖...
echo 安装后端依赖...
cd backend
call mvn clean install -q
if errorlevel 1 (
    echo ❌ 后端依赖安装失败
    cd ..
    pause
    exit /b 1
)

echo 安装前端依赖...
cd ../frontend
call npm install --silent
if errorlevel 1 (
    echo ❌ 前端依赖安装失败
    cd ..
    pause
    exit /b 1
)

echo 安装Python依赖...
cd ../python-audit-service
call pip install -r requirements.txt --quiet
if errorlevel 1 (
    echo ❌ Python依赖安装失败
    cd ..
    pause
    exit /b 1
)

cd ..
echo ✅ 依赖安装完成
echo.

echo [4/4] 启动服务...
echo.

echo 启动后端服务 (端口8080)...
start "后端服务" cmd /k "cd backend && mvn spring-boot:run"

timeout /t 10 /nobreak >nul

echo 启动前端服务 (端口5173)...
start "前端服务" cmd /k "cd frontend && npm run dev"

timeout /t 5 /nobreak >nul

echo 启动Python审计服务 (端口5000)...
start "Python服务" cmd /k "cd python-audit-service && python src/app.py"

echo.
echo ========================================
echo 🎉 项目启动完成！
echo ========================================
echo.
echo 访问地址：
echo   前端界面: http://localhost:5173
echo   后端API:   http://localhost:8080
echo   Python服务: http://localhost:5000
echo.
echo 默认账号：
echo   用户名: admin
echo   密码: 123456
echo.
echo 按任意键打开浏览器...
pause >nul

start http://localhost:5173

echo.
echo 提示：如果服务启动失败，请查看对应的命令窗口错误信息
echo 配置文件位置：dep/环境搭建指南.md
echo.
pause

