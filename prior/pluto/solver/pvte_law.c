#include "pluto.h"
#include "cuair.h"
double InternalEnergyFunc(double *v,double T){return InternalEnergy(v,T);}
double Gamma1(double *v){return 1.+cu_rgas(v)/cu_cv(v);}
void GetMu(double T,double rho,double *mu){*mu=28.965;}

