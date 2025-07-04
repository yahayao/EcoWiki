# EcoWiki 快速重启 PowerShell 脚本
# 智能检测和重启前后端服务

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$Host.UI.RawUI.WindowTitle = "EcoWiki 快速重启程序"

# 定义颜色函数
function Write-ColorText {
    param([string]$Text, [ConsoleColor]$Color = "White")
    Write-Host $Text -ForegroundColor $Color
}

function Write-Success { param([string]$Text); Write-ColorText "✅ $Text" -Color Green }
function Write-Error { param([string]$Text); Write-ColorText "❌ $Text" -Color Red }
function Write-Info { param([string]$Text); Write-ColorText "📘 $Text" -Color Cyan }
function Write-Warning { param([string]$Text); Write-ColorText "⚠️  $Text" -Color Yellow }

Clear-Host
Write-Host ""
Write-ColorText "==========================================" -Color Magenta
Write-ColorText "       EcoWiki 快速重启程序" -Color Magenta  
Write-ColorText "==========================================" -Color Magenta
Write-Host ""

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

try {
    # 1. 智能停止现有服务
    Write-ColorText "🔄 正在停止现有服务..." -Color Yellow
    Write-Host ""

    # 停止EcoWiki后端进程
    Write-Info "检查并停止EcoWiki后端服务..."
    $backendProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object {
        $_.CommandLine -like "*ecowiki-backend*" -or 
        $_.CommandLine -like "*spring-boot:run*" -or
        $_.MainWindowTitle -like "*EcoWiki Backend*"
    }
    
    if ($backendProcesses) {
        $backendProcesses | Stop-Process -Force
        Write-Success "后端服务已停止 ($($backendProcesses.Count) 个进程)"
    } else {
        Write-Info "未找到运行中的后端服务"
    }

    # 停止前端开发服务器
    Write-Info "检查并停止前端开发服务器..."
    $frontendProcesses = Get-Process -Name "node" -ErrorAction SilentlyContinue | Where-Object {
        $_.CommandLine -like "*npm run dev*" -or 
        $_.CommandLine -like "*vite*" -or
        $_.MainWindowTitle -like "*EcoWiki Frontend*"
    }
    
    if ($frontendProcesses) {
        $frontendProcesses | Stop-Process -Force
        Write-Success "前端服务已停止 ($($frontendProcesses.Count) 个进程)"
    } else {
        Write-Info "未找到运行中的前端服务"
    }

    # 额外清理可能残留的进程
    Start-Sleep -Seconds 1
    Write-Success "清理完成"

    Write-Host ""
    Write-ColorText "🚀 重新启动服务..." -Color Green
    Write-Host ""

    # 2. 智能启动后端
    $BackendDir = Join-Path $ScriptDir "www\backend"
    $JarFiles = Get-ChildItem "$BackendDir\target\ecowiki-backend-*.jar" -ErrorAction SilentlyContinue | 
                Where-Object { $_.Name -notlike "*-sources.jar" -and $_.Name -notlike "*-javadoc.jar" }

    if ($JarFiles.Count -gt 0) {
        # 使用已编译的JAR文件快速启动
        $JarFile = $JarFiles[0]
        Write-Info "检测到已编译的JAR文件，使用快速启动模式..."
        Write-ColorText "📦 JAR文件: $($JarFile.Name)" -Color Gray
        
        $BackendJob = Start-Job -ScriptBlock {
            param($JarPath, $WorkingDir)
            Set-Location $WorkingDir
            & java -jar $JarPath
        } -ArgumentList $JarFile.FullName, $BackendDir
        
        Write-Success "后端服务已启动 (JAR模式)"
    } else {
        # 回退到Maven启动方式
        Write-Warning "未找到已编译的JAR文件，使用Maven启动模式..."
        Write-Info "这可能需要更长时间..."
        
        $BackendJob = Start-Job -ScriptBlock {
            param($WorkingDir)
            Set-Location $WorkingDir
            & mvn spring-boot:run
        } -ArgumentList $BackendDir
        
        Write-Success "后端服务已启动 (Maven模式)"
    }

    # 3. 等待后端启动并检查状态
    Write-Info "🕐 等待后端服务启动..."
    $retryCount = 0
    $maxRetries = 15
    $backendReady = $false

    do {
        Start-Sleep -Seconds 2
        $retryCount++
        
        # 检查8080端口是否监听
        $portCheck = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
        if ($portCheck) {
            Write-Success "后端服务启动成功 (端口8080已监听)"
            $backendReady = $true
            break
        }
        
        # 检查后端进程是否还在运行
        $jobState = Get-Job $BackendJob.Id | Select-Object -ExpandProperty State
        if ($jobState -eq "Completed" -or $jobState -eq "Failed") {
            Write-Error "后端服务启动失败"
            break
        }
        
        Write-Info "等待中... ($retryCount/$maxRetries)"
        
    } while ($retryCount -lt $maxRetries)

    if (-not $backendReady) {
        Write-Warning "后端服务启动超时或失败，但继续启动前端服务"
    }

    # 4. 启动前端服务
    Write-Host ""
    Write-Info "🎨 启动前端开发服务器..."
    
    $FrontendDir = Join-Path $ScriptDir "www\frontend"
    Set-Location $FrontendDir

    # 检查前端依赖
    if (-not (Test-Path "node_modules")) {
        Write-Warning "前端依赖缺失，正在安装..."
        & npm install --silent
        if ($LASTEXITCODE -ne 0) {
            Write-Error "前端依赖安装失败"
            throw "前端依赖安装失败"
        }
        Write-Success "前端依赖安装完成"
    }

    # 启动前端开发服务器
    $FrontendJob = Start-Job -ScriptBlock {
        param($WorkingDir)
        Set-Location $WorkingDir
        & npm run dev
    } -ArgumentList $FrontendDir

    Write-Success "前端开发服务器已启动"
    Set-Location $ScriptDir

    # 5. 显示结果
    Write-Host ""
    Write-ColorText "✅ 服务重启完成！" -Color Green
    Write-Host ""
    Write-ColorText "📖 访问地址:" -Color Cyan
    Write-Host "   前端: http://localhost:5173"
    Write-Host "   后端: http://localhost:8080"
    Write-Host "   管理后台: http://localhost:5173/admin"
    Write-Host ""
    Write-ColorText "💡 提示:" -Color Yellow
    Write-Host "   - 前端热重载已启用，修改代码会自动刷新"
    Write-Host "   - 后端支持热部署，大部分更改无需重启"
    Write-Host "   - 按 Ctrl+C 停止当前PowerShell会话"
    Write-Host "   - 服务将在后台继续运行"
    Write-Host ""

    # 等待一段时间显示状态信息
    Start-Sleep -Seconds 3

    # 最终状态检查
    Write-Info "🔍 最终状态检查..."
    
    # 检查前端端口
    $frontendPort = Get-NetTCPConnection -LocalPort 5173 -State Listen -ErrorAction SilentlyContinue
    if ($frontendPort) {
        Write-Success "前端服务运行正常 (端口5173)"
    } else {
        Write-Warning "前端服务可能还在启动中"
    }
    
    # 检查后端端口
    $backendPort = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
    if ($backendPort) {
        Write-Success "后端服务运行正常 (端口8080)"
    } else {
        Write-Warning "后端服务可能还在启动中"
    }

} catch {
    Write-Error "重启过程中发生错误: $($_.Exception.Message)"
    Write-Host ""
    Write-Warning "请检查错误信息并手动启动服务"
} finally {
    Set-Location $ScriptDir
    Write-Host ""
    Write-Info "重启脚本执行完成"
    Write-Host ""
    Read-Host "按任意键退出"
}
