import com.sun.tools.attach.VirtualMachine;import javax.management.*;import javax.management.remote.*;import java.lang.management.*;
/** Read-only thread stacks of this task's Java builder, to locate long API work. */
public class JavaThreadSnapshot {public static void main(String[] a)throws Exception{
 VirtualMachine vm=VirtualMachine.attach(a[0]);String addr=vm.startLocalManagementAgent();vm.detach();
 try(JMXConnector c=JMXConnectorFactory.connect(new JMXServiceURL(addr))){
 MemoryMXBean mem=ManagementFactory.newPlatformMXBeanProxy(c.getMBeanServerConnection(),ManagementFactory.MEMORY_MXBEAN_NAME,MemoryMXBean.class);
 System.out.println("HEAP "+mem.getHeapMemoryUsage());
 for(ObjectName gc:c.getMBeanServerConnection().queryNames(new ObjectName("java.lang:type=GarbageCollector,*"),null))
  System.out.println("GC "+gc+" time_ms="+c.getMBeanServerConnection().getAttribute(gc,"CollectionTime")+" count="+c.getMBeanServerConnection().getAttribute(gc,"CollectionCount"));
 ThreadMXBean t=ManagementFactory.newPlatformMXBeanProxy(c.getMBeanServerConnection(),ManagementFactory.THREAD_MXBEAN_NAME,ThreadMXBean.class);
 for(ThreadInfo i:t.dumpAllThreads(false,false))if(i.getThreadState()==Thread.State.RUNNABLE){System.out.println(i.getThreadName());for(StackTraceElement e:i.getStackTrace())System.out.println("  "+e);}
 }
}}
