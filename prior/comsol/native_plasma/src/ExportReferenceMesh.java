import com.comsol.model.*;import com.comsol.model.util.*;import java.io.*;
/** Freeze the last saved material-to-spatial map into interpolation data. */
public class ExportReferenceMesh {public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);String s=ExportNative.savedSolution(m);ExportNative.dataset(m,s);int last=m.sol(s).getPVals().length;
for(String n:new String[]{"volume","surface"}){
m.result().numerical().create("ref"+n,"Eval");m.result().numerical("ref"+n).set("data","dset1");m.result().numerical("ref"+n).set("solnum",new int[]{last});if(n.equals("surface"))m.result().numerical("ref"+n).selection().named("surface");
m.result().numerical("ref"+n).set("expr",n.equals("volume")?new String[]{"R","Z","r-R","z-Z"}:new String[]{"R","etaSurface*1[um]"});double[][][] d=m.result().numerical("ref"+n).getData();try(PrintWriter p=new PrintWriter(a[1]+"_"+n+".txt")){for(int k=0;k<d[0][0].length;k++){for(int j=0;j<d.length;j++){if(j>0)p.print(" ");p.print(d[j][0][k]);}p.println();}}
}System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}}
