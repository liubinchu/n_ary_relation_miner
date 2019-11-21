package top.ericcliu;

import javafx.util.Pair;
import top.ericcliu.ds.DFScode;
import top.ericcliu.ds.DFScodeInstance;
import top.ericcliu.ds.GSpanEdge;
import top.ericcliu.ds.MultiLabelGraph;
import top.ericcliu.util.NaryMDCJustifier;
import top.ericcliu.util.SingleLabelUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * @author liubi
 * @date 2019-05-19 21:33
 **/
public class NAryRelationMiner {
    private MultiLabelGraph dataGraph;
    private Double threshold;
    /**
     * 支持度 用以判断 1. 是否作为 父模式扩展（MNI）2. 是否作为频繁模式输出（instance num）
     */
    private int support;
    /**
     * 模式扩展的最大深度 <= maxDepth
     */
    private int maxDepth;
    private double relatedRatio;
    private int resultSize = 0;

    public NAryRelationMiner(MultiLabelGraph dataGraph, double thresh, int maxDepth, double relatedRatio) throws Exception {
        this.dataGraph = dataGraph;
        this.threshold = thresh;
        this.support = Math.max(2, ((Double) (threshold * this.dataGraph.getTypeRelatedNum())).intValue());
        this.maxDepth = maxDepth;
        this.relatedRatio = relatedRatio;
        //清洗不频繁的边
        for (Integer labelA : this.dataGraph.getGraphEdge().rowKeySet()) {
            for (Integer labelB : this.dataGraph.getGraphEdge().columnKeySet()) {
                Map<DFScode, DFScodeInstance> map = this.dataGraph.getGraphEdge().get(labelA, labelB);
                if (map != null) {
                    boolean changed = false;
                    Iterator<Map.Entry<DFScode, DFScodeInstance>> it = map.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<DFScode, DFScodeInstance> entry = it.next();
                        if (entry.getValue().calMNI() < this.support) {
                            it.remove();
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
        ArrayList<GSpanEdge> childrenEdges = SingleLabelUtil.nAryRelationExtension(parent,this.dataGraph,this.maxDepth);
        ArrayList<Pair<DFScode,DFScodeInstance>> children = new ArrayList<>(childrenEdges.size());
        for (GSpanEdge childEdge : childrenEdges) {
            DFScode childDFScode = new DFScode(parent).addEdge(childEdge);
            if (!new NaryMDCJustifier(childDFScode,this.maxDepth).justify()) {
                continue;
            }
            DFScodeInstance childInstance = SingleLabelUtil.subGraphIsomorphism(parent, parentInstances, childEdge,
                    false,this.dataGraph);
            childDFScode.setRootNodeNum(childInstance.calRootNodeNum());
            // 使用 RootNodeNum 作为频繁度剪枝手段
            if(childDFScode.getRootNodeNum()<this.support){
                continue;
            }
            {
                childDFScode.setMNI(childInstance.calMNI());
                childDFScode.setInstanceNum(childInstance.getInstances().size());
                childDFScode.setRootNodeRatio((double) (childDFScode.getRootNodeNum()
                        /this.dataGraph.getTypeRelatedNum()));
                childDFScode.setRelatedRatio(SingleLabelUtil.calRelatedRatio(childDFScode,this.dataGraph));
            }
            children.add(new Pair<>(childDFScode, childInstance));
        }
        if(children.isEmpty()&&parent.getEdgeSeq().size()>1){
            // 如果是叶子节点 且 不是二元关系 ie. 不止一条边，保存
            SingleLabelUtil.savePattern(parent, parentInstances,this.maxDepth,this.threshold,
                    this.relatedRatio,this.resultSize++,this.dataGraph,"SLNaryRelation");
        }
        else {
            // 如果不是叶子节点，向下递归
            for(Pair<DFScode,DFScodeInstance> child : children){
                mineCore(child.getKey(),child.getValue());
            }
        }
    }

    public void mine() throws Exception {
        Iterator<Map<DFScode, DFScodeInstance>> iterator = this.getDataGraph().getGraphEdge().values().iterator();
        while (iterator.hasNext()) {
            Map<DFScode, DFScodeInstance> map = iterator.next();
            for (Map.Entry<DFScode, DFScodeInstance> entry : map.entrySet()) {
                if (entry.getKey().fetchNodeLabel(0).equals(this.dataGraph.getReplacedTypeId())) {
                    // 仅从当前 typeId 作为根节点 出发 拓展
                    mineCore(entry.getKey(), entry.getValue());
                }
            }
        }
    }


    public static void main(String[] args) throws Exception {
        String filePath = args[0];
        double threshold = Double.parseDouble(args[1]);
        int maxDepth = Integer.parseInt(args[2]);
        double relatedRatio = Double.parseDouble(args[3]);

/*        String filePath = "D_10P_0.8351461857952731R_1.0T_8980466.json";
        //String filePath = "D_10P_0.7616333464587202R_1.0T_8980377.json";
        double threshold = 0.1;
        int maxDepth = 10;
        double relatedRatio = 0.1;*/

        try {
            MultiLabelGraph graph = new MultiLabelGraph(filePath);
            NAryRelationMiner miner = new NAryRelationMiner(graph, threshold, maxDepth, relatedRatio);
            miner.mine();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
