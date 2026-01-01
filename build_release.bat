@echo off
echo Building NoUCall in RELEASE mode without logs...
cd /d "%~dp0"

echo Cleaning previous build...
gradlew clean

echo Building release APK...
gradlew assembleRelease

echo.
echo Release APK built successfully!
echo Location: app\build\outputs\apk\release\app-release.apk
echo.
echo Installing release APK...
adb install -r app\build\outputs\apk\release\app-release.apk

if %errorlevel% neq 0 (
    echo Installation failed! Make sure device is connected and USB debugging is enabled.
    pause
    exit /b 1
)

echo Installation successful, launching app...
adb shell am start -n com.noucall.app/com.noucall.app.MainActivity

if %errorlevel% neq 0 (
    echo Launch failed!
    pause
    exit /b 1
)

echo.
echo Release installation complete!
echo Debug logs are DISABLED in this version
pause
