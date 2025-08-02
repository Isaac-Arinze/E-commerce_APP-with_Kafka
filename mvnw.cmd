@ECHO OFF
@REM ----------------------------------------------------------------------------
@REM Maven Wrapper Startup Script for Windows
@REM ----------------------------------------------------------------------------

@REM Based on Apache Maven Wrapper 3.2.0 (compatible with Maven 3.9.x)

SETLOCAL

SET MVNW_VERBOSE=%MVNW_VERBOSE%

SET WRAPPER_DIR=%~dp0
SET WRAPPER_DIR=%WRAPPER_DIR:~0,-1%

IF NOT "%JAVA_HOME%"=="" (
  SET "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) ELSE (
  SET "JAVA_EXE=java"
)

SET MVN_WRAPPER_DIR=%WRAPPER_DIR%\.mvn
SET WRAPPER_JAR=%MVN_WRAPPER_DIR%\wrapper\maven-wrapper.jar
SET WRAPPER_PROPERTIES=%MVN_WRAPPER_DIR%\wrapper\maven-wrapper.properties

SET DEFAULT_WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar

IF NOT EXIST "%MVN_WRAPPER_DIR%\wrapper" (
  MKDIR "%MVN_WRAPPER_DIR%\wrapper" 2>NUL
)

IF NOT EXIST "%WRAPPER_JAR%" (
  ECHO maven-wrapper.jar not found. Attempting to download...
  WHERE curl >NUL 2>&1
  IF %ERRORLEVEL%==0 (
    curl -s -L -o "%WRAPPER_JAR%" "%DEFAULT_WRAPPER_URL%"
  ) ELSE (
    WHERE powershell >NUL 2>&1
    IF %ERRORLEVEL%==0 (
      powershell -NoProfile -Command "Invoke-WebRequest -UseBasicParsing -Uri '%DEFAULT_WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'"
    ) ELSE (
      ECHO Could not download maven-wrapper.jar automatically.
      ECHO Please download it manually from:
      ECHO   %DEFAULT_WRAPPER_URL%
      ECHO and place it at:
      ECHO   %WRAPPER_JAR%
    )
  )
)

SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

"%JAVA_EXE%" %MAVEN_OPTS% -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%WRAPPER_DIR%" %WRAPPER_LAUNCHER% %*
ENDLOCAL
