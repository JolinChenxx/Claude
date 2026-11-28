import com.comsol.model.*;
import com.comsol.model.util.*;
import java.io.*;
import java.util.*;
public class R2Export {
 static double[] grid(double max){ArrayList<Double> a=new ArrayList<Double>();
  for(double x=.05;x<30;x+=1)a.add(x);for(double x=30;x<150;x+=3)a.add(x);for(double x=150;x<=max;x+=10)a.add(x);
  double[] b=new double[a.size()];for(int i=0;i<b.length;i++)b[i]=a.get(i);return b;}
 static void arr(PrintWriter w,double[] a){w.print("[");for(int i=0;i<a.length;i++){if(i>0)w.print(",");w.print(a[i]);}w.print("]");}
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("M",a[0]);String prefix=a[1];String kin="sol2";
  double[] all=m.sol(kin).getPVals();ArrayList<Integer> indices=new ArrayList<Integer>();ArrayList<Double> times=new ArrayList<Double>();
  double previous=-1;for(int i=0;i<all.length;i++)if(all[i]>previous+1e-18){indices.add(i+1);times.add(all[i]);previous=all[i];}
  if(times.size()<2)throw new RuntimeException("No solved kinetic history");
  double[] ts=new double[times.size()];for(int i=0;i<ts.length;i++)ts[i]=times.get(i);
  m.result().dataset().create("r2data","Solution");m.result().dataset("r2data").set("solution",kin);
  double[] r=grid(650),z=grid(1000);int np=r.length*z.length;
  boolean coupled=Arrays.asList(m.component("c").variable("r2v").varnames()).contains("energyClosure");
  String[] ex={"bT","ne2","rho0*max(bCu,0)/bRho","bRho","c7/max(cTotal,1e-30)","bUr","bUz","bP+deltaP","ne2","bZ/1[m]","bJ",
   "rho0*c0/mCu","rho0*c1/mCu","rho0*c2/mCu","rho0*c3/mCu","rho0*c4/mCu","rho0*c5/mCu","rho0*c6/mCu","rho0*c7/mCu",
   "Te2","deltaU","deltaP","Qescape","Qcoll","Qelastic","Qrrkin","Qquench","nAirE","Lescape/1[m]","bLaser","Uchem","TeRaw","bEth","rho0*cTotal","bNe",
   "rho0*e0*ee","(bEth-Uchem-rho0*e0*ee)/bRho",coupled?"pPl":"bP+deltaP",coupled?"energyClosure*rho0*e0":"0[Pa]","Qff2","ne2*kb*Te2*divU"};
  m.result().numerical().create("r2fields","Interp");m.result().numerical("r2fields").set("data","r2data");m.result().numerical("r2fields").selection().set(2);
  m.result().numerical("r2fields").set("solnum",new int[]{1});m.result().numerical("r2fields").set("expr",new String[]{"r/1[um]"});
  m.result().numerical("r2fields").set("coord",new double[][]{{10e-6},{10e-6}});
  double test=m.result().numerical("r2fields").getData()[0][0][0];double scale=Math.abs(test-10)<1e-4?1e-6:1;
  double[][] xy=new double[2][np];for(int j=0;j<z.length;j++)for(int i=0;i<r.length;i++){int k=j*r.length+i;xy[0][k]=r[i]*scale;xy[1][k]=z[j]*scale;}
  m.result().numerical("r2fields").set("coord",xy);m.result().numerical("r2fields").set("expr",ex);
  PrintWriter meta=new PrintWriter(prefix+"_fields.json");meta.print("{\"shape\":["+ts.length+","+ex.length+","+z.length+","+r.length+"],\"dtype\":\">f4\",\"coordinates_um\":{\"r\":");arr(meta,r);meta.print(",\"z\":");arr(meta,z);meta.print("},\"times_s\":");arr(meta,ts);
  meta.print(",\"expressions\":[");for(int i=0;i<ex.length;i++){if(i>0)meta.print(",");meta.print("\""+ex[i]+"\"");}
  meta.print("],\"model_parameters\":{");String[] params={"twoT","quenchP","trapping","electronConduct"};
  for(int i=0;i<params.length;i++){if(i>0)meta.print(",");meta.print("\""+params[i]+"\":"+m.param().evaluate(params[i]));}
  meta.println("},\"source_model\":\""+a[0].replace("\\","/")+"\"}");meta.close();
  DataOutputStream out=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(prefix+"_fields.bin"),1048576));
  for(int start=0;start<ts.length;start+=4){int count=Math.min(4,ts.length-start);int[] ids=new int[count];for(int j=0;j<count;j++)ids[j]=indices.get(start+j);
   m.result().numerical("r2fields").set("solnum",ids);double[][][] values=m.result().numerical("r2fields").getData();
   for(int j=0;j<count;j++)for(int e=0;e<ex.length;e++)for(int k=0;k<np;k++){
    double v=values[e][j][k];if(!Double.isFinite(v))throw new RuntimeException("Nonfinite field "+ex[e]+" t="+ts[start+j]);out.writeFloat((float)v);}
   if(start%20==0)System.out.println("EXPORTED "+(start+count)+"/"+ts.length);
  }out.close();
  String[] integ={"t","bJ*rho0*(y0+y1+y2+y3+y4+y5+y6+y7)","bJ*rho0*bCu","bJ*deltaU","bJ*bEth","bJ*abs(deltaU)","bJ*Qescape","bJ*Qcoll","bJ*Qelastic","bJ*Qquench",
   "bJ*rho0*(min(y0,0)+min(y1,0)+min(y2,0)+min(y3,0)+min(y4,0)+min(y5,0)+min(y6,0)+min(y7,0))","bJ*Uchem","bJ*Ue","bJ*bLaser",
   "bJ*rho0*e0*ee","bJ*rho0*e0*min(ee,0)","bJ*Qff2","bJ*ne2*kb*Te2*divU",coupled?"bJ*abs(energyClosure)*rho0*e0":"0[Pa]"};
  int[] ids=new int[indices.size()];for(int i=0;i<ids.length;i++)ids[i]=indices.get(i);
  m.result().numerical().create("r2int","IntSurface");m.result().numerical("r2int").selection().set(2);m.result().numerical("r2int").set("data","r2data");
  m.result().numerical("r2int").set("solnum",ids);m.result().numerical("r2int").set("expr",integ);m.result().numerical("r2int").set("intvolume",true);
  double[][] vals=m.result().numerical("r2int").getReal();PrintWriter w=new PrintWriter(prefix+"_integrals.csv");
  w.println("t_s,copper_kg,background_copper_kg,deltaU_J,background_thermal_J,abs_deltaU_J,escape_W,collision_W,elastic_W,quench_W,negative_copper_kg,chemical_J,electron_J,laser_W,electron_stored_J,negative_electron_J,free_free_W,electron_expansion_W,abs_energy_closure_J");
  for(int j=0;j<ts.length;j++){w.print(ts[j]);for(int i=1;i<vals.length;i++)w.print(","+vals[i][j]);w.println();}w.close();
  System.out.println("COMPLETE nt="+ts.length+" last_ns="+ts[ts.length-1]*1e9);ModelUtil.clear();System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
