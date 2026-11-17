/* tc_kappa.c -- user function of the PLUTO thermal-conduction module (official extension point):
 * isotropic conductivity from the lipsim table (opt.bin field "kappa", W/m/K), converted to code units for
 * a temperature gradient in K per code length. No saturation (TC_SATURATED_FLUX NO in definitions.h). */
#include "lip_glue.h"

void TC_kappa(double *v, double x1, double x2, double x3, double *kpar, double *knor, double *phi)
{
  (void)x1; (void)x2; (void)x3;
  double k_SI = lip_kappa_SI(v);
  /* code flux = rho_u v_u^3 ; flux_SI = k_SI * dT/dx_code / L_u */
  *kpar = k_SI / (LIP_LEN_SI * LIP_PRS_SI * LIP_VEL_SI);
  *knor = *kpar;
  *phi = 1.0e30;
}
