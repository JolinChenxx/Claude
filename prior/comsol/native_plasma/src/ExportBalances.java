import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
public class ExportBalances {
 public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);String saved=ExportNative.savedSolution(m);
  System.out.println("BALANCE LOAD "+saved);ExportNative.dataset(m,saved);
  for(String n:new String[]{"Far","Base"}){m.component("c").cpl().create("balance"+n,"Integration");m.component("c").cpl("balance"+n).set("opname","balance"+n);m.component("c").cpl("balance"+n).selection().geom("g",1);m.component("c").cpl("balance"+n).selection().named(n.equals("Far")?"far":"bottom");m.component("c").cpl("balance"+n).set("axisym",true);}
  for(String op:new String[]{"intGas","intSurf","intCu"})m.component("c").cpl(op).set("axisym",true);
  m.component("c").variable().create("balanceFarVars");m.component("c").variable("balanceFarVars").selection().geom("g",1);m.component("c").variable("balanceFarVars").selection().named("far");
  String cu="0[kg/(m^2*s)]",ion="0[mol/(m^2*s)]";
  for(String n:Chemistry.NAMES){String flux="(plas.tfluxr_w"+n+"*nr+plas.tfluxz_w"+n+"*nz)";if(n.startsWith("Cu"))cu+="+"+flux;
   if(n.endsWith("_1p"))ion+="+"+flux+"/plas.M_w"+n;if(n.endsWith("_1m"))ion+="-"+flux+"/plas.M_w"+n;}
  m.component("c").variable("balanceFarVars").set("balanceCuFlux",cu);
  m.component("c").variable("balanceFarVars").set("balanceChargeFlux","F_const*("+ion+")-e_const*((plas.gflux_ner+plas.ne*urP)*nr+(plas.gflux_nez+plas.ne*uzP)*nz)");
  System.out.println("BALANCE FLUX VARIABLES");String[][] ga=m.component("c").physics("flow").feature("gfeq1").getStringMatrix("Ga");
  m.component("c").variable("gasv").set("balanceEnergyR","rho0*e0*("+ga[3][0]+")/max(r/Rdom,1e-12)");
  m.component("c").variable("gasv").set("balanceEnergyZ","rho0*e0*("+ga[3][1]+")/max(r/Rdom,1e-12)");
  m.component("c").variable("balanceFarVars").set("balanceEnergyFlux","balanceEnergyR*nr+balanceEnergyZ*nz");
  ArrayList<String> ex=new ArrayList<>(Arrays.asList("c.balanceFar(c.balanceCuFlux)","c.balanceFar(c.balanceChargeFlux)","c.balanceFar(c.balanceEnergyFlux)","c.balanceBase(-c.kCu*(c.Tr*nr+c.Tz*nz))","c.intSurf(c.Qsurface)","c.intGas(c.Qlaser)","c.intGas(c.Qrad)","c.intGas(c.Qff)","c.intSurf(c.QworkWall)","c.intSurf(c.mdot)","c.intSurf(c.Iwall)","c.qmetal*1[pC]","c.intGas(c.plas.scharge)","c.intGas(c.rhoCopper)","c.intCu(rhoCu)","c.intGas(rho0*e0*c.qE)","c.intCu(rhoCu*c.hCond+rhoCu*(c.urCu^2+c.uzCu^2)/2)","c.intGas(epsilon0_const*(c.plas.Etr^2+c.plas.Etz^2)/2)","c.intSurf(c.recoil*(c.urCu*c.ncr+c.uzCu*c.ncz))","c.intCu(c.darcy*(c.urCu^2+c.uzCu^2))"));
  if(a.length>2&&a[2].equals("trace"))ex.addAll(Arrays.asList("c.intGas(c.QnumCuExc)","c.intGas(c.SneNumerical)","c.intGas(abs(c.SneNumerical))","c.intGas(4*pi*c.jVisible)"));
  System.out.println("BALANCE UPDATE");m.sol(saved).updateSolution();m.result().numerical().create("balances","EvalGlobal");m.result().numerical("balances").set("data","dset1");m.result().numerical("balances").set("expr",ex.toArray(new String[0]));System.out.println("BALANCE EVALUATE");
  ExportNative.csv(a[1],ex.toArray(new String[0]),m.sol(saved).getPVals(),m.result().numerical("balances").getReal());System.out.println("BALANCES EXPORTED "+saved);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
