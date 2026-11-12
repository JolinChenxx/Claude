import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Export real saved native populations and the conservative axial ledgers. */
public class ExportAxialPlume {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("axialExport",a[0]);System.out.println("AXIAL MODEL LOADED");String saved=ExportNative.savedSolution(m);ExportNative.dataset(m,saved);int n=Integer.parseInt(m.variable("axialFlux").get("axialCellCount"));
  ArrayList<String> ex=new ArrayList<>();
  boolean grid=Arrays.asList(m.variable("axialFlux").varnames()).contains("gridPlumeMode");
  for(int j=0;j<n;j++){
   String cell="cell"+(char)('A'+j),p="plasma"+(char)('A'+j),prefix=cell+".";
   for(String s:new String[]{"zCenter","zLower","zUpper","dz","Rp","Vp","u","rhoP","rhoCopper","nCuTotal","Tg","TeK","pPl","Msource","Psource","Plaser","Qrad","Qff","QeRR","Qei","Ue","Uchem","Etot","nCuH3","nCuH5","nCuT5","Qfb_CuH3","Qfb_CuH5","tailQfb","nCu2","nCu3","nCu4","alphaLaser","beamTransmission","laserTau"})ex.add(prefix+s);
   ex.add(prefix+p+".ne");for(String s:Chemistry.NAMES){ex.add(prefix+p+".w"+s);ex.add(prefix+p+".n_w"+s);}ex.add(prefix+p+".wCuT5");
   if(grid)for(String s:new String[]{"rLower","rUpper","rCenter","uR","uZ"})ex.add(prefix+s);
   for(int q=2;q<=4;q++)ex.add(prefix+p+".wCu_"+q+"p");
   for(int k=1;k<=8;k++){ex.add(prefix+"jline"+k);ex.add(prefix+"tauLine"+k+"/"+prefix+"Req");}
  }
  for(String s:new String[]{"axialTotalEnergy","axialCopper","axialRadiation","axialLaser","axialIncident","targetMassSource","targetPowerSource","axialOutCu","energyF"+n,"axialCellCount"})ex.add(s);
  if(Arrays.asList(m.variable("axialFlux").varnames()).contains("prescribedUpstreamOutflow"))ex.add("prescribedUpstreamOutflow");
  if(grid)for(String s:new String[]{"gridPlumeMode","gridRadialCount","gridAxialCount"})ex.add(s);
  for(String s:new String[]{"axialInputEnergy*energyScale","axialRadiatedEnergy*energyScale","axialOutEnergy*energyScale","axialInputCu*mScale","axialOutputCu*mScale"})ex.add("cellA."+s);
  m.result().numerical().create("axialHistory","EvalGlobal");m.result().numerical("axialHistory").set("data","dset1");m.result().numerical("axialHistory").set("expr",ex.toArray(new String[0]));System.out.println("UPDATE SAVED SOLUTION");m.sol(saved).updateSolution();System.out.println("EVALUATE "+ex.size()+" GLOBAL EXPRESSIONS");
  ExportNative.csv(a[1],ex.toArray(new String[0]),m.sol(saved).getPVals(),m.result().numerical("axialHistory").getReal());System.out.println("AXIAL SAVED STATES EXPORTED "+m.sol(saved).getPVals().length);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
