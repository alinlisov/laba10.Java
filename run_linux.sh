#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p out/classes out/test
find src -name '*.java' -print0 | xargs -0 javac -encoding UTF-8 -d out/classes
cp -r resources/* out/classes/
find test -name '*.java' -print0 | xargs -0 javac -encoding UTF-8 -cp out/classes -d out/test
case "${1:-main}" in
  main) java -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 --add-opens=java.base/java.lang=ALL-UNNAMED -cp out/classes lab10.Main ;;
  test) java -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 --add-opens=java.base/java.lang=ALL-UNNAMED -cp out/classes:out/test lab10.VerificationTests ;;
  *) echo 'Usage: ./run_linux.sh main|test'; exit 1 ;;
esac
