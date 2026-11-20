import com.comsol.model.*;import com.comsol.model.util.*;
/** Conservative ALE in fixed reference coordinates: x=(R,Z-s(R,Z,t)).
 * This is a physical moving interface, not a translation of plotted fields.
 * J=1-s_Z; contravariant flux=(J F_r, s_R F_r+F_z-w_z U).
 * da=J and source=-J_t U are the product derivative d(J U)/dt.
 */
public class BuildALE {
 static Model m;static void v(String n,String e){m.component("c").variable("v").set(n,e);}
 static String dr(String q){return "(d("+q+",r)+sR/Jale*d("+q+",z))";}
 static String dz(String q){return "(d("+q+",z)/Jale)";}
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.load("M",a[0]);m.param().set("netEvap","1");m.param().set("aleOn","1");
  boolean column=a.length>9&&a[9].equals("column");m.param().set("columnALE",column?"1":"0");
  if(a.length>2)m.param().set("nuFactor",a[2]);if(a.length>4)m.param().set("meshFactor",a[4]);
  if(a.length>6)m.param().set("Rdom",a[6]);if(a.length>7)m.param().set("Zdom",a[7]);
  m.param().set("nuLocal",a.length>8?a[8]:"0");
  v("YgradALE","sqrt("+dr("Yraw")+"^2+"+dz("Yraw")+"^2+1e-20[1/m^2])");
  v("DfrontALE","nuLocal*h*(speedP+csBound)*exp(-(Yraw/0.02)^2)*(h*YgradALE/(0.02+h*YgradALE))");
  v("Dnum","nuFactor*h*(speedP+csBound)+DfrontALE");
  m.component("c").physics().create("map","GeneralFormPDE","g",new String[]{"sMap"});
  m.component("c").physics("map").selection().all();m.component("c").physics("map").feature("gfeq1").set("da","0");
  m.param().set("meshSmoothZ",a.length>5?a[5]:"1");
  m.component("c").physics("map").feature("gfeq1").set("Ga",new String[]{"-axisWeight*d(sMap,r)","-meshSmoothZ*axisWeight*d(sMap,z)"});
  m.component("c").physics("map").feature("gfeq1").set("f","0");
  m.component("c").physics("map").create("surface","DirichletBoundary",1);m.component("c").physics("map").feature("surface").selection().set(4);
  m.component("c").physics("map").feature("surface").set("r","depth/1[um]");
  m.component("c").physics("map").create("outer","DirichletBoundary",1);m.component("c").physics("map").feature("outer").selection().set(new int[]{2,5,6,7});
  m.component("c").physics("map").feature("outer").set("r","0");m.component("c").physics("map").prop("ShapeProperty").set("order",2);
  v("sALE","1[um]*sMap");v("sR","d(sALE,r)");v("Jale","1-d(sALE,z)");v("wALE","-d(sALE,t)");v("JtALE","d(wALE,z)");
  if(column){
   // Exact monotone column mapping: J_solid=1-depth/Dcu (>0 for depth<Dcu),
   // J_gas=1+depth/Zdom. Avoids quadratic harmonic-displacement overshoots.
   m.component("c").physics().remove("map");
   m.component("c").cpl().create("extDepth","GeneralExtrusion");m.component("c").cpl("extDepth").selection().geom("g",1);m.component("c").cpl("extDepth").selection().set(4);
   m.component("c").cpl("extDepth").set("usesrcmap","off");m.component("c").cpl("extDepth").set("dstmap",new String[]{"r","0"});
   v("columnFactor","if(z>=0,1-z/Zdom,1+z/Dcu)");v("depthColumn","extDepth(depth)");v("depthRateColumn","extDepth(d(depth,t))");
   v("sALE","depthColumn*columnFactor");v("sR","extDepth(dtang(depth,r))*columnFactor");
   v("Jale","if(z>=0,1+depthColumn/Zdom,1-depthColumn/Dcu)");v("wALE","-depthRateColumn*columnFactor");v("JtALE","if(z>=0,depthRateColumn/Zdom,-depthRateColumn/Dcu)");
  }
  v("zPhysical","z-sALE");v("surfaceStretch","sqrt(1+dtang(depth,r)^2)");v("surfaceR","dtang(depth,r)/surfaceStretch");v("surfaceZ","1/surfaceStretch");
  v("pressureWorkALE","-pPl*mdot/8940[kg/m^3]");
  String[] u={"qR","qMr","qMz","qE","qC"};
  String[] fr={"u0*qMr-Dnum*"+dr("qR"),"u0*qMr*qMr/max(qR,1e-9)+(pPl-pAmb)/(rho0*u0)-Dnum*"+dr("qMr"),"u0*qMr*qMz/max(qR,1e-9)-Dnum*"+dr("qMz"),"urP*(qE+pPl/(rho0*e0))-Dnum*"+dr("qE")+"-kGas*"+dr("Tpl")+"/(rho0*e0)","urP*qC-Dnum*"+dr("qC")};
  String[] fz={"u0*qMz-Dnum*"+dz("qR"),"u0*qMr*qMz/max(qR,1e-9)-Dnum*"+dz("qMr"),"u0*qMz*qMz/max(qR,1e-9)+(pPl-pAmb)/(rho0*u0)-Dnum*"+dz("qMz"),"uzP*(qE+pPl/(rho0*e0))-Dnum*"+dz("qE")+"-kGas*"+dz("Tpl")+"/(rho0*e0)","uzP*qC-Dnum*"+dz("qC")};
  String[][] flux=new String[5][2];String[] src=new String[5],da=new String[5];
  for(int i=0;i<5;i++){v("FR"+i,fr[i]);v("FZ"+i,fz[i]);flux[i][0]="axisWeight*Jale*FR"+i;flux[i][1]="axisWeight*(sR*FR"+i+"+FZ"+i+"-wALE*"+u[i]+")";src[i]="-axisWeight*JtALE*"+u[i];da[i]="axisWeight*Jale";}
  src[1]+="+axisWeight*Jale*(pPl-pAmb)/(rho0*u0*max(r,1e-10[m]))";src[3]+="+axisWeight*Jale*(QlaserP-Qrad)/(rho0*e0)";
  m.component("c").physics("flow").feature("gfeq1").set("Ga",flux);m.component("c").physics("flow").feature("gfeq1").set("f",src);
  // General form da is a diagonal coefficient matrix for a multi-field system.
  String[][] dam=new String[5][5];for(int i=0;i<5;i++)for(int j=0;j<5;j++)dam[i][j]=i==j?da[i]:"0";
  m.component("c").physics("flow").feature("gfeq1").set("da","axisWeight*Jale");
  String[] bg={"axisWeight*surfaceStretch*mdot/rho0","axisWeight*surfaceStretch*((pPl-pAmb+mdotEvap*vKn)*surfaceR-mdotReturn*urP)/(rho0*u0)","axisWeight*surfaceStretch*((pPl-pAmb+mdotEvap*vKn)*surfaceZ-mdotReturn*uzP)/(rho0*u0)","axisWeight*surfaceStretch*(Qmass-qExchange+pressureWorkALE)/(rho0*e0)","axisWeight*surfaceStretch*mdot/rho0"};
  m.component("c").physics("flow").feature("abl").set("g",bg);
  v("solidFR","-kCu*"+dr("Ts")+"/(8940[kg/m^3]*e0)");v("solidFZ","-kCu*"+dz("Ts")+"/(8940[kg/m^3]*e0)");
  m.component("c").physics("solid").feature("gfeq1").set("da","axisWeight*Jale");
  m.component("c").physics("solid").feature("gfeq1").set("Ga",new String[]{"axisWeight*Jale*solidFR","axisWeight*(sR*solidFR+solidFZ-wALE*hs)"});
  m.component("c").physics("solid").feature("gfeq1").set("f","-axisWeight*JtALE*hs");
  m.component("c").physics("solid").feature("heat").set("g","axisWeight*(Igas-surfaceStretch*(Qmass-qExchange+pressureWorkALE))/(8940[kg/m^3]*e0)");
  m.component("c").physics("rec").feature("dode1").set("f","mdot*surfaceStretch/8940[kg/m^3]");
  for(String ph:new String[]{"optics","opticsUp"}){
   String q=ph.equals("optics")?"opt":"optUp";String sign=ph.equals("optics")?"-":"";
   m.component("c").physics(ph).feature("gfeq1").set("Ga",new String[]{"0","axisWeight*("+sign+q+"-0.02*h*d("+q+",z)/Jale)"});
   m.component("c").physics(ph).feature("gfeq1").set("f","-axisWeight*Jale*alphaIB*"+q);
  }
  for(String s:m.sol().tags())m.sol(s).clearSolutionData();m.component("c").geom("g").run();m.component("c").mesh("mesh").run();
  m.sol().remove("sol1");m.study("std").createAutoSequences("sol");
  String times="range(0,2[ns],1000[ns]) range(1010[ns],10[ns],2120[ns])";
  m.sol("sol1").feature("t1").set("tlist",times);m.sol("sol1").feature("t1").set("rtol",.002);m.sol("sol1").feature("t1").set("maxorder",1);
  m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");
  m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf",a.length>3?a[3]:"1[ns]");
  m.sol("sol1").feature("t1").set("atolglobalmethod","scaled");m.sol("sol1").feature("t1").set("atolglobalvaluemethod","factor");m.sol("sol1").feature("t1").set("atolglobalfactor",.0001);
  // Automatic DAE scaling estimated depth at 1e8 m from its vanishing initial
  // evaporation derivative. Restore physical scales inherited by the validated LTE.
  for(String f:new String[]{"qR","qMr","qMz","qE","qC","hs","opt","optUp","sMap","depth"}){
   if(column&&f.equals("sMap"))continue;
   m.sol("sol1").feature("v1").feature("c_"+f).set("scalemethod","manual");
   m.sol("sol1").feature("v1").feature("c_"+f).set("scaleval",f.equals("depth")?"1e-6":"1");
  }
  // Time solver defaults (4 Newton iterations, minimal Jacobian updates)
  // repeatedly reject steps across LTE interpolation/phase-change slopes.
  m.sol("sol1").feature("t1").feature("fc1").set("jtech",a.length>10?a[10]:"minimal");
  m.sol("sol1").feature("t1").feature("fc1").set("maxiter",15);
  if(a.length>11){m.sol("sol1").feature("t1").feature("fc1").set("dtech",a[11].equals("auto")?"auto":"const");if(!a[11].equals("auto"))m.sol("sol1").feature("t1").feature("fc1").set("damp",Double.parseDouble(a[11]));m.sol("sol1").feature("t1").feature("fc1").set("maxiter",25);}
  m.label("S4 LTE conservative mapped ALE + net evaporation");m.save(a[1]);m.save(a[1].replace(".mph","_model.java"),"java");
  System.out.println("ALE_READY "+a[1]);ModelUtil.clear();System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
