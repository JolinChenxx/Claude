import com.comsol.model.*;import com.comsol.model.util.*;
/** Refine only the observed gas transport-front band, preserving the sheath. */
public class RefinePlumeFront {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("frontRefine",a[0]);
  for(int side=0;side<2;side++){
   String tag="columnEdge3_"+side;
   int old=m.component("c").mesh("mesh").feature(tag).feature("dist").getInt("elemcount");
   m.component("c").mesh("mesh").feature(tag).feature("dist").set("elemcount",2*old);
   System.out.println("2-80um band "+tag+" elements "+old+" -> "+2*old);
  }
  m.component("c").mesh("mesh").run();
  for(String s:m.sol().tags())m.sol(s).clearSolutionData();
  m.sol("sol1").feature("v1").set("initmethod","init");
  String times="range(0,2[ns],80[ns]) range(81[ns],1[ns],125[ns])";
  m.param().set("tEnd","125[ns]");m.study("std").feature("time").set("tlist",times);m.sol("sol1").feature("t1").set("tlist",times);
  m.sol("sol1").feature("t1").set("tout","tstepsclosest");
  m.label("Fixed target spatial native Plasma; doubled axial cells only in observed 2-80um transport front; fresh initial state, same rates and source");
  m.save(a[1]);System.out.println("PLUME FRONT REFINEMENT READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
