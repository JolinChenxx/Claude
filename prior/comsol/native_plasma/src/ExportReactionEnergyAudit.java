import com.comsol.model.*;import com.comsol.model.util.*;import java.io.*;import java.util.*;
/** Inspect the actual saved native collision operator, including generated rates. */
public class ExportReactionEnergyAudit {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("auditEnergy",a[0]);String s=ExportNative.savedSolution(m);ExportNative.dataset(m,s);
  ArrayList<String> ex=new ArrayList<>(Arrays.asList("Tg","TeK","plas.ne","plas.en","rhoP","nuEN","nuEI","kmCu","Qei","QeRR","QeAttach","Qff","Qrad","alphaLaser","plas.Re","plas.Sen"));
  try(PrintWriter w=new PrintWriter(a[1]+"_equations.txt")){
   for(String f:m.component("c").physics("plas").feature().tags()){
    String typ=m.component("c").physics("plas").feature(f).getType();
    if(typ.equals("ElectronImpactReaction")||typ.equals("Reaction")||f.equals("pes1")){
     w.println("FEATURE "+f+" "+typ);
     for(String[] row:m.component("c").physics("plas").feature(f).featureInfo("info").getInfoTable("Expression")){
      w.println(Arrays.toString(row));
      if(row.length>2 && row[1].matches("plas\\.(R_|Ren_|r_|de_).*"))ex.add(row[1]);
     }
    }
   }
  }
  for(String n:Chemistry.NAMES)ex.add("plas.n_w"+n);
  m.result().numerical("rxOut").set("data","dset1");m.result().numerical("rxOut").set("solnum",new int[]{m.sol(s).getPVals().length});
  // Evaluate independently: unavailable generated names must not hide the useful audit.
  ArrayList<String> valid=new ArrayList<>();ArrayList<double[]> rows=new ArrayList<>();
  for(String e:ex){try{m.result().numerical("rxOut").set("expr",new String[]{e});double[][][] d=m.result().numerical("rxOut").getData();valid.add(e);rows.add(d[0][0]);}catch(Exception err){System.out.println("UNAVAILABLE "+e);}}
  try(PrintWriter w=new PrintWriter(a[1]+".csv")){w.println("slot,"+String.join(",",valid));for(int i=0;i<rows.get(0).length;i++){w.print(i);for(double[] row:rows)w.print(","+row[i]);w.println();}}
  System.out.println("SAVED ENERGY AUDIT "+rows.get(0).length+" cells, "+valid.size()+" fields");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
