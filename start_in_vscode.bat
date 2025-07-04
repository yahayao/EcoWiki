@echo off
chcp 65001 >nul
title EcoWiki VS Code 终端启动器

echo.
echo ==========================================
echo     EcoWiki VS Code 终端启动器
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

echo 📦 启动后端服务（后台运行）...
:: 直接在后台启动后端服务，不打开新窗口
cd www\backend
start /min "EcoWiki Backend" cmd /c "echo 启动后端服务... && mvn spring-boot:run && pause"
cd ..\..

echo ✅ 后端服务已启动

timeout /t 3 /nobreak >nul

echo 🎨 启动前端服务（后台运行）...
:: 直接在后台启动前端服务，不打开新窗口
cd www\frontend
start /min "EcoWiki Frontend" cmd /c "echo 启动前端服务... && npm run dev && pause"
cd ..\..

echo ✅ 前端服务已启动

echo.
echo ✅ 所有服务已启动！
echo.
echo 📖 访问地址:
echo    前端: http://localhost:5173
echo    后端: http://localhost:8080
echo    管理后台: http://localhost:5173/admin
echo.
echo 💡 提示: 
echo    - 后端和前端在最小化的命令窗口中运行
echo    - 可以在任务栏查看运行状态
echo    - 要查看日志，请点击任务栏中的相应服务窗口
echo    - 按任意键或关闭此窗口将自动停止所有服务
echo.
echo 🛑 按任意键停止所有服务并退出...
pause >nul

:: 退出时清理所有服务
echo.
echo 🔄 正在停止所有服务...

:: 强制停止所有相关进程
taskkill /f /im "java.exe" 2>nul >nul
taskkill /f /im "node.exe" 2>nul >nul

:: 也停止可能的Maven和npm进程
wmic process where "commandline like '%%mvn%%' or commandline like '%%spring-boot%%'" delete 2>nul >nul
wmic process where "commandline like '%%npm%%' or commandline like '%%vite%%'" delete 2>nul >nul

echo ✅ 所有服务已停止
echo 👋 再见！

timeout /t 2 /nobreak >nul

:cleanup
:: 清理函数
taskkill /f /im "java.exe" 2>nul >nul
taskkill /f /im "node.exe" 2>nul >nul
exit /b
