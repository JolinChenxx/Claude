import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Axial conservative EOS coupling test. No Plasma or synthetic light claim.
 * Copper/air are conserved, energy is inverted through the tabulated nuclear EOS.
 * The laser optical closure is the inherited 10% target absorption distributed
 * over a 20nm dense-material layer; no fitted volumetric ejection source.
 */
public class BuildAxialHEM {
 static Model m;static void p(String n,String e){m.param().set(n,e);}static void v(String n,String e){m.component("c").variable("state").set(n,e);}
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.create("axialHEM");String root=a[0],name=a[1];
  p("rhoRef","8942.230330721954[kg/m^3]");p("rhoS","1000[kg/m^3]");p("uS","1000[m/s]");p("eS","1e6[J/kg]");p("Tamb","293.15[K]");p("pAmb","101325[Pa]");p("Ra","287[J/(kg*K)]");p("rhoAmb","pAmb/(Ra*Tamb)");
  p("Ein","400[uJ]");p("tau","60[ns]");p("tp","120[ns]");p("w0","11[um]");p("Acu","0.1");p("tEnd",a.length>2?a[2]:"100[ns]");p("Tcrit","9030.31715144617[K]");p("rhocrit","1917.4313[kg/m^3]");
  m.component().create("c",true);m.component("c").geom().create("g",1);
  double[] b={-20,-5,-1,-.2,0,.2,1,5,20,100};int[] counts={20,40,80,40,40,80,80,100,160};
  for(int i=0;i<b.length-1;i++){String s="seg"+i;m.component("c").geom("g").create(s,"Interval");m.component("c").geom("g").feature(s).set("p",new String[]{b[i]+"[um]",b[i+1]+"[um]"});m.component("c").geom("g").feature(s).set("selresult",true);}
  m.component("c").geom("g").run();
  m.func().create("cuEOS","Interpolation");m.func("cuEOS").set("source","file");m.func("cuEOS").set("filename",root+"/data/Cu_HEM_20261110/eos_comsol.txt");m.func("cuEOS").set("nargs",2);
  String[] names={"cuP","cuE","cuCv","cuCs","cuXv","cuAg","cuPhase"};String[][] fun=new String[names.length][2];for(int i=0;i<names.length;i++)fun[i]=new String[]{names[i],""+(i+1)};m.func("cuEOS").set("funcs",fun);m.func("cuEOS").set("interp","linear");m.func("cuEOS").set("extrap","const");m.func("cuEOS").importData();
  m.component("c").variable().create("state");
  v("T","1[K]*exp(theta)");v("rhoC","rhoS*exp(wC)");v("rhoA","rhoAmb*exp(wA)");v("rho","rhoC+rhoA");v("u","rhoS*uS*qm/rho");v("Etot","rhoS*eS*qE");v("lT","log10(T/1[K])");v("lR","log10(max(rhoC,1e-10[kg/m^3])/1[kg/m^3])");
  v("pCu","cuP(lT,lR)*1[Pa]");v("eCu","cuE(lT,lR)*1[J/kg]");v("xV","min(1,max(0,cuXv(lT,lR)))");v("alphaG","min(1,max(1e-8,cuAg(lT,lR)))");v("pGas","pCu+rhoA*Ra*T/alphaG");
  v("rhoVapor","rhoC*xV");v("rhoCond","rhoC*(1-xV)");v("Einternal","rhoC*eCu+rhoA*2.5*Ra*T");
  v("cs","max(cuCs(lT,lR)*1[m/s],sqrt(1.4*Ra*T)*alphaG)");v("Dnum","0.15*h*(sqrt(u^2+1[m^2/s^2])+cs)");v("kcond","(385*(1-flc2hs(T-1358[K],10[K]))+165*flc2hs(T-1358[K],10[K]))*1[W/(m*K)]");v("kMix","(1-alphaG)*kcond+alphaG*0.026[W/(m*K)]*(T/300[K])^0.7");
  v("solidFrac","(1-xV)*(1-flc2hs(T-1358[K],10[K]))");v("drag","rho*solidFrac^2*1e10[1/s]");
  v("denseOptical","min(1,(1-alphaG)+flc2hs(T-Tcrit,100[K])*rhoC^2/(rhoC^2+rhocrit^2))");v("alphaOpt","denseOptical/20[nm]");v("Ipeak","2*Ein/(pi*w0^2)*sqrt(4*log(2)/pi)/tau");v("pulse","exp(-4*log(2)*((t-tp)/tau)^2)");v("Qabs","Acu*alphaOpt*Ipeak*light");v("Qreflected","(1-Acu)*alphaOpt*Ipeak*light");
  v("sinit","0.5*(1-tanh(x/0.01[um]))");v("rhoCi","rhoRef*sinit+1e-10[kg/m^3]");v("rhoAi","rhoAmb*(1-sinit)+1e-12[kg/m^3]");v("Ei","rhoCi*cuE(log10(Tamb/1[K]),log10(rhoCi/1[kg/m^3]))*1[J/kg]+rhoAi*2.5*Ra*Tamb");
  m.component("c").physics().create("hydro","GeneralFormPDE","g",new String[]{"wC","wA","qm","qE"});
  m.component("c").physics("hydro").feature("gfeq1").set("da",new String[]{"exp(wC)","exp(wA)","1","1"});
  m.component("c").physics("hydro").feature("gfeq1").set("Ga",new String[][]{{"(rhoC*u-Dnum*d(rhoC,x))/rhoS"},{"(rhoA*u-Dnum*d(rhoA,x))/rhoAmb"},{"(rho*u^2+pGas-pAmb)/(rhoS*uS)-Dnum*d(qm,x)"},{"((Etot+pGas)*u-kMix*d(T,x))/(rhoS*eS)-Dnum*d(qE,x)"}});
  m.component("c").physics("hydro").feature("gfeq1").set("f",new String[]{"0","0","-drag*u/(rhoS*uS)","Qabs/(rhoS*eS)"});
  for(String[] x:new String[][]{{"wC","log(rhoCi/rhoS)"},{"wA","log(rhoAi/rhoAmb)"},{"qm","0"},{"qE","Ei/(rhoS*eS)"}})m.component("c").physics("hydro").feature("init1").set(x[0],x[1]);
  m.component("c").physics().create("thermal","GeneralFormPDE","g",new String[]{"theta"});m.component("c").physics("thermal").feature("gfeq1").set("da","0");m.component("c").physics("thermal").feature("gfeq1").set("Ga","0");m.component("c").physics("thermal").feature("gfeq1").set("f","(Etot-rho*u^2/2-Einternal)/(rhoS*eS)");m.component("c").physics("thermal").feature("init1").set("theta","log(Tamb/1[K])");
  m.component("c").physics().create("optics","GeneralFormPDE","g",new String[]{"light"});m.component("c").physics("optics").feature("gfeq1").set("da","0");m.component("c").physics("optics").feature("gfeq1").set("Ga","-light-0.005*h*d(light,x)");m.component("c").physics("optics").feature("gfeq1").set("f","-alphaOpt*light");m.component("c").physics("optics").feature("init1").set("light","pulse*exp(-max(-x,0[m])/20[nm])");
  int nb=b.length;m.component("c").physics("optics").create("incident","DirichletBoundary",0);m.component("c").physics("optics").feature("incident").selection().set(nb);m.component("c").physics("optics").feature("incident").set("r","pulse");
  m.component("c").physics("hydro").create("far","DirichletBoundary",0);m.component("c").physics("hydro").feature("far").selection().set(1,nb);m.component("c").physics("hydro").feature("far").set("r",new String[]{"log(rhoCi/rhoS)","log(rhoAi/rhoAmb)","0","Ei/(rhoS*eS)"});
  m.component("c").mesh().create("mesh");for(int i=0;i<counts.length;i++){String f="edge"+i;m.component("c").mesh("mesh").create(f,"Edge");m.component("c").mesh("mesh").feature(f).selection().named("g_seg"+i+"_dom");m.component("c").mesh("mesh").feature(f).create("dist","Distribution");m.component("c").mesh("mesh").feature(f).feature("dist").set("numelem",counts[i]);}m.component("c").mesh("mesh").run();
  for(String ph:new String[]{"hydro","thermal","optics"})m.component("c").physics(ph).prop("ShapeProperty").set("order",1);
  m.component("c").cpl().create("intAll","Integration");m.component("c").cpl("intAll").selection().all();
  m.study().create("std");m.study("std").create("time","Transient");m.study("std").feature("time").set("tlist","range(0,2[ns],tEnd)");m.study("std").feature("time").set("rtol",0.002);m.study("std").setGenPlots(false);m.study("std").createAutoSequences("sol");
  m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","0.2[ns]");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","1e-5[ns]");
  m.label("Axial HEM copper-air EOS/flux test; no native plasma yet; no synthetic image");m.save(root+"/models/"+name+"_ready.mph");System.out.println("AXIAL HEM READY "+name);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
