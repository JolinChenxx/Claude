import unittest
import numpy as np
from convergence_metrics import assess,combined
from camera_s4 import gate,response

class Checks(unittest.TestCase):
    def test_same_signal(self):
        self.assertTrue(assess([1.,2.],[1.,2.])['within_10pct'])
    def test_zero_not_evaluable(self):
        self.assertEqual(assess([0.,0.],[0.,0.])['status'],'not_evaluable')
    def test_saturation_cannot_hide_physics(self):
        a=np.array([1.,2.]);b=2*a
        cam=assess(response(a,1e40,0),response(b,1e40,0))
        physical=assess(a,b)
        self.assertTrue(cam['within_10pct'])
        self.assertFalse(combined({'brightness':cam},physical))
    def test_new_signal(self):
        self.assertFalse(assess([1.,0.],[1.,.02])['within_10pct'])
    def test_nonfinite(self):
        for x in [np.nan,np.inf]:self.assertEqual(assess([1.,2.],[1.,x])['status'],'failed')
    def test_gate_coverage(self):
        with self.assertRaises(ValueError):gate(np.array([0.,2.]),np.ones((2,1,1)),[1.])
    def test_faint_finite_is_mathematically_evaluable(self):
        self.assertTrue(assess([1e-70,2e-70],[1e-70,2e-70])['within_10pct'])
    def test_machine_report_nonfinite_and_text_unavailable(self):
        import json
        from compare_numerics import finite_json
        from deliver_report import percent
        data=finite_json({'peak':np.nan,'nested':[np.inf,-np.inf]})
        self.assertEqual(json.loads(json.dumps(data,allow_nan=False)),{'peak':None,'nested':[None,None]})
        self.assertEqual(percent(None),'不可评估')

if __name__=='__main__':unittest.main()
