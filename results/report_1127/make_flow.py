import matplotlib; matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib import font_manager
from matplotlib.patches import FancyBboxPatch
font_manager.fontManager.addfont("/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc")
plt.rcParams["font.family"]="WenQuanYi Zen Hei"
bx=[("① 靶加热与蒸发\n一维热传导 + 熔化\nKnight 层（声速蒸发、背压、回凝）\n→ 蒸气质量/能量通量","#dbe9fb"),
("② 二维轴对称等离子体流体（PLUTO）\n局部热平衡 + 电离\n激光屏蔽（Picard 迭代）\n热传导 + 辐射损失 + 壁面传热","#fde6d6"),
("③ 合成成像\nb 波段(510±12 nm)发射\n自吸收、相机离焦模糊\n靶面遮挡、t0 平移 28 ns","#dcf3e8"),
("④ 与实验同一套指标\n总亮度 L、σx/σz 二阶矩\n质心高度、冲击波半径\n（亮度在 300 ns 归一）","#efe0f5")]
fig,ax=plt.subplots(figsize=(13,3.2)); ax.axis("off"); ax.set_xlim(0,13); ax.set_ylim(0,3)
for i,(t,c) in enumerate(bx):
    x=0.1+i*3.25
    ax.add_patch(FancyBboxPatch((x,0.4),2.9,2.2,boxstyle="round,pad=0.05",fc=c,ec="#444"))
    ax.text(x+1.45,1.5,t,ha="center",va="center",fontsize=9.5)
    if i<3: ax.annotate("",xy=(x+3.2,1.5),xytext=(x+2.95,1.5),arrowprops=dict(arrowstyle="->",lw=2))
ax.annotate("",xy=(4.9,0.38),xytext=(4.9,0.38))
ax.text(6.5,0.1,"Picard 迭代：②算出的等离子体吸收 → 回到①修正靶面入射激光 → 重算，通常 3 轮（it0 → it2）",ha="center",fontsize=9,color="#444")
fig.savefig("figs/flow.png",dpi=160,bbox_inches="tight")
