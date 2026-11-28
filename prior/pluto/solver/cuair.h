#ifndef CUAIR_H
#define CUAIR_H
#define CU_MASS 1.055206e-25
#define KB_SI 1.380649e-23
#define EV_SI 1.602176634e-19
#define CU_CHI_JKG (7.72638*EV_SI/CU_MASS)
double cu_rgas(const double *v);
double cu_cv(const double *v);
double cu_line_loss(double T, double rho, double y, double ion, double width_nm, double length_m);
double cu_ff_loss(double T, double rho, double ion);
double cu_temporal_cdf(double t);
#endif

