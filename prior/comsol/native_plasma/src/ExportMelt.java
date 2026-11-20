import com.comsol.model.*;
import com.comsol.model.util.*;
import java.io.*;
public class ExportMelt{
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);String saved=ExportNative.savedSolution(m);double[] ts=m.sol(saved).getPVals();if(ts.length<2)throw new Exception("No transient saved");ExportNative.dataset(m,saved);
 for(String op:new String[]{"intCu","intSurf"})m.component("c").cpl(op).set("axisym",true);
 m.sol(saved).updateSolution();
 String[] ge={"c.intCu(rhoCu)","c.intCu(rhoCu*c.hCond+rhoCu*(c.urCu^2+c.uzCu^2)/2)","c.intSurf(c.mdot)","c.intSurf(c.Qsurface)","c.intSurf(c.Qmass)","c.intSurf(c.recoil*(c.urCu*c.ncr+c.uzCu*c.ncz))","c.intSurf(c.sigmaCu)","c.intCu(rhoCu*(c.urCu^2+c.uzCu^2)/2)"};
 m.result().numerical().create("gh","EvalGlobal");m.result().numerical("gh").set("data","dset1");m.result().numerical("gh").set("expr",ge);
 ExportNative.csv(a[1].replace("_surface.csv","_melt_history.csv"),ge,ts,m.result().numerical("gh").getReal());
 m.result().numerical().create("s","Eval");m.result().numerical("s").set("data","dset1");m.result().numerical("s").selection().named("surface");m.result().numerical("s").set("expr",new String[]{"r","z","T","urCu","uzCu","mdot","fL","Qsurface","Qmass","mdot*hCond","recoil","d(r,TIME)","d(z,TIME)","Tr","Tz","cpCu","p","ncr","ncz","vnSurf","vnPhysical","mdotEvap","mdotReturn"});
 try(PrintWriter p=new PrintWriter(a[1])){p.println("time_s,r_m,z_m,T_K,ur_m_s,uz_m_s,mdot_kg_m2_s,liquid_fraction,Qsurface,Qmass,advected_enthalpy,recoil,mesh_ur,mesh_uz,Tr,Tz,cpCu,p,ncr,ncz,vnSurf,vnPhysical,evap_kg_m2_s,return_kg_m2_s");for(int i=0;i<ts.length;i++){m.result().numerical("s").set("solnum",new int[]{i+1});double[][][] d=m.result().numerical("s").getData();for(int k=0;k<d[0][0].length;k++){p.print(ts[i]);for(int e=0;e<d.length;e++)p.print(","+d[e][0][k]);p.println();}}}
 System.out.println("MELT EXPORTED "+ts.length+" states, last "+ts[ts.length-1]);System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}

