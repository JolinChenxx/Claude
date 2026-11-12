import com.comsol.model.*;
import com.comsol.model.util.*;
/** Preserve the native global model and its tolerances across saved segments. */
public class ContinueGlobalPlume {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("globalContinue",a[0]);
 String saved=ExportNative.savedSolution(m);double[] ts=m.sol(saved).getPVals();double start=ts[ts.length-1],end=Double.parseDouble(a[2])*1e-9;
 if(start>=end)throw new IllegalArgumentException("Already complete");
 if(java.util.Arrays.asList(m.sol().tags()).contains("globalInitial"))m.sol().remove("globalInitial");
 m.sol(saved).copySolution("globalInitial");String times="range("+start+","+a[3]+"[ns],"+end+") "+end;
 m.param().set("tEnd",Double.toString(end));m.study("std").feature("time").set("tlist",times);m.sol("sol1").feature("t1").set("tlist",times);
 m.sol("sol1").feature("v1").set("control","user");m.sol("sol1").feature("v1").set("initmethod","sol");m.sol("sol1").feature("v1").set("initsol","globalInitial");m.sol("sol1").feature("v1").set("solnum","interp");m.sol("sol1").feature("v1").set("t",Double.toString(start));
 m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("maxstepbdf",a[4]+"[ns]");
 if(a.length>5){m.study("std").feature("time").set("rtol",a[5]);m.sol("sol1").feature("t1").set("rtol",a[5]);System.out.println("EXPLORATORY RELATIVE TOLERANCE "+a[5]+"; same physics; stricter comparison required");}
 m.save(a[1]);System.out.println("GLOBAL CONTINUE "+start+" -> "+end);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
