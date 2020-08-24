package top.ericcliu.Miner;

import javafx.util.Pair;
import top.ericcliu.ds.*;
import top.ericcliu.tools.DepthPrinter;
import top.ericcliu.util.MLNaryMDCJustifier;
import top.ericcliu.util.MultiLabelUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * @author liubi
 * @date 2019-05-27 10:10
 **/
public class MLNAryRelationMiner {
    public static int leafNums = 0;
    //挖掘空间中叶子节点数
    public static int nodeNums = 0;
    //挖掘空间中节点数
    // 用以统计结果数量
    private MultiLabelGraph dataGraph;
    private double threshold;

    //private static Logger log = LogUtil.getLogger();
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

    public MLNAryRelationMiner(MultiLabelGraph dataGraph, double threshold, int maxDepth, double relatedRatio) throws Exception {
        this.dataGraph = dataGraph;
        this.threshold = threshold;
        this.support = Math.max(2, ((Double) (threshold * this.dataGraph.getTypeRelatedNum())).intValue());
        this.maxDepth = maxDepth;
        this.relatedRatio = relatedRatio;
        //清洗不频繁的边
        for (int labelA : this.dataGraph.getGraphEdge().rowKeySet()) {
            for (int labelB : this.dataGraph.getGraphEdge().columnKeySet()) {
                Map<DFScode, DFScodeInstance> map = this.dataGraph.getGraphEdge().get(labelA, labelB);
                if (map != null) {
                    boolean changed = false;
                    Iterator<Map.Entry<DFScode, DFScodeInstance>> it = map.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<DFScode, DFScodeInstance> entry = it.next();
                        if (entry.getKey().getEdgeSeq().size() != 1) {
                            throw new Exception("illegal edge");
                        }
                        if (entry.getValue().calMNI() < this.support) {
                            it.remove();
                            changed = true;
                        }

/*                        MLDFScode mldfScode = new MLDFScode(entry.getKey());
                        MLDFScodeInstance mldfScodeInstance = new MLDFScodeInstance(entry.getValue());
                        mldfScode.setRootNodeNum(mldfScodeInstance.calNMNI());
                        mldfScode.setInstanceNum(mldfScodeInstance.getInstances().size());
                        mldfScode.setMNI(mldfScodeInstance.calMMNI());
                        mldfScode.setRootNodeRatio(((double) mldfScode.getRootNodeNum()
                                / (double) this.dataGraph.getTypeRelatedNum()));
                        mldfScode.setRelatedRatio(MultiLabelUtil.calRelatedRatio(mldfScode,
                                this.dataGraph));
                        System.out.println(mldfScode.toJsonString());
                        System.out.println(new MLDFScodeString(
                                mldfScode,
                                "E:\\bioportal.sqlite",
                                10156).toJsonString());*/
                    }
                    if (changed) {
                        this.dataGraph.getGraphEdge().put(labelA, labelB, map);
                    }
                }
            }
        }
    }

    private void mineCore(MLDFScode parent, MLDFScodeInstance parentInstances, int depth) throws Exception {
        // 该深度是模式扩展的搜索深度，也即搜索空间树的深度，在单标签中同 模式的深度相等 在多标签中，
        // 模式的一个节点上可能扩展扩展多次（产生多标签），因此搜索空间树的深度大于等于模式的深度
        DepthPrinter.log(depth, this.dataGraph.graphName);
        System.out.println("Depth: " + depth +
                ",NMNI:" + parent.getRootNodeNum() +
                ",MMNI:" + parent.getMNI() +
                ",InstanceNum:" + parent.getInstanceNum()
        );
        ArrayList<Pair<Boolean, MLGSpanEdge>> childEdgePairs = MultiLabelUtil.nAryRelationExtension(parent, this.maxDepth, this.dataGraph);
        ArrayList<Pair<MLDFScode, MLDFScodeInstance>> children = new ArrayList<>(childEdgePairs.size());
        for (Pair<Boolean, MLGSpanEdge> childEdgePair : childEdgePairs) {
            MLDFScode childDFScode = new MLDFScode(parent);
            if (childEdgePair.getKey()) {
                // add label
                childDFScode.addLabel(childEdgePair.getValue());
            } else {
                // add forward edge
                childDFScode.addEdge(childEdgePair.getValue());
            }
            if (!new MLNaryMDCJustifier(childDFScode).justify()) {
                continue;
            }
            MLDFScodeInstance childInstance = MultiLabelUtil.subGraphIsomorphism(parent, parentInstances, childEdgePair, this.dataGraph);

            childDFScode.setRootNodeNum(childInstance.calNMNI());
            childDFScode.setInstanceNum(childInstance.getInstances().size());
            childDFScode.setMNI(childInstance.calMMNI());
            childDFScode.setRootNodeRatio(((double) childDFScode.getRootNodeNum()
                    / (double) this.dataGraph.getTypeRelatedNum()));
            childDFScode.setRelatedRatio(MultiLabelUtil.calRelatedRatio(childDFScode,
                    this.dataGraph));

            System.out.println(childDFScode.toJsonString());
            System.out.println(new MLDFScodeString(
                    childDFScode,
                    "E:\\bioportal.sqlite",
                    10156).toJsonString());
            // 使用 NMNI 作为频繁度剪枝手段
            if (childDFScode.getRootNodeNum() < this.support) {
                continue;
            }
            children.add(new Pair<>(childDFScode, childInstance));
        }
        if (children.isEmpty() && parent.getEdgeSeq().size() > 1) {
            // 如果是叶子节点 且 不是二元关系 ie. 不止一条边，保存
            leafNums++;
            nodeNums++;
            MultiLabelUtil.savePattern(parent, parentInstances, this.maxDepth, this.threshold,
                    this.relatedRatio, this.resultSize++, this.dataGraph);
        } else {
            nodeNums++;
            // 如果不是叶子节点，向下递归
            for (Pair<MLDFScode, MLDFScodeInstance> child : children) {
                mineCore(child.getKey(), child.getValue(), (depth + 1));
            }
        }
    }

    public void mine() throws Exception {
        Iterator<Map<DFScode, DFScodeInstance>> iterator = this.dataGraph.getGraphEdge().values().iterator();
        while (iterator.hasNext()) {
            Map<DFScode, DFScodeInstance> map = iterator.next();
            for (Map.Entry<DFScode, DFScodeInstance> entry : map.entrySet()) {
                if (entry.getKey().fetchNodeLabel(0).equals(this.dataGraph.getReplacedTypeId())) {
                    // 仅从当前 typeId 作为根节点 出发 拓展
                    MLDFScode mldfScode = new MLDFScode(entry.getKey());
                    MLDFScodeInstance mldfScodeInstance = new MLDFScodeInstance(entry.getValue());
                    mldfScode.setRootNodeNum(mldfScodeInstance.calNMNI());
                    mldfScode.setInstanceNum(mldfScodeInstance.getInstances().size());
                    mldfScode.setMNI(mldfScodeInstance.calMMNI());
                    mldfScode.setRootNodeRatio(((double) mldfScode.getRootNodeNum()
                            / (double) this.dataGraph.getTypeRelatedNum()));
                    mldfScode.setRelatedRatio(MultiLabelUtil.calRelatedRatio(mldfScode,
                            this.dataGraph));
                    mineCore(mldfScode, mldfScodeInstance, 1);
                }
            }
        }
    }


    public static void main(String[] args) throws Exception {
/*        String filePath = args[0];
        double threshold = Double.parseDouble(args[1]);
        int maxDepth = Integer.parseInt(args[2]);
        double relatedRatio = Double.parseDouble(args[3]);*/
        String filePath = "D_5P_10156R_50T_10156.json";
        double threshold = 0.01;
        int maxDepth = 3;
        double relatedRatio = 0.001;
        double purity = Double.parseDouble(filePath.split("[_R]")[2]);
        // 当前版本不起作用 经过验证，相关性度量不起作用
        try {
            long startTime = System.currentTimeMillis();
            MultiLabelGraph graph = new MultiLabelGraph(filePath);
            MLNAryRelationMiner miner = new MLNAryRelationMiner(graph,
                    threshold, maxDepth, relatedRatio);
            miner.mine();
            System.out.println("filePath,edges,nodes,threshold,support,purity,time,leafNums,nodeNums");
            System.out.println(filePath +
                    "," + graph.getValueGraph().edges().size()
                    + "," + graph.getValueGraph().nodes().size()
                    + "," + threshold
                    + "," + miner.support
                    + "," + purity
                    + "," + (System.currentTimeMillis() - startTime)
                    + "," + leafNums
                    + "," + nodeNums
            );
        } catch (Exception e) {
            System.out.println(filePath + ":      " + e.getMessage());
        }
    }
}
