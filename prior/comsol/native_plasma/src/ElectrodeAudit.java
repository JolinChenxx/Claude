import com.comsol.model.*;
import com.comsol.model.util.*;
public class ElectrodeAudit {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);String saved=ExportNative.savedSolution(m);ExportNative.dataset(m,saved);
 m.component("c").cpl().create("farE","Integration");m.component("c").cpl("farE").set("opname","farE");m.component("c").cpl("farE").selection().geom("g",1);m.component("c").cpl("farE").selection().named("far");
 for(String op:new String[]{"intSurf","intGas","farE"}){m.component("c").cpl(op).set("axisym",true);m.component("c").cpl(op).set("intorder",10);}
 m.component("c").cpl().create("lineE","Integration");m.component("c").cpl("lineE").set("opname","lineE");m.component("c").cpl("lineE").selection().geom("g",1);m.component("c").cpl("lineE").selection().named("surface");m.component("c").cpl("lineE").set("axisym",false);m.component("c").cpl("lineE").set("intorder",10);
 boolean weak=m.component("c").physics("plas").feature("metal").getString("constraintOptions").equals("weakConstraints");
 String displacement=weak?"c.lineE(d(c.V_lm,TIME)+c.V_lm*(dtang(d(r,TIME),r)+dtang(d(z,TIME),z)))":"c.intSurf(c.plas.metal.nJd)";
 String[] ex={"c.qmetal*1[pC]","c.intGas(c.plas.scharge)","c.intSurf(epsilon0_const*(c.plas.Etr*c.ncr+c.plas.Etz*c.ncz))","c.farE(epsilon0_const*(c.plas.Etr*nr+c.plas.Etz*nz))","c.intSurf(c.Iwall)",displacement,"c.intSurf(c.plas.metal.nJe+c.plas.metal.nJt)","c.intSurf(1)",weak?"c.lineE(-c.V_lm)":"c.qmetal*1[pC]"};
 m.sol(saved).updateSolution();m.result().numerical().create("elAudit","EvalGlobal");m.result().numerical("elAudit").set("data","dset1");m.result().numerical("elAudit").set("expr",ex);ExportNative.csv(a[1],ex,m.sol(saved).getPVals(),m.result().numerical("elAudit").getReal());System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
