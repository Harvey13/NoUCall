@echo off
setlocal enabledelayedexpansion

echo Listing connected devices...
adb devices > temp_devices.txt

REM Count devices (excluding header line)
for /f %%i in ('findstr /c:"device" temp_devices.txt ^| findstr /v "List" ^| find /c "device"') do set DEVICE_COUNT=%%i

echo.
if %DEVICE_COUNT%==1 (
    REM Get the device ID
    for /f "tokens=1" %%a in ('findstr /c:"device" temp_devices.txt ^| findstr /v "List"') do set DEVICE_ID=%%a
    echo Only one device found: !DEVICE_ID!
    echo Using device !DEVICE_ID!...
    adb -s !DEVICE_ID! logcat | findstr CallBlocker
) else (
    type temp_devices.txt
    echo.
    echo Multiple devices found. Enter the device ID from the list above:
    set /p DEVICE_ID=
    if "!DEVICE_ID!"=="" (
        echo No device ID entered. Using first device...
        for /f "tokens=1" %%a in ('findstr /c:"device" temp_devices.txt ^| findstr /v "List"') do (
            set DEVICE_ID=%%a
            goto :found
        )
        :found
        adb -s !DEVICE_ID! logcat | findstr CallBlocker
    ) else (
        echo Using device !DEVICE_ID!...
        adb -s !DEVICE_ID! logcat | findstr CallBlocker
    )
)

del temp_devices.txt
pause