import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Physics tag and identifier are distinct API concepts; set both explicitly. */
public class FixAxialNamespaces {
 public static void main(String[] a){try{
  ModelUtil.initStandalone(false);Model m=ModelUtil.load("fixAxialNamespaces",a[0]);
  ArrayList<String> species=new ArrayList<>(Arrays.asList(Chemistry.NAMES));species.add("CuT5");species.add("Cu_2p");species.add("Cu_3p");species.add("Cu_4p");
  Map<String,String> chemistry=AxialChemicalRates.rates();int count=Integer.parseInt(m.variable("axialFlux").get("axialCellCount"));
  for(int j=0;j<count;j++){
   String suffix=""+(char)('A'+j),cell="cell"+suffix,p="plasma"+suffix;
   System.out.println("IDENTIFIER "+cell+" "+m.component(cell).physics(p).identifier()+" -> "+p);
   m.component(cell).physics(p).identifier(p);
   m.component(cell).physics(p).feature("pes1").featureInfo("info").set(p+".Vol_gm",new String[]{"Vp"});
   m.component(cell).physics(p).feature("pes1").featureInfo("info").set(p+".mfinGm",new String[]{"Msource"});
   m.component(cell).physics(p).feature("pes1").featureInfo("info").set(p+".Nn0",new String[]{"pAmb/(k_B_const*Tamb)"});
   for(String n:species){if(n.equals("N2"))continue;
    String nn=n.replaceFirst("(_[1-4][pm])$",suffix+"$1")+(n.contains("_")?"":suffix),rate;
    if(n.matches("Cu_[234]p")){int q=n.charAt(3)-'0';rate="(multNet"+(q-1)+(q<4?"-multNet"+q:"")+")/N_A_const";}
    else{rate=chemistry.get(n).replace("plas.",p+".");if(n.equals("Cu_1p"))rate="("+rate+")-multNet1/N_A_const";}
    String input="speciesF"+j+"_"+n+"-speciesF"+(j+1)+"_"+n;
    if(j==0&&n.startsWith("Cu")&&!n.matches("Cu_[234]p"))input+="+thermalFlux"+n+"(t/1[s])*1[kg/s]";
    m.component(cell).physics(p).feature(nn).featureInfo("info").set(p+".R_w"+nn,new String[]{"("+rate+")+("+input+")/(Vp*"+p+".M_w"+n+")"});
   }
   for(String f:new String[]{"O2"+suffix,"Cu"+suffix}){
    boolean found=false;
    for(String[] row:m.component(cell).physics(p).feature(f).featureInfo("info").getInfoTable("Expression"))if(row.length>2&&row[1].equals(p+".R_w"+f)){System.out.println("SOURCE AUDIT "+cell+" "+Arrays.toString(row));found=row[2].contains("speciesF"+j+"_");}
    if(!found)throw new RuntimeException("Native source override absent "+cell+" "+f);
   }
  }
  for(String n:m.variable("axialFlux").varnames()){String s=m.variable("axialFlux").get(n);if(s.contains("0[m/s]^2"))m.variable("axialFlux").set(n,s.replace("0[m/s]^2","(0[m/s])^2"));}
  for(String s:m.sol().tags())m.sol(s).clearSolutionData();m.save(a[1]);System.out.println("AXIAL NAMESPACES AND SOURCES VERIFIED "+count);System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.exit(1);}}
}
