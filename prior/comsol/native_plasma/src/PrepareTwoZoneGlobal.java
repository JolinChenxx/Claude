import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Native Plasma in two conservative reservoirs: spot-sized source and expanding
 * outer gas, plus swept-air inertia. This is a spatially reduced diagnostic,
 * not a two-dimensional plume solution or a fitted luminous-core shape.
 */
public class PrepareTwoZoneGlobal {
 static Model m;
 static void v(String comp,String n,String x){m.component(comp).variable(comp.equals("c")?"gasv":"outerGas").set(n,x);}
 static void b(String n,String x){m.component("c").variable("boundv").set(n,x);}
 static String outerName(String n){return n.contains("_1")?n.replace("_1","X_1"):n+"X";}
 static String rate(String comp,String n){String p=comp.equals("c")?"plas":"plas2",nativeName=comp.equals("c")?n:outerName(n);
  for(String[] row:m.component(comp).physics(p).feature(nativeName).featureInfo("info").getInfoTable("Expression"))if(row.length>2&&(p+".R_w"+nativeName).equals(row[1]))return row[2];
  throw new RuntimeException("Missing native rate "+comp+" "+n);
 }
 static void setRate(String comp,String n,String x){String p=comp.equals("c")?"plas":"plas2",nativeName=comp.equals("c")?n:outerName(n);m.component(comp).physics(p).feature(nativeName).featureInfo("info").set(p+".R_w"+nativeName,new String[]{x});}
 static void equations(String comp,String[] names,String[] eq,String[] init){String tag=comp.equals("c")?"bulk":"outerBulk";
  m.component(comp).physics(tag).feature("ge1").set("name",names);m.component(comp).physics(tag).feature("ge1").set("equation",eq);m.component(comp).physics(tag).feature("ge1").set("initialValueU",init);
 }
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.load("twoZone",a[0]);for(String s:m.sol().tags())m.sol().remove(s);
  AddOuterComponent.apply(m);
  // The outer component contains native chemistry and equations only. It has
  // no second laser target, second heat capacity or artificial surface source.
  m.component("outer").physics().remove("outerHeat");
  m.component("outer").material().remove("outerCopper");
  for(String n:new String[]{"logRadius","pRadius","gasEnergy","logGasT","shellMass","shellEnergy"})v("outer",n,"outer"+n);
  v("outer","Rp","Rinit*(1+exp(3*logRadius))^(1/3)");v("outer","Hp","Rp");
  v("outer","Vp","Vinit*exp(3*logRadius)");
  v("outer","kineticFactor","(3/5)*(Rp^5-Rinit^5)/(Rp^2*(Rp^3-Rinit^3))");
  v("outer","kineticFactorDerivative","kineticFactor*(5*Rp^4/(Rp^5-Rinit^5)-2/Rp-3*Rp^2/(Rp^3-Rinit^3))");
  v("outer","inertialMass","shellMass*mScale+kineticFactor*Mgas");
  v("outer","vRadius","momentumScale*pRadius/inertialMass");v("outer","vHeight","vRadius");
  v("outer","Vdot","2*pi*Rp^2*vRadius");v("outer","volumeRate","Vdot/Vp");
  v("outer","Kinetic","0.5*inertialMass*vRadius^2");
  v("outer","Msource","c.outMassRate");v("outer","Psource","c.outPower");v("outer","PeSource","c.outElectronPower");
  v("outer","shellStoredEnergy","shellEnergy*energyScale");
  v("outer","gasLoadingHeat","0.5*vRadius^2*(kineticFactor*Msource+Mgas*kineticFactorDerivative*vRadius)");
  v("outer","shockHeating","0.5*Msweep*vRadius^2");
  v("outer","totalGasShellEnergy","gasEnergy*energyScale+shellStoredEnergy+Kinetic");
  v("outer","laserTau","alphaLaser*(Rp-Rinit)");
  // The same laser rays cross outer gas, inner gas and the target; the reflected
  // pass is attenuated in the reverse order. Beam areas are integrated over
  // the actual target mesh, matching the surface heat flux exactly.
  b("contactInner","flc2hs(Rinit-r,1[um])");b("contactOuter","flc2hs(outer.Rp-r,1[um])");
  v("c","coveredInnerPower","intSurf(Ipeak*pulse*exp(-2*(r/w0)^2)*contactInner)");
  v("c","coveredOuterPower","intSurf(Ipeak*pulse*exp(-2*(r/w0)^2)*contactOuter)");
  b("Qsurface","Acu*Ipeak*pulse*exp(-2*(r/w0)^2)*(1-contactOuter*(1-outer.beamTransmission))*(1-contactInner*(1-beamTransmission))");
  v("c","Plaser","coveredInnerPower*outer.beamTransmission*beamAbsorptance*(1+(1-Acu)*beamTransmission)");
  v("outer","Plaser","beamAbsorptance*(c.coveredOuterPower+(1-Acu)*beamTransmission*(c.coveredOuterPower-c.coveredInnerPower*(1-c.beamTransmission^2)))");
  v("c","meanLaserIntensity","coveredInnerPower*outer.beamTransmission*Hp/Vp*if(laserTau<1e-4,1-laserTau/2+laserTau^2/6-laserTau^3/24,beamAbsorptance/laserTau)*(1+(1-Acu)*beamTransmission)");
  v("outer","meanLaserIntensity","(Rp-Rinit)/Vp*if(laserTau<1e-4,1-laserTau/2+laserTau^2/6-laserTau^3/24,beamAbsorptance/laserTau)*(c.coveredOuterPower+(1-Acu)*beamTransmission*(c.coveredOuterPower-c.coveredInnerPower*(1-c.beamTransmission^2)))");
  // Bidirectional isentropic exchange with donor composition and full donor
  // enthalpy. The sink and source use the exact same flux expression.
  v("outer","gammaNozzle","1+(nHeavy*k_B_const+(2/3)*Ue/Tg)/(CvVol+Ue/Tg)");
  v("c","nozzleRatio","max(nozzleRatioCritical,min(1,outer.pPl/pPl))");
  v("c","forwardMassRate","nozzleArea*(sqrt(nozzleFluxSquared+1e-20[kg^2/(m^4*s^2)])-1e-10[kg/(m^2*s)])");
  v("outer","nozzleRatio","max((2/(gammaNozzle+1))^(gammaNozzle/(gammaNozzle-1)),min(1,c.pPl/pPl))");
  v("outer","nozzleFluxSquared","max(2*gammaNozzle/(gammaNozzle-1)*rhoP*pPl*(nozzleRatio^(2/gammaNozzle)-nozzleRatio^((gammaNozzle+1)/gammaNozzle)),0[kg^2/(m^4*s^2)])");
  v("c","reverseMassRate","nozzleArea*(sqrt(outer.nozzleFluxSquared+1e-20[kg^2/(m^4*s^2)])-1e-10[kg/(m^2*s)])");
  v("outer","specificEnthalpy","(CvVol*Tg+Uchem+Ue+pPl)/rhoP");
  v("c","outMassRate","forwardMassRate-reverseMassRate");
  v("c","outPower","forwardMassRate*outSpecificEnthalpy-reverseMassRate*outer.specificEnthalpy");
  v("c","outElectronPower","forwardMassRate*(Ue+pe)/rhoP-reverseMassRate*(outer.Ue+outer.pe)/outer.rhoP");
  v("c","outCuRate","forwardMassRate*rhoCopper/rhoP-reverseMassRate*outer.rhoCopper/outer.rhoP");
  ArrayList<String> species=new ArrayList<>(Arrays.asList(Chemistry.NAMES));species.add("CuT5");
  for(String n:species){if(n.equals("N2"))continue;
   String inner=rate("c",n),outer=rate("outer",n);
   setRate("c",n,"("+inner+")+reverseMassRate*(outer.plas2.w"+n+"-plas.w"+n+")/(Vp*plas.M_w"+n+")");
   String inherited=n.equals("CuT5")?"outerIntSurf(source_CuT5)/(Vp*Mcu)":n.startsWith("Cu")?"(outerIntSurf(source_"+n+")/Vp)/plas2.M_w"+outerName(n):n.equals("O2")?"(Msweep*(0.21*0.0319988[kg/mol]/Mair)/Vp)/plas2.M_wO2X":null;
   // Keep the native reaction expression, dropping its recorded target/air
   // source. Express the same transfer positively in the receiving reservoir.
   if(inherited!=null){
    if(!outer.contains(inherited))throw new RuntimeException("Cannot isolate inherited source "+n+": "+outer);
    outer=outer.replace(inherited,"0[mol/(m^3*s)]");
   }
   setRate("outer",n,"("+outer+")+(c.forwardMassRate*c.plas.w"+n+"-c.reverseMassRate*plas2.w"+n+")/(Vp*plas2.M_w"+n+")");
  }
  m.component("outer").physics("plas2").feature("pes1").set("Pgm","Plaser+PeSource-Vp*(QeRR+QeAttach+Qff+Qei+(Ue+pe)*volumeRate)-Vp*tailPhotoEvents*tailI*e_const");
  equations("outer",new String[]{"outerlogRadius","outerpRadius","outergasEnergy","outerlogGasT","outershellMass","outershellEnergy"},new String[]{
   "outerlogRadiust-vRadius*Rp^2/(Rp^3-Rinit^3)",
   "outerpRadiust-2*pi*Rp^2*(pPl-pAmb)/momentumScale",
   "outergasEnergyt-(Psource+Plaser-Qrad*Vp-pPl*Vdot+gasLoadingHeat)/energyScale",
   "(gasEnergy*energyScale-Vp*(CvVol*Tg+Uchem+Ue))/energyScale",
   "outershellMasst-Msweep/mScale","outershellEnergyt-(eAir*Msweep+shockHeating)/energyScale"
  },new String[]{"0","0","2.5*pAmb*Vinit/energyScale","log(Tamb/1[K])","0","0"});
  // Global input/radiation ledgers include both zones. Their internal exchange
  // remains visible in outEnergy but cancels from the combined conservation.
  equations("c",new String[]{"gasEnergy","logGasT","absorbedEnergy","radiatedEnergy","ambientWork","entrainedEnergy","removedMass","outMass","outEnergy","outCuMass"},new String[]{
   "gasEnergyt-(Psource+Plaser-Qrad*Vp-outPower)/energyScale",
   "(gasEnergy*energyScale-Vp*(CvVol*Tg+Uchem+Ue))/energyScale",
   "absorbedEnergyt-(intSurf(Qsurface)+Plaser+outer.Plaser)/energyScale",
   "radiatedEnergyt-(Qrad*Vp+outer.Qrad*outer.Vp)/energyScale",
   "ambientWorkt-pAmb*outer.Vdot/energyScale","entrainedEnergyt-eAir*outer.Msweep/energyScale",
   "removedMasst-intSurf(mdot)/mScale","outMasst-outMassRate/mScale","outEnergyt-outPower/energyScale","outCuMasst-outCuRate/mScale"
  },new String[]{"2.5*pAmb*Vinit/energyScale","log(Tamb/1[K])","0","0","0","0","0","0","0","0"});
  // Core and outer material are initially at the same ambient density and
  // seed density. All structural changes restart from this initial state.
  m.param().set("tEnd",a.length>2?a[2]+"[ns]":"203[ns]");
  m.study("std").feature("time").set("tlist","range(0,1[ns],tEnd)");m.study("std").createAutoSequences("sol");
  m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","0.5[ns]");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("atolglobal","1e-7");
  for(String f:m.sol("sol1").feature("v1").feature().tags()){
   String scale=f.equals("c_T")?"1000":f.endsWith("En")?"1e7":f.endsWith("_pA")?"1e5":f.endsWith("_ODE1")?"1":f.endsWith("_w")?(f.endsWith("_O2_w")||f.endsWith("_O2X_w")?"1":"1e-20"):null;
   if(scale!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",scale);}
  }
  m.label("Native two-zone Plasma: conservative source/outer exchange and swept-air shell; reduced geometry, no S4 fitting");m.save(a[1]);m.save(a[1].replace(".mph",".java"),"java");
  System.out.println("TWO ZONE READY: two native plasma components, one target, conservative exchange; no solved result yet");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
