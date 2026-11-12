import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Same axial transport, driven by actual G21..G44 source-reservoir outflow.
 * Diagnostic: the upstream singly-ionized truncation remains a limitation. */
public class PrepareAxialSourceOutflow {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("axialFed",a[0]);for(String s:m.sol().tags())m.sol(s).clearSolutionData();
  ArrayList<String> species=new ArrayList<>(Arrays.asList(Chemistry.NAMES));species.add("CuT5");ArrayList<String[]> funcs=new ArrayList<>();int col=1;
  for(String n:new String[]{"sourceMass","sourcePower","sourceElectron","sourceTargetK","sourceLaser","sourceTargetAbsorption"})funcs.add(new String[]{n,Integer.toString(col++)});for(String n:species)funcs.add(new String[]{"sourceFlux"+n,Integer.toString(col++)});funcs.add(new String[]{"sourceAxialSpeed",Integer.toString(col++)});
  m.func().create("axialFedSource","Interpolation");m.func("axialFedSource").set("source","file");m.func("axialFedSource").set("filename",a[2]);m.func("axialFedSource").set("nargs",1);m.func("axialFedSource").set("funcs",funcs.toArray(new String[0][]));m.func("axialFedSource").set("interp","linear");m.func("axialFedSource").set("extrap","const");m.func("axialFedSource").importData();
  m.variable("axialFlux").set("axialIncident","0[W]");m.variable("axialFlux").set("targetMassSource","sourceMass(t/1[s])*1[kg/s]");m.variable("axialFlux").set("targetPowerSource","sourcePower(t/1[s])*1[W]");m.variable("axialFlux").set("targetElectronSource","sourceElectron(t/1[s])*1[W]");m.variable("axialFlux").set("targetNormalSpeed","sourceAxialSpeed(t/1[s])*1[m/s]");m.variable("axialFlux").set("prescribedUpstreamOutflow","1");
  Map<String,String> chemical=AxialChemicalRates.rates();
  for(String n:species){if(n.equals("N2"))continue;String nativeName=n.replaceFirst("(_1[pm])$","A$1")+(n.contains("_")?"":"A"),key="plasmaA.R_w"+nativeName;
   String rate=chemical.get(n);if(rate==null)throw new RuntimeException("Missing chemical source "+n);
   rate=rate.replace("plas.","plasmaA.");if(n.equals("Cu_1p"))rate="("+rate+")-multNet1/N_A_const";
   rate="("+rate+")+(speciesF0_"+n+"-speciesF1_"+n+"+sourceFlux"+n+"(t/1[s])*1[kg/s])/(Vp*plasmaA.M_w"+n+")";
   m.component("cellA").physics("plasmaA").feature(nativeName).featureInfo("info").set(key,new String[]{rate});
  }
  // Copper source ledger excludes the small early air outflow.
  String cu="0[kg/s]";for(String n:species)if(n.startsWith("Cu"))cu+="+sourceFlux"+n+"(t/1[s])*1[kg/s]";
  String[] eq=m.component("cellA").physics("ledger").feature("ge1").getStringArray("equation");eq[3]="axialInputCut-("+cu+")/mScale";m.component("cellA").physics("ledger").feature("ge1").set("equation",eq);
  m.sol("sol1").feature("v1").set("initmethod","init");m.label("Native axial FV Plasma from actual source-reservoir outflow; no outer laser; prescribed cross section; upstream Cu+ truncation diagnostic");m.save(a[1]);System.out.println("AXIAL FED OUTFLOW READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
