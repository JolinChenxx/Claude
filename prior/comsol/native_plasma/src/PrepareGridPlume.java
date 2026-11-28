import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Coarse axisymmetric Euler FV grid, one existing native Plasma per cell.
 * No prescribed plume radius. Four radial by six axial cells; diagnostic only. */
public class PrepareGridPlume {
 static Model m;static int nr=4,nz=6;static ArrayList<String> sp=new ArrayList<>();
 static double[] re={0,11e-6,45e-6,130e-6,400e-6},ze={0,9e-6,35e-6,90e-6,190e-6,350e-6,650e-6};
 static String c(int j){return "cell"+(char)('A'+j);}static String p(int j){return "plasma"+(char)('A'+j);}
 static String num(double d){return String.format(Locale.US,"%.14g",d);}
 static void g(String n,String v){m.variable("axialFlux").set(n,v);}static void v(int j,String n,String s){m.component(c(j)).variable("gasv"+(char)('A'+j)).set(n,s);}
 static String s(int j,String n){if(j>=0)return c(j)+"."+(n.startsWith("w")?p(j)+".":"")+n;
  if(n.equals("rhoP"))return "rhoAmb";if(n.equals("pPl"))return "pAmb";if(n.equals("uR")||n.equals("uZ"))return "(0[m/s])";if(n.equals("sound"))return "sqrt(1.4*pAmb/rhoAmb)";if(n.equals("Etot"))return "(2.5*pAmb)";if(n.equals("Ue"))return "(0[J/m^3])";
  if(n.equals("wO2"))return "(0.21*0.0319988[kg/mol]/Mair)";if(n.equals("wN2"))return "(0.79*0.0280134[kg/mol]/Mair)";return "0";}
 static String rf(int i,int k){return "gridR"+i+"Z"+k;}static String zf(int i,int k){return "gridZ"+k+"R"+i;}
 static void wall(String f,double ar,int j,boolean radial){
  String A=num(ar)+"[m^2]";g(f+"mass","0[kg/s]");g(f+"energy","0[W]");g(f+"electron","0[W]");g(f+"velocity","0[m/s]");
  g(f+"momentR",radial?A+"*"+s(j,"pPl"):"0[N]");g(f+"momentZ",radial?"0[N]":A+"*"+s(j,"pPl"));for(String n:sp)g(f+"species"+n,"0[kg/s]");
 }
 static void face(String f,double ar,int l,int r,boolean radial){
  String A=num(ar)+"[m^2]",rl=s(l,"rhoP"),rr=s(r,"rhoP"),pl=s(l,"pPl"),pr=s(r,"pPl"),ul=s(l,radial?"uR":"uZ"),ur=s(r,radial?"uR":"uZ"),a=f+"wave";
  g(a,"max(abs("+ul+")+"+s(l,"sound")+",abs("+ur+")+"+s(r,"sound")+")");g(f+"velocity","0.5*("+ul+"+"+ur+")");
  g(f+"mass",A+"*0.5*("+rl+"*"+ul+"+"+rr+"*"+ur+"-"+a+"*("+rr+"-"+rl+"))");
  for(String comp:new String[]{"R","Z"}){String vl=s(l,"u"+comp),vr=s(r,"u"+comp);boolean normal=radial==comp.equals("R");
   g(f+"moment"+comp,A+"*0.5*("+rl+"*"+ul+"*"+vl+"+"+rr+"*"+ur+"*"+vr+(normal?"+"+pl+"+"+pr:"")+"-"+a+"*("+rr+"*"+vr+"-"+rl+"*"+vl+"))");}
  g(f+"energy",A+"*0.5*(("+s(l,"Etot")+"+"+pl+")*"+ul+"+("+s(r,"Etot")+"+"+pr+")*"+ur+"-"+a+"*("+s(r,"Etot")+"-"+s(l,"Etot")+"))");
  g(f+"electron",A+"*0.5*("+s(l,"Ue")+"*"+ul+"+"+s(r,"Ue")+"*"+ur+"-"+a+"*("+s(r,"Ue")+"-"+s(l,"Ue")+"))");
  for(String n:sp){String dl=rl+"*"+s(l,"w"+n),dr=rr+"*"+s(r,"w"+n);g(f+"species"+n,A+"*0.5*("+dl+"*"+ul+"+"+dr+"*"+ur+"-"+a+"*("+dr+"-"+dl+"))");}
 }
 static String net(int i,int k,String n){return rf(i,k)+n+"-"+rf(i+1,k)+n+"+"+zf(i,k)+n+"-"+zf(i,k+1)+n;}
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);m=ModelUtil.load("gridPlume",a[0]);System.out.println("GRID INPUT LOADED");for(String ss:m.sol().tags())m.sol().remove(ss);
  sp.addAll(Arrays.asList(Chemistry.NAMES));sp.add("CuT5");sp.add("Cu_2p");sp.add("Cu_3p");sp.add("Cu_4p");Map<String,String> chemistry=AxialChemicalRates.rates();
  g("gridPlumeMode","1");g("gridRadialCount","4");g("gridAxialCount","6");
  for(int k=0;k<nz;k++)for(int i=0;i<nr;i++){
   int j=k*nr+i;String sf=""+(char)('A'+j),pl=p(j);double dz=ze[k+1]-ze[k],az=Math.PI*(re[i+1]*re[i+1]-re[i]*re[i]);
   v(j,"rLower",num(re[i])+"[m]");v(j,"rUpper",num(re[i+1])+"[m]");v(j,"rCenter",num((re[i]+re[i+1])/2)+"[m]");v(j,"zLower",num(ze[k])+"[m]");v(j,"zUpper",num(ze[k+1])+"[m]");v(j,"zCenter",num((ze[k]+ze[k+1])/2)+"[m]");v(j,"dz",num(dz)+"[m]");
   v(j,"Vp",num(az*dz)+"[m^3]");v(j,"Rp","rUpper");v(j,"Hp","dz");v(j,"Req","(3*Vp/(4*pi))^(1/3)");v(j,"uZ","moment"+sf+"*momentumScale/Mgas");v(j,"uR","radMoment"+sf+"*momentumScale/Mgas");v(j,"u","uZ");v(j,"vRadius","uR");v(j,"vHeight","uZ");v(j,"Kinetic","0.5*Mgas*(uR^2+uZ^2)");
   v(j,"Msource",net(i,k,"mass")+(j==0?"+targetMassSource":""));v(j,"Psource",j==0?"targetPowerSource":"0[W]");
   String div=num(2*Math.PI*re[i+1]*dz)+"[m^2]*"+rf(i+1,k)+"velocity-"+num(2*Math.PI*re[i]*dz)+"[m^2]*"+rf(i,k)+"velocity+"+num(az)+"[m^2]*("+zf(i,k+1)+"velocity-"+zf(i,k)+"velocity)";
   v(j,"PeSource",net(i,k,"electron")+"-pe*("+div+")"+(j==0?"+targetElectronSource":""));
   String forward="0",back="0";for(int kk=k+1;kk<nz;kk++)forward+="+"+s(kk*nr+i,"laserTau");for(int kk=0;kk<nz;kk++)back+="+"+s(kk*nr+i,"laserTau");for(int kk=0;kk<k;kk++)back+="+"+s(kk*nr+i,"laserTau");
   String beam="(exp(-2*(rLower/w0)^2)-exp(-2*(rUpper/w0)^2))";
   v(j,"incomingForward","axialIncident*"+beam+"*exp(-("+forward+"))");v(j,"incomingBack","axialIncident*"+beam+"*exp(-("+back+"))");
   for(String n:sp){if(n.equals("N2"))continue;String nn=n.replaceFirst("(_[1-4][pm])$",sf+"$1")+(n.contains("_")?"":sf),rate;
    if(n.matches("Cu_[234]p")){int q=n.charAt(3)-'0';rate="(multNet"+(q-1)+(q<4?"-multNet"+q:"")+")/N_A_const";}
    else{rate=chemistry.get(n).replace("plas.",pl+".");if(n.equals("Cu_1p"))rate="("+rate+")-multNet1/N_A_const";}
    String input=net(i,k,"species"+n);if(j==0&&n.startsWith("Cu")&&!n.matches("Cu_[234]p"))input+="+thermalFlux"+n+"(t/1[s])*1[kg/s]";
    m.component(c(j)).physics(pl).feature(nn).featureInfo("info").set(pl+".R_w"+nn,new String[]{"("+rate+")+("+input+")/(Vp*"+pl+".M_w"+n+")"});
   }
   m.component(c(j)).physics("axialBulk"+sf).feature("ge1").set("name",new String[]{"energyGas"+sf,"logT"+sf,"moment"+sf,"radMoment"+sf});
   m.component(c(j)).physics("axialBulk"+sf).feature("ge1").set("equation",new String[]{"energyGas"+sf+"t-("+net(i,k,"energy")+"+Psource+Plaser-Qrad*Vp)/energyScale","(gasEnergy*energyScale-Vp*(CvVol*Tg+Uchem+Ue)-Kinetic)/energyScale","moment"+sf+"t-("+net(i,k,"momentZ")+(j==0?"+targetMassSource*targetNormalSpeed":"")+")/momentumScale","radMoment"+sf+"t-("+net(i,k,"momentR")+"+pPl*"+num(2*Math.PI*dz*(re[i+1]-re[i]))+"[m^2])/momentumScale"});
   m.component(c(j)).physics("axialBulk"+sf).feature("ge1").set("initialValueU",new String[]{"2.5*pAmb*Vp/energyScale","log(Tamb/1[K])","0","0"});
  }
  for(int k=0;k<nz;k++)for(int i=0;i<=nr;i++){double ar=2*Math.PI*re[i]*(ze[k+1]-ze[k]);if(i==0)wall(rf(i,k),ar,k*nr,true);else face(rf(i,k),ar,k*nr+i-1,i==nr?-1:k*nr+i,true);}
  for(int i=0;i<nr;i++)for(int k=0;k<=nz;k++){double ar=Math.PI*(re[i+1]*re[i+1]-re[i]*re[i]);if(k==0)wall(zf(i,k),ar,i,false);else face(zf(i,k),ar,(k-1)*nr+i,k==nz?-1:k*nr+i,false);}
  String ene="0[J]",cu="0[kg]",rad="0[W]",laser="0[W]",outE="0[W]",outCu="0[kg/s]";
  for(int j=0;j<nr*nz;j++){ene+="+"+s(j,"gasEnergy")+"*energyScale";cu+="+"+s(j,"rhoCopper")+"*"+s(j,"Vp");rad+="+"+s(j,"Qrad")+"*"+s(j,"Vp");laser+="+"+s(j,"Plaser");}
  for(int k=0;k<nz;k++){outE+="+"+rf(nr,k)+"energy";for(String n:sp)if(n.startsWith("Cu"))outCu+="+"+rf(nr,k)+"species"+n;}
  for(int i=0;i<nr;i++){outE+="+"+zf(i,nz)+"energy";for(String n:sp)if(n.startsWith("Cu"))outCu+="+"+zf(i,nz)+"species"+n;}
  g("axialTotalEnergy",ene);g("axialCopper",cu);g("axialRadiation",rad);g("axialLaser",laser);g("axialOutCu",outCu);g("energyF24",outE);
  m.study("std").feature("time").set("tlist","range(0,2[ns],1000[ns])");m.study("std").feature("time").set("rtol",1e-4);m.study("std").createAutoSequences("sol");
  m.sol("sol1").feature("t1").set("maxorder",2);m.sol("sol1").feature("t1").set("maxstepconstraintbdf","const");m.sol("sol1").feature("t1").set("maxstepbdf","1[ns]");m.sol("sol1").feature("t1").set("initialstepbdfactive",true);m.sol("sol1").feature("t1").set("initialstepbdf","0.001[ns]");m.sol("sol1").feature("t1").set("atolglobal","1e-7");
  for(String f:m.sol("sol1").feature("v1").feature().tags()){
   String scale=f.matches(".*_energy[A-Z]$")?"1e7":f.endsWith("_pA")?"1e5":f.contains("ODE")?"1":f.endsWith("_w")?(f.matches(".*_O2[A-Z]_w")?"1":"1e-8"):null;
   if(scale!=null){m.sol("sol1").feature("v1").feature(f).set("scalemethod","manual");m.sol("sol1").feature("v1").feature(f).set("scaleval",scale);}
  }
  m.label("Native Plasma + COARSE 4r x6z axisymmetric Euler FV; H04 prescribed thermal input concentrated within r11um; no moving surface or sheath; no radius fit");m.save(a[1]);System.out.println("AXISYMMETRIC GRID READY 24 native cells");System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
