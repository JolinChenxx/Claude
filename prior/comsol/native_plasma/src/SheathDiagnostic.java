import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
import java.io.*;
/** Compare charge quadrature and resolve profiles normal to the heated target. */
public class SheathDiagnostic {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);String saved=ExportNative.savedSolution(m);ExportNative.dataset(m,saved);
 ArrayList<String> ex=new ArrayList<>();
 for(int order:new int[]{2,4,6,10,16}){String tag="chg"+order;m.component("c").cpl().create(tag,"Integration");m.component("c").cpl(tag).selection().named("g_gas_dom");m.component("c").cpl(tag).set("opname",tag);m.component("c").cpl(tag).set("axisym",true);m.component("c").cpl(tag).set("intorder",order);ex.add("c."+tag+"(c.plas.scharge)");}
 ex.add("c.qmetal*1[pC]");m.sol(saved).updateSolution();m.result().numerical().create("chargeAudit","EvalGlobal");m.result().numerical("chargeAudit").set("data","dset1");m.result().numerical("chargeAudit").set("expr",ex.toArray(new String[0]));ExportNative.csv(a[1]+"_charge_quadrature.csv",ex.toArray(new String[0]),m.sol(saved).getPVals(),m.result().numerical("chargeAudit").getReal());
 m.result().numerical().create("wallShape","Eval");m.result().numerical("wallShape").set("data","dset1");m.result().numerical("wallShape").selection().named("surface");m.result().numerical("wallShape").set("expr",new String[]{"r","z"});m.result().numerical("wallShape").set("solnum",new int[]{m.sol(saved).getPVals().length});double[][][] wall=m.result().numerical("wallShape").getData();
 TreeMap<Double,Double> heights=new TreeMap<>();for(int k=0;k<wall[0][0].length;k++)heights.put(wall[0][0][k],wall[1][0][k]);
 double[][] xy=new double[2][303];double[] dz=new double[303];for(int ir=0;ir<3;ir++)for(int j=0;j<101;j++){int k=ir*101+j;double r=new double[]{0.1e-6,5e-6,10e-6}[ir];Map.Entry<Double,Double> lo=heights.floorEntry(r),hi=heights.ceilingEntry(r);double z=lo.getKey().equals(hi.getKey())?lo.getValue():lo.getValue()+(hi.getValue()-lo.getValue())*(r-lo.getKey())/(hi.getKey()-lo.getKey());xy[0][k]=r;dz[k]=Math.pow(10,-9+5*j/100.);xy[1][k]=z+dz[k];}
 String[] fields={"plas.ne","plas.n_wCu_1p","plas.n_wN2_1p","plas.n_wO2_1m","plas.Te","Tg","plas.V","rhoP","plas.wCu","plas.wN2","h","sqrt(epsilon0_const*plas.Te/(e_const*plas.ne))"};
 m.result().numerical().create("sheathProfile","Interp");m.result().numerical("sheathProfile").set("data","dset1");m.result().numerical("sheathProfile").selection().named("g_gas_dom");m.result().numerical("sheathProfile").set("coord",xy);m.result().numerical("sheathProfile").set("expr",fields);m.result().numerical("sheathProfile").set("solnum",new int[]{m.sol(saved).getPVals().length});double[][][] vals=m.result().numerical("sheathProfile").getData();
 try(PrintWriter p=new PrintWriter(a[1]+"_sheath_profile.csv")){p.println("r_m,z_m,dz_from_wall_m,"+String.join(",",fields));for(int k=0;k<xy[0].length;k++){p.print(xy[0][k]+","+xy[1][k]+","+dz[k]);for(int j=0;j<fields.length;j++)p.print(","+vals[j][0][k]);p.println();}}
 System.out.println("SHEATH DIAGNOSTIC EXPORTED "+saved);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
