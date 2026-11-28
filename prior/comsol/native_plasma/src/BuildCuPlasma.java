import com.comsol.model.*;
import com.comsol.model.util.*;
import java.io.*;
import java.util.*;

/** Native transient plasma, compressible gas, molten Cu and real moving mesh. */
public class BuildCuPlasma {
 static Model m;
 static void p(String n,String v){m.param().set(n,v);}
 static void v(String group,String n,String x){m.component("c").variable(group).set(n,x);}
 static void box(String tag,int dim,String xmin,String xmax,String zmin,String zmax){
  m.component("c").selection().create(tag,"Box");
  m.component("c").selection(tag).set("entitydim",dim);
  m.component("c").selection(tag).set("xmin",xmin);m.component("c").selection(tag).set("xmax",xmax);
  m.component("c").selection(tag).set("ymin",zmin);m.component("c").selection(tag).set("ymax",zmax);
  m.component("c").selection(tag).set("condition","inside");
 }
 static void rect(String tag,String[]pos,String[]size){
  m.component("c").geom("g").create(tag,"Rectangle");m.component("c").geom("g").feature(tag).set("pos",pos);m.component("c").geom("g").feature(tag).set("size",size);m.component("c").geom("g").feature(tag).set("selresult",true);
 }
 static void feature(String ph,String tag,String type,int dim,String sel){
  m.component("c").physics(ph).create(tag,type,dim);m.component("c").physics(ph).feature(tag).selection().named(sel);
 }
 public static void main(String[] args){try{
  ModelUtil.initStandalone(false);m=ModelUtil.create("R1");String root=args[0];String name=args[1];
  m.modelPath(root+"/models");m.label("Route 1: Cu melt + compressible gas + native non-LTE Plasma");
  p("Ein","400[uJ]");p("tau","60[ns]");p("tp","120[ns]");p("w0","11[um]");p("Acu","0.1");p("lambdaL","1064[nm]");
  p("Rdom","1[mm]");p("Zdom","1.5[mm]");p("Dcu","100[um]");p("Tamb","293.15[K]");p("pAmb","101325[Pa]");
  p("rhoCu","8960[kg/m^3]");p("Mcu","0.063546[kg/mol]");p("rho0","1[kg/m^3]");p("u0","1000[m/s]");p("e0","1e6[J/kg]");
  p("Rair","R_const/(0.79*0.0280134[kg/mol]+0.21*0.0319988[kg/mol])");p("rhoAmb","pAmb/(Rair*Tamb)");
  p("Bcu","5815069.632191036[J/kg]");p("Tm","1358[K]");p("Lm","204000[J/kg]");p("Lv","5069000[J/kg]");p("Tv","2836[K]");
  p("seedNe","1e9[1/m^3]");p("workCu","4.65[V]");p("ARich","1.20173e6[A/(m^2*K^2)]");p("accom","1");
  p("nuFactor","0.3");p("meshFactor","1");p("meltOn","1");p("chemOn","1");p("tEnd",args.length>2?args[2]:"180[ns]");
  m.component().create("c",true);m.component("c").geom().create("g",2);m.component("c").geom("g").axisymmetric(true);
  rect("cu",new String[]{"0","-Dcu"},new String[]{"Rdom","Dcu"});
  rect("gas",new String[]{"0","0"},new String[]{"Rdom","Zdom"});
  rect("core",new String[]{"0","0"},new String[]{"60[um]","80[um]"});
  rect("mid",new String[]{"0","0"},new String[]{"300[um]","400[um]"});
  rect("meltcore",new String[]{"0","-15[um]"},new String[]{"60[um]","15[um]"});
  m.component("c").geom("g").run();
  box("surface",1,"-1e-10","Rdom+1e-10[m]","-1e-10","1e-10");
  box("beam",1,"-1e-10","60[um]+1e-10[m]","-1e-10","1e-10");
  box("axis",1,"-1e-10","1e-10","-Dcu-1e-10[m]","Zdom+1e-10[m]");
  box("bottom",1,"-1e-10","Rdom+1e-10[m]","-Dcu-1e-10[m]","-Dcu+1e-10[m]");
  box("top",1,"-1e-10","Rdom+1e-10[m]","Zdom-1e-10[m]","Zdom+1e-10[m]");
  box("rightgas",1,"Rdom-1e-10[m]","Rdom+1e-10[m]","-1e-10","Zdom+1e-10[m]");
  box("rightcu",1,"Rdom-1e-10[m]","Rdom+1e-10[m]","-Dcu-1e-10[m]","1e-10");
  m.component("c").selection().create("far","Union");m.component("c").selection("far").set("entitydim",1);m.component("c").selection("far").set("input",new String[]{"top","rightgas"});
  m.component("c").selection().create("fixed","Union");m.component("c").selection("fixed").set("entitydim",1);m.component("c").selection("fixed").set("input",new String[]{"top","rightgas","rightcu","bottom"});
  for(String s:new String[]{"g_cu_dom","g_gas_dom","surface","beam","far"})System.out.println("SELECTION "+s+" "+Arrays.toString(m.component("c").selection(s).entities()));
  for(String group:new String[]{"gasv","solidv","boundv"})m.component("c").variable().create(group);
  m.component("c").variable("gasv").selection().named("g_gas_dom");m.component("c").variable("solidv").selection().named("g_cu_dom");
  m.component("c").variable("boundv").selection().geom("g",1);m.component("c").variable("boundv").selection().named("surface");

  // The conserved gas energy contains sensible, chemical and electron energy.
  m.component("c").physics().create("flow","GeneralFormPDE","g",new String[]{"qR","qMr","qMz","qE"});m.component("c").physics("flow").selection().named("g_gas_dom");
  m.component("c").physics().create("temp","GeneralFormPDE","g",new String[]{"Tg"});m.component("c").physics("temp").selection().named("g_gas_dom");
  m.component("c").physics().create("plas","ColdPlasma","g");m.component("c").physics("plas").selection().named("g_gas_dom");
  Chemistry.apply(m,root+"/data");
  m.component("c").physics("plas").feature("N2_1p").set("InitIon",false);
  m.component("c").physics("plas").feature("N2_1p").set("x0","seedNe*k_B_const*Tamb/pAmb");
  // Native ion initial values use number density; freeze only their initial gas density.
  m.component("c").physics("plas").feature("pes1").featureInfo("info").set("plas.Nn0",new String[]{"pAmb/(k_B_const*Tamb)"});
  for(String ion:new String[]{"N2_1p","O2_1p","N_1p","O_1p","O_1m","O2_1m","Cu_1p"})m.component("c").physics("plas").feature(ion).set("n0",ion.equals("N2_1p")?"seedNe":"1e-24*pAmb/(k_B_const*Tamb)");
  m.component("c").physics("plas").prop("DiffusionModel").set("DiffusionModel","FicksLaw");
  m.component("c").physics("plas").prop("TransportSettings").set("Convection",true);
  m.component("c").physics("plas").prop("ElectronProperties").set("MeanElectronEnergyModel","LocalEnergyApproximationE");
  m.component("c").physics("plas").prop("Stabilization").set("SourceStabilization",false);
  m.component("c").physics("plas").prop("Stabilization").set("ReactionSourceStabilization",false);
  m.component("c").physics("plas").feature("pes1").set("DensityType","DensityType");m.component("c").physics("plas").feature("pes1").set("rho","rhoP");
  m.component("c").physics("plas").feature("pes1").set("T","Tg");m.component("c").physics("plas").feature("pes1").set("pA","pHeavy");
  m.component("c").physics("plas").feature("pes1").set("u",new String[]{"urP","0","uzP"});
  m.component("c").physics("plas").feature("pes1").set("SpecifyElectronDensityAndEnergy","SpecifyMueOnly");m.component("c").physics("plas").feature("pes1").set("mue","e_const/(me_const*nuCollision)");
  m.component("c").physics("plas").feature("init1").set("neinit","seedNe");m.component("c").physics("plas").feature("init1").set("ebarinit","1.5*k_B_const*Tamb/e_const");
  feature("plas","bulkconv","ElectronProductionRate",2,"g_gas_dom");m.component("c").physics("plas").feature("bulkconv").set("Re","-urP*d(plas.ne,r)-uzP*d(plas.ne,z)-plas.ne*divU");
  feature("plas","power","GeneralPowerDeposition",2,"g_gas_dom");m.component("c").physics("plas").feature("power").set("Qgen","Qlaser-QeRR-QeAttach-Qff-urP*d(Ue,r)-uzP*d(Ue,z)-(Ue+pe)*divU-Qei");
  feature("plas","wall","WallDriftDiffusion",1,"surface");m.component("c").physics("plas").feature("wall").set("ThermalEmissionModel","RichardsonLaw");m.component("c").physics("plas").feature("wall").set("lambda_R","1");m.component("c").physics("plas").feature("wall").set("Phi","workCu/1[V]");m.component("c").physics("plas").feature("wall").set("T_s","T");m.component("c").physics("plas").feature("wall").set("th_ebar","2*k_B_const*T/e_const");m.component("c").physics("plas").feature("wall").set("incmig",true);
  feature("plas","float","FloatingPotential",1,"surface");m.component("c").physics("plas").feature("float").set("Q0","qmetal*1[pC]");
  m.component("c").physics().create("charge","GlobalEquations","g");
  m.component("c").physics("charge").feature("ge1").set("name",new String[]{"qmetal"});
  m.component("c").physics("charge").feature("ge1").set("equation",new String[]{"qmetalt-intSurf(Iwall)/1[pC]"});
  m.component("c").physics("charge").feature("ge1").set("initialValueU",new String[]{"0"});
  m.component("c").physics("charge").feature("ge1").set("valueType","real");
  feature("plas","ground","Ground",1,"top");feature("plas","out","Outflow",1,"far");m.component("c").physics("plas").feature("out").set("IncludeIons",true);feature("plas","eout","ElectronOutlet",1,"far");
  feature("plas","feed","HeavySpeciesFlux",1,"surface");m.component("c").physics("plas").feature("feed").set("IncludeConvection",true);

  // Condensed copper: latent heat plus actual Navier-Stokes liquid motion.
  m.component("c").physics().create("melt","LaminarFlow","g");m.component("c").physics("melt").selection().named("g_cu_dom");m.component("c").physics("melt").prop("PhysicalModelProperty").set("Compressibility","Incompressible");
  m.component("c").physics("melt").field("velocity").field("umelt");
  m.component("c").physics("melt").field("velocity").component(new String[]{"urCu","uphiCu","uzCu"});
  m.component("c").physics().create("ht","HeatTransfer","g");m.component("c").physics("ht").selection().named("g_cu_dom");m.component("c").physics("ht").feature("init1").set("Tinit","Tamb");
  v("solidv","fL","0.5*(1+(T-Tm)/sqrt((T-Tm)^2+100[K^2]))");v("solidv","cpCu","385[J/(kg*K)]*(1-fL)+572[J/(kg*K)]*fL+Lm*50[K^2]/((T-Tm)^2+100[K^2])^1.5");
  v("solidv","hCond","385[J/(kg*K)]*(T-Tamb)+187[J/(kg*K)]*0.5*(T-Tamb+sqrt((T-Tm)^2+100[K^2])-sqrt((Tamb-Tm)^2+100[K^2]))+Lm*(fL-0.5*(1+(Tamb-Tm)/sqrt((Tamb-Tm)^2+100[K^2])))");
  v("solidv","kCu","385[W/(m*K)]*(1-fL)+165[W/(m*K)]*fL");v("solidv","darcy","1e13[kg/(m^3*s)]*(1-fL)^2/(fL^3+1e-3)");
  m.component("c").material().create("copper","Common");m.component("c").material("copper").selection().named("g_cu_dom");
  m.component("c").material("copper").propertyGroup("def").set("density","rhoCu");m.component("c").material("copper").propertyGroup("def").set("dynamicviscosity","0.005[Pa*s]");m.component("c").material("copper").propertyGroup("def").set("heatcapacity","cpCu");m.component("c").material("copper").propertyGroup("def").set("thermalconductivity","kCu");
  feature("melt","drag","VolumeForce",2,"g_cu_dom");m.component("c").physics("melt").feature("drag").set("F",new String[]{"-darcy*urCu","0","-darcy*uzCu"});
  feature("melt","surf","BoundaryStress",1,"surface");m.component("c").physics("melt").feature("surf").set("Fbnd",new String[]{"-(pPl-pAmb+recoil)*ncr","0","-(pPl-pAmb+recoil)*ncz"});
  feature("melt","capillary","WeakContribution",1,"surface");m.component("c").physics("melt").feature("capillary").set("weakExpression","-sigmaCu*(dtang(test(urCu),r)+dtang(test(uzCu),z)+test(urCu)/max(r,1e-12[m]))");
  feature("ht","advection","HeatSource",2,"g_cu_dom");m.component("c").physics("ht").feature("advection").set("Q0","-rhoCu*cpCu*(urCu*d(T,r)+uzCu*d(T,z))");
  feature("ht","laser","HeatFluxBoundary",1,"surface");m.component("c").physics("ht").feature("laser").set("q0","Qsurface-Qmass+mdot*hCond+Qexchange-Qthermion");
  feature("ht","base","TemperatureBoundary",1,"bottom");m.component("c").physics("ht").feature("base").set("T0","Tamb");

  // Actual moving spatial mesh; no inherited hand-written coordinate transform.
  m.component("c").common().create("deform","DeformingDomain");m.component("c").common("deform").selection().all();m.component("c").common("deform").set("smoothingType","yeoh");
  m.component("c").common().create("meshSurf","PrescribedNormalMeshVelocity");m.component("c").common("meshSurf").selection().named("surface");m.component("c").common("meshSurf").set("prescribedNormalVelocity","meltOn*(urCu*nr+uzCu*nz-mdot/rhoCu*(ncr*nr+ncz*nz))");
  m.component("c").common().create("meshFixed","FixedBoundary");m.component("c").common("meshFixed").selection().named("fixed");
  m.component("c").common().create("meshAxis","Symmetry");m.component("c").common("meshAxis").selection().named("axis");
  m.component("c").cpl().create("intSurf","Integration");m.component("c").cpl("intSurf").selection().named("surface");
  m.component("c").cpl().create("intGas","Integration");m.component("c").cpl("intGas").selection().named("g_gas_dom");
  m.component("c").cpl().create("intCu","Integration");m.component("c").cpl("intCu").selection().named("g_cu_dom");
  GasFields.apply(m);

  // Laser propagation along z, including reflected return through the plume.
  for(String ph:new String[]{"ld","lu"}){
   String q=ph.equals("ld")?"optD":"optU";String sign=ph.equals("ld")?"-":"";
   m.component("c").physics().create(ph,"GeneralFormPDE","g",new String[]{q});m.component("c").physics(ph).selection().named("g_gas_dom");
   m.component("c").physics(ph).feature("gfeq1").set("da","0");m.component("c").physics(ph).feature("gfeq1").set("Ga",new String[]{"0",sign+q+"-0.02*h*d("+q+",z)"});m.component("c").physics(ph).feature("gfeq1").set("f","-alphaLaser*"+q);
   feature(ph,"input","DirichletBoundary",1,ph.equals("ld")?"top":"surface");m.component("c").physics(ph).feature("input").set("r",ph.equals("ld")?"pulse":"(1-Acu)*optD");
  }
  v("gasv","pulse","exp(-4*log(2)*((t-tp)/tau)^2)");v("gasv","Ipeak","2*Ein/(pi*w0^2)*sqrt(4*log(2)/pi)/tau");v("gasv","Ibeam","Ipeak*exp(-2*(r/w0)^2)");v("gasv","Qlaser","alphaLaser*Ibeam*(optD+optU)");

  String[][] flux={{"rhoP*urP/rho0","rhoP*uzP/rho0"},
   {"(rhoP*urP^2+pPl-pAmb-tauRR)/(rho0*u0)","(rhoP*urP*uzP-tauRZ)/(rho0*u0)"},
   {"(rhoP*urP*uzP-tauRZ)/(rho0*u0)","(rhoP*uzP^2+pPl-pAmb-tauZZ)/(rho0*u0)"},
   {"(urP*(rho0*e0*qE+pPl)-tauRR*urP-tauRZ*uzP-kGas*d(Tg,r)+Hdiffr+e_const*plas.gflux_enr)/(rho0*e0)","(uzP*(rho0*e0*qE+pPl)-tauRZ*urP-tauZZ*uzP-kGas*d(Tg,z)+Hdiffz+e_const*plas.gflux_enz)/(rho0*e0)"}};
  m.component("c").physics("flow").feature("gfeq1").set("Ga",flux);
  m.component("c").physics("flow").feature("gfeq1").set("f",new String[]{"0","(pPl-pAmb-tauPP)/(rho0*u0*max(r,1e-12[m]))+plas.scharge*plas.Etr/(rho0*u0)","plas.scharge*plas.Etz/(rho0*u0)","(Qlaser-Qrad+Qfield)/(rho0*e0)"});
  m.component("c").physics("flow").feature("init1").set("qR","rhoAmb/rho0");m.component("c").physics("flow").feature("init1").set("qE","2.5*pAmb/(rho0*e0)");
  feature("flow","feed","FluxBoundary",1,"surface");m.component("c").physics("flow").feature("feed").set("g",new String[]{"(mdot+rhoP*vnSurf)/rho0","((pPl-pAmb+recoil)*ncr+mdot*urCu+rhoP*urP*vnSurf)/(rho0*u0)","((pPl-pAmb+recoil)*ncz+mdot*uzCu+rhoP*uzP*vnSurf)/(rho0*u0)","(Qmass-Qexchange+QelectronWall+(rho0*e0*qE+pPl)*vnSurf)/(rho0*e0)"});
  feature("flow","axisbc","DirichletBoundary",1,"axis");m.component("c").physics("flow").feature("axisbc").set("r",new String[]{"0","0","0","0"});m.component("c").physics("flow").feature("axisbc").set("useDirichletCondition",new int[]{0,1,0,0});
  feature("flow","farbc","DirichletBoundary",1,"far");m.component("c").physics("flow").feature("farbc").set("r",new String[]{"rhoAmb/rho0","0","0","2.5*pAmb/(rho0*e0)"});
  m.component("c").physics("temp").feature("gfeq1").set("da","0");m.component("c").physics("temp").feature("gfeq1").set("Ga",new String[]{"0","0"});m.component("c").physics("temp").feature("gfeq1").set("f","(rho0*e0*qE-rhoP*(urP^2+uzP^2)/2-Uchem-Ue-CvVol*Tg)/(rho0*e0)");m.component("c").physics("temp").feature("init1").set("Tg","Tamb");

  m.component("c").mesh().create("mesh");m.component("c").mesh("mesh").feature("size").set("custom",true);m.component("c").mesh("mesh").feature("size").set("hmax","80[um]*meshFactor");m.component("c").mesh("mesh").feature("size").set("hmin","0.15[um]*meshFactor");m.component("c").mesh("mesh").feature("size").set("hgrad",1.25);
  String[] ms={"mid","core","meltcore"};String[] mh={"8[um]","2[um]","0.6[um]"};
  for(int i=0;i<ms.length;i++){String tag="sz"+i;m.component("c").mesh("mesh").create(tag,"Size");m.component("c").mesh("mesh").feature(tag).selection().named("g_"+ms[i]+"_dom");m.component("c").mesh("mesh").feature(tag).set("custom",true);m.component("c").mesh("mesh").feature(tag).set("hmaxactive",true);m.component("c").mesh("mesh").feature(tag).set("hmax",mh[i]+"*meshFactor");}
  m.component("c").mesh("mesh").create("edge","Edge");m.component("c").mesh("mesh").feature("edge").selection().named("beam");m.component("c").mesh("mesh").feature("edge").create("size","Size");m.component("c").mesh("mesh").feature("edge").feature("size").set("custom",true);m.component("c").mesh("mesh").feature("edge").feature("size").set("hmax","0.5[um]*meshFactor");m.component("c").mesh("mesh").feature("edge").feature("size").set("hmin","0.1[um]*meshFactor");
  m.component("c").mesh("mesh").create("tri","FreeTri");m.component("c").mesh("mesh").run();
  CouplingFixes.apply(m);
  ThermalBalance.apply(m);
  MeshGraph.velocity(m);
  m.study().create("std");m.study("std").create("time","Transient");m.study("std").feature("time").set("tlist","range(0,2[ns],tEnd)");m.study("std").feature("time").set("rtol",0.003);m.study("std").setGenPlots(false);m.study("std").createAutoSequences("sol");
  m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","0.5[ns]");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");
  for(String f:m.sol("sol1").feature("v1").feature().tags()){
   try{String scale=f.endsWith("Tg")||f.endsWith("T")?"1000":f.endsWith("urCu")||f.endsWith("uzCu")?"1000":f.toLowerCase().contains("disp")?"1e-5":null;if(scale!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",scale);}}catch(Exception ignored){}
  }
  m.save(root+"/models/"+name+"_ready.mph");m.save(root+"/src/"+name+"_model.java","java");
  PrintWriter out=new PrintWriter(root+"/logs/"+name+"_equations.txt");
  for(String ph:new String[]{"plas","flow","melt","ht"})for(String f:m.component("c").physics(ph).feature().tags())try{out.println(ph+"/"+f+" "+Arrays.deepToString(m.component("c").physics(ph).feature(f).featureInfo("info").getInfoTable("Weak")));}catch(Exception ignored){}
  out.close();System.out.println("READY "+name);System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
