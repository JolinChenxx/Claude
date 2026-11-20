param(
    [Parameter(Mandatory=$true)]
    [ValidateSet('status','check','post','compare','report','build','solve','resume')]
    [string]$Action,
    [Parameter(ValueFromRemainingArguments=$true)]
    [string[]]$Arguments
)
$ErrorActionPreference='Stop'
$env:PYTHONUTF8='1'
$projectRoot=Join-Path $PSScriptRoot 'project'
$python64=Join-Path (Split-Path $PSScriptRoot -Parent) 'runtime\Python311\python.exe'
if (-not (Test-Path -LiteralPath $python64)) { throw "Missing independent Python runtime: $python64" }
if (-not $env:COMSOL_ROOT) { $env:COMSOL_ROOT='C:\Program Files\COMSOL\COMSOL64\Multiphysics' }
Push-Location $projectRoot
try {
    switch ($Action) {
        'status' {
            Get-Content -LiteralPath (Join-Path $projectRoot 'delivery_state.json')
            Get-Content -LiteralPath (Join-Path $projectRoot 'status.json')
        }
        'check' { & $python64 (Join-Path $PSScriptRoot 'server_tools\server_setup.py') check }
        'report' { & $python64 (Join-Path $projectRoot 'work\deliver_report.py') }
        default { & $python64 (Join-Path $projectRoot 'work\reproduce.py') $Action @Arguments }
    }
    if ($Action -ne 'status' -and $LASTEXITCODE -ne 0) { throw "Command failed with exit code $LASTEXITCODE" }
}
finally { Pop-Location }
