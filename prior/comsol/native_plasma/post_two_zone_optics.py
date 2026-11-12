from pathlib import Path
import sys,subprocess
R=Path(__file__).resolve().parent
case,*history=sys.argv[1:]
subprocess.run([sys.executable,'-X','utf8',str(R/'post_two_zone.py'),case,*history],check=True)
subprocess.run([sys.executable,'-X','utf8',str(R/'render_two_zone.py'),*history,case],check=True)
chain='-'.join([*history,case])
if (R/'results'/f'{chain}_global_images.npz').exists():
    subprocess.run([sys.executable,'-X','utf8',str(R/'calibrate_global_camera.py'),chain],check=True)
