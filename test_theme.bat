@echo off
echo Testing NoUCall with Day/Night mode functionality...
cd /d "%~dp0"

echo Building and installing debug version...
call .\build_and_install.bat

echo.
echo ========================================
echo TESTING DAY/NIGHT MODE FUNCTIONALITY
echo ========================================
echo.
echo 1. Open the app
echo 2. Click on the settings menu (icon with gears)
echo 3. Look for "Mode Sombre" switch
echo 4. Toggle the switch to change between light/dark themes
echo 5. Verify that the theme changes immediately
echo 6. Close and reopen the app to verify persistence
echo.
echo Features to verify:
echo - [ ] Settings menu opens correctly
echo - [ ] Dark mode switch is visible
echo - [ ] Switch toggles between on/off
echo - [ ] Theme changes immediately when toggled
echo - [ ] Theme persists after app restart
echo - [ ] All activities respect the theme (Main, Settings, Statistics)
echo - [ ] Last detected number info is displayed
echo - [ ] Clear logs button works
echo.
echo The app package name is: com.noucall.app.debug
echo.
pause
