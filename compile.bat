@echo off
chcp 65001 >nul
echo ========================================
echo AI Transform 项目编译脚本
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

echo [2/3] 正在编译项目...
call mvn compile
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [错误] 编译失败
    pause
    exit /b 1
)
echo.

echo [3/3] 编译完成！
echo.
echo 编译结果位置: target\classes
echo.
echo 可选操作:
echo   - 运行项目: mvn spring-boot:run
echo   - 打包项目: mvn clean package
echo.

pause

