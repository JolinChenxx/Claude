import com.comsol.model.*;import com.comsol.model.util.*;
/** Consistent SUPG for the advected height graph. Its residual vanishes for
 * the exact kinematic jump condition; no imposed lift or surface smoothing. */
public class PrepareSurfaceSUPG {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("heightSUPG",a[0]);
 m.component("c").physics("surfaceODE").create("heightSUPG","WeakContribution",1);
 m.component("c").physics("surfaceODE").feature("heightSUPG").selection().named("surface");
 m.component("c").physics("surfaceODE").feature("heightSUPG").set("weakExpression","-r/Rdom*h*urCu/(2*sqrt(urCu^2+1e-8[m^2/s^2]))*dtang(test(etaSurface),R)*(d(etaSurface,TIME)-meltOn*(uzCu+urCu*ncr/ncz-mdot/(rhoCu*ncz))/1[um])");
 m.sol("sol1").feature("t1").feature("coupledCu").set("jtech","onevery");
 m.label("Copper ALE height transport with consistent SUPG; no prescribed trajectory; ambient predictor only");m.save(a[1]);System.out.println("HEIGHT SUPG READY");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
