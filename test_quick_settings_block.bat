@echo off
echo ========================================
echo TESTING QUICK SETTINGS WITH BLOCK OPTION
echo ========================================
echo.
echo This will test the new feature to add allowed numbers to blocked prefixes
echo.

echo Building and installing debug version...
cd /d "%~dp0"
call .\build_and_install.bat

echo.
echo ========================================
echo NEW FEATURE TEST INSTRUCTIONS
echo ========================================
echo.
echo 1. Open the app
echo 2. Make/receive a call that gets ALLOWED (not blocked)
echo 3. Click on settings menu (icon with gears)
echo 4. Verify the menu shows:
echo    - Mode : ☀️ Clair / 🌙 Sombre
echo    - Dernier numéro: [number]
echo    - AUTORISÉ - [reason]
echo    - ➕ Ajouter '[number]' aux préfixes bloqués  <-- NEW!
echo    - Effacer les logs de détection
echo.
echo 5. Click on "➕ Ajouter..." option
echo 6. Confirm dialog should appear with extracted prefix
echo 7. Click "Ajouter" to add to blocked prefixes
echo 8. Verify the prefix appears in main app list
echo 9. Future calls from this prefix should be blocked
echo.
echo ========================================
echo BEHAVIOR DETAILS
echo ========================================
echo.
echo ✅ Option appears ONLY when:
echo    - Last number exists AND
echo    - Number was AUTORISÉ (allowed)
echo.
echo ✅ Prefix extraction logic:
echo    - International (+33...): Uses country code (33)
echo    - National (06...): Takes first 6 digits
echo.
echo ✅ Confirmation dialog shows:
echo    - Extracted prefix
echo    - Warning about blocking future calls
echo.
echo ✅ After adding:
echo    - Toast confirmation
echo    - UI refreshes to show new prefix
echo    - Future calls from this prefix will be BLOCKED
echo.
echo ========================================
echo TESTING SCENARIOS
echo ========================================
echo.
echo Scenario 1: International number
echo - Number: +33612345678
echo - Prefix extracted: 336123
echo - Result: Blocks all +336123... calls
echo.
echo Scenario 2: National number  
echo - Number: 0612345678
echo - Prefix extracted: 061234
echo - Result: Blocks all 061234... calls
echo.
echo Scenario 3: No number detected
echo - Menu shows: "Aucun numéro détecté"
echo - No add option appears
echo.
echo Scenario 4: Number was blocked
echo - Menu shows: "BLOQUÉ - [reason]"
echo - No add option appears (already blocked)
echo.
pause
