# PowerShell runner script for Spring Boot Backend
$backendDir = $PSScriptRoot
Set-Location $backendDir

Write-Host "==========================================================" -ForegroundColor Green
Write-Host " Starting Student Management System Backend Setup" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green

# 1. Check Java
Write-Host "`n[1/3] Checking Java runtime..." -ForegroundColor Cyan
if (!(Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Host "ERROR: Java is not found in your system PATH." -ForegroundColor Red
    Write-Host "Please ensure OpenJDK 17+ is installed." -ForegroundColor Yellow
    exit 1
}
java -version

# 2. Check or auto-install Maven
Write-Host "`n[2/3] Checking Maven installation..." -ForegroundColor Cyan
$mvnExec = $null

# Check if 'mvn' is in PATH
if (Get-Command mvn -ErrorAction SilentlyContinue) {
    $mvnExec = "mvn"
    Write-Host "Found system Maven in PATH." -ForegroundColor Green
}
# Check if portable maven was already downloaded in backend directory
elseif (Test-Path "$backendDir\mvn-portable") {
    $found = Get-ChildItem -Path "$backendDir\mvn-portable" -Filter "mvn.cmd" -Recurse | Select-Object -First 1
    if ($found) {
        $mvnExec = $found.FullName
        Write-Host "Found portable Maven at: $mvnExec" -ForegroundColor Green
    }
}

# If Maven is not installed anywhere, automatically download and set up portable Maven
if (!$mvnExec) {
    Write-Host "Maven ('mvn') was not detected on your system." -ForegroundColor Yellow
    Write-Host "Automatically setting up portable Apache Maven for you (this takes ~15 seconds)..." -ForegroundColor Cyan

    $zipUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip"
    $zipPath = "$env:TEMP\apache-maven-portable.zip"
    $destDir = "$backendDir\mvn-portable"

    try {
        Write-Host "Downloading Apache Maven from Apache Archive..." -ForegroundColor Gray
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $zipUrl -OutFile $zipPath -UseBasicParsing

        Write-Host "Extracting to $destDir..." -ForegroundColor Gray
        New-Item -ItemType Directory -Path $destDir -Force | Out-Null
        Expand-Archive -Path $zipPath -DestinationPath $destDir -Force

        Remove-Item -Path $zipPath -Force -ErrorAction SilentlyContinue

        $found = Get-ChildItem -Path $destDir -Filter "mvn.cmd" -Recurse | Select-Object -First 1
        if ($found) {
            $mvnExec = $found.FullName
            Write-Host "Portable Maven ready: $mvnExec" -ForegroundColor Green
        } else {
            throw "mvn.cmd not found after extraction."
        }
    } catch {
        Write-Host "Auto-download failed: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "Please install Maven manually by running:" -ForegroundColor Yellow
        Write-Host "    winget install Apache.Maven" -ForegroundColor White
        Write-Host "Then close and restart your terminal." -ForegroundColor White
        exit 1
    }
}

# 3. Configure Database Credentials
Write-Host "`n[3/3] Checking MySQL configuration..." -ForegroundColor Cyan
if (-not $env:DB_USERNAME) { $env:DB_USERNAME = "root" }
if (-not $env:DB_PASSWORD) {
    Write-Host "Using default XAMPP MySQL credentials (User: $env:DB_USERNAME, empty password)." -ForegroundColor Gray
    Write-Host "If your MySQL has a password, set it using: `$env:DB_PASSWORD = 'your_password'" -ForegroundColor Gray
}

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host " Launching Spring Boot Backend on http://localhost:8080" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green

& $mvnExec spring-boot:run
