package top.ericcliu.util;

import javafx.util.Pair;

import java.io.File;
import java.util.*;

/**
 * @author liubi
 * @date 2019-06-04 14:56
 **/
public class MultiLabelUtil {
    public MultiLabelUtil() {
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
    public static   ArrayList<Pair<Boolean, MLGSpanEdge>>
    nAryRelationExtension(MLDFScode parent,int maxDepth, MultiLabelGraph dataGraph) throws Exception {
        ArrayList<Pair<Boolean, MLGSpanEdge>> childrenEdge = new ArrayList<>();
        LinkedList<Integer> RMP = parent.fetchRightMostPath();
        if (RMP.size() == 0 || RMP.size() == 1) {
            throw new Exception("right most path size is 0 or 1, ERROR");
        }
        if (maxDepth < 1) {
            throw new Exception("maxDepth must > 0, ERROR");
        }
        int maxSizeRMP = maxDepth + 1;
        boolean extendOnNode = true;
        if (RMP.size() >= maxSizeRMP) {
            // 超过最大深度,则跳过最右节点
            extendOnNode = false;
        }
        //extend a new label on right most node
        Iterator<Integer> descRMPit = RMP.descendingIterator();
        int RMNode = descRMPit.next(); // last Edge end node
        int RMNodeF = descRMPit.next(); // last Edge start node
        Set<Integer> RMNodeLabels = new HashSet<>(parent.fetchNodeLabel(RMNode));
        LinkedList<Integer> RMNodeFLabels = parent.fetchNodeLabel(RMNodeF);
        MLGSpanEdge lastEdge = parent.getEdgeSeq().get(parent.getEdgeSeq().size() - 1);
        int edgeLabel = lastEdge.getEdgeLabel();
        if (!parent.fetchNodeLabel(RMNode).equals(lastEdge.getLabelB())){
            throw new Exception("最右节点 没有出现在 最后一个边上");
        }
        if(!RMNodeFLabels.equals(lastEdge.getLabelA())){
            new Exception("最右节点的签一个节点 没有出现在 最后一个边上");
        }

        Set<GSpanEdgeModified> children = new HashSet<>();
        // 所有标签能够拓展出的边
        for (int RMNodeFLabel : RMNodeFLabels) {
            Set<GSpanEdgeModified> childrenTemp = new HashSet<>();
            Collection<Map<DFScode, DFScodeInstance>> collection = dataGraph.getGraphEdge().row(RMNodeFLabel).values();
            for (Map<DFScode, DFScodeInstance> map : dataGraph.getGraphEdge().row(RMNodeFLabel).values()) {
                // 单个起始节点标签相同
                for (DFScode dfScode : map.keySet()) {
                    GSpanEdge edge = dfScode.getEdgeSeq().get(0);
                    if (edge.getEdgeLabel() == edgeLabel
                            && !RMNodeLabels.contains(edge.getLabelB())) {
                        // 边标签相同,且 最右节点上 不包含 新扩展的标签
                        childrenTemp.add(new GSpanEdgeModified(dfScode.getEdgeSeq().get(0)));
                    }
                }
                if (children.isEmpty()) {
                    children.addAll(childrenTemp);
                } else {
                    children.retainAll(childrenTemp);
                }
            }
        }
        for (GSpanEdgeModified child : children) {

            LinkedList<Integer> RMNodeNewLabel = new LinkedList<>();
            RMNodeNewLabel.add(child.getLabelB());
            int newEdgeLabel = child.getEdgeLabel();
            if(newEdgeLabel != edgeLabel){
                throw new Exception("wrong edge label");
            }
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
                Set<GSpanEdgeModified> childrenTemp = new HashSet<>();
                // 单个标签能够扩展出的边
                for (Map<DFScode, DFScodeInstance> map : dataGraph.getGraphEdge().row(RMPNodeLabel).values()) {
                    for(DFScode dfScode : map.keySet()){
                        childrenTemp.add(new GSpanEdgeModified(dfScode.getEdgeSeq().get(0)));
                    }
                }
                if (children.isEmpty()) {
                    children.addAll(childrenTemp);
                } else {
                    //判等时 不跟据 nodeALabels
                    children.retainAll(childrenTemp);
                }
            }
            for (GSpanEdgeModified child : children) {
                int node2 = parent.getMaxNodeId() + 1;
                LinkedList<Integer> node2Labels = new LinkedList<>();
                node2Labels.add(child.getLabelB());
                int newEdgeLabel = child.getEdgeLabel();
                childrenEdge.add(new Pair<>(false,
                        new MLGSpanEdge<>(RMPNode, node2, RMPNodeLabels, node2Labels, newEdgeLabel, 0)));
            }
        }
        return childrenEdge;
    }

    public static  MLDFScodeInstance subGraphIsomorphism(MLDFScode parent,
                                                         MLDFScodeInstance parentInstances,
                                                         Pair<Boolean, MLGSpanEdge> childEdge,
                                                         MultiLabelGraph dataGraph) throws Exception {
        // 假设 parent 和  childernEdge 能够组成合法的childDFScode， 合法性检查已经完成
        MLDFScodeInstance childInstance = new MLDFScodeInstance();
        if (childEdge.getKey()) {
            //true: 在最右节点上添加新的标签
            int RMNode = childEdge.getValue().getNodeB();
            int newLabel = (int) childEdge.getValue().getLabelB().get(0);
            Set<Integer> newLabelNode = dataGraph.queryNodesByLabel(newLabel);
            // newLabelNode 中的实力节点包含 newLabel标签
            if(RMNode != parent.fetchRightMostPath().get(parent.fetchRightMostPath().size() - 1)) {
                throw new Exception("新增的标签不在最右节点上");
            }
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
            if(childEdge.getValue().getLabelB().size() != 1)
            {
                throw new Exception("非法参数 Pair<Boolean,MLGSpanEdge> childEdge");
            }
            int edgeLabel = childEdge.getValue().getEdgeLabel();
            int nodeBLabel = (int) childEdge.getValue().getLabelB().getFirst();
            Map<Integer, Integer> nodeAIdMap = parentInstances.fetchInstanceNode(nodeA);
            Set<Integer> posNodeBIds = dataGraph.queryNodesByLabel(nodeBLabel);
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
                    if (!dataGraph.getValueGraph().hasEdgeConnecting(nodeAId, posNodeBId)) {
                        continue;
                    }
                    int edgeValue = ((int) dataGraph.getValueGraph().edgeValue(nodeAId, posNodeBId).get());
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
    public static void savePattern(MLDFScode MLChildCode, MLDFScodeInstance MLChildInstance,
                                   int maxDepth, double threshold, double relatedRatio,
                                   int resultIndex,MultiLabelGraph dataGraph) throws Exception {
        String dirPath = "result_Thresh_"+threshold+"D_"+maxDepth+"related_ratio_"+relatedRatio+
                File.separator + dataGraph.graphName + "MNI_" + threshold;
        String fileName = dataGraph.graphName + "MNI_" + threshold + "Id_" + (resultIndex) + ".json";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        MLChildCode.saveToFile(dirPath + File.separator + "RE_" + fileName, false);
        MLChildInstance.sample(1, 10, 10).
                saveToFile(dirPath + File.separator + "IN_" + fileName, false);
    }

    public static double calRelatedRatio(MLDFScode mldfScode, MultiLabelGraph dataGraph) {
        double relatedRatio = 0d;
        for (MLGSpanEdge edge : mldfScode.getEdgeSeq()) {
            relatedRatio += edge.calMNI(dataGraph);
        }
        relatedRatio = mldfScode.getMNI() * mldfScode.getEdgeSeq().size() / relatedRatio;
        return relatedRatio;
    }
}
