/* Unit tests for lip_physics: Beer–Lambert column (analytic) and energy closure. */
#include "../lip/lip_physics.h"
#include <math.h>
#include <stdio.h>
int main(void)
{
  int nz = 200, fail = 0;
  double dz[200], a[200], q[200], Is, Ie;
  for (int k = 0; k < nz; k++) { dz[k] = 1e-6 * (1 + 0.01 * k); a[k] = 2000.0 + 10.0 * k; }
  double I0 = 3.3e13, A = 0.2;
  double absd = lip_laser_column(nz, dz, a, I0, A, q, &Is, &Ie);
  double tau = 0; for (int k = 0; k < nz; k++) tau += a[k] * dz[k];
  double Is_exact = I0 * exp(-tau), Ie_exact = Is_exact * (1 - A) * exp(-tau);
  double sumq = 0; for (int k = 0; k < nz; k++) sumq += q[k] * dz[k];
  /* energy closure: incident = absorbed in gas + absorbed by target + escaped */
  double closure = (absd + A * Is + Ie) / I0 - 1;
  printf("I_surf rel err %.2e, I_esc rel err %.2e, sum q dz vs absorbed %.2e, closure %.2e\n",
         Is / Is_exact - 1, Ie / Ie_exact - 1, sumq / absd - 1, closure);
  if (fabs(Is / Is_exact - 1) > 1e-12 || fabs(Ie / Ie_exact - 1) > 1e-12) fail = 1;
  if (fabs(sumq / absd - 1) > 1e-12 || fabs(closure) > 1e-12) fail = 1;
  return fail;
}
