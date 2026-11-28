import com.comsol.model.*;
import com.comsol.model.util.*;
import java.io.*;
public class AuditBounds {
  public static void main(String[] a) {
    try {
      ModelUtil.initStandalone(false); Model m=ModelUtil.load("M",a[0]);
      String[] ex={"t","-Tpl","-rhoP","-ethRaw/rhoSafe*rho0","Yraw","-Yraw","leArg","-leArg","lrArg","-lrArg","xionCu","-xionCu","-opt","opt-Ipulse","if(Yraw>1.0001||Yraw< -0.0001,1,0)","if(ethRaw<0,1,0)","if(r>0.8*Rdom||z>0.8*Zdom,abs(pPl-pAmb)/pAmb,0)","if(r>0.8*Rdom||z>0.8*Zdom,Yraw,0)"};
      m.result().numerical().create("auditBound","MaxSurface");
      m.result().numerical("auditBound").selection().set(new int[]{2});
      m.result().numerical("auditBound").set("data","dset1");
      m.result().numerical("auditBound").set("expr",ex);
      double[][] v=m.result().numerical("auditBound").getReal();
      {PrintWriter w=new PrintWriter(a[1]);
        w.println(String.join(",",ex));
        for(int j=0;j<v[0].length;j++) {for(int i=0;i<v.length;i++){if(i>0)w.print(",");w.print(v[i][j]);} w.println();}w.close();
      }
      System.out.println("AUDITED "+v[0].length+" times");
      ModelUtil.clear();System.exit(0);
    } catch(Exception e) {e.printStackTrace();System.exit(1);}
  }
}
