"""Generate an isolated spatial FV/native-Plasma thermal-feedback comparator.
The production spatial ALE branch remains separate. This comparator removes the
old prescribed shape and 10/90 optical split; it is not a sheath calculation.
"""
from pathlib import Path
R=Path(__file__).resolve().parent
base=(R/'src/PrepareGridPlume.java').read_text()
base=base.replace('public class PrepareGridPlume','public class BuildSpatialGridThermal')
base=base.replace('static int nr=4,nz=6','static int nr=8,nz=10')
base=base.replace('static double[] re={0,11e-6,45e-6,130e-6,400e-6},ze={0,9e-6,35e-6,90e-6,190e-6,350e-6,650e-6};','static double[] re={0,4e-6,8e-6,11e-6,25e-6,60e-6,150e-6,400e-6,1200e-6},ze={0,1e-6,4e-6,12e-6,35e-6,90e-6,190e-6,350e-6,650e-6,1100e-6,1800e-6};')
base=base.replace('static String c(int j){return "cell"+(char)(\'A\'+j);}static String p(int j){return "plasma"+(char)(\'A\'+j);}', 'static String suffix(int j){String s="";for(int k=j+1;k>0;k=(k-1)/26)s=(char)(65+(k-1)%26)+s;return s;} static String c(int j){return "cell"+suffix(j);}static String p(int j){return "plasma"+suffix(j);}')
base=base.replace('"gasv"+(char)(\'A\'+j)','"gasv"+suffix(j)').replace('String sf=""+(char)(\'A\'+j)','String sf=suffix(j)')
needle='sp.addAll(Arrays.asList(Chemistry.NAMES));'
setup='''
  m.component().remove("outer");m.component("c").physics().remove("plas");m.component("c").physics().remove("bulk");
  m.variable().create("axialFlux");m.param().set("tp","120[ns]");m.param().set("cameraZero","90[ns]");
  g("axialIncident","Ein/(tau*sqrt(pi/(4*log(2))))*exp(-4*log(2)*((t-tp)/tau)^2)");
  g("targetMassSource","c.intSurf(c.mdot)");g("targetPowerSource","c.intSurf(c.Qmass-c.Qexchange)");g("targetElectronSource","c.intSurf(c.electronFeed)");
  m.component("c").variable("gasv").set("pulse","exp(-4*log(2)*((t-tp)/tau)^2)");
  m.component("c").variable("gasv").set("Ipeak","2*Ein/(pi*w0^2)*sqrt(4*log(2)/pi)/tau");
  for(int j=0;j<nr*nz;j++){
   String sf=suffix(j);AddAxialComponent.apply(m,c(j),sf);m.component(c(j)).physics().remove("bulk"+sf);
   v(j,"logGasT","logT"+sf);v(j,"gasEnergy","energyGas"+sf);v(j,"Etot","gasEnergy*energyScale/Vp");v(j,"volumeRate","0[1/s]");v(j,"Vdot","0[m^3/s]");v(j,"Msweep","0[kg/s]");
   v(j,"sound","sqrt((1+pPl/(CvVol*Tg+Ue))*pPl/rhoP)");v(j,"laserTau","alphaLaser*dz");v(j,"beamTransmission","exp(-laserTau)");v(j,"beamAbsorptance","if(laserTau<1e-4,laserTau*(1-laserTau/2+laserTau^2/6),1-exp(-laserTau))");
   v(j,"Plaser","(incomingForward+incomingBack)*beamAbsorptance");v(j,"meanLaserIntensity","(incomingForward+incomingBack)*dz/Vp*if(laserTau<1e-4,1-laserTau/2+laserTau^2/6,beamAbsorptance/laserTau)");
   m.component(c(j)).physics().create("axialBulk"+sf,"GlobalEquations","g"+sf);
   m.component(c(j)).mesh().create("dummy"+sf);m.component(c(j)).mesh("dummy"+sf).autoMeshSize(9);m.component(c(j)).mesh("dummy"+sf).run();
   System.out.println("NATIVE CELL "+j+"/"+(nr*nz));System.out.flush();
  }
'''
base=base.replace(needle,setup+'\n  '+needle)
base=base.replace('g("gridRadialCount","4");g("gridAxialCount","6");','g("gridRadialCount",Integer.toString(nr));g("gridAxialCount",Integer.toString(nz));g("axialCellCount",Integer.toString(nr*nz));')
base=base.replace('(j==0?"+targetMassSource":"")','(k==0?"+columnMass"+i:"")')
base=base.replace('v(j,"Psource",j==0?"targetPowerSource":"0[W]");','v(j,"Psource",k==0?"columnPower"+i:"0[W]");')
base=base.replace('(j==0?"+targetElectronSource":"")','(k==0?"+columnElectron"+i:"")')
base=base.replace('"axialIncident*"+beam+"*exp(-( "+back+', '"(1-Acu)*axialIncident*"+beam+"*exp(-( "+back+') if False else base
base=base.replace('v(j,"incomingBack","axialIncident*"','v(j,"incomingBack","(1-Acu)*axialIncident*"')
base=base.replace('if(j==0&&n.startsWith("Cu")&&!n.matches("Cu_[234]p"))input+="+thermalFlux"+n+"(t/1[s])*1[kg/s]";','if(k==0&&n.startsWith("Cu"))input+="+columnSpecies"+i+"_"+n;')
base=base.replace('(j==0?"+targetMassSource*targetNormalSpeed":"")','(k==0?"+columnMomentum"+i:"")')
needle='String ene="0[J]",cu="0[kg]"'
coupling='''
  String trans="0",tg="0[K]",rho="0[kg/m^3]",pcu="0[Pa]",ret="0[kg/(m^2*s)]",returnH="0[W/m^2]";
  for(int i=0;i<nr;i++){
   String mask="if(r>="+num(re[i])+"[m]&&r<"+num(re[i+1])+"[m],1,0)",tau="0";
   for(int k=0;k<nz;k++)tau+="+"+s(k*nr+i,"laserTau");
   g("columnTransmission"+i,"exp(-("+tau+"))");trans+="+"+mask+"*columnTransmission"+i;
   tg+="+"+mask+"*"+s(i,"Tg");pcu+="+"+mask+"*"+s(i,"pPl");
   String returnMass="0[kg/(m^2*s)]",returnEnergy="0[W/m^2]",returnElectron="0[W/m^2]";
   for(String n:sp)if(n.startsWith("Cu")){
    String nn="n_"+n;
    String density=s(i,"rhoP")+"*"+s(i,"w"+n);
    String rr="("+density+")*sqrt(R_const*"+s(i,"Tg")+"/(2*pi*Mcu))";
    g("returnRing"+i+"_"+n,rr);returnMass+="+returnRing"+i+"_"+n;
    double ev=0;int charge=0;
    String[] cn={"Cu","CuD5","CuD3","CuP1","CuP3","CuH3","CuH5","CuT5","Cu_1p","Cu_2p","Cu_3p","Cu_4p"};
    double[] ce={0,1.388948,1.6422256,3.7858977,3.816692,6.1911751,6.1920252,6.8676456,7.72638,28.01877,64.85977,122.23977};
    for(int q=0;q<cn.length;q++)if(n.equals(cn[q])){ev=ce[q];charge=q>=8?q-7:0;}
    returnEnergy+="+returnRing"+i+"_"+n+"*(Bcu+2.5*R_const*"+s(i,"Tg")+"/Mcu+"+num(ev)+"[V]*F_const/Mcu+0.5*("+s(i,"uR")+"^2+"+s(i,"uZ")+"^2))";
    returnElectron+="+returnRing"+i+"_"+n+"*"+charge+"*2*R_const*"+s(i,"TeK")+"/Mcu";
    String emission=n.matches("Cu_[234]p")?"0[kg/(m^2*s)]":"mdotEvap*emitFraction_"+n;
    g("columnSpecies"+i+"_"+n,"c.intSurf("+mask+"*("+emission+"-returnRing"+i+"_"+n+"))");
   }
   g("columnReturn"+i,returnMass);g("columnReturnElectron"+i,returnElectron);g("columnReturnEnergy"+i,returnEnergy+"+columnReturnElectron"+i);
   ret+="+"+mask+"*columnReturn"+i;returnH+="+"+mask+"*columnReturnEnergy"+i;
   g("columnMass"+i,"c.intSurf("+mask+"*mdot)");g("columnPower"+i,"c.intSurf("+mask+"*(Qmass-Qexchange))");g("columnElectron"+i,"c.intSurf("+mask+"*(electronFeed-columnReturnElectron"+i+"))");
   g("columnMomentum"+i,"c.intSurf("+mask+"*(mdotEvap*sqrt(R_const*T/Mcu)-columnReturn"+i+"*"+s(i,"uZ")+"))");
  }
  m.component("c").variable("boundv").set("gridTransmission",trans);m.component("c").variable("boundv").set("gridGasT",tg);m.component("c").variable("boundv").set("pPl",pcu);
  m.component("c").variable("boundv").set("mdotReturn",ret);m.component("c").variable("boundv").set("mdot","mdotEvap-mdotReturn");
  m.component("c").variable("boundv").set("Qsurface","Acu*Ipeak*pulse*exp(-2*(r/w0)^2)*gridTransmission");
  m.component("c").variable("boundv").set("Qexchange","0[W/m^2]");
  m.component("c").variable("boundv").set("electronFeed","2*R_const*T/Mcu*mdotEvap*emitFraction_Cu_1p");
  m.component("c").variable("boundv").set("Qmass","mdotEvap*(Bcu+3*R_const*T/Mcu+F_const*emitMeanEnergy/Mcu)+electronFeed-("+returnH+")");
  m.component("c").physics("ht").feature("laser").set("q0","Qsurface-Qmass+Qexchange");
  m.component(c(0)).physics().create("ledger","GlobalEquations","gA");
  m.component(c(0)).physics("ledger").feature("ge1").set("name",new String[]{"axialInputEnergy","axialRadiatedEnergy","axialOutEnergy","axialInputCu","axialOutputCu","targetLaserEnergy"});
  m.component(c(0)).physics("ledger").feature("ge1").set("equation",new String[]{"axialInputEnergyt-(targetPowerSource+axialLaser)/energyScale","axialRadiatedEnergyt-axialRadiation/energyScale","axialOutEnergyt-energyF24/energyScale","axialInputCut-targetMassSource/mScale","axialOutputCut-axialOutCu/mScale","targetLaserEnergyt-c.intSurf(c.Qsurface)/energyScale"});
  m.component(c(0)).physics("ledger").feature("ge1").set("initialValueU",new String[]{"0","0","0","0","0","0"});
'''
base=base.replace(needle,coupling+'\n  '+needle)
base=base.replace('m.label("Native Plasma + COARSE 4r x6z axisymmetric Euler FV; H04 prescribed thermal input concentrated within r11um; no moving surface or sheath; no radius fit")','m.label("Spatial FV native Plasma with direct copper thermal/laser shielding feedback; 8r x10z, fixed surface, no sheath; not spatially converged")')
base=base.replace('System.out.println("AXISYMMETRIC GRID READY 24 native cells")','System.out.println("SPATIAL THERMAL GRID READY "+(nr*nz)+" native cells")')
# Preserve a reviewable generated source. No source rate is fitted to S4.
(R/'src/BuildSpatialGridThermal.java').write_text(base)
print('Generated BuildSpatialGridThermal')
