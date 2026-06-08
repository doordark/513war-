@echo off
chcp 65001 >nul
echo ========================================
echo   Java 2D 塔防游戏 - 一键启动
echo ========================================
echo.

REM 检查 Java 是否安装
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Java，请先安装 JDK 17 或更高版本
    pause
    exit /b 1
)

echo [1/2] 正在编译...
javac -d out -cp "javafx-sdk-21/lib/*" -sourcepath src src/main/*.java src/entity/*.java src/entity/monster/*.java src/entity/tower/*.java src/enums/*.java src/map/*.java src/wave/*.java
if %errorlevel% neq 0 (
    echo [错误] 编译失败！
    pause
    exit /b 1
)
echo [成功] 编译完成
echo.

echo [2/2] 正在启动游戏...
echo.
java --module-path javafx-sdk-21/lib --add-modules javafx.controls,javafx.fxml -cp out main.GameMain
