"""Build reproducible Maxwellian rate tables and native COMSOL chemistry.

SI number-density rates are converted to COMSOL molar rates explicitly.
No S4 observations enter these calculations.
"""
from pathlib import Path
import csv,json,re,shutil,requests
import numpy as np
from scipy.constants import e,m_e,k,h,c,epsilon_0,pi,Avogadro
from scipy.integrate import trapezoid

ROOT=Path(__file__).resolve().parent; DATA=ROOT/'data'
OLD=ROOT.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105/project/data'
APP=Path('C:/Program Files/COMSOL/COMSOL64/Multiphysics/applications/Plasma_Module')
Te=np.geomspace(.015,100,501)
rates={}; reactions=[]; provenance=[]
def add_rate(name,value):
    v=np.asarray(value)+np.zeros_like(Te)
    if not np.all(np.isfinite(v)) or np.any(v<0):raise ValueError(name)
    rates[name]=np.maximum(v,1e-150)
    return f'10^lr_{name}(log10(TeV/1[V]))'
def rx(name,formula,kexpr,kind='Reaction',de=0,source='',order=2):
    reactions.append(dict(name=name,formula=formula,k=kexpr,kind=kind,de=de,source=source,order=order))
def avg(E,S):
    E=np.asarray(E);S=np.asarray(S)
    out=[]
    for t in Te:
        grid=np.unique(np.r_[E,np.geomspace(max(E[E>0].min(),1e-5),max(E.max(),40*t),2400)])
        sig=np.interp(grid,E,S,left=0,right=0)
        out.append(np.sqrt(8*e/(pi*m_e))*trapezoid(sig*grid*np.exp(-grid/t),grid)/t**1.5)
    return np.array(out)
def parse_xs(path):
    lines=path.read_text().splitlines();out=[];i=0
    while i<len(lines):
        kind=lines[i].strip()
        if kind not in ['ELASTIC','EXCITATION','IONIZATION','ATTACHMENT']:i+=1;continue
        formula=lines[i+1].strip();th=float(lines[i+2].split()[0]);i+=3
        while i<len(lines) and not lines[i].startswith('---'):i+=1
        i+=1;xy=[]
        while i<len(lines) and not lines[i].startswith('---'):
            a=lines[i].split()
            if len(a)>=2:xy.append([float(a[0]),float(a[1])])
            i+=1
        xy=np.array(xy);out.append((kind,formula,th,xy[:,0],xy[:,1]));i+=1
    return out

species=[
 ('N2',.0280134,0,1,0,'N2'),('O2',.0319988,0,3,0,'O2'),
 ('N',.0140067,4.88,4,0,'N'),('O',.0159994,2.58,9,0,'O'),
 ('N2_1p',.0280134,15.58,2,1,'N2'),('O2_1p',.0319988,12.06,4,1,'O2'),
 ('N_1p',.0140067,19.414,9,1,'N'),('O_1p',.0159994,16.198,4,1,'O'),
 ('O_1m',.0159994,1.119,4,-1,'O'),('O2_1m',.0319988,-.45,2,-1,'O2'),
 ('Cu',.063546,0,2,0,'UserDefined'),('CuD5',.063546,1.388948,6,0,'UserDefined'),
 ('CuD3',.063546,1.6422256,4,0,'UserDefined'),('CuP1',.063546,3.7858977,2,0,'UserDefined'),
 ('CuP3',.063546,3.816692,4,0,'UserDefined'),('CuH3',.063546,6.1911751,4,0,'UserDefined'),
 ('CuH5',.063546,6.1920252,6,0,'UserDefined'),('Cu_1p',.063546,7.72638,1,1,'UserDefined')]
sp={x[0]:dict(name=x[0],M=x[1],energy_eV=x[2],g=x[3],charge=x[4],preset=x[5]) for x in species}

# Phelps data bundled with the COMSOL dry-air Boltzmann example.
for mol in ['N2','O2']:
    path=APP/'Two-Term_Boltzmann_Equation'/f'{mol}_phelps_xsecs_air.txt'
    shutil.copy2(path,DATA/path.name)
    for j,(kind,formula,de,E,S) in enumerate(parse_xs(path)):
        name=f'{mol}_{j}';expr=add_rate(name,avg(E,S))
        if kind=='ELASTIC':
            rx(name,f'e+{mol}=>e+{mol}',expr,'Elastic',de,'COMSOL/Phelps',2)
            sp[mol]['kmom']=expr
        elif kind=='IONIZATION':rx(name,formula,expr,'Ionization',de,'COMSOL/Phelps',2)
        elif kind=='ATTACHMENT':rx(name,formula,expr,'Attachment',0,'COMSOL/Phelps',2)
        elif mol=='O2' and de in [6.,8.4,9.97]:
            rx(name,'e+O2=>e+2O',expr,'Excitation',de,'Phelps dissociative electronic excitation approximation',2)
        else:
            # Unresolved rovibrational/electronic states: energy goes to gas in
            # this fast-relaxation first closure; no fictitious visible emission.
            rx(name,f'e+{mol}=>e+{mol}',expr,'Excitation',de,'Phelps; unresolved-state fast relaxation',2)

# Winter & Hazi (1982), Tables V and VI, present normalization, A^2 -> m^2.
for name,thr,weight,values in [
 ('CuD5',sp['CuD5']['energy_eV'],1,[.35,.96,.30,.051]),
 ('CuD3',sp['CuD3']['energy_eV'],1,[.25,.71,.25,.037]),
 ('CuP1',sp['CuP1']['energy_eV'],1/3,[2.98,13.7,14.9,5.96,4.19]),
 ('CuP3',sp['CuP3']['energy_eV'],2/3,[2.98,13.7,14.9,5.96,4.19])]:
    Es=[6,10,20,60] if len(values)==4 else [6,10,20,60,100]
    E=np.r_[thr,Es,np.geomspace(Es[-1]*1.1,2000,70)]
    tail=np.array(values[-1])*Es[-1]/E[1+len(Es):]
    S=np.r_[0,np.array(values),tail]*weight*1e-20
    ku=avg(E,S);up=add_rate(name+'_up',ku)
    # Detailed balance computed stably directly from shifted cross sections.
    Ed=np.geomspace(1e-5,2000,2400)
    Sd=2/sp[name]['g']*(Ed+thr)/Ed*np.interp(Ed+thr,E,S,right=0)
    down=add_rate(name+'_down',avg(Ed,Sd))
    rx(name+'up',f'e+Cu=>e+{name}',up,'Excitation',thr,'Winter-Hazi 1982, threshold interpolation',2)
    rx(name+'down',f'e+{name}=>e+Cu',down,'Excitation',-thr,'Microscopic detailed balance',2)

# Obtain all needed radiative decays, including UV resonance losses.
url=(OLD/'Cu_I_380_780.url').read_text().strip().replace('low_w=380','low_w=300').replace('upp_w=780','upp_w=1000')
raw=DATA/'Cu_I_300_1000.tsv'
if not raw.exists():
    q=requests.get(url,timeout=40);q.raise_for_status()
    if 'Aki(s^-1)' not in q.text:raise ValueError('NIST returned no line table')
    raw.write_text(q.text,encoding='utf8')
(DATA/'Cu_I_300_1000.url').write_text(url,encoding='utf8')
def num(s):return float(s.strip('=" ').replace('?',''))
lines=[];bound=[s for s in sp.values() if s['name'].startswith('Cu') and s['charge']==0]
for row in csv.DictReader(raw.read_text().splitlines(),delimiter='\t'):
    try:A=num(row['Aki(s^-1)']);lo=num(row['Ei(eV)']);hi=num(row['Ek(eV)'])
    except (ValueError,KeyError,AttributeError):continue
    lower=next((s for s in bound if abs(s['energy_eV']-lo)<2e-5),None)
    upper=next((s for s in bound if abs(s['energy_eV']-hi)<2e-5),None)
    if not lower or not upper:continue
    lam=num(row['ritz_wl_air(nm)'] or row['obs_wl_air(nm)'])
    f=1.4991938e-16*(lam*10)**2*A*upper['g']/lower['g']
    lines.append(dict(lower=lower['name'],upper=upper['name'],A=A,lambda_nm=lam,delta_eV=hi-lo,f=f,accuracy=row['Acc']))
    rx('rad'+str(len(lines)),upper['name']+'=>'+lower['name'],f'{A}[1/s]*beta{len(lines)}','Reaction',0,'NIST ASD; local escape probability',1)
    # Visible allowed transition excitation: van Regemorter effective collision strength.
    if lower['name']!='Cu':
        delta=hi-lo;ups=14.5*lower['g']*f*(13.605693/delta)**2*.2
        ku=8.629e-12*ups/(lower['g']*np.sqrt(Te*e/k))*np.exp(-delta/Te)
        kd=8.629e-12*ups/(upper['g']*np.sqrt(Te*e/k))
        rx('lineup'+str(len(lines)),f'e+{lower["name"]}=>e+{upper["name"]}',add_rate('lineup'+str(len(lines)),ku),'Excitation',delta,'van Regemorter 1962, gbar=0.2',2)
        rx('linedn'+str(len(lines)),f'e+{upper["name"]}=>e+{lower["name"]}',add_rate('linedn'+str(len(lines)),kd),'Excitation',-delta,'Detailed balance',2)

# Lotz valence-electron ionization; reverse three-body recombination.
for name,I,qocc in [('Cu',7.72638,1),('N',14.534,3),('O',13.618,4)]+[(s['name'],7.72638-s['energy_eV'],1) for s in bound if s['name']!='Cu']:
    E=np.geomspace(I,2000,2500);sigma=4.5e-18*qocc*np.log(E/I)/(E*I)
    ki=avg(E,sigma);expr=add_rate(name+'_ion',ki)
    ion='Cu+' if name.startswith('Cu') else name+'+'
    gi=sp['Cu_1p' if name.startswith('Cu') else name+'_1p']['g'];gb=sp[name]['g']
    # Shifted integral removes the exp(-I/T) factor before detailed balance.
    z=np.geomspace(1e-6,2000,2500)
    sig=4.5e-18*qocc*np.log((z+I)/I)/((z+I)*I)
    kr=[]
    for t in Te:
        scaled=np.sqrt(8*e/(pi*m_e))*trapezoid(sig*(z+I)*np.exp(-z/t),z)/t**1.5
        kr.append(scaled/((2*gi/gb)*(2*pi*m_e*e*t/h**2)**1.5))
    rx(name+'ion',f'e+{name}=>2e+{ion}',expr,'Ionization',I,'Lotz 1967/1969 valence approximation',2)
    rx(name+'3rec',f'2e+{ion}=>e+{name}',add_rate(name+'_3rec',kr),'Excitation',-I,'Detailed balance with Lotz',3)
    # Hydrogenic Kramers photoionization + Milne detailed balance, per level.
    from numpy.polynomial.laguerre import laggauss
    xx,ww=laggauss(64);sig0=6.3e-22*np.sqrt(13.605693/I);nu0=I*e/h
    alpha=[]
    for t in Te:
        nu=nu0+t*e/h*xx
        alpha.append(gb/(2*gi)*(h*h/(2*pi*m_e*e*t))**1.5*8*pi/c**2*(t*e/h)*np.sum(ww*nu**2*sig0*(nu0/nu)**3))
    rx(name+'rr',f'e+{ion}=>{name}',add_rate(name+'_rr',alpha),'Reaction',I,'Hydrogenic Kramers-Milne approximation, not measured Cu rate',2)

# Air loss channels: Kossyi et al. 1992, minimal subset.
rx('recN2','e+N2+=>2N','2.8e-13[m^3/s]*(300[K]/TeK)^0.5','Reaction',0,'Kossyi 1992',2)
rx('recO2','e+O2+=>2O','2e-13[m^3/s]*(300[K]/TeK)^0.7','Reaction',0,'Kossyi 1992',2)
rx('att3','e+O2+O2=>O2-+O2','1.4e-41[m^6/s]*(300[K]/TeK)*exp(-600[K]/Tg+700[K]*(TeK-Tg)/(TeK*Tg))','Reaction',0,'Kossyi 1992',3)
rx('detach','O-+O=>O2+e','3e-16[m^3/s]','Reaction',0,'COMSOL O chemistry',2)
rx('detach2','O2-+O=>O2+O+e','3.3e-16[m^3/s]','Reaction',0,'Kossyi 1992',2)
for ion,neutral in [('Cu+','Cu'),('N2+','N2'),('O2+','O2'),('N+','N'),('O+','O')]:
    for neg,nneutral in [('O-','O'),('O2-','O2')]:
        rx('neutral'+str(len(reactions)),f'{ion}+{neg}=>{neutral}+{nneutral}','1.5e-13[m^3/s]','Reaction',0,'COMSOL oxygen ion-ion recombination approximation',2)
# Thermal dissociation, number-density rates converted from Park molar coefficients.
for mol,atom,D,gatom,gMol,thetaR,thetaV,A,power,Tact in [
 ('N2','N',9.76,4,1,2.88,3390,7e15,-1.6,113200),('O2','O',5.16,9,3,2.07,2270,2e15,-1.5,59500)]:
    mu=sp[atom]['M']/Avogadro/2
    K=f'((2*pi*{mu}[kg]*k_B_const*Tg/h_const^2)^1.5*{gatom*gatom}/{gMol}/(Tg/(2*{thetaR}[K]))*(1-exp(-{thetaV}[K]/Tg))*exp(-{D}[eV]/(k_B_const*Tg)))'
    kd=f'{A}[m^3/(mol*s)]*(Tg/1[K])^({power})*exp(-{Tact}[K]/Tg)/N_A_const'
    for collider in ['N2','O2','Cu']:
        rx(mol+'d'+collider,f'{mol}+{collider}=>2{atom}+{collider}',kd,'Reaction',0,'Park thermal dissociation',2)
        # Stable exponent cancellation in reverse expression.
        rev=f'{A}[m^3/(mol*s)]*(Tg/1[K])^({power})*exp((-{Tact}[K]+{D}[eV]/k_B_const)/Tg)/N_A_const/((2*pi*{mu}[kg]*k_B_const*Tg/h_const^2)^1.5*{gatom*gatom}/{gMol}/(Tg/(2*{thetaR}[K]))*(1-exp(-{thetaV}[K]/Tg)))'
        rx(mol+'r'+collider,f'2{atom}+{collider}=>{mol}+{collider}',rev,'Reaction',0,'Thermal detailed balance; ground-state partition functions',3)

columns=list(rates)
np.savetxt(DATA/'log_rates.txt',np.column_stack([np.log10(Te),*[np.log10(rates[k]) for k in columns]]),fmt='%.12e')
(DATA/'chemistry.json').write_text(json.dumps({'species':list(sp.values()),'reactions':reactions,'lines':lines,'rate_columns':columns,'Te_eV_range':[float(Te.min()),float(Te.max())]},indent=2),encoding='utf8')
J=['import com.comsol.model.*;','public class Chemistry {','public static final String[] NAMES=new String[]{'+','.join('"'+n+'"' for n in sp)+'};','public static void apply(Model m,String path){',
'm.func().create("rates","Interpolation");m.func("rates").set("source","file");m.func("rates").set("filename",path+"/log_rates.txt");m.func("rates").set("nargs",1);',
'm.func("rates").set("funcs",new String[][]{'+','.join('{"lr_'+n+'","'+str(i+1)+'"}' for i,n in enumerate(columns))+'});m.func("rates").set("interp","piecewisecubic");m.func("rates").set("extrap","const");m.func("rates").importData();']
def java_str(s):return json.dumps(s)
for a in reactions:
    tag=a['name'];kind=a['kind'];isEI=kind!='Reaction'
    J.append(f'm.component("c").physics("plas").create("{tag}","'+('ElectronImpactReaction' if isEI else 'Reaction')+'",2);')
    pref=f'm.component("c").physics("plas").feature("{tag}")'
    J.append(pref+'.set("formula",'+java_str(a['formula'])+');')
    kk=a['k']
    if kk.startswith('10^'):kk+='*1[m^'+str(3*(a['order']-1))+'/s]' if a['order']>1 else '*1[1/s]'
    if a['order']>1:kk+='*N_A_const'+('^2' if a['order']==3 else '')
    J.append(pref+'.set("kf",'+java_str(kk)+');')
    if isEI:
        J.append(pref+f'.set("type","{kind}");')
        J.append(pref+'.set("'+('mratio' if kind=='Elastic' else 'de')+'",'+java_str(str(a['de']))+');')
for name,a in sp.items():
    pref=f'm.component("c").physics("plas").feature("{name}")'
    J.extend([pref+'.set("PresetSpeciesData",'+java_str(a['preset'])+');',pref+'.set("M",'+java_str(str(a['M'])+'[kg/mol]')+');',pref+'.set("x0",'+java_str('.79' if name=='N2' else '.21' if name=='O2' else '1e-24')+');',pref+'.set("Dfs","Dgas");'])
    if a['charge']:
        J.extend([pref+'.set("MobilityDiffusivitySpecification","SpecifyMobilityComputeDiffusivity");',pref+'.set("MobilitySpecification","SpecifyConstantMobility");',pref+'.set("um","e_const*Dgas/(k_B_const*Tg)");'])
    if name.startswith('Cu'):
        J.extend([pref+'.set("sigma","3[angstrom]");',pref+'.set("epsilonkb","100[K]");'])
J+=['m.component("c").physics("plas").feature("N2").set("FromMassConstraint",true);','m.component("c").physics("plas").feature("N2_1p").set("InitIon",true);','}}']
(ROOT/'src/Chemistry.java').write_text('\n'.join(J),encoding='utf8')
sources={
'Winter_Hazi_1982':{'doi':'10.2172/5526164','url':'https://www.osti.gov/servlets/purl/5526164','used':'Tables V/VI present normalization; fine-structure 2P branching by statistical weights; linear threshold extrapolation, inverse-energy tail'},
'Lotz_1967':{'doi':'10.1086/190154','used':'valence-only semiempirical ionization; not a complete subshell model'},
'Lotz_Cu_1969':{'doi':'10.1007/BF01394789'},
'van_Regemorter':{'doi':'10.1086/147445','used':'allowed excited-state transitions; fixed Gaunt factor 0.2'},
'NIST_ASD':{'url':url,'used':'bound energies, degeneracies, A values'},
'Phelps':{'path':str(APP/'Two-Term_Boltzmann_Equation'),'used':'bundled dry-air cross sections, Maxwellian integration'},
'approximations':['Kramers-Milne hydrogenic radiative recombination, not measured Cu data','unresolved molecular excitation is treated as fast gas heating','local escape probability with physical w0 escape length','ground-state molecular partitions for thermal detailed balance','Fick diffusion and hard-sphere transport closure']}
(DATA/'sources_and_assumptions.json').write_text(json.dumps(sources,indent=2),encoding='utf8')
print('SPECIES',len(sp),'REACTIONS',len(reactions),'RATES',len(columns),'LINES',len(lines))
print('VISIBLE',[(a['lambda_nm'],a['upper'],a['lower']) for a in lines if 475<a['lambda_nm']<525])
