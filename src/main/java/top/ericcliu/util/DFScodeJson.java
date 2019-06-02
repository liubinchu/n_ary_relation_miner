package top.ericcliu.util;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * DFScode 中 ArrayList<GSpanEdge> edgeSeq; jackson 无法很好支持，将其改为 Object类型
 */
public class DFScodeJson implements Cloneable{
    private Integer rootNodeId = -1;
    private Integer MNI = -1;
    private Double relatedRatio = -1.0;
    private Integer instanceNum = -1;
    private Integer maxNodeId = -1;
    private ArrayList<Object> edgeSeq;
    private Map<Integer, Integer> nodeLabelMap;
    private Integer rootNodeNum = -1;
    private Double rootNodeRatio = -1.0;


    public DFScodeJson(DFScode dfScode){
        this.rootNodeId = dfScode.getRootNodeId();
        this.MNI = dfScode.getMNI();
        this.relatedRatio = dfScode.getRelatedRatio();
        this.instanceNum = dfScode.getInstanceNum();
        this.maxNodeId = dfScode.getMaxNodeId();
        this.edgeSeq = new ArrayList<>(dfScode.getEdgeSeq().size());
        for(GSpanEdge edge : dfScode.getEdgeSeq()){
            this.edgeSeq.add( (Object) edge);
        }
        this.nodeLabelMap = new TreeMap<>(dfScode.getNodeLabelMap());
        this.rootNodeNum = dfScode.getRootNodeNum();
        this.rootNodeRatio = dfScode.getRootNodeRatio();
    }

    public DFScodeJson(Integer rootNodeId, Integer MNI, Double relatedRatio,
                       Integer instanceNum, Integer maxNodeId, ArrayList<Object> edgeSeq,
                       Map<Integer, Integer> nodeLabelMap, Integer rootNodeNum,
                       Double rootNodeRatio) {
        this.rootNodeId = rootNodeId;
        this.MNI = MNI;
        this.relatedRatio = relatedRatio;
        this.instanceNum = instanceNum;
        this.maxNodeId = maxNodeId;
        this.edgeSeq = edgeSeq;
        this.nodeLabelMap = nodeLabelMap;
        this.rootNodeNum = rootNodeNum;
        this.rootNodeRatio = rootNodeRatio;
    }

    public DFScodeJson() {
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

    public ArrayList<Object> getEdgeSeq() {
        return edgeSeq;
    }

    public void setEdgeSeq(ArrayList<Object> edgeSeq) {
        this.edgeSeq = edgeSeq;
    }

    public Map<Integer, Integer> getNodeLabelMap() {
        return nodeLabelMap;
    }

    public void setNodeLabelMap(Map<Integer, Integer> nodeLabelMap) {
        this.nodeLabelMap = nodeLabelMap;
    }
}