import com.comsol.model.*;
public class MeshGraph{
 public static void velocity(Model m){
  m.component("c").common().remove("meshSurf");
  m.component("c").common().create("meshSurf","PrescribedMeshVelocity");m.component("c").common("meshSurf").selection().named("surface");
  m.component("c").common("meshSurf").set("prescribedMeshVelocity",new String[]{"0","0","meltOn*(uzCu+urCu*ncr/ncz-mdot/(rhoCu*ncz))"});
  m.component("c").common("meshSurf").set("constraintType","Nitsche");
  m.component("c").common("deform").set("smoothingType","laplace");
 }
 public static void apply(Model m){
  m.component("c").common().remove("meshSurf");
  if(!java.util.Arrays.asList(m.component("c").physics().tags()).contains("surfaceODE"))m.component("c").physics().create("surfaceODE","BoundaryODE","g",new String[]{"etaSurface"});
  m.component("c").physics("surfaceODE").selection().named("surface");
  m.component("c").physics("surfaceODE").prop("ShapeProperty").set("frame","material");
  m.component("c").physics("surfaceODE").prop("ShapeProperty").set("shapeFunctionType","shlag");
  m.component("c").physics("surfaceODE").prop("ShapeProperty").set("valueType","real");
  m.component("c").physics("surfaceODE").prop("ShapeProperty").set("order",1);
  m.component("c").physics("surfaceODE").feature("dode1").set("da","r/Rdom");
  m.component("c").physics("surfaceODE").feature("dode1").set("f","r/Rdom*meltOn*(uzCu+urCu*ncr/ncz-mdot/(rhoCu*ncz))/1[um]");
  m.component("c").common().create("meshSurf","PrescribedMeshDisplacement");m.component("c").common("meshSurf").selection().named("surface");
  // etaSurface is the absolute height; displacement is relative to the
  // material coordinate Z, which COMSOL maps across remeshing (not Zm).
  m.component("c").common("meshSurf").set("prescribedMeshDisplacement",new String[]{"0","0","etaSurface*1[um]-Z"});
  m.component("c").common("meshSurf").set("constraintType","pointwise");
  m.component("c").common("deform").set("smoothingType","laplace");
 }
}
