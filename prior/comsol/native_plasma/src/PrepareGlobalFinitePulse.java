import com.comsol.model.*;
import com.comsol.model.util.*;

/** Timing/envelope sensitivity, not a measured laser waveform.
 * Experimental t=0 is assigned to the leading half-maximum of a 60 ns pulse.
 * The old comparison shift remains 118 ns. No camera or rate parameters change.
 */
public class PrepareGlobalFinitePulse {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("finitePulse",a[0]);
  for(String s:m.sol().tags())m.sol(s).clearSolutionData();
  m.param().set("pulseStart","118[ns]");
  m.param().set("pulseEdge","1[ns]");
  m.component("c").variable("gasv").set("pulse","0.5*(erf((t-pulseStart)/pulseEdge)-erf((t-pulseStart-tau)/pulseEdge))");
  m.component("c").variable("gasv").set("Pincident","Ein/tau*pulse");
  m.component("c").variable("gasv").set("Ipeak","2*Ein/(pi*w0^2*tau)");
  m.sol("sol1").feature("v1").set("initmethod","init");
  m.sol("sol1").feature("t1").set("maxstepbdf","0.5[ns]");
  m.label("Native global Plasma: explicit 5d, finite 60ns pulse from comparison zero; unmeasured envelope sensitivity");
  m.save(a[1]);
  m.save(a[1].replace(".mph",".java"),"java");
  System.out.println("FINITE PULSE READY:400uJ,60ns FWHM,118ns leading edge,1ns erf transition; unmeasured envelope");
  System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
