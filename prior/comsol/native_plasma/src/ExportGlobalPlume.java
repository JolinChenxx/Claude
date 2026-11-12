import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;
/** Actual saved global-plasma states and their thermal/momentum ledger. */
public class ExportGlobalPlume {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("globalExport",a[0]);String saved=ExportNative.savedSolution(m);ExportNative.dataset(m,saved);
 m.component("c").cpl().create("maxTarget","Maximum");m.component("c").cpl("maxTarget").selection().geom("g",1);m.component("c").cpl("maxTarget").selection().named("surface");m.component("c").cpl("maxTarget").set("opname","maxTarget");
 m.component("c").cpl().create("intBase","Integration");m.component("c").cpl("intBase").selection().geom("g",1);m.component("c").cpl("intBase").selection().named("bottom");m.component("c").cpl("intBase").set("axisym",true);m.component("c").cpl("intBase").set("intorder",8);
 ArrayList<String> ex=new ArrayList<>();
 for(String s:new String[]{"Rp","Hp","Vp","Mgas","vRadius","vHeight","Tg","TeK","plas.ne","pPl","rhoCopper","nCuTotal","alphaLaser","beamTransmission","Plaser","Pincident","Psource","PeSource","Qrad","Qff","QeRR","QeAttach","Qei","plas.Sen","Ue","Uchem","CvVol*Tg","Kinetic","gasEnergy*energyScale","absorbedEnergy*energyScale","radiatedEnergy*energyScale","ambientWork*energyScale","entrainedEnergy*energyScale","removedMass*mScale","intCu(rhoCu*hCond)","intSurf(mdotEvap)","intSurf(mdotReturn)","intSurf(Qsurface)","intSurf(Qmass)","intSurf(Qexchange)","maxTarget(T)","Msweep","Msource","volumeRate","nCu_1p","nCuH3","nCuH5"})ex.add("c."+s);
 for(int i=0;i<ex.size();i++){
  String e=ex.get(i).replace("CvVol*Tg","CvVol*c.Tg").replace("rhoCu*hCond","rhoCu*c.hCond").replace("maxTarget(T)","maxTarget(c.T)");
  for(String name:new String[]{"mdotEvap","mdotReturn","Qsurface","Qmass","Qexchange"})e=e.replace("intSurf("+name+")","intSurf(c."+name+")");ex.set(i,e);
 }
 for(String n:Chemistry.NAMES){ex.add("c.plas.n_w"+n);ex.add("c.plas.R_w"+n);}
 if(Arrays.asList(m.component("c").variable("gasv").varnames()).contains("shellStoredEnergy")){
  for(String n:new String[]{"shellStoredEnergy","totalGasShellEnergy","shellMass*mScale","inertialMass","gasLoadingHeat","shockHeating"})ex.add("c."+n);
 }
 if(Arrays.asList(m.component("c").variable("gasv").varnames()).contains("outMassRate")){
  for(String n:new String[]{"outMassRate","outPower","outElectronPower","outCuRate","outMass*mScale","outEnergy*energyScale","outCuMass*mScale","nozzleRatio","gammaNozzle"})ex.add("c."+n);
 }
 if(Arrays.asList(m.component("c").physics("plas").feature().tags()).contains("CuT5")){
  for(String n:new String[]{"nCuT5","plas.R_wCuT5","tailQfb","tailQline","tailPhotoEvents","tailNetExcitation","tailNetIonization","alphaPhoto","alphaIB","meanLaserIntensity"})ex.add("c."+n);
 }
 for(String n:new String[]{"Qfb_CuH3","Qfb_CuH5","ffVisibleFraction","jfbVisible","jVisible","plas.Nn0","plas.neinit"})ex.add("c."+n);
 ex.add("c.intBase(-c.kCu*(c.Tr*nr+c.Tz*nz))");
 for(int i=1;i<=8;i++){ex.add("c.jline"+i);ex.add("c.tauLine"+i+"/c.Req");ex.add("c.beta"+i);}
 boolean fed=Arrays.asList(m.component("c").variable("gasv").varnames()).contains("feedSourceMode");
 if(fed)for(String n:new String[]{"feedSourceMode","feedTargetTemperature","feedLaserAbsorption","feedTargetAbsorption"})ex.add("c."+n);
 String[] eval=ex.toArray(new String[0]);
 if(fed)for(int i=0;i<eval.length;i++){
  if(eval[i].contains("intCu("))eval[i]="0[J]";
  else if(eval[i].contains("intBase("))eval[i]="0[W]";
  else if(eval[i].contains("maxTarget("))eval[i]="c.feedTargetTemperature";
  else if(eval[i].contains("intSurf(c.mdotEvap)"))eval[i]="c.Msource";
  else if(eval[i].contains("intSurf(c.mdotReturn)"))eval[i]="0[kg/s]";
  else if(eval[i].contains("intSurf(c.Qsurface)")||eval[i].contains("intSurf(c.Qexchange)"))eval[i]="0[W]";
  else if(eval[i].contains("intSurf(c.Qmass)"))eval[i]="c.Psource";
 }
 m.result().numerical().create("globalHistory","EvalGlobal");m.result().numerical("globalHistory").set("data","dset1");m.result().numerical("globalHistory").set("expr",eval);
 m.sol(saved).updateSolution();ExportNative.csv(a[1],ex.toArray(new String[0]),m.sol(saved).getPVals(),m.result().numerical("globalHistory").getReal());System.out.println("GLOBAL SAVED STATES EXPORTED "+m.sol(saved).getPVals().length);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}}
