@echo off
echo ========================================
echo TESTING PHONE NUMBER NORMALIZATION
echo ========================================
echo.
echo This will test the new phone number normalization feature
echo that removes country codes and uses national format
echo.

echo Building and installing debug version...
cd /d "%~dp0"
call .\build_and_install.bat

echo.
echo ========================================
echo NORMALIZATION EXAMPLES
echo ========================================
echo.
echo Input -> Output (National Format):
echo.
echo French numbers:
echo +33612345678 -> 0612345678
echo 0033612345678 -> 0612345678  
echo 0612345678 -> 0612345678
echo 612345678 -> 0612345678
echo.
echo Belgian numbers:
echo +3212345678 -> 12345678
echo 003212345678 -> 12345678
echo.
echo Other countries:
echo +4412345678 -> 12345678  (UK)
echo +4912345678 -> 12345678  (Germany)
echo +3412345678 -> 12345678  (Spain)
echo.
echo ========================================
echo PREFIX EXTRACTION LOGIC
echo ========================================
echo.
echo After normalization, the system extracts the first 6 digits:
echo.
echo 0612345678 -> Prefix: 061234
echo 12345678 -> Prefix: 123456
echo.
echo ========================================
echo TESTING SCENARIOS
echo ========================================
echo.
echo Scenario 1: French mobile number
echo - Original: +33612345678
echo - Normalized: 0612345678  
echo - Prefix extracted: 061234
echo - Blocks: All 061234... numbers
echo.
echo Scenario 2: French landline
echo - Original: +33123456789
echo - Normalized: 0123456789
echo - Prefix extracted: 012345
echo - Blocks: All 012345... numbers
echo.
echo Scenario 3: National format
echo - Original: 0612345678
echo - Normalized: 0612345678
echo - Prefix extracted: 061234
echo - Blocks: All 061234... numbers
echo.
echo Scenario 4: Short format
echo - Original: 612345678
echo - Normalized: 0612345678
echo - Prefix extracted: 061234
echo - Blocks: All 061234... numbers
echo.
echo ========================================
echo TEST INSTRUCTIONS
echo ========================================
echo.
echo 1. Make/receive calls from different number formats
echo 2. Check the quick settings menu
echo 3. Click "Add to blocked prefixes" 
echo 4. Verify the extracted prefix is in national format
echo 5. Confirm the prefix appears correctly in the main list
echo.
pause
