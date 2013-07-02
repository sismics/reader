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
  File reader-agent.jar
  File reader-agent.exe
  File reader-agent.properties
  File reader-agent-elevated.exe
  File reader.war
  File LICENSE.txt

  # Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\Reader "Install_Dir" "$INSTDIR"

  # Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Reader" "DisplayName" "Reader"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Reader" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Reader" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Reader" "NoRepair" 1
  WriteUninstaller "uninstall.exe"

  # Restore Reader Agent properties
  CopyFiles /SILENT  $TEMP\reader-agent.properties $INSTDIR\reader-agent.properties
  Delete $TEMP\reader-agent.properties

  # Add Windows Firewall exception.
  # (Requires NSIS plugin found on http://nsis.sourceforge.net/NSIS_Simple_Firewall_Plugin to be installed
  # as NSIS_HOME/Plugins/SimpleFC.dll)
  SimpleFC::AddApplication "Reader Agent" "$INSTDIR\reader-agent.exe" 0 2 "" 1
  SimpleFC::AddApplication "Reader Agent (Elevated)" "$INSTDIR\reader-agent-elevated.exe" 0 2 "" 1

  # Start agent.
  Exec '"$INSTDIR\reader-agent-elevated.exe" -balloon'

SectionEnd


Section "Start Menu Shortcuts"

  CreateDirectory "$SMPROGRAMS\Reader"
  CreateShortCut "$SMPROGRAMS\Reader\Open Reader.lnk"          "$INSTDIR\reader.url"         ""         "$INSTDIR\reader-agent.exe"  0
  CreateShortCut "$SMPROGRAMS\Reader\Reader Tray Icon.lnk"     "$INSTDIR\reader-agent.exe"   "-balloon" "$INSTDIR\reader-agent.exe"  0
  CreateShortCut "$SMPROGRAMS\Reader\Start Reader.lnk" "$INSTDIR\reader-agent-elevated.exe" "-start"   "$INSTDIR\reader-agent-elevated.exe"  0
  CreateShortCut "$SMPROGRAMS\Reader\Stop Reader.lnk"  "$INSTDIR\reader-agent-elevated.exe" "-stop"    "$INSTDIR\reader-agent-elevated.exe"  0
  CreateShortCut "$SMPROGRAMS\Reader\Uninstall Reader.lnk"     "$INSTDIR\uninstall.exe"        ""         "$INSTDIR\uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\Reader\Getting Started.lnk"        "$INSTDIR\Getting Started.html" ""         "$INSTDIR\Getting Started.html" 0

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
  Delete "$INSTDIR\reader-agent.jar"
  Delete "$INSTDIR\reader-agent.exe"
  Delete "$INSTDIR\reader-agent.properties"
  Delete "$INSTDIR\reader-agent-elevated.exe"
  Delete "$INSTDIR\reader.war"
  Delete "$INSTDIR\uninstall.exe"
  RMDir "$INSTDIR"

  # Remove Windows Firewall exception.
  # (Requires NSIS plugin found on http://nsis.sourceforge.net/NSIS_Simple_Firewall_Plugin to be installed
  # as NSIS_HOME/Plugins/SimpleFC.dll)
  SimpleFC::RemoveApplication "$INSTDIR\reader-agent.exe"
  SimpleFC::RemoveApplication "$INSTDIR\reader-agent-elevated.exe"

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
