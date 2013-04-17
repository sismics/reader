# reader.nsi

!include "WordFunc.nsh"
!include "MUI.nsh"

!insertmacro VersionCompare

# The name of the installer
Name "Reader"

# The default installation directory
InstallDir $PROGRAMFILES\Reader

# Registry key to check for directory (so if you install again, it will
# overwrite the old one automatically)
InstallDirRegKey HKLM "Software\Reader" "Install_Dir"

#--------------------------------
#Interface Configuration

!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Header\orange.bmp"
!define MUI_FINISHPAGE_SHOWREADME "$INSTDIR\Getting Started.html"
!define MUI_FINISHPAGE_SHOWREADME_TEXT "View Getting Started document"

#--------------------------------
# Pages

# This page checks for JRE
Page custom CheckInstalledJRE

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

# Languages
!insertmacro MUI_LANGUAGE "English"

Section "Reader"

  SectionIn RO

  # Install for all users
  SetShellVarContext "all"

  # Silently uninstall existing version.
  ExecWait '"$INSTDIR\uninstall.exe" /S _?=$INSTDIR'

  # Set output path to the installation directory.
  SetOutPath $INSTDIR

  # Write files.
  File ..\reader-distribution-standalone-${reader.version}\reader.bat
  File ..\reader-distribution-standalone-${reader.version}\reader-standalone.jar
  File ..\reader-distribution-standalone-${reader.version}\sismicsreader.war

  # Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\Reader "Install_Dir" "$INSTDIR"

  # Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Reader" "DisplayName" "Reader"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Reader" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Reader" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Reader" "NoRepair" 1
  WriteUninstaller "uninstall.exe"

SectionEnd


Section "Start Menu Shortcuts"

  CreateDirectory "$SMPROGRAMS\Reader"
  CreateShortCut "$SMPROGRAMS\Reader\Open Reader.lnk"          "$INSTDIR\reader.url"         ""         "$INSTDIR\reader.bat"  0
  CreateShortCut "$SMPROGRAMS\Reader\Uninstall Reader.lnk"     "$INSTDIR\uninstall.exe"        ""         "$INSTDIR\uninstall.exe" 0

SectionEnd


# Uninstaller

Section "Uninstall"

  # Uninstall for all users
  SetShellVarContext "all"

  # Remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Reader"
  DeleteRegKey HKLM SOFTWARE\Reader

  # Remove files.
  Delete "$SMSTARTUP\Reader.lnk"
  RMDir /r "$SMPROGRAMS\Reader"
  Delete "$INSTDIR\reader.bat"
  Delete "$INSTDIR\reader-standalone.jar"
  Delete "$INSTDIR\sismicsreader.war"
  Delete "$INSTDIR\uninstall.exe"
  RMDir /r "$INSTDIR\log"
  RMDir "$INSTDIR"

SectionEnd


Function CheckInstalledJRE
    # Read the value from the registry into the $0 register
    ReadRegStr $0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" CurrentVersion

    # Check JRE version. At least 1.6 is required.
    #   $1=0  Versions are equal
    #   $1=1  Installed version is newer
    #   $1=2  Installed version is older (or non-existent)
    ${VersionCompare} $0 "1.6" $1
    IntCmp $1 2 InstallJRE 0 0
    Return

    InstallJRE:
      # Launch Java web installer.
      MessageBox MB_OK "Java was not found and will now be installed."
      File /oname=$TEMP\jre-setup.exe jre-7u21-windows-i586.exe
      ExecWait '"$TEMP\jre-setup.exe"' $0
      Delete "$TEMP\jre-setup.exe"

FunctionEnd
