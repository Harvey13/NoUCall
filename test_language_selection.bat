@echo off
echo ========================================
echo TESTING LANGUAGE SELECTION FEATURE
echo ========================================
echo.
echo This will test the new language selection feature
echo with French/English flags in quick settings
echo.

echo Building and installing debug version...
cd /d "%~dp0"
call .\build_and_install.bat

echo.
echo ========================================
echo LANGUAGE SELECTION FEATURES
echo ========================================
echo.
echo 1. Open app
echo 2. Click on settings menu (icon with gears)
echo 3. Menu should now show:
echo    - Mode : ☀️ Clair / 🌙 Sombre
echo    - 🇫🇷 Français / 🇬🇧 English  <-- NEW!
echo    - Dernier numéro: [number]
echo    - Add to blocked prefixes (if applicable)
echo    - Effacer les logs de détection
echo.
echo 4. Click on language option
echo 5. Language selection dialog should appear:
echo    ○ 🇫🇷 Français
echo    ○ 🇬🇧 English
echo.
echo 6. Select English
echo 7. App should restart and show English interface
echo 8. All text should be in English
echo 9. Open settings again - should show English text
echo.
echo ========================================
expected BEHAVIOR
echo ========================================
echo.
echo ✅ Language persistence:
echo    - Choice saved in SharedPreferences
echo    - Applied immediately after restart
echo    - Survives app restart
echo.
echo ✅ Text localization:
echo    - All UI strings use resources
echo    - French: values/strings.xml
echo    - English: values-en/strings.xml
echo.
echo ✅ Menu adaptation:
echo    - Shows current language with flag
echo    - Language selection dialog
echo    - Immediate restart on change
echo.
echo ========================================
echo TESTING SCENARIOS
echo ========================================
echo.
echo Scenario 1: French to English
echo 1. Start in French (default)
echo 2. Open quick settings
echo 3. Select English
echo 4. Verify: All text in English
echo 5. Restart app
echo 6. Verify: Still in English
echo.
echo Scenario 2: English to French
echo 1. Switch to English
echo 2. Open quick settings
echo 3. Select French
echo 4. Verify: All text in French
echo 5. Restart app
echo 6. Verify: Still in French
echo.
echo Scenario 3: Persistence
echo 1. Change language
echo 2. Force close app
echo 3. Reopen app
echo 4. Verify: Language choice persisted
echo.
echo ========================================
echo KEY FILES MODIFIED
echo ========================================
echo.
echo ✅ LocaleManager.kt - Language management utility
echo ✅ values-en/strings.xml - English translations
echo ✅ values/strings.xml - Updated with new strings
echo ✅ MainActivity.kt - Language selection UI
echo.
pause
