@echo off
chcp 65001 >nul
echo ========================================
echo AI Transform 项目打包脚本
echo ========================================
echo.

REM 检查Maven是否安装
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 未找到 Maven，请确保 Maven 已安装并添加到 PATH 环境变量中
    echo.
    pause
    exit /b 1
)

echo [1/3] 正在清理项目...
call mvn clean -q
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 清理失败
    pause
    exit /b 1
)
echo ✓ 清理完成
echo.

echo [2/3] 正在编译和打包项目...
call mvn package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [错误] 打包失败
    pause
    exit /b 1
)
echo.

echo [3/3] 打包完成！
echo.
echo JAR 文件位置: target\ai-transform-1.0.0.jar
echo.
echo 运行方式:
echo   java -jar target\ai-transform-1.0.0.jar
echo.

pause








