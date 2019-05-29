package top.ericcliu.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;

import java.io.File;
import java.util.*;

/**
 * @author liubi
 * @date 2019-05-26 21:39
 **/
public class MLDFScode implements SaveToFile{
    /**
     * -1 未计算
     */
    private int rootNodeId = -1;
    private int MNI = -1;
    private double relatedRatio = -1.0;
    private int instanceNum = -1;
    private int maxNodeId = -1;
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
        //MLDFScodeJson dfScodeJson;
        MLDFScode mldfScode ;
        if (file.exists()) {
            // 增加jackson 对google guava的支持
            ObjectMapper mapper = new ObjectMapper();
            mldfScode = mapper.readValue(file, MLDFScode.class);
        } else {
            throw new Exception("file does not exist");
        }
        return mldfScode;
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
            LinkedList<Integer> RMP = this.fatchRightMostPath();
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
            if (!first&&lastNodeB==nodeB&&lastNodeA==nodeA) {
                this.addLabel(edge);
            } else  {
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

    }

    public MLDFScode(DFScode dfScode){
        this.rootNodeId = dfScode.getRootNodeId();
        this.MNI = dfScode.getMNI();
        this.relatedRatio = dfScode.getRelatedRatio();
        this.instanceNum = dfScode.getInstanceNum();
        this.maxNodeId = dfScode.getMaxNodeId();
        this.turn = dfScode.getEdgeSeq().size();

        for(GSpanEdge edge : dfScode.getEdgeSeq()) {
            this.edgeSeq.add(new MLGSpanEdge(edge));
        }
        for(Map.Entry<Integer, Integer> entry: dfScode.getNodeLabelMap().entrySet()){
            LinkedList<Integer> labels = new LinkedList<>();
            labels.add(entry.getValue());
            this.nodeLabelMap.put(entry.getKey(),labels);
        }
    }


    public LinkedList<Integer> fatchRightMostPath() throws Exception {
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
        return rootNodeId == mldfScode.rootNodeId &&
                MNI == mldfScode.MNI &&
                Double.compare(mldfScode.relatedRatio, relatedRatio) == 0 &&
                instanceNum == mldfScode.instanceNum &&
                maxNodeId == mldfScode.maxNodeId &&
                turn == mldfScode.turn &&
                Objects.equal(edgeSeq, mldfScode.edgeSeq) &&
                Objects.equal(nodeLabelMap, mldfScode.nodeLabelMap);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(rootNodeId, MNI, relatedRatio, instanceNum, maxNodeId, turn, edgeSeq, nodeLabelMap);
    }

    public Set<Integer> fatchNodes() {
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

    public int getTurn() {
        return turn;
    }

    public static void main(String[] args) throws Exception {
        LinkedList<Integer> label1 = new LinkedList<>();
        label1.add(1);
        LinkedList<Integer> label2 = new LinkedList<>();
        label2.add(2);
        LinkedList<Integer> label3 = new LinkedList<>();
        label3.add(3);
        LinkedList<Integer> label4 = new LinkedList<>();
        label4.add(4);

        ArrayList<MLGSpanEdge> edgeSeq = new ArrayList<>(7);
        edgeSeq.add(new MLGSpanEdge(1, 2, label1, label1, 1, 1));
        //1
        edgeSeq.add(new MLGSpanEdge(2, 3, label1, label2, 1, 1));
        //2
        edgeSeq.add(new MLGSpanEdge(2, 4, label1, label3, 1, 1));
        //3
        edgeSeq.add(new MLGSpanEdge(4, 5, label3, label1, 1, 1));
        //4
        edgeSeq.add(new MLGSpanEdge(1, 5, label1, label3, 1, 1));
        //5
        edgeSeq.add(new MLGSpanEdge(5, 6, label3, label4, 1, 1));
        //6
        edgeSeq.add(new MLGSpanEdge(5, 6, label3, label2, 1, 1));
        MLDFScode dfScode = new MLDFScode(edgeSeq);

        dfScode.saveToFile("mlDFScode.json",false);
        dfScode = MLDFScode.readFromFile("mlDFScode.json");
        System.out.println(dfScode);
    }

}
