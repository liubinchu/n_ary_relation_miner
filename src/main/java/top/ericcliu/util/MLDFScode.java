package top.ericcliu.util;

import com.google.common.base.Objects;

import java.util.*;

/**
 * @author liubi
 * @date 2019-05-26 21:39
 **/
public class MLDFScode {
    /**
     * -1 未计算
     */
    private Integer rootNodeId = -1;
    private Integer MNI = -1;
    private Double relatedRatio = -1.0;
    private Integer instanceNum = -1;
    private Integer maxNodeId = -1;
    /**
     * 边的集合，边的排序代表着边的添加次序
     */
    private ArrayList<MLGSpanEdge> edgeSeq = new ArrayList<>();
    /**
     * key : nodes appeared in this DFS code, ie nodeId in DFScode, having no relation with dataGraph
     * value : node labels of this node in DFS code
     */
    private Map<Integer, LinkedList<Integer>> nodeLabelMap = new HashMap<>();

    public MLDFScode addEdge(MLGSpanEdge edge) throws Exception {
        boolean first = false;
        if (edgeSeq.size() == 0 && nodeLabelMap.size() == 0) {
            //在空的DFScode中加入一条边
            first = true;
        } else if (edgeSeq.size() == 0 || nodeLabelMap.size() == 0) {
            throw new IllegalArgumentException("illegal multi label DFS code");
        }
        if (first) {
            this.rootNodeId = (Integer) edge.getLabelA().getFirst();
        }
        else {
            LinkedList<Integer> RMP = this.getRightMostPath();
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

    public MLDFScode(MLGSpanEdge edge) {
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
        for (MLGSpanEdge edge : edgeSeq) {
            this.addEdge(edge);
        }
    }

    public LinkedList<Integer> getRightMostPath() throws Exception {
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

    /**
     * 获得DFScode中 节点的标签
     *
     * @param nodeId DFScode 中 节点id
     * @return
     */
    public LinkedList<Integer> getNodeLabel(Integer nodeId) {
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
        return Objects.equal(rootNodeId, mldfScode.rootNodeId) &&
                Objects.equal(MNI, mldfScode.MNI) &&
                Objects.equal(relatedRatio, mldfScode.relatedRatio) &&
                Objects.equal(instanceNum, mldfScode.instanceNum) &&
                Objects.equal(maxNodeId, mldfScode.maxNodeId) &&
                Objects.equal(edgeSeq, mldfScode.edgeSeq) &&
                Objects.equal(nodeLabelMap, mldfScode.nodeLabelMap);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(rootNodeId, MNI, relatedRatio, instanceNum, maxNodeId, edgeSeq, nodeLabelMap);
    }

    public Set<Integer> getNodes(){
        return nodeLabelMap.keySet();
    }

    public Integer getRootNodeId() {
        return rootNodeId;
    }

    public void setRootNodeId(Integer rootNodeId) {
        this.rootNodeId = rootNodeId;
    }

    public Integer getMNI() {
        return MNI;
    }

    public void setMNI(Integer MNI) {
        this.MNI = MNI;
    }

    public Double getRelatedRatio() {
        return relatedRatio;
    }

    public void setRelatedRatio(Double relatedRatio) {
        this.relatedRatio = relatedRatio;
    }

    public Integer getInstanceNum() {
        return instanceNum;
    }

    public void setInstanceNum(Integer instanceNum) {
        this.instanceNum = instanceNum;
    }

    public Integer getMaxNodeId() {
        return maxNodeId;
    }

    public void setMaxNodeId(Integer maxNodeId) {
        this.maxNodeId = maxNodeId;
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

    public static void main(String[] args) throws Exception {
        LinkedList<Integer> label1 = new LinkedList<Integer>();
        label1.add(1);

        LinkedList<Integer> label2 = new LinkedList<Integer>();
        label2.add(2);

        LinkedList<Integer> label3 = new LinkedList<Integer>();
        label3.add(3);

        LinkedList<Integer> label4 = new LinkedList<Integer>();
        label4.add(4);

        ArrayList<MLGSpanEdge> edgeSeq = new ArrayList<>(7);
        edgeSeq.add(new MLGSpanEdge(1, 2, label1, label1, 1, 1));
        //1
        edgeSeq.add(new MLGSpanEdge(2, 3, label1, label2, 1, 1));
        //2
        edgeSeq.add(new MLGSpanEdge(3, 1, label2, label1, 1, 1));
        //3
        edgeSeq.add(new MLGSpanEdge(2, 4, label1, label3, 1, 1));
        //4
        edgeSeq.add(new MLGSpanEdge(4, 1, label3, label1, 1, 1));
        //5
        edgeSeq.add(new MLGSpanEdge(1, 5, label1, label3, 1, 1));
        //6
        edgeSeq.add(new MLGSpanEdge(5, 6, label3, label4, 1, 1));
        //7

        // addEdge() test
        //MLDFScode dfScode = new MLDFScode();
/*        for (MLGSpanEdge edge : edgeSeq) {
            dfScode.addEdge(edge);
        }*/
        System.out.println(" ");
        // MLDFScode(ArrayList<MLGSpanEdge> edgeSeq) test

        MLDFScode dfScode = new MLDFScode(edgeSeq.get(0));
        dfScode.addEdge(edgeSeq.get(1));

        dfScode = new MLDFScode(edgeSeq);
        System.out.println(dfScode.getRightMostPath());
        System.out.println(" ");
    }

}
