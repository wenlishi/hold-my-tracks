@echo off
echo Starting Track Backend Service...

REM 检查Java环境
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java is not installed or not in PATH
    pause
    exit /b 1
)

REM 检查Maven环境
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Maven is not installed or not in PATH
    pause
    exit /b 1
)

echo Building and starting the application...

REM 编译并运行应用
mvn spring-boot:run

if %errorlevel% neq 0 (
    echo Error: Failed to start the application
    pause
    exit /b 1
)

pause