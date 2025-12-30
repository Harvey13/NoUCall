@echo off
echo Listing connected devices...
adb devices

echo.
echo Enter the device ID from the list above (or press Enter if only one device):
set /p DEVICE_ID=

if "%DEVICE_ID%"=="" (
    echo Using default device...
    adb logcat | findstr CallBlocker
) else (
    echo Using device %DEVICE_ID%...
    adb -s %DEVICE_ID% logcat | findstr CallBlocker
)

pause