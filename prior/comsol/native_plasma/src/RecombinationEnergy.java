import com.comsol.model.*;
public class RecombinationEnergy { public static void apply(Model m){
if(java.util.Arrays.asList(m.func().tags()).contains("rrEnergy"))m.func().remove("rrEnergy");
m.func().create("rrEnergy","Interpolation");m.func("rrEnergy").set("source","file");m.func("rrEnergy").set("filename","D:/junlin/PlasmaPlume/route1/data/rr_mean_energy.txt");m.func("rrEnergy").set("nargs",1);
m.func("rrEnergy").set("funcs",new String[][]{{"rrMean_Curr","1"},{"rrMean_Nrr","2"},{"rrMean_Orr","3"},{"rrMean_CuD5rr","4"},{"rrMean_CuD3rr","5"},{"rrMean_CuP1rr","6"},{"rrMean_CuP3rr","7"},{"rrMean_CuH3rr","8"},{"rrMean_CuH5rr","9"}});
m.func("rrEnergy").set("interp","piecewisecubic");m.func("rrEnergy").set("extrap","const");m.func("rrEnergy").importData();
}}