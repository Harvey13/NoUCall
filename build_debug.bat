@echo off
echo Building NoUCall in DEBUG mode with logs...
cd /d "%~dp0"

echo Cleaning previous build...
gradlew clean

echo Building debug APK...
gradlew assembleDebug

echo.
echo Debug APK built successfully!
echo Location: app\build\outputs\apk\debug\app-debug.apk
echo.
echo Installing debug APK...
adb install -r app\build\outputs\apk\debug\app-debug.apk

echo.
echo Debug installation complete!
echo Debug logs are ENABLED in this version
pause
