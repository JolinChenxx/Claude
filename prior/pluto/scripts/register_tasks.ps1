$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$campaign = Get-Content -LiteralPath (Join-Path $projectRoot 'state/campaign.json') -Raw | ConvertFrom-Json
$deadline = [DateTimeOffset]::Parse($campaign.deadline).ToOffset([TimeSpan]::FromHours(8))
$now = [DateTimeOffset]::UtcNow.ToOffset([TimeSpan]::FromHours(8))
$nextHour = [DateTimeOffset]::new($now.Year,$now.Month,$now.Day,$now.Hour,0,0,$now.Offset).AddHours(1)
$nextMorning = [DateTimeOffset]::new($now.Year,$now.Month,$now.Day,7,0,0,$now.Offset)
if ($nextMorning -le $now) { $nextMorning = $nextMorning.AddDays(1) }
$sid = [Security.Principal.WindowsIdentity]::GetCurrent().User.Value
$python = Join-Path $projectRoot '.venv/Scripts/pythonw.exe'
$report = Join-Path $projectRoot 'scripts/report.py'
function Register-ReportTask([string]$name,[string]$kind,[DateTimeOffset]$start,[string]$repeat) {
    $end = $deadline.ToString('yyyy-MM-ddTHH:mm:sszzz')
    $begin = $start.ToString('yyyy-MM-ddTHH:mm:sszzz')
    $repetition = if ($repeat) { "<Repetition><Interval>$repeat</Interval><StopAtDurationEnd>false</StopAtDurationEnd></Repetition>" } else { '' }
    $xml = @"
<?xml version="1.0" encoding="UTF-16"?>
<Task version="1.4" xmlns="http://schemas.microsoft.com/windows/2004/02/mit/task">
<RegistrationInfo><Description>S4 local deterministic report; no model/API calls.</Description></RegistrationInfo>
<Triggers><TimeTrigger>$repetition<StartBoundary>$begin</StartBoundary><EndBoundary>$end</EndBoundary><Enabled>true</Enabled></TimeTrigger></Triggers>
<Principals><Principal id="Author"><UserId>$sid</UserId><LogonType>InteractiveToken</LogonType><RunLevel>LeastPrivilege</RunLevel></Principal></Principals>
<Settings><MultipleInstancesPolicy>IgnoreNew</MultipleInstancesPolicy><DisallowStartIfOnBatteries>false</DisallowStartIfOnBatteries><StopIfGoingOnBatteries>false</StopIfGoingOnBatteries><StartWhenAvailable>true</StartWhenAvailable><RunOnlyIfNetworkAvailable>false</RunOnlyIfNetworkAvailable><ExecutionTimeLimit>PT15M</ExecutionTimeLimit><Enabled>true</Enabled></Settings>
<Actions Context="Author"><Exec><Command>$python</Command><Arguments>&quot;$report&quot; $kind</Arguments><WorkingDirectory>$projectRoot</WorkingDirectory></Exec></Actions>
</Task>
"@
    Register-ScheduledTask -TaskName $name -Xml $xml -Force | Select-Object TaskName,State
}
Register-ReportTask 'S4Plume_Hourly' 'hourly' $nextHour 'PT1H'
Register-ReportTask 'S4Plume_Daily0700' 'daily' $nextMorning 'P1D'
# Final report is triggered by the campaign supervisor after its final outputs are committed.
Get-ScheduledTask -TaskName 'S4Plume_*' | Get-ScheduledTaskInfo | Select-Object TaskName,NextRunTime,LastTaskResult
