package top.ericcliu;

import javafx.util.Pair;
import lombok.extern.log4j.Log4j2;
import top.ericcliu.ds.DFScode;
import top.ericcliu.ds.DFScodeInstance;
import top.ericcliu.ds.GSpanEdge;
import top.ericcliu.ds.MultiLabelGraph;
import top.ericcliu.util.MinDFSCodeJustifier;
import top.ericcliu.util.SingleLabelUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author liubi
 * @date 2019-01-19 21:08
 **/
@Log4j2
public class GSpanMiner {
    private MultiLabelGraph dataGraph;
    private double threshold;
    /**
     * 支持度 用以判断 1. 是否作为 父模式扩展（MNI）2. 是否作为频繁模式输出（instance num）
     */
    private int support;
    private int resultSize = 0;

    public GSpanMiner(MultiLabelGraph dataGraph, double thresh) throws Exception {
        this.dataGraph = dataGraph;
        this.threshold = thresh;
        this.support = Math.max(2, (int) (threshold * this.dataGraph.getTypeRelatedNum()));
        //清洗不频繁的边
        for (int labelA : this.dataGraph.getGraphEdge().rowKeySet()) {
            for (int labelB : this.dataGraph.getGraphEdge().columnKeySet()) {
                Map<DFScode, DFScodeInstance> map = this.dataGraph.getGraphEdge().get(labelA, labelB);
                if (map != null) {
                    boolean changed = false;
                    Iterator<Entry<DFScode, DFScodeInstance>> iterator = map.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Entry<DFScode, DFScodeInstance> entry = iterator.next();
                        if (entry.getValue().calMNI() < this.support) {
                            iterator.remove();
                            changed = true;
                        }
                    }
                    if (changed) {
                        this.dataGraph.getGraphEdge().put(labelA, labelB, map);
                    }
                }
            }
        }
    }

    public MultiLabelGraph getDataGraph() {
        return dataGraph;
    }

    private void mineCore(DFScode parent, DFScodeInstance parentInstances) throws Exception {
        ArrayList<GSpanEdge> childrenEdges = SingleLabelUtil.rightMostPathExtension(parent, this.dataGraph);
        ArrayList<Pair<DFScode, DFScodeInstance>> children = new ArrayList<>(childrenEdges.size());
        for (GSpanEdge childEdge : childrenEdges) {
            DFScode childDFScode = new DFScode(parent).addEdge(childEdge);
            if (!new MinDFSCodeJustifier(childDFScode).justify()) {
                continue;
            }
            DFScodeInstance childInstance = SingleLabelUtil.subGraphIsomorphism(parent, parentInstances, childEdge,
                    true, this.dataGraph);
            childDFScode.setRootNodeNum(childInstance.calRootNodeNum());
            // 使用 RootNodeNum 作为频繁度剪枝手段
            if (childDFScode.getRootNodeNum() < this.support) {
                continue;
            }
            {
                childDFScode.setMNI(childInstance.calMNI());
                childDFScode.setInstanceNum(childInstance.getInstances().size());
                childDFScode.setRootNodeRatio((double) (childDFScode.getRootNodeNum()
                        / this.dataGraph.getTypeRelatedNum()));
                childDFScode.setRelatedRatio(SingleLabelUtil.calRelatedRatio(childDFScode, this.dataGraph));
            }
            children.add(new Pair<>(childDFScode, childInstance));
        }
        if (children.isEmpty() && parent.getEdgeSeq().size() > 1) {
            // 如果是叶子节点 且 不是二元关系 ie. 不止一条边，保存
            SingleLabelUtil.savePattern(parent, parentInstances, Integer.MAX_VALUE, this.threshold,
                    Integer.MAX_VALUE, this.resultSize++, this.dataGraph, "gSpan");
        } else {
            // 如果不是叶子节点，向下递归
            for (Pair<DFScode, DFScodeInstance> child : children) {
                mineCore(child.getKey(), child.getValue());
            }
        }
    }

    public void mine() throws Exception {
        Iterator<Map<DFScode, DFScodeInstance>> iterator = this.getDataGraph().getGraphEdge().values().iterator();
        while (iterator.hasNext()) {
            Map<DFScode, DFScodeInstance> map = iterator.next();
            for (Entry<DFScode, DFScodeInstance> entry : map.entrySet()) {
                if (entry.getKey().fetchNodeLabel(0).equals(this.dataGraph.getReplacedTypeId())) {
                    // 仅从当前 typeId 作为根节点 出发 拓展
                    mineCore(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
/*        String filePath = args[0];
        Double dataSetSizeRelatedthreshold = Double.parseDouble(args[1]);*/
        //String filePath = "D_10P_0.7378246753246751R_1.0T_11260.json";
        String filePath = "D_10P_0.8351461857952731R_1.0T_8980466.json";
        double threshold = 0.1;
        try {
            long startTime = System.currentTimeMillis();
            GSpanMiner miner = new GSpanMiner(new MultiLabelGraph(filePath), threshold);
            miner.mine();
            log.info(filePath + "," + (System.currentTimeMillis() - startTime) + "," + miner.support);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}


