/* pvte_law.c -- user file of the PVTE_LAW EOS (official extension point): lipsim LTE tables. */
#include "lip_glue.h"

double InternalEnergyFunc(double *v, double T) { return InternalEnergy(v, T); }

/* mean molecular weight consistent with p = rho k T / (mu m_u), for the Cu-free case (used only by
 * upstream code paths that lack the tracer; lipsim code always uses Pressure()) */
void GetMu(double T, double rho, double *mu)
{
  double v[NVAR];
  v[RHO] = rho; v[TRC] = 0.0;
  *mu = rho * T / (KELVIN * Pressure(v, T));
}

/* Gamma1 = rho c^2 / p with the equilibrium sound speed of the table */
double Gamma1(double *v)
{
  double T = lip_T_from_prim(v), rho = v[RHO] * LIP_RHO_SI;
  double c2 = lip_eos_cs2(&lip_eos, rho, T, lip_Y(v));
  return rho * c2 / (v[PRS] * LIP_PRS_SI);
}
