import json,tempfile,unittest
from pathlib import Path
from unittest.mock import patch
import atomic_json64

class AtomicWrites(unittest.TestCase):
    def test_transient_reader_lock_preserves_old_then_replaces(self):
        with tempfile.TemporaryDirectory() as directory:
            target=Path(directory)/'status.json';target.write_text('{"old":true}')
            original=atomic_json64.os.replace;attempts=[]
            def replace(source,destination):
                attempts.append(1)
                if len(attempts)<3:
                    self.assertEqual(json.loads(target.read_text()),{'old':True})
                    raise PermissionError('simulated transient Windows lock')
                return original(source,destination)
            with patch.object(atomic_json64.os,'replace',replace):atomic_json64.write_json(target,{'new':True})
            self.assertEqual(json.loads(target.read_text()),{'new':True})
            self.assertEqual(len(attempts),3)
            self.assertEqual(list(Path(directory).glob('*.tmp')),[])
    def test_persistent_lock_is_bounded_and_old_state_survives(self):
        with tempfile.TemporaryDirectory() as directory:
            target=Path(directory)/'status.json';target.write_text('{"old":true}')
            with patch.object(atomic_json64.os,'replace',side_effect=PermissionError('persistent')):
                with self.assertRaises(PermissionError):atomic_json64.write_json(target,{'new':True},timeout=0)
            self.assertEqual(json.loads(target.read_text()),{'old':True})
            self.assertEqual(list(Path(directory).glob('*.tmp')),[])

if __name__=='__main__':unittest.main()
