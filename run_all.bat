@echo off
REM run_all.bat - packages, builds and runs docker-compose with health checks and smoke tests
cd /d %~dp0

:: create logs dir
if not exist logs mkdir logs

:: Step 1: Build package (shaded jar)
echo 1/5: Packaging project with Maven...
call mvn -DskipTests=false package
if errorlevel 1 (
  echo Maven package failed
  exit /b 1
)

:: Step 2: Build images
echo 2/5: Building docker images...
docker-compose build
if errorlevel 1 (
  echo docker-compose build failed
  exit /b 2
)

:: Step 3: Start DB only and wait for its health
echo 3/5: Starting DB and waiting for it to become healthy...
docker-compose up -d db
if errorlevel 1 (
  echo docker-compose up db failed
  exit /b 3
)

:: Wait loop: check docker container health via docker inspect
set CONTAINER_NAME=
for /f "tokens=1" %%i in ('docker-compose ps -q db') do set CONTAINER_NAME=%%i
if "%CONTAINER_NAME%"=="" (
  echo Could not find db container ID
  exit /b 4
)

set /a ELAPSED=0
set TIMEOUT=60
echo Waiting up to %TIMEOUT% seconds for Postgres to be healthy (container %CONTAINER_NAME%)...
:db_wait_loop
set HEALTH_STATUS=
for /f "usebackq tokens=*" %%H in (`docker inspect --format="{{.State.Health.Status}}" %CONTAINER_NAME% 2^>nul`) do set HEALTH_STATUS=%%H
if defined HEALTH_STATUS (
  if /I "%HEALTH_STATUS%"=="healthy" (
    echo DB container reports healthy.
    goto db_healthy
  ) else (
    echo DB health status: %HEALTH_STATUS%
  )
) else (
  echo No health status reported yet.
)
if %ELAPSED% GEQ %TIMEOUT% (
  echo Timeout waiting for DB to become healthy after %ELAPSED% seconds.
  echo Collecting diagnostic logs to logs/db_timeout_*.log ...
  docker-compose logs db --no-color > logs/db_timeout_compose.log 2>&1
  docker inspect --format='{{json .State.Health}}' %CONTAINER_NAME% > logs/db_timeout_health.json 2>&1
  docker-compose logs app --no-color > logs/app_timeout_compose.log 2>&1
  echo Attempting to run psql inside DB container to list tables and count devices (if container allows)...
  docker exec %CONTAINER_NAME% psql -U postgres -d postgres -c "\dt" > logs/db_timeout_tables.txt 2>&1 || echo psql list tables failed >> logs/db_timeout_tables.txt
  docker exec %CONTAINER_NAME% psql -U postgres -d postgres -c "select count(*) from devices;" > logs/db_timeout_devices_count.txt 2>&1 || echo psql count devices failed >> logs/db_timeout_devices_count.txt
  echo Diagnostics collected in logs/ folder.
  exit /b 5
)
ping -n 3 127.0.0.1 >nul
set /a ELAPSED+=3
echo Still waiting for DB to be healthy... %ELAPSED% sec
goto db_wait_loop

:db_healthy
echo DB is healthy. Proceeding to start app.

:: PRE-START: free port 8082 if any local process is listening (useful for dev iteration)
echo Pre-check: freeing port 8082 if occupied...
set KILLED_ANY=0
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":8082"') do (
  echo Found process with PID %%p using port 8082 - attempting to kill...
  taskkill /PID %%p /F >nul 2>&1
  if errorlevel 0 (
    echo Killed PID %%p.
    set KILLED_ANY=1
  ) else (
    echo Failed to kill PID %%p. You may need to run this script as Administrator.
  )
)
if "%KILLED_ANY%"=="1" (
  echo Waiting 2 seconds for OS to free the port...
  ping -n 2 127.0.0.1 >nul
)

:: Step 4: Start app service
echo 4/5: Starting app service...
docker-compose up -d app
if errorlevel 1 (
  echo docker-compose up app failed
  exit /b 6
)

:: Step 5: Run smoke tests using existing script (it waits for /health). Pass host/port if needed.
echo 5/5: Running smoke tests...
call run_wait_and_smoke.bat localhost 8082 60
set SMOKE_EXIT=%ERRORLEVEL%
if %SMOKE_EXIT%==0 (
  echo Smoke tests passed.
) else (
  echo Smoke tests failed with exit code %SMOKE_EXIT%.
  echo Collecting diagnostic logs to logs/smoke_fail_*.log ...
  for /f "tokens=1" %%i in ('docker-compose ps -q db') do set DB_CID=%%i
  for /f "tokens=1" %%i in ('docker-compose ps -q app') do set APP_CID=%%i
  docker-compose logs db --no-color > logs/db_smoke_fail_compose.log 2>&1
  docker-compose logs app --no-color > logs/app_smoke_fail_compose.log 2>&1
  if defined DB_CID (
    docker inspect --format='{{json .State.Health}}' %DB_CID% > logs/db_smoke_fail_health.json 2>&1
    docker exec %DB_CID% psql -U postgres -d postgres -c "\dt" > logs/db_smoke_fail_tables.txt 2>&1 || echo psql list tables failed >> logs/db_smoke_fail_tables.txt
    docker exec %DB_CID% psql -U postgres -d postgres -c "select count(*) from devices;" > logs/db_smoke_fail_devices_count.txt 2>&1 || echo psql count devices failed >> logs/db_smoke_fail_devices_count.txt
  ) else (
    echo Could not determine DB container ID; skipping psql checks.
  )
  echo Diagnostics collected in logs/ folder.
)
exit /b %SMOKE_EXIT%