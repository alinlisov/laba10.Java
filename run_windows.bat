@echo off
chcp 65001 >nul
cd /d "%~dp0"
if not exist out\classes mkdir out\classes
if not exist out\test mkdir out\test
powershell -NoProfile -Command "$files = Get-ChildItem -Path src -Filter '*.java' -Recurse | ForEach-Object FullName; & javac -encoding UTF-8 -d out/classes $files; if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }"
if errorlevel 1 (
  echo Compilation failed.
  pause
  exit /b 1
)
xcopy /E /I /Y resources out\classes >nul
powershell -NoProfile -Command "$files = Get-ChildItem -Path test -Filter '*.java' -Recurse | ForEach-Object FullName; & javac -encoding UTF-8 -cp out/classes -d out/test $files; if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }"
if errorlevel 1 (
  echo Compilation failed.
  pause
  exit /b 1
)
if /I "%1"=="test" (
  java -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 --add-opens=java.base/java.lang=ALL-UNNAMED -cp "out\classes;out\test" lab10.VerificationTests
) else (
  java -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 --add-opens=java.base/java.lang=ALL-UNNAMED -cp out\classes lab10.Main
)
pause
