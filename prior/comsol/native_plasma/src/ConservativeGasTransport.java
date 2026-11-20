import com.comsol.model.*;
import com.comsol.model.util.*;
/** Numerical mass diffusion with matched momentum, enthalpy and species transport. */
public class ConservativeGasTransport {
 static void g(Model m,String n,String e){m.component("c").variable("gasv").set(n,e);}
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 g(m,"JmassR","-Dnum*d(rhoP,r)");g(m,"JmassZ","-Dnum*d(rhoP,z)");
 g(m,"uMassR","urP+JmassR/rhoSafe");g(m,"uMassZ","uzP+JmassZ/rhoSafe");
 g(m,"divUMass","d(uMassR,r)+uMassR/max(r,1e-12[m])+d(uMassZ,z)");
 String[][] ga=m.component("c").physics("flow").feature("gfeq1").getStringMatrix("Ga");
 String[] jm={"JmassR","JmassZ"};
 for(int j=0;j<2;j++){
  ga[0][j]+="+axisWeight*"+jm[j]+"/rho0";
  ga[1][j]+="+axisWeight*urP*"+jm[j]+"/(rho0*u0)";
  ga[2][j]+="+axisWeight*uzP*"+jm[j]+"/(rho0*u0)";
  ga[3][j]+="+axisWeight*(rho0*e0*qE+pPl)*"+jm[j]+"/(rhoSafe*rho0*e0)";
 }
 m.component("c").physics("flow").feature("gfeq1").set("Ga",ga);
 m.component("c").physics("plas").feature("pes1").set("u",new String[]{"uMassR","0","uMassZ"});
 for(String n:Chemistry.NAMES){
  m.component("c").physics("plas").feature(n).set("Dfs","Dgas+Dnum");
  if(n.endsWith("_1p")||n.endsWith("_1m"))m.component("c").physics("plas").feature(n).featureInfo("info").set("plas.um_w"+n,new String[]{"e_const*Dgas/(k_B_const*Tgas)"});
 }
 // The artificial diffusivity is excluded from electrical mobility.
 m.component("c").physics("plas").prop("InconsistentStabilization").set("IsotropicDiffusionIons",false);
 m.component("c").physics("plas").feature("bulkconv").set("Re","-uMassR*d(plas.ne,r)-uMassZ*d(plas.ne,z)-plas.ne*divUMass+SneNumerical");
 m.component("c").physics("plas").feature("power").set("Qgen","Qlaser-QeRR-QeAttach-Qff-uMassR*d(Ue,r)-uMassZ*d(Ue,z)-(Ue+pe)*divUMass-Qei+1.5*e_const*TeV*SneNumerical");
 m.component("c").physics("plas").feature("wallConv").set("weakExpression","(vnSurf-uMassR*ncr-uMassZ*ncz)*(plas.ne*test(Ne)+plas.en*test(En))");
 m.save(a[1]);System.out.println("CONSERVATIVE GAS TRANSPORT READY; NUMERICAL DIFFUSION REQUIRES LATER MESH CHECK");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
