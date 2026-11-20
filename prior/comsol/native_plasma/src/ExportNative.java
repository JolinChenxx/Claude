import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
import java.io.*;
public class ExportNative {
 static void dataset(Model m,String saved){if(!Arrays.asList(m.result().dataset().tags()).contains("dset1"))m.result().dataset().create("dset1","Solution");m.result().dataset("dset1").set("solution",saved);m.result().dataset("dset1").set("frametype","spatial");}
 static String savedSolution(Model m){
  String selected="sol1";double end=-1;
  // Select the newest saved interval first. A disabled remesh node can retain
  // an older output solution; it must not hide a newer non-remeshed solve.
  for(String sol:m.sol().tags())try{if(!sol.matches("sol[0-9]+"))continue;double[] ts=m.sol(sol).getPVals();if(ts.length>1&&ts[ts.length-1]>end){selected=sol;end=ts[ts.length-1];}}catch(Exception ignored){}
  // Prefer the full multi-mesh output only when it reaches the same endpoint.
  for(String sol:m.sol().tags())try{for(String f:m.sol(sol).feature("t1").feature().tags())if(m.sol(sol).feature("t1").feature(f).getType().equals("AutoRemesh")){
   String out=m.sol(sol).feature("t1").feature(f).getString("tadapsol");double[] ts=m.sol(out).getPVals();if(ts.length>1&&ts[ts.length-1]>=end-1e-16){selected=out;end=ts[ts.length-1];}
  }}catch(Exception ignored){}return selected;
 }
 static double[] grid(double end){ArrayList<Double> a=new ArrayList<>();for(double x=.1;x<60;x+=1)a.add(x);for(double x=60;x<300;x+=4)a.add(x);for(double x=300;x<=end;x+=10)a.add(x);return a.stream().mapToDouble(Double::doubleValue).toArray();}
 static void csv(String path,String[] expr,double[] times,double[][] data)throws Exception{try(PrintWriter p=new PrintWriter(path)){p.print("time_s");for(String e:expr)p.print(",\""+e.replace("\"","\"\"")+"\"");p.println();for(int i=0;i<times.length;i++){p.print(times[i]);for(int j=0;j<expr.length;j++)p.print(","+data[j][i]);p.println();}}}
 public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);String out=a[1];String saved=savedSolution(m);double[] ts=m.sol(saved).getPVals();if(ts.length<2)throw new Exception("No transient interval saved");
 dataset(m,saved);
 m.component("c").cpl().create("maxGas","Maximum");m.component("c").cpl("maxGas").selection().named("g_gas_dom");
 m.component("c").cpl().create("maxSurf","Maximum");m.component("c").cpl("maxSurf").selection().named("surface");
 m.component("c").cpl().create("minGas","Minimum");m.component("c").cpl("minGas").selection().named("g_gas_dom");
 m.component("c").cpl("maxSurf").set("opname","maxSurf");m.component("c").cpl("maxSurf").selection().geom("g",1);m.component("c").cpl("maxSurf").selection().named("surface");
 m.component("c").cpl("maxGas").set("opname","maxGas");m.component("c").cpl("minGas").set("opname","minGas");
 for(String op:new String[]{"intSurf","intGas","intCu"})m.component("c").cpl(op).set("axisym",true);
 m.sol(saved).updateSolution();
 String[] ge={"c.maxSurf(c.T)","c.maxSurf(z)","c.maxGas(c.plas.ne)","c.maxGas(c.Tg)","c.maxGas(c.plas.Te*e_const/k_B_const)","c.minGas(c.Tg)","c.minGas(c.rhoP)","c.intGas(c.rhoCopper)","c.intCu(rhoCu)","c.intGas(rho0*e0*c.qE)","c.intCu(rhoCu*c.hCond+rhoCu*(c.urCu^2+c.uzCu^2)/2)","c.intGas(epsilon0_const*(c.plas.Etr^2+c.plas.Etz^2)/2)","c.intSurf(c.Qsurface)","c.intGas(c.Qlaser)","c.intGas(c.Qrad)","c.intSurf(c.mdot)","c.intSurf(c.QworkWall)","c.qmetal*1[pC]","c.intGas(c.plas.scharge)","c.intSurf(c.Iwall)"};
 m.result().numerical().create("history","EvalGlobal");m.result().numerical("history").set("expr",ge);m.result().numerical("history").set("data","dset1");csv(out+"_history.csv",ge,ts,m.result().numerical("history").getReal());
 String[] ex={"Tg","TeK","plas.ne","rhoCopper","rhoP","urP","uzP","pPl","plas.V","Qlaser","Qrad","Qff","nCuTotal","niZ2","jline3","jline4","jline5","jline6","tauLine3/w0","tauLine4/w0","tauLine5/w0","tauLine6/w0","nCu_1p","nCuH3","nCuH5"};
 if(Arrays.asList(m.func().tags()).contains("tailRates")){ArrayList<String> full=new ArrayList<>(Arrays.asList(ex));full.add("tailPopulation");ex=full.toArray(new String[0]);}
 double[] r=grid(1000),z=grid(1000);
 if(Arrays.asList(m.component("c").physics().tags()).contains("flatLedger")){
  TreeSet<Double> fine=new TreeSet<>();for(double zz:z)fine.add(zz);for(double zz:new double[]{.001,.002,.005,.01,.02,.05,.2,.5})fine.add(zz);z=fine.stream().mapToDouble(Double::doubleValue).toArray();
 }
 int n=r.length*z.length;double[][] xy=new double[2][n];for(int j=0;j<z.length;j++)for(int i=0;i<r.length;i++){int k=j*r.length+i;xy[0][k]=r[i]*1e-6;xy[1][k]=z[j]*1e-6;}
 m.result().numerical().create("gasFields","Interp");m.result().numerical("gasFields").set("data","dset1");m.result().numerical("gasFields").selection().named("g_gas_dom");m.result().numerical("gasFields").set("coord",xy);m.result().numerical("gasFields").set("expr",ex);
 try(PrintWriter p=new PrintWriter(out+"_fields.json")){p.println("{\"shape\":["+ts.length+","+ex.length+","+z.length+","+r.length+"],\"dtype\":\">f4\",\"times_s\":"+Arrays.toString(ts)+",\"r_um\":"+Arrays.toString(r)+",\"z_um\":"+Arrays.toString(z)+",\"expressions\":[\""+String.join("\",\"",ex)+"\"],\"note\":\"Fixed spatial sampling; points outside the moving gas domain may be NaN.\"}");}
 try(DataOutputStream f=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(out+"_fields.bin")))){for(int i=0;i<ts.length;i++){m.result().numerical("gasFields").set("solnum",new int[]{i+1});double[][][] d=m.result().numerical("gasFields").getData();for(int e=0;e<ex.length;e++)for(int k=0;k<n;k++)f.writeFloat((float)d[e][0][k]);if(i%10==0)System.out.println("GAS "+i+"/"+ts.length);}}
 m.result().numerical().create("surfaceFields","Eval");m.result().numerical("surfaceFields").set("data","dset1");m.result().numerical("surfaceFields").selection().named("surface");m.result().numerical("surfaceFields").set("expr",new String[]{"r","z","T","urCu","uzCu","mdot","pPl","plas.V","plas.ne"});
 try(PrintWriter p=new PrintWriter(out+"_surface.csv")){p.println("time_s,r_m,z_m,T_K,ur_m_s,uz_m_s,mdot_kg_m2_s,p_Pa,V_V,ne_m3");for(int i=0;i<ts.length;i++){m.result().numerical("surfaceFields").set("solnum",new int[]{i+1});double[][][] d=m.result().numerical("surfaceFields").getData();for(int k=0;k<d[0][0].length;k++){p.print(ts[i]);for(int e=0;e<d.length;e++)p.print(","+d[e][0][k]);p.println();}}}
 System.out.println("EXPORTED "+ts.length+" times to "+ts[ts.length-1]);System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
