@echo off
setlocal enabledelayedexpansion

echo Building NoUCall app...
cd /d "%~dp0"

call gradlew.bat assembleDebug
set BUILD_ERROR=%errorlevel%

if %BUILD_ERROR% neq 0 (
    echo Build failed with error code %BUILD_ERROR%!
    pause
    exit /b 1
)

echo Build successful, installing app on device...
adb install -r app\build\outputs\apk\debug\app-debug.apk

if %errorlevel% neq 0 (
    echo Installation failed! Make sure device is connected and USB debugging is enabled.
    pause
    exit /b 1
)

echo Installation successful, launching app...
adb shell am start -n com.noucall.app/.MainActivity

if %errorlevel% neq 0 (
    echo Launch failed!
    pause
    exit /b 1
)

echo Done! App built, installed and launched.
pause