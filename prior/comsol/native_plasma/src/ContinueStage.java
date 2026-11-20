import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;

/** Continue exactly from the last saved state on the unchanged fixed geometry. */
public class ContinueStage {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("stage",a[0]);
  String saved=ExportNative.savedSolution(m);double[] t=m.sol(saved).getPVals();
  double start=t[t.length-1],end=Double.parseDouble(a[2])*1e-9,step=Double.parseDouble(a[3])*1e-9;
  if(start>=end)throw new IllegalArgumentException("Saved endpoint already reaches requested endpoint");
  if(Arrays.asList(m.sol().tags()).contains("stageInitial"))m.sol().remove("stageInitial");
  m.sol(saved).copySolution("stageInitial");
  String times="range("+start+","+step+","+end+") "+end;
  m.param().set("tEnd",Double.toString(end));
  m.study("std").feature("time").set("tlist",times);m.sol("sol1").feature("t1").set("tlist",times);
  m.sol("sol1").feature("v1").set("control","user");m.sol("sol1").feature("v1").set("initmethod","sol");
  m.sol("sol1").feature("v1").set("initsol","stageInitial");m.sol("sol1").feature("v1").set("solnum","interp");m.sol("sol1").feature("v1").set("t",Double.toString(start));
  m.sol("sol1").feature("t1").set("consistent","bweuler");
  m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");
  m.sol("sol1").feature("t1").set("maxstepbdf",(a.length>4?a[4]:a[3])+"[ns]");
  m.sol("sol1").feature("t1").set("maxstepconstraintbdf",a.length>5?"expr":"const");
  if(a.length>5)m.sol("sol1").feature("t1").set("maxstepexpressionbdf",a[5]);
  // Preserve an actual accepted state when nonlinear algebraic storage is used;
  // interpolating log fields and physical storage independently breaks closure.
  boolean storage=Arrays.asList(m.component("c").physics().tags()).contains("storage");
  m.sol("sol1").feature("t1").set("tout",storage?"tstepsclosest":"tlist");
  // Working accuracy for mechanism comparisons; final candidate gets a
  // tighter time-step and mesh comparison after complete exposure coverage.
  String physicalT=m.component("c").variable("gasv").get("Tg");
  boolean positive=physicalT!=null&&physicalT.contains("logTgas");
  m.study("std").feature("time").set("rtol",positive?0.001:0.003);m.sol("sol1").feature("t1").set("rtol",positive?0.001:0.003);
  m.sol("sol1").feature("t1").feature("coupledAll").set("dtech",positive?"auto":"const");
  if(!positive)m.sol("sol1").feature("t1").feature("coupledAll").set("damp",1);
  m.sol("sol1").feature("t1").feature("coupledAll").set("maxiter",positive?15:10);
  m.save(a[1]);System.out.println("STAGE READY: start="+start+" end="+end+" output_step="+step);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
