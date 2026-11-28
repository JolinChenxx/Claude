/* test_lip_breakdown.c -- print v1m kernels: "Te k_ion saha alpha(ne,Te,n0) E1(x=Te/1e4) ne_eq(after long step)" */
#include "../lip/lip_physics.h"
#include <stdio.h>

int main(void)
{
  const double a = 4.5e-18, sig = 1.0e-19, nCu = 6.4e26;
  for (double Te = 3000.0; Te <= 60000.0; Te *= 1.25) {
    double ne = lip_ne_step(1e20, nCu, Te, 1.0, a);   /* 1 s step: backward Euler -> steady state */
    printf("%.17g %.17g %.17g %.17g %.17g %.17g\n", Te, lip_k_ion_cu(Te, a), lip_saha_cu(Te),
           lip_alpha_2t(1e25, Te, 5e26, sig), lip_e1(Te / 1e4), ne);
  }
  printf("TE %.17g\n", lip_te_qs(1.7e13, 5.0e22, 6.4e26, 4696.0, sig, a));
  return 0;
}
