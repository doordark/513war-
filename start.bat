@echo off

echo ========================================
echo   Java 2D Tower Defense - Launcher
echo ========================================
echo.

REM Check Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java not found. Please install JDK 17+.
    pause
    exit /b 1
)

echo [1/2] Compiling...
javac -encoding UTF-8 -d out -cp "javafx-sdk-21/lib/*" -sourcepath src src/main/*.java src/sound/*.java src/entity/*.java src/entity/monster/*.java src/entity/tower/*.java src/enums/*.java src/map/*.java src/wave/*.java
if %errorlevel% neq 0 (
    echo [ERROR] Compile failed!
    pause
    exit /b 1
)
echo [OK] Compile done.

REM Copy resources
if exist resources (
    xcopy /E /I /Y /Q resources out >nul
    echo [OK] Resources synced.
)
echo.

echo [2/2] Starting game...
echo.
java -Dfile.encoding=UTF-8 --module-path javafx-sdk-21/lib --add-modules javafx.controls,javafx.fxml -cp out main.GameMain

echo.
echo Game closed. Press any key to exit...
pause
