@echo off
echo ========================================
echo TESTING NOUCALL MENU SETTINGS
echo ========================================
echo.
echo This will test the new menu-based settings system
echo.

echo Building DEBUG version (with logs)...
cd /d "%~dp0"
call .\gradlew assembleDebug

echo.
echo Building RELEASE version (without logs)...
call .\gradlew assembleRelease

echo.
echo ========================================
echo INSTALLATION OPTIONS
echo ========================================
echo.
echo 1. Install DEBUG version:
echo    adb install -r app\build\outputs\apk\debug\app-debug.apk
echo.
echo 2. Install RELEASE version:
echo    adb install -r app\build\outputs\apk\release\app-release.apk
echo.
echo ========================================
echo TESTING INSTRUCTIONS
echo ========================================
echo.
echo 1. Install either debug or release version
echo 2. Open the app
echo 3. Click the settings menu (icon with gears)
echo 4. A popup menu should appear with:
echo    - Mode : ☀️ Clair / 🌙 Sombre
echo    - Dernier numéro détecté avec raison
echo    - Effacer les logs de détection
echo.
echo 5. Test each option:
echo    - Click on mode to toggle day/night
echo    - Verify the last detection info
echo    - Click to clear logs
echo.
echo 6. Make/receive calls to test detection
echo    - Check that the last detection updates
echo    - Verify the reason (BLOQUÉ or AUTORISÉ)
echo.
echo ========================================
echo DIFFERENCES BETWEEN VERSIONS
echo ========================================
echo.
echo DEBUG version:
echo - Package: com.noucall.app.debug
echo - Logs enabled in Logcat
echo - All debug information visible
echo.
echo RELEASE version:
echo - Package: com.noucall.app
echo - No logs in Logcat (performance optimized)
echo - Last detection still works for debugging
echo.
echo ========================================
echo KEY FEATURES
echo ========================================
echo ✅ Menu popup instead of separate activity
echo ✅ Day/Night mode toggle
echo ✅ Last detected number display
echo ✅ Block/Allow reason shown
echo ✅ Works in both debug and release
echo ✅ Clear logs functionality
echo ✅ Theme persistence
echo.
pause
