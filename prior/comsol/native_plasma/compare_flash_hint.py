from pathlib import Path
import json,numpy as np
R=Path(__file__).resolve().parent
a=np.load(R/'results/flash_hint_reference.npz');b=np.load(R/'results/flash_hint_hint.npz')
timings={mode:json.loads((R/'results'/f'flash_hint_{mode}.json').read_text()) for mode in ['reference','hint']}
comparison={k:float(np.max(abs(a[k]-b[k]))/max(np.max(abs(a[k])),1.)) for k in a.files}
report=dict(peak_scaled_field_differences=comparison,timings=timings,scope='Same phase/energy root tolerance, coefficients, and physical states. Saved temperature checked before bracketing. Positivity neighborhood is evaluated only when a negative species exists; same rejection threshold.')
(R/'results/flash_hint_comparison.json').write_text(json.dumps(report,indent=2));print(json.dumps(report),flush=True)
if max(comparison.values())>1e-6:raise RuntimeError('Root implementation difference needs review')
