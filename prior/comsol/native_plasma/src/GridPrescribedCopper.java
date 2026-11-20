import com.comsol.model.*;import com.comsol.model.util.*;
/** Partition the expensive spatial copper heat solve from the native FV grid.
 * The input is a saved copper calculation, not a fitted source or trajectory.
 * This diagnostic keeps the grid's state-resolved evaporation and return flux;
 * its changed outgoing state enthalpy has NOT yet been iterated back to copper.
 */
public class GridPrescribedCopper {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("gridPrescribed",a[0]);
  m.component("c").physics().remove("ht");
  m.func().create("gridCopperHistory","Interpolation");
  m.func("gridCopperHistory").set("source","file");m.func("gridCopperHistory").set("filename",a[2]);
  m.func("gridCopperHistory").set("nargs",2);
  m.func("gridCopperHistory").set("funcs",new String[][]{{"gridCuHeight","1"},{"gridCuTemperature","2"},{"gridCuUr","3"},{"gridCuUz","4"},{"gridCuEvap","5"}});
  m.func("gridCopperHistory").set("interp","linear");m.func("gridCopperHistory").set("extrap","const");m.func("gridCopperHistory").importData();
  m.component("c").variable("gasv").set("T","gridCuTemperature(r/1[um],t/1[ns])*1[K]");
  m.component("c").variable("gasv").set("Tr","d(T,r)");m.component("c").variable("gasv").set("Tz","0[K/m]");
  m.component("c").variable("boundv").set("mdotEvap","gridCuEvap(r/1[um],t/1[ns])*1[kg/(m^2*s)]");
  m.param().set("tEnd",a[3]);
  String times="range(0,2[ns],80[ns]) range(81[ns],1[ns],tEnd)";
  m.study("std").feature("time").set("tlist",times);m.sol("sol1").feature("t1").set("tlist",times);
  // Reuse the existing solver tree; COMSOL updates dependent variables on run.
  m.sol("sol1").feature("v1").set("initmethod","init");
  m.label("80 native Plasma FV cells; computed C201 thermal/evaporation predictor; fixed plane, no sheath; state enthalpy feedback to copper not converged");
  m.save(a[1]);System.out.println("GRID PRESCRIBED COPPER READY; source support ends at "+a[3]);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
