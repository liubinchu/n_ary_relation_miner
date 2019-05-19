package top.ericcliu;

import top.ericcliu.util.*;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author liubi
 * @date 2019-01-19 21:08
 **/
public class GSpanMiner {
    private MultiLabelGraph dataGraph;
    private Double threshold;
    private Integer MNIThreshold;// >= 该阈值 则认为频繁
    private LinkedList<DFScode> result;

    //private LinkedList<Integer> stepRecorder ;

    public MultiLabelGraph getDataGraph() {
        return dataGraph;
    }

    public void setDataGraph(MultiLabelGraph dataGraph) {
        this.dataGraph = dataGraph;
    }

    public GSpanMiner(MultiLabelGraph dataGraph, double thresh) throws Exception {
        this.result = new LinkedList<>();
        this.dataGraph = dataGraph;
        this.threshold = thresh;
        int num = this.dataGraph.getTypeRelatedNum();
        this.MNIThreshold = ((Double) (threshold * this.dataGraph.getTypeRelatedNum())).intValue();
        if (this.MNIThreshold < 2) {
            this.MNIThreshold = 2;
        }
        //清洗不频繁的边
        for (Integer labelA : this.dataGraph.getGraphEdge().rowKeySet()) {
            for (Integer labelB : this.dataGraph.getGraphEdge().columnKeySet()) {
                Map<DFScode, DFScodeInstance> map = this.dataGraph.getGraphEdge().get(labelA, labelB);
                if (map != null) {
                    Boolean changed = false;
                    Iterator<Entry<DFScode, DFScodeInstance>> iterator = map.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Entry<DFScode, DFScodeInstance> entry = iterator.next();
                        if (entry.getValue().calMNI() < this.MNIThreshold) {
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
            int rightMostNodelabel = parent.getNodeLabel(rightMostNode);
            while (rightMostPathIt.hasNext()) {
                int node2 = rightMostPathIt.next();
                int label2 = parent.getNodeLabel(node2);
                Set<DFScode> possibleChildren = new HashSet<>();
                Map<DFScode, DFScodeInstance> map1 = this.dataGraph.getGraphEdge().get(rightMostNodelabel, label2);
                Map<DFScode, DFScodeInstance> map2 = this.dataGraph.getGraphEdge().get(label2, rightMostNodelabel);
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
            Integer nodeInRMPLabel = parent.getNodeLabel(nodeInRMP);
            Set<DFScode> possibleChildren = new HashSet<>();
            for (Map<DFScode, DFScodeInstance> map : this.dataGraph.getGraphEdge().row(nodeInRMPLabel).values()) {
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
            for (Map<DFScode, DFScodeInstance> map : this.dataGraph.getGraphEdge().column(nodeInRMPLabel).values()) {
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


    private DFScodeInstance subGraphIsomorphism(DFScode parent, DFScodeInstance parentInstances, GSpanEdge childernEdge) throws Exception {
        // 假设 parent 和  childernEdge 能够组成合法的childDFScode， 合法性检查已经完成
        DFScodeInstance childInstance = new DFScodeInstance();
        DFScode child = ((DFScode) parent.clone()).addEdge(childernEdge);
        int nodeA = childernEdge.getNodeA();
        int nodeB = childernEdge.getNodeB();
        int labelB = childernEdge.getLabelB();
        int edgeLabel = childernEdge.getEdgeLabel();
        if (nodeA < nodeB) {
            // forward edge
            Map<Integer, Integer> nodeAIdsDSMap = parentInstances.fetchInstanceNode(nodeA);
            // key : instance id , value : node id in data set
            Collection<Integer> possNodeBIds = this.dataGraph.getLabelNodes().get(labelB);
            // possible node B id in data set
            for (Entry<Integer, Integer> nodeAIdDSMap : nodeAIdsDSMap.entrySet()) {
                Integer instanceId = nodeAIdDSMap.getKey();
                Set<Integer> nodes = new HashSet<>();
                for (Integer node : parentInstances.getInstances().get(instanceId)) {
                    nodes.add(node);
                }
                Integer nodeAIdDS = nodeAIdDSMap.getValue();
                for (Integer possNodeBId : possNodeBIds) {
                    if (nodes.contains(possNodeBId)) {
                        continue;
                    }
                    if (!this.dataGraph.getValueGraph().hasEdgeConnecting(nodeAIdDS, possNodeBId)) {
                        continue;
                    }
                    Integer edgeValue = ((Integer) this.dataGraph.getValueGraph().edgeValue(nodeAIdDS, possNodeBId).get());
                    if (!edgeValue.equals(edgeLabel)) {
                        continue;
                    }
                    int newLength = parentInstances.getInstances().get(instanceId).length + 1;
                    int[] newInstance = Arrays.copyOf(parentInstances.getInstances().get(instanceId), newLength);
                    newInstance[newLength - 1] = possNodeBId;
                    childInstance.addInstance(child, newInstance);
                }
            }
        } else {
            // backward edge
            for (int instanceId = 0; instanceId < parentInstances.getInstances().size(); instanceId++) {
                Integer nodeAIdsDS = parentInstances.getInstances().get(instanceId)[nodeA];
                Integer nodeBIdsDS = parentInstances.getInstances().get(instanceId)[nodeB];
                boolean correctEdge = this.dataGraph.getValueGraph().hasEdgeConnecting(nodeAIdsDS, nodeBIdsDS);
                correctEdge = (correctEdge == false ? false : ((Integer) this.dataGraph.getValueGraph().edgeValue(nodeAIdsDS, nodeBIdsDS).get()).equals(edgeLabel));
                // Guava Value Graph 不允许 两个节点之间存在多条边， 在KB 中 存在这种情况 暂时不考虑
                // 目前只考虑 两个节点之间只存在一条边
                if (correctEdge) {
                    int[] nodeInstanceMap = parentInstances.getInstances().get(instanceId);
                    childInstance.addInstance(child, nodeInstanceMap);
                }
            }
        }
        return childInstance;
    }

    private double calRelatedRatio(Integer MNI, DFScode childDFScode) throws Exception {
        Double relatedRatio = 0d;
        for (GSpanEdge edge : childDFScode.getEdgeSeq()) {
            Map<DFScode, DFScodeInstance> map = this.getDataGraph().getGraphEdge().get(edge.getLabelA(), edge.getLabelB());
            DFScode dfScode = null;
            if (map == null) {
                map = this.getDataGraph().getGraphEdge().get(edge.getLabelB(), edge.getLabelA());
                dfScode = new DFScode(new GSpanEdge(0, 1, edge.getLabelB(), edge.getLabelA(), edge.getEdgeLabel(), edge.getDirection()));
            }
            if (dfScode == null) {
                dfScode = new DFScode(new GSpanEdge(0, 1, edge.getLabelA(), edge.getLabelB(), edge.getEdgeLabel(), edge.getDirection()));
            }
            relatedRatio += map.get(dfScode).calMNI();
        }
        relatedRatio = MNI * childDFScode.getEdgeSeq().size() / relatedRatio;
        System.out.println("relatedRatio" + relatedRatio);
        return relatedRatio;
    }


    private void gspanCore(DFScode parent, DFScodeInstance parentInstances) throws Exception {
        ArrayList<GSpanEdge> childrenEdges = rightMostPathExtension(parent);
        for (GSpanEdge childEdge : childrenEdges) {
            DFScode childDFScode = ((DFScode) parent.clone()).addEdge(childEdge);
            // 最小DFScode剪枝
            boolean isCannoical = new MinDFSCodeJustifier(childDFScode).justify();
            if (isCannoical) {
                //频繁度剪枝
                DFScodeInstance childInstance = subGraphIsomorphism(parent, parentInstances, childEdge);
                Integer MNI = childInstance.calMNI();
                if (MNI >= this.MNIThreshold) {
                    //相关度剪枝
                    double relatedRatio = calRelatedRatio(MNI, childDFScode);
                    if (relatedRatio > 0.5) {
                        this.result.add(childDFScode);
                        File dir = new File(this.getDataGraph().graphName + "MNI_" + threshold);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        childDFScode.saveToFile(this.getDataGraph().graphName + "MNI_" + threshold + File.separator + "RE_" + this.getDataGraph().graphName + "MNI_" + threshold + "Id_" + this.result.size() + ".json", false);
                        System.out.println("instance num:" + childInstance.getInstances().size());
                        childInstance.sample(1, 10, 10).saveToFile(this.getDataGraph().graphName + "MNI_" + threshold + File.separator + "IN_" + this.getDataGraph().graphName + "MNI_" + threshold + "Id_" + this.result.size() + ".json", false);
                        gspanCore(childDFScode, childInstance);
                    }
                }
                childInstance = null;
                System.gc();
            }
        }
    }

    public void mineGSpan() throws Exception {
        Iterator<Map<DFScode, DFScodeInstance>> iterator = this.getDataGraph().getGraphEdge().values().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Map<DFScode, DFScodeInstance> map = iterator.next();
            for (Entry<DFScode, DFScodeInstance> entry : map.entrySet()) {
/*                if (this.getDataGraph().getTypeId().equals(entry.getKey().getNodeLabel(0)) ||
                        this.getDataGraph().getTypeId().equals(entry.getKey().getNodeLabel(1))) {*/
                if (entry.getKey().getNodeLabel(0).equals(Integer.MIN_VALUE) ||
                        entry.getKey().getNodeLabel(1).equals(Integer.MIN_VALUE)) {
                    // 仅从当前 typeId 作为根节点 出发 拓展
                    gspanCore(entry.getKey(), entry.getValue());
                    System.out.println("finish the " + (i++) + "nd edge");
                }
            }
        }
        //System.out.println("result" + this.result);
    }

    public void mineNAryRelation() throws Exception {
        Iterator<Map<DFScode, DFScodeInstance>> iterator = this.getDataGraph().getGraphEdge().values().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Map<DFScode, DFScodeInstance> map = iterator.next();
            for (Entry<DFScode, DFScodeInstance> entry : map.entrySet()) {
                //this.stepRecorder = new LinkedList<>();
                //this.stepRecorder.add(i++);
                gspanCore(entry.getKey(), entry.getValue());
                System.out.println("finish the " + (i++) + "nd edge");
            }
        }
        //System.out.println("result" + this.result);
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
            System.out.println("    MNISupportThreshold" + miner.MNIThreshold);
            miner.mineGSpan();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}


/*
        MultiLabelGraph big = new MultiLabelGraph(false);


        //GSpanMiner gSpanMinerbig = new GSpanMiner(big, 0.001);
        //gSpanMinerbig.mine();

        GSpanMiner gSpanMinerFromFile = new GSpanMiner(graphFromFile, 0.01);
        gSpanMinerFromFile.mine();*/
/*        GSpanMiner gSpanMinersmall = new GSpanMiner(small, 0.001);
        gSpanMinersmall.mine();*/

