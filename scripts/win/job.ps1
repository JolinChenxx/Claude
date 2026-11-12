<#
Long-job management (Windows PowerShell 5.1+ / pwsh 7)
Usage:
  .\scripts\win\job.ps1 start  -Id T005 -Cmd "build\solver.exe cases\base.toml" [-RunRoot D:\sim_runs] [-Threads 72]
  .\scripts\win\job.ps1 status -Id T005
  .\scripts\win\job.ps1 stop   -Id T005
  .\scripts\win\job.ps1 list
For each job: <RunRoot>\<Id>\job.json (metadata), stdout.log, stderr.log, exitcode.txt (written when it finishes)
The started process keeps running after the Codex session ends; the next pass checks it with status.
#>
param(
  [Parameter(Mandatory = $true, Position = 0)][ValidateSet('start', 'status', 'stop', 'list')][string]$Action,
  [string]$Id,
  [string]$Cmd,
  [string]$RunRoot = $(if ($env:SIM_RUN_ROOT) { $env:SIM_RUN_ROOT } else { 'D:\sim_runs' }),
  [int]$Threads = 72,
  [int]$Tail = 30
)
$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path "$PSScriptRoot\..\..").Path

function JobDir([string]$id) { Join-Path $RunRoot $id }

switch ($Action) {
  'start' {
    if (-not $Id -or -not $Cmd) { throw 'start requires -Id and -Cmd' }
    $dir = JobDir $Id
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    $meta = Join-Path $dir 'job.json'
    if (Test-Path $meta) {
      $old = Get-Content $meta -Raw | ConvertFrom-Json
      if (Get-Process -Id $old.pid -ErrorAction SilentlyContinue) { throw "job $Id still running (pid $($old.pid))" }
    }
    Remove-Item -ErrorAction SilentlyContinue (Join-Path $dir 'exitcode.txt')
    $out = Join-Path $dir 'stdout.log'; $err = Join-Path $dir 'stderr.log'
    $ec = Join-Path $dir 'exitcode.txt'
    # Wrap in cmd so the exit code is written to a file when the job ends; pass the thread count via environment variables
    $wrapped = "set OMP_NUM_THREADS=$Threads&& set SIM_OUT_DIR=$dir&& cd /d `"$repo`" && $Cmd & echo !ERRORLEVEL! > `"$ec`""
    $p = Start-Process -FilePath 'cmd.exe' -ArgumentList '/v:on', '/c', $wrapped -RedirectStandardOutput $out -RedirectStandardError $err -WindowStyle Hidden -PassThru
    $commit = (git -C $repo rev-parse --short HEAD) 2>$null
    [pscustomobject]@{ id = $Id; pid = $p.Id; cmd = $Cmd; threads = $Threads; commit = $commit; started = (Get-Date).ToString('s'); dir = $dir } |
      ConvertTo-Json | Set-Content -Encoding UTF8 $meta
    Write-Output "started $Id pid=$($p.Id) dir=$dir"
  }
  'status' {
    if (-not $Id) { throw 'status requires -Id' }
    $dir = JobDir $Id; $meta = Join-Path $dir 'job.json'
    if (-not (Test-Path $meta)) { Write-Output "STATE=none"; break }
    $m = Get-Content $meta -Raw | ConvertFrom-Json
    $ec = Join-Path $dir 'exitcode.txt'
    $alive = [bool](Get-Process -Id $m.pid -ErrorAction SilentlyContinue)
    if ($alive) { $state = 'running' }
    elseif (Test-Path $ec) { $code = (Get-Content $ec -Raw).Trim(); $state = $(if ($code -eq '0') { 'finished' } else { "failed(exit=$code)" }) }
    else { $state = 'died(no exitcode; killed or machine rebooted)' }
    $elapsed = (New-TimeSpan -Start ([datetime]$m.started) -End (Get-Date)).ToString('d\.hh\:mm\:ss')
    Write-Output "STATE=$state  elapsed=$elapsed  pid=$($m.pid)  commit=$($m.commit)"
    Write-Output "--- stdout (last $Tail) ---"; Get-Content (Join-Path $dir 'stdout.log') -Tail $Tail -ErrorAction SilentlyContinue
    Write-Output "--- stderr (last $Tail) ---"; Get-Content (Join-Path $dir 'stderr.log') -Tail $Tail -ErrorAction SilentlyContinue
  }
  'stop' {
    $m = Get-Content (Join-Path (JobDir $Id) 'job.json') -Raw | ConvertFrom-Json
    & taskkill.exe /PID $m.pid /T /F | Out-Null
    Write-Output "stopped $Id"
  }
  'list' {
    Get-ChildItem $RunRoot -Directory -ErrorAction SilentlyContinue | ForEach-Object {
      $mf = Join-Path $_.FullName 'job.json'
      if (Test-Path $mf) { $m = Get-Content $mf -Raw | ConvertFrom-Json; $a = [bool](Get-Process -Id $m.pid -ErrorAction SilentlyContinue); "{0,-8} alive={1,-5} started={2} cmd={3}" -f $m.id, $a, $m.started, $m.cmd }
    }
  }
}
