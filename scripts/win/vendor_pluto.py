"""T008: copy the verified official archive and actual T005 build adapters."""
from pathlib import Path
import hashlib
import json
import os
import shutil
import subprocess
import tarfile

REPO = Path(__file__).resolve().parents[2]
ROOT = Path('D:/sim_tools/pluto_ref')
SOURCE = Path('D:/junlin/PlasmaPlumeHighInfluence/vendor/pluto-4.4-patch4.tar.gz')
EXPECTED = '1ba5527b76d49fdd78ae24dbfbdad085ec83393748f1e618516a9d63bd945787'

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def main():
    assert sha(SOURCE) == EXPECTED, 'Official archive checksum differs'
    assert SOURCE.stat().st_size <= 45_000_000, 'Use the task source-only procedure'
    dest = REPO / 'third_party/pluto'
    out = REPO / 'results/T008'
    dest.mkdir(parents=True, exist_ok=True)
    out.mkdir(parents=True, exist_ok=True)
    shutil.copy2(SOURCE, dest / SOURCE.name)
    assert sha(dest / SOURCE.name) == EXPECTED
    with tarfile.open(SOURCE) as tf:
        license_text = tf.extractfile('PLUTO/COPYING').read().decode('utf-8')
    first_paragraph = license_text.strip().split('\n\n')[0]
    fence = chr(96) * 3
    dest.joinpath('README.md').write_text(
        '# PLUTO official source archive\n\n'
        '- Version: 4.4-patch4.\n'
        '- URL: https://plutocode.ph.unito.it/Downloads/pluto-4.4-patch4.tar.gz\n'
        f'- SHA256: {EXPECTED}.\n'
        f'- Bytes: {SOURCE.stat().st_size}.\n'
        '- Original archive copied byte-for-byte; not repackaged.\n'
        '- 本目录文件不得修改。Do not modify archived files or adapter copies.\n'
        '- License file: PLUTO/COPYING. Its opening paragraph is reproduced verbatim below; the complete file in the archive governs use.\n\n'
        f'{fence}text\n{first_paragraph}\n{fence}\n\n'
        'windows/ holds build adapters copied from the actual T005 build. No project physics patches, MPI SDK, or ANSYS files are included.\n', encoding='utf-8')
    sources = {
        'Src/HD/makefile': ROOT / 'PLUTO/Src/HD/makefile',
        'Config/Windows.serial.gcc.defs': ROOT / 'PLUTO/Config/Windows.serial.gcc.defs',
        'Config/Windows.mpi8.gcc.defs': ROOT / 'PLUTO/Config/Windows.mpi8.gcc.defs',
        'windows_compat.c': ROOT / 'build/Sedov_mpi8/windows_compat.c',
        'windows_compat.h': ROOT / 'build/Sedov_mpi8/windows_compat.h',
    }
    records = []
    for name, src in sources.items():
        target = dest / 'windows' / name
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, target)
        assert sha(src) == sha(target)
        records.append(dict(file=name, source=str(src), sha256=sha(target), bytes=target.stat().st_size))
    prov = json.loads((REPO / 'results/T005/build_provenance.json').read_text())
    flags = (ROOT / 'PLUTO/Config/Windows.mpi8.gcc.defs').read_text()
    dest.joinpath('windows/README.md').write_text(
        '# Exact T005 Windows build adapter copies\n\n'
        '- Src/HD/makefile: VPATH separators for native mingw32-make.\n'
        '- Config/Windows.serial.gcc.defs: serial GCC build flags.\n'
        '- Config/Windows.mpi8.gcc.defs: MPI GCC flags; mpi8 names the T005 build, not a fixed process count.\n'
        '- windows_compat.c: POSIX random-number compatibility.\n'
        '- windows_compat.h: Windows/SAL compatibility declarations.\n\n'
        f'Compiler: {prov["compiler"]} (MinGW GCC 5.1.0).\n\n'
        f'MS-MPI launcher: {prov["mpi_executable"]}.\n\n'
        'MS-MPI headers/library: D:/sim_tools/pluto_ref/msmpi/include, include/x64, lib/msmpi.lib. SDK and compiler binaries are not included.\n\n'
        f'Actual MPI configuration:\n\n{fence}make\n{flags}{fence}\n\n'
        'Serial has the same optimization/compatibility flags without MPI paths. Local makefile generation follows scripts/win/build_pluto_reference.py. No fluid-physics changes.\n', encoding='utf-8')
    dest.joinpath('.gitattributes').write_text('*.tar.gz -text\nwindows/** -text\n')
    env = os.environ.copy()
    env['PATH'] = str(Path(prov['compiler']).parent) + os.pathsep + str(Path(prov['mpi_executable']).parent) + os.pathsep + env['PATH']
    exe = ROOT / 'build/Sedov_mpi8/pluto.exe'
    proc = subprocess.run([str(exe), '--help'], cwd=out, env=env, capture_output=True, timeout=30)
    help_bytes = proc.stdout + proc.stderr
    (out / 'pluto_help.txt').write_bytes(help_bytes)
    result = dict(archive_sha256=EXPECTED, archive_bytes=SOURCE.stat().st_size, repackaged=False,
                  license_file='PLUTO/COPYING', license_opening=first_paragraph, adapters=records,
                  help_command=[str(exe), '--help'], help_exit=proc.returncode, supports_dec=b'-dec n1' in help_bytes)
    (out / 'manifest.json').write_text(json.dumps(result, indent=2), encoding='utf-8')
    (out / 'sha256.txt').write_text(f'{EXPECTED}  third_party/pluto/{SOURCE.name}\n' +
        ''.join(f'{r["sha256"]}  third_party/pluto/windows/{r["file"]}\n' for r in records))
    # Stock PLUTO terminates its help branch with QUIT_PLUTO(1).
    assert proc.returncode in (0, 1) and result['supports_dec'], result
    print(json.dumps(result, indent=2), flush=True)

if __name__ == '__main__':
    main()
