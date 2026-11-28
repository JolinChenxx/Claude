$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$campaign = Get-Content -LiteralPath (Join-Path $projectRoot 'state/campaign.json') -Raw | ConvertFrom-Json
$deadline = [DateTimeOffset]::Parse($campaign.deadline).ToOffset([TimeSpan]::FromHours(8))
$start = [DateTimeOffset]::UtcNow.AddSeconds(5).ToOffset([TimeSpan]::FromHours(8))
$sid = [Security.Principal.WindowsIdentity]::GetCurrent().User.Value
$python = Join-Path $projectRoot '.venv/Scripts/pythonw.exe'
$controller = Join-Path $projectRoot 'scripts/campaign.py'
$begin = $start.ToString('yyyy-MM-ddTHH:mm:sszzz')
$end = $deadline.ToString('yyyy-MM-ddTHH:mm:sszzz')
$xml = @"
<?xml version="1.0" encoding="UTF-16"?>
<Task version="1.4" xmlns="http://schemas.microsoft.com/windows/2004/02/mit/task">
<RegistrationInfo><Description>S4 72-hour local computation supervisor; no LLM/API calls.</Description></RegistrationInfo>
<Triggers><TimeTrigger><Repetition><Interval>PT5M</Interval><StopAtDurationEnd>false</StopAtDurationEnd></Repetition><StartBoundary>$begin</StartBoundary><EndBoundary>$end</EndBoundary><Enabled>true</Enabled></TimeTrigger><LogonTrigger><EndBoundary>$end</EndBoundary><Enabled>true</Enabled><UserId>$sid</UserId></LogonTrigger></Triggers>
<Principals><Principal id="Author"><UserId>$sid</UserId><LogonType>InteractiveToken</LogonType><RunLevel>LeastPrivilege</RunLevel></Principal></Principals>
<Settings><MultipleInstancesPolicy>IgnoreNew</MultipleInstancesPolicy><DisallowStartIfOnBatteries>false</DisallowStartIfOnBatteries><StopIfGoingOnBatteries>false</StopIfGoingOnBatteries><StartWhenAvailable>true</StartWhenAvailable><ExecutionTimeLimit>PT75H</ExecutionTimeLimit><Enabled>true</Enabled></Settings>
<Actions Context="Author"><Exec><Command>$python</Command><Arguments>&quot;$controller&quot;</Arguments><WorkingDirectory>$projectRoot</WorkingDirectory></Exec></Actions>
</Task>
"@
Register-ScheduledTask -TaskName 'S4Plume_Watchdog' -Xml $xml -Force | Select-Object TaskName,State
Start-ScheduledTask -TaskName 'S4Plume_Watchdog'
