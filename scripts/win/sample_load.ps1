param([string]$OutFile)
$ErrorActionPreference='Stop'
$before=@{}
Get-Process | ForEach-Object { if ($null -ne $_.CPU) { $before[$_.Id]=[double]$_.CPU } }
$watch=[Diagnostics.Stopwatch]::StartNew()
Start-Sleep -Seconds 2
$after=Get-Process
$seconds=$watch.Elapsed.TotalSeconds
$inventory=Get-CimInstance Win32_Process
$fdtdIds=New-Object 'System.Collections.Generic.HashSet[int]'
foreach($p in $inventory) { if($p.Name -match 'fdtd') { [void]$fdtdIds.Add([int]$p.ProcessId) } }
do {
  $added=$false
  foreach($p in $inventory) { if($fdtdIds.Contains([int]$p.ParentProcessId) -and $fdtdIds.Add([int]$p.ProcessId)) { $added=$true } }
} while($added)
$samples=@($after | ForEach-Object {
  if($before.ContainsKey($_.Id) -and $null -ne $_.CPU -and $_.ProcessName -ne 'Idle') {
    [pscustomobject]@{pid=$_.Id; name=$_.ProcessName; cpu_seconds=[double]$_.CPU; sampled_cores=[Math]::Max([double]0,([double]$_.CPU-$before[$_.Id])/$seconds); fdtd=$fdtdIds.Contains($_.Id)}
  }
})
$busy=($samples | Measure-Object sampled_cores -Sum).Sum
$fdtd=($samples | Where-Object fdtd | Measure-Object sampled_cores -Sum).Sum
$result=[pscustomobject]@{
  sampled_at=[System.TimeZoneInfo]::ConvertTimeBySystemTimeZoneId([DateTimeOffset]::UtcNow,'China Standard Time').ToString('o');
  sample_seconds=$seconds; physical_cores=64; logical_processors=128;
  sampled_cpu_core_equivalents=$busy; free_physical_core_estimate=[Math]::Max(0,64-[Math]::Ceiling($busy));
  fdtd_core_equivalents=[double]$fdtd;
  note='CPU-time deltas estimate core equivalents, not NUMA/SMT placement. Unrelated jobs are untouched.';
  top10_sampled=@($samples | Sort-Object sampled_cores -Descending | Select-Object -First 10);
  top10_cumulative=@($samples | Sort-Object cpu_seconds -Descending | Select-Object -First 10)
}
$json=$result | ConvertTo-Json -Depth 5
if($OutFile) { $json | Set-Content -Encoding UTF8 -LiteralPath $OutFile }
$json
