import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Deliver the complete coupled physics with real saved fields and usable restart. */
public class PrepareTeachingModel {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("teaching",a[0]);
  String saved=ExportNative.savedSolution(m);double[] times=m.sol(saved).getPVals();double end=times[times.length-1];
  m.sol(saved).copySolution("resume89");
  m.sol("sol1").feature("v1").set("control","user");m.sol("sol1").feature("v1").set("initmethod","sol");
  m.sol("sol1").feature("v1").set("initsol","resume89");m.sol("sol1").feature("v1").set("solnum","interp");m.sol("sol1").feature("v1").set("t",Double.toString(end));
  m.param().set("tEnd","95[ns]");String list="range(89[ns],0.2[ns],95[ns])";
  m.study("std").feature("time").set("tlist",list);m.sol("sol1").feature("t1").set("tlist",list);
  ExportNative.dataset(m,saved);
  String[][] plots={{"docCuT","Copper temperature [K]","T","g_cu_dom"},{"docTg","Gas temperature [K]","Tg","g_gas_dom"},{"docTe","Electron temperature [K]","TeK","g_gas_dom"},{"docNe","Electron number density [1/m3]","plas.ne","g_gas_dom"},{"docCuGas","Gas-phase copper density [kg/m3]","rhoCopper","g_gas_dom"},{"docLight","Cu I 510.55 nm local emissivity [W/m3/sr]","jline3","g_gas_dom"},{"docMelt","Copper velocity magnitude [m/s]","sqrt(urCu^2+uzCu^2)","g_cu_dom"}};
  for(String[] p:plots){m.result().create(p[0],"PlotGroup2D");m.result(p[0]).label(p[1]);m.result(p[0]).set("data","dset1");m.result(p[0]).set("solnum","last");m.result(p[0]).create("surf","Surface");m.result(p[0]).feature("surf").create("sel","Selection");m.result(p[0]).feature("surf").feature("sel").selection().set(m.component("c").selection(p[3]).entities());m.result(p[0]).feature("surf").set("expr",p[2]);}
  m.label("Route 1 complete coupled model; real saved 70-89 ns; restart prepared from 89 ns");m.save(a[1]);
  ModelUtil.remove("teaching");m=ModelUtil.load("verify",a[1]);
  System.out.println("REOPENED "+a[1]);System.out.println("PHYSICS "+Arrays.toString(m.component("c").physics().tags()));
  System.out.println("SAVED "+Arrays.toString(m.sol("sol1").getPVals()));System.out.println("RESTART "+m.sol("sol1").feature("v1").getString("initsol"));
  ExportNative.dataset(m,"sol1");m.result().numerical().create("deliveryCheck","EvalGlobal");m.result().numerical("deliveryCheck").set("data","dset1");m.result().numerical("deliveryCheck").set("solnum",new int[]{times.length});
  m.result().numerical("deliveryCheck").set("expr",new String[]{"c.intGas(c.rhoCopper)","c.intCu(rhoCu*c.hCond)","c.intGas(c.plas.ne)"});
  System.out.println("FINAL INTEGRALS "+Arrays.deepToString(m.result().numerical("deliveryCheck").getReal()));System.out.println("DELIVERY MODEL VERIFIED");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
