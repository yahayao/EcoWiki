@echo off
chcp 65001 >nul
title EcoWiki Launcher

echo.
echo ============================================
echo        EcoWiki Dev Environment
echo        Backend: FastAPI  ^|  Frontend: Vite
echo ============================================
echo.

cd /d "%~dp0..\.."

if not exist "www\backend\main.py" (
    echo [ERROR] www\backend\main.py not found.
    echo         Run this script from the EcoWiki root directory.
    pause
    exit /b 1
)
if not exist "www\frontend\package.json" (
    echo [ERROR] www\frontend\package.json not found.
    pause
    exit /b 1
)

node -v >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Node.js not detected. Please install Node.js first.
    pause
    exit /b 1
)

set BACKEND_CMD=

where uv >nul 2>&1
if not errorlevel 1 (
    set "BACKEND_CMD=uv run uvicorn main:app --host 0.0.0.0 --port 8080 --reload"
    echo [OK] Python runtime: uv
    goto :python_ok
)

if exist "www\backend\.venv\Scripts\uvicorn.exe" (
    set "BACKEND_CMD=.venv\Scripts\uvicorn.exe main:app --host 0.0.0.0 --port 8080 --reload"
    echo [OK] Python runtime: .venv
    goto :python_ok
)

where python >nul 2>&1
if not errorlevel 1 (
    python -c "import uvicorn" >nul 2>&1
    if not errorlevel 1 (
        set "BACKEND_CMD=python -m uvicorn main:app --host 0.0.0.0 --port 8080 --reload"
        echo [OK] Python runtime: python (global)
        goto :python_ok
    )
    echo [INFO] Installing backend dependencies...
    pip install -r www\backend\requirements.txt -q
    set "BACKEND_CMD=python -m uvicorn main:app --host 0.0.0.0 --port 8080 --reload"
    goto :python_ok
)

echo [ERROR] No Python runtime found.
echo         Install uv: https://docs.astral.sh/uv/getting-started/installation/
pause
exit /b 1

:python_ok

if not exist "www\frontend\node_modules" (
    echo [INFO] First run - installing frontend dependencies...
    cd www\frontend
    call npm install
    if errorlevel 1 (
        echo [ERROR] npm install failed.
        cd ..\..
        pause
        exit /b 1
    )
    cd ..\..
    echo [OK] Frontend dependencies installed.
)

set TMP_DIR=C:\ecowiki_tmp
if not exist "%TMP_DIR%" mkdir "%TMP_DIR%"
set TMP_BACK=%TMP_DIR%\backend.bat
set TMP_FRONT=%TMP_DIR%\frontend.bat
set "PROJ_ROOT=%CD%\"

echo [INFO] Runtime detected: %BACKEND_CMD%

del "%TMP_BACK%" 2>nul
echo @echo off >> "%TMP_BACK%"
echo chcp 65001 ^>nul >> "%TMP_BACK%"
echo title EcoWiki Backend - FastAPI :8080 >> "%TMP_BACK%"
echo cd /d "%PROJ_ROOT%www\backend" >> "%TMP_BACK%"
echo echo [Backend] dir: %PROJ_ROOT%www\backend >> "%TMP_BACK%"
echo echo [Backend] cmd: %BACKEND_CMD% >> "%TMP_BACK%"
echo echo. >> "%TMP_BACK%"
echo %BACKEND_CMD% >> "%TMP_BACK%"
echo echo. >> "%TMP_BACK%"
echo echo [Backend] Process exited. Press any key to close... >> "%TMP_BACK%"
echo pause ^>nul >> "%TMP_BACK%"

del "%TMP_FRONT%" 2>nul
echo @echo off >> "%TMP_FRONT%"
echo chcp 65001 ^>nul >> "%TMP_FRONT%"
echo title EcoWiki Frontend - Vite :5173 >> "%TMP_FRONT%"
echo cd /d "%PROJ_ROOT%www\frontend" >> "%TMP_FRONT%"
echo echo [Frontend] dir: %PROJ_ROOT%www\frontend >> "%TMP_FRONT%"
echo echo. >> "%TMP_FRONT%"
echo npm run dev >> "%TMP_FRONT%"
echo echo. >> "%TMP_FRONT%"
echo echo [Frontend] Process exited. Press any key to close... >> "%TMP_FRONT%"
echo pause ^>nul >> "%TMP_FRONT%"

echo [INFO] Killing processes on port 8080 / 5173...
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr /r " :8080 "') do (
    taskkill /f /pid %%a >nul 2>&1
)
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr /r " :5173 "') do (
    taskkill /f /pid %%a >nul 2>&1
)

echo [INFO] Starting backend (FastAPI :8080)...
start "EcoWiki Backend" cmd /k "%TMP_BACK%"

echo [INFO] Waiting 8s for backend to initialize...
timeout /t 8 /nobreak >nul

echo [INFO] Starting frontend (Vite :5173)...
start "EcoWiki Frontend" cmd /k "%TMP_FRONT%"

echo.
echo ============================================
echo   Services started. Open in browser:
echo.
echo   Frontend    http://localhost:5173
echo   Backend     http://localhost:8080
echo   API Docs    http://localhost:8080/api/docs
echo   Messages    http://localhost:5173/messages
echo ============================================
echo.
echo   Service windows:
echo     "EcoWiki Backend"   - backend log (stays open on error)
echo     "EcoWiki Frontend"  - frontend log
echo.
echo   Press any key to stop all services and exit...
pause >nul

echo.
echo [INFO] Stopping services...
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr /r " :8080 "') do taskkill /f /pid %%a >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr /r " :5173 "') do taskkill /f /pid %%a >nul 2>&1
rmdir /s /q "%TMP_DIR%" >nul 2>&1
echo [OK] Done.
