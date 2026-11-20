import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
/** Mechanical terms of the implemented moving-interface energy budget.
 * These terms diagnose mechanical exchange/dissipation; they are not an
 * assertion that all mechanical sinks have already been returned to heat.
 */
public class ExportMovingMechanics {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("movingWork",a[0]);String saved=ExportNative.savedSolution(m);ExportNative.dataset(m,saved);
 for(String op:new String[]{"intSurf","intCu"}){m.component("c").cpl(op).set("axisym",true);m.component("c").cpl(op).set("intorder",8);}
 m.component("c").variable("boundv").set("movingUn","urCu*ncr+uzCu*ncz");
 m.component("c").variable("solidv").set("movingViscous","2*0.005[Pa*s]*(d(urCu,r)^2+d(uzCu,z)^2+(urCu/max(r,1e-12[m]))^2+0.5*(d(urCu,z)+d(uzCu,r))^2)");
 String[] ex={"c.intSurf(c.pPl*c.vnSurf)","c.intSurf(-(c.pPl-pAmb+c.recoil)*c.movingUn)","c.intSurf(-c.sigmaCu*(dtang(c.urCu,r)+dtang(c.uzCu,z)+c.urCu/max(r,1e-12[m])))","c.intSurf(-c.mdot*(c.urCu^2+c.uzCu^2)/2)","c.intCu(-c.darcy*(c.urCu^2+c.uzCu^2))","c.intCu(-c.movingViscous)","c.intSurf(c.pPl*(c.vnSurf-c.movingUn))","c.intSurf(pAmb*c.movingUn)","c.intSurf(c.mdot)","c.intSurf((c.vnPhysical-c.vnSurf)*rhoCu)"};
 m.sol(saved).updateSolution();m.result().numerical().create("mw","EvalGlobal");m.result().numerical("mw").set("data","dset1");m.result().numerical("mw").set("expr",ex);ExportNative.csv(a[1],ex,m.sol(saved).getPVals(),m.result().numerical("mw").getReal());
 for(String f:new String[]{"fluid1","laser"})InspectFeatures.dump(m.component("c").physics("ht").feature(f),"heat "+f);
 System.out.println("MOVING MECHANICAL TERMS EXPORTED");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
