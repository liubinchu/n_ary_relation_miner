package top.ericcliu.util;

import java.sql.Connection;
import java.util.*;

/**
 * @author liubi
 * @date 2019-04-09 14:51
 **/
public class DFScodeString implements Cloneable , SaveToFile{
    private Integer rootNodeId = -1;
    private Integer MNI = -1;
    private Double relatedRatio = -1.0;
    private Integer instanceNum = -1;
    private Integer maxNodeId = -1;
    /**
     * 边的集合，边的排序代表着边的添加次序
     */
    private ArrayList<GSpanEdgeString> edgeSeq = new ArrayList<>();
    /**
     * key : nodes appeared in this DFS code, ie nodeId in DFScode, having no relation with dataGraph
     * value : node label of this node in DFS code
     */
    private Map<Integer, String> nodeLabelMap = new HashMap<>();

    public DFScodeString() {
    }
    public DFScodeString(DFScode dfScode,String databasePath,Integer relationId) throws Exception {
        if(dfScode==null){
            throw new Exception("DFScode is null");
        }
        else {
            this.rootNodeId = dfScode.getRootNodeId();
            this.MNI = dfScode.getMNI();
            this.relatedRatio = dfScode.getRelatedRatio();
            this.instanceNum = dfScode.getInstanceNum();
            this.maxNodeId  = dfScode.getMaxNodeId();
            DataBaseTools dataBaseTools = new DataBaseTools();
            try {
                Connection db =dataBaseTools.sqliteConect(databasePath);
                if(dfScode.getNodeLabelMap()!=null&&!dfScode.getNodeLabelMap().isEmpty()){
                    Set<Map.Entry<Integer,Integer>> nodeLabelMapEntrySet = dfScode.getNodeLabelMap().entrySet();
                    for(Map.Entry<Integer,Integer> entry : nodeLabelMapEntrySet){
                        if(entry.getValue().equals(Integer.MIN_VALUE)){
                            this.nodeLabelMap.put(entry.getKey(),dataBaseTools.printer(db,relationId));
                        }
                        else {
                            this.nodeLabelMap.put(entry.getKey(),dataBaseTools.printer(db,entry.getValue()));
                        }
                    }
                }
                db.close();
            }catch (Exception e){
                e.printStackTrace();
            }

            if(dfScode.getEdgeSeq()!=null&&!dfScode.getEdgeSeq().isEmpty()){
                for(GSpanEdge edge : dfScode.getEdgeSeq()){
                    this.edgeSeq.add(new GSpanEdgeString(edge,databasePath,relationId));
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DFScodeString that = (DFScodeString) o;

        if (!Objects.equals(edgeSeq, that.edgeSeq)) {
            return false;
        }
        if (!Objects.equals(maxNodeId, that.maxNodeId)) {
            return false;
        }
        return Objects.equals(nodeLabelMap, that.nodeLabelMap);
    }

    @Override
    public int hashCode() {
        int result = edgeSeq != null ? edgeSeq.hashCode() : 0;
        result = 31 * result + (maxNodeId != null ? maxNodeId.hashCode() : 0);
        result = 31 * result + (nodeLabelMap != null ? nodeLabelMap.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DFScodeString{" +
                "edgeSeq=" + edgeSeq +
                ", maxNodeId=" + maxNodeId +
                ", nodeLabelMap=" + nodeLabelMap +
                '}';
    }

    public ArrayList<GSpanEdgeString> getEdgeSeq() {
        return edgeSeq;
    }

    public void setEdgeSeq(ArrayList<GSpanEdgeString> edgeSeq) {
        this.edgeSeq = edgeSeq;
    }

    public Integer getMaxNodeId() {
        return maxNodeId;
    }

    public void setMaxNodeId(Integer maxNodeId) {
        this.maxNodeId = maxNodeId;
    }

    public Map<Integer, String> getNodeLabelMap() {
        return nodeLabelMap;
    }

    public void setNodeLabelMap(Map<Integer, String> nodeLabelMap) {
        this.nodeLabelMap = nodeLabelMap;
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

    public static void main(String[] args)  {
    }
}
