@echo off
REM Usage: run_wait_and_smoke.bat [host] [port] [timeoutSeconds]
setlocal enabledelayedexpansion
set HOST=%1
if "%HOST%"=="" set HOST=localhost
set PORT=%2
if "%PORT%"=="" set PORT=8082
set TIMEOUT=%3
if "%TIMEOUT%"=="" set TIMEOUT=60

echo Waiting for service at http://%HOST%:%PORT%/health (timeout %TIMEOUT%s)...
set /a ELAPSED=0
set HEALTH_OK=0
:poll_loop
for /f "usebackq tokens=*" %%A in (`powershell -Command "Try { $r = Invoke-RestMethod -Uri 'http://%HOST%:%PORT%/health' -UseBasicParsing -TimeoutSec 5; if ($r.status -eq 'UP' -and $r.db -eq 'UP') { Write-Output 'OK' } } Catch { }"`) do (
    set RESPONSE=%%A
)
if "%RESPONSE%"=="OK" (
    set HEALTH_OK=1
    goto :healthy
)
if %ELAPSED% GEQ %TIMEOUT% (
    echo Timeout waiting for healthy /health (db:UP) endpoint after %ELAPSED% seconds.
    goto :failed
)
ping -n 3 127.0.0.1 >nul
set /a ELAPSED+=2
echo Still waiting for DB to be UP... %ELAPSED% sec
goto :poll_loop

:healthy
echo Service is healthy (DB UP) after %ELAPSED% seconds.

echo Running smoke tests against /devices/ and /devices/count/ ...
set DEVICE_OK=0
set COUNT_OK=0

echo Testing /devices/ ...
powershell -Command "Try { $r = Invoke-RestMethod -Uri 'http://%HOST%:%PORT%/devices/' -UseBasicParsing -TimeoutSec 10; if ($r -is [System.Array] -or $r -is [System.Object[]]) { Write-Output 'DEVICES_OK' } else { Write-Output 'DEVICES_OK' } } Catch { Write-Output 'DEVICES_FAIL' }" > tmp_devices_result.txt
set /p DEV_RESULT=<tmp_devices_result.txt
if "%DEV_RESULT%"=="DEVICES_OK" (
    set DEVICE_OK=1
    echo PASS: /devices/ returned JSON array
) else (
    echo FAIL: /devices/ did not return expected JSON (or request failed)
)
del /q tmp_devices_result.txt >nul 2>&1

echo Testing /devices/count/ ...
powershell -Command "Try { $r = Invoke-RestMethod -Uri 'http://%HOST%:%PORT%/devices/count/' -UseBasicParsing -TimeoutSec 10; if ($r.count -ne $null) { Write-Output 'COUNT_OK' } else { Write-Output 'COUNT_FAIL' } } Catch { Write-Output 'COUNT_FAIL' }" > tmp_count_result.txt
set /p CNT_RESULT=<tmp_count_result.txt
if "%CNT_RESULT%"=="COUNT_OK" (
    set COUNT_OK=1
    echo PASS: /devices/count/ returned JSON with count property
) else (
    echo FAIL: /devices/count/ did not return expected JSON (or request failed)
)
del /q tmp_count_result.txt >nul 2>&1

echo Summary:
if %DEVICE_OK%==1 (echo PASS: /devices/) else (echo FAIL: /devices/)
if %COUNT_OK%==1 (echo PASS: /devices/count/) else (echo FAIL: /devices/count/)

if %DEVICE_OK%==1 if %COUNT_OK%==1 (
    echo All smoke tests passed.
    exit /b 0
) else (
    echo Some smoke tests failed.
    exit /b 2
)

:failed
echo Service did not become healthy (db:UP) in time. Aborting smoke tests.
exit /b 1