/* init.c -- lipsim v0: ambient air at rest; physics in LipSource (split_source.c). */
#include "lip_glue.h"

static double rho_amb = -1.0;

static double ambient_density(void)
{
  /* rho such that p(rho, T_AMB, Y=0) = P_AMB (bisection in log rho on the table) */
  double lo = -5.0, hi = 3.0;
  lip_init();
  for (int it = 0; it < 200; it++) {
    double mid = 0.5 * (lo + hi);
    double p = lip_eos_p(&lip_eos, pow(10.0, mid), g_inputParam[T_AMB], 0.0);
    if (p > g_inputParam[P_AMB]) hi = mid; else lo = mid;
  }
  return pow(10.0, 0.5 * (lo + hi));
}

void Init(double *v, double x1, double x2, double x3)
{
  (void)x1; (void)x2; (void)x3;
  if (rho_amb < 0.0) rho_amb = ambient_density();
  v[RHO] = rho_amb / LIP_RHO_SI;
  v[VX1] = 0.0;
  v[VX2] = 0.0;
  v[PRS] = g_inputParam[P_AMB] / LIP_PRS_SI;
  v[TRC] = 0.0;
#if NTRACER > 1
  v[TRC + 1] = 0.0;
#endif
}

void InitDomain(Data *d, Grid *grid) { (void)d; (void)grid; }

void Analysis(const Data *d, Grid *grid) { lip_analysis(d, grid); }

void UserDefBoundary(const Data *d, RBox *box, int side, Grid *grid)
{ (void)d; (void)box; (void)side; (void)grid; }
