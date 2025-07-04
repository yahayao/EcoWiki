@echo off
chcp 65001 >nul
title EcoWiki 快速重启

echo.
echo ==========================================
echo        EcoWiki 快速重启程序
echo ==========================================
echo.

cd /d "%~dp0"

echo 🔄 正在停止现有服务...

:: 更精确地停止EcoWiki相关进程
echo 检查并停止EcoWiki后端服务...
wmic process where "commandline like '%%ecowiki-backend%%' or commandline like '%%spring-boot:run%%'" delete 2>nul >nul
if %errorlevel%==0 (
    echo ✅ 后端服务已停止
) else (
    echo ℹ️  未找到运行中的后端服务
)

echo 检查并停止前端开发服务器...
wmic process where "commandline like '%%npm run dev%%' or commandline like '%%vite%%'" delete 2>nul >nul
if %errorlevel%==0 (
    echo ✅ 前端服务已停止
) else (
    echo ℹ️  未找到运行中的前端服务
)

:: 额外清理：停止可能残留的Maven和Node进程
for /f "tokens=2" %%i in ('tasklist /fi "imagename eq java.exe" /fo csv ^| findstr "mvn"') do (
    taskkill /f /pid %%i 2>nul >nul
)

for /f "tokens=2" %%i in ('tasklist /fi "imagename eq node.exe" /fo csv ^| findstr "vite\|dev"') do (
    taskkill /f /pid %%i 2>nul >nul
)

echo ✅ 清理完成

echo.
echo 🚀 重新启动服务...
echo.

:: 检查是否已经编译过后端
if not exist "www\backend\target\ecowiki-backend-*.jar" (
    echo 📦 后端未编译，先进行快速编译...
    cd www\backend
    mvn compile -q
    if errorlevel 1 (
        echo ❌ 后端编译失败，尝试使用mvn spring-boot:run启动
        start "EcoWiki Backend" cmd /c "mvn spring-boot:run"
    ) else (
        echo ✅ 编译完成，使用spring-boot:run启动
        start "EcoWiki Backend" cmd /c "mvn spring-boot:run"
    )
    cd ..\..
) else (
    echo 📦 检测到已编译的JAR文件，使用快速启动...
    cd www\backend
    :: 尝试直接运行JAR文件（更快）
    for %%f in (target\ecowiki-backend-*.jar) do (
        start "EcoWiki Backend" cmd /c "java -jar %%f"
        goto backend_started
    )
    :: 如果JAR方式失败，回退到mvn方式
    start "EcoWiki Backend" cmd /c "mvn spring-boot:run"
    :backend_started
    cd ..\..
)

:: 等待后端启动
echo 🕐 等待后端服务启动...
:: 等待后端启动
echo 🕐 等待后端服务启动...
:: 检查后端是否启动成功
set retry_count=0
:check_backend
set /a retry_count+=1
timeout /t 2 /nobreak >nul

:: 检查8080端口是否被监听
netstat -an | findstr ":8080.*LISTENING" >nul
if %errorlevel%==0 (
    echo ✅ 后端服务启动成功 ^(端口8080已监听^)
    goto start_frontend
)

if %retry_count% LSS 15 (
    echo 🕐 等待中... ^(%retry_count%/15^)
    goto check_backend
)

echo ⚠️  后端服务启动超时，但继续启动前端服务

:start_frontend
echo.
echo 🎨 启动前端开发服务器...
cd www\frontend

:: 检查前端依赖
if not exist "node_modules" (
    echo 📦 前端依赖缺失，正在安装...
    npm install --silent
    if errorlevel 1 (
        echo ❌ 前端依赖安装失败
        pause
        exit /b 1
    )
)

start "EcoWiki Frontend" cmd /c "npm run dev"
cd ..\..

echo.
echo ✅ 服务重启完成！
echo.
echo 📖 访问地址:
echo    前端: http://localhost:5173
echo    后端: http://localhost:8080
echo    管理后台: http://localhost:5173/admin
echo.
echo 💡 提示: 
echo    - 前端热重载已启用，修改代码会自动刷新
echo    - 如需停止服务，请关闭对应的命令行窗口
echo    - 或使用 Ctrl+C 在各自窗口中停止服务

timeout /t 5 /nobreak >nul
