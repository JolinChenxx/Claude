/* Unit test: C table interpolation / EOS inversion vs values printed by the Python reference.
 * usage: test_lip_tables <eos.bin> <cases.txt>   (cases: lrho lT Y e_ref p_ref) */
#include "../lip/lip_tables.h"
#include <math.h>
#include <stdio.h>
int main(int argc, char **argv)
{
  LipEOS eos;
  if (argc < 3 || lip_eos_init(&eos, argv[1])) { printf("init failed\n"); return 2; }
  FILE *fp = fopen(argv[2], "r");
  double lr, lT, Y, eref, pref, worst_e = 0, worst_p = 0, worst_T = 0, worst_c = 0;
  int n = 0;
  while (fscanf(fp, "%lf %lf %lf %lf %lf", &lr, &lT, &Y, &eref, &pref) == 5) {
    double rho = pow(10, lr), T = pow(10, lT), Tinv, Tinvp;
    double e = lip_eos_e(&eos, rho, T, Y), p = lip_eos_p(&eos, rho, T, Y);
    lip_eos_T_from_e(&eos, rho, e, Y, &Tinv);
    lip_eos_T_from_p(&eos, rho, p, Y, &Tinvp);
    double c2 = lip_eos_cs2(&eos, rho, T, Y);
    worst_e = fmax(worst_e, fabs(e / eref - 1));
    worst_p = fmax(worst_p, fabs(p / pref - 1));
    worst_T = fmax(worst_T, fmax(fabs(Tinv / T - 1), fabs(Tinvp / T - 1)));
    if (!(c2 > 0)) worst_c = 1;
    n++;
  }
  printf("cases=%d max_rel_err e=%.2e p=%.2e T_roundtrip=%.2e cs2_bad=%g\n", n, worst_e, worst_p, worst_T, worst_c);
  return (worst_e < 1e-9 && worst_p < 1e-9 && worst_T < 1e-9 && worst_c == 0) ? 0 : 1;
}
