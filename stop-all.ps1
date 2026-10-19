# BhookBuddy - Stop All Services Script
Write-Host "Stopping all BhookBuddy services..." -ForegroundColor Yellow

$ports = @(3000, 8080, 8081, 8082, 8083, 8084, 8085, 8086, 9092, 9093)

foreach ($port in $ports) {
    $processes = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($pidToKill in $processes) {
        if ($pidToKill -and $pidToKill -gt 0) {
            try {
                Stop-Process -Id $pidToKill -Force -ErrorAction SilentlyContinue
                Write-Host "Killed process $pidToKill listening on port $port" -ForegroundColor Green
            } catch {
                Write-Host "Could not kill process $pidToKill on port $port" -ForegroundColor Red
            }
        }
    }
}

Write-Host "All BhookBuddy services have been stopped." -ForegroundColor Cyan
