import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;

/** Eliminated Cu 5d 2D5/2: collisional excitation, ionization, recombination,
 * and 1064 nm photoionization. The parent and ion remain native species.
 */
public class AddTailPhoto {
 static Model m;
 static void v(String n,String s){m.component("c").variable("gasv").set(n,s);}
 static void rx(String tag,String formula,String rate){
  m.component("c").physics("plas").create(tag,"Reaction",2);
  m.component("c").physics("plas").feature(tag).set("formula",formula);
  m.component("c").physics("plas").feature(tag).set("kf",rate);
 }
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.load("tail",a[0]);
  m.param().set("tailPhotoOn",a.length>2?a[2]:"1");
  m.param().set("tailA","2.1e7[1/s]");m.param().set("tailDelta","3.0509536[V]");
  m.param().set("tailI","0.8587344[V]");m.param().set("tailParentI","3.909688[V]");
  m.param().set("tailF","0.07794434001494746");
  m.param().set("tailSigma0","6.3e-22[m^2]*sqrt(13.605693[V]/tailI)");
  m.func().create("tailRates","Interpolation");m.func("tailRates").set("source","file");
  m.func("tailRates").set("filename","D:/junlin/PlasmaPlume/route1/data/tail5d_log_rates.txt");
  m.func("tailRates").set("nargs",1);m.func("tailRates").set("funcs",new String[][]{{"tailUp","1"},{"tailDn","2"},{"tailIon","3"},{"tailRec","4"},{"tailRR","5"},{"tailRRmean","6"}});
  m.func("tailRates").set("interp","piecewisecubic");m.func("tailRates").set("extrap","const");m.func("tailRates").importData();
  v("tailKu","10^tailUp(log10(TeV/1[V]))*1[m^3/s]");
  v("tailKd","10^tailDn(log10(TeV/1[V]))*1[m^3/s]");
  v("tailKi","10^tailIon(log10(TeV/1[V]))*1[m^3/s]");
  v("tailK3","10^tailRec(log10(TeV/1[V]))*1[m^6/s]");
  v("tailKrr","10^tailRR(log10(TeV/1[V]))*1[m^3/s]");
  v("tailEphRR","10^tailRRmean(log10(TeV/1[V]))*1[V]");
  v("tailDnu","1/(406.2638[nm])*sqrt(2*k_B_const*Tgas/(Mcu/N_A_const))");
  v("tailTau","e_const^2/(4*epsilon0_const*me_const*c_const)*tailF/(sqrt(pi)*tailDnu)*nCuP3*w0");
  v("tailBeta","if(tailTau<1e-3,1-tailTau/2+tailTau^2/6,(1-exp(-tailTau))/tailTau)");
  v("tailPhoton","h_const*c_const/lambdaL");
  v("tailSigma","tailPhotoOn*tailSigma0*(tailI*e_const/tailPhoton)^3");
  v("tailPhotoRate","tailSigma*Ibeam*(optD+optU)/tailPhoton");
  v("tailLowerRate","tailA*tailBeta+plas.ne*tailKd");
  v("tailUpperLoss","tailLowerRate+plas.ne*tailKi+tailPhotoRate");
  v("tailPopulation","(plas.ne*nCuP3*tailKu+plas.ne^2*nCu_1p*tailK3+plas.ne*nCu_1p*tailKrr)/tailUpperLoss");
  v("tailForwardK","tailKu*(plas.ne*tailKi+tailPhotoRate)/tailUpperLoss");
  v("tailReverse3K","tailK3*tailLowerRate/tailUpperLoss");
  v("tailReverseRRK","tailKrr*tailLowerRate/tailUpperLoss");
  v("tailNetRate","plas.ne*nCuP3*tailForwardK-plas.ne^2*nCu_1p*tailReverse3K-plas.ne*nCu_1p*tailReverseRRK");
  v("tailQphoto","tailPopulation*tailPhotoRate*tailPhoton");
  v("tailQrad","e_const*(tailPopulation*tailA*tailBeta*tailDelta+plas.ne*nCu_1p*tailKrr*tailEphRR)");
  v("tailQelectron","tailQphoto-tailQrad-e_const*tailParentI*tailNetRate");
  v("tailFraction","tailPopulation/(nCuP3+1[1/m^3])");
  // General reaction nodes supply number and species sources only. Their
  // electron energy is provided explicitly by the exact QSS energy identity.
  rx("tailForward","e+CuP3=>2e+Cu+","tailForwardK*N_A_const");
  rx("tailReverse3","2e+Cu+=>e+CuP3","tailReverse3K*N_A_const^2");
  rx("tailReverseRR","e+Cu+=>CuP3","tailReverseRRK*N_A_const");
  // The inherited compressible/trace corrections override native heavy-species
  // rate expressions with the earlier reaction list. New reaction nodes update
  // electron sources automatically, but those two overridden heavy rates must
  // receive the matching stoichiometric increment explicitly.
  for(String n:new String[]{"CuP3","Cu_1p"}){
   String oldRate=ConservativeTrace.rate(m,n);
   m.component("c").physics("plas").feature(n).featureInfo("info").set("plas.R_w"+n,new String[]{"("+oldRate+")"+(n.equals("CuP3")?"-":"+")+"tailNetRate/N_A_const"});
  }
  String oldAlpha=m.component("c").variable("gasv").get("alphaLaser");
  String oldRad=m.component("c").variable("gasv").get("Qrad");
  v("alphaIB",oldAlpha);v("alphaLaser","alphaIB+tailPopulation*tailSigma");
  v("QradBase",oldRad);v("Qrad","QradBase+tailQrad");
  String power=m.component("c").physics("plas").feature("power").getString("Qgen");
  m.component("c").physics("plas").feature("power").set("Qgen",power+"-tailQrad-e_const*tailParentI*tailNetRate");
  m.sol("sol1").feature("v1").set("initmethod","init");
  m.sol("sol1").feature("t1").set("maxstepconstraintbdf","expr");m.sol("sol1").feature("t1").set("maxstepexpressionbdf","if(t<80[ns],2[ns],0.5[ns])");
  m.label("Native spatial Plasma + Cu 5d assisted ionization/photoabsorption; inherited geometry and source; QSS validity requires audit");
  m.save(a[1]);System.out.println("TAIL 5D READY: source-conserving QSS, photo="+m.param().get("tailPhotoOn"));System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
