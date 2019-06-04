package top.ericcliu;

import javafx.util.Pair;
import top.ericcliu.ds.*;
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
    private MultiLabelGraph dataGraph;
    private Double threshold;
    /**
     * 支持度 用以判断 1. 是否作为 父模式扩展（MNI）2. 是否作为频繁模式输出（instance num）
     */
    private Integer support;
    /**
     * 模式扩展的最大深度 <= maxDepth
     */
    private int maxDepth;
    private Double relatedRatio;
    private int resultSize = 0;

    public MLNAryRelationMiner(MultiLabelGraph dataGraph, double threshold, int maxDepth, double relatedRatio) throws Exception {
        this.dataGraph = dataGraph;
        this.threshold = threshold;
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
                        if(entry.getKey().getEdgeSeq().size()!=1){
                            throw new Exception("illegal edge");
                        }
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

    private void mineCore(MLDFScode parent, MLDFScodeInstance parentInstances) throws Exception {
        ArrayList<Pair<Boolean, MLGSpanEdge>> childEdgePairs = MultiLabelUtil.nAryRelationExtension(parent,this.maxDepth,this.dataGraph);
        ArrayList<Pair<MLDFScode,MLDFScodeInstance>> children = new ArrayList<>(childEdgePairs.size());
        for (Pair<Boolean, MLGSpanEdge> childEdgePair : childEdgePairs) {
            MLDFScode childDFScode = new MLDFScode(parent);
            if (childEdgePair.getKey()) {
                // add label
                childDFScode.addLabel(childEdgePair.getValue());
            } else {
                //add forward edge
                childDFScode.addEdge(childEdgePair.getValue());
            }
            if (!new MLNaryMDCJustifier(childDFScode).justify()) {
                continue;
            }
            MLDFScodeInstance childInstance = MultiLabelUtil.subGraphIsomorphism(parent, parentInstances, childEdgePair,this.dataGraph);
            childDFScode.setRootNodeNum(childInstance.calRootNodeNum());
            if (childDFScode.getRootNodeNum() < this.support) {
                continue;
            }
            {
                childDFScode.setInstanceNum(childInstance.getInstances().size());
                childDFScode.setMNI(childInstance.calMNI());
                childDFScode.setRootNodeRatio(((double) childDFScode.getRootNodeNum()
                        / (double) this.dataGraph.getTypeRelatedNum()));
                childDFScode.setRelatedRatio(MultiLabelUtil.calRelatedRatio(childDFScode,this.dataGraph));
            }
            children.add(new Pair<>(childDFScode, childInstance));
        }
        if(children.isEmpty()){
            // 如果是叶子节点，保存
            MultiLabelUtil.savePattern(parent,parentInstances,this.maxDepth,this.threshold,
                    this.relatedRatio,this.resultSize++,this.dataGraph);
        }else {
            // 如果不是叶子节点，向下递归
            for(Pair<MLDFScode,MLDFScodeInstance> child : children){
                mineCore(child.getKey(),child.getValue());
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
                    mineCore(mldfScode, mldfScodeInstance);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
/*        String filePath = args[0];
        double threshold = Double.parseDouble(args[1]);
        int maxDepth = Integer.parseInt(args[2]);
        double relatedRatio = Double.parseDouble(args[3]);*/
        String filePath = "D_10P_0.7378246753246751R_1.0T_11260.json";
        //String filePath = "D_10P_0.8351461857952731R_1.0T_8980466.json";
        //String filePath = "small";
        double threshold = 0.1;
        int maxDepth = 10;
        double relatedRatio = 0.001;
        try {
            long startTime = System.currentTimeMillis();
            MLNAryRelationMiner miner = new MLNAryRelationMiner(new MultiLabelGraph(filePath),
                    threshold, maxDepth, relatedRatio);
            miner.mine();
            System.out.println(filePath+","+ (System.currentTimeMillis() - startTime) + "," + miner.support);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
