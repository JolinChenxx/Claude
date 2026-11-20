import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
import java.io.*;
/** Export the actual endpoint nodes responsible for extrema and rapid changes. */
public class NodeStiffnessAudit {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("nodeAudit",a[0]);String saved=ExportNative.savedSolution(m);ExportNative.dataset(m,saved);
  ArrayList<String> ex=new ArrayList<>(Arrays.asList("r","z","Tg","TeK","plas.ne","plas.en","rhoP","rhoCopper","plas.wN2","urP","uzP","pPl","plas.V","Qlaser","plas.Re","plas.Sen","Uchem","Ue","sqrt(epsilon0_const*plas.Te/(e_const*plas.ne))","h","abs(d(plas.ne,TIME))/max(plas.ne,1[1/m^3])","abs(d(plas.en,TIME))/max(plas.en,1[V/m^3])","abs(d(Tg,TIME))/Tg","abs(d(rhoP,TIME))/rhoP","alphaLaser","nuEN","nuEI"));
  for(String n:Chemistry.NAMES){ex.add("plas.n_w"+n);ex.add("plas.R_w"+n);ex.add("abs(d(plas.n_w"+n+",TIME))/max(plas.n_w"+n+",1[1/m^3])");}
  double[] t=m.sol(saved).getPVals();m.result().numerical().create("nodes","Eval");m.result().numerical("nodes").set("data","dset1");m.result().numerical("nodes").selection().named("g_gas_dom");m.result().numerical("nodes").set("expr",ex.toArray(new String[0]));m.result().numerical("nodes").set("solnum",new int[]{t.length});double[][][] d=m.result().numerical("nodes").getData();
  try(PrintWriter out=new PrintWriter(a[1])){out.print("criterion,time_s");for(String s:ex)out.print(",\""+s+"\"");out.println();
   for(int j=2;j<ex.size();j++)for(int sign:new int[]{-1,1}){
    int k=0;for(int i=1;i<d[j][0].length;i++)if(sign*d[j][0][i]>sign*d[j][0][k])k=i;
    out.print("\""+(sign==1?"max ":"min ")+ex.get(j)+"\","+t[t.length-1]);for(int v=0;v<ex.size();v++)out.print(","+d[v][0][k]);out.println();
   }
  }
  System.out.println("ENDPOINT NODE AUDIT "+t[t.length-1]);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
