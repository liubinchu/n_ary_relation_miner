package top.ericcliu.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author liubi
 * @date 2019-04-09 14:51
 **/
public class DFScodeString implements Cloneable {
    /**
     * 边的集合，边的排序代表着边的添加次序
     */
    private ArrayList<GSpanEdgeString> edgeSeq = new ArrayList<>();
    private Integer maxNodeId = -1;
    /**
     * key : nodes appeared in this DFS code, ie nodeId in DFScode, having no relation with dataGraph
     * value : node label of this node in DFS code
     */
    private Map<Integer, String> nodeLabelMap = new HashMap<>();


/*    public DFScodeString(DFScodeJson dfScodeJson){
        ObjectMapper mapper = new ObjectMapper();
        this.edgeSeq = new ArrayList<>(dfScodeJson.getEdgeSeq().size());
        for(Object object : dfScodeJson.getEdgeSeq()){
            this.edgeSeq.add(mapper.convertValue(object,GSpanEdge.class));
        }
        this.maxNodeId = dfScodeJson.getMaxNodeId();
        this.nodeLabelMap = new TreeMap<>(dfScodeJson.getNodeLabelMap());
    }*/

    public DFScodeString() {
    }
    public DFScodeString(DFScode dfScode,String databasePath) throws Exception {
        if(dfScode==null){
            throw new Exception("DFScode is null");
        }
        else {
            this.maxNodeId  = dfScode.getMaxNodeId();
            DataBaseTools dataBaseTools = new DataBaseTools();
            try {
                Connection db =dataBaseTools.sqliteConect(databasePath);
                if(dfScode.getNodeLabelMap()!=null&&!dfScode.getNodeLabelMap().isEmpty()){
                    Set<Map.Entry<Integer,Integer>> nodeLabelMapEntrySet = dfScode.getNodeLabelMap().entrySet();
                    for(Map.Entry<Integer,Integer> entry : nodeLabelMapEntrySet){
                        this.nodeLabelMap.put(entry.getKey(),dataBaseTools.printer(db,entry.getValue()));
                    }
                }
                db.close();
            }catch (Exception e){
                e.printStackTrace();
            }

            if(dfScode.getEdgeSeq()!=null&&!dfScode.getEdgeSeq().isEmpty()){
                for(GSpanEdge edge : dfScode.getEdgeSeq()){
                    this.edgeSeq.add(new GSpanEdgeString(edge,databasePath));
                }
            }
        }
    }





   public boolean saveToFile(String filePath, boolean isAppend) throws Exception {
        File file = new File(filePath);
        FileWriter fileWriter;
        if(file.exists()){
            fileWriter = new FileWriter(filePath,isAppend);
        }
        else {
            fileWriter = new FileWriter(filePath);
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(fileWriter,this);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<GSpanEdgeString> getEdgeSeq() {
        return edgeSeq;
    }

    public Integer getMaxNodeId() {
        return maxNodeId;
    }

    public Map<Integer, String> getNodeLabelMap() {
        return nodeLabelMap;
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

        if (edgeSeq != null ? !edgeSeq.equals(that.edgeSeq) : that.edgeSeq != null) {
            return false;
        }
        if (maxNodeId != null ? !maxNodeId.equals(that.maxNodeId) : that.maxNodeId != null) {
            return false;
        }
        return nodeLabelMap != null ? nodeLabelMap.equals(that.nodeLabelMap) : that.nodeLabelMap == null;
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




    public static void main(String[] args)  {


    }
}
