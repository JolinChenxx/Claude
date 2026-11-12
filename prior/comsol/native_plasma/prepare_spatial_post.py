"""Adapt the existing layered camera to general native-cell labels and metadata."""
from pathlib import Path
R=Path(__file__).resolve().parent
src=(R/'src/ExportAxialPlume.java').read_text()
src=src.replace('public class ExportAxialPlume {','public class ExportSpatialGrid {\n static String suffix(int j){String s="";for(int k=j+1;k>0;k=(k-1)/26)s=(char)(65+(k-1)%26)+s;return s;}')
src=src.replace('"cell"+(char)(\'A\'+j),p="plasma"+(char)(\'A\'+j)','"cell"+suffix(j),p="plasma"+suffix(j)')
src=src.replace('"energyF"+n','"energyF24"')
src=src.replace('m.result().numerical().create("axialHistory"', 'for(String q:new String[]{"c.intCu(rhoCu*c.hCond)","c.intSurf(c.Qsurface)","c.intSurf(c.Qmass)","cellA.targetLaserEnergy*energyScale","cameraZero/1[ns]"})ex.add(q);\n  m.result().numerical().create("axialHistory"')
(R/'src/ExportSpatialGrid.java').write_text(src)
s=(R/'post_grid_plume.py').read_text()
s=s.replace('def initialize(', 'def suffix(j):\n    s=""\n    while j>=0:\n        s=chr(65+j%26)+s;j=j//26-1\n    return s\n\ndef initialize(')
s=s.replace('chr(65+j)','suffix(j)')
# Restore the label function body after the global replacement.
s=s.replace('s=suffix(j%26)+s','s=chr(65+j%26)+s')
s=s.replace("ns=d['time_s']*1e9", "ns=d['time_s']*1e9;offset=float(d.get('cameraZero/1[ns]',np.array([90.]))[0])")
s=s.replace('ns-q-118','ns-q-offset').replace('times+113','times+offset-5').replace('times+123','times+offset+5').replace('times+118','times+offset')
s=s.replace("model_kind='coarse axisymmetric FV native Plasma; 4r x6z actual transport; one-way H04 thermal predictor'", "model_kind=f'axisymmetric FV native Plasma; {nr}r x{nz}z; computed thermal/laser feedback, fixed surface',camera_zero_model_ns=offset,spatial_geometry=f'{nr}r x{nz}z axisymmetric finite volumes'")
s=s.replace("'Very coarse4r x6z Euler FV; no spatial convergence claim','H04 source placed within r11um; radial source footprint unresolved','No plasma-to-target feedback, moving liquid or sheath'", "'First-order FV transport; spatial convergence not yet checked','Copper heat PDE and dynamic laser shielding, fixed surface','No moving liquid or explicit sheath; direct conductive wall heat not yet included'")
s=s.replace("subprocess.run([sys.executable,'-X','utf8',str(R/'calibrate_global_camera.py'),case]", "subprocess.run([sys.executable,'-X','utf8',str(R/'calibrate_global_camera.py'),case]")
(R/'post_spatial_grid.py').write_text(s)
print('General grid export and camera written')
