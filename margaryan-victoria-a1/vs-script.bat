@echo off
echo Compiling.....
echo.
javac a1\*.java
echo.
rem choice /c re /m " Do you want to run or exit?"
:UserChoice
set /p "CMD=Do you want to run or exit? [run/exit] "
rem if errorlevel 2 goto Exit
rem if errorlevel 1 goto Run
if "%CMD%"=="exit" goto Exit
if "%CMD%"=="run" goto Run
echo "%CMD%" is not valid, try again!
echo.
goto UserChoice
:Run
java -Dsun.java2d.d3d=false a1.MyGame
pause
:Exit
echo.
echo Exiting.....
ping -n 1 localhost >nul
echo.
exit