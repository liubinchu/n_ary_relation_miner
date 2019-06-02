package top.ericcliu.util;

import javafx.util.Pair;

import java.util.*;

/**
 * @author liubi
 * @date 2019-05-28 09:41
 **/
public class MLNaryMDCJustifier {
    private MultiLabelGraph mlDFSCodeGraph;
    private MLDFScode mlDFSCode;

    public MLNaryMDCJustifier(MLDFScode mlDFSCode) throws Exception {
        this.mlDFSCode = mlDFSCode;
        this.mlDFSCodeGraph = new MultiLabelGraph(this.mlDFSCode);
    }

    public boolean justify() throws Exception {
        try {
            int edgeIndex = -1;
            // 标记待判断的DFS code 边 id
            MLDFScode minDFScode = new MLDFScode();
            MLDFScodeInstance minDFSCodeInstance = null;
            // 选取最小边
            GSpanEdge minEdge = null;
            Iterator<Map<DFScode, DFScodeInstance>> mapIt = this.mlDFSCodeGraph.getGraphEdge().values().iterator();
            while (mapIt.hasNext()) {
                for (Map.Entry<DFScode, DFScodeInstance> entry : mapIt.next().entrySet()) {
                    ArrayList<GSpanEdge> edgeSeq = entry.getKey().getEdgeSeq();
                    DFScodeInstance currentInstance = entry.getValue();
                    if(edgeSeq.size() != 1){
                        throw new Exception("dFSCodeGraph 初始化 存在问题");
                    }
                    GSpanEdge currentEdge = edgeSeq.get(0);
                    if (minEdge == null || minEdge.compareTo(currentEdge) > 0) {
                        minEdge = currentEdge;
                        minDFSCodeInstance = new MLDFScodeInstance(currentInstance);
                    }
                }
            }
            MLGSpanEdge minMLEdge = new MLGSpanEdge(minEdge);
            if (!compare(new Pair(false, minMLEdge), ++edgeIndex, minDFScode)) {
                // 生成的DFScode 更小， 给定dfs code不是最小DFScode
                return false;
            } else {
                minDFScode.addEdge(minMLEdge);
                // 向最小DFScode添加最小边，若当前多标签边标签已扩展完，则更新index
            }
            minMLEdge = null;
            minEdge = null;

            while (minDFScode.getTurn() < this.mlDFSCode.getTurn()) {
                //对最小DFS code 进行最右拓展
                ArrayList<Pair<Boolean, MLGSpanEdge>> childrenEdge = nAryRelationExtension(minDFScode);
                Map<Pair<Boolean, MLGSpanEdge>, MLDFScodeInstance> childrenEdgeInstanceMap = new HashMap<>(childrenEdge.size());
                Pair<Boolean, MLGSpanEdge> minMLEdgePair = null;
                Iterator<Pair<Boolean, MLGSpanEdge>> edgeIt = childrenEdge.iterator();
                while (edgeIt.hasNext()) {
                    Pair<Boolean, MLGSpanEdge> childEdgePair = edgeIt.next();
                    MLDFScodeInstance childInstace = subGraphIsomorphism(minDFScode, minDFSCodeInstance, childEdgePair);
                    childrenEdgeInstanceMap.put(childEdgePair, childInstace);
                }
                for (Map.Entry<Pair<Boolean, MLGSpanEdge>, MLDFScodeInstance> entry : childrenEdgeInstanceMap.entrySet()) {
                    if (entry.getValue().calMNI() > 0) {
                        if (minMLEdgePair == null || entry.getKey().getValue().compareTo(minMLEdgePair.getValue())<0) {
                            minMLEdgePair = entry.getKey();
                            minDFSCodeInstance = entry.getValue();
                        }
                    }
                }
                if (minMLEdgePair == null) {
                    System.err.println("childrenEdge size == 0, or all childInstace.getMNI() < 0, no valid childrenEdge");
                    return false;
                    //  应该不会出现这种情况 bug 待解决
                } else {
                    if (minMLEdgePair.getKey()) {
                        // add label
                        if (!compare(minMLEdgePair, edgeIndex, minDFScode)) {
                            return false;
                        }
                        minDFScode.addLabel(minMLEdgePair.getValue());
                    } else {
                        // add forward edge
                        if (!compare(minMLEdgePair, ++edgeIndex, minDFScode)) {
                            return false;
                        }
                        minDFScode.addEdge(minMLEdgePair.getValue());
                    }
                }

            }
            return true;
        }catch (Exception e){
            // 吃掉bug
            return false;
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

        Set<DFScode> children = new HashSet<>();
        // 所有标签能够拓展出的边
        for (int RMNodeFLabel : RMNodeFLabels) {
            Set<DFScode> childrenTemp = new HashSet<>();
            Collection<Map<DFScode, DFScodeInstance>> collection = this.mlDFSCodeGraph.getGraphEdge().row(RMNodeFLabel).values();
            for (Map<DFScode, DFScodeInstance> map : this.mlDFSCodeGraph.getGraphEdge().row(RMNodeFLabel).values()) {
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
            // 多标签，所有标签都能够扩展出的边
            children = new HashSet<>();
            for (Integer RMPNodeLabel : RMPNodeLabels) {
                Set<DFScode> childrenTemp = new HashSet<>();
                // 单个标签能够扩展出的边
                for (Map<DFScode, DFScodeInstance> map : this.mlDFSCodeGraph.getGraphEdge().row(RMPNodeLabel).values()) {
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
            Set<Integer> newLabelNode = this.mlDFSCodeGraph.queryNodesByLabel(newLabel);
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
            Set<Integer> posNodeBIds = this.mlDFSCodeGraph.queryNodesByLabel(nodeBLabel);
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
                    if (!this.mlDFSCodeGraph.getValueGraph().hasEdgeConnecting(nodeAId, posNodeBId)) {
                        continue;
                    }
                    int edgeValue = ((int) this.mlDFSCodeGraph.getValueGraph().edgeValue(nodeAId, posNodeBId).get());
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

    /**
     * 判断给定的minEdgePair 和  index 指定的this.mlDFSCode 中的边的大小
     *
     * @param minEdgePair
     * @param index
     * @return true: minEdgePair 是该步需要扩展的最小边
     * false: minEdgePair 不是该步需要扩展的最小边
     */
    private boolean compare(Pair<Boolean, MLGSpanEdge> minEdgePair, int index, MLDFScode minDFScode) throws Exception {
        MLGSpanEdge minEdge = minEdgePair.getValue();
        MLGSpanEdge dfScodeEdge = this.mlDFSCode.getEdgeSeq().get(index);
        {
           if (minEdge.getNodeA() != dfScodeEdge.getNodeA()
                    || minEdge.getNodeB() != dfScodeEdge.getNodeB()
                    || minEdge.getLabelB().size() != 1) {
               throw new Exception("非法输入1");
            }
        }
        if (minEdge.getDirection() != dfScodeEdge.getDirection()) {
            return false;
        }
        int minEdgeLabelB = (int) minEdge.getLabelB().getFirst();
        Set<Integer> dfScodeEdgeLabelB = new HashSet<>(dfScodeEdge.getLabelB());
        Set<Integer> minEdgeLabelA = new HashSet<>(minEdge.getLabelA());
        Set<Integer> dfScodeEdgeLabelA = new HashSet<>(dfScodeEdge.getLabelA());
        if (minEdgePair.getKey()) {
            // add label
            if(!minEdgeLabelA.equals(dfScodeEdgeLabelA)){
                throw new Exception("非法输入2");
            }
            if (minEdge.getEdgeLabel() != dfScodeEdge.getEdgeLabel()) {
                return false;
            }
            int labelIndex = minDFScode.fetchNodeLabel(minDFScode.getMaxNodeId()).size();
            return dfScodeEdge.getLabelB().get(labelIndex).equals(minEdgeLabelB);
        } else {
            // add a forward edge
            Set<Integer> temp = new HashSet<>(minEdgeLabelA);
            temp.removeAll(dfScodeEdgeLabelA);
            if(!temp.isEmpty()){
                throw new Exception("非法输入3");
            }
            if (minDFScode != null && minDFScode.getEdgeSeq() != null && !minDFScode.getEdgeSeq().isEmpty()) {
                Set<Integer> lELB_GEN = new HashSet<>(minDFScode.getEdgeSeq().get(minDFScode.getEdgeSeq().size() - 1).getLabelB());
                // node B 's label of last edge in generated DFS code
                Set<Integer> lELB_DFS = new HashSet<>(this.mlDFSCode.getEdgeSeq().get(minDFScode.getEdgeSeq().size() - 1).getLabelB());
                // node B 's label of last edge in  DFS code needed justified
                lELB_DFS.removeAll(lELB_GEN);
                if (!lELB_DFS.isEmpty()) {
                    return false;
                    // 上一个节点 标签未扩展完全
                }
            }
            if (!minEdgeLabelA.equals(dfScodeEdgeLabelA)) {
                return false;
            }
            if (minEdge.getEdgeLabel() != dfScodeEdge.getEdgeLabel()) {
                return false;
            }
            return dfScodeEdge.getLabelB().getFirst().equals(minEdgeLabelB);
        }
    }

/*    *//**
     * 判断两个给定的MLGSpanEdge 的大小
     * 除了 labelB之外 其他元素都应该相同
     * labelB 应该只有一个
     *
     * @param edgeAPair
     * @param edgeBPair
     * @return
     *//*
    private int compare(Pair<Boolean, MLGSpanEdge> edgeAPair, Pair<Boolean, MLGSpanEdge> edgeBPair) throws Exception {
        int addLabelA = edgeAPair.getKey() ? 0 : 1;
        int addLabelB = edgeBPair.getKey() ? 0 : 1;
        if (addLabelA != addLabelB) {
            return addLabelA - addLabelB;
        }
        MLGSpanEdge edgeA = edgeAPair.getValue();
        MLGSpanEdge edgeB = edgeBPair.getValue();
        if (edgeA.getNodeA() != edgeB.getNodeA()) {
            return edgeA.getNodeA() - edgeB.getNodeA();
        }
        if (edgeA.getNodeB() != edgeB.getNodeB()) {
            return edgeA.getNodeB() - edgeB.getNodeB();
        }
        if (edgeA.getDirection() != edgeB.getDirection()) {
            return edgeA.getDirection() - edgeB.getDirection();
        }
        Set<Integer> edgeALabelA = new HashSet<>(edgeA.getLabelA());
        Set<Integer> edgeBLabelA = new HashSet<>(edgeB.getLabelA());
        if (!edgeALabelA.equals(edgeBLabelA)
                || edgeA.getLabelB().size() != 1
                || edgeB.getLabelB().size() != 1) {
            throw new Exception("非法参数");
        }
        if (edgeA.getEdgeLabel() != edgeB.getEdgeLabel()) {
            return edgeA.getEdgeLabel() - edgeB.getEdgeLabel();
        }
        return (int) edgeA.getLabelB().getFirst() - (int) edgeB.getLabelB().getFirst();
    }*/
}