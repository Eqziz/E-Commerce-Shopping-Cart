@echo off
title E-Commerce Shopping Cart - Swing UI
echo Starting E-Commerce Shopping Cart Swing Front-end...
if not exist "bin" mkdir "bin"
if not exist "bin\com\ecommerce\frontend\EcommerceSwingApp.class" (
    echo Compiling application...
    dir /s /b src\*.java Front-end\src\*.java > "%TEMP%\sources.txt" && javac -encoding UTF-8 -d bin -cp bin @"%TEMP%\sources.txt" && del "%TEMP%\sources.txt"
)
java -cp bin com.ecommerce.frontend.EcommerceSwingApp
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Recompiling...
    dir /s /b src\*.java Front-end\src\*.java > "%TEMP%\sources.txt" && javac -encoding UTF-8 -d bin -cp bin @"%TEMP%\sources.txt" && del "%TEMP%\sources.txt"
    java -cp bin com.ecommerce.frontend.EcommerceSwingApp
)
