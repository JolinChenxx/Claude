import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Export only actual saved states from both native plasma components. */
public class ExportTwoZone {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("twoZoneExport",a[0]);String saved=ExportNative.savedSolution(m);ExportNative.dataset(m,saved);
  m.component("c").cpl().create("maxTarget","Maximum");m.component("c").cpl("maxTarget").selection().geom("g",1);m.component("c").cpl("maxTarget").selection().named("surface");m.component("c").cpl("maxTarget").set("opname","maxTarget");
  m.component("c").cpl().create("intBase","Integration");m.component("c").cpl("intBase").selection().geom("g",1);m.component("c").cpl("intBase").selection().named("bottom");m.component("c").cpl("intBase").set("axisym",true);m.component("c").cpl("intBase").set("intorder",8);
  ArrayList<String> ex=new ArrayList<>();
  for(String comp:new String[]{"c","outer"}){
   String prefix=comp+".",p=comp.equals("c")?"plas":"plas2";
   for(String n:new String[]{"Rp","Hp","Vp","Mgas","vRadius","Tg","TeK","pPl","rhoCopper","nCuTotal","alphaLaser","beamTransmission","Plaser","Psource","PeSource","Qrad","Qff","QeRR","QeAttach","Qei","Ue","Uchem","meanLaserIntensity","tailPhotoEvents","nCuH3","nCuH5","nCuT5","Qfb_CuH3","Qfb_CuH5","tailQfb","Kinetic","Msource","volumeRate"})ex.add(prefix+n);
   ex.add(prefix+p+".ne");ex.add(prefix+p+".rho");ex.add(prefix+"CvVol*"+prefix+"Tg");ex.add(prefix+"gasEnergy*energyScale");
   for(String n:Chemistry.NAMES){ex.add(prefix+p+".n_w"+n);ex.add(prefix+p+".w"+n);}
   ex.add(prefix+p+".wCuT5");
   if(Arrays.asList(m.component(comp).variable(comp.equals("c")?"gasv":"outerGas").varnames()).contains("nCu2")){
    for(int q=2;q<=4;q++){ex.add(prefix+"nCu"+q);ex.add(prefix+p+".w"+(comp.equals("c")?"Cu":"CuX")+"_"+q+"p");}
   }
   for(int k=1;k<=8;k++){ex.add(prefix+"jline"+k);ex.add(prefix+"tauLine"+k+"/"+prefix+"Req");ex.add(prefix+"beta"+k);}
  }
  for(String n:new String[]{"absorbedEnergy*energyScale","radiatedEnergy*energyScale","ambientWork*energyScale","entrainedEnergy*energyScale","removedMass*mScale","outMass*mScale","outEnergy*energyScale","outCuMass*mScale","outMassRate","outPower","outElectronPower","outCuRate","forwardMassRate","reverseMassRate","Pincident","coveredInnerPower","coveredOuterPower","intCu(rhoCu*c.hCond)","intSurf(c.mdotEvap)","intSurf(c.mdotReturn)","intSurf(c.Qsurface)","maxTarget(c.T)","intBase(-c.kCu*(c.Tr*nr+c.Tz*nz))"})ex.add("c."+n);
  for(String n:new String[]{"shellStoredEnergy","shellMass*mScale","totalGasShellEnergy","gasLoadingHeat","shockHeating","Msweep","Vdot","kineticFactor"})ex.add("outer."+n);
  boolean prescribed=Arrays.asList(m.component("c").variable("gasv").varnames()).contains("thermalSourceMode");
  if(prescribed)for(String n:new String[]{"thermalSourceMode","prescribedTargetK","prescribedTargetAbsorption"})ex.add("c."+n);
  String[] eval=ex.toArray(new String[0]);
  if(prescribed)for(int i=0;i<eval.length;i++){
   if(eval[i].contains("intCu("))eval[i]="0[J]";
   else if(eval[i].contains("intBase("))eval[i]="0[W]";
   else if(eval[i].contains("maxTarget("))eval[i]="c.prescribedTargetK";
   else if(eval[i].contains("intSurf(c.mdotEvap)"))eval[i]="thermalMass(t/1[s])*1[kg/s]";
   else if(eval[i].contains("intSurf(c.mdotReturn)"))eval[i]="0[kg/s]";
   else if(eval[i].contains("intSurf(c.Qsurface)"))eval[i]="c.prescribedTargetAbsorption";
  }
  m.result().numerical().create("twoZoneHistory","EvalGlobal");m.result().numerical("twoZoneHistory").set("data","dset1");m.result().numerical("twoZoneHistory").set("expr",eval);
  m.sol(saved).updateSolution();ExportNative.csv(a[1],ex.toArray(new String[0]),m.sol(saved).getPVals(),m.result().numerical("twoZoneHistory").getReal());
  System.out.println("TWO ZONE SAVED STATES EXPORTED "+m.sol(saved).getPVals().length);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
