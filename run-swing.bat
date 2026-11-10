@echo off
title E-Commerce Shopping Cart - Swing UI
echo Starting E-Commerce Shopping Cart Swing Front-end...
java -cp bin com.ecommerce.frontend.EcommerceSwingApp
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Recompiling...
    javac -encoding UTF-8 -d bin -cp bin src/com/ecommerce/**/*.java Front-end/src/com/ecommerce/frontend/*.java
    java -cp bin com.ecommerce.frontend.EcommerceSwingApp
)
