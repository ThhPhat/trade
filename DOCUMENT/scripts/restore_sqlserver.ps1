param (
    [string]$ServerInstance = "localhost"
)

$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent (Split-Path -Parent $scriptDir)
$bakSource = Join-Path $scriptDir "QL_NhaSach.bak"
$tempDir = "C:\Temp"
$bakTemp = Join-Path $tempDir "QL_NhaSach.bak"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " [THE4BOOKSTORE] TIEN TRINH PHUC HOI CSDL SQL SERVER " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan

if (-not (Test-Path $bakSource)) {
    Write-Host "[!] Khong tim thay file backup tai: $bakSource" -ForegroundColor Red
    exit 1
}

# 1. Chuan bi thu muc C:\Temp
Write-Host "[1/4] Chuan bi file backup trong thu muc dem C:\Temp..." -ForegroundColor Yellow
if (-not (Test-Path $tempDir)) {
    New-Item -ItemType Directory -Path $tempDir -Force | Out-Null
}
try {
    icacls $tempDir /grant "Everyone:(OI)(CI)F" /T | Out-Null
} catch {}
Copy-Item $bakSource $bakTemp -Force
Write-Host "  -> Da copy QL_NhaSach.bak vao $tempDir" -ForegroundColor Green

# 2. Do tim SQL Server instance
Write-Host "[2/4] Kiem tra ket noi SQL Server..." -ForegroundColor Yellow
$instances = @($ServerInstance, ".\SQLEXPRESS", "localhost\SQLEXPRESS", "(local)")
$connectedInstance = $null

foreach ($inst in $instances) {
    try {
        $testConn = New-Object System.Data.SqlClient.SqlConnection("Server=$inst;Database=master;Integrated Security=True;TrustServerCertificate=True;Connect Timeout=3")
        $testConn.Open()
        $testConn.Close()
        $connectedInstance = $inst
        Write-Host "  -> Ket noi thanh cong den SQL Server: $connectedInstance" -ForegroundColor Green
        break
    } catch {}
}

if (-not $connectedInstance) {
    Write-Host "[X] Khong the ket noi SQL Server voi Windows Authentication tren localhost hoac .\SQLEXPRESS." -ForegroundColor Red
    Write-Host "Vui long kiem tra dich vu SQL Server da duoc Start chua." -ForegroundColor Red
    exit 1
}

# 3. Phuc hoi CSDL
Write-Host "[3/4] Dang phuc hoi co so du lieu QL_NhaSach..." -ForegroundColor Yellow
$conn = New-Object System.Data.SqlClient.SqlConnection("Server=$connectedInstance;Database=master;Integrated Security=True;TrustServerCertificate=True")
$conn.Open()

$restoreSql = "IF EXISTS (SELECT name FROM sys.databases WHERE name = 'QL_NhaSach') " +
"BEGIN " +
"    ALTER DATABASE [QL_NhaSach] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; " +
"END; " +
"RESTORE DATABASE [QL_NhaSach] FROM DISK = '$bakTemp' WITH REPLACE; " +
"ALTER DATABASE [QL_NhaSach] SET MULTI_USER; " +
"IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = 'the4book_user') " +
"BEGIN " +
"    CREATE LOGIN [the4book_user] WITH PASSWORD = 'ChangeMe@2026', CHECK_POLICY = OFF; " +
"END; " +
"USE [QL_NhaSach]; " +
"IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'the4book_user') " +
"BEGIN " +
"    CREATE USER [the4book_user] FOR LOGIN [the4book_user]; " +
"END; " +
"ALTER ROLE [db_owner] ADD MEMBER [the4book_user];"

$cmd = $conn.CreateCommand()
$cmd.CommandTimeout = 120
$cmd.CommandText = $restoreSql
$cmd.ExecuteNonQuery() | Out-Null
$conn.Close()
Write-Host "  -> Phuc hoi CSDL QL_NhaSach va cau hinh user the4book_user THANH CONG!" -ForegroundColor Green

# 4. Kiem tra va tao file .env
Write-Host "[4/4] Kiem tra cau hinh file .env..." -ForegroundColor Yellow
$repoRoot = (Get-Item $scriptDir).Parent.Parent.FullName
$envFile = Join-Path $repoRoot ".env"
$envExample = Join-Path $repoRoot ".env.example"

if (-not (Test-Path $envFile) -and (Test-Path $envExample)) {
    Copy-Item $envExample $envFile -Force
    Write-Host "  -> Da tu dong tao file .env tu .env.example" -ForegroundColor Green
} else {
    Write-Host "  -> File .env da san sang." -ForegroundColor Cyan
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "[OK] HOAN TAT THIET LAP CSDL! BAN DA SAN SANG CHAY DU AN." -ForegroundColor Green
Write-Host "  Lenh chay web: .\mvnw.cmd spring-boot:run" -ForegroundColor Yellow
Write-Host "  Tai khoan mau (mat khau tat ca la 123456):" -ForegroundColor Yellow
Write-Host "    - Admin:   admin@the4bookstore.vn" -ForegroundColor Gray
Write-Host "    - Manager: manager1@the4bookstore.vn" -ForegroundColor Gray
Write-Host "    - Vendor:  shop_fahasa@gmail.com" -ForegroundColor Gray
Write-Host "    - Khach:   tran.van.b@gmail.com" -ForegroundColor Gray
Write-Host "==========================================================" -ForegroundColor Cyan
