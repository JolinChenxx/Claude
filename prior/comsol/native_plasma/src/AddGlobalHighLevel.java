import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Explicit native Cu 5d state, avoiding the former quasi-steady elimination.
 * Data: NIST energies/A, van Regemorter/Lotz, Kramers-Milne approximations.
 * This optional case must start from the original initial state. */
public class AddGlobalHighLevel {
 static Model m;
 static void v(String n,String s){m.component("c").variable("gasv").set(n,s);}
 static void b(String n,String s){m.component("c").variable("boundv").set(n,s);}
 static String g(String n){return m.component("c").variable("gasv").get(n);}
 static String boundary(String n){return m.component("c").variable("boundv").get(n);}
 static void rx(String tag,String formula,String rate,String de){
  m.component("c").physics("plas").create(tag,de==null?"Reaction":"ElectronImpactReaction",2);
  m.component("c").physics("plas").feature(tag).set("formula",formula);m.component("c").physics("plas").feature(tag).set("kf",rate);
  if(de!=null){m.component("c").physics("plas").feature(tag).set("type","Excitation");m.component("c").physics("plas").feature(tag).set("de",de);}
 }
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.load("highLevel",a[0]);
  String oldParent=ConservativeTrace.rate(m,"CuP3"),oldIon=ConservativeTrace.rate(m,"Cu_1p");
  m.param().set("tailEnergy","6.8676456[V]");m.param().set("tailI","0.8587344[V]");m.param().set("tailDelta","3.0509536[V]");m.param().set("tailA","2.1e7[1/s]");
  m.func().create("tailRates","Interpolation");m.func("tailRates").set("source","file");m.func("tailRates").set("filename","D:/junlin/PlasmaPlume/route1/data/tail5d_log_rates.txt");m.func("tailRates").set("nargs",1);m.func("tailRates").set("funcs",new String[][]{{"tailUp","1"},{"tailDn","2"},{"tailIon","3"},{"tailRec","4"},{"tailRR","5"},{"tailRRmean","6"}});m.func("tailRates").set("interp","piecewisecubic");m.func("tailRates").set("extrap","const");m.func("tailRates").importData();
  for(String[] q:new String[][]{{"tailKu","tailUp","m^3/s"},{"tailKd","tailDn","m^3/s"},{"tailKi","tailIon","m^3/s"},{"tailK3","tailRec","m^6/s"},{"tailKrr","tailRR","m^3/s"},{"tailMeanRR","tailRRmean","V"}})v(q[0],"10^"+q[1]+"(log10(TeV/1[V]))*1["+q[2]+"]");
  rx("nativeTailUp","e+CuP3=>e+CuT5","tailKu*N_A_const","3.0509536");rx("nativeTailDn","e+CuT5=>e+CuP3","tailKd*N_A_const","-3.0509536");
  rx("nativeTailIon","e+CuT5=>2e+Cu+","tailKi*N_A_const","0.8587344");rx("nativeTail3rec","2e+Cu+=>e+CuT5","tailK3*N_A_const^2","-0.8587344");
  rx("nativeTailRR","e+Cu+=>CuT5","tailKrr*N_A_const",null);rx("nativeTailRad","CuT5=>CuP3","tailA*tailBeta",null);rx("nativeTailPhoto","CuT5=>e+Cu+","tailPhotoRate",null);
  m.component("c").physics("plas").feature("CuT5").set("M","Mcu");m.component("c").physics("plas").feature("CuT5").set("x0","1e-24");v("plas.wCuT5","wCuT5");v("nCuT5","plas.n_wCuT5");
  for(String n:new String[]{"nCuTotal","nHeavy"})v(n,g(n)+"+nCuT5");v("CvVol",g("CvVol")+"+1.5*k_B_const*nCuT5");v("Uchem",g("Uchem")+"+e_const*tailEnergy*nCuT5");
  v("tailDnu","1/(406.2638[nm])*sqrt(2*k_B_const*Tg/(Mcu/N_A_const))");v("tailTau","e_const^2/(4*epsilon0_const*me_const*c_const)*0.07794434001494746/(sqrt(pi)*tailDnu)*max(nCuP3-(4/6)*nCuT5,0[1/m^3])*Req");
  v("tailBeta","if(tailTau<0.01,1-3*tailTau/4+2*tailTau^2/5,3/(4*tailTau)*(1-1/(2*tailTau^2)+(1/tailTau+1/(2*tailTau^2))*exp(-2*tailTau)))");
  v("tailPhoton","h_const*c_const/lambdaL");v("tailSigma","6.3e-22[m^2]*sqrt(13.605693[V]/tailI)*(tailI*e_const/tailPhoton)^3");v("alphaIB",g("alphaLaser"));v("alphaPhoto","nCuT5*tailSigma");v("alphaLaser","alphaIB+alphaPhoto");
  // Define the same volume-averaged intensity for both absorption channels.
  // Its finite thin limit is Pincident*coverage*H*(1+reflection)/V.
  v("meanLaserIntensity","Pincident*beamCoverage*Hp/Vp*if(laserTau<1e-4,1-laserTau/2+laserTau^2/6-laserTau^3/24,(1-exp(-laserTau))/laserTau)*(1+(1-Acu)*beamTransmission)");
  v("tailPhotoRate","tailSigma*meanLaserIntensity/tailPhoton");v("tailPhotoEvents","nCuT5*tailPhotoRate");
  v("tailNetExcitation","plas.ne*nCuP3*tailKu-plas.ne*nCuT5*tailKd-tailA*tailBeta*nCuT5");
  v("tailNetIonization","plas.ne*nCuT5*tailKi-plas.ne^2*nCu_1p*tailK3-plas.ne*nCu_1p*tailKrr+tailPhotoEvents");
  m.component("c").physics("plas").feature("CuP3").featureInfo("info").set("plas.R_wCuP3",new String[]{"("+oldParent+")-tailNetExcitation/N_A_const"});
  m.component("c").physics("plas").feature("Cu_1p").featureInfo("info").set("plas.R_wCu_1p",new String[]{"("+oldIon+")+tailNetIonization/N_A_const"});
  b("emitWeight_CuT5","6*exp(-tailEnergy*e_const/(k_B_const*T))");b("emitPartition",boundary("emitPartition")+"+emitWeight_CuT5");b("emitFraction_CuT5","emitWeight_CuT5/emitPartition");
  b("emitMeanEnergy",boundary("emitMeanEnergy")+"+emitFraction_CuT5*tailEnergy");v("returnMeanEnergy",g("returnMeanEnergy")+"+nCuT5/max(nCuTotal,1[1/m^3])*tailEnergy");b("source_CuT5","mdotEvap*emitFraction_CuT5-mdotReturn*nCuT5/max(nCuTotal,1[1/m^3])");
  m.component("c").physics("plas").feature("CuT5").featureInfo("info").set("plas.R_wCuT5",new String[]{"(tailNetExcitation-tailNetIonization)/N_A_const+intSurf(source_CuT5)/(Vp*Mcu)"});
  v("tailQfb","plas.ne*nCu_1p*tailKrr*tailMeanRR*e_const");v("tailQline","tailA*tailBeta*nCuT5*tailDelta*e_const");v("Qrad",g("Qrad")+"+tailQfb+tailQline");v("QeRR",g("QeRR")+"+plas.ne*nCu_1p*tailKrr*(tailMeanRR-tailI)*e_const");
  String power=m.component("c").physics("plas").feature("pes1").getString("Pgm");m.component("c").physics("plas").feature("pes1").set("Pgm",power+"-Vp*tailPhotoEvents*tailI*e_const");
  m.component("c").physics("plas").create("elasticTail","ElectronImpactReaction",2);m.component("c").physics("plas").feature("elasticTail").set("formula","e+CuT5=>e+CuT5");m.component("c").physics("plas").feature("elasticTail").set("type","Elastic");m.component("c").physics("plas").feature("elasticTail").set("kf","kmCu*N_A_const");m.component("c").physics("plas").feature("elasticTail").set("mratio","me_const*N_A_const/Mcu");
  // Rebuild all dependent variables after adding the explicit state.
  for(String s:m.sol().tags())m.sol().remove(s);m.study("std").createAutoSequences("sol");
  m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","0.5[ns]");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("atolglobal","1e-7");
  for(String f:m.sol("sol1").feature("v1").feature().tags()){
   String scale=f.equals("c_T")?"1000":f.equals("c_En")?"1e7":f.equals("c_plas_pA")?"1e5":f.equals("c_ODE1")?"1":f.startsWith("c_plas_")&&f.endsWith("_w")?(f.equals("c_plas_O2_w")?"1":"1e-20"):null;
   if(scale!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",scale);}
  }
  m.label("Native global Plasma, explicit Cu5d state and 1064nm photoionization; approximate cross sections");m.save(a[1]);m.save(a[1].replace(".mph",".java"),"java");System.out.println("EXPLICIT NATIVE HIGH STATE PREPARED; no solution yet");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
