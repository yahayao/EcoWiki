@echo off
chcp 65001 >nul
title EcoWiki 开发环境启动器

echo.
echo ==========================================
echo     🌱 EcoWiki 开发环境启动器
echo ==========================================
echo     版本: v2.0 (包含自动依赖更新)
echo     功能: 自动更新依赖 + 启动前后端服务
echo ==========================================
echo.

cd /d "%~dp0"

:: 设置退出时自动清理
set backend_pid=
set frontend_pid=

:: 捕获 Ctrl+C 信号，确保退出时停止所有服务
if "%1"=="cleanup" goto cleanup

echo 🔄 正在停止现有服务...

:: 停止现有的Java和Node进程
taskkill /f /im "java.exe" 2>nul >nul
taskkill /f /im "node.exe" 2>nul >nul

echo ✅ 现有服务已停止

echo.
echo 🚀 在VS Code终端中启动服务...
echo.

:: 确保在项目根目录
if not exist "www\backend" (
    echo ❌ 未找到后端目录
    echo 💡 请确保在EcoWiki项目根目录运行此脚本
    pause
    exit /b 1
)

if not exist "www\frontend" (
    echo ❌ 未找到前端目录
    echo 💡 请确保在EcoWiki项目根目录运行此脚本
    pause
    exit /b 1
)

echo.
echo ==========================================
echo     📦 更新依赖包
echo ==========================================
echo.

echo 🔄 更新后端依赖包...
cd www\backend
echo    - 检查Maven依赖更新...
call mvn dependency:resolve-sources -q
if errorlevel 1 (
    echo    ⚠️  Maven依赖解析警告，继续执行...
) else (
    echo    ✅ Maven依赖检查完成
)
cd ..\..

echo.
echo 🔄 更新前端依赖包...
cd www\frontend
echo    - 检查npm依赖更新...
call npm install --silent
if errorlevel 1 (
    echo    ❌ npm依赖安装失败
    echo    💡 请检查网络连接或依赖配置
    cd ..\..
    pause
    exit /b 1
) else (
    echo    ✅ npm依赖安装完成
)
cd ..\..

echo.
echo ==========================================
echo     🚀 启动服务
echo ==========================================
echo.

echo 📦 启动后端服务（后台运行）...
echo    - 编译并启动Spring Boot应用...
:: 直接在后台启动后端服务，不打开新窗口
cd www\backend
start /min "EcoWiki Backend" cmd /c "echo [后端服务] 正在启动Spring Boot应用... && mvn spring-boot:run && echo [后端服务] 服务已停止 && pause"
cd ..\..

echo ✅ 后端服务启动命令已执行
echo    - 服务正在后台启动中，请稍候...

echo.
echo ⏰ 等待后端服务初始化...
timeout /t 5 /nobreak >nul

echo 🎨 启动前端服务（后台运行）...
echo    - 启动Vite开发服务器...
:: 直接在后台启动前端服务，不打开新窗口
cd www\frontend
start /min "EcoWiki Frontend" cmd /c "echo [前端服务] 正在启动Vite开发服务器... && npm run dev && echo [前端服务] 服务已停止 && pause"
cd ..\..

echo ✅ 前端服务启动命令已执行
echo    - 服务正在后台启动中，请稍候...

echo.
echo ==========================================
echo     ✅ 服务启动完成
echo ==========================================
echo.
echo 📖 访问地址:
echo    🌐 前端应用:     http://localhost:5173
echo    🔧 后端API:      http://localhost:8080
echo    👤 管理后台:     http://localhost:5173/admin
echo    📊 API文档:      http://localhost:8080/swagger-ui.html
echo.
echo 📱 服务状态监控:
echo    - 后端服务窗口: "EcoWiki Backend" (最小化)
echo    - 前端服务窗口: "EcoWiki Frontend" (最小化)
echo.
echo 💡 使用提示: 
echo    - 服务在后台最小化窗口中运行
echo    - 可在任务栏查看运行状态和日志
echo    - 点击任务栏中的服务窗口可查看详细日志
echo    - 首次启动可能需要1-2分钟完成初始化
echo    - 如遇问题，请检查任务栏中的服务窗口错误信息
echo.
echo ⏰ 等待服务完全启动中...
timeout /t 3 /nobreak >nul
echo.
echo 🛑 按任意键停止所有服务并退出...
pause >nul

:: 退出时清理所有服务
echo.
echo ==========================================
echo     🔄 正在停止所有服务
echo ==========================================
echo.

echo 🛑 停止后端服务...
:: 强制停止所有相关进程
taskkill /f /im "java.exe" 2>nul >nul
echo    ✅ Java进程已停止

echo 🛑 停止前端服务...
taskkill /f /im "node.exe" 2>nul >nul
echo    ✅ Node.js进程已停止

echo 🧹 清理相关进程...
:: 也停止可能的Maven和npm进程
wmic process where "commandline like '%%mvn%%' or commandline like '%%spring-boot%%'" delete 2>nul >nul
wmic process where "commandline like '%%npm%%' or commandline like '%%vite%%'" delete 2>nul >nul
echo    ✅ 构建工具进程已清理

echo.
echo ✅ 所有服务已安全停止
echo 👋 感谢使用 EcoWiki！

timeout /t 2 /nobreak >nul

:cleanup
:: 清理函数 - 安全停止所有相关服务
echo 🧹 执行清理程序...
taskkill /f /im "java.exe" 2>nul >nul
taskkill /f /im "node.exe" 2>nul >nul
wmic process where "commandline like '%%mvn%%' or commandline like '%%spring-boot%%'" delete 2>nul >nul
wmic process where "commandline like '%%npm%%' or commandline like '%%vite%%'" delete 2>nul >nul
echo ✅ 清理完成
exit /b
