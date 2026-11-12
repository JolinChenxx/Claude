import com.comsol.model.*;
import com.comsol.model.util.*;
public class R2NoTrap {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("M",a[0]);
  m.param().set("quenchP","0");m.param().set("trapping","0");
  m.label("R2_COUPLED_NT_ready.mph");m.save(a[1]);
  System.out.println("READY: upper radiative-loss bound, trapping=0 quench=0");
  ModelUtil.clear();System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
}
