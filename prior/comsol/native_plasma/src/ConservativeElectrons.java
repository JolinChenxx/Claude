import com.comsol.model.*;
import com.comsol.model.util.*;
/** Write electron/energy convection as weak conservative fluxes, including ALE boundaries. */
public class ConservativeElectrons {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 m.component("c").physics("plas").feature("bulkconv").set("Re","SneNumerical");
 m.component("c").physics("plas").feature("power").set("Qgen","Qlaser-QeRR-QeAttach-Qff-pe*divUMass-Qei+1.5*e_const*TeV*SneNumerical");
 m.component("c").physics("plas").create("conservativeElectronFlux","WeakContribution",2);m.component("c").physics("plas").feature("conservativeElectronFlux").selection().named("g_gas_dom");
 m.component("c").physics("plas").feature("conservativeElectronFlux").set("weakExpression","plas.ne*(uMassR*test(Ner)+uMassZ*test(Nez))+plas.en*(uMassR*test(Enr)+uMassZ*test(Enz))");
 m.component("c").physics("plas").feature("wallConv").set("weakExpression","vnSurf*(plas.ne*test(Ne)+plas.en*test(En))");
 m.component("c").physics("plas").create("electronFarConv","WeakContribution",1);m.component("c").physics("plas").feature("electronFarConv").selection().named("far");m.component("c").physics("plas").feature("electronFarConv").set("weakExpression","-(uMassR*nr+uMassZ*nz)*(plas.ne*test(Ne)+plas.en*test(En))");
 // Native residual stabilization sees only native expressions, not the added flux.
 for(String s:new String[]{"StreamlineDiffusionElectronsLog","CrosswindDiffusionElectronsLog"})m.component("c").physics("plas").prop("ConsistentStabilization").set(s,false);
 m.save(a[1]);System.out.println("CONSERVATIVE ELECTRON CONVECTION READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
