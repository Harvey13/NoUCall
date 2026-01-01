@echo off
echo ========================================
echo TESTING BLOCKED CALLS COUNT FIX
echo ========================================
echo.
echo This will verify that "Appels Bloqués: X" is now
echo properly translated to "Blocked Calls: X" in English
echo.

echo Building and installing debug version...
cd /d "%~dp0"
call .\build_and_install.bat

echo.
echo ========================================
echo ROOT CAUSE IDENTIFIED
echo ========================================
echo.
echo PROBLEM: "Appels Bloqués: 0" still showing in English mode
echo.
echo ROOT CAUSE: The string "blocked_calls_count" was missing
echo from values-en/strings.xml file!
echo.
echo When getString(R.string.blocked_calls_count, count) is called:
echo - French: Found "Appels Bloqués: %1$d" in values/strings.xml
echo - English: NOT FOUND in values-en/strings.xml 
echo - Result: Android falls back to French string
echo.
echo SOLUTION: Added missing string to English file:
echo <string name="blocked_calls_count">Blocked Calls: %1$d</string>
echo.
echo ========================================
step-by-step VERIFICATION
echo ========================================
echo.
echo 1. Start app in English:
echo    - Quick settings -> 🇬🇧 English
echo    - App restarts in English
echo.
echo 2. Open Statistics page
echo 3. Verify the red counter text:
echo    - Should show: "Blocked Calls: 0"
echo    - Should NOT show: "Appels Bloqués: 0"
echo.
echo 4. Test language switching:
echo    - Switch to French -> Should show "Appels Bloqués: 0"
echo    - Switch back to English -> Should show "Blocked Calls: 0"
echo.
echo 5. Verify "No data" text:
echo    - Should show "No data" in English
echo    - Should show "Aucune donnée" in French
echo.
echo ========================================
echo EXPECTED RESULT
echo ========================================
echo.
echo ENGLISH MODE:
echo - Counter: "Blocked Calls: 0" (red text)
echo - Empty state: "No data"
echo.
echo FRENCH MODE:
echo - Counter: "Appels Bloqués: 0" (red text)  
echo - Empty state: "Aucune donnée"
echo.
echo ========================================
echo FILES MODIFIED
echo ========================================
echo.
echo ✅ values-en/strings.xml - Added missing "blocked_calls_count" string
echo.
echo This was the critical missing piece!
echo.
pause
