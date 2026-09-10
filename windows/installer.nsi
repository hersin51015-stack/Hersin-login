; NSIS Modern User Interface installer script for Windows 7 / 8 / 10 / 11
!include "MUI2.nsh"

Name "Hersin Login App"
OutFile "HersinLogin_Setup.exe"
InstallDir "$LOCALAPPDATA\HersinApp"
InstallDirRegKey HKCU "Software\HersinApp" ""
RequestExecutionLevel user

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_LANGUAGE "English"

Section "Install"
  SetOutPath "$INSTDIR"
  File "HersinLogin_Win7.py"
  File "win_app.c"
  File "install_hersin_app.bat"

  CreateDirectory "$SMPROGRAMS\Hersin Login"
  CreateShortcut "$SMPROGRAMS\Hersin Login\Hersin Login.lnk" "$INSTDIR\install_hersin_app.bat"
  CreateShortcut "$DESKTOP\Hersin Login.lnk" "$INSTDIR\install_hersin_app.bat"
  WriteUninstaller "$INSTDIR\Uninstall.exe"
SectionEnd

Section "Uninstall"
  Delete "$DESKTOP\Hersin Login.lnk"
  Delete "$SMPROGRAMS\Hersin Login\*.*"
  RMDir "$SMPROGRAMS\Hersin Login"
  Delete "$INSTDIR\*.*"
  RMDir "$INSTDIR"
SectionEnd
