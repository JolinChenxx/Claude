/* Conservative finite-volume deposition, reduced Cu/Cu+ chemistry and cooling.
   Hydro is the upstream PLUTO solver. No experimental image enters this file. */
#include "pluto.h"
#include "cuair.h"
double cu_radiated_local=0.;
double cu_temporal_cdf(double t){
  if(t<=0.)return 0.;
  double f=g_inputParam[TAIL_FRAC],tau=g_inputParam[TAIL_NS];
  double pulse=t<60.?(t/60.-sin(2.*CONST_PI*t/60.)/(2.*CONST_PI)):1.;
  return (1.-f)*pulse+(t>60.?f*(1.-exp(-(t-60.)/tau)):0.);
}
/* E1 exponential integral, convergent series/continued fraction. */
static double expint1(double x){
  int n;double a,b,c,d,del,h,sum,term;
  if(x>700.)return 0.;
  if(x<1.){sum=-.5772156649015329-log(x);term=1.;
    for(n=1;n<60;n++){term*=-x/n;del=-term/n;sum+=del;if(fabs(del)<1e-13)break;}return sum;}
  b=x+1.;c=1.e30;d=1./b;h=d;
  for(n=1;n<80;n++){a=-n*n;b+=2.;d=1./(a*d+b);c=b+a/c;del=c*d;h*=del;if(fabs(del-1.)<1e-10)break;}
  return h*exp(-x);
}
double cu_line_loss(double T,double rho,double y,double ion,double width,double length){
  if(T<500.||y<=ion)return 0.;
  /* Cu I 510.55 nm reduced two-level LTE excitation closure.
     Eupper=3.817 eV, gupper/g_ground=2, A=2e6 s^-1.
     Partition truncation, LTE level populations and grey escape are approximations. */
  double neutral=rho*fmax(y-ion,0.)/CU_MASS;
  double fraction=2.*exp(-3.817*EV_SI/(KB_SI*T));
  double lambda=510.55e-9,photon=6.62607015e-34*299792458./lambda;
  double emission=neutral*(fraction/(1.+fraction))*2.e6*photon;
  double planck=2.*6.62607015e-34*pow(299792458.,2)/pow(lambda,5)/expm1(photon/(KB_SI*T));
  double opacity=emission/(4.*CONST_PI*fmax(planck*width*1.e-9,1.e-30));
  return emission/(1.+opacity*length);
}
double cu_ff_loss(double T,double rho,double ion){
  double ne=rho*fmax(ion,0.)/CU_MASS;
  return 1.426e-40*sqrt(fmax(T,1.))*ne*ne;
}
void SplitSource(Data *d,double dt,timeStep *Dts,Grid *grid){
  int i,j,k,n;static double ***kernel=NULL;static double norm=0.;
  double v[NVAR],mass=g_inputParam[MASS_NG]*1.e6,energy=g_inputParam[ENERGY_UJ]*1.e6;
  double frac=g_inputParam[KINETIC_FRAC],w=g_inputParam[WAIST_UM],h=g_inputParam[SOURCE_HEIGHT_UM];
  double velocity=sqrt(2.*frac*energy/mass);
  if(kernel==NULL){
    kernel=ARRAY_3D(NX3_TOT,NX2_TOT,NX1_TOT,double);double local=0.;
    DOM_LOOP(k,j,i){
      double r=grid->xgc[IDIR][i],z=grid->xgc[JDIR][j];
      kernel[k][j][i]=exp(-2.*r*r/(w*w)-z/h);
      local+=kernel[k][j][i]*2.*CONST_PI*grid->dV[k][j][i];
    }
#ifdef PARALLEL
    MPI_Allreduce(&local,&norm,1,MPI_DOUBLE,MPI_SUM,MPI_COMM_WORLD);
#else
    norm=local;
#endif
  }
  double df=cu_temporal_cdf(g_time+dt)-cu_temporal_cdf(g_time);
  DOM_LOOP(k,j,i){
    for(n=0;n<NVAR;n++)v[n]=d->Vc[n][k][j][i];
    double T,rho=v[RHO];GetPV_Temperature(v,&T);
    double dm=mass*df*kernel[k][j][i]/norm;
    double total=InternalEnergy(v,T)+.5*rho*(v[VX1]*v[VX1]+v[VX2]*v[VX2]+v[VX3]*v[VX3]);
    if(dm>1.e-25){
      total+=energy/mass*dm;
      v[CU_Y]=(rho*v[CU_Y]+dm)/(rho+dm);
      v[CU_ION_Y]=(rho*v[CU_ION_Y]+1.e-3*dm)/(rho+dm);
      v[VX1]=rho*v[VX1]/(rho+dm);
      v[VX2]=(rho*v[VX2]+dm*velocity)/(rho+dm);
      v[VX3]=rho*v[VX3]/(rho+dm);v[RHO]=rho+=dm;
    }
    double kinetic=.5*rho*(v[VX1]*v[VX1]+v[VX2]*v[VX2]+v[VX3]*v[VX3]);
    double internal=total-kinetic;
    GetEV_Temperature(internal,v,&T);T=fmax(T,T_CUT_RHOE);
    double y=fmax(0.,fmin(1.,v[CU_Y]));
    double ion=fmax(0.,fmin(y,v[CU_ION_Y]));
    if(y>1.e-9){
      /* Subcycle chemistry; thermal feedback resolved within source update.
         Lotz single-valence electron ionization and detailed-balance 3-body recombination. */
      int sub;double remaining=dt*1.e-9;
      for(sub=0;sub<64 && remaining>1.e-20;sub++){
        double ne=rho*fmax(ion,1.e-12*y)/CU_MASS,te=KB_SI*T/EV_SI;
        double ki=6.692e5*4.5e-18/7.72638/sqrt(fmax(te,1.e-6))*expint1(7.72638/fmax(te,1.e-6));
        double saha=2.414e21*pow(T,1.5)*exp(-7.72638/fmax(te,1.e-6));
        double recomb3=saha>1.e-50?ki/saha:0.;
        double rr=2.6e-19*pow(fmax(T,300.)/1.e4,-.7);
        double aa=ki*ne,bb=recomb3*ne*ne+rr*ne;
        double step=fmin(remaining,.15/fmax(aa+bb,1.));
        if(sub==63)step=remaining;
        double target=y*aa/fmax(aa+bb,1.e-30);
        double updated=target+(ion-target)*exp(-(aa+bb)*step);
        /* Energy cannot supply more ionization than the available thermal energy. */
        double maxion=ion+fmax(internal*1.e6/rho-ion*CU_CHI_JKG-cu_cv(v)*50.,0.)/CU_CHI_JKG;
        updated=fmin(y,fmax(0.,fmin(updated,maxion)));
        double radrec=rr*ne*(rho*ion/CU_MASS)*(7.72638*EV_SI+1.5*KB_SI*T);
        double losses=g_inputParam[COOLING_SCALE]*(cu_ff_loss(T,rho,ion)+radrec+
          cu_line_loss(T,rho,y,ion,g_inputParam[LINE_WIDTH_NM],50.e-6));
        double loss=fmin(losses*step/1.e6,fmax(0.,internal-rho*(updated*CU_CHI_JKG+cu_cv(v)*50.)/1.e6));
        internal-=loss;cu_radiated_local+=loss*2.*CONST_PI*grid->dV[k][j][i];
        ion=updated;v[CU_ION_Y]=ion;GetEV_Temperature(internal,v,&T);T=fmax(T,50.);
        remaining-=step;
      }
    }
    v[PRS]=Pressure(v,T);
    for(n=0;n<NVAR;n++)d->Vc[n][k][j][i]=v[n];
  }
}

