#include "pluto.h"
#include "cuair.h"
void TC_kappa(double *v,double x1,double x2,double x3,double *kp,double *kn,double *phi){
  double T, y=fmin(1.,fmax(0.,v[CU_Y])),q=fmin(y,fmax(0.,v[CU_ION_Y]));
  GetPV_Temperature(v,&T);T=fmax(T,50.);
  /* Reduced neutral conductivity plus electron Spitzer scaling (ln Lambda=5).
     Electron term weighted by ionized Cu mass fraction; flux limiter below. */
  double k=(1.-y)*.026*pow(T/300.,.7)+y*.02*sqrt(T/300.)
           +q*1.84e-10*pow(T,2.5)/5.;
  /* Local parabolic_update override supplies temperature in kelvin. */
  *kp=*kn=k/1.e3; /* k/(rho_unit*v_unit^3*L_unit) */
  *phi=.3;
}

