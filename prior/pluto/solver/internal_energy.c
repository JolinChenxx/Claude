#include "pluto.h"
#include "cuair.h"
double InternalEnergy(double *v,double T){
  double q=fmin(fmax(v[CU_Y],0.),fmax(v[CU_ION_Y],0.));
  return v[RHO]*(cu_cv(v)*T+q*CU_CHI_JKG)/1.e6;
}
int GetEV_Temperature(double rhoe,double *v,double *T){
  double q=fmin(fmax(v[CU_Y],0.),fmax(v[CU_ION_Y],0.));
  *T=(rhoe*1.e6/fmax(v[RHO],1.e-30)-q*CU_CHI_JKG)/cu_cv(v);
  return !isfinite(*T)||*T<T_CUT_RHOE;
}
void SoundSpeed2(const State *p,int beg,int end,int pos,Grid *grid){
  int i;for(i=beg;i<=end;i++)p->a2[i]=Gamma1(p->v[i])*p->v[i][PRS]/p->v[i][RHO];
}
void MakeInternalEnergyTable(void){}
void MakeEV_TemperatureTable(void){}

