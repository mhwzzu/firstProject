$projectRoot = $PSScriptRoot
$processFile = Join-Path $projectRoot 'data\runtime\local-processes.json'
if (-not (Test-Path -LiteralPath $processFile)) {
    Write-Host '没有找到本地运行记录。'
    exit 0
}

$saved = Get-Content -LiteralPath $processFile -Raw | ConvertFrom-Json
@($saved.backendPid, $saved.frontendPid) | ForEach-Object {
    $process = Get-Process -Id $_ -ErrorAction SilentlyContinue
    if ($process) { Stop-Process -Id $process.Id }
}
Remove-Item -LiteralPath $processFile
Write-Host '本地前后端服务已停止。'
