package top.ericcliu.util;

import org.apache.jena.base.Sys;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * @author liubi
 * @date 2019-04-28 22:10
 **/
public class MiningRes {
    /**
     * 边的集合，边的排序代表着边的添加次序
     */
    private ArrayList<GSpanEdge> edgeSeq;
    private Integer maxNodeId = -1;
    /**
     * key : nodes appeared in this DFS code, ie nodeId in DFScode, having no relation with dataGraph
     * value : node label of this node in DFS code
     */
    private Map<Integer, Integer> nodeLabelMap;

    private Integer relatedRatio;
    private Integer instanceNum;
    private Integer MNI;
    private Map<Integer, Set<Integer>> nodeCardinality;

    public MiningRes() {
    }
    public MiningRes(DFScode dfScode) {
        this.edgeSeq = dfScode.getEdgeSeq();
        this.maxNodeId = dfScode.getMaxNodeId();
        this.nodeLabelMap = dfScode.getNodeLabelMap();
        this.relatedRatio = -1; // need to be initial
        this.instanceNum = -1; // need to be initial
        this.nodeCardinality = null; // need to be initial
    }

    public ArrayList<GSpanEdge> getEdgeSeq() {
        return edgeSeq;
    }

    public Integer getMaxNodeId() {
        return maxNodeId;
    }

    public Map<Integer, Integer> getNodeLabelMap() {
        return nodeLabelMap;
    }

    public Integer getRelatedRatio() {
        return relatedRatio;
    }

    public Integer getInstanceNum() {
        return instanceNum;
    }

    public Integer getMNI() {
        return MNI;
    }

    public Map<Integer, Set<Integer>> getNodeCardinality() {
        return nodeCardinality;
    }
    public static void main(String[] args){
        String file = "D_10P_0.7378246753246751R_1.0T_11260.jsonMNI_0.1";
        String[] strings = file.split("T_|.json");
        System.currentTimeMillis();
    }
}
