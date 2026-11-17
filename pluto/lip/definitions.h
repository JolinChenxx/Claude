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
#define  NTRACER                        1
#define  PARTICLES                      NO
#define  USER_DEF_PARAMETERS            9

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

/* -- supplementary constants (user editable) -- */
#define  UNIT_DENSITY                   1.0e-3   /* g/cm^3 = 1 kg/m^3 */
#define  UNIT_LENGTH                    1.0e-4   /* cm = 1 um */
#define  UNIT_VELOCITY                  1.0e5    /* cm/s = 1 km/s  -> time 1 ns, pressure 1 MPa */
#define  LIMITER                        VANLEER_LIM
#define  SHOCK_FLATTENING               MULTID
#define  CHAR_LIMITING                  NO
#define  T_CUT_RHOE                     1.0
#define  TC_SATURATED_FLUX              NO
