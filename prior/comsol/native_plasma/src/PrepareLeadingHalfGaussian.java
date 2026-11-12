import com.comsol.model.*;import com.comsol.model.util.*;
/** Temporal-envelope uncertainty: keep camera shift, put the two half maxima
 * at the reported beginning/end of 0-60 ns irradiation. Not a measured pulse. */
public class PrepareLeadingHalfGaussian {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("leadingHalfGaussian",a[0]);
  for(String s:m.sol().tags())m.sol(s).clearSolutionData();
  m.param().set("tp","118[ns]+tau/2");
  m.component("c").variable("gasv").set("pulse","exp(-4*log(2)*((t-tp)/tau)^2)");
  m.component("c").variable("gasv").set("Pincident","Ein/(tau*sqrt(pi/(4*log(2))))*pulse");
  m.component("c").variable("gasv").set("Ipeak","2*Ein/(pi*w0^2*tau*sqrt(pi/(4*log(2))))");
  if(a.length>2){
   String[][] funcs=m.func("thermalFeed").getStringMatrix("funcs");m.func().remove("thermalFeed");
   m.func().create("thermalFeed","Interpolation");m.func("thermalFeed").set("source","file");m.func("thermalFeed").set("filename",a[2]);m.func("thermalFeed").set("nargs",1);m.func("thermalFeed").set("funcs",funcs);m.func("thermalFeed").set("interp","linear");m.func("thermalFeed").set("extrap","const");m.func("thermalFeed").importData();
  }
  m.sol("sol1").feature("v1").set("initmethod","init");
  m.label("Gaussian pulse half maxima at experimental0/60ns,118ns camera shift; unmeasured envelope sensitivity");
  m.save(a[1]);System.out.println("LEADING HALF GAUSSIAN READY; normalization400uJ; centre148ns, FWHM60ns");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
