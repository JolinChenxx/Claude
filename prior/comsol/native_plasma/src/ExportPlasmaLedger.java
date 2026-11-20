import com.comsol.model.*;import com.comsol.model.util.*;
/** Integrated actual gas state and interface powers; no inferred image gain. */
public class ExportPlasmaLedger {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("ledgerExport",a[0]);String saved=ExportNative.savedSolution(m);ExportNative.dataset(m,saved);
 for(String op:new String[]{"intGas","intSurf"})m.component("c").cpl(op).set("axisym",true);
 String[] ex={"c.intGas(c.rhoCopper)","c.intGas(rho0*e0*c.qE)","c.intGas(c.CvVol*c.Tg)","c.intGas(c.Uchem)","c.intGas(c.Ue)","c.intGas(0.5*c.rhoP*(c.urP^2+c.uzP^2))","c.intGas(c.Qlaser)","c.intGas(c.Qrad)","c.intSurf(c.Qsurface)","c.intSurf(c.Qmass)","c.intSurf(c.Qexchange)","c.intSurf(c.Qthermion)","c.intSurf(c.mdotEvap)","c.intSurf(c.mdotReturn)","c.intSurf(c.Iwall)","c.intGas(c.plas.scharge)","c.intGas(c.plas.ne)","c.intGas(c.plas.Re)","c.intGas(c.Qei)","c.intGas(c.jline3)","c.intGas(c.jline4+c.jline5+c.jline6)"};
 m.sol(saved).updateSolution();m.result().numerical().create("plasmaLedger","EvalGlobal");m.result().numerical("plasmaLedger").set("data","dset1");m.result().numerical("plasmaLedger").set("expr",ex);
 ExportNative.csv(a[1],ex,m.sol(saved).getPVals(),m.result().numerical("plasmaLedger").getReal());System.out.println("PLASMA LEDGER EXPORTED");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
