# Diagnostic script for adb reverse and backend reachability
# Usage: Open PowerShell as the user and run from project: .\tools\diagnose_adb.ps1

Write-Host "Checking connected devices via adb..."
adb devices

Write-Host "Listing current adb reverse rules..."
adb reverse --list

Write-Host "Setting adb reverse tcp:8084 -> tcp:8084 (if device connected)"
adb reverse tcp:8084 tcp:8084

Write-Host "Verifying reverse rules after setting..."
adb reverse --list

Write-Host "Testing backend endpoint via adb reverse from phone (127.0.0.1:8084)"
try {
    $resp = Invoke-WebRequest -UseBasicParsing -Uri "http://127.0.0.1:8084/api/categories" -TimeoutSec 5
    Write-Host "Status:" $resp.StatusCode
} catch {
    Write-Host "Request failed (phone unreachable via adb reverse). If you are using USB only, ensure you accepted the RSA fingerprint on device and that adb reverse is supported."
}

Write-Host "Testing OSRM route service (from PC) to measure latency"
$start = Get-Date
try{
    $r = Invoke-WebRequest -UseBasicParsing -Uri "https://router.project-osrm.org/route/v1/driving/2.3522,48.8566;2.295,48.8738?overview=false" -TimeoutSec 10
    $dur = (Get-Date) - $start
    Write-Host "OSRM responded in:" $dur.TotalMilliseconds "ms" -ForegroundColor Green
} catch {
    Write-Host "OSRM test failed or timed out" -ForegroundColor Red
}

Write-Host "Done. If phone can't reach tile.openstreetmap.org, connect phone to Wi-Fi or use a tunneling solution."