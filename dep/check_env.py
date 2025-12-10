#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
环境检查脚本
检查所有必要的环境和依赖是否正确安装
"""

import sys
import subprocess
import platform
import os

def run_command(cmd, description):
    """运行命令并返回结果"""
    try:
        result = subprocess.run(cmd, shell=True, capture_output=True, text=True, timeout=10)
        return result.returncode == 0, result.stdout.strip(), result.stderr.strip()
    except subprocess.TimeoutExpired:
        return False, "", "命令执行超时"
    except Exception as e:
        return False, "", str(e)

def check_java():
    """检查Java环境"""
    print("[INFO] 检查JDK...")
    success, stdout, stderr = run_command("java -version", "Java版本检查")
    if success:
        # Java版本信息输出到stderr
        version_info = stderr.split('\n')[0] if stderr else stdout
        print(f"[OK] JDK: {version_info}")
        return True
    else:
        print("[ERROR] JDK未安装或配置不正确")
        print("   请安装JDK 17+: https://adoptium.net/")
        return False

def check_maven():
    """检查Maven"""
    print("[INFO] 检查Maven...")
    success, stdout, stderr = run_command("mvn -version", "Maven版本检查")
    if success:
        version_line = stdout.split('\n')[0] if stdout else ""
        print(f"[OK] Maven: {version_line}")
        return True
    else:
        print("[ERROR] Maven未安装或配置不正确")
        print("   请安装Maven 3.6+: https://maven.apache.org/download.cgi")
        return False

def check_python():
    """检查Python"""
    print("[INFO] 检查Python...")
    success, stdout, stderr = run_command("python --version", "Python版本检查")
    if not success:
        success, stdout, stderr = run_command("python3 --version", "Python3版本检查")

    if success:
        print(f"[OK] Python: {stdout}")
        return True
    else:
        print("[ERROR] Python未安装或配置不正确")
        print("   请安装Python 3.8+: https://www.python.org/downloads/")
        return False

def check_nodejs():
    """检查Node.js"""
    print("[INFO] 检查Node.js...")
    success, stdout, stderr = run_command("node --version", "Node.js版本检查")
    if success:
        print(f"[OK] Node.js: {stdout}")
        return True
    else:
        print("[ERROR] Node.js未安装或配置不正确")
        print("   请安装Node.js 16+: https://nodejs.org/")
        return False

def check_mysql():
    """检查MySQL"""
    print("[INFO] 检查MySQL...")
    success, stdout, stderr = run_command("mysql --version", "MySQL版本检查")
    if success:
        version_info = stdout.split('\n')[0] if stdout else ""
        print(f"[OK] MySQL: {version_info}")

        # 检查服务是否运行
        if platform.system() == "Windows":
            success, stdout, stderr = run_command("sc query MySQL80", "MySQL服务状态")
            if "RUNNING" in stdout:
                print("[OK] MySQL服务正在运行")
                return True
            else:
                print("[WARN] MySQL服务未启动，请启动MySQL80服务")
                return False
        else:
            # Linux/Mac检查
            success, stdout, stderr = run_command("systemctl is-active mysql 2>/dev/null || systemctl is-active mysqld 2>/dev/null || brew services list | grep mysql", "MySQL服务状态")
            if success and ("active" in stdout or "started" in stdout):
                print("[OK] MySQL服务正在运行")
                return True
            else:
                print("[WARN] MySQL服务未启动")
                return False
    else:
        print("[ERROR] MySQL未安装")
        print("   请安装MySQL 8.0: https://dev.mysql.com/downloads/mysql/")
        return False

def check_database_connection():
    """检查数据库连接"""
    print("[INFO] 检查数据库连接...")
    try:
        import mysql.connector
        conn = mysql.connector.connect(
            host="localhost",
            user="root",
            password="123456",
            database="hpims"
        )
        conn.close()
        print("[OK] 数据库连接正常")
        return True
    except ImportError:
        print("[WARN] mysql-connector-python未安装，尝试命令行连接...")
        success, stdout, stderr = run_command('mysql -u root -p123456 -e "SELECT 1;"', "数据库连接测试")
        if success:
            print("[OK] 数据库连接正常")
            return True
        else:
            print("[ERROR] 数据库连接失败")
            print("   请确保MySQL服务启动且密码正确")
            return False
    except Exception as e:
        print(f"[ERROR] 数据库连接失败: {e}")
        print("   请检查数据库配置和连接信息")
        return False

def check_project_structure():
    """检查项目结构"""
    print("[INFO] 检查项目结构...")

    required_dirs = [
        "backend/src/main/java",
        "backend/src/main/resources",
        "frontend/src",
        "python-audit-service/src",
        "database"
    ]

    required_files = [
        "backend/pom.xml",
        "backend/src/main/resources/application.yml",
        "frontend/package.json",
        "python-audit-service/requirements.txt",
        "database/init.sql"
    ]

    all_good = True

    for dir_path in required_dirs:
        if os.path.isdir(dir_path):
            print(f"[OK] 目录存在: {dir_path}")
        else:
            print(f"[ERROR] 目录缺失: {dir_path}")
            all_good = False

    for file_path in required_files:
        if os.path.isfile(file_path):
            print(f"[OK] 文件存在: {file_path}")
        else:
            print(f"[ERROR] 文件缺失: {file_path}")
            all_good = False

    return all_good

def main():
    """主函数"""
    print("=" * 50)
    print("  医院药房智能管理系统 - 环境检查")
    print("=" * 50)
    print()

    # 获取当前目录
    current_dir = os.getcwd()
    print(f"[INFO] 当前目录: {current_dir}")
    print()

    checks = [
        ("Java环境", check_java),
        ("Maven", check_maven),
        ("Python", check_python),
        ("Node.js", check_nodejs),
        ("MySQL", check_mysql),
        ("数据库连接", check_database_connection),
        ("项目结构", check_project_structure)
    ]

    results = []
    for name, check_func in checks:
        print(f"【{name}】")
        result = check_func()
        results.append(result)
        print()

    # 总结
    print("=" * 50)
    print("检查结果总结")
    print("=" * 50)

    passed = sum(results)
    total = len(results)

    print(f"[OK] 通过: {passed}/{total}")
    print(f"[ERROR] 失败: {total - passed}/{total}")
    print()

    if passed == total:
        print("[SUCCESS] 所有环境检查通过！可以启动项目了。")
        print()
        print("启动命令：")
        if platform.system() == "Windows":
            print("  双击运行 start.bat")
        else:
            print("  ./start.sh")
    else:
        print("[WARN] 部分环境检查失败，请根据上述提示修复问题。")
        print("[INFO] 详细配置指南：dep/环境搭建指南.md")

    print()
    print("按任意键退出...")
    try:
        input()
    except EOFError:
        pass  # 非交互环境自动退出

if __name__ == "__main__":
    main()
