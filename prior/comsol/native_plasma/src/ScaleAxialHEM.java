import com.comsol.model.*;import com.comsol.model.util.*;
/** Fresh axial solve with physically scaled logarithms and conserved fields. */
public class ScaleAxialHEM {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("hemScale",a[0]);
 if(a.length>2&&a[2].equals("continue")){
  String saved=ExportNative.savedSolution(m);double[] ts=m.sol(saved).getPVals();double start=ts[ts.length-1];m.sol(saved).copySolution("hemInitial");
  m.sol("sol1").feature("v1").set("control","user");m.sol("sol1").feature("v1").set("initmethod","sol");m.sol("sol1").feature("v1").set("initsol","hemInitial");m.sol("sol1").feature("v1").set("solnum","interp");m.sol("sol1").feature("v1").set("t",Double.toString(start));
  String list="range("+start+",0.5[ns],100[ns])";m.study("std").feature("time").set("tlist",list);m.sol("sol1").feature("t1").set("tlist",list);m.sol("sol1").feature("t1").set("consistent","bweuler");
 }
 if(a.length>2&&a[2].equals("contact")){
  m.component("c").variable("state").set("shockSensor","min(1,h*sqrt(d(pGas,x)^2+1[Pa^2/m^2])/(abs(pGas)+pAmb))");
  m.component("c").variable("state").set("Dnum","0.15*h*(sqrt(u^2+1e-8[m^2/s^2])+cs*shockSensor)");
  if(a.length>3)m.param().set("Acu",a[3]);
  if(a.length>4){m.param().set("tEnd",a[4]);m.study("std").feature("time").set("tlist","range(0,1[ns],tEnd)");m.sol("sol1").feature("t1").set("tlist","range(0,1[ns],tEnd)");}
 }
 for(String f:m.sol("sol1").feature("v1").feature().tags()){
  String scale=f.equals("c_qE")||f.equals("c_qm")?"1e-3":"1";
  m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",scale);
 }
 for(String f:m.sol("sol1").feature("t1").feature().tags())if(m.sol("sol1").feature("t1").feature(f).getType().equals("FullyCoupled")){
  m.sol("sol1").feature("t1").feature(f).set("jtech","onevery");m.sol("sol1").feature("t1").feature(f).set("maxiter",15);m.sol("sol1").feature("t1").feature(f).set("dtech","auto");
 }
 m.save(a[1]);System.out.println("AXIAL SCALE/NEWTON CORRECTION READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
