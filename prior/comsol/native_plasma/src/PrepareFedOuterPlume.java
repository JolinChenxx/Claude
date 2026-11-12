import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Native outer Plasma driven by actual saved source flux; one-way diagnosis.
 * Removes the duplicated target heat PDE from this downstream calculation.
 * No fitted energy input, no emission outside the computed outer volume.
 */
public class PrepareFedOuterPlume {
 static Model m;static void v(String n,String x){m.component("c").variable("gasv").set(n,x);}
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.load("fedOuter",a[0]);for(String s:m.sol().tags())m.sol().remove(s);
  m.component("c").physics().remove("ht");m.component("c").material().remove("copper");
  ArrayList<String> species=new ArrayList<>(Arrays.asList(Chemistry.NAMES));species.add("CuT5");
  ArrayList<String[]> funcs=new ArrayList<>();int col=1;
  for(String n:new String[]{"sourceMass","sourcePower","sourceElectron","sourceTargetK","sourceLaser","sourceTargetAbsorption"})funcs.add(new String[]{n,Integer.toString(col++)});
  for(String n:species)funcs.add(new String[]{"sourceFlux"+n,Integer.toString(col++)});
  m.func().create("fedSource","Interpolation");m.func("fedSource").set("source","file");m.func("fedSource").set("filename",a[2]);m.func("fedSource").set("nargs",1);m.func("fedSource").set("funcs",funcs.toArray(new String[0][]));m.func("fedSource").set("interp","linear");m.func("fedSource").set("extrap","const");m.func("fedSource").importData();
  v("feedSourceMode","1");v("T","Tamb");v("Tr","0[K/m]");v("Tz","0[K/m]");
  v("feedTargetTemperature","sourceTargetK(t/1[s])*1[K]");v("feedLaserAbsorption","sourceLaser(t/1[s])*1[W]");v("feedTargetAbsorption","sourceTargetAbsorption(t/1[s])*1[W]");
  v("Msource","sourceMass(t/1[s])*1[kg/s]");v("Psource","sourcePower(t/1[s])*1[W]");v("PeSource","sourceElectron(t/1[s])*1[W]");
  v("Plaser","0[W]");v("meanLaserIntensity","0[W/m^2]");
  for(String n:species){if(n.equals("N2"))continue;
   String old=ConservativeTrace.rate(m,n);
   if(n.startsWith("Cu"))old=old.replace("intSurf(source_"+n+")","0[kg/s]");
   m.component("c").physics("plas").feature(n).featureInfo("info").set("plas.R_w"+n,new String[]{"("+old+")+sourceFlux"+n+"(t/1[s])*1[kg/s]/(Vp*plas.M_w"+n+")"});
  }
  String[] names={"logRadius","pRadius","gasEnergy","logGasT","shellMass","shellEnergy","absorbedEnergy","radiatedEnergy","ambientWork","entrainedEnergy","removedMass"};
  String cu="0[kg/s]";for(String n:species)if(n.startsWith("Cu"))cu+="+sourceFlux"+n+"(t/1[s])*1[kg/s]";
  String[] eq={"logRadiust-vRadius/Rp","pRadiust-2*pi*Rp^2*(pPl-pAmb)/momentumScale","gasEnergyt-(Psource-Qrad*Vp-pPl*Vdot+gasLoadingHeat)/energyScale","(gasEnergy*energyScale-Vp*(CvVol*Tg+Uchem+Ue))/energyScale","shellMasst-Msweep/mScale","shellEnergyt-(eAir*Msweep+shockHeating)/energyScale","absorbedEnergyt-Psource/energyScale","radiatedEnergyt-Qrad*Vp/energyScale","ambientWorkt-pAmb*Vdot/energyScale","entrainedEnergyt-eAir*Msweep/energyScale","removedMasst-("+cu+")/mScale"};
  String[] init={"0","0","2.5*pAmb*Vinit/energyScale","log(Tamb/1[K])","0","0","0","0","0","0","0"};
  m.component("c").physics("bulk").feature("ge1").set("name",names);m.component("c").physics("bulk").feature("ge1").set("equation",eq);m.component("c").physics("bulk").feature("ge1").set("initialValueU",init);
  m.param().set("tEnd",a[3]+"[ns]");m.study("std").feature("time").set("tlist","range(0,1[ns],tEnd)");m.study("std").createAutoSequences("sol");
  m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","1[ns]");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("atolglobal","1e-7");
  for(String f:m.sol("sol1").feature("v1").feature().tags()){
   String scale=f.endsWith("En")?"1e7":f.endsWith("_pA")?"1e5":f.endsWith("_ODE1")?"1":f.endsWith("_w")?(f.endsWith("_O2_w")?"1":"1e-8"):null;
   if(scale!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",scale);}
  }
  m.label("Native outer plasma from saved source flux; one-way, no target PDE, no outer laser absorption; assumed hemisphere");m.save(a[1]);m.save(a[1].replace(".mph",".java"),"java");System.out.println("FED OUTER READY; input ledger is injected enthalpy, NOT laser absorption");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
