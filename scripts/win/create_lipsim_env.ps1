<# T003: create the card's environment with configuration confined to its prefix. #>
$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path "$PSScriptRoot\..\..").Path
$condaExe = 'D:\sim_tools\miniforge3\Scripts\conda.exe'
$envPrefix = 'D:\sim_tools\envs\lipsim'
$configPath = 'D:\sim_tools\miniforge3\.condarc'
if (-not (Test-Path -LiteralPath $condaExe)) { throw 'Miniforge installation is not complete' }
if (Test-Path -LiteralPath (Join-Path $envPrefix 'conda-meta\history')) { throw 'Environment already exists; inspect it rather than recreate' }
@'
proxy_servers:
  http: http://127.0.0.1:7897
  https: http://127.0.0.1:7897
'@ | Set-Content -LiteralPath $configPath -Encoding utf8
# These affect this child process only, never the user/system environment.
$env:CONDARC = $configPath
$env:CONDA_PKGS_DIRS = 'D:\sim_tools\miniforge3\pkgs'
$env:CONDA_ENVS_PATH = 'D:\sim_tools\envs'
$env:CONDA_REGISTER_ENVS = 'false'
$env:CONDA_NOTIFY_OUTDATED_CONDA = 'false'
$env:PYTHONIOENCODING = 'utf-8'
Set-Location $repo
Copy-Item -LiteralPath $configPath -Destination 'results\T003\prefix-condarc.txt'
$process = Start-Process -FilePath $condaExe -ArgumentList 'env','create','--yes','--file','env\environment.yml','--prefix',$envPrefix -WorkingDirectory $repo -WindowStyle Hidden -Wait -PassThru -RedirectStandardOutput 'results\T003\env-create-stdout.log' -RedirectStandardError 'results\T003\env-create-stderr.log'
$code = $process.ExitCode
$code | Set-Content 'results\T003\env-create-exitcode.txt'
Write-Output "conda_env_create_exit=$code"
if ($code -ne 0) { throw 'Environment creation failed; inspect results/T003/env-create-*.log' }
