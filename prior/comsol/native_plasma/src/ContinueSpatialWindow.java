import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
public class ContinueSpatialWindow{public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("window",a[0]);String s=ExportNative.savedSolution(m);double[] ts=m.sol(s).getPVals();double start=ts[ts.length-1];
 if(Arrays.asList(m.sol().tags()).contains("windowInitial"))m.sol().remove("windowInitial");m.sol(s).copySolution("windowInitial");
 double end=Double.parseDouble(a[2]);String list=start+" range("+Math.ceil(start*1e9)+"[ns],1[ns],"+end+"[ns])";
 m.param().set("tEnd",end+"[ns]");m.study("std").feature("time").set("tlist",list);m.sol("sol1").feature("t1").set("tlist",list);m.sol("sol1").feature("t1").set("tout","tstepsclosest");
 m.sol("sol1").feature("v1").set("control","user");m.sol("sol1").feature("v1").set("initmethod","sol");m.sol("sol1").feature("v1").set("initsol","windowInitial");m.sol("sol1").feature("v1").set("solnum","interp");m.sol("sol1").feature("v1").set("t",Double.toString(start));
 m.sol("sol1").feature("t1").set("consistent","bweuler");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");
 m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","0.2[ns]");m.sol("sol1").feature("t1").feature("coupledAll").set("jtech","minimal");
 if(a.length>3){
  m.sol("sol1").feature("t1").feature("coupledAll").set("jtech","onevery");
  if(a[3].equals("directinit"))m.sol("sol1").feature("t1").set("consistent","off");
  m.sol("sol1").feature("t1").set("keeplog",true);
  m.sol("sol1").feature("t1").set("tout","tsteps");
  m.sol("sol1").feature("t1").set("tstepsstore",1);
  System.out.println("SOLVER COMPARISON "+a[3]+"; equations, tolerances and initial field unchanged");
 }
 m.save(a[1]);System.out.println("SPATIAL WINDOW "+start+" TO "+end+" ns");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
