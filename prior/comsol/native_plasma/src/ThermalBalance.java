import com.comsol.model.*;
public class ThermalBalance { public static void apply(Model m){
m.component("c").physics("plas").feature("N2_1").set("de","0.02*(1-exp(0.02[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.02[V]*e_const/(k_B_const*Tgas)))");
m.component("c").physics("plas").feature("N2_2").set("de","0.29*(1-exp(0.29[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.29[V]*e_const/(k_B_const*Tgas)))");
m.component("c").physics("plas").feature("N2_3").set("de","0.29*(1-exp(0.29[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.29[V]*e_const/(k_B_const*Tgas)))");
m.component("c").physics("plas").feature("N2_4").set("de","0.59*(1-exp(0.59[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.59[V]*e_const/(k_B_const*Tgas)))");
m.component("c").physics("plas").feature("N2_5").set("de","0.88*(1-exp(0.88[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.88[V]*e_const/(k_B_const*Tgas)))");
m.component("c").physics("plas").feature("N2_6").set("de","1.17*(1-exp(1.17[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.17[V]*e_const/(k_B_const*Tgas)))");
m.component("c").physics("plas").feature("N2_7").set("de","1.47*(1-exp(1.47[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.47[V]*e_const/(k_B_const*Tgas)))");
m.component("c").physics("plas").feature("N2_8").set("de","1.76*(1-exp(1.76[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.76[V]*e_const/(k_B_const*Tgas)))");
m.component("c").physics("plas").feature("O2_3").set("de","0.02*(1-exp(0.02[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.02[V]*e_const/(k_B_const*Tgas)))");
m.component("c").physics("plas").feature("O2_4").set("de","0.19*(1-exp(0.19[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.19[V]*e_const/(k_B_const*Tgas)))");
m.component("c").physics("plas").feature("O2_5").set("de","0.19*(1-exp(0.19[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.19[V]*e_const/(k_B_const*Tgas)))");
m.component("c").physics("plas").feature("O2_6").set("de","0.38*(1-exp(0.38[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.38[V]*e_const/(k_B_const*Tgas)))");
m.component("c").physics("plas").feature("O2_7").set("de","0.38*(1-exp(0.38[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.38[V]*e_const/(k_B_const*Tgas)))");
m.component("c").physics("plas").feature("O2_8").set("de","0.57*(1-exp(0.57[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.57[V]*e_const/(k_B_const*Tgas)))");
m.component("c").physics("plas").feature("O2_9").set("de","0.75*(1-exp(0.75[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.75[V]*e_const/(k_B_const*Tgas)))");
m.component("c").physics("plas").feature("O2_10").set("de","0.977*(1-exp(0.977[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-0.977[V]*e_const/(k_B_const*Tgas)))");
m.component("c").physics("plas").feature("O2_11").set("de","1.627*(1-exp(1.627[V]*(1/TeV-e_const/(k_B_const*Tgas))))/(1+exp(-1.627[V]*e_const/(k_B_const*Tgas)))");
}}