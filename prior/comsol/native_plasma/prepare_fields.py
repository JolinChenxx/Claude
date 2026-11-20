from pathlib import Path
import json
R=Path(__file__).resolve().parent
d=json.loads((R/'data/chemistry.json').read_text());sp=d['species'];lines=d['lines'];rxs=d['reactions']
J=['import com.comsol.model.*;','public class GasFields {','static Model m;','static void g(String n,String x){m.component("c").variable("gasv").set(n,x);}','static void b(String n,String x){m.component("c").variable("boundv").set(n,x);}','public static void apply(Model model){m=model;']
def var(group,n,s):J.append(f'{group}({json.dumps(n)},{json.dumps(s)});')
def g(n,s):var('g',n,s)
def b(n,s):var('b',n,s)
def numberk(a):
    s=a['k']
    if s.startswith('10^'):s+='*1[m^'+str(3*(a['order']-1))+'/s]' if a['order']>1 else '*1[1/s]'
    return s
g('rhoP','rho0*qR');g('rhoSafe','max(rhoP,1e-8[kg/m^3])');g('urP','u0*qMr/max(qR,1e-8)');g('uzP','u0*qMz/max(qR,1e-8)')
g('Tgas','max(Tg,150[K])');g('TeV','max(plas.Te,0.015[V])');g('TeK','plas.Te*e_const/k_B_const');g('Ue','e_const*plas.en');g('pe','(2/3)*Ue')
g('divU','d(urP,r)+urP/max(r,1e-12[m])+d(uzP,z)')
for s in sp:g('n'+s['name'],'plas.n_w'+s['name'])
g('nHeavy','+'.join('n'+s['name'] for s in sp));g('nCuTotal','+'.join('n'+s['name'] for s in sp if s['name'].startswith('Cu')))
g('rhoCopper','nCuTotal*Mcu/N_A_const');g('Ycu','rhoCopper/rhoSafe');g('CvVol','k_B_const*('+ '+'.join(('2.5' if s['preset'] in ['N2','O2'] else '1.5')+'*n'+s['name'] for s in sp)+')')
g('Uchem','Bcu*rhoCopper+e_const*('+ '+'.join(str(s['energy_eV'])+'[V]*n'+s['name'] for s in sp if s['energy_eV'])+')')
g('pHeavy','nHeavy*k_B_const*Tgas');g('pPl','pHeavy+pe');g('cs','sqrt(1.667*pPl/rhoSafe)');g('speedP','sqrt(urP^2+uzP^2+1e-6[m^2/s^2])')
g('Dnum','nuFactor*h*(speedP+cs)');g('Dgas','1.8e-5[m^2/s]*(Tgas/300[K])^1.75*rhoAmb/rhoSafe')
g('muGas','1.8e-5[Pa*s]*(Tgas/300[K])^0.7+rhoSafe*Dnum');g('kGas','0.026[W/(m*K)]*(Tgas/300[K])^0.7+rhoSafe*Dnum*(CvVol/rhoSafe+nHeavy*k_B_const/rhoSafe)/0.7')
g('tauRR','2*muGas*d(urP,r)-2/3*muGas*divU');g('tauZZ','2*muGas*d(uzP,z)-2/3*muGas*divU');g('tauRZ','muGas*(d(urP,z)+d(uzP,r))');g('tauPP','2*muGas*urP/max(r,1e-12[m])-2/3*muGas*divU')
kn2=next(s['kmom'] for s in sp if s['name']=='N2');ko2=next(s['kmom'] for s in sp if s['name']=='O2')
g('kmN2',kn2+'*1[m^3/s]');g('kmO2',ko2+'*1[m^3/s]');g('kmCu','2e-19[m^2]*sqrt(8*e_const*TeV/(pi*me_const))')
g('nuEN','nN2*kmN2+nO2*kmO2+(nHeavy-nN2-nO2)*kmCu')
g('niZ2','+'.join('n'+s['name'] for s in sp if s['charge']))
g('coulombLog','max(2,min(15,23-log(sqrt(max(plas.ne/1e6[1/m^3],1))*(TeV/1[V])^(-1.5))))')
g('nuEI','2.91e-12[m^3/s]*niZ2*coulombLog/(TeV/1[V])^1.5');g('nuCollision','max(nuEN+nuEI,1e5[1/s])')
g('alphaLaser','plas.ne*e_const^2/(epsilon0_const*me_const*c_const)*nuCollision/((2*pi*c_const/lambdaL)^2+nuCollision^2)')
g('Qei','3*me_const/(Mcu/N_A_const)*plas.ne*k_B_const*(TeK-Tgas)*nuEI')
hd={a:[] for a in ['r','z']};jd={a:[] for a in ['r','z']}
for s in sp:
    n=s['name'];M=s['M'];cph=3.5 if s['preset'] in ['N2','O2'] else 2.5
    hi=f'({cph}*R_const*Tgas/{M}[kg/mol]+{s["energy_eV"]}[V]*F_const/{M}[kg/mol]'+('+Bcu)' if n.startswith('Cu') else ')')
    g('h_'+n,hi)
    for a in ['r','z']:
        jf=f'(plas.tflux{a}_w{n}-plas.cflux{a}_w{n})'
        hd[a].append(f'h_{n}*{jf}')
        if s['charge']:jd[a].append(f'{s["charge"]}*F_const/{M}[kg/mol]*{jf}')
for a in ['r','z']:
    g('Hdiff'+a,'+'.join(hd[a]));g('Jtotal'+a,'+'.join(jd[a])+f'-e_const*plas.gflux_ne{a}+plas.scharge*u'+('rP' if a=='r' else 'zP'))
g('Qfield','Jtotalr*plas.Etr+Jtotalz*plas.Etz')
rad=[];vis=[];rr=[];qe=[];fbvis=[]
for i,a in enumerate(lines,1):
    lo=a['lower'];up=a['upper'];lam=a['lambda_nm'];f=a['f'];delta=a['delta_eV'];A=a['A'];gl=next(s['g'] for s in sp if s['name']==lo);gu=next(s['g'] for s in sp if s['name']==up)
    g(f'dnu{i}',f'1/({lam}[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))')
    g(f'sigline{i}',f'e_const^2/(4*epsilon0_const*me_const*c_const)*{f}/(sqrt(pi)*dnu{i})')
    g(f'tauLine{i}',f'sigline{i}*max(n{lo}-{gl/gu}*n{up},0[1/m^3])*w0')
    g(f'beta{i}',f'if(tauLine{i}<1e-3,1-tauLine{i}/2+tauLine{i}^2/6,(1-exp(-tauLine{i}))/tauLine{i})')
    g(f'jline{i}',f'{A}[1/s]*n{up}*{delta}[V]*e_const/(4*pi)')
    rad.append(f'4*pi*jline{i}*beta{i}')
    if 475<lam<525:vis.append(f'jline{i}')
for a in rxs:
    if not a['name'].endswith('rr'):continue
    s=a['name'][:-2];ion='Cu_1p' if s.startswith('Cu') else s+'_1p'
    expr=f'({numberk(a)})*plas.ne*n{ion}'
    g('r_'+a['name'],expr)
    g('Eph_'+a['name'],f'10^rrMean_{a["name"]}(log10(TeV/1[V]))*1[V]')
    g('Qfb_'+s,f'r_{a["name"]}*e_const*Eph_{a["name"]}')
    rr.append('Qfb_'+s);qe.append(f'r_{a["name"]}*e_const*(Eph_{a["name"]}-{a["de"]}[V])')
    if a['de']<1239.841984/475:
        I=a['de'];band=f'(exp(-max(h_const*c_const/(525[nm]*e_const)-{I}[V],0[V])/TeV)-exp(-max(h_const*c_const/(475[nm]*e_const)-{I}[V],0[V])/TeV))'
        fbvis.append(f'Qfb_{s}*{band}/(4*pi)')
g('Qff','1.426e-40[W*m^3/K^0.5]*sqrt(TeK)*plas.ne*niZ2')
g('Qrad','+'.join(rad+rr+['Qff']))
# Frequency-integrated Maxwellian free-free spectrum, constant Gaunt factor.
g('ffVisibleFraction','exp(-h_const*c_const/(525[nm]*e_const*TeV))-exp(-h_const*c_const/(475[nm]*e_const*TeV))')
g('jfbVisible','+'.join(fbvis));g('jVisible','+'.join(vis)+'+Qff*ffVisibleFraction/(4*pi)+jfbVisible');g('QeRR','+'.join(qe))
attach=[]
for a in rxs:
    if a['kind']=='Attachment':attach.append(f'({numberk(a)})*plas.ne*nO2')
    if a['name']=='att3':attach.append(f'({numberk(a)})*plas.ne*nO2^2')
    if a['name']=='recN2':attach.append(f'({numberk(a)})*plas.ne*nN2_1p')
    if a['name']=='recO2':attach.append(f'({numberk(a)})*plas.ne*nO2_1p')
g('QeAttach','1.5*e_const*TeV*('+'+'.join(attach)+')')

# Surface balances. n_c points from condensed copper into gas.
b('ncr','if(nz>=0,nr,-nr)');b('ncz','abs(nz)');b('kappa','dtang(ncr,r)+dtang(ncz,z)+ncr/max(r,1e-12[m])')
b('sigmaCu','max(0.05[N/m],1.257[N/m]-0.000192[N/(m*K)]*(T-1356[K]))')
b('psatCu','pAmb*exp((Mcu/N_A_const)*Lv/k_B_const*(1/Tv-1/max(T,150[K])))')
b('mdotEvap','accom*psatCu*sqrt((Mcu/N_A_const)/(2*pi*k_B_const*max(T,150[K])))')
b('vKn','sqrt(k_B_const*max(T,150[K])/(Mcu/N_A_const))')
ret=[];returnenergy=[];charge=[];feeds={s['name']:'0[kg/(m^2*s)]' for s in sp}
for s in sp:
    n=s['name'];M=s['M'];z=s['charge']
    if n.startswith('Cu') or z:
        gamma=f'n{n}*sqrt(k_B_const*Tgas/(2*pi*{M}[kg/mol]/N_A_const))'
        if z:gamma+=f'+n{n}*max(-{z}*e_const*Dgas/(k_B_const*Tgas)*(plas.Etr*ncr+plas.Etz*ncz),0[m/s])'
        b('Gwall_'+n,gamma)
        if z:charge.append(f'{z}*Gwall_{n}')
        if n.startswith('Cu'):
            ret.append(f'Gwall_{n}*Mcu/N_A_const');returnenergy.append(f'Gwall_{n}*Mcu/N_A_const*(h_{n}+0.5*(urP^2+uzP^2))')
            feeds[n]=f'-Gwall_{n}*Mcu/N_A_const'
        else:
            neutral=n.split('_')[0];feeds[n]=f'-Gwall_{n}*{M}[kg/mol]/N_A_const';feeds[neutral]+=f'+Gwall_{n}*{M}[kg/mol]/N_A_const'
feeds['Cu']='mdotEvap'+feeds['Cu']
b('mdotReturn','+'.join(ret));b('mdot','mdotEvap-mdotReturn');b('recoil','mdotEvap*vKn')
b('vnSurf','meltOn*(urCu*ncr+uzCu*ncz-mdot/rhoCu)')
b('Qmass','mdotEvap*(Bcu+2.5*R_const*T/Mcu+0.5*((urCu+vKn*ncr)^2+(uzCu+vKn*ncz)^2))-('+'+'.join(returnenergy)+')')
b('hAir','k_B_const/4*(3.5*nN2*sqrt(8*k_B_const*Tgas/(pi*0.0280134[kg/mol]/N_A_const))+3.5*nO2*sqrt(8*k_B_const*Tgas/(pi*0.0319988[kg/mol]/N_A_const))+2.5*nN*sqrt(8*k_B_const*Tgas/(pi*0.0140067[kg/mol]/N_A_const))+2.5*nO*sqrt(8*k_B_const*Tgas/(pi*0.0159994[kg/mol]/N_A_const)))')
b('Qexchange','accom*hAir*(Tg-T)');b('Qsurface','Acu*Ibeam*optD*ncz')
b('Gtherm','plas.thflux')
b('GeIn','-plas.ne_bnd-plas.nemig_bnd')
b('Iwall','e_const*('+'+'.join(charge)+'-GeIn+Gtherm)')
b('QelectronWall','e_const*(plas.en_bnd+plas.ten_bnd+plas.enmig_bnd)')
b('QworkWall','e_const*workCu*(Gtherm-GeIn)');b('Qthermion','QelectronWall+QworkWall')
names=[s['name'] for s in sp if s['name']!='N2']
flux=[feeds[n]+f'+plas.rho_w{n}*vnSurf' for n in names]
J.append('m.component("c").physics("plas").feature("feed").set("specNames",new String[]{'+','.join(json.dumps(x) for x in names)+'});')
J.append('m.component("c").physics("plas").feature("feed").set("influx",new String[]{'+','.join(json.dumps(x) for x in flux)+'});')
J+=['}}'];(R/'src/GasFields.java').write_text('\n'.join(J),encoding='utf8')
print('GasFields generated:',len(J),'statements')
