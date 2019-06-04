package top.ericcliu.util;

import java.util.*;

/**
 * @author liubi
 * @date 2019-05-22 09:29
 **/
public class NaryMDCJustifier {
    private MultiLabelGraph dFSCodeGraph;
    private DFScode dFScode;

    public NaryMDCJustifier(DFScode dfScode) throws Exception {
        this.dFScode = dfScode;
        this.dFSCodeGraph = new MultiLabelGraph(this.dFScode);
    }

    public boolean justify() throws Exception {
        int edgeIndex = 0;
        // 标记待判断的DFScode边 id
        int maxTurn = this.dFScode.getEdgeSeq().size();
        DFScode minDFScode = new DFScode();
        DFScodeInstance minDFSCodeInstance = null;
        // 选取最小边
        GSpanEdge minEdge = null;
        Iterator<Map<DFScode, DFScodeInstance>> mapIt = this.dFSCodeGraph.getGraphEdge().values().iterator();
        while (mapIt.hasNext()) {
            for (Map.Entry<DFScode, DFScodeInstance> entry : mapIt.next().entrySet()) {
                ArrayList<GSpanEdge> edgeSeq = entry.getKey().getEdgeSeq();
                DFScodeInstance currentInstance = entry.getValue();
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
            Map<GSpanEdge, DFScodeInstance> childrenEdgeInstanceMap = new HashMap<>(childrenEdge.size());
            Iterator<GSpanEdge> edgeIt = childrenEdge.iterator();
            while (edgeIt.hasNext()) {
                GSpanEdge childEdge = edgeIt.next();
                DFScodeInstance childInstace = subGraphIsomorphism(minDFScode, minDFSCodeInstance, childEdge);
                childrenEdgeInstanceMap.put(childEdge,childInstace);
            }
            minEdge = null;
            for(Map.Entry<GSpanEdge, DFScodeInstance> entry : childrenEdgeInstanceMap.entrySet()){
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

    private DFScodeInstance subGraphIsomorphism(DFScode parent, DFScodeInstance parentInstances, GSpanEdge childernEdge) throws Exception {
        // 假设 parent 和  childernEdge 能够组成合法的childDFScode， 合法性检查已经完成
        DFScodeInstance childInstance = new DFScodeInstance();
        DFScode child = new DFScode(parent).addEdge(childernEdge);
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
        LinkedList<Integer> rightMostPath = parent.fetchRightMostPath();
        if (rightMostPath.size() == 0) {
            throw new Exception("right most path size is 0 or 1, ERROR");
        }
        // forward extend
        Iterator<Integer> descRMPit = rightMostPath.descendingIterator();
        while (descRMPit.hasNext()) {
            Integer nodeInRMP = descRMPit.next();
            Integer nodeInRMPLabel = parent.fetchNodeLabel(nodeInRMP);
            Set<DFScode> possibleChildren = new HashSet<>();
            for (Map<DFScode, DFScodeInstance> map : this.dFSCodeGraph.getGraphEdge().row(nodeInRMPLabel).values()) {
                possibleChildren.addAll(map.keySet());
            }
            for (DFScode possibleChild : possibleChildren) {
                if (possibleChild.getEdgeSeq().size() != 1) {
                    throw new Exception("wrong edge");
                }
                int nodeId2 = parent.getMaxNodeId() + 1;
                int nodeLabel2 = possibleChild.getEdgeSeq().get(0).getLabelB();
                int edgeLabel = possibleChild.getEdgeSeq().get(0).getEdgeLabel();
                GSpanEdge possibleEdge = new GSpanEdge(nodeInRMP, nodeId2, nodeInRMPLabel, nodeLabel2, edgeLabel, 0);
                childrenEdge.add(possibleEdge);
            }
        }
        return childrenEdge;
    }
}
