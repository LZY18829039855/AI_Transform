# AI Transform 项目编译脚本 (PowerShell)
# 编码: UTF-8

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "AI Transform 项目编译脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查Maven是否安装
$mvnPath = Get-Command mvn -ErrorAction SilentlyContinue
if (-not $mvnPath) {
    Write-Host "[错误] 未找到 Maven，请确保 Maven 已安装并添加到 PATH 环境变量中" -ForegroundColor Red
    Write-Host ""
    Read-Host "按 Enter 键退出"
    exit 1
}

try {
    Write-Host "[1/3] 正在清理项目..." -ForegroundColor Yellow
    mvn clean -q
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[错误] 清理失败" -ForegroundColor Red
        Read-Host "按 Enter 键退出"
        exit 1
    }
    Write-Host "✓ 清理完成" -ForegroundColor Green
    Write-Host ""

    Write-Host "[2/3] 正在编译项目..." -ForegroundColor Yellow
    mvn compile
    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "[错误] 编译失败" -ForegroundColor Red
        Read-Host "按 Enter 键退出"
        exit 1
    }
    Write-Host ""

    Write-Host "[3/3] 编译完成！" -ForegroundColor Green
    Write-Host ""
    Write-Host "编译结果位置: target\classes" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "可选操作:" -ForegroundColor Yellow
    Write-Host "  - 运行项目: mvn spring-boot:run" -ForegroundColor White
    Write-Host "  - 打包项目: mvn clean package" -ForegroundColor White
    Write-Host ""
    
} catch {
    Write-Host "[错误] 编译过程中发生异常: $_" -ForegroundColor Red
    Read-Host "按 Enter 键退出"
    exit 1
}

Read-Host "按 Enter 键退出"




