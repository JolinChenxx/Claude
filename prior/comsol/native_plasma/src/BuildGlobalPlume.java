import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
/** Native global Plasma + axisymmetric target heat + expanding ellipsoid.
 * Reduced, quasineutral bulk model. No resolved sheath or plume spatial PDE.
 * Global mass, momentum and total energy determine volume and gas temperature.
 */
public class BuildGlobalPlume {
 static Model m;
 static void v(String n,String x){m.component("c").variable("gasv").set(n,x);}
 static void b(String n,String x){m.component("c").variable("boundv").set(n,x);}
 static void p(String n,String x){m.param().set(n,x);}
 static void nativeVar(String f,String n,String x){m.component("c").physics("plas").feature(f).featureInfo("info").set(n,new String[]{x});}
 static void rect(String name,String[] pos,String[] size){m.component("c").geom("g").create(name,"Rectangle");m.component("c").geom("g").feature(name).set("pos",pos);m.component("c").geom("g").feature(name).set("size",size);m.component("c").geom("g").feature(name).set("selresult",true);}
 static void select(String name,int dim,String x0,String x1,String z0,String z1){
  m.component("c").selection().create(name,"Box");m.component("c").selection(name).set("entitydim",dim);m.component("c").selection(name).set("xmin",x0);m.component("c").selection(name).set("xmax",x1);m.component("c").selection(name).set("ymin",z0);m.component("c").selection(name).set("ymax",z1);m.component("c").selection(name).set("condition","inside");
 }
 public static void main(String[] a){try{
 ModelUtil.initStandalone(false);m=ModelUtil.create("globalPlume");String root=a[0],caseName=a[1];
 for(String[] row:new String[][]{{"w0","11[um]"},{"Ein","400[uJ]"},{"tau","60[ns]"},{"tp","120[ns]"},{"lambdaL","1064[nm]"},{"Acu","0.1"},{"Rdom","650[um]"},{"Dcu","100[um]"},{"Tamb","293.15[K]"},{"pAmb","101325[Pa]"},{"rhoCu","8960[kg/m^3]"},{"Mcu","0.063546[kg/mol]"},{"rho0","1[kg/m^3]"},{"u0","1000[m/s]"},{"e0","1e6[J/kg]"},{"Bcu","5815069.632191036[J/kg]"},{"Tm","1358[K]"},{"Lm","204000[J/kg]"},{"Lv","5069000[J/kg]"},{"Tv","2836[K]"},{"workCu","4.65[V]"},{"accom","1"},{"seedNe","1e9[1/m^3]"},{"Rinit","11[um]"},{"Hinit","11[um]"},{"tEnd","1000[ns]"},{"mScale","1e-12[kg]"},{"momentumScale","1e-9[kg*m/s]"},{"energyScale","1[uJ]"}})p(row[0],row[1]);
 p("Mair","0.79*0.0280134[kg/mol]+0.21*0.0319988[kg/mol]");p("rhoAmb","pAmb*Mair/(R_const*Tamb)");p("Vinit","2*pi/3*Rinit^2*Hinit");
 // Moving boundary sweeping stationary air: h_air*Mdot-p_air*Vdot
 // equals e_air*Mdot during expansion. Internal energy alone double counts work.
 p("eAir","3.5*R_const*Tamb/Mair");
 m.component().create("c",true);m.component("c").geom().create("g",2);m.component("c").geom("g").axisymmetric(true);
 rect("cu",new String[]{"0","-Dcu"},new String[]{"Rdom","Dcu"});rect("gas",new String[]{"0","0"},new String[]{"Rdom","1[um]"});rect("hot",new String[]{"0","-15[um]"},new String[]{"40[um]","15[um]"});m.component("c").geom("g").run();
 select("surface",1,"-1e-12","Rdom+1e-12[m]","-1e-12","1e-12");select("beam",1,"-1e-12","40[um]+1e-12[m]","-1e-12","1e-12");select("bottom",1,"-1e-12","Rdom+1e-12[m]","-Dcu-1e-12[m]","-Dcu+1e-12[m]");
 for(String group:new String[]{"gasv","boundv","solidv"})m.component("c").variable().create(group);
 m.component("c").variable("boundv").selection().geom("g",1);m.component("c").variable("boundv").selection().named("surface");m.component("c").variable("solidv").selection().named("g_cu_dom");
 for(String[] op:new String[][]{{"intSurf","surface"},{"intCu","g_cu_dom"}}){m.component("c").cpl().create(op[0],"Integration");if(op[0].equals("intSurf"))m.component("c").cpl(op[0]).selection().geom("g",1);m.component("c").cpl(op[0]).selection().named(op[1]);m.component("c").cpl(op[0]).set("axisym",true);m.component("c").cpl(op[0]).set("intorder",8);}
 m.component("c").physics().create("plas","ColdPlasma","g");m.component("c").physics("plas").selection().named("g_gas_dom");m.component("c").physics("plas").prop("DiffusionModel").set("DiffusionModel","GlobalModel");m.component("c").physics("plas").prop("Reactor").set("ReactorType","ClosedReactor");
 m.component("c").physics("plas").prop("Stabilization").set("SourceStabilization",false);m.component("c").physics("plas").prop("Stabilization").set("ReactionSourceStabilization",false);
 Chemistry.apply(m,root+"/data");GasFields.apply(m,false);RecombinationEnergy.apply(m);ThermalBalance.apply(m);NeutralCollisionCount.apply(m);
 for(String n:Chemistry.NAMES){if(!n.equals("N2"))v("plas.w"+n,"w"+n);m.component("c").physics("plas").feature(n).set("n0",n.equals("O2")?"0.21*pAmb/(k_B_const*Tamb)":n.endsWith("_1p")||n.endsWith("_1m")?"1e7[1/m^3]":"1[1/m^3]");}
 m.component("c").physics("plas").feature("N2_1p").set("n0","seedNe-2e7[1/m^3]");m.component("c").physics("plas").feature("N2_1p").set("InitIon",false);
 m.component("c").physics("plas").feature("pes1").set("Tgm","Tg");m.component("c").physics("plas").feature("pes1").set("Tgm0","Tamb");m.component("c").physics("plas").feature("pes1").set("p0","pAmb");m.component("c").physics("plas").feature("pes1").set("ebarinitgm","1.5*k_B_const*Tamb/e_const");
 v("Tg","exp(logGasT)*1[K]");v("Tgas","Tg");v("rhoP","plas.rho");v("urP","0[m/s]");v("uzP","0[m/s]");v("Dgas","1e-5[m^2/s]");v("Rp","Rinit*exp(logRadius)");v("Hp","Hinit*exp(logHeight)");v("Vp","2*pi/3*Rp^2*Hp");v("Mgas","rhoP*Vp");v("vRadius","momentumScale*pRadius/Mgas");v("vHeight","momentumScale*pHeight/Mgas");v("volumeRate","2*vRadius/Rp+vHeight/Hp");v("Vdot","Vp*volumeRate");v("Msweep","rhoAmb*max(Vdot,0[m^3/s])");v("Kinetic","Mgas*(2*vRadius^2+vHeight^2)/10");v("pPl","plas.pA+pe");v("Req","(3*Vp/(4*pi))^(1/3)");
 v("pulse","exp(-4*log(2)*((t-tp)/tau)^2)");v("Pincident","Ein*sqrt(4*log(2)/pi)/tau*pulse");v("Ipeak","2*Ein/(pi*w0^2)*sqrt(4*log(2)/pi)/tau");v("beamCoverage","1-exp(-2*(Rp/w0)^2)");v("laserTau","alphaLaser*Hp");v("beamTransmission","exp(-laserTau)");
 // Optical depths of seed electrons can be <1e-16. Avoid subtracting two
 // rounded identical numbers: that would erase heating per electron entirely.
 v("beamAbsorptance","if(laserTau<1e-4,laserTau*(1-laserTau/2+laserTau^2/6-laserTau^3/24),1-exp(-laserTau))");v("Plaser","Pincident*beamCoverage*beamAbsorptance*(1+(1-Acu)*beamTransmission)");
 // Common mean chord in a sphere-equivalent volume; no adjustable escape scale.
 for(int li=1;li<=8;li++){String n="tauLine"+li;v(n,m.component("c").variable("gasv").get(n).replace("*w0","*Req"));v("beta"+li,"if("+n+"<0.01,1-3*"+n+"/4+2*"+n+"^2/5,3/(4*"+n+")*(1-1/(2*"+n+"^2)+(1/"+n+"+1/(2*"+n+"^2))*exp(-2*"+n+")))");}
 String weighted="0[mol/(kg*m^3)]";
 for(String n:Chemistry.NAMES){
  if(n.endsWith("_1p")||n.endsWith("_1m"))weighted+="+plas.n_w"+n+"/plas.M_w"+n;
  if(n.equals("N")||n.equals("O")||(n.startsWith("Cu")&&!n.equals("Cu_1p"))){String tag="elasticAtom"+n;m.component("c").physics("plas").create(tag,"ElectronImpactReaction",2);m.component("c").physics("plas").feature(tag).set("formula","e+"+n+"=>e+"+n);m.component("c").physics("plas").feature(tag).set("type","Elastic");m.component("c").physics("plas").feature(tag).set("kf","kmCu*N_A_const");m.component("c").physics("plas").feature(tag).set("mratio","me_const*N_A_const/plas.M_w"+n);}
 }
 v("Qei","3*me_const*N_A_const*plas.ne*k_B_const*(TeK-Tgas)*2.91e-12[m^3/s]*coulombLog/(TeV/1[V])^1.5*("+weighted+")");
 // The target uses the same inherited latent-heat and vapor-pressure closure.
 String[][] solids={{"fL","0.5*(1+(T-Tm)/sqrt((T-Tm)^2+100[K^2]))"},{"cpCu","385[J/(kg*K)]*(1-fL)+572[J/(kg*K)]*fL+Lm*50[K^2]/((T-Tm)^2+100[K^2])^1.5"},{"hCond","385[J/(kg*K)]*(T-Tamb)+187[J/(kg*K)]*0.5*(T-Tamb+sqrt((T-Tm)^2+100[K^2])-sqrt((Tamb-Tm)^2+100[K^2]))+Lm*(fL-0.5*(1+(Tamb-Tm)/sqrt((Tamb-Tm)^2+100[K^2])))"},{"kCu","385[W/(m*K)]*(1-fL)+165[W/(m*K)]*fL"}};
 for(String[] row:solids)m.component("c").variable("solidv").set(row[0],row[1]);
 m.component("c").material().create("copper","Common");m.component("c").material("copper").selection().named("g_cu_dom");for(String[] row:new String[][]{{"density","rhoCu"},{"heatcapacity","cpCu"},{"thermalconductivity","kCu"}})m.component("c").material("copper").propertyGroup("def").set(row[0],row[1]);
 b("contact","flc2hs(Rp-r,1[um])");b("Qsurface","Acu*Ipeak*pulse*exp(-2*(r/w0)^2)*(1-contact*(1-beamTransmission))");b("mdotReturn","contact*rhoCopper*sqrt(k_B_const*Tg/(2*pi*Mcu/N_A_const))");b("mdot","mdotEvap-mdotReturn");
 String sum="0",exc="0[V]",returnExc="0[V]";
 for(int i=0;i<SurfaceStateFeed.CU.length;i++){String n=SurfaceStateFeed.CU[i];String e=i==7?"(workCu-"+SurfaceStateFeed.ENERGY[i]+"[V])*e_const/(k_B_const*T)":"-"+SurfaceStateFeed.ENERGY[i]+"[V]*e_const/(k_B_const*T)";b("emitWeight_"+n,SurfaceStateFeed.WEIGHT[i]+"*exp("+e+")");sum+="+emitWeight_"+n;}
 b("emitPartition",sum);
 for(int i=0;i<SurfaceStateFeed.CU.length;i++){String n=SurfaceStateFeed.CU[i];b("emitFraction_"+n,"emitWeight_"+n+"/emitPartition");exc+="+emitFraction_"+n+"*"+SurfaceStateFeed.ENERGY[i]+"[V]";returnExc+="+plas.n_w"+n+"/max(nCuTotal,1[1/m^3])*"+SurfaceStateFeed.ENERGY[i]+"[V]";b("source_"+n,"mdotEvap*emitFraction_"+n+"-mdotReturn*plas.n_w"+n+"/max(nCuTotal,1[1/m^3])");}
 b("emitMeanEnergy",exc);v("returnMeanEnergy",returnExc);
 b("electronSourceEnergy","2*k_B_const*T*mdotEvap*N_A_const/Mcu*emitFraction_Cu_1p-2.5*k_B_const*TeK*mdotReturn*N_A_const/Mcu*nCu_1p/max(nCuTotal,1[1/m^3])");
 b("Qmass","mdotEvap*(Bcu+3*R_const*T/Mcu+F_const*emitMeanEnergy/Mcu)-mdotReturn*(Bcu+2.5*R_const*Tg/Mcu+F_const*returnMeanEnergy/Mcu)+electronSourceEnergy");
 b("Qexchange","contact*2.5*k_B_const*nHeavy*sqrt(8*k_B_const*Tg/(pi*Mair/N_A_const))/4*(Tg-T)");
 v("Msource","intSurf(mdot)+Msweep");v("Psource","intSurf(Qmass-Qexchange)");v("PeSource","intSurf(electronSourceEnergy)");
 nativeVar("pes1","plas.Vol_gm","Vp");nativeVar("pes1","plas.mfinGm","Msource");
 // Native x0 of ions uses Nn0. It must not evaluate at the default algebraic
 // logGasT=0 before consistent initialization (that would mean 1 K).
 nativeVar("pes1","plas.Nn0","pAmb/(k_B_const*Tamb)");
 // Native global fractions already contain -w_i*Msource/V. Add physical sources.
 for(String n:Chemistry.NAMES){if(n.equals("N2"))continue;String source=n.equals("O2")?"Msweep*(0.21*0.0319988[kg/mol]/Mair)/Vp":n.startsWith("Cu")?"intSurf(source_"+n+")/Vp":"0[kg/(m^3*s)]";nativeVar(n,"plas.R_w"+n,"("+ConservativeTrace.rate(m,n)+")+("+source+")/plas.M_w"+n);}
 // Conserved rho*V rather than rho in an expanding volume.
 for(String[] row:m.component("c").physics("plas").feature("pes1").featureInfo("info").getInfoTable("Weak"))if(row[1].contains("d(plas.rho,t)*test(plas.pA)"))nativeVar("pes1",row[2],"(d(plas.rho,t)+plas.rho*volumeRate)*test(plas.pA)");
 m.component("c").physics("plas").feature("pes1").set("Pgm","Plaser+PeSource-Vp*(QeRR+QeAttach+Qff+Qei+(Ue+pe)*volumeRate)");
 m.component("c").physics().create("ht","HeatTransfer","g");m.component("c").physics("ht").selection().named("g_cu_dom");m.component("c").physics("ht").feature("init1").set("Tinit","Tamb");m.component("c").physics("ht").create("laser","HeatFluxBoundary",1);m.component("c").physics("ht").feature("laser").selection().named("surface");m.component("c").physics("ht").feature("laser").set("q0","Qsurface-Qmass+Qexchange");m.component("c").physics("ht").create("base","TemperatureBoundary",1);m.component("c").physics("ht").feature("base").selection().named("bottom");m.component("c").physics("ht").feature("base").set("T0","Tamb");
 m.component("c").physics().create("bulk","GlobalEquations","g");
 String[] names={"logRadius","logHeight","pRadius","pHeight","gasEnergy","logGasT","absorbedEnergy","radiatedEnergy","ambientWork","entrainedEnergy","removedMass"};
 String[] eq={"logRadiust-vRadius/Rp","logHeightt-vHeight/Hp","pRadiust-5*(pPl-pAmb)*Vp/(Rp*momentumScale)","pHeightt-5*(pPl-pAmb)*Vp/(Hp*momentumScale)","gasEnergyt-(Psource+Plaser-Qrad*Vp-pAmb*Vdot+eAir*Msweep)/energyScale","(gasEnergy*energyScale-Vp*(CvVol*Tg+Uchem+Ue)-Kinetic)/energyScale","absorbedEnergyt-(intSurf(Qsurface)+Plaser)/energyScale","radiatedEnergyt-Qrad*Vp/energyScale","ambientWorkt-pAmb*Vdot/energyScale","entrainedEnergyt-eAir*Msweep/energyScale","removedMasst-intSurf(mdot)/mScale"};
 String[] init={"0","0","0","0","2.5*pAmb*Vinit/energyScale","log(Tamb/1[K])","0","0","0","0","0"};
 m.component("c").physics("bulk").feature("ge1").set("name",names);m.component("c").physics("bulk").feature("ge1").set("equation",eq);m.component("c").physics("bulk").feature("ge1").set("initialValueU",init);
 m.component("c").mesh().create("mesh");m.component("c").mesh("mesh").feature("size").set("custom",true);m.component("c").mesh("mesh").feature("size").set("hmax","15[um]");m.component("c").mesh("mesh").feature("size").set("hmin","0.3[um]");m.component("c").mesh("mesh").feature("size").set("hgrad",1.3);
 m.component("c").mesh("mesh").create("hotSize","Size");m.component("c").mesh("mesh").feature("hotSize").selection().named("g_hot_dom");m.component("c").mesh("mesh").feature("hotSize").set("custom",true);m.component("c").mesh("mesh").feature("hotSize").set("hmaxactive",true);m.component("c").mesh("mesh").feature("hotSize").set("hmax","0.5[um]");m.component("c").mesh("mesh").create("tri","FreeTri");m.component("c").mesh("mesh").run();
 m.study().create("std");m.study("std").create("time","Transient");m.study("std").feature("time").set("tlist","range(0,1[ns],tEnd)");m.study("std").feature("time").set("rtol",1e-4);m.study("std").createAutoSequences("sol");
 m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","0.5[ns]");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");
 // Automatic Jacobian scaling of vanishing neutral traces gave temperature
 // scales near 1e18 K. Use physical scales, retaining tiny ion absolute errors.
 for(String f:m.sol("sol1").feature("v1").feature().tags()){
  String scale=f.equals("c_T")?"1000":f.equals("c_En")?"1e7":f.equals("c_plas_pA")?"1e5":f.equals("c_ODE1")?"1":f.startsWith("c_plas_")&&f.endsWith("_w")?(f.equals("c_plas_O2_w")?"1":"1e-20"):null;
  if(scale!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",scale);}
  System.out.println("FIELD "+f+" SCALE "+scale);
 }
 m.sol("sol1").feature("t1").set("atolglobal","1e-7");
 m.label("Reduced native global Plasma, expanding ellipsoid and target heat; not spatial/sheath validation");m.save(root+"/models/"+caseName+"_ready.mph");m.save(root+"/src/"+caseName+"_model.java","java");System.out.println("GLOBAL PLUME PREPARED: native reactions, quasineutrality, 2T, expansion, coupled target; no solution yet");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
