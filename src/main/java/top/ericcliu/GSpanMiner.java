package top.ericcliu;

import javafx.util.Pair;
import top.ericcliu.util.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author liubi
 * @date 2019-01-19 21:08
 **/
public class GSpanMiner {
    private MultiLabelGraph dataGraph;
    private Double threshold;
    /**
     * 支持度 用以判断 1. 是否作为 父模式扩展（MNI）2. 是否作为频繁模式输出（instance num）
     */
    private Integer support;
    private double relatedRatio;
    private int resultSize = 0;

    public GSpanMiner(MultiLabelGraph dataGraph, double thresh) throws Exception {
        this.dataGraph = dataGraph;
        this.threshold = thresh;
        this.support = Math.max(2, ((Double) (threshold * this.dataGraph.getTypeRelatedNum())).intValue());
        //清洗不频繁的边
        for (Integer labelA : this.dataGraph.getGraphEdge().rowKeySet()) {
            for (Integer labelB : this.dataGraph.getGraphEdge().columnKeySet()) {
                Map<DFScode, DFScodeInstance> map = this.dataGraph.getGraphEdge().get(labelA, labelB);
                if (map != null) {
                    Boolean changed = false;
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

    public void setDataGraph(MultiLabelGraph dataGraph) {
        this.dataGraph = dataGraph;
    }







    private void mineCore(DFScode parent, DFScodeInstance parentInstances) throws Exception {
        ArrayList<GSpanEdge> childrenEdges = SingleLabelUtil.rightMostPathExtension(parent,this.dataGraph);
        ArrayList<Pair<DFScode, DFScodeInstance>> children = new ArrayList<>(childrenEdges.size());
        for (GSpanEdge childEdge : childrenEdges) {
            DFScode childDFScode = new DFScode(parent).addEdge(childEdge);
            if (!new MinDFSCodeJustifier(childDFScode).justify()) {
                continue;
            }
            DFScodeInstance childInstance = SingleLabelUtil.subGraphIsomorphism(parent, parentInstances, childEdge,
                    true,this.dataGraph);
            childDFScode.setRootNodeNum(childInstance.calRootNodeNum());
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
        if (children.isEmpty()) {
            // 如果是叶子节点，保存
            SingleLabelUtil.savePattern(parent, parentInstances, Integer.MAX_VALUE, this.threshold,
                    this.relatedRatio, this.resultSize++, this.dataGraph);
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
        String filePath = "D_10P_0.7378246753246751R_1.0T_11260.json";
        //String filePath = "D_10P_0.7616333464587202R_1.0T_8980377.json";
        Double dataSetSizeRelatedthreshold = 0.1;
        try {
            MultiLabelGraph graph = new MultiLabelGraph(filePath);
            System.out.println("finish read file");
            GSpanMiner miner = new GSpanMiner(graph, dataSetSizeRelatedthreshold);
            System.out.println(miner.getDataGraph().graphName);
            System.out.println(graph.getGraphEdge());
            System.out.println("    MNISupportThreshold" + miner.support);
            miner.mine();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}


