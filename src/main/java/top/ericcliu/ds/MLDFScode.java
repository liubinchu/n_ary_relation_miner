package top.ericcliu.ds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;

/**
 * @author liubi
 * @date 2019-05-26 21:39
 **/
public class MLDFScode implements SaveToFile {
    /**
     * -1 未计算
     */
    private int rootNodeId = -1;
    private int MNI = -1;
    private double relatedRatio = -1.0;
    private int instanceNum = -1;
    private int maxNodeId = -1;
    /**
     * 不重复的根节点的个数
     */
    private Integer rootNodeNum = -1;
    /**
     * 数据图共具有n个不同的rootNode，该模式具有m个不同的rootNode
     * rootNodeRatio = m/n
     */
    private Double rootNodeRatio = -1.0;
    /**
     * 经过增长的轮数
     */
    private int turn = 0;
    /**
     * 边的集合，边的排序代表着边的添加次序
     */
    private ArrayList<MLGSpanEdge> edgeSeq = new ArrayList<>();
    /**
     * key : nodes appeared in this DFS code, ie nodeId in DFScode, having no relation with dataGraph
     * value : node labels of this node in DFS code
     */
    private Map<Integer, LinkedList<Integer>> nodeLabelMap = new HashMap<>();

    public static MLDFScode readFromFile(String filePath) throws Exception {
        File file = new File(filePath);
        if (file.exists()) {
            // 增加jackson 对google guava的支持
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(file, MLDFScode.class);
        } else {
            throw new Exception("file does not exist");
        }
    }

    /**
     * if this dfsCode is parent of  possibleChild return true else return false
     *
     * @param other
     * @return 1 equal, 0 parent -1 not parent(child/no relation)
     */
    public int isParentOf(@Nonnull MLDFScode other) throws Exception {
        if (other.getEdgeSeq().isEmpty() || this.getEdgeSeq().isEmpty()) {
            throw new Exception("illegal ML DFS code");
        } else if (other.getEdgeSeq().size() < this.getEdgeSeq().size()) {
            return -1;
        } else {
            return MLDFScodeTree.isParentOf(new MLDFScodeTree(this), new MLDFScodeTree(other));
        }
    }

    public MLDFScode addEdge(MLGSpanEdge edge) throws Exception {
        this.turn++;
        boolean first = false;
        if (edgeSeq.size() == 0 && nodeLabelMap.size() == 0) {
            //在空的DFScode中加入一条边
            first = true;
        } else if (edgeSeq.size() == 0 || nodeLabelMap.size() == 0) {
            throw new IllegalArgumentException("illegal multi label DFS code");
        }
        if (first) {
            this.rootNodeId = (Integer) edge.getLabelA().getFirst();
        } else {
            LinkedList<Integer> RMP = this.fetchRightMostPath();
            // 合法性检查
            if (RMP.size() < 2) {
                throw new IllegalArgumentException("illegal multi label DFS code");
            }
            if (edge.getNodeA() < edge.getNodeB()) {
                // forward edge
                Set<Integer> RMPnodes = new HashSet<>(RMP);
                if (!RMPnodes.contains(edge.getNodeA())) {
                    throw new IllegalArgumentException("illegal multi label GSpan Edge");
                }
            } else if (edge.getNodeA() == edge.getNodeB()) {
                throw new IllegalArgumentException("illegal multi label GSpan Edge");
            } else {
                // backward edge, 在最右路径中，先找到nodeA,再找到nodeB
                try {
                    while (RMP.getLast() != edge.getNodeA()) {
                        RMP.removeLast();
                    }
                    while (RMP.getLast() != edge.getNodeB()) {
                        RMP.removeLast();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new IllegalArgumentException("illegal multi label GSpan Edge");
                }
            }
        }
        this.edgeSeq.add(new MLGSpanEdge(edge));
        this.nodeLabelMap.put(edge.getNodeA(), edge.getLabelA());
        this.nodeLabelMap.put(edge.getNodeB(), edge.getLabelB());
        this.maxNodeId = edge.getNodeA() > edge.getNodeB() ? edge.getNodeA() : edge.getNodeB();
        return this;
    }

    public MLDFScode addLabel(MLGSpanEdge edge) throws Exception {
        this.turn++;
        MLGSpanEdge lastEdge = this.edgeSeq.get(this.edgeSeq.size() - 1);
        // 因为不存在后向边，边集的最有一个边，一定是最右边
        if (edge.getNodeA() != lastEdge.getNodeA()
                || edge.getNodeB() != lastEdge.getNodeB()
                || !edge.getLabelA().equals(lastEdge.getLabelA())
                || edge.getEdgeLabel() != lastEdge.getEdgeLabel()) {
            throw new IllegalArgumentException("非法传入参数 edge");
        }
        int nodeB = lastEdge.getNodeB();
        int labelBNew = (int) edge.getLabelB().getFirst();
        lastEdge.addLabelToNodeB(labelBNew);
        this.nodeLabelMap.get(nodeB).add(labelBNew);
        return this;
    }

    public MLDFScode(MLGSpanEdge edge) {
        this.turn++;
        this.edgeSeq.add(new MLGSpanEdge(edge));
        this.nodeLabelMap.put(edge.getNodeA(), edge.getLabelA());
        this.nodeLabelMap.put(edge.getNodeB(), edge.getLabelB());
        this.maxNodeId = edge.getNodeA() > edge.getNodeB() ? edge.getNodeA() : edge.getNodeB();
        if (edge.getLabelA().size() == 1) {
            this.rootNodeId = (Integer) edge.getLabelA().getFirst();
        }
    }

    public MLDFScode() {
    }

    public MLDFScode(ArrayList<MLGSpanEdge> edgeSeq) throws Exception {
        int lastNodeA = Integer.MAX_VALUE;
        int lastNodeB = Integer.MAX_VALUE;
        boolean first = true;
        for (MLGSpanEdge edge : edgeSeq) {
            int nodeA = edge.getNodeA();
            int nodeB = edge.getNodeB();
            if (!first && lastNodeB == nodeB && lastNodeA == nodeA) {
                this.addLabel(edge);
            } else {
                first = false;
                this.addEdge(edge);
            }
            lastNodeA = nodeA;
            lastNodeB = nodeB;
        }
    }

    public MLDFScode(MLDFScode mldfScode) {
        this.rootNodeId = mldfScode.rootNodeId;
        this.MNI = mldfScode.MNI;
        this.relatedRatio = mldfScode.relatedRatio;
        this.instanceNum = mldfScode.instanceNum;
        this.maxNodeId = mldfScode.maxNodeId;
        this.turn = mldfScode.turn;

        this.edgeSeq = new ArrayList<>(mldfScode.edgeSeq.size());
        for (MLGSpanEdge edge : mldfScode.edgeSeq) {
            this.edgeSeq.add(new MLGSpanEdge(edge));
        }

        this.nodeLabelMap = new HashMap<>(mldfScode.nodeLabelMap.size());
        for (Map.Entry<Integer, LinkedList<Integer>> entry : mldfScode.nodeLabelMap.entrySet()) {
            this.nodeLabelMap.put(entry.getKey(), new LinkedList<>(entry.getValue()));
        }
        this.rootNodeNum = mldfScode.getRootNodeNum();
        this.rootNodeRatio = mldfScode.getRootNodeRatio();
    }

    public MLDFScode(DFScode dfScode) {
        this.rootNodeId = dfScode.getRootNodeId();
        this.MNI = dfScode.getMNI();
        this.relatedRatio = dfScode.getRelatedRatio();
        this.instanceNum = dfScode.getInstanceNum();
        this.maxNodeId = dfScode.getMaxNodeId();
        this.turn = dfScode.getEdgeSeq().size();

        for (GSpanEdge edge : dfScode.getEdgeSeq()) {
            this.edgeSeq.add(new MLGSpanEdge(edge));
        }
        for (Map.Entry<Integer, Integer> entry : dfScode.getNodeLabelMap().entrySet()) {
            LinkedList<Integer> labels = new LinkedList<>();
            labels.add(entry.getValue());
            this.nodeLabelMap.put(entry.getKey(), labels);
        }
        this.rootNodeNum = dfScode.getRootNodeNum();
        this.rootNodeRatio = dfScode.getRootNodeRatio();
    }


    public LinkedList<Integer> fetchRightMostPath() throws Exception {
        LinkedList<Integer> rightMostPath = new LinkedList<>();
        for (MLGSpanEdge edge : this.edgeSeq) {
            int nodeA = edge.getNodeA();
            int nodeB = edge.getNodeB();
            if (nodeA < nodeB) {
                //forward edge
                if (rightMostPath.size() == 0) {
                    rightMostPath.add(nodeA);
                    rightMostPath.add(nodeB);
                } else {
                    while (rightMostPath.getLast() != nodeA) {
                        rightMostPath.removeLast();
                    }
                    if (rightMostPath.size() == 0) {
                        throw new Exception("input error in DFScode, not connected graph");
                    }
                    rightMostPath.add(nodeB);
                }
            } else if (nodeA == nodeB) {
                throw new Exception("input error in GSpanEdge, nodeA == nodeB");
            }
            // backward edge 对于 最右路径没有 改变
            // 因此 DFScode生长时需要判断是否真的增长了
        }
        return rightMostPath;
    }

    public Set<Integer> fetchNodes() {
        return nodeLabelMap.keySet();
    }

    /**
     * 获得DFScode中 节点的标签
     *
     * @param nodeId DFScode 中 节点id
     * @return
     */
    public LinkedList<Integer> fetchNodeLabel(Integer nodeId) {
        return this.nodeLabelMap.get(nodeId);
    }

    @Override
    public String toString() {
        return "MLDFScode{" +
                "rootNodeId=" + rootNodeId +
                ", MNI=" + MNI +
                ", relatedRatio=" + relatedRatio +
                ", instanceNum=" + instanceNum +
                ", maxNodeId=" + maxNodeId +
                ", rootNodeNum=" + rootNodeNum +
                ", rootNodeRatio=" + rootNodeRatio +
                ", turn=" + turn +
                ", edgeSeq=" + edgeSeq +
                ", nodeLabelMap=" + nodeLabelMap +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MLDFScode mldfScode = (MLDFScode) o;
        return rootNodeId == mldfScode.rootNodeId &&
                MNI == mldfScode.MNI &&
                Double.compare(mldfScode.relatedRatio, relatedRatio) == 0 &&
                instanceNum == mldfScode.instanceNum &&
                maxNodeId == mldfScode.maxNodeId &&
                turn == mldfScode.turn &&
                Objects.equal(rootNodeNum, mldfScode.rootNodeNum) &&
                Objects.equal(rootNodeRatio, mldfScode.rootNodeRatio) &&
                Objects.equal(edgeSeq, mldfScode.edgeSeq) &&
                Objects.equal(nodeLabelMap, mldfScode.nodeLabelMap);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(rootNodeId, MNI, relatedRatio, instanceNum, maxNodeId, rootNodeNum, rootNodeRatio, turn, edgeSeq, nodeLabelMap);
    }

    public int getRootNodeId() {
        return rootNodeId;
    }

    public void setRootNodeId(int rootNodeId) {
        this.rootNodeId = rootNodeId;
    }

    public int getMNI() {
        return MNI;
    }

    public void setMNI(int MNI) {
        this.MNI = MNI;
    }

    public double getRelatedRatio() {
        return relatedRatio;
    }

    public void setRelatedRatio(double relatedRatio) {
        this.relatedRatio = relatedRatio;
    }

    public int getInstanceNum() {
        return instanceNum;
    }

    public void setInstanceNum(int instanceNum) {
        this.instanceNum = instanceNum;
    }

    public int getMaxNodeId() {
        return maxNodeId;
    }

    public void setMaxNodeId(int maxNodeId) {
        this.maxNodeId = maxNodeId;
    }

    public Integer getRootNodeNum() {
        return rootNodeNum;
    }

    public void setRootNodeNum(Integer rootNodeNum) {
        this.rootNodeNum = rootNodeNum;
    }

    public Double getRootNodeRatio() {
        return rootNodeRatio;
    }

    public void setRootNodeRatio(Double rootNodeRatio) {
        this.rootNodeRatio = rootNodeRatio;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public ArrayList<MLGSpanEdge> getEdgeSeq() {
        return edgeSeq;
    }

    public void setEdgeSeq(ArrayList<MLGSpanEdge> edgeSeq) {
        this.edgeSeq = edgeSeq;
    }

    public Map<Integer, LinkedList<Integer>> getNodeLabelMap() {
        return nodeLabelMap;
    }

    public void setNodeLabelMap(Map<Integer, LinkedList<Integer>> nodeLabelMap) {
        this.nodeLabelMap = nodeLabelMap;
    }
}
