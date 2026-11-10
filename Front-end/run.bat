@echo off
title E-Commerce Shopping Cart - Swing Front-end
echo ================================================================================
echo Starting E-Commerce Shopping Cart Swing Application...
echo ================================================================================
cd /d "%~dp0\.."
if not exist "bin\com\ecommerce\frontend\EcommerceSwingApp.class" (
    echo Compiling application...
    javac -encoding UTF-8 -d bin -cp bin src/com/ecommerce/**/*.java Front-end/src/com/ecommerce/frontend/*.java
)
java -cp bin com.ecommerce.frontend.EcommerceSwingApp
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with code %ERRORLEVEL%.
    pause
)
