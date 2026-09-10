@echo off
setlocal EnableDelayedExpansion
title Hersin App - Windows Installer
color 1F

echo =======================================================================
echo                 HERSIN LOGIN APP - WINDOWS INSTALLER
echo               Compatible with Windows 7, 8, 10, and 11
echo =======================================================================
echo.

set "TARGET_DIR=%LOCALAPPDATA%\HersinApp"
if not defined LOCALAPPDATA set "TARGET_DIR=%USERPROFILE%\AppData\Local\HersinApp"

echo [*] Target Directory: %TARGET_DIR%
echo [*] Creating application directory...
if not exist "%TARGET_DIR%" mkdir "%TARGET_DIR%"

echo [*] Copying application files...
copy /Y "%~dp0HersinLogin_Win7.py" "%TARGET_DIR%\HersinLogin.pyw" >nul
copy /Y "%~dp0win_app.c" "%TARGET_DIR%\win_app.c" >nul

:: Create launch batch runner
(
echo @echo off
echo start "" pythonw "%TARGET_DIR%\HersinLogin.pyw"
echo if %%errorlevel%% neq 0 start "" python "%TARGET_DIR%\HersinLogin.pyw"
) > "%TARGET_DIR%\launch.bat"

echo [*] Creating Desktop Shortcut...
set "SHORTCUT_VBS=%TEMP%\CreateShortcut_%RANDOM%.vbs"
(
echo Set oWS = WScript.CreateObject("WScript.Shell"^)
echo sLinkFile = oWS.SpecialFolders("Desktop"^) ^& "\Hersin Login.lnk"
echo Set oLink = oWS.CreateShortcut(sLinkFile^)
echo oLink.TargetPath = "%TARGET_DIR%\launch.bat"
echo oLink.WorkingDirectory = "%TARGET_DIR%"
echo oLink.Description = "Hersin Login Application"
echo oLink.Save
) > "%SHORTCUT_VBS%"
cscript //nologo "%SHORTCUT_VBS%"
del "%SHORTCUT_VBS%" 2>nul

echo [*] Creating Start Menu Shortcut...
set "START_MENU=%APPDATA%\Microsoft\Windows\Start Menu\Programs\HersinApp"
if not exist "%START_MENU%" mkdir "%START_MENU%"
(
echo Set oWS = WScript.CreateObject("WScript.Shell"^)
echo sLinkFile = "%START_MENU%\Hersin Login.lnk"
echo Set oLink = oWS.CreateShortcut(sLinkFile^)
echo oLink.TargetPath = "%TARGET_DIR%\launch.bat"
echo oLink.WorkingDirectory = "%TARGET_DIR%"
echo oLink.Description = "Hersin Login Application"
echo oLink.Save
) > "%SHORTCUT_VBS%"
cscript //nologo "%SHORTCUT_VBS%"
del "%SHORTCUT_VBS%" 2>nul

echo.
echo =======================================================================
echo [SUCCESS] Hersin Login App has been installed!
echo - Desktop icon created: "Hersin Login"
echo - Start Menu shortcut created
echo.
echo Launching application now...
start "" "%TARGET_DIR%\launch.bat"
echo =======================================================================
pause
