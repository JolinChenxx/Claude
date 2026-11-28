/* test_lip_knight.c -- print lip_knight_bp(pr) for pr read from argv file: "pr flux M tr rr" per line. */
#include "../lip/lip_physics.h"
#include <stdio.h>

int main(int argc, char **argv)
{
  if (argc < 2) return 2;
  FILE *fp = fopen(argv[1], "r");
  if (!fp) return 3;
  double pr;
  while (fscanf(fp, "%lf", &pr) == 1) {
    double M, tr, rr, f = lip_knight_bp(pr, &M, &tr, &rr);
    printf("%.17g %.17g %.17g %.17g %.17g\n", pr, f, M, tr, rr);
  }
  fclose(fp);
  return 0;
}
