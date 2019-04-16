package top.ericcliu.util;

import java.util.*;

/**
 * @author liubi
 * @date 2019-02-17 10:12
 * 用以 判断一个DFScode 是否是 最小DFScode
 * 是一个受限版本的gSpan
 **/
public class MinDFSCodeJustifier {
    private MultiLabelGraph dFSCodeGraph;
    private DFScode dFScode;

    public MinDFSCodeJustifier(DFScode dfScode) throws Exception {
        this.dFScode = dfScode;
        this.dFSCodeGraph = new MultiLabelGraph(this.dFScode);
    }

    public boolean justify() throws Exception {
        Integer edgeIndex = 0;
        // 标记待判断的DFScode边 id
        Integer maxTurn = this.dFScode.getEdgeSeq().size();
        DFScode minDFScode = new DFScode();
        EdgeInstance minDFSCodeInstance = null;

        // 选取最小边
        GSpanEdge minEdge = null;
        Iterator<Map<DFScode, EdgeInstance>> mapIt = this.dFSCodeGraph.getGraphEdge().values().iterator();
        while (mapIt.hasNext()) {
            for (Map.Entry<DFScode, EdgeInstance> entry : mapIt.next().entrySet()) {
                ArrayList<GSpanEdge> edgeSeq = entry.getKey().getEdgeSeq();
                EdgeInstance currentInstance = entry.getValue();
                GSpanEdge currentEdge = null;
                if (edgeSeq.size() != 1) {
                    throw new Exception("dFSCodeGraph 初始化 存在问题");
                } else {
                    currentEdge = edgeSeq.get(0);
                }
                if (minEdge == null || minEdge.compareTo(currentEdge) > 0) {
                    minEdge = currentEdge;
                    minDFSCodeInstance = currentInstance;
                }
            }
        }
        if (minEdge.compareTo(dFScode.getEdgeSeq().get(edgeIndex++)) < 0) {
            // 生成的DFScode 更小， 给定dfs code不是最小DFScode
            return false;
        } else {
            minDFScode.addEdge(minEdge);
        }
        while (edgeIndex < maxTurn) {
            //对最小边进行最右拓展
            ArrayList<GSpanEdge> childrenEdge = rightMostPathExtension(minDFScode);
            Map<GSpanEdge,EdgeInstance> childrenEdgeInstanceMap = new HashMap<>(childrenEdge.size());
            minEdge = null;
            Iterator<GSpanEdge> edgeIt = childrenEdge.iterator();
            while (edgeIt.hasNext()) {
                GSpanEdge childEdge = edgeIt.next();
                EdgeInstance childInstace = subGraphIsomorphism(minDFScode, minDFSCodeInstance, childEdge);
                childrenEdgeInstanceMap.put(childEdge,childInstace);
            }
            for(Map.Entry<GSpanEdge,EdgeInstance> entry : childrenEdgeInstanceMap.entrySet()){
                if (entry.getValue().calMNI() > 0) {
                    if (minEdge == null || minEdge.compareTo(entry.getKey()) > 0) {
                        minEdge = entry.getKey();
                        minDFSCodeInstance = entry.getValue();
                    }
                }
            }
            if (minEdge == null) {
                //Integer childrenSize = childrenEdge.size();
                System.err.println("childrenEdge size == 0, or all childInstace.getMNI() < 0, no valid childrenEdge");
                return false ;
                //  应该不会出现这种情况 bug 待解决
                //throw new Exception("childrenEdge size == 0, or all childInstace.getMNI() < 0, no valid childrenEdge");
            } else if (minEdge.compareTo(dFScode.getEdgeSeq().get(edgeIndex++)) < 0) {
                // 生成的DFScode 更小， 给定dfs code不是最小DFScode
                return false;
            }
            minDFScode.addEdge(minEdge);
        }
        return true;
    }

    private EdgeInstance subGraphIsomorphism(DFScode parent, EdgeInstance parentInstances, GSpanEdge childernEdge) throws Exception {
        // 假设 parent 和  childernEdge 能够组成合法的childDFScode， 合法性检查已经完成
        EdgeInstance childInstance = new EdgeInstance();
        DFScode child = ((DFScode) parent.clone()).addEdge(childernEdge);
        int nodeA = childernEdge.getNodeA();
        int nodeB = childernEdge.getNodeB();
        int labelB = childernEdge.getLabelB();
        int edgeLabel = childernEdge.getEdgeLabel();
        //Map<Integer, Integer> nodeAIdsDSMap = parentInstances.getInstances().column(nodeA);
        // key : instance id , value : node id in data set
        if (nodeA < nodeB) {
            // forward edge
            Map<Integer, Integer> nodeAIdsDSMap = parentInstances.fetchInstanceNode(nodeA);
            Collection<Integer> possNodeBIds = this.dFSCodeGraph.getLabelNodes().get(labelB);
            // possible node B id in data set
            for (Map.Entry<Integer, Integer> nodeAIdDSMap : nodeAIdsDSMap.entrySet()) {
                Integer instanceId = nodeAIdDSMap.getKey();
                Set<Integer> nodes = new HashSet<>();
                for(Integer node : parentInstances.getInstances().get(instanceId)){
                    nodes.add(node);
                }
                Integer nodeAIdDS = nodeAIdDSMap.getValue();
                for (Integer possNodeBId : possNodeBIds) {
                    if (nodes.contains(possNodeBId)) {
                        continue;
                    }
                    if (!this.dFSCodeGraph.getValueGraph().hasEdgeConnecting(nodeAIdDS, possNodeBId)) {
                        continue;
                    }
                    Integer edgeValue = ((Integer) this.dFSCodeGraph.getValueGraph().edgeValue(nodeAIdDS, possNodeBId).get());
                    if (!edgeValue.equals(edgeLabel)) {
                        continue;
                    }
                    int newLength = parentInstances.getInstances().get(instanceId).length+1;
                    int[] newInstance = Arrays.copyOf(parentInstances.getInstances().get(instanceId), newLength);
                    newInstance[newLength-1] = possNodeBId;
                    childInstance.addInstance(child, newInstance);
                }
            }
        } else {
            // backward edge
            //Set<Integer> instanceIds = parentInstances.getInstances().rowKeySet();
            for(int instanceId=0;  instanceId<parentInstances.getInstances().size(); instanceId++){
                Integer nodeAIdsDS = parentInstances.getInstances().get(instanceId)[nodeA];
                Integer nodeBIdsDS = parentInstances.getInstances().get(instanceId)[nodeB];
                boolean correctEdge = this.dFSCodeGraph.getValueGraph().hasEdgeConnecting(nodeAIdsDS, nodeBIdsDS);
                correctEdge = (correctEdge == false ? false : ((Integer) this.dFSCodeGraph.getValueGraph().edgeValue(nodeAIdsDS, nodeBIdsDS).get()).equals(edgeLabel));
                // Guava Value Graph 不允许 两个节点之间存在多条边， 在KB 中 存在这种情况 暂时不考虑
                // 目前只考虑 两个节点之间只存在一条边
                if (correctEdge) {
                    int [] nodeInstanceMap = parentInstances.getInstances().get(instanceId);
                    childInstance.addInstance(child, nodeInstanceMap);
                }
            }
        }
        return childInstance;
    }

    /**
     * 模式拓展原则， 先从最右节点后向拓展，距离根节点近的节点优先
     * 再 在最右路径上，前向拓展，距离根节点远的节点优先
     *
     * @param parent
     * @return
     * @throws Exception
     */
    public ArrayList<GSpanEdge> rightMostPathExtension(DFScode parent) throws Exception {
        ArrayList<GSpanEdge> childrenEdge = new ArrayList<>();
        LinkedList<Integer> rightMostPath = parent.getRightMostPath();
        Integer rightMostNode = rightMostPath.getLast();
        if (rightMostPath.size() == 0) {
            throw new Exception("right most path size is 0 or 1, ERROR");
        } else if (rightMostPath.size() > 2) {
            // backward extend, 最后1个节点，无需和最右节点组成边，也即最右节点 不允许和最右节点组成后向边，构成self looped edge
            rightMostPath.removeLast();
            int tempNode = rightMostPath.removeLast();
            // 最后两个节点不参与后向拓展
            ListIterator<Integer> rightMostPathIt = rightMostPath.listIterator();
            int rightMostNodeLabel = parent.getNodeLabel(rightMostNode);
            while (rightMostPathIt.hasNext()) {
                int node2 = rightMostPathIt.next();
                int label2 = parent.getNodeLabel(node2);
                Set<DFScode> possibleChildren = new HashSet<>();
                Map<DFScode, EdgeInstance> map1 = this.dFSCodeGraph.getGraphEdge().get(rightMostNodeLabel, label2);
                Map<DFScode, EdgeInstance> map2 = this.dFSCodeGraph.getGraphEdge().get(label2, rightMostNodeLabel);
                if (map1 != null) {
                    possibleChildren.addAll(map1.keySet());
                }
                if (map2 != null) {
                    possibleChildren.addAll(map2.keySet());
                }
                for (DFScode possibleChild : possibleChildren) {
                    if (possibleChild.getEdgeSeq().size() != 1) {
                        throw new Exception("wrong edge");
                    }
                    GSpanEdge possibleEdge = new GSpanEdge(rightMostNode, node2, rightMostNodeLabel, label2, possibleChild.getEdgeSeq().get(0).getEdgeLabel(), 1);
                    GSpanEdge possibleEdgeReverse = new GSpanEdge(node2, rightMostNode, label2, rightMostNodeLabel, possibleChild.getEdgeSeq().get(0).getEdgeLabel(), 1);
                    if (!parent.getEdgeSeq().contains(possibleEdge) && !parent.getEdgeSeq().contains(possibleEdgeReverse)) {
                        childrenEdge.add(possibleEdge);
                    }
                }
            }
            rightMostPath.addLast(tempNode);
            rightMostPath.addLast(rightMostNode);
            // 最后两个节点不参与后向拓展
        }
        // forward extend
        Iterator<Integer> descRMPit = rightMostPath.descendingIterator();
        while (descRMPit.hasNext()) {
            Integer nodeInRMP = descRMPit.next();
            Integer nodeInRMPLabel = parent.getNodeLabel(nodeInRMP);
            Set<DFScode> possibleChildren = new HashSet<>();
            for (Map<DFScode, EdgeInstance> map : this.dFSCodeGraph.getGraphEdge().row(nodeInRMPLabel).values()) {
                possibleChildren.addAll(map.keySet());
            }
            for (DFScode possibleChild : possibleChildren) {
                if (possibleChild.getEdgeSeq().size() != 1) {
                    throw new Exception("wrong edge");
                }
                int nodeId2 = parent.getMaxNodeId() + 1;
                int nodeLabel2 = possibleChild.getEdgeSeq().get(0).getLabelB();
                int edgeLabel = possibleChild.getEdgeSeq().get(0).getEdgeLabel();
                GSpanEdge possibleEdge = new GSpanEdge(nodeInRMP, nodeId2, nodeInRMPLabel, nodeLabel2, edgeLabel, 1);
                childrenEdge.add(possibleEdge);
            }
            possibleChildren = new HashSet<>();
            for (Map<DFScode, EdgeInstance> map : this.dFSCodeGraph.getGraphEdge().column(nodeInRMPLabel).values()) {
                possibleChildren.addAll(map.keySet());
            }
            for (DFScode possibleChild : possibleChildren) {
                if (possibleChild.getEdgeSeq().size() != 1) {
                    throw new Exception("wrong edge");
                }
                int nodeId2 = parent.getMaxNodeId() + 1;
                int nodeLabel2 = possibleChild.getEdgeSeq().get(0).getLabelA();
                int edgeLabel = possibleChild.getEdgeSeq().get(0).getEdgeLabel();
                GSpanEdge possibleEdge = new GSpanEdge(nodeInRMP, nodeId2, nodeInRMPLabel, nodeLabel2, edgeLabel, 1);
                childrenEdge.add(possibleEdge);
            }
        }
        return childrenEdge;
    }
}
