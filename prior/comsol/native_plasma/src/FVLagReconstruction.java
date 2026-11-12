import com.comsol.model.*;
import com.comsol.model.util.*;
/** Lag only the FV reconstructed flux Jacobian; residual/physics unchanged. */
public class FVLagReconstruction {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);
 String[][] ga=m.component("c").physics("flow").feature("gfeq1").getStringMatrix("Ga");
 for(int j=0;j<2;j++){String d=j==0?"r":"z";ga[3][j]=ga[3][j].replace("Hdiff"+d,"nojac(Hdiff"+d+")").replace("plas.gflux_en"+d,"nojac(plas.gflux_en"+d+")");}
 m.component("c").physics("flow").feature("gfeq1").set("Ga",ga);
 m.save(a[1]);System.out.println("FV FLUX JACOBIAN LAGGED; residual unchanged");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
