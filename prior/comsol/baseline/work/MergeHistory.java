import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
public class MergeHistory {public static void main(String[] a){try{
 ModelUtil.initStandalone(false);Model m=ModelUtil.load("M",a[0]);if(!Arrays.asList(m.sol().tags()).contains("history")){System.out.println("NO_HISTORY");ModelUtil.clear();System.exit(0);}
 double[] ht=m.sol("history").getPVals(),nt=m.sol("sol1").getPVals();
 if(ht.length==0||nt.length==0||Math.abs(ht[ht.length-1]-nt[0])>1e-17)throw new IllegalStateException("History join times differ");
 double[] hu=m.sol("history").getU(ht.length),nu=m.sol("sol1").getU(1);
 if(hu.length!=nu.length)throw new IllegalStateException("History join DOF counts differ");
 double norm=0,diff=0,maxDiff=0,maxValue=0;
 for(int i=0;i<hu.length;i++){if(!Double.isFinite(hu[i])||!Double.isFinite(nu[i]))throw new IllegalStateException("Nonfinite join state");double d=nu[i]-hu[i];norm+=hu[i]*hu[i];diff+=d*d;maxDiff=Math.max(maxDiff,Math.abs(d));maxValue=Math.max(maxValue,Math.abs(hu[i]));}
 double relative=Math.sqrt(diff/Math.max(norm,1e-100));
 java.io.PrintWriter audit=new java.io.PrintWriter(a[0]+"_join64.json");audit.println("{\"join_time_s\":"+nt[0]+",\"dof_count\":"+hu.length+",\"relative_state_L2\":"+relative+",\"max_absolute_difference\":"+maxDiff+",\"max_source_absolute_value\":"+maxValue+",\"same_join_time\":true}");audit.close();
 System.out.println("JOIN_STATE_RELATIVE_L2 "+relative);
 if(relative>1e-5)throw new IllegalStateException("Restart state discontinuity requires review: "+relative);
 if(Arrays.asList(m.sol().tags()).contains("merged"))m.sol().remove("merged");
 m.sol().create("merged");m.sol("merged").study("std");m.sol("merged").create("cs","CombineSolution");
 m.sol("merged").feature("cs").set("soloper","concatenation");m.sol("merged").feature("cs").set("cssol1","history");m.sol("merged").feature("cs").set("cssol2","sol1");m.sol("merged").runAll();
 double[] tt=m.sol("merged").getPVals();System.out.println("MERGED "+tt.length+" "+tt[0]+" "+tt[tt.length-1]);
 if(!Arrays.asList(m.result().dataset().tags()).contains("dset1"))m.result().dataset().create("dset1","Solution");
 m.result().dataset("dset1").set("solution","merged");m.save(a[0]);ModelUtil.clear();System.exit(0);
 }catch(Exception e){e.printStackTrace();System.exit(1);}}}
