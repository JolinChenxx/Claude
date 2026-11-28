import com.comsol.model.*;import com.comsol.model.util.*;
/** Native quasineutral hot gas separated from swept ambient air.
 * Spherical hemispherical moment model: hot gas has homologous velocity,
 * ambient air occupies a thin inertial shell. No instantaneous air mixing.
 * Shell thermal energy is accounted for, but not assigned fictitious emission.
 */
public class PrepareGlobalSweptShell {
 static Model m;
 static void v(String n,String s){m.component("c").variable("gasv").set(n,s);}
 static void nativeVar(String f,String n,String x){m.component("c").physics("plas").feature(f).featureInfo("info").set(n,new String[]{x});}
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.load("sweptShell",a[0]);
  for(String s:m.sol().tags())m.sol(s).clearSolutionData();
  v("Hp","Rp");v("inertialMass","shellMass*mScale+0.6*Mgas");
  v("vRadius","momentumScale*pRadius/inertialMass");v("vHeight","vRadius");
  v("Kinetic","0.5*inertialMass*vRadius^2");
  v("Msource","intSurf(mdot)");
  // The copied O2 source included swept-air injection. Native N2 is the mass
  // remainder, so removing the O2 and total-mass injections removes all sweep.
  String old=ConservativeTrace.rate(m,"O2");
  nativeVar("O2","plas.R_wO2","("+old+")-Msweep*(0.21*0.0319988[kg/mol]/Mair)/(Vp*plas.M_wO2)");
  v("shellStoredEnergy","shellEnergy*energyScale");
  v("totalGasShellEnergy","gasEnergy*energyScale+shellStoredEnergy+Kinetic");
  v("gasLoadingHeat","0.3*Msource*vRadius^2");
  v("shockHeating","0.5*Msweep*vRadius^2");
  String[] names={"logRadius","pRadius","gasEnergy","logGasT","shellMass","shellEnergy","absorbedEnergy","radiatedEnergy","ambientWork","entrainedEnergy","removedMass"};
  String[] eq={
   "logRadiust-vRadius/Rp",
   "pRadiust-2*pi*Rp^2*(pPl-pAmb)/momentumScale",
   "gasEnergyt-(Psource+Plaser-Qrad*Vp-pPl*Vdot+gasLoadingHeat)/energyScale",
   "(gasEnergy*energyScale-Vp*(CvVol*Tg+Uchem+Ue))/energyScale",
   "shellMasst-Msweep/mScale",
   "shellEnergyt-(eAir*Msweep+shockHeating)/energyScale",
   "absorbedEnergyt-(intSurf(Qsurface)+Plaser)/energyScale",
   "radiatedEnergyt-Qrad*Vp/energyScale",
   "ambientWorkt-pAmb*Vdot/energyScale",
   "entrainedEnergyt-eAir*Msweep/energyScale",
   "removedMasst-intSurf(mdot)/mScale"};
  String[] init={"0","0","2.5*pAmb*Vinit/energyScale","log(Tamb/1[K])","0","0","0","0","0","0","0"};
  m.component("c").physics("bulk").feature("ge1").set("name",names);
  m.component("c").physics("bulk").feature("ge1").set("equation",eq);
  m.component("c").physics("bulk").feature("ge1").set("initialValueU",init);
  for(String s:m.sol().tags())m.sol().remove(s);m.study("std").createAutoSequences("sol");
  m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","0.5[ns]");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("atolglobal","1e-7");
  for(String f:m.sol("sol1").feature("v1").feature().tags()){
   String scale=f.equals("c_T")?"1000":f.equals("c_En")?"1e7":f.equals("c_plas_pA")?"1e5":f.equals("c_ODE1")?"1":f.startsWith("c_plas_")&&f.endsWith("_w")?(f.equals("c_plas_O2_w")?"1":"1e-20"):null;
   if(scale!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",scale);}
  }
  m.label("Native global hot Plasma plus separate swept-air shell; no instantaneous ambient mixing; approximate geometry");
  m.save(a[1]);m.save(a[1].replace(".mph",".java"),"java");
  System.out.println("SWEPT SHELL READY: momentum/energy conserving moment closure; no shell light assigned");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
