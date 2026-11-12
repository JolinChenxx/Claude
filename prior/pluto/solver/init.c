#include "pluto.h"
#include "cuair.h"
void Init(double *v,double r,double z,double phi){
  v[RHO]=1.176624;v[VX1]=v[VX2]=v[VX3]=0.;
  v[CU_Y]=v[CU_ION_Y]=0.;v[PRS]=Pressure(v,300.);
  g_smallDensity=1.e-12;g_smallPressure=1.e-12;
}
void InitDomain(Data *d,Grid *grid){}
void UserDefBoundary(const Data *d,RBox *box,int side,Grid *grid){}
extern double cu_radiated_local;
void Analysis(const Data *d,Grid *grid){
  int i,j,k,n;double v[NVAR],sum[5]={0},tot[5]={0};
  DOM_LOOP(k,j,i){
    for(n=0;n<NVAR;n++)v[n]=d->Vc[n][k][j][i];
    double T,vol=2.*CONST_PI*grid->dV[k][j][i];
    GetPV_Temperature(v,&T);
    double kin=.5*v[RHO]*(v[VX1]*v[VX1]+v[VX2]*v[VX2]+v[VX3]*v[VX3]);
    sum[0]+=v[RHO]*v[CU_Y]*vol*1.e-6; /* ng */
    sum[1]+=(InternalEnergy(v,T)+kin-1.176624*717.625*300./1.e6)*vol*1.e-6;
    sum[2]+=kin*vol*1.e-6;
    sum[3]+=v[RHO]*v[CU_ION_Y]*CU_CHI_JKG/1.e6*vol*1.e-6;
  }
  sum[4]=cu_radiated_local*1.e-6;
#ifdef PARALLEL
  MPI_Reduce(sum,tot,5,MPI_DOUBLE,MPI_SUM,0,MPI_COMM_WORLD);
#else
  for(n=0;n<5;n++)tot[n]=sum[n];
#endif
  if(prank==0){
    FILE *fp=fopen("budget.csv",g_stepNumber==0?"w":"a");
    if(g_stepNumber==0)fprintf(fp,"time_ns,cu_mass_ng,excess_energy_uj,kinetic_uj,ionization_uj,radiated_segment_uj,mass_in_ng,energy_in_uj\n");
    double frac=cu_temporal_cdf(g_time);
    fprintf(fp,"%.9g,%.12g,%.12g,%.12g,%.12g,%.12g,%.12g,%.12g\n",g_time,
      tot[0],tot[1],tot[2],tot[3],tot[4],frac*g_inputParam[MASS_NG],frac*g_inputParam[ENERGY_UJ]);
    fclose(fp);
  }
}

