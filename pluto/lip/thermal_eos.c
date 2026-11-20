/* thermal_eos.c -- REPLACES PLUTO Src/EOS/PVTE/thermal_eos.c (listed in build_provenance.json with diff).
 * Reason: the upstream LTE branch builds T(p/rho, rho) tables from GetMu(T, rho), which cannot depend on
 * the Cu tracer. Here p(rho,T,Y) and its inverse come from the lipsim LTE table (lip_tables.c), Y = v[TRC].
 * Exported functions keep the upstream names and signatures. */
#include "lip_glue.h"

void MakePV_TemperatureTable(void) { lip_init(); }

double Pressure(double *v, double T)
{
  lip_init();
  return lip_eos_p(&lip_eos, v[RHO] * LIP_RHO_SI, T, lip_Y(v)) / LIP_PRS_SI;
}

int GetPV_Temperature(double *v, double *T)
{
  if (v[PRS] < 0.0) return 1;
  lip_init();
  int s = lip_eos_T_from_p(&lip_eos, v[RHO] * LIP_RHO_SI, v[PRS] * LIP_PRS_SI, lip_Y(v), T);
  return (s == 1 || s == 3 || *T < T_CUT_RHOE) ? 1 : 0;
}
