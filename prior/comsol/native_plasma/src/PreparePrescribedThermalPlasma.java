import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Modular predictor: native two-zone multi-charge Plasma with the actually
 * solved thermal evaporation history. Source heat is one-way; gas zones retain
 * their mutual pressure/composition/enthalpy feedback. No image-derived input.
 */
public class PreparePrescribedThermalPlasma {
 static Model m;
 static void v(String n,String x){m.component("c").variable("gasv").set(n,x);}
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.load("prescribedThermal",a[0]);for(String s:m.sol().tags())m.sol().remove(s);
  m.component("c").physics().remove("ht");m.component("c").material().remove("copper");
  ArrayList<String[]> funcs=new ArrayList<>();int col=1;
  for(String n:new String[]{"thermalMass","thermalPower","thermalElectron","thermalTargetK","thermalTargetAbsorption","thermalStoredEnergy","thermalBasePower"})funcs.add(new String[]{n,Integer.toString(col++)});
  for(String n:SurfaceStateFeed.CU)funcs.add(new String[]{"thermalFlux"+n,Integer.toString(col++)});funcs.add(new String[]{"thermalFluxCuT5",Integer.toString(col++)});
  m.func().create("thermalFeed","Interpolation");m.func("thermalFeed").set("source","file");m.func("thermalFeed").set("filename",a[2]);m.func("thermalFeed").set("nargs",1);m.func("thermalFeed").set("funcs",funcs.toArray(new String[0][]));m.func("thermalFeed").set("interp","linear");m.func("thermalFeed").set("extrap","const");m.func("thermalFeed").importData();
  v("thermalSourceMode","1");v("T","Tamb");v("Tr","0[K/m]");v("Tz","0[K/m]");
  v("prescribedTargetK","thermalTargetK(t/1[s])*1[K]");v("prescribedTargetAbsorption","thermalTargetAbsorption(t/1[s])*1[W]");
  v("Msource","thermalMass(t/1[s])*1[kg/s]-outMassRate");v("Psource","thermalPower(t/1[s])*1[W]");v("PeSource","thermalElectron(t/1[s])*1[W]");
  ArrayList<String> species=new ArrayList<>(Arrays.asList(SurfaceStateFeed.CU));species.add("CuT5");
  for(String n:species){String rate=ConservativeTrace.rate(m,n);rate=rate.replace("intSurf(source_"+n+")","thermalFlux"+n+"(t/1[s])*1[kg/s]");m.component("c").physics("plas").feature(n).featureInfo("info").set("plas.R_w"+n,new String[]{rate});}
  for(int q=2;q<=4;q++){
   String n="Cu_"+q+"p",rate=ConservativeTrace.rate(m,n);rate=rate.replace("intSurf(mdotReturn*nCu"+q+"/nCuTotal)","0[kg/s]");m.component("c").physics("plas").feature(n).featureInfo("info").set("plas.R_w"+n,new String[]{rate});
  }
  // Reserve the already solved Acu laser share for the target. The remaining
  // light follows a forward/back path through both native plasma reservoirs.
  v("coveredInnerPower","(1-Acu)*Pincident*(1-exp(-2*(Rinit/w0)^2))");v("coveredOuterPower","(1-Acu)*Pincident*(1-exp(-2*(outer.Rp/w0)^2))");
  for(String comp:new String[]{"c","outer"}){
   String group=comp.equals("c")?"gasv":"outerGas";
   for(String n:new String[]{"Plaser","meanLaserIntensity"})m.component(comp).variable(group).set(n,m.component(comp).variable(group).get(n).replace("(1-Acu)","1"));
  }
  String[] eq=m.component("c").physics("bulk").feature("ge1").getStringArray("equation");
  for(int i=0;i<eq.length;i++){
   if(eq[i].startsWith("absorbedEnergyt"))eq[i]="absorbedEnergyt-(Psource+Plaser+outer.Plaser)/energyScale";
   if(eq[i].startsWith("removedMasst"))eq[i]="removedMasst-thermalMass(t/1[s])*1[kg/s]/mScale";
  }
  m.component("c").physics("bulk").feature("ge1").set("equation",eq);
  m.param().set("tEnd","1000[ns]");m.study("std").feature("time").set("tlist","range(0,1[ns],tEnd)");m.study("std").createAutoSequences("sol");
  m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","1[ns]");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("atolglobal","1e-7");
  for(String f:m.sol("sol1").feature("v1").feature().tags()){
   String scale=f.endsWith("En")?"1e7":f.endsWith("_pA")?"1e5":f.endsWith("_ODE1")?"1":f.endsWith("_w")?(f.endsWith("_O2_w")||f.endsWith("_O2X_w")?"1":"1e-8"):null;
   if(scale!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",scale);}
  }
  m.label("Modular thermal predictor + native two-zone Cu0-Cu4+ Plasma; prescribed evaporation, reserved target laser share, no thermal feedback");m.save(a[1]);m.save(a[1].replace(".mph",".java"),"java");System.out.println("PRESCRIBED THERMAL PLASMA READY; gas exchange two-way, target one-way");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
