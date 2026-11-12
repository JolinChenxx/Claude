from pathlib import Path
import sys,json,numpy as np
R=Path(__file__).resolve().parent;worker=sys.argv[1];p=R/'results'/worker
a=np.loadtxt(p/'state.txt');n=a[:,7:];z=np.array([0,0,0,0,1,1,1,1,-1,-1,0,0,0,0,0,0,0,1,2,3,4]);q=n@z-a[:,4]
den=np.maximum(abs(n)@abs(z)+abs(a[:,4]),1.)
te=2/3*a[:,5]/np.maximum(a[:,4],1e-100)
indices=sorted(set([int(np.argmax(abs(q)/den)),int(np.argmax(te)),int(np.argmin(a[:,2])),int(np.argmin(a[:,3])),int(np.argmax(a[:,1]))]))
report=dict(worker=worker,request=(p/'request.properties').read_text(),T_range=[float(a[:,2].min()),float(a[:,2].max())],Te_eV_range=[float(te.min()),float(te.max())],rho_range=[float(a[:,1].min()),float(a[:,1].max())],cv_range=[float(a[:,3].min()),float(a[:,3].max())],max_local_charge_fraction=float(np.max(abs(q)/den)),negative_entries=int((a[:,1:]<0).sum()),rows=[dict(index=i,T=a[i,2],Te_eV=te[i],ne=a[i,4],cv=a[i,3],rho=a[i,1],charge_fraction=q[i]/den[i]) for i in indices])
(R/'results'/f'{worker}_failed_input_audit.json').write_text(json.dumps(report,indent=2));print(json.dumps(report),flush=True)
