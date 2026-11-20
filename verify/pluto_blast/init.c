/* T009 task-specified ideal-gas hemisphere. Stock PLUTO user hooks only. */
#include "pluto.h"
#define RHO_AMB 1.2041
#define P_AMB 0.101325

void Init(double *v, double x1, double x2, double x3)
{
  g_gamma = 1.4;
  v[RHO] = RHO_AMB;
  v[PRS] = P_AMB;
  v[VX1] = v[VX2] = v[VX3] = 0.0;
}

void InitDomain(Data *d, Grid *grid)
{
  int i,j,k;
  double local_vol=0.0, global_vol, dp, r, z;
  const double r0 = g_inputParam[R0_UM];
  DOM_LOOP(k,j,i) {
    r=grid->x[IDIR][i]; z=grid->x[JDIR][j];
    if (r*r+z*z < r0*r0) local_vol += 2.0*CONST_PI*grid->dV[k][j][i];
  }
  global_vol=local_vol;
#ifdef PARALLEL
  MPI_Allreduce(&local_vol,&global_vol,1,MPI_DOUBLE,MPI_SUM,MPI_COMM_WORLD);
#endif
  if (global_vol <= 0.0) { print("Empty deposition volume\n"); QUIT_PLUTO(1); }
  dp = (g_gamma-1.0)*g_inputParam[E_UJ]*1.e6/global_vol;
  DOM_LOOP(k,j,i) {
    r=grid->x[IDIR][i]; z=grid->x[JDIR][j];
    if (r*r+z*z < r0*r0) d->Vc[PRS][k][j][i] = P_AMB+dp;
  }
  if (prank == 0) {
    FILE *f=fopen("initialization.csv","w");
    if (!f) { QUIT_PLUTO(1); }
    fprintf(f,"E_nominal_uJ,R0_um,discrete_volume_um3,delta_p_MPa\n%.17g,%.17g,%.17g,%.17g\n",
            g_inputParam[E_UJ],r0,global_vol,dp);
    fclose(f);
  }
}

void Analysis(const Data *d, Grid *grid)
{
  int i,j,k;
  double local[5]={0.0,0.0,0.0,0.0,0.0}, total[5];
  double vol,rho,p,u,v,w,energy;
  DOM_LOOP(k,j,i) {
    vol=2.0*CONST_PI*grid->dV[k][j][i];
    rho=d->Vc[RHO][k][j][i]; p=d->Vc[PRS][k][j][i];
    u=d->Vc[VX1][k][j][i]; v=d->Vc[VX2][k][j][i]; w=d->Vc[VX3][k][j][i];
    energy=p/(g_gamma-1.0)+0.5*rho*(u*u+v*v+w*w);
    local[0] += energy*vol;
    local[1] += (energy-P_AMB/(g_gamma-1.0))*vol;
    local[2] += rho*vol;
    local[3] += vol;
    if (grid->x[IDIR][i] > g_domEnd[IDIR]-grid->dx[IDIR][i] ||
        grid->x[JDIR][j] > g_domEnd[JDIR]-grid->dx[JDIR][j]) {
      if (p>local[4]) local[4]=p;
    }
  }
  for(i=0;i<5;i++) total[i]=local[i];
#ifdef PARALLEL
  MPI_Allreduce(local,total,4,MPI_DOUBLE,MPI_SUM,MPI_COMM_WORLD);
  MPI_Allreduce(local+4,total+4,1,MPI_DOUBLE,MPI_MAX,MPI_COMM_WORLD);
#endif
  if (prank == 0) {
    FILE *f=fopen("budget.csv",g_stepNumber==0 ? "w":"a");
    if (!f) { QUIT_PLUTO(1); }
    if (g_stepNumber==0) fprintf(f,"t_ns,step,total_uJ,excess_uJ,mass_code,volume_um3,outer_max_p_MPa\n");
    fprintf(f,"%.17g,%ld,%.17g,%.17g,%.17g,%.17g,%.17g\n",
      g_time,(long)g_stepNumber,total[0]/1.e6,total[1]/1.e6,total[2],total[3],total[4]);
    fclose(f);
  }
}

void UserDefBoundary(const Data *d, RBox *box, int side, Grid *grid) {}
