$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$runtimeDir = Join-Path $projectRoot 'data\runtime'
$logDir = Join-Path $projectRoot 'data\logs'
New-Item -ItemType Directory -Force -Path $runtimeDir | Out-Null
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

$backend = Start-Process -FilePath 'mvn.cmd' -ArgumentList 'spring-boot:run' -WorkingDirectory (Join-Path $projectRoot 'backend') -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $logDir 'backend.out.log') -RedirectStandardError (Join-Path $logDir 'backend.err.log')
$frontend = Start-Process -FilePath 'npm.cmd' -ArgumentList 'run','dev' -WorkingDirectory (Join-Path $projectRoot 'frontend') -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $logDir 'frontend.out.log') -RedirectStandardError (Join-Path $logDir 'frontend.err.log')

@{ backendPid = $backend.Id; frontendPid = $frontend.Id; startedAt = (Get-Date).ToString('o') } |
    ConvertTo-Json |
    Set-Content -LiteralPath (Join-Path $runtimeDir 'local-processes.json') -Encoding UTF8

Start-Sleep -Seconds 3
Start-Process 'http://localhost:5173'
Write-Host '两个人的漫游簿已启动：http://localhost:5173'
Write-Host '如需停止，请运行 .\stop-local.ps1'
