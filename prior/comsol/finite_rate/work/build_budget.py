from pathlib import Path
import re
root=Path(__file__).resolve().parents[1]
source=root.parent/'COMSOL_CuPlasma_S4_server_handoff_20261105/project/work/ExportS4.java'
s=source.read_text().replace('public class ExportS4','public class R2Budget')
s=re.sub(r'String active=.*?;double\[\] times=',r'String active="sol2";double[] times=',s)
(root/'work/R2Budget.java').write_text(s)
