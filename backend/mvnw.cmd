@ECHO OFF
SETLOCAL
SET WRAPPER_JAR=%~dp0\.mvn\wrapper\maven-wrapper.jar
SET WRAPPER_DL_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar
IF NOT EXIST %WRAPPER_JAR% (
  MKDIR %~dp0\.mvn\wrapper 2>NUL
  POWERSHELL -Command "Invoke-WebRequest -Uri %WRAPPER_DL_URL% -OutFile %WRAPPER_JAR%" || (
    ECHO Failed to download Maven Wrapper & EXIT /B 1 )
)
WHERE java >NUL 2>&1 || (ECHO Java not found & EXIT /B 1)
java -cp %WRAPPER_JAR% org.apache.maven.wrapper.MavenWrapperMain %*
