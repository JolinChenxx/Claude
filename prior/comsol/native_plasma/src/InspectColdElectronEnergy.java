import com.comsol.model.*;import com.comsol.model.util.*;
public class InspectColdElectronEnergy{public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("inspectCold",a[0]);
 m.result().numerical().create("coldAudit","Interp");m.result().numerical("coldAudit").set("data","dset1");
 String[] ex={"plas.ne","plas.en","plas.Te","TeV","TeK","QeAttach","QeRR","Qei","Qff","Qlaser","plas.R_en","plas.R_e","Tg"};
 m.result().numerical("coldAudit").set("coord",new double[][]{{2.5e-6},{2.5e-6}});m.result().numerical("coldAudit").set("solnum",new int[]{m.sol("sol1").getPVals().length});
 for(String e:ex){try{m.result().numerical("coldAudit").set("expr",new String[]{e});System.out.println(e+"="+m.result().numerical("coldAudit").getData()[0][0][0]);}catch(Throwable z){System.out.println("UNAVAILABLE "+e);}}
 System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
