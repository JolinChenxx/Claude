import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
import java.io.*;
/** Save the actual composition at the lowest-N2 nodes of a stalled solution. */
public class InspectDepletedAir {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("air",a[0]);String saved=ExportNative.savedSolution(m);ExportNative.dataset(m,saved);double[] t=m.sol(saved).getPVals();
 ArrayList<String> ex=new ArrayList<>(Arrays.asList("r","z","Tg","TeK","plas.ne","rhoP","rhoCopper","plas.wN2","urP","uzP","Qlaser","Uchem","Ue"));
 for(String n:Chemistry.NAMES){ex.add("plas.n_w"+n);ex.add("plas.R_w"+n);}
 m.result().numerical().create("air","Eval");m.result().numerical("air").set("data","dset1");m.result().numerical("air").selection().named("g_gas_dom");m.result().numerical("air").set("expr",ex.toArray(new String[0]));m.result().numerical("air").set("solnum",new int[]{t.length});
 double[][][] d=m.result().numerical("air").getData();Integer[] order=new Integer[d[0][0].length];for(int i=0;i<order.length;i++)order[i]=i;Arrays.sort(order,Comparator.comparingDouble(i->d[7][0][i]));
 try(PrintWriter out=new PrintWriter(a[1])){out.println("time_s,"+String.join(",",ex));for(int q=0;q<Math.min(30,order.length);q++){int i=order[q];out.print(t[t.length-1]);for(int j=0;j<ex.size();j++)out.print(","+d[j][0][i]);out.println();}}
 System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
