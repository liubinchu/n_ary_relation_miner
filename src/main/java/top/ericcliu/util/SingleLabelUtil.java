package top.ericcliu.util;

import java.io.File;
import java.util.*;

/**
 * @author liubi
 * @date 2019-06-04 15:30
 **/
public class SingleLabelUtil {

    /**
     * 模式拓展原则，在gSpan最右拓展的基础上，进行修改 加上针对多元关系模式扩展的限制
     * @param parent
     * @return
     * @throws Exception
     */
    public static ArrayList<GSpanEdge> nAryRelationExtension(DFScode parent, MultiLabelGraph dataGraph, int maxDepth) throws Exception {
        ArrayList<GSpanEdge> childrenEdge = new ArrayList<>();
        LinkedList<Integer> rightMostPath = parent.fetchRightMostPath();
        if (rightMostPath.size() == 0 || rightMostPath.size() == 1) {
            throw new Exception("right most path size is 0 or 1, ERROR");
        }
        if (maxDepth < 1) {
            throw new Exception("maxDepth must > 0, ERROR");
        }
        int maxSizeRMP = maxDepth + 1;
        boolean extendOnNode = true;
        if (rightMostPath.size() >= maxSizeRMP) {
            // 超过最大深度,则跳过最右节点
            extendOnNode = false;
        }
        // forward extend
        Iterator<Integer> descRMPit = rightMostPath.descendingIterator();
        while (descRMPit.hasNext()) {
            Integer nodeInRMP = descRMPit.next();
            Integer nodeInRMPLabel = parent.fetchNodeLabel(nodeInRMP);
            if (!extendOnNode) {
                // 跳过最右节点
                extendOnNode = true;
                continue;
            }
            Set<DFScode> possibleChildren = new HashSet<>();
            for (Map<DFScode, DFScodeInstance> map : dataGraph.getGraphEdge().row(nodeInRMPLabel).values()) {
                possibleChildren.addAll(map.keySet());
            }
            for (DFScode possibleChild : possibleChildren) {
                if (possibleChild.getEdgeSeq().size() != 1) {
                    throw new Exception("wrong edge");
                }
                int node2 = parent.getMaxNodeId() + 1;
                int nodeLabel2 = possibleChild.getEdgeSeq().get(0).getLabelB();
                int edgeLabel = possibleChild.getEdgeSeq().get(0).getEdgeLabel();
                childrenEdge.add(new GSpanEdge(nodeInRMP, node2, nodeInRMPLabel, nodeLabel2, edgeLabel, 0));
            }
        }
        return childrenEdge;
    }

    /**
     * 模式拓展原则， 先从最右节点后向拓展，距离根节点近的节点优先
     * 再 在最右路径上，前向拓展，距离根节点远的节点优先
     *
     * @param parent
     * @return
     * @throws Exception
     */
    public static ArrayList<GSpanEdge> rightMostPathExtension(DFScode parent, MultiLabelGraph dataGraph) throws Exception {
        ArrayList<GSpanEdge> childrenEdge = new ArrayList<>();
        LinkedList<Integer> rightMostPath = parent.fetchRightMostPath();
        Integer rightMostNode = rightMostPath.getLast();
        if (rightMostPath.size() == 0 || rightMostPath.size() == 1) {
            throw new Exception("right most path size is 0 or 1, ERROR");
        } else if (rightMostPath.size() > 2) {
            // backward extend, 最后1个节点，无需和最右节点组成边，也即最右节点 不允许和最右节点组成后向边，构成self looped edge
            rightMostPath.removeLast();
            int tempNode = rightMostPath.removeLast();
            // 最后两个节点不参与后向拓展
            ListIterator<Integer> rightMostPathIt = rightMostPath.listIterator();
            int rightMostNodelabel = parent.fetchNodeLabel(rightMostNode);
            while (rightMostPathIt.hasNext()) {
                int node2 = rightMostPathIt.next();
                int label2 = parent.fetchNodeLabel(node2);
                Set<DFScode> possibleChildren = new HashSet<>();
                if (dataGraph.getGraphEdge().get(rightMostNodelabel, label2) != null) {
                    possibleChildren.addAll(dataGraph.getGraphEdge().get(rightMostNodelabel, label2).keySet());
                }
                if (dataGraph.getGraphEdge().get(label2, rightMostNodelabel) != null) {
                    possibleChildren.addAll(dataGraph.getGraphEdge().get(label2, rightMostNodelabel).keySet());
                }
                for (DFScode possibleChild : possibleChildren) {
                    if (possibleChild.getEdgeSeq().size() != 1) {
                        throw new Exception("wrong edge");
                    }
                    GSpanEdge possibleEdge = new GSpanEdge(rightMostNode, node2, rightMostNodelabel, label2, possibleChild.getEdgeSeq().get(0).getEdgeLabel(), 1);
                    GSpanEdge possibleEdgeReverse = new GSpanEdge(node2, rightMostNode, label2, rightMostNodelabel, possibleChild.getEdgeSeq().get(0).getEdgeLabel(), 1);
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
            Integer nodeInRMPLabel = parent.fetchNodeLabel(nodeInRMP);
            Set<DFScode> possibleChildren = new HashSet<>();
            for (Map<DFScode, DFScodeInstance> map : dataGraph.getGraphEdge().row(nodeInRMPLabel).values()) {
                possibleChildren.addAll(map.keySet());
            }
            for (DFScode possibleChild : possibleChildren) {
                if (possibleChild.getEdgeSeq().size() != 1) {
                    throw new Exception("wrong edge");
                }
                int node2 = parent.getMaxNodeId() + 1;
                int nodeLabel2 = possibleChild.getEdgeSeq().get(0).getLabelB();
                int edgeLabel = possibleChild.getEdgeSeq().get(0).getEdgeLabel();
                GSpanEdge possibleEdge = new GSpanEdge(nodeInRMP, node2, nodeInRMPLabel, nodeLabel2, edgeLabel, 1);
                childrenEdge.add(possibleEdge);
            }
            possibleChildren = new HashSet<>();
            for (Map<DFScode, DFScodeInstance> map : dataGraph.getGraphEdge().column(nodeInRMPLabel).values()) {
                possibleChildren.addAll(map.keySet());
            }
            for (DFScode possibleChild : possibleChildren) {
                if (possibleChild.getEdgeSeq().size() != 1) {
                    throw new Exception("wrong edge");
                }
                int node2 = parent.getMaxNodeId() + 1;
                int nodeLabel2 = possibleChild.getEdgeSeq().get(0).getLabelA();
                int edgeLabel = possibleChild.getEdgeSeq().get(0).getEdgeLabel();
                GSpanEdge possibleEdge = new GSpanEdge(nodeInRMP, node2, nodeInRMPLabel, nodeLabel2, edgeLabel, 1);
                childrenEdge.add(possibleEdge);
            }
        }
        return childrenEdge;
    }

    public static DFScodeInstance subGraphIsomorphism(DFScode parent, DFScodeInstance parentInstances, GSpanEdge childernEdge,
                                                boolean backwardExtend, MultiLabelGraph dataGraph) throws Exception {
        // 假设 parent 和  childernEdge 能够组成合法的childDFScode， 合法性检查已经完成
        DFScodeInstance childInstance = new DFScodeInstance();
        DFScode child = new DFScode(parent).addEdge(childernEdge);
        int nodeA = childernEdge.getNodeA();
        int nodeB = childernEdge.getNodeB();
        int labelB = childernEdge.getLabelB();
        int edgeLabel = childernEdge.getEdgeLabel();
        if (nodeA < nodeB) {
            // forward edge
            Map<Integer, Integer> nodeAIdsDSMap = parentInstances.fetchInstanceNode(nodeA);
            // key : instance id , value : node id in data set
            Collection<Integer> possNodeBIds = dataGraph.getLabelNodes().get(labelB);
            // possible node B id in data set
            for (Map.Entry<Integer, Integer> nodeAIdDSMap : nodeAIdsDSMap.entrySet()) {
                Integer instanceId = nodeAIdDSMap.getKey();
                Set<Integer> appearedNodes = new HashSet<>();
                for (Integer node : parentInstances.getInstances().get(instanceId)) {
                    appearedNodes.add(node);
                }
                Integer nodeAIdDS = nodeAIdDSMap.getValue();
                for (Integer possNodeBId : possNodeBIds) {
                    if (appearedNodes.contains(possNodeBId)) {
                        continue;
                    }
                    if (!dataGraph.getValueGraph().hasEdgeConnecting(nodeAIdDS, possNodeBId)) {
                        continue;
                    }
                    Integer edgeValue = ((Integer) dataGraph.getValueGraph().edgeValue(nodeAIdDS, possNodeBId).get());
                    if (!edgeValue.equals(edgeLabel)) {
                        continue;
                    }
                    int newLength = parentInstances.getInstances().get(instanceId).length + 1;
                    int[] newInstance = Arrays.copyOf(parentInstances.getInstances().get(instanceId), newLength);
                    newInstance[newLength - 1] = possNodeBId;
                    childInstance.addInstance(child, newInstance);
                }
            }
        } else if (nodeA>nodeB && backwardExtend) {
            // backward edge
            for (int instanceId = 0; instanceId < parentInstances.getInstances().size(); instanceId++) {
                Integer nodeAIdsDS = parentInstances.getInstances().get(instanceId)[nodeA];
                Integer nodeBIdsDS = parentInstances.getInstances().get(instanceId)[nodeB];
                boolean correctEdge = dataGraph.getValueGraph().hasEdgeConnecting(nodeAIdsDS, nodeBIdsDS);
                correctEdge = (correctEdge == false ? false : ((Integer) dataGraph.getValueGraph().edgeValue(nodeAIdsDS, nodeBIdsDS).get()).equals(edgeLabel));
                // Guava Value Graph 不允许 两个节点之间存在多条边， 在KB 中 存在这种情况 暂时不考虑
                // 目前只考虑 两个节点之间只存在一条边
                if (correctEdge) {
                    int[] nodeInstanceMap = parentInstances.getInstances().get(instanceId);
                    childInstance.addInstance(child, nodeInstanceMap);
                }
            }
        }
        else {
            throw new Exception("appear backward edge, but only forward extend is allowed");
        }
        return childInstance;
    }

    public static void savePattern(DFScode childDFScode, DFScodeInstance childInstance,
                             int maxDepth, double threshold, double relatedRatio,
                             int resultIndex,MultiLabelGraph dataGraph) throws Exception {
        String dirPath = "result_Thresh_"+threshold+"D_"+maxDepth+"related_ratio_"+relatedRatio+
                File.separator + dataGraph.graphName + "MNI_" + threshold;
        String fileName = dataGraph.graphName + "MNI_" + threshold + "Id_" + (resultIndex) + ".json";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        childDFScode.saveToFile(dirPath + File.separator + "RE_" + fileName, false);
        childInstance.sample(1, 10, 10).
                saveToFile(dirPath + File.separator + "IN_" + fileName, false);
    }

    public static double calRelatedRatio(DFScode dFScode ,MultiLabelGraph dataGraph) throws Exception {
        double relatedRatio = 0d;
        for (GSpanEdge edge : dFScode.getEdgeSeq()) {
            Map<DFScode, DFScodeInstance> map = dataGraph.getGraphEdge().get(edge.getLabelA(), edge.getLabelB());
            DFScode dfScodeEdge = new DFScode(new GSpanEdge(0, 1, edge.getLabelA(),
                    edge.getLabelB(), edge.getEdgeLabel(), edge.getDirection()));
            relatedRatio += map.get(dfScodeEdge).calMNI();
        }
        relatedRatio = dFScode.getMNI() * dFScode.getEdgeSeq().size() / relatedRatio;
        return relatedRatio;
    }
}
