import com.comsol.model.*;
import com.comsol.model.util.*;
public class CopperRateAudit {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);String saved=ExportNative.savedSolution(m);ExportNative.dataset(m,saved);
 for(String op:new String[]{"intGas","intSurf"}){m.component("c").cpl(op).set("axisym",true);m.component("c").cpl(op).set("intorder",12);}
 String rt="0[kg/(m^3*s)]";for(String n:Chemistry.NAMES)if(n.startsWith("Cu"))rt+="+plas.M_w"+n+"*plas.R_w"+n;
 m.component("c").variable("gasv").set("cuReactionSum",rt);
 m.component("c").variable("gasv").set("cuDt","d(rhoCopper,TIME)-d(r,TIME)*d(rhoCopper,r)-d(z,TIME)*d(rhoCopper,z)");
 m.component("c").variable("gasv").set("continuityResidual","d(rhoP,TIME)-d(r,TIME)*d(rhoP,r)-d(z,TIME)*d(rhoP,z)+d(rhoP*uMassR,r)+rhoP*uMassR/max(r,1e-12[m])+d(rhoP*uMassZ,z)");
 String[] ex={"c.intGas(c.rhoCopper)","c.intSurf(c.mdot)","c.intGas(c.cuDt)-c.intSurf(c.rhoCopper*c.vnSurf)","c.intGas(c.cuReactionSum)","c.intGas(c.Ycu*c.continuityResidual)","c.intGas(abs(c.Ycu*c.continuityResidual))","c.intSurf(c.rhoCopper*c.vnSurf)"};
 m.sol(saved).updateSolution();m.result().numerical().create("curate","EvalGlobal");m.result().numerical("curate").set("data","dset1");m.result().numerical("curate").set("expr",ex);ExportNative.csv(a[1],ex,m.sol(saved).getPVals(),m.result().numerical("curate").getReal());System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
