import com.comsol.model.*;import com.comsol.model.util.*;
/** Fixed spot-sized near-target reservoir with native Plasma and nozzle outflow.
 * Diagnostic of whether volume averaging erases local avalanche absorption.
 * No emitting outer plume is assigned. No S4-derived dimensions or rates.
 */
public class PrepareGlobalSourceLayer {
 static Model m;
 static void v(String n,String s){m.component("c").variable("gasv").set(n,s);}
 static void nativeVar(String f,String n,String x){m.component("c").physics("plas").feature(f).featureInfo("info").set(n,new String[]{x});}
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.load("sourceLayer",a[0]);
  for(String s:m.sol().tags())m.sol(s).clearSolutionData();
  v("Rp","Rinit");v("Hp","Hinit");v("vRadius","0[m/s]");v("vHeight","0[m/s]");
  v("volumeRate","0[1/s]");v("Kinetic","0[J]");v("Msweep","0[kg/s]");
  v("nozzleArea","2*pi*Rinit^2");
  v("gammaNozzle","1+(nHeavy*k_B_const+(2/3)*Ue/Tg)/(CvVol+Ue/Tg)");
  v("nozzleRatioCritical","(2/(gammaNozzle+1))^(gammaNozzle/(gammaNozzle-1))");
  v("nozzleRatio","max(nozzleRatioCritical,min(1,pAmb/pPl))");
  v("nozzleFluxSquared","max(2*gammaNozzle/(gammaNozzle-1)*rhoP*pPl*(nozzleRatio^(2/gammaNozzle)-nozzleRatio^((gammaNozzle+1)/gammaNozzle)),0[kg^2/(m^4*s^2)])");
  // Smooth zero-pressure-difference limit; 1e-10 kg/(m2 s) is negligible.
  v("outMassRate","nozzleArea*(sqrt(nozzleFluxSquared+1e-20[kg^2/(m^4*s^2)])-1e-10[kg/(m^2*s)])");
  v("outSpecificEnthalpy","(CvVol*Tg+Uchem+Ue+pPl)/rhoP");
  v("outPower","outMassRate*outSpecificEnthalpy");
  v("outElectronPower","outMassRate*(Ue+pe)/rhoP");
  v("outCuRate","outMassRate*rhoCopper/rhoP");
  v("Msource","intSurf(mdot)-outMassRate");
  for(String n:Chemistry.NAMES){if(n.equals("N2"))continue;
   String old=ConservativeTrace.rate(m,n);
   nativeVar(n,"plas.R_w"+n,"("+old+")-outMassRate*plas.w"+n+"/(Vp*plas.M_w"+n+")");
  }
  if(java.util.Arrays.asList(m.component("c").physics("plas").feature().tags()).contains("CuT5")){
   String old=ConservativeTrace.rate(m,"CuT5");
   nativeVar("CuT5","plas.R_wCuT5","("+old+")-outMassRate*plas.wCuT5/(Vp*plas.M_wCuT5)");
  }
  String pg=m.component("c").physics("plas").feature("pes1").getString("Pgm");
  m.component("c").physics("plas").feature("pes1").set("Pgm","("+pg+")-outElectronPower");
  String[] names={"gasEnergy","logGasT","absorbedEnergy","radiatedEnergy","ambientWork","entrainedEnergy","removedMass","outMass","outEnergy","outCuMass"};
  String[] eq={"gasEnergyt-(Psource+Plaser-Qrad*Vp-outPower)/energyScale","(gasEnergy*energyScale-Vp*(CvVol*Tg+Uchem+Ue))/energyScale","absorbedEnergyt-(intSurf(Qsurface)+Plaser)/energyScale","radiatedEnergyt-Qrad*Vp/energyScale","ambientWorkt","entrainedEnergyt","removedMasst-intSurf(mdot)/mScale","outMasst-outMassRate/mScale","outEnergyt-outPower/energyScale","outCuMasst-outCuRate/mScale"};
  String[] init={"2.5*pAmb*Vinit/energyScale","log(Tamb/1[K])","0","0","0","0","0","0","0","0"};
  m.component("c").physics("bulk").feature("ge1").set("name",names);m.component("c").physics("bulk").feature("ge1").set("equation",eq);m.component("c").physics("bulk").feature("ge1").set("initialValueU",init);
  for(String s:m.sol().tags())m.sol().remove(s);m.study("std").createAutoSequences("sol");
  m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","0.5[ns]");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("atolglobal","1e-7");
  for(String f:m.sol("sol1").feature("v1").feature().tags()){
   String scale=f.equals("c_T")?"1000":f.equals("c_En")?"1e7":f.equals("c_plas_pA")?"1e5":f.equals("c_ODE1")?"1":f.startsWith("c_plas_")&&f.endsWith("_w")?(f.equals("c_plas_O2_w")?"1":"1e-20"):null;
   if(scale!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",scale);}
  }
  m.label("Diagnostic: native spot-sized plasma reservoir with isentropic nozzle, coupled copper heat; not S4 spatial prediction");m.save(a[1]);m.save(a[1].replace(".mph",".java"),"java");
  System.out.println("SOURCE LAYER READY; mass/enthalpy outflow, fixed radius from laser spot; no outer emission");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
