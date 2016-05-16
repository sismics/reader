@echo off

rem #################################################################
rem #
rem # Sismics Reader standalone startup script.
rem #
rem # Author: Jean-Marc Tremeaux <jm.tremeaux@gmail.com>
rem #
rem #################################################################

rem The directory where reader will create its files (database, index...)
rem Make sure it is writable.
rem Leave it empty to use the users's directory (default: %APPDATA%\Sismics\Reader)
set READER_HOME=%APPDATA%\Sismics\Reader

rem The host name or IP address on which to bind Reader. Only relevant if you have
rem multiple network interfaces and want to make Reader available on only one of them.
rem The default value 0.0.0.0 will bind Reader to all available network interfaces.
rem set READER_HOST=0.0.0.0

rem The port on which Reader will listen for incoming HTTP traffic. Default: 4001.
rem set READER_PORT=4001

rem The context path (i.e., the last part of the Reader URL).  Typically "/" or "/reader".
rem Default: "/".
rem set READER_CONTEXT_PATH=/

rem The memory limit (max Java heap size) in megabytes.
set READER_MAX_MEMORY=150

rem Set to true to activate SSL
set READER_SECURE=false

rem Set to true to activate header-based authentication
set READER_HEADER_AUTHENTICATION=false

rem The keystore path
set READER_KEYSTORE_PATH=

rem The keystore password
set READER_KEYSTORE_PASSWORD=

rem The key manager password
set READER_KEYMANAGER_PASSWORD=

java -Xmx%READER_MAX_MEMORY%m  -Dreader.home=%READER_HOME% -Dreader.host=%READER_HOST% -Dreader.port=%READER_PORT% -Dreader.secure=%READER_SECURE% -Dreader.header_authentication=%READER_HEADER_AUTHENTICATION% -Dreader.keystore_path=%READER_KEYSTORE_PATH% -Dreader.keystore_password=%READER_KEYSTORE_PASSWORD% -Dreader.keymanager_password=%READER_KEYMANAGER_PASSWORD% -Dreader.contextPath=%READER_CONTEXT_PATH% -jar reader-standalone.jar
