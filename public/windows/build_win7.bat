@echo off
title Build Hersin Login for Windows 7
echo ========================================================
echo  Building Hersin Login Executable for Windows 7 / 8 / 10 / 11
echo ========================================================

where gcc >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] GCC compiler not found in PATH!
    echo Please install MinGW-w64 or TDM-GCC to compile on Windows.
    pause
    exit /b 1
)

echo Compiling win_app.c with Windows 7 (Subsystem 6.1) target...
gcc -O2 -mwindows -DWINVER=0x0601 -D_WIN32_WINNT=0x0601 -Wl,--subsystem,windows:6.1 win_app.c -o HersinLogin_Win7.exe -lkernel32 -luser32 -lgdi32 -lshell32

if %errorlevel% equ 0 (
    echo [SUCCESS] HersinLogin_Win7.exe generated successfully!
    echo Running app...
    start HersinLogin_Win7.exe
) else (
    echo [FAILED] Compilation failed.
)
pause
