package top.ericcliu.ds;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * @author liubi
 * @date 2019-01-19 20:15
 **/
public class DFScode implements Cloneable {
    /**
     * -1 未计算
     */
    private Integer rootNodeId = -1;
    private Integer MNI = -1;
    private Double relatedRatio = -1.0;
    private Integer instanceNum = -1;
    private Integer maxNodeId = -1;
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
     * 边的集合，边的排序代表着边的添加次序
     */
    private ArrayList<GSpanEdge> edgeSeq;
    /**
     * key : nodes appeared in this DFS code, ie nodeId in DFScode, having no relation with dataGraph
     * value : node label of this node in DFS code
     */
    private Map<Integer, Integer> nodeLabelMap;


    public DFScode addEdge(GSpanEdge edge) throws CloneNotSupportedException {
        // 还需要 判断加入的边是否合法
        if (this.edgeSeq == null) {
            this.edgeSeq = new ArrayList<>();
        }
        if (this.nodeLabelMap == null) {
            this.nodeLabelMap = new HashMap<>();
        }

        this.edgeSeq.add(((GSpanEdge) edge.clone()));
        Integer nodeA = edge.getNodeA();
        Integer nodeB = edge.getNodeB();
        this.nodeLabelMap.put(nodeA, edge.getLabelA());
        this.nodeLabelMap.put(nodeB, edge.getLabelB());
        this.maxNodeId = nodeA > nodeB ? nodeA : nodeB;
        return this;
    }

    public DFScode(GSpanEdge edge) throws CloneNotSupportedException {
        this.edgeSeq = new ArrayList<>();
        this.nodeLabelMap = new HashMap<>();
        this.edgeSeq.add(((GSpanEdge) edge.clone()));
        Integer nodeA = edge.getNodeA();
        Integer nodeB = edge.getNodeB();
        this.nodeLabelMap.put(nodeA, edge.getLabelA());
        this.nodeLabelMap.put(nodeB, edge.getLabelB());
        this.maxNodeId = nodeA > nodeB ? nodeA : nodeB;
        this.rootNodeId = edge.getLabelA();
    }

    public DFScode(DFScodeJson dfScodeJson) {
        this.rootNodeId = dfScodeJson.getRootNodeId();
        this.MNI = dfScodeJson.getMNI();
        this.relatedRatio = dfScodeJson.getRelatedRatio();
        this.instanceNum = dfScodeJson.getInstanceNum();

        this.maxNodeId = dfScodeJson.getMaxNodeId();

        ObjectMapper mapper = new ObjectMapper();
        this.edgeSeq = new ArrayList<>(dfScodeJson.getEdgeSeq().size());
        for (Object object : dfScodeJson.getEdgeSeq()) {
            this.edgeSeq.add(mapper.convertValue(object, GSpanEdge.class));
        }
        this.nodeLabelMap = new TreeMap<>(dfScodeJson.getNodeLabelMap());
        this.rootNodeNum = dfScodeJson.getRootNodeNum();
        this.rootNodeRatio = dfScodeJson.getRootNodeRatio();
    }

    public DFScode() {
        this.edgeSeq = new ArrayList<>();
        this.nodeLabelMap = new HashMap<>();
    }

    public DFScode(ArrayList<GSpanEdge> edgeSeq) {
        this.edgeSeq = new ArrayList<>(edgeSeq);
        this.nodeLabelMap = new HashMap<>();

        int minNodeId = Integer.MAX_VALUE;
        for (GSpanEdge edge : edgeSeq) {
            int nodeA = edge.getNodeA();
            int nodeB = edge.getNodeB();
            this.nodeLabelMap.put(nodeA, edge.getLabelA());
            this.nodeLabelMap.put(nodeB, edge.getLabelB());
            this.maxNodeId = Math.max(this.maxNodeId, nodeA);
            this.maxNodeId = Math.max(this.maxNodeId, nodeB);
            minNodeId = Math.min(minNodeId, nodeA);
            minNodeId = Math.min(minNodeId, nodeB);
        }
        this.rootNodeId = fetchNodeLabel(minNodeId);
    }

    public DFScode(DFScode dfScode) {
        this.rootNodeId = dfScode.rootNodeId;
        this.MNI = dfScode.MNI;
        this.relatedRatio = dfScode.relatedRatio;
        this.instanceNum = dfScode.instanceNum;
        this.maxNodeId = dfScode.maxNodeId;
        this.edgeSeq = new ArrayList<>(dfScode.edgeSeq.size());
        for (GSpanEdge edge : dfScode.edgeSeq) {
            this.edgeSeq.add(new GSpanEdge(edge));
        }
        this.nodeLabelMap = new HashMap<>(dfScode.nodeLabelMap.size());
        for (Map.Entry<Integer, Integer> entry : dfScode.nodeLabelMap.entrySet()) {
            this.nodeLabelMap.put(entry.getKey(), entry.getValue());
        }
        this.rootNodeNum = dfScode.getRootNodeNum();
        this.rootNodeRatio = dfScode.getRootNodeRatio();
    }

    /**
     * if this dfsCode is parent of  possibleChild return true else return false
     *
     * @param possibleChild
     * @return 1 equal, 0 parent -1 not parent(child/no relation)
     */
    public int isParentOf(DFScode possibleChild) throws Exception {
        //return -1;
        if (possibleChild.getEdgeSeq().isEmpty() || this.getEdgeSeq().isEmpty()) {
            throw new Exception("illegal DFS code");
        } else if (possibleChild.getEdgeSeq().size() < this.getEdgeSeq().size()) {
            return -1;
        } else {
            return DFScodeTree.isParentOf(new DFScodeTree(this), new DFScodeTree(possibleChild));
        }
    }

    public boolean saveToFile(String filePath, boolean isAppend) throws Exception {
        File file = new File(filePath);
        FileWriter fileWriter;
        DFScodeJson dfScodeJson = new DFScodeJson(this);
        if (file.exists()) {
            fileWriter = new FileWriter(filePath, isAppend);
        } else {
            fileWriter = new FileWriter(filePath);
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(fileWriter, dfScodeJson);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static DFScode readFromFile(String filePath) throws Exception {
        File file = new File(filePath);
        DFScodeJson dfScodeJson;
        if (file.exists()) {
            // 增加jackson 对google guava的支持
            ObjectMapper mapper = new ObjectMapper();

            dfScodeJson = mapper.readValue(file, DFScodeJson.class);
        } else {
            throw new Exception("file does not exist");
        }
        return new DFScode(dfScodeJson);
    }

    public LinkedList<Integer> fetchRightMostPath() throws Exception {
        LinkedList<Integer> rightMostPath = new LinkedList<>();
        for (GSpanEdge edge : this.edgeSeq) {
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
    public Integer fetchNodeLabel(Integer nodeId) {
        return this.nodeLabelMap.get(nodeId);
    }

    public Set<Integer> fetchNodes() {
        return nodeLabelMap.keySet();
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

    public void setMaxNodeId(Integer maxNodeId) {
        this.maxNodeId = maxNodeId;
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

    public ArrayList<GSpanEdge> getEdgeSeq() {
        return edgeSeq;
    }

    public void setEdgeSeq(ArrayList<GSpanEdge> edgeSeq) {
        this.edgeSeq = edgeSeq;
    }


    public Integer getMaxNodeId() {
        return maxNodeId;
    }

    public Map<Integer, Integer> getNodeLabelMap() {
        return nodeLabelMap;
    }

    public void setNodeLabelMap(Map<Integer, Integer> nodeLabelMap) {
        this.nodeLabelMap = nodeLabelMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DFScode dfScode = (DFScode) o;
        return Objects.equals(edgeSeq, dfScode.edgeSeq);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edgeSeq);
    }

    @Override
    public String toString() {
        return "DFScode{" +
                "edgeSeq=" + edgeSeq +
                '}';
    }
}
