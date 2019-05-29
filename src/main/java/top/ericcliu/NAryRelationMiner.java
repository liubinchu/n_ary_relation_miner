package top.ericcliu;

import top.ericcliu.util.*;

import java.io.File;
import java.util.*;

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
    private Integer support;
    /**
     * 模式扩展的最大深度 <= maxDepth
     */
    private int maxDepth;
    private Double relatedRatio;
    private int resultSize = 0;


    //private LinkedList<Integer> stepRecorder ;

    public MultiLabelGraph getDataGraph() {
        return dataGraph;
    }

    public void setDataGraph(MultiLabelGraph dataGraph) {
        this.dataGraph = dataGraph;
    }

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

    /**
     * 模式拓展原则，在gSpan最右拓展的基础上，进行修改 加上针对多元关系模式扩展的限制
     *
     * @param parent
     * @return
     * @throws Exception
     */
    private ArrayList<GSpanEdge> nAryRelationExtension(DFScode parent) throws Exception {
        ArrayList<GSpanEdge> childrenEdge = new ArrayList<>();
        LinkedList<Integer> rightMostPath = parent.getRightMostPath();
        if (rightMostPath.size() == 0 || rightMostPath.size() == 1) {
            throw new Exception("right most path size is 0 or 1, ERROR");
        }
        if (this.maxDepth < 1) {
            throw new Exception("maxDepth must > 0, ERROR");
        }
        int maxSizeRMP = this.maxDepth + 1;
        boolean extendOnNode = true;
        if (rightMostPath.size() >= maxSizeRMP) {
            // 超过最大深度,则跳过最右节点
            extendOnNode = false;
        }
        // forward extend
        Iterator<Integer> descRMPit = rightMostPath.descendingIterator();
        while (descRMPit.hasNext()) {
            Integer nodeInRMP = descRMPit.next();
            Integer nodeInRMPLabel = parent.getNodeLabel(nodeInRMP);
            if (!extendOnNode) {
                // 跳过最右节点
                extendOnNode = true;
                continue;
            }
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
                childrenEdge.add(new GSpanEdge(nodeInRMP, node2, nodeInRMPLabel, nodeLabel2, edgeLabel, 0));
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

        //forward edge
        Map<Integer, Integer> nodeAIdsDSMap = parentInstances.fetchInstanceNode(nodeA);
        // key : instance id , value : node id in data set
        Collection<Integer> possNodeBIds = this.dataGraph.getLabelNodes().get(labelB);
        // possible node B id in data set
        for (Map.Entry<Integer, Integer> nodeAIdDSMap : nodeAIdsDSMap.entrySet()) {
            int instanceId = nodeAIdDSMap.getKey();
            Set<Integer> appearedNodes = new HashSet<>();
            for (Integer node : parentInstances.getInstances().get(instanceId)) {
                appearedNodes.add(node);
            }
            Integer nodeAIdDS = nodeAIdDSMap.getValue();
            for (Integer possNodeBId : possNodeBIds) {
                if (appearedNodes.contains(possNodeBId)) {
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
        return childInstance;
    }

    private double calRelatedRatio(int MNI, DFScode dFScode) throws Exception {
        double relatedRatio = 0d;
        for (GSpanEdge edge : dFScode.getEdgeSeq()) {
            Map<DFScode, DFScodeInstance> map = this.getDataGraph().getGraphEdge().get(edge.getLabelA(), edge.getLabelB());
            DFScode dfScodeEdge = new DFScode(new GSpanEdge(0, 1, edge.getLabelA(), edge.getLabelB(), edge.getEdgeLabel(), edge.getDirection()));
            relatedRatio += map.get(dfScodeEdge).calMNI();
        }
        relatedRatio = MNI * dFScode.getEdgeSeq().size() / relatedRatio;
        System.out.println("relatedRatio" + relatedRatio);
        return relatedRatio;
    }

    private void savePattern(DFScode childDFScode, DFScodeInstance childInstance) throws Exception {
        File dir = new File(this.dataGraph.graphName + "MNI_" + threshold);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        childDFScode.setInstanceNum(childInstance.getInstances().size());
        childDFScode.saveToFile(this.dataGraph.graphName + "MNI_" + threshold + File.separator + "RE_" + this.dataGraph.graphName + "MNI_" + threshold + "Id_" + (++resultSize) + ".json", false);
        childInstance.sample(1, 10, 10).saveToFile(this.dataGraph.graphName + "MNI_" + threshold + File.separator + "IN_" + this.dataGraph.graphName + "MNI_" + threshold + "Id_" + resultSize + ".json", false);
    }

    private void mineCore(DFScode parent, DFScodeInstance parentInstances) throws Exception {
        ArrayList<GSpanEdge> childrenEdges = nAryRelationExtension(parent);
        for (GSpanEdge childEdge : childrenEdges) {
            DFScode childDFScode = ((DFScode) parent.clone()).addEdge(childEdge);
            if (!new NaryMDCJustifier(childDFScode).justify()) {
                // 最小DFS code剪枝
                continue;
            }
            DFScodeInstance childInstance = subGraphIsomorphism(parent, parentInstances, childEdge);
            int MNI = childInstance.calMNI();
            childDFScode.setMNI(MNI);
            if (MNI < this.support) {
                //频繁度剪枝
                if (childInstance.getInstances().size() >= this.support) {
                    // 如果 MNI 不频繁 但是 instance Num 频繁，需要输出模式 但是 不扩展
                    double relatedRatio = calRelatedRatio(MNI, childDFScode);
                    childDFScode.setRelatedRatio(relatedRatio);
                    savePattern(childDFScode, childInstance);
                }
                continue;
            }
            double relatedRatio = calRelatedRatio(MNI, childDFScode);
            if (relatedRatio < this.relatedRatio) {
                //相关度剪枝
                //continue;
            }
            childDFScode.setRelatedRatio(relatedRatio);
            savePattern(childDFScode, childInstance);
            mineCore(childDFScode, childInstance);
        }
    }

    public void mineNAryRelation() throws Exception {
        Iterator<Map<DFScode, DFScodeInstance>> iterator = this.getDataGraph().getGraphEdge().values().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Map<DFScode, DFScodeInstance> map = iterator.next();
            for (Map.Entry<DFScode, DFScodeInstance> entry : map.entrySet()) {
                if (entry.getKey().getNodeLabel(0).equals(this.dataGraph.getReplacedTypeId())) {
                    // 仅从当前 typeId 作为根节点 出发 拓展
                    mineCore(entry.getKey(), entry.getValue());
                    System.out.println("finish the " + (++i) + "nd edge");
                }
            }
        }
    }


    public static void main(String[] args) throws Exception {
        String filePath = args[0];
        Double dataSetSizeRelatedthreshold = Double.parseDouble(args[1]);
        int maxDepth = Integer.parseInt(args[2]);
        double relatedRatioThreshold = Double.parseDouble(args[3]);
        //String filePath = "D_10P_0.7378246753246751R_1.0T_11260.json";
        //String filePath = "D_10P_0.7616333464587202R_1.0T_8980377.json";
        //double dataSetSizeRelatedthreshold = 0.1;
        try {
            MultiLabelGraph graph = new MultiLabelGraph(filePath);
            System.out.println("finish read file");
            NAryRelationMiner miner = new NAryRelationMiner(graph, dataSetSizeRelatedthreshold, maxDepth, relatedRatioThreshold);
            System.out.println(miner.getDataGraph().graphName);
            System.out.println(graph.getGraphEdge());
            System.out.println("SupportThreshold" + miner.support);
            miner.mineNAryRelation();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
