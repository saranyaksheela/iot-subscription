@echo off
cd /d %~dp0
REM Build package (shaded jar)
mvn -DskipTests=false package
if errorlevel 1 (
  echo Maven package failed
  exit /b 1
)
REM Build and bring up docker-compose
docker-compose build
if errorlevel 1 (
  echo docker-compose build failed
  exit /b 1
)
docker-compose up -d
echo Deployed compose services
