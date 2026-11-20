"""Run the card's two imaging commands sequentially with 16 workers each."""
from pathlib import Path
import subprocess
import sys

repo = Path(__file__).resolve().parents[2]
failed = []
for series in ("low", "high"):
    command = [sys.executable, "scripts/image_members.py", "D:/sim_runs/T015",
               "--series", series, "--tables", "D:/sim_runs/tables/v1",
               "--workers", "16"]
    print("starting", series, flush=True)
    result = subprocess.run(command, cwd=repo)
    print("series completed", series, "rc", result.returncode, flush=True)
    if result.returncode:
        failed.append(series)
print("failed series:", failed, flush=True)
raise SystemExit(1 if failed else 0)
