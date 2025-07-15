@echo off
chcp 65001
cls
echo ====================================
echo      EcoWiki 一键部署脚本
echo ====================================
echo.

REM 检查MySQL是否可用
echo 正在检查MySQL连接...
mysql --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未检测到MySQL，请确保MySQL已安装并在PATH中
    pause
    exit /b 1
)

echo MySQL检测成功！
echo.

REM 设置数据库连接参数
set /p DB_HOST="请输入数据库主机地址 (默认: localhost): "
if "%DB_HOST%"=="" set DB_HOST=localhost

set /p DB_PORT="请输入数据库端口 (默认: 3306): "
if "%DB_PORT%"=="" set DB_PORT=3306

set /p DB_NAME="请输入数据库名称 (默认: ecowiki): "
if "%DB_NAME%"=="" set DB_NAME=ecowiki

set /p DB_USER="请输入数据库用户名 (默认: root): "
if "%DB_USER%"=="" set DB_USER=root

echo.
echo ====================================
echo        数据库配置信息
echo ====================================
echo 主机: %DB_HOST%
echo 端口: %DB_PORT%
echo 数据库: %DB_NAME%
echo 用户: %DB_USER%
echo ====================================
echo.

set /p CONFIRM="确认以上配置是否正确？(y/n): "
if /i not "%CONFIRM%"=="y" (
    echo 部署已取消
    pause
    exit /b 0
)

echo.
echo 正在连接数据库...

REM 检查数据库连接
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p -e "SELECT 1;" >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 数据库连接失败，请检查连接参数和密码
    pause
    exit /b 1
)

echo 数据库连接成功！
echo.

REM 创建数据库（如果不存在）
echo 正在创建数据库...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p -e "CREATE DATABASE IF NOT EXISTS %DB_NAME% CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if %errorlevel% neq 0 (
    echo 错误: 数据库创建失败
    pause
    exit /b 1
)

echo 数据库创建成功！
echo.

REM 执行初始化脚本
echo 正在执行数据库初始化...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p %DB_NAME% < COMPLETE_DATABASE_INIT.sql
if %errorlevel% neq 0 (
    echo 错误: 数据库初始化失败
    pause
    exit /b 1
)

echo.
echo ====================================
echo        数据库初始化完成！
echo ====================================
echo.

REM 更新后端配置文件
echo 正在更新后端配置文件...
cd www\backend\src\main\resources

REM 创建本地配置文件
if not exist application-local.properties (
    echo 创建 application-local.properties...
    (
        echo # EcoWiki 数据库配置
        echo # 自动生成于 %date% %time%
        echo.
        echo # MySQL数据库配置
        echo spring.datasource.url=jdbc:mysql://%DB_HOST%:%DB_PORT%/%DB_NAME%?useSSL=true^&serverTimezone=Asia/Shanghai^&allowPublicKeyRetrieval=true^&useUnicode=true^&characterEncoding=utf8
        echo spring.datasource.username=%DB_USER%
        echo spring.datasource.password=your_password_here
        echo.
        echo # 请手动修改上方密码为实际密码
    ) > application-local.properties
    
    echo 配置文件已创建：www\backend\src\main\resources\application-local.properties
    echo 请手动编辑该文件，设置正确的数据库密码
) else (
    echo 配置文件已存在，跳过创建
)

cd ..\..\..\..\..\

echo.
echo ====================================
echo        部署完成！
echo ====================================
echo.
echo 数据库信息:
echo   数据库名: %DB_NAME%
echo   主机地址: %DB_HOST%:%DB_PORT%
echo.
echo 默认管理员账户:
echo   用户名: superadmin
echo   密码: EcoWiki@2025
echo   邮箱: admin@ecowiki.com
echo.
echo 重要提醒:
echo 1. 请立即登录系统并修改默认密码
echo 2. 请手动编辑 application-local.properties 设置数据库密码
echo 3. 确保后端配置正确后重启应用
echo.
echo 启动后端服务命令:
echo   cd www\backend
echo   mvn spring-boot:run
echo.
echo 启动前端服务命令:
echo   cd www\frontend  
echo   npm run dev
echo.
echo ====================================

pause
