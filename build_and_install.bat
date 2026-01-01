@echo off
setlocal enabledelayedexpansion

echo Building NoUCall app in DEBUG mode...
cd /d "%~dp0"

echo Cleaning previous build...
call gradlew.bat clean

echo Building debug APK with logs enabled...
call gradlew.bat assembleDebug
set BUILD_ERROR=%errorlevel%

if %BUILD_ERROR% neq 0 (
    echo Build failed with error code %BUILD_ERROR%!
    pause
    exit /b 1
)

echo Build successful, installing debug app on device...
adb install -r app\build\outputs\apk\debug\app-debug.apk

if %errorlevel% neq 0 (
    echo Installation failed! Make sure device is connected and USB debugging is enabled.
    pause
    exit /b 1
)

echo Installation successful, launching app...
adb shell am start -n com.noucall.app.debug/com.noucall.app.MainActivity

if %errorlevel% neq 0 (
    echo Launch failed!
    pause
    exit /b 1
)

echo Done! Debug app built, installed and launched.
echo Debug logs are ENABLED in this version
pause