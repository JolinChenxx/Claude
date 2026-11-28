import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
/** Short continuation storing actual accepted timesteps to locate an observed stall. */
public class AcceptedTimeContinuation {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("step",a[0]);String saved=ExportNative.savedSolution(m);double[] ts=m.sol(saved).getPVals();double start=a.length>3?Double.parseDouble(a[3])*1e-9:ts[ts.length-1];
 if(start<ts[0]||start>ts[ts.length-1]+1e-16)throw new IllegalArgumentException("Restart time outside saved interval");
 if(Arrays.asList(m.sol().tags()).contains("stageInitial"))m.sol().remove("stageInitial");m.sol(saved).copySolution("stageInitial");
 String times=start+" "+a[2]+"[ns]";m.param().set("tEnd",a[2]+"[ns]");m.study("std").feature("time").set("tlist",times);m.sol("sol1").feature("t1").set("tlist",times);
 m.sol("sol1").feature("v1").set("control","user");m.sol("sol1").feature("v1").set("initmethod","sol");m.sol("sol1").feature("v1").set("initsol","stageInitial");m.sol("sol1").feature("v1").set("solnum","interp");m.sol("sol1").feature("v1").set("t",Double.toString(start));
 m.sol("sol1").feature("t1").set("tout","tsteps");m.sol("sol1").feature("t1").set("tstepsstore",1);
 m.sol("sol1").feature("t1").set("consistent","bweuler");m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","0.05[ns]");
 m.save(a[1]);System.out.println("ACCEPTED STEP DIAGNOSTIC: start="+start+" end="+a[2]+"ns");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
