import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Actual integrated thermal source from saved copper temperatures. */
public class ExportThermalSource {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("thermalExport",a[0]);String saved=ExportNative.savedSolution(m);ExportNative.dataset(m,saved);
  m.component("c").cpl().create("maxTarget","Maximum");m.component("c").cpl("maxTarget").selection().geom("g",1);m.component("c").cpl("maxTarget").selection().named("surface");
  m.component("c").cpl().create("intBase","Integration");m.component("c").cpl("intBase").selection().geom("g",1);m.component("c").cpl("intBase").selection().named("bottom");m.component("c").cpl("intBase").set("axisym",true);m.component("c").cpl("intBase").set("intorder",8);
  ArrayList<String> ex=new ArrayList<>();
  for(String s:new String[]{"intSurf(c.mdotEvap)","intSurf(c.QthermalEmission)","intSurf(2*k_B_const*c.T*c.mdotEvap*N_A_const/Mcu*c.emitFraction_Cu_1p)","maxTarget(c.T)","intSurf(c.Qsurface)","intCu(rhoCu*c.hCond)","intBase(-c.kCu*(c.Tr*nr+c.Tz*nz))"})ex.add("c."+s);
  for(String n:SurfaceStateFeed.CU)ex.add("c.intSurf(c.mdotEvap*c.emitFraction_"+n+")");ex.add("c.intSurf(c.mdotEvap*c.emitFraction_CuT5)");
  m.result().numerical().create("thermalFeed","EvalGlobal");m.result().numerical("thermalFeed").set("data","dset1");m.result().numerical("thermalFeed").set("expr",ex.toArray(new String[0]));
  m.sol(saved).updateSolution();ExportNative.csv(a[1],ex.toArray(new String[0]),m.sol(saved).getPVals(),m.result().numerical("thermalFeed").getReal());System.out.println("THERMAL SAVED FEED EXPORTED");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
