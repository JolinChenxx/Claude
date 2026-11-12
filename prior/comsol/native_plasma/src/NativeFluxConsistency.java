import com.comsol.model.*;
import com.comsol.model.util.*;
/** Couple the actual native diffusive flux and use matched wall quadrature. */
public class NativeFluxConsistency {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 m.component("c").physics("plas").prop("TransportSettings").set("MixtureDiffusionCorrection",true);
 for(String n:Chemistry.NAMES)for(String d:new String[]{"r","z"})m.component("c").physics("plas").feature(n).featureInfo("info").set("plas.tflux"+d+"_w"+n,new String[]{"plas.cflux"+d+"_w"+n+"+plas.j"+d+"_w"+n});
 // Native Plasma 6.4's FEM wall weak terms use order 2. Their integrals can
 // differ drastically from the conductor integral when emitted/return fluxes
 // nearly cancel. Replace only the quadrature, preserving the same wall law.
 String wall="(plas.ne_bnd+plas.tne_bnd+plas.nemig_bnd)*test(Ne)+(plas.en_bnd+plas.ten_bnd+plas.enmig_bnd)*test(En)";
 for(int q:new int[]{2,8}){String tag="wallQuadrature"+q;m.component("c").physics("plas").create(tag,"WeakContribution",1);m.component("c").physics("plas").feature(tag).selection().named("surface");m.component("c").physics("plas").feature(tag).set("automaticQuadrature",false);m.component("c").physics("plas").feature(tag).set("integrationOrder",q);m.component("c").physics("plas").feature(tag).set("weakExpression",(q==2?"-(":"(")+wall+")");}
 m.component("c").cpl("intSurf").set("intorder",8);
 m.component("c").cpl().create("metalLine","Integration");m.component("c").cpl("metalLine").set("opname","metalLine");m.component("c").cpl("metalLine").selection().geom("g",1);m.component("c").cpl("metalLine").selection().named("surface");m.component("c").cpl("metalLine").set("axisym",false);m.component("c").cpl("metalLine").set("intorder",8);
 m.component("c").physics("plas").feature("metal").featureInfo("info").set("plas.I_1",new String[]{"-metalLine(d(V_lm,TIME)+V_lm*lineAreaRate)-intSurf(Iwall)"});
 for(String f:new String[]{"wallConv","metalReactionCorrection"}){m.component("c").physics("plas").feature(f).set("automaticQuadrature",false);m.component("c").physics("plas").feature(f).set("integrationOrder",8);}
 m.save(a[1]);System.out.println("NATIVE DIFFUSION ENTHALPY + MATCHED ORDER-8 WALL QUADRATURE READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
