# BhookBuddy - Start All Services Script
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Starting BhookBuddy Food Delivery Platform" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

$BaseDir = $PSScriptRoot

# 1. Start Kafka if not already running on port 9092
$kafkaConn = Test-NetConnection -ComputerName localhost -Port 9092 -WarningAction SilentlyContinue
if (-not $kafkaConn.TcpTestSucceeded) {
    Write-Host "[1/9] Starting Kafka Broker (KRaft mode)..." -ForegroundColor Yellow
    $env:KAFKA_HEAP_OPTS = "-Xms128m -Xmx256m"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$BaseDir\kafka'; `$env:KAFKA_HEAP_OPTS = '-Xms128m -Xmx256m'; cmd.exe /c 'bin\windows\kafka-server-start.bat config\kraft\server.properties'" -WindowStyle Minimized
    Start-Sleep -Seconds 6
} else {
    Write-Host "[1/9] Kafka is already running on port 9092." -ForegroundColor Green
}

# Helper to start Spring Boot service
function Start-ServiceIfDown($name, $jarPath, $port) {
    $conn = Test-NetConnection -ComputerName localhost -Port $port -WarningAction SilentlyContinue
    if (-not $conn.TcpTestSucceeded) {
        Write-Host "Starting $name on port $port..." -ForegroundColor Yellow
        Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$BaseDir'; java -Xms64m -Xmx192m -jar '$jarPath'" -WindowStyle Minimized
        Start-Sleep -Seconds 4
    } else {
        Write-Host "$name is already running on port $port." -ForegroundColor Green
    }
}

# 2. Start Core Data Services
Start-ServiceIfDown "User Service" "$BaseDir\backend\user-service\target\user-service-1.0.0.jar" 8081
Start-ServiceIfDown "Restaurant Service" "$BaseDir\backend\restaurant-service\target\restaurant-service-1.0.0.jar" 8082

# 3. Start Order, Payment, Delivery & Notification Services
Start-ServiceIfDown "Order Service" "$BaseDir\backend\order-service\target\order-service-1.0.0.jar" 8083
Start-ServiceIfDown "Payment Service" "$BaseDir\backend\payment-service\target\payment-service-1.0.0.jar" 8084
Start-ServiceIfDown "Delivery Service" "$BaseDir\backend\delivery-service\target\delivery-service-1.0.0.jar" 8085
Start-ServiceIfDown "Notification Service" "$BaseDir\backend\notification-service\target\notification-service-1.0.0.jar" 8086

# 4. Start API Gateway
Start-ServiceIfDown "API Gateway" "$BaseDir\backend\api-gateway\target\api-gateway-1.0.0.jar" 8080

# 5. Start Frontend
$frontConn = Test-NetConnection -ComputerName localhost -Port 3000 -WarningAction SilentlyContinue
if (-not $frontConn.TcpTestSucceeded) {
    Write-Host "Starting Frontend (Vite) on port 3000..." -ForegroundColor Yellow
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$BaseDir\frontend'; npm run dev" -WindowStyle Minimized
    Start-Sleep -Seconds 3
} else {
    Write-Host "Frontend is already running on port 3000." -ForegroundColor Green
}

Write-Host "`nAll services initialized!" -ForegroundColor Green
Write-Host "Frontend URL:  http://localhost:3000" -ForegroundColor Cyan
Write-Host "API Gateway:   http://localhost:8080" -ForegroundColor Cyan
Write-Host "Admin Login:   admin@swiftbite.com / Admin@123" -ForegroundColor Cyan
Write-Host "Demo User:     john@example.com / User@123" -ForegroundColor Cyan
