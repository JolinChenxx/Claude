import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Axial conservative EOS coupling test. No Plasma or synthetic light claim.
 * Copper/air are conserved, energy is inverted through the tabulated nuclear EOS.
 * The laser optical closure is the inherited 10% target absorption distributed
 * over a 20nm dense-material layer; no fitted volumetric ejection source.
 */
public class BuildAxisymmetricHEM {
 static void box(String tag,int dim,double r0,double r1,double z0,double z1){m.component("c").selection().create(tag,"Box");m.component("c").selection(tag).set("entitydim",dim);m.component("c").selection(tag).set("xmin",r0+"[um]");m.component("c").selection(tag).set("xmax",r1+"[um]");m.component("c").selection(tag).set("ymin",z0+"[um]");m.component("c").selection(tag).set("ymax",z1+"[um]");m.component("c").selection(tag).set("condition","inside");}
 static Model m;static void p(String n,String e){m.param().set(n,e);}static void v(String n,String e){m.component("c").variable("state").set(n,e);}
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.create("axisymmetricHEM");String root=a[0],name=a[1];
  p("rhoRef","8942.230330721954[kg/m^3]");p("rhoS","1000[kg/m^3]");p("uS","1000[m/s]");p("eS","1e6[J/kg]");p("Tamb","293.15[K]");p("pAmb","101325[Pa]");p("Ra","287[J/(kg*K)]");p("rhoAmb","pAmb/(Ra*Tamb)");
  p("Ein","400[uJ]");p("tau","60[ns]");p("tp","120[ns]");p("w0","11[um]");p("Acu","0.1");p("tEnd",a.length>2?a[2]:"100[ns]");p("Tcrit","9030.31715144617[K]");p("rhocrit","1917.4313[kg/m^3]");
  m.component().create("c",true);m.component("c").geom().create("g",2);m.component("c").geom("g").axisymmetric(true);
  double[] b={-100,-20,-5,-1,-.2,0,.2,1,5,20,100,400,1500};int[] counts={8,12,20,40,20,20,40,40,30,30,15,15};
  double[] rb={0,60,500};int[] rn={30,24};
  for(int k=0;k<2;k++)for(int i=0;i<b.length-1;i++){String f="seg"+k+"_"+i;m.component("c").geom("g").create(f,"Rectangle");m.component("c").geom("g").feature(f).set("pos",new String[]{rb[k]+"[um]",b[i]+"[um]"});m.component("c").geom("g").feature(f).set("size",new String[]{(rb[k+1]-rb[k])+"[um]",(b[i+1]-b[i])+"[um]"});m.component("c").geom("g").feature(f).set("selresult",true);}
  m.component("c").geom("g").run();
  box("top",1,-.001,500.001,1499.999,1500.001);box("bottom",1,-.001,500.001,-100.001,-99.999);box("right",1,499.999,500.001,-100.001,1500.001);box("axis",1,-.001,.001,-100.001,1500.001);
  m.component("c").selection().create("far","Union");m.component("c").selection("far").set("entitydim",1);m.component("c").selection("far").set("input",new String[]{"top","bottom","right"});
  m.func().create("cuEOS","Interpolation");m.func("cuEOS").set("source","file");m.func("cuEOS").set("filename",root+"/data/Cu_HEM_20261110/eos_comsol.txt");m.func("cuEOS").set("nargs",2);
  String[] names={"cuP","cuE","cuCv","cuCs","cuXv","cuAg","cuPhase"};String[][] fun=new String[names.length][2];for(int i=0;i<names.length;i++)fun[i]=new String[]{names[i],""+(i+1)};m.func("cuEOS").set("funcs",fun);m.func("cuEOS").set("interp","linear");m.func("cuEOS").set("extrap","const");m.func("cuEOS").importData();
  m.component("c").variable().create("state");
  v("T","1[K]*exp(theta)");v("rhoC","rhoS*exp(wC)");v("rhoA","rhoAmb*exp(wA)");v("rho","rhoC+rhoA");v("u","rhoS*uS*qm/rho");v("ur","rhoS*uS*qr/rho");v("axisWeight","max(r,1e-12[m])/500[um]");v("Etot","rhoS*eS*qE");v("lT","log10(T/1[K])");v("lR","log10(max(rhoC,1e-10[kg/m^3])/1[kg/m^3])");
  v("pCu","cuP(lT,lR)*1[Pa]");v("eCu","cuE(lT,lR)*1[J/kg]");v("xV","min(1,max(0,cuXv(lT,lR)))");v("alphaG","min(1,max(1e-8,cuAg(lT,lR)))");v("pGas","pCu+rhoA*Ra*T/alphaG");
  v("rhoVapor","rhoC*xV");v("rhoCond","rhoC*(1-xV)");v("Einternal","rhoC*eCu+rhoA*2.5*Ra*T");
  v("cs","max(cuCs(lT,lR)*1[m/s],sqrt(1.4*Ra*T)*alphaG)");v("shockSensor","min(1,h*sqrt(d(pGas,r)^2+d(pGas,z)^2+1[Pa^2/m^2])/(abs(pGas)+pAmb))");v("Dnum","0.15*h*(sqrt(u^2+ur^2+1e-8[m^2/s^2])+cs*shockSensor)");v("kcond","(385*(1-flc2hs(T-1358[K],10[K]))+165*flc2hs(T-1358[K],10[K]))*1[W/(m*K)]");v("kMix","(1-alphaG)*kcond+alphaG*0.026[W/(m*K)]*(T/300[K])^0.7");
  v("solidFrac","(1-xV)*(1-flc2hs(T-1358[K],10[K]))");v("drag","rho*solidFrac^2*1e10[1/s]");
  v("denseOptical","min(1,(1-alphaG)+flc2hs(T-Tcrit,100[K])*rhoC^2/(rhoC^2+rhocrit^2))");v("alphaOpt","denseOptical/20[nm]");v("Ipeak","2*Ein/(pi*w0^2)*sqrt(4*log(2)/pi)/tau");v("pulse","exp(-4*log(2)*((t-tp)/tau)^2)");v("Qabs","Acu*alphaOpt*Ipeak*exp(-2*(r/w0)^2)*light");v("Qreflected","(1-Acu)*alphaOpt*Ipeak*exp(-2*(r/w0)^2)*light");
  v("sinit","0.5*(1-tanh(z/0.01[um]))");v("rhoCi","rhoRef*sinit+1e-10[kg/m^3]");v("rhoAi","rhoAmb*(1-sinit)+1e-12[kg/m^3]");v("Ei","rhoCi*cuE(log10(Tamb/1[K]),log10(rhoCi/1[kg/m^3]))*1[J/kg]+rhoAi*2.5*Ra*Tamb");
  m.component("c").physics().create("hydro","GeneralFormPDE","g",new String[]{"wC","wA","qr","qm","qE"});
  m.component("c").physics("hydro").feature("gfeq1").set("da",new String[]{"axisWeight*exp(wC)","axisWeight*exp(wA)","axisWeight","axisWeight","axisWeight"});
  m.component("c").physics("hydro").feature("gfeq1").set("Ga",new String[][]{{"axisWeight*(rhoC*ur-Dnum*d(rhoC,r))/rhoS","axisWeight*(rhoC*u-Dnum*d(rhoC,z))/rhoS"},{"axisWeight*(rhoA*ur-Dnum*d(rhoA,r))/rhoAmb","axisWeight*(rhoA*u-Dnum*d(rhoA,z))/rhoAmb"},{"axisWeight*((rho*ur^2+pGas-pAmb)/(rhoS*uS)-Dnum*d(qr,r))","axisWeight*(rho*ur*u/(rhoS*uS)-Dnum*d(qr,z))"},{"axisWeight*(rho*u*ur/(rhoS*uS)-Dnum*d(qm,r))","axisWeight*((rho*u^2+pGas-pAmb)/(rhoS*uS)-Dnum*d(qm,z))"},{"axisWeight*(((Etot+pGas)*ur-kMix*d(T,r))/(rhoS*eS)-Dnum*d(qE,r))","axisWeight*(((Etot+pGas)*u-kMix*d(T,z))/(rhoS*eS)-Dnum*d(qE,z))"}});
  m.component("c").physics("hydro").feature("gfeq1").set("f",new String[]{"0","0","(pGas-pAmb)/(rhoS*uS*500[um])-axisWeight*drag*ur/(rhoS*uS)","-axisWeight*drag*u/(rhoS*uS)","axisWeight*Qabs/(rhoS*eS)"});
  for(String[] q:new String[][]{{"wC","log(rhoCi/rhoS)"},{"wA","log(rhoAi/rhoAmb)"},{"qr","0"},{"qm","0"},{"qE","Ei/(rhoS*eS)"}})m.component("c").physics("hydro").feature("init1").set(q[0],q[1]);
  m.component("c").physics().create("thermal","GeneralFormPDE","g",new String[]{"theta"});m.component("c").physics("thermal").feature("gfeq1").set("da","0");m.component("c").physics("thermal").feature("gfeq1").set("Ga",new String[]{"0","0"});m.component("c").physics("thermal").feature("gfeq1").set("f","axisWeight*(Etot-rho*(u^2+ur^2)/2-Einternal)/(rhoS*eS)");m.component("c").physics("thermal").feature("init1").set("theta","log(Tamb/1[K])");
  m.component("c").physics().create("optics","GeneralFormPDE","g",new String[]{"light"});m.component("c").physics("optics").feature("gfeq1").set("da","0");m.component("c").physics("optics").feature("gfeq1").set("Ga",new String[]{"0","axisWeight*(-light-0.005*h*d(light,z))"});m.component("c").physics("optics").feature("gfeq1").set("f","-axisWeight*alphaOpt*light");m.component("c").physics("optics").feature("init1").set("light","pulse*exp(-max(-z,0[m])/20[nm])");
  m.component("c").physics("optics").create("incident","DirichletBoundary",1);m.component("c").physics("optics").feature("incident").selection().named("top");m.component("c").physics("optics").feature("incident").set("r","pulse");
  m.component("c").physics("hydro").create("farbc","DirichletBoundary",1);m.component("c").physics("hydro").feature("farbc").selection().named("far");m.component("c").physics("hydro").feature("farbc").set("r",new String[]{"log(rhoCi/rhoS)","log(rhoAi/rhoAmb)","0","0","Ei/(rhoS*eS)"});
  m.component("c").physics("hydro").create("axisbc","DirichletBoundary",1);m.component("c").physics("hydro").feature("axisbc").selection().named("axis");m.component("c").physics("hydro").feature("axisbc").set("r",new String[]{"0","0","0","0","0"});m.component("c").physics("hydro").feature("axisbc").set("useDirichletCondition",new int[]{0,0,1,0,0});
  m.component("c").mesh().create("mesh");
  for(int i=0;i<counts.length;i++){
   String f="zEdges"+i;box(f,1,-.001,500.001,b[i]-.001,b[i+1]+.001);
   m.component("c").selection().create(f+"vert","Box");m.component("c").selection(f+"vert").set("entitydim",1);m.component("c").selection(f+"vert").set("xmin","-0.001[um]");m.component("c").selection(f+"vert").set("xmax","0.001[um]");m.component("c").selection(f+"vert").set("ymin",(b[i]-.001)+"[um]");m.component("c").selection(f+"vert").set("ymax",(b[i+1]+.001)+"[um]");m.component("c").selection(f+"vert").set("condition","inside");
   m.component("c").mesh("mesh").create(f,"Edge");m.component("c").mesh("mesh").feature(f).selection().named(f+"vert");m.component("c").mesh("mesh").feature(f).create("dist","Distribution");m.component("c").mesh("mesh").feature(f).feature("dist").set("numelem",counts[i]);
  }
  for(int k=0;k<2;k++){String f="radial"+k;box(f,1,rb[k]-.001,rb[k+1]+.001,-100.001,-99.999);m.component("c").mesh("mesh").create(f,"Edge");m.component("c").mesh("mesh").feature(f).selection().named(f);m.component("c").mesh("mesh").feature(f).create("dist","Distribution");m.component("c").mesh("mesh").feature(f).feature("dist").set("numelem",rn[k]);}
  m.component("c").mesh("mesh").create("map","Map");m.component("c").mesh("mesh").run();
  for(String ph:new String[]{"hydro","thermal","optics"})m.component("c").physics(ph).prop("ShapeProperty").set("order",1);
  m.component("c").cpl().create("intAll","Integration");m.component("c").cpl("intAll").selection().all();m.component("c").cpl("intAll").set("opname","intAll");m.component("c").cpl("intAll").set("axisym",true);
  m.study().create("std");m.study("std").create("time","Transient");m.study("std").feature("time").set("tlist","range(0,2[ns],tEnd)");m.study("std").feature("time").set("rtol",0.002);m.study("std").setGenPlots(false);m.study("std").createAutoSequences("sol");
  m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","0.2[ns]");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","1e-5[ns]");
  for(String f:m.sol("sol1").feature("v1").feature().tags()){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",f.equals("c_qE")||f.equals("c_qm")||f.equals("c_qr")?"1e-3":"1");}
  for(String f:m.sol("sol1").feature("t1").feature().tags())if(m.sol("sol1").feature("t1").feature(f).getType().equals("FullyCoupled")){m.sol("sol1").feature("t1").feature(f).set("maxiter",15);m.sol("sol1").feature("t1").feature(f).set("jtech","minimal");}
  m.label("Axisymmetric HEM copper-air EOS/flux predictor; no native plasma yet; no synthetic image");m.save(root+"/models/"+name+"_ready.mph");System.out.println("AXISYMMETRIC HEM READY "+name);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
