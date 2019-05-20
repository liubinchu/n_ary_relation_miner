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
    }

    public DFScodeJson(Integer rootNodeId,
                       Integer MNI,
                       Double relatedRatio,
                       Integer instanceNum,
                       Integer maxNodeId,
                       ArrayList<Object> edgeSeq,
                       Map<Integer, Integer> nodeLabelMap) {
        this.rootNodeId = rootNodeId;
        this.MNI = MNI;
        this.relatedRatio = relatedRatio;
        this.instanceNum = instanceNum;
        this.maxNodeId = maxNodeId;
        this.edgeSeq = new ArrayList<>(edgeSeq.size());
        this.edgeSeq.addAll(edgeSeq);
        this.nodeLabelMap = nodeLabelMap;
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