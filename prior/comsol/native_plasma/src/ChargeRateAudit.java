import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class ChargeRateAudit {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);String saved=ExportNative.savedSolution(m);ExportNative.dataset(m,saved);
 int order=a.length>2?Integer.parseInt(a[2]):10;
 for(String op:new String[]{"intGas","intSurf"}){m.component("c").cpl(op).set("axisym",true);m.component("c").cpl(op).set("intorder",op.equals("intSurf")?8:order);}
 m.component("c").cpl().create("farQ","Integration");m.component("c").cpl("farQ").set("opname","farQ");m.component("c").cpl("farQ").selection().geom("g",1);m.component("c").cpl("farQ").selection().named("far");m.component("c").cpl("farQ").set("axisym",true);
 String ion="0[1/m^3]",src="0[mol/(m^3*s)]";
 for(String n:Chemistry.NAMES){String sg=n.endsWith("_1p")?"+":n.endsWith("_1m")?"-":null;if(sg!=null){ion+=sg+"plas.n_w"+n;src+=sg+"plas.R_w"+n;}}
 m.component("c").variable("gasv").set("ionNetAudit",ion);m.component("c").variable("gasv").set("ionSourceAudit","N_A_const*("+src+")");
 m.component("c").variable("gasv").set("meshDivAudit","d(d(r,TIME),r)+d(r,TIME)/max(r,1e-12[m])+d(d(z,TIME),z)");
 String[] ex={"c.intGas(c.plas.ne)","c.intGas(c.ionNetAudit)","c.intGas(d(c.plas.ne,TIME)+c.plas.ne*c.meshDivAudit)","c.intGas(d(c.ionNetAudit,TIME)+c.ionNetAudit*c.meshDivAudit)","c.intGas(c.plas.Re)","c.intGas(c.ionSourceAudit)","c.intSurf(c.Gtherm-c.GeIn)","c.intSurf(c.Iwall)/e_const-c.intSurf(c.Gtherm-c.GeIn)","c.farQ((c.plas.gflux_ner+c.plas.ne*c.uMassR)*nr+(c.plas.gflux_nez+c.plas.ne*c.uMassZ)*nz)","c.intGas(c.SneNumerical)","c.intGas(c.plas.scharge)","c.qmetal*1[pC]","d(c.qmetal,TIME)*1[pC]","c.intSurf(c.Iwall)"};
 m.sol(saved).updateSolution();m.result().numerical().create("qrate","EvalGlobal");m.result().numerical("qrate").set("data","dset1");m.result().numerical("qrate").set("expr",ex);ExportNative.csv(a[1],ex,m.sol(saved).getPVals(),m.result().numerical("qrate").getReal());
 for(String ph:new String[]{"pes1","bulkconv","conservativeElectronFlux","wallConv","electronFarConv"})for(String[] row:m.component("c").physics("plas").feature(ph).featureInfo("info").getInfoTable("Weak"))System.out.println(ph+" "+Arrays.toString(row));
 System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}}
