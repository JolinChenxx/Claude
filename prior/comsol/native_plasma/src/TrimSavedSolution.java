import com.comsol.model.*;import com.comsol.model.util.*;import java.util.*;
/** Save actual solver vectors only through the requested valid time. No extrapolation. */
public class TrimSavedSolution {public static void main(String[] a){try{ModelUtil.initStandalone(false);Model m=ModelUtil.load("m",a[0]);String s=ExportNative.savedSolution(m);double cutoff=Double.parseDouble(a[2]);double[] t=m.sol(s).getPVals();int n=0;while(n<t.length&&t[n]<=cutoff+1e-16)n++;if(n<2)throw new RuntimeException("Need two saved times");
String out=s;double[][] vectors=new double[n][];for(int i=0;i<n;i++)vectors[i]=m.sol(s).getU(i+1);m.sol(s).clearSolutionData();m.sol(s).setPNames(new String[]{"t"});m.sol(s).setPVals(Arrays.copyOf(t,n));for(int i=0;i<n;i++)m.sol(s).setU(i+1,vectors[i]);m.sol(s).createSolution();
for(String sol:m.sol().tags())if(!sol.equals(out))m.sol().remove(sol);
ExportNative.dataset(m,out);m.label("Route 1 actual saved solution, valid-time cutoff "+cutoff+" s");m.save(a[1]);System.out.println("TRIMMED REAL SOLUTION "+n+" times through "+t[n-1]);System.exit(0);}catch(Throwable e){e.printStackTrace();System.exit(1);}}}
