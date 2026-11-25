/* test_lip_droplet.c -- v1r parcel evaporation in a uniform gas: integrate lip_drop_dm with step dt until the parcel
 * is gone and print the evaporation time. argv: a0 rho_l qfm K Lv dt. Also prints q(a0) and q_FM(rho, T, mbar, deps)
 * for argv 7..10 when given. */
#include "../lip/lip_physics.h"
#include <stdio.h>
#include <stdlib.h>

int main(int argc, char **argv)
{
  if (argc < 7) return 2;
  double a0 = atof(argv[1]), rl = atof(argv[2]), qfm = atof(argv[3]), K = atof(argv[4]), Lv = atof(argv[5]), dt = atof(argv[6]);
  double m0 = 1e-15, m = m0, t = 0.0;
  long n = 0;
  while (m > 1e-9 * m0 && n < 100000000L) { m -= lip_drop_dm(m, m0, a0, rl, qfm, K, Lv, dt); t += dt; n++; }
  printf("%.17g %.17g\n", t, lip_drop_q(qfm, K, a0));
  if (argc >= 11) printf("%.17g\n", lip_drop_qfm(atof(argv[7]), atof(argv[8]), atof(argv[9]), atof(argv[10])));
  return 0;
}
