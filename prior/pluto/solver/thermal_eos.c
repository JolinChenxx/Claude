/* Frozen-species ideal-mixture pressure. All custom thermodynamics are SI;
   PLUTO conservative units have pressure/energy density scale 1e6 Pa. */
#include "pluto.h"
#include "cuair.h"
double cu_rgas(const double *v){
  double y=fmin(1.,fmax(0.,v[CU_Y])), q=fmin(y,fmax(0.,v[CU_ION_Y]));
  return (1.-y)*287.05 + (y+q)*KB_SI/CU_MASS;
}
double cu_cv(const double *v){
  double y=fmin(1.,fmax(0.,v[CU_Y])), q=fmin(y,fmax(0.,v[CU_ION_Y]));
  return (1.-y)*717.625 + 1.5*(y+q)*KB_SI/CU_MASS;
}
double Pressure(double *v,double T){return v[RHO]*cu_rgas(v)*T/1.e6;}
int GetPV_Temperature(double *v,double *T){
  *T=v[PRS]*1.e6/(fmax(v[RHO],1.e-30)*cu_rgas(v));
  return !isfinite(*T)||*T<T_CUT_RHOE;
}
void MakePV_TemperatureTable(void){}

