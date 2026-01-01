@echo off
echo ========================================
echo TESTING DUPLICATE LINES FIX
echo ========================================
echo.
echo This will verify that the duplicate "Blocked Calls"
echo lines have been removed and only one red line
echo remains that follows the selected language
echo.

echo Building and installing debug version...
cd /d "%~dp0"
call .\build_and_install.bat

echo.
echo ========================================
echo PROBLEM IDENTIFIED AND FIXED
echo ========================================
echo.
echo ISSUE: Two lines showing blocked calls count:
echo 1. White text: "Blocked Calls: %1$d" 
echo 2. Red text: "Appels Bloqués: 0"
echo.
echo ROOT CAUSE:
echo - Layout XML had separate title TextView
echo - Java code had hard-coded French text
echo - Both were displayed simultaneously
echo.
echo SOLUTION:
echo 1. Removed title TextView from layout XML
echo 2. Fixed Java code to use localized string
echo 3. Single TextView now shows localized text in red
echo.
echo ========================================
 EXPECTED BEHAVIOR
echo ========================================
echo.
echo FRENCH MODE:
echo - Single red line: "Appels Bloqués: 0"
echo - No white line above
echo.
echo ENGLISH MODE:
echo - Single red line: "Blocked Calls: 0" 
echo - No white line above
echo.
echo LANGUAGE SWITCHING:
echo - Text changes when language changes
echo - Always red color
echo - Always single line
echo.
echo ========================================
echo VERIFICATION TEST
echo ========================================
echo.
echo 1. Start app in French
echo 2. Go to Statistics page
echo 3. Verify: Only ONE red line "Appels Bloqués: X"
echo 4. Switch to English (quick settings)
echo 5. App restarts, go to Statistics
echo 6. Verify: Only ONE red line "Blocked Calls: X"
echo 7. Switch back to French
echo 8. Verify: Back to "Appels Bloqués: X"
echo.
echo ========================================
echo FILES MODIFIED
echo ========================================
echo.
echo ✅ activity_statistics.xml - Removed duplicate title TextView
echo ✅ StatisticsActivity.kt - Fixed hard-coded text to use getString()
echo.
echo Now only one localized red line remains!
echo.
pause
