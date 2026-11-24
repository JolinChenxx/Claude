/* definitions.h -- lipsim v0 (PLAN v3): 2D axisymmetric HD, LTE table EOS (PVTE_LAW replaced by lip_eos),
 * one tracer = Cu mass fraction. Physical inputs come from pluto.ini user parameters, which are generated
 * from the parameter registry by lipsim.batch (never typed by hand). */
#define  PHYSICS                        HD
#define  DIMENSIONS                     2
#define  GEOMETRY                       CYLINDRICAL
#define  BODY_FORCE                     NO
#define  COOLING                        NO
#define  RECONSTRUCTION                 LINEAR
#define  TIME_STEPPING                  RK2
#define  NTRACER                        2   /* tr1 = Y_Cu; tr2 = v1m non-equilibrium electrons per Cu-atom mass (BD_ON) */
#define  PARTICLES                      NO
#define  USER_DEF_PARAMETERS            21

/* -- physics dependent declarations -- */
#define  DUST_FLUID                     NO
#define  EOS                            PVTE_LAW
#define  ENTROPY_SWITCH                 NO
#define  THERMAL_CONDUCTION             RK_LEGENDRE   /* v1 */
#define  VISCOSITY                      NO
#define  ROTATING_FRAME                 NO

/* -- user-defined parameters (labels) -- */
#define  E_PULSE                        0   /* J        (registry E_pulse_*) */
#define  D_BEAM                         1   /* m, 1/e2 diameter (registry D_beam_*) */
#define  FWHM                           2   /* s        (registry tau_fwhm) */
#define  T_PEAK                         3   /* s        (t0 convention: 10% of peak on leading edge) */
#define  NEC_LIMIT                      4   /* 0 = thin, 1 = resonance lines trapped */
#define  P_AMB                          5   /* Pa       (registry p_ambient) */
#define  T_AMB                          6   /* K        (registry T_ambient) */
#define  SRC_ON                         7   /* 1 = evaporation source from target table */
#define  T_FLOOR                        8   /* K, radiation energy limiter floor (= T_AMB) */
#define  DROP_M                         9   /* kg, droplet mass evaporated in the Cu core (v1j bound; 0 = off) */
#define  DROP_TCAP                      10  /* K, droplet temperature on release (= f_Tcap * T_c) */
#define  DROP_LV                        11  /* J/kg, latent heat taken from the gas on evaporation */
#define  DROP_T0                        12  /* s, start of droplet release window */
#define  DROP_T1                        13  /* s, end of droplet release window */
#define  BACKP                          14  /* 1 = Knight evaporation with back pressure (v1k); 0 = sonic (v1j) */
#define  BD_ON                          15  /* 1 = v1m two-temperature avalanche breakdown during the pulse; 0 = LTE only */
#define  SIG_EN                         16  /* m^2, electron momentum-transfer cross-section (registry sigma_en_Cu) */
#define  LOTZ_A                         17  /* m^2 eV^2, Lotz constant for Cu 4s (registry lotz_a_cu) */
#define  DROP_TB                        18  /* K; > 0: v1n energy-limited droplet release (mixture must stay >= T_boil); 0 = off */
#define  VMOM                           19  /* 1 = v1p: the evaporating vapour carries its Knight exit momentum mdot*u_v (+z); 0 = off */
#define  DROP_VD                        20  /* m/s; > 0: v1q droplets launched at DROP_T0 from the face at this measured speed evaporate only in the row at z_d = v_d (t - DROP_T0); 0 = whole core */

/* -- supplementary constants (user editable) -- */
#define  UNIT_DENSITY                   1.0e-3   /* g/cm^3 = 1 kg/m^3 */
#define  UNIT_LENGTH                    1.0e-4   /* cm = 1 um */
#define  UNIT_VELOCITY                  1.0e5    /* cm/s = 1 km/s  -> time 1 ns, pressure 1 MPa */
#define  LIMITER                        VANLEER_LIM
#define  SHOCK_FLATTENING               MULTID
#define  CHAR_LIMITING                  NO
#define  T_CUT_RHOE                     1.0
#define  TC_SATURATED_FLUX              NO
#define  MULTIPLE_LOG_FILES             YES   /* one log per MPI rank: abort messages of any rank are kept (T016) */
