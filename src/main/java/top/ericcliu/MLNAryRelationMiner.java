package top.ericcliu;

import javafx.util.Pair;
import top.ericcliu.util.*;

import java.io.File;
import java.util.*;

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

    /**
     * 模式拓展原则，在gSpan最右拓展的基础上, 进行修改
     * 去除后向扩展，具有前向扩展的深度限制
     * 1. 首先尝试在最右节点上 扩展一个标签
     * 2. 其次尝试在最右路径上 前向扩展
     *
     * @param parent
     * @return ArrayList<Pair < Boolean, MLGSpanEdge>>:
     * true: 在最右节点上添加新的标签
     * false: 在最右路径上扩展边
     * @throws Exception
     */
    private ArrayList<Pair<Boolean, MLGSpanEdge>> nAryRelationExtension(MLDFScode parent) throws Exception {
        ArrayList<Pair<Boolean, MLGSpanEdge>> childrenEdge = new ArrayList<>();
        LinkedList<Integer> RMP = parent.fetchRightMostPath();
        if (RMP.size() == 0 || RMP.size() == 1) {
            throw new Exception("right most path size is 0 or 1, ERROR");
        }
        if (this.maxDepth < 1) {
            throw new Exception("maxDepth must > 0, ERROR");
        }
        int maxSizeRMP = this.maxDepth + 1;
        boolean extendOnNode = true;
        if (RMP.size() >= maxSizeRMP) {
            // 超过最大深度,则跳过最右节点
            extendOnNode = false;
        }
        // extend a new label on right most node
        Iterator<Integer> descRMPit = RMP.descendingIterator();
        int RMNode = descRMPit.next(); // last Edge end node
        int RMNodeF = descRMPit.next(); // last Edge start node
        Set<Integer> RMNodeLabels = new HashSet<>(parent.fetchNodeLabel(RMNode));
        LinkedList<Integer> RMNodeFLabels = parent.fetchNodeLabel(RMNodeF);
        MLGSpanEdge lastEdge = parent.getEdgeSeq().get(parent.getEdgeSeq().size() - 1);
        int edgeLabel = lastEdge.getEdgeLabel();
        assert parent.fetchNodeLabel(RMNode).equals(lastEdge.getLabelB()) : "最右节点 没有出现在 最后一个边上";
        assert RMNodeFLabels.equals(lastEdge.getLabelA()) : "最右节点的签一个节点 没有出现在 最后一个边上";

        Set<DFScode> children = new HashSet<>();
        // 所有标签能够拓展出的边
        for (int RMNodeFLabel : RMNodeFLabels) {
            Set<DFScode> childrenTemp = new HashSet<>();
            Collection<Map<DFScode, DFScodeInstance>> collection = this.dataGraph.getGraphEdge().row(RMNodeFLabel).values();
            for (Map<DFScode, DFScodeInstance> map : this.dataGraph.getGraphEdge().row(RMNodeFLabel).values()) {
                // 单个起始节点标签相同
                for (DFScode dfScode : map.keySet()) {
                    GSpanEdge edge = dfScode.getEdgeSeq().get(0);
                    if (edge.getEdgeLabel() == edgeLabel
                            && !RMNodeLabels.contains(edge.getLabelB())) {
                        // 边标签相同,且 最右节点上 不包含 新扩展的标签
                        childrenTemp.add(dfScode);
                    }
                }
                if (children.isEmpty()) {
                    children.addAll(childrenTemp);
                } else {
                    children.retainAll(childrenTemp);
                }
            }
        }
        for (DFScode child : children) {
            if (child.getEdgeSeq().size() != 1) {
                throw new Exception("wrong edge");
            }
            LinkedList<Integer> RMNodeNewLabel = new LinkedList<>();
            RMNodeNewLabel.add(child.getEdgeSeq().get(0).getLabelB());
            int newEdgeLabel = child.getEdgeSeq().get(0).getEdgeLabel();
            assert newEdgeLabel == edgeLabel : "wrong edge label";
            childrenEdge.add(new Pair<>(true,
                    new MLGSpanEdge<>(RMNodeF, RMNode, RMNodeFLabels, RMNodeNewLabel, newEdgeLabel, 0)));
        }

        // forward extend
        descRMPit = RMP.descendingIterator();
        while (descRMPit.hasNext()) {
            int RMPNode = descRMPit.next();
            LinkedList<Integer> RMPNodeLabels = parent.fetchNodeLabel(RMPNode);
            if (!extendOnNode) {
                // 跳过最右节点
                extendOnNode = true;
                continue;
            }
            // 多标签，所有标签都能够扩展出的边
            children = new HashSet<>();
            for (Integer RMPNodeLabel : RMPNodeLabels) {
                Set<DFScode> childrenTemp = new HashSet<>();
                // 单个标签能够扩展出的边
                for (Map<DFScode, DFScodeInstance> map : this.dataGraph.getGraphEdge().row(RMPNodeLabel).values()) {
                    childrenTemp.addAll(map.keySet());
                }
                if (children.isEmpty()) {
                    children.addAll(childrenTemp);
                } else {
                    children.retainAll(childrenTemp);
                }
            }
            for (DFScode child : children) {
                if (child.getEdgeSeq().size() != 1) {
                    throw new Exception("wrong edge");
                }
                int node2 = parent.getMaxNodeId() + 1;
                LinkedList<Integer> node2Labels = new LinkedList<>();
                node2Labels.add(child.getEdgeSeq().get(0).getLabelB());
                int newEdgeLabel = child.getEdgeSeq().get(0).getEdgeLabel();
                childrenEdge.add(new Pair<>(false,
                        new MLGSpanEdge<>(RMPNode, node2, RMPNodeLabels, node2Labels, newEdgeLabel, 0)));
            }
        }
        return childrenEdge;
    }

    private MLDFScodeInstance subGraphIsomorphism(MLDFScode parent, MLDFScodeInstance parentInstances, Pair<Boolean, MLGSpanEdge> childEdge) throws Exception {
        // 假设 parent 和  childernEdge 能够组成合法的childDFScode， 合法性检查已经完成
        MLDFScodeInstance childInstance = new MLDFScodeInstance();
        if (childEdge.getKey()) {
            //true: 在最右节点上添加新的标签
            int RMNode = childEdge.getValue().getNodeB();
            int newLabel = (int) childEdge.getValue().getLabelB().get(0);
            Set<Integer> newLabelNode = this.dataGraph.queryNodesByLabel(newLabel);
            // newLabelNode 中的实力节点包含 newLabel标签
            assert RMNode == parent.fetchRightMostPath().get(parent.fetchRightMostPath().size() - 1)
                    : "新增的标签不在最右节点上";
            MLDFScode child = new MLDFScode(parent).addLabel(childEdge.getValue());
            for (int[] parentInstance : parentInstances.getInstances()) {
                if (newLabelNode.contains(parentInstance[RMNode])) {
                    // 最右节点的实例节点，包含newLabel标签
                    childInstance.addInstance(child, parentInstance);
                }
            }
        } else {
            //false: 在最右路径上扩展前向边，拓展的边 nodeB上只具有一个标签（增加标签的工作在上面的if条件完成）
            MLDFScode child = new MLDFScode(parent).addEdge(childEdge.getValue());
            int nodeA = childEdge.getValue().getNodeA();
            int nodeB = childEdge.getValue().getNodeB();
            {
                assert childEdge.getValue().getLabelB().size() == 1
                        : "非法参数 Pair<Boolean,MLGSpanEdge> childEdge";
            }
            int edgeLabel = childEdge.getValue().getEdgeLabel();
            int nodeBLabel = (int) childEdge.getValue().getLabelB().getFirst();
            Map<Integer, Integer> nodeAIdMap = parentInstances.fetchInstanceNode(nodeA);
            Set<Integer> posNodeBIds = this.dataGraph.queryNodesByLabel(nodeBLabel);
            for (Map.Entry<Integer, Integer> nodeAIdEntry : nodeAIdMap.entrySet()) {
                int instanceId = nodeAIdEntry.getKey();
                Set<Integer> appearedNodes = new HashSet<>();
                for (Integer node : parentInstances.getInstances().get(instanceId)) {
                    appearedNodes.add(node);
                }
                int nodeAId = nodeAIdEntry.getValue();
                for (int posNodeBId : posNodeBIds) {
                    if (appearedNodes.contains(posNodeBId)) {
                        continue;
                    }
                    if (!this.dataGraph.getValueGraph().hasEdgeConnecting(nodeAId, posNodeBId)) {
                        continue;
                    }
                    int edgeValue = ((int) this.dataGraph.getValueGraph().edgeValue(nodeAId, posNodeBId).get());
                    if (edgeLabel != edgeValue) {
                        continue;
                    }
                    int newLength = parentInstances.getInstances().get(instanceId).length + 1;
                    int[] newInstance = Arrays.copyOf(parentInstances.getInstances().get(instanceId), newLength);
                    newInstance[newLength - 1] = posNodeBId;
                    childInstance.addInstance(child, newInstance);
                }
            }

        }
        return childInstance;
    }

    private double calRelatedRatio(MLDFScode mldfScode) {
        double relatedRatio = 0d;
        for (MLGSpanEdge edge : mldfScode.getEdgeSeq()) {
            relatedRatio += edge.calMNI(this.dataGraph);
        }
        relatedRatio = mldfScode.getMNI() * mldfScode.getEdgeSeq().size() / relatedRatio;
        return relatedRatio;
    }

    private void savePattern(MLDFScode childDFScode, MLDFScodeInstance childInstance) throws Exception {
        String dirPath = "result_Thresh_"+this.threshold+"D_"+this.maxDepth+"related_ratio_"+this.relatedRatio+
                File.separator + this.dataGraph.graphName + "MNI_" + threshold;
        String fileName = this.dataGraph.graphName + "MNI_" + threshold + "Id_" + (++resultSize) + ".json";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        childDFScode.saveToFile(dirPath + File.separator + "RE_" + fileName, false);
        childInstance.sample(1, 10, 10).
                saveToFile(dirPath + File.separator + "IN_" + fileName, false);
    }

    private void mineCore(MLDFScode parent, MLDFScodeInstance parentInstances) throws Exception {
        ArrayList<Pair<Boolean, MLGSpanEdge>> childEdgePairs = nAryRelationExtension(parent);
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
            MLDFScodeInstance childInstance = subGraphIsomorphism(parent, parentInstances, childEdgePair);
            childDFScode.setRootNodeNum(childInstance.calRootNodeNum());
            if (childDFScode.getRootNodeNum() < this.support) {
                //频繁度剪枝
                continue;
            }
            {
                childDFScode.setInstanceNum(childInstance.getInstances().size());
                childDFScode.setMNI(childInstance.calMNI());
                childDFScode.setRootNodeRatio(((double) childDFScode.getRootNodeNum() / (double) this.dataGraph.getTypeRelatedNum()));
                childDFScode.setRelatedRatio(calRelatedRatio(childDFScode));
            }

            savePattern(childDFScode, childInstance);
            mineCore(childDFScode, childInstance);
        }
    }

    public void mine() throws Exception {
        Iterator<Map<DFScode, DFScodeInstance>> iterator = this.dataGraph.getGraphEdge().values().iterator();
        while (iterator.hasNext()) {
            Map<DFScode, DFScodeInstance> map = iterator.next();
            for (Map.Entry<DFScode, DFScodeInstance> entry : map.entrySet()) {
                if (entry.getKey().getNodeLabel(0).equals(this.dataGraph.getReplacedTypeId())) {
                    // 仅从当前 typeId 作为根节点 出发 拓展
                    MLDFScode mldfScode = new MLDFScode(entry.getKey());
                    MLDFScodeInstance mldfScodeInstance = new MLDFScodeInstance(entry.getValue());
                    mineCore(mldfScode, mldfScodeInstance);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String filePath = args[0];
        double threshold = Double.parseDouble(args[1]);
        int maxDepth = Integer.parseInt(args[2]);
        double relatedRatio = Double.parseDouble(args[3]);
        //String filePath = "D_10P_0.7378246753246751R_1.0T_11260.json";
/*        String filePath = "D_10P_0.8351461857952731R_1.0T_8980466.json";
        double threshold = 0.001;
        int maxDepth = 10;
        double relatedRatio = 0.001;*/
        try {
            MLNAryRelationMiner miner = new MLNAryRelationMiner(new MultiLabelGraph(filePath),
                    threshold, maxDepth, relatedRatio);
            long startTime = System.currentTimeMillis();
            miner.mine();
            System.out.println(filePath+","+ (System.currentTimeMillis() - startTime) + "," + miner.support);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
