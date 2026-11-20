/*
 * probe_native.java
 */

import com.comsol.model.*;
import com.comsol.model.util.*;

/** Model exported on Nov 7 2026, 17:49 by COMSOL 6.4.0.429. */
public class probe_native {

  public static Model run() {
    Model model = ModelUtil.create("Model");

    model.modelPath("D:\\junlin\\PlasmaPlume\\route1\\src");

    model.component().create("c", true);

    model.component("c").geom().create("g", 2);
    model.component("c").geom("g").axisymmetric(true);
    model.component("c").geom("g").create("rect", "Rectangle");
    model.component("c").geom("g").run();

    model.component("c").physics().create("plas", "Plasma", "g");

    return model;
  }

  public static void main(String[] args) {
    run();
  }

}
