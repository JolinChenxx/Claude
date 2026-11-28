import com.comsol.model.*;import com.comsol.model.util.*;
/** Quantities needed to assess a floating zero-current bulk-source approximation. */
public class ExportSurfaceClosure {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("surfaceClosure",a[0]);String s=ExportNative.savedSolution(m);ExportNative.dataset(m,s);
 m.component("c").cpl("intSurf").set("axisym",true);m.component("c").cpl("intSurf").set("intorder",8);
 boolean thermal=java.util.Arrays.asList(m.component("c").variable().tags()).contains("surfaceFeed");
 System.out.println("THERMAL_SURFACE_STATE_FEED "+thermal);
 InspectFeatures.dump(m.component("c").physics("plas").feature("feed"),"feed");
 String[] ex={"c.intSurf(c.Gtherm)","c.intSurf(c.GeIn)","c.intSurf(c.Gtherm-c.GeIn)","c.intSurf(c.Gemit_Cu_1p)","c.intSurf(c.Gwall_Cu_1p)","c.intSurf(c.Iwall)/e_const","c.intSurf(c.QelectronWall)","c.intSurf(c.QemitStates)","c.intSurf(2*k_B_const*c.T*c.Gemit_Cu_1p)","c.intSurf(c.mdotEvap)","c.intSurf(c.mdotReturn)","c.intSurf(c.Qthermion)"};
 if(!thermal){ex[3]="0[1/s]";ex[7]="0[W]";ex[8]="0[W]";}
 m.result().numerical().create("closure","EvalGlobal");m.result().numerical("closure").set("data","dset1");m.result().numerical("closure").set("expr",ex);ExportNative.csv(a[1],ex,m.sol(s).getPVals(),m.result().numerical("closure").getReal());System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
