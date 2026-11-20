# Process-local conda runtime paths; no activation or policy changes.
$prefix = 'D:/sim_tools/envs/lipsim'
$env:PATH = "$prefix;$prefix/Library/bin;$prefix/Scripts;" + $env:PATH
$env:PYTHONDONTWRITEBYTECODE = '1'
$env:PYTHONIOENCODING = 'utf-8'
$env:OMP_NUM_THREADS = '1'
$env:OPENBLAS_NUM_THREADS = '1'
$env:MKL_NUM_THREADS = '1'
& "$prefix/python.exe" @args
exit $LASTEXITCODE
