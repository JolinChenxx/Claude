import com.comsol.model.*;import com.comsol.model.util.*;import java.io.*;
public class ExportS4 {
 static Model m;static boolean ale=false;static String only="";static int[] uniqueIds;
 static void calc(String tag,String type,int[] sel,String[] ex,String p)throws Exception{
  if(!only.isEmpty()&&!only.equals(tag))return;
  if(!ale)for(int i=0;i<ex.length;i++)ex[i]=ex[i].replace("Jale","1").replace("surfaceStretch","1").replace("zPhysical","z");
  System.out.println("EXPORT "+tag);m.result().numerical().create(tag,type);
  m.result().numerical(tag).selection().set(sel);m.result().numerical(tag).set("data","s4data");m.result().numerical(tag).set("expr",ex);
  m.result().numerical(tag).set("solnum",uniqueIds);
  // P1 density and conserved species have exact vertex bounds; positive P1
  // mixtures also bound specific thermal energy below by their vertex minima.
  // Keep local nonlinear peaks explicitly labelled as sampled extrema.
  if(type.startsWith("Max"))m.result().numerical(tag).set("refine",1);
  if(type.equals("IntSurface")||type.equals("IntLine")){
   m.result().numerical(tag).set(type.equals("IntSurface")?"intvolume":"intsurface",true);
   m.result().numerical(tag).set("intorderactive",true);m.result().numerical(tag).set("intorder",tag.equals("photons")?30:12);
  }
  double[][] val=m.result().numerical(tag).getReal();PrintWriter w=new PrintWriter(p+"_"+tag+".csv");
  for(int i=0;i<ex.length;i++){if(i>0)w.print(",");w.print(ex[i]);}w.println();
  for(int j=0;j<val[0].length;j++){for(int i=0;i<val.length;i++){if(i>0)w.print(",");w.print(val[i][j]);}w.println();}w.close();
 }
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.load("M",a[0]);String p=a[1];String active=java.util.Arrays.asList(m.sol().tags()).contains("merged")?"merged":"sol1";double[] times=m.sol(active).getPVals();
  java.util.ArrayList<Integer> ids=new java.util.ArrayList<Integer>();double previous=-1;for(int i=0;i<times.length;i++)if(times[i]>previous+1e-18){ids.add(i+1);previous=times[i];}
  uniqueIds=new int[ids.size()];double[] ut=new double[ids.size()];for(int i=0;i<ids.size();i++){uniqueIds[i]=ids.get(i);ut[i]=times[ids.get(i)-1];}times=ut;
  if(a.length>2)only=a[2];
  try{ale=m.param().evaluate("aleOn")>.5;}catch(Exception ignored){}
  if(!ale){m.component("c").variable("v").set("Jale","1");m.component("c").variable("v").set("surfaceStretch","1");m.component("c").variable("v").set("zPhysical","z");}
  m.result().dataset().create("s4data","Solution");m.result().dataset("s4data").set("solution",active);
  calc("max","MaxSurface",new int[]{2},new String[]{"t","Tpl","nePl","speedP","Yraw","-Yraw","-rhoP","-ethRaw/rhoSafe*rho0","leArg","-leArg","lrArg","-lrArg","xionCu","opt","-opt","opt-1","optUp","-optUp","optUp-(1-Asurf)","if(r>0.8*Rdom||z>0.8*Zdom,abs(pPl-pAmb)/pAmb,0)","if(r>0.8*Rdom||z>0.8*Zdom,Yraw,0)","log10(max((e0*qE-Bcu*qC)/max(qR,1e-9),6400[J/kg])/1[J/kg])"},p);
  calc("gas","IntSurface",new int[]{2},new String[]{"Jale*rho0*qC","Jale*rhoP-rhoAmb","Jale*rho0*e0*qE-rhoAmb*eAmb","Jale*QlaserP","Jale*Qrad","Jale*nePl","if(Yraw>0.001,Jale*nePl,0)"},p);
  calc("solid","IntSurface",new int[]{1},new String[]{"Jale*8940[kg/m^3]*e0*hs"},p);
  calc("wall","IntLine",new int[]{4},new String[]{"surfaceStretch*mdot","8940[kg/m^3]*depth","Igas","surfaceStretch*Qmass","surfaceStretch*qExchange","surfaceStretch*mdotEvap","surfaceStretch*mdotReturn"},p);
  calc("wallmax","MaxLine",new int[]{4},new String[]{"t","Ts","depth","mdotEvap","mdotReturn","if(mdotEvap>1e-6[kg/(m^2*s)],returnFraction,0)"},p);
  calc("photons","IntLine",new int[]{5},new String[]{"Ipeak*Irad*Ipulse","Iup"},p);
  String dc="rho0*(nr*(up(Dnum*qCr)+down(Dnum*qCr))+nz*(up(Dnum*qCz)+down(Dnum*qCz)))";
  String de="nr*(up(rho0*e0*Dnum*qEr+kGas*d(Tpl,r))+down(rho0*e0*Dnum*qEr+kGas*d(Tpl,r)))+nz*(up(rho0*e0*Dnum*qEz+kGas*d(Tpl,z))+down(rho0*e0*Dnum*qEz+kGas*d(Tpl,z)))";
  String dr="rho0*(nr*(up(Dnum*qRr)+down(Dnum*qRr))+nz*(up(Dnum*qRz)+down(Dnum*qRz)))";
  if(ale)calc("far","IntLine",new int[]{5,7},new String[]{"rho0*(nr*(up(Jale*FR4)+down(Jale*FR4))+nz*(up(sR*FR4+FZ4-wALE*qC)+down(sR*FR4+FZ4-wALE*qC)))","rho0*e0*(nr*(up(Jale*FR3)+down(Jale*FR3))+nz*(up(sR*FR3+FZ3-wALE*qE)+down(sR*FR3+FZ3-wALE*qE)))","rho0*(nr*(up(Jale*FR0)+down(Jale*FR0))+nz*(up(sR*FR0+FZ0-wALE*qR)+down(sR*FR0+FZ0-wALE*qR)))"},p);
  else calc("far","IntLine",new int[]{5,7},new String[]{"rho0*(nr*urP*qC+nz*uzP*qC)-("+dc+")","nr*urP*(rho0*e0*qE+pPl)+nz*uzP*(rho0*e0*qE+pPl)-("+de+")","rho0*u0*(nr*qMr+nz*qMz)-("+dr+")"},p);
  if(ale){calc("ale","IntSurface",new int[]{2},new String[]{"Jale","JtALE","abs(JtALE-d(wALE,z))","Jale*rhoP-rhoAmb"},p);calc("alemax","MaxSurface",new int[]{1,2},new String[]{"-Jale","abs(JtALE-d(wALE,z))","abs(sR)"},p);}
  calc("fronts","MaxSurface",new int[]{2},new String[]{"t","if(Yraw>0.001,zPhysical/1[um],0)","if(Yraw>0.001,2*r/1[um],0)","if(nePl>1e20[1/m^3]&&Yraw>0.001,zPhysical/1[um],0)","if(abs(pPl-pAmb)>0.05*pAmb,zPhysical/1[um],0)"},p);
  PrintWriter w=new PrintWriter(p+"_metadata.json");w.println("{\"last_time_s\":"+times[times.length-1]+",\"target_time_s\":"+m.param().evaluate("tEnd")+",\"saved_times\":"+times.length+",\"Ein_J\":"+m.param().evaluate("Ein")+",\"w0_m\":"+m.param().evaluate("w0")+",\"netEvap\":"+m.param().evaluate("netEvap")+",\"ALE\":"+ale+",\"maximum_refinement\":1,\"nonlinear_peaks_are_sampled\":true,\"raw_P1_vertex_bounds_and_thermal_upper_bound\":true}");w.close();
  ModelUtil.clear();System.out.println("EXPORTED");System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
