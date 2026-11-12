import com.comsol.model.*;
import com.comsol.model.util.*;
public class ContinueSaved {public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 String saved=ExportNative.savedSolution(m);double[] ts=m.sol(saved).getPVals();String start=a.length>2?a[2]:Double.toString(ts[ts.length-1]);m.sol(saved).copySolution("continued");
 m.param().set("tEnd","1000[ns]");String times=a.length>3?a[3]:"range("+start+",2[ns],tEnd)";m.study("std").feature("time").set("tlist",times);m.sol("sol1").feature("t1").set("tlist",times);
 m.sol("sol1").feature("v1").set("control","user");m.sol("sol1").feature("v1").set("initmethod","sol");m.sol("sol1").feature("v1").set("initsol","continued");m.sol("sol1").feature("v1").set("solnum","interp");m.sol("sol1").feature("v1").set("t",start);
 m.sol("sol1").feature("t1").set("consistent","bweuler");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");
 if(a.length>4)m.sol("sol1").feature("t1").feature("coupledCu").set("jtech",a[4]);
 m.save(a[1]);System.out.println("CONTINUATION READY "+saved+" "+start);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
