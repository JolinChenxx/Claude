"""Capture a stable, completed live checkpoint without writing to its source."""
import datetime
import hashlib
import json
import pathlib
import struct
import subprocess

SOURCE = pathlib.Path('D:/sim_runs/T016r/low_trap_G0/it0')
ROOT = pathlib.Path('D:/sim_runs/T016s')
OUT = pathlib.Path(__file__).resolve().parent
EXE = pathlib.Path('D:/sim_tools/pluto_lip/build_v1d/mpi/pluto.exe')
EXPECTED_EXE = '0f9e454c13685412da7fce91c104789caf76f002e7685dd17e6fcd8d088481ca'


def sha(data):
    return hashlib.sha256(data).hexdigest()


def main():
    assert not ROOT.exists(), 'Refusing to reuse an existing T016s directory'
    assert sha(EXE.read_bytes()) == EXPECTED_EXE
    for attempt in range(1, 4):
        dbl = (SOURCE / 'dbl.out').read_bytes()
        restart = (SOURCE / 'restart.out').read_bytes()
        if not dbl.endswith(b'\n') or len(restart) % 128:
            continue
        row = dbl.decode('ascii').splitlines()[-1]
        fields = row.split()
        index, t_ns, step = int(fields[0]), float(fields[1]), int(fields[3])
        frame = f'data.{index:04d}.dbl'
        names = ['pluto.ini', 'grid.out', 'restart.out', 'dbl.out', frame]
        names += [p.relative_to(SOURCE).as_posix()
                  for p in sorted((SOURCE / 'lip_tables').rglob('*')) if p.is_file()]
        snapshot = {name: (SOURCE / name).read_bytes() for name in names}
        if snapshot['dbl.out'] != dbl or snapshot['restart.out'] != restart:
            continue
        if any((SOURCE / name).read_bytes() != data for name, data in snapshot.items()):
            continue
        matching = []
        for offset in range(0, len(restart), 128):
            values = struct.unpack_from('<17i4xdd40s', restart, offset)
            if values[1] == index and values[0] == step and abs(values[17] - t_ns) < 0.0001:
                matching.append(dict(record=offset // 128, step=values[0],
                                     time_ns=values[17], dt=values[18]))
        assert len(matching) == 1, 'Selected dbl has no unique matching restart record'
        assert len(snapshot[frame]) == 140 * 210 * 9 * 8, 'Incomplete G0 frame'
        break
    else:
        raise RuntimeError('Could not capture a stable complete live checkpoint')
    original_ini = snapshot['pluto.ini']
    assert b'rmax_par' not in original_ini
    newline = b'\r\n' if b'\r\n' in original_ini else b'\n'
    marker = b'[Time]' + newline
    assert original_ini.count(marker) == 1
    changed_ini = original_ini.replace(marker, marker + b'rmax_par 1000' + newline, 1)
    assert changed_ini.replace(b'rmax_par 1000' + newline, b'', 1) == original_ini
    manifest = dict(card='T016s', captured=datetime.datetime.now().astimezone().isoformat(),
                    source=str(SOURCE), source_commit='e35d65c',
                    preparation_commit=subprocess.check_output(['git', 'rev-parse', 'HEAD'], text=True).strip(),
                    executable=str(EXE), executable_sha256=EXPECTED_EXE,
                    restart=index, restart_time_ns=matching[0]['time_ns'],
                    restart_record=matching[0], output_record=row,
                    checkpoint_bytes=len(snapshot[frame]), stable_read_attempt=attempt,
                    source_hashes={name: sha(data) for name, data in snapshot.items()}, copies=[])
    ROOT.mkdir()
    for member in ['A', 'B']:
        dest = ROOT / member
        dest.mkdir()
        hashes = {}
        for name, data in snapshot.items():
            if member == 'B' and name == 'pluto.ini':
                data = changed_ini
            path = dest / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(data)
            assert path.read_bytes() == data
            hashes[name] = sha(data)
        (OUT / member).mkdir(exist_ok=True)
        (OUT / member / 'initial_pluto.ini').write_bytes((dest / 'pluto.ini').read_bytes())
        manifest['copies'].append(dict(member=member, directory=str(dest),
                                       rmax_par=100 if member == 'A' else 1000, hashes=hashes))
    payload = json.dumps(manifest, indent=2) + '\n'
    (ROOT / 'preflight.json').write_text(payload, encoding='utf-8')
    (OUT / 'preflight.json').write_text(payload, encoding='utf-8')
    print(payload)


if __name__ == '__main__':
    main()
