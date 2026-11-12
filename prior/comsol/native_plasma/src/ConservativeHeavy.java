import com.comsol.model.*;
import com.comsol.model.util.*;
/** Native heavy-species transport completed as conservative ALE mass equations.
 * Cancels the strong convection and adds flux form. No reaction rate is changed.
 */
public class ConservativeHeavy {
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 String saved=ExportNative.savedSolution(m);m.sol(saved).copySolution("heavyInit");String start=a.length>2?a[2]:"110[ns]";
 String domain="0",far="0";
 for(String n:Chemistry.NAMES)if(!n.equals("N2")){
 String w="plas.w"+n,v="W"+n;
 domain+="+(-"+w+"*(d(rhoP,TIME)-d(r,TIME)*d(rhoP,r)-d(z,TIME)*d(rhoP,z))+rhoP*(uMassR*d("+w+",r)+uMassZ*d("+w+",z)))*test("+v+")+rhoP*"+w+"*(uMassR*test("+v+"r)+uMassZ*test("+v+"z))";
 far+="-rhoP*"+w+"*(uMassR*nr+uMassZ*nz)*test("+v+")";
 }
 for(String tag:new String[]{"heavyConservative","heavyFarConvection"}){m.component("c").physics("plas").create(tag,"WeakContribution",tag.equals("heavyConservative")?2:1);m.component("c").physics("plas").feature(tag).selection().named(tag.equals("heavyConservative")?"g_gas_dom":"far");m.component("c").physics("plas").feature(tag).set("weakExpression",tag.equals("heavyConservative")?domain:far);}
 m.component("c").physics("plas").feature("feed").set("IncludeConvection",false);
 String times="range("+start+",0.2[ns],123[ns])";m.study("std").feature("time").set("tlist",times);m.sol("sol1").feature("t1").set("tlist",times);
 m.sol("sol1").feature("v1").set("control","user");m.sol("sol1").feature("v1").set("initmethod","sol");m.sol("sol1").feature("v1").set("initsol","heavyInit");m.sol("sol1").feature("v1").set("solnum","interp");m.sol("sol1").feature("v1").set("t",start);
 m.sol("sol1").feature("t1").set("consistent","bweuler");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");
 m.save(a[1]);System.out.println("CONSERVATIVE HEAVY SPECIES READY FROM "+start);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
