setlocal

set CLASSPATH=.;WEB-INF/classes
for %%i in (WEB-INF\lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%i

echo %CLASSPATH%

java %*

endlocal
