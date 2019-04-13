package top.ericcliu.util;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class DFScodeJson implements Cloneable{
    private ArrayList<Object> edgeSeq;
    // DFScode 中 ArrayList<GSpanEdge> edgeSeq; jackson 无法很好支持，将其改为 Object类型
    private Integer maxNodeId = -1;
    private Map<Integer, Integer> nodeLabelMap;

    public DFScodeJson(DFScode dfScode){
        this.edgeSeq = new ArrayList<>(dfScode.getEdgeSeq().size());
        for(GSpanEdge edge : dfScode.getEdgeSeq()){
            this.edgeSeq.add( (Object) edge);
        }
        this.maxNodeId = dfScode.getMaxNodeId();
        this.nodeLabelMap = new TreeMap<>(dfScode.getNodeLabelMap());
    }

    public DFScodeJson(ArrayList<Object> edgeSeq, Integer maxNodeId, Map<Integer, Integer> nodeLabelMap) {
        this.edgeSeq = edgeSeq;
        this.maxNodeId = maxNodeId;
        this.nodeLabelMap = nodeLabelMap;
    }

    public DFScodeJson() {
    }

    public ArrayList<Object> getEdgeSeq() {
        return edgeSeq;
    }

    public void setEdgeSeq(ArrayList<Object> edgeSeq) {
        this.edgeSeq = edgeSeq;
    }

    public Integer getMaxNodeId() {
        return maxNodeId;
    }

    public void setMaxNodeId(Integer maxNodeId) {
        this.maxNodeId = maxNodeId;
    }

    public Map<Integer, Integer> getNodeLabelMap() {
        return nodeLabelMap;
    }

    public void setNodeLabelMap(Map<Integer, Integer> nodeLabelMap) {
        this.nodeLabelMap = nodeLabelMap;
    }
}