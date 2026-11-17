/* internal_energy.c -- REPLACES PLUTO Src/EOS/PVTE/internal_energy.c (listed with diff in build_provenance).
 * Reason: as thermal_eos.c -- rhoe(rho,T,Y) and T(rho,rhoe,Y) from the lipsim LTE table with Y = v[TRC];
 * the sound speed uses Gamma1() from pvte_law.c (equilibrium c^2 from table derivatives). */
#include "lip_glue.h"

void MakeInternalEnergyTable(void) { lip_init(); }
void MakeEV_TemperatureTable(void) { lip_init(); }

double InternalEnergy(double *v, double T)
{
  lip_init();
  double rho = v[RHO] * LIP_RHO_SI;
  return rho * lip_eos_e(&lip_eos, rho, T, lip_Y(v)) / LIP_PRS_SI;
}

int GetEV_Temperature(double rhoe, double *v, double *T)
{
  if (rhoe < 0.0) return 1;
  lip_init();
  double rho = v[RHO] * LIP_RHO_SI;
  int s = lip_eos_T_from_e(&lip_eos, rho, rhoe * LIP_PRS_SI / rho, lip_Y(v), T);
  return (s == 1 || s == 3 || *T < T_CUT_RHOE) ? 1 : 0;
}

void SoundSpeed2(const State *p, int beg, int end, int pos, Grid *grid)
{
  int i;
  (void)pos; (void)grid;
  for (i = beg; i <= end; i++) p->a2[i] = Gamma1(p->v[i]) * p->v[i][PRS] / p->v[i][RHO];
}
