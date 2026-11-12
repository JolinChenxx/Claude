import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class MassFluxAudit {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("flux",a[0]);String saved=ExportNative.savedSolution(m);ExportNative.dataset(m,saved);
 if(a.length>1)MassAveragedDiffusion.apply(m);
 String jr="0[kg/(m^2*s)]",jz=jr,src="0[kg/(m^3*s)]";
 for(String n:Chemistry.NAMES){jr+="+plas.jr_w"+n;jz+="+plas.jz_w"+n;src+="+plas.M_w"+n+"*plas.R_w"+n;}
 m.component("c").variable("gasv").set("sumJrAudit",jr);m.component("c").variable("gasv").set("sumJzAudit",jz);m.component("c").variable("gasv").set("sumSourceAudit",src);
 String[] ex={"r","z","plas.wN2","rhoP","sumJrAudit","sumJzAudit","rhoP*uMassR","rhoP*uMassZ","sumSourceAudit","plas.R_wN2*plas.M_wN2","d(sumJrAudit,r)+d(sumJzAudit,z)+if(r>0,sumJrAudit/r,d(sumJrAudit,r))"};
 m.result().numerical().create("flux","Interp");m.result().numerical("flux").set("data","dset1");m.result().numerical("flux").set("expr",ex);
 m.result().numerical("flux").set("coord",new double[][]{{0,0.1e-6,1e-6,10e-6,30e-6},{1.8111662992370601e-6,2.1e-6,2e-6,10e-6,20e-6}});m.result().numerical("flux").set("solnum",new int[]{m.sol(saved).getPVals().length});
 m.sol(saved).updateSolution();double[][][] d=m.result().numerical("flux").getData();System.out.println(String.join(",",ex));for(int p=0;p<5;p++){for(int i=0;i<ex.length;i++)System.out.print((i==0?"":",")+d[i][0][p]);System.out.println();}
 System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
