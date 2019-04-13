package top.ericcliu.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * @author liubi
 * typeId: id of type in database
 * nodes: nodes id belonging to this type
 * triples: related triples with given maxDeep
 * maxDeep: steps when extract related triples
 * nodeLabels: labels of this id
 */
public class TypeRelatedGraph {
    private Integer typeId;
    private List<Integer> nodes;
    private List<Integer[]> triples;
    private Integer maxDeep;
    private Multimap<Integer,Integer> nodeLabels;

    private static List<Integer> getNodesOfType(Connection db, Integer typeId) throws SQLException {
        Statement stmt = db.createStatement();
        String sql = "SELECT COUNT(DISTINCT node_id) FROM \"nodes_type\" WHERE type_id =" + typeId.toString();
        ResultSet res = stmt.executeQuery(sql);
        Integer typesNum = res.getInt("COUNT(DISTINCT node_id)");
        List<Integer> nodes = new ArrayList<>(typesNum);
        res.close();
        stmt.close();

        sql = "SELECT DISTINCT node_id FROM \"nodes_type\" WHERE type_id =" + typeId.toString();
        res = stmt.executeQuery(sql);
        while (res.next()) {
            nodes.add(res.getInt("node_id"));
        }
        res.close();
        stmt.close();
        return nodes;
    }

    private static List<Integer[]> generateTriples(Connection db,
                                                   Integer nodeId,
                                                   Integer deep,
                                                   List<Integer[]> triples,
                                                   Integer maxDeep,
                                                   Multimap<Integer, Integer> nodeLabels
    )throws SQLException {
        // deep controls the deep of recursion ie. the steps when generate triples from one node
        // 使用前 应在函数外 声明一个set<multimap> nodeLabels 用以记录 node 的label
        if(deep >maxDeep) {return triples;}
        if(triples == null){
            triples = new ArrayList<Integer[]>();
        }
        // 查找 第一层节点label label 加入nodeLabels
        Statement stmt = db.createStatement();
        String sql = "SELECT type_id FROM \"nodes_type\" WHERE node_id =" + nodeId.toString();
        ResultSet res = stmt.executeQuery(sql);
        while(res.next()){
            nodeLabels.put(nodeId,res.getInt("type_id"));
        }
        res.close();
        stmt.close();

        // 查找 triple List<Integer[]> triples,
        sql = "SELECT * FROM \"triples_all\" WHERE subject_id =" + nodeId.toString();
        res = stmt.executeQuery(sql);
        Set<Integer> objects = new HashSet<>();
        while(res.next()){
            Integer[] triple = new Integer[3];
            triple[0] = res.getInt("subject_id");
            triple[1] = res.getInt("predicate_id");
            triple[2] = res.getInt("object_id");
            if(triple[1] == 4 ) {continue;}
            // http://www.w3.org/1999/02/22-rdf-syntax-ns#type
            objects.add(triple[2]);
            triples.add(triple);
        }
        res.close();
        stmt.close();
        // 查找 object label 加入
        for(Integer object : objects){
            sql = "SELECT type_id FROM \"nodes_type\" WHERE node_id =" + object.toString();
            res = stmt.executeQuery(sql);
            boolean haveType = false;
            while(res.next()){
                haveType = true;
                nodeLabels.put(object,res.getInt("type_id"));
            }
            res.close();
            stmt.close();
            if(haveType == false){
                // 该node为 literal
                sql = "SELECT string_type_id FROM \"mapping\" WHERE id =" + object.toString();
                res = stmt.executeQuery(sql);
                while(res.next()){
                    nodeLabels.put(object,-res.getInt("string_type_id"));
                }
            }
            res.close();
            stmt.close();
        }
        for(Integer object:objects){
            return generateTriples(db,object,deep+1,triples,maxDeep,nodeLabels);
        }
        return triples;
    }

    /**
     * 从给定类型 typeID 下 的所有节点 nodeId ，向外搜索n步 生成同类型相关的子图、
     * 存储 typeId / type下的所有 nodeId / id形式的三元组
     * @param db
     * @param maxDeep
     * @param typeId
     * @return
     * @throws SQLException
     */
    public static TypeRelatedGraph extractTypeRelatedGraph(Connection db,
                                                           Integer maxDeep,
                                                           Integer typeId, String filePath, boolean saveToFile, double sampleRatio) throws Exception {
        // 使用前 应在函数外 声明一个set<multimap> nodeLabels 用以记录 node 的label

        List<Integer> OriginNodes = getNodesOfType(db,typeId);
        int sampleQuantity = (int) (OriginNodes.size()*sampleRatio);
        Random random  = new Random();
        List<Integer> nodes = new ArrayList<>(sampleQuantity);
        for(int i=0;i<sampleQuantity;i++){
            nodes.add(random.nextInt(OriginNodes.size()-1));
        }

        List<Integer[]> triples = null;
        Multimap<Integer,Integer> nodeLabels =  TreeMultimap.create();
        for(Integer nodeId : nodes ){
            triples = generateTriples(db,nodeId,1,triples,maxDeep,nodeLabels);
        }
        TypeRelatedGraph typeRelatedGraph = new TypeRelatedGraph(typeId, nodes, triples,maxDeep,nodeLabels);
        if(saveToFile){
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new GuavaModule());
            // 增加jackson 对google guava的支持
            File resultFile = new File(filePath);
            if(resultFile.createNewFile()){
                mapper.writeValue(resultFile,typeRelatedGraph);
            }
            else {
                throw new Exception("file already exist");
            }
        }
        return typeRelatedGraph;
    }

    public static TypeRelatedGraph readFromFile(String filePath) throws Exception {
        File file = new File(filePath);
        TypeRelatedGraph typeRelatedGraph;
        if(file.exists()){
            // 增加jackson 对google guava的支持
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new GuavaModule());
            typeRelatedGraph = mapper.readValue(file, TypeRelatedGraph.class);

        }
        else {
            throw new Exception("file does not exist");
        }
        return typeRelatedGraph;
    }

    public TypeRelatedGraph() {
    }
    public TypeRelatedGraph(TypeRelatedGraph s) {
        this.typeId = s.typeId;
        this.nodes = s.nodes;
        this.triples = s.triples;
        this.maxDeep = s.maxDeep;
        this.nodeLabels = s.nodeLabels;
    }

    public TypeRelatedGraph(Integer typeId, List<Integer> nodes, List<Integer[]> triples, Integer maxDeep, Multimap nodeLabels) {
        this.typeId = typeId;
        this.nodes = nodes;
        //当前类型下的所有节点 ， 子图的起始节点
        this.triples = triples;
        // s p o
        this.maxDeep = maxDeep;
        // 子图拓展的最大步数
        this.nodeLabels = nodeLabels;
        // 子图中所有出现过的节点 (不限制于第一层节点) 的类型标签
    }

    public Multimap<Integer,Integer> getNodeLabels() { return nodeLabels; }

    public void setNodeLabels(Multimap<Integer,Integer> nodeLabels) { this.nodeLabels = nodeLabels; }

    public Integer getMaxDeep(){
        return this.maxDeep;
    }

    public void setMaxDeep(Integer maxDeep){
        this.maxDeep = maxDeep;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public List<Integer> getNodes() {
        return nodes;
    }

    public void setNodes(List<Integer> nodes) {
        this.nodes = nodes;
    }

    public List<Integer[]> getTriples() {
        return triples;
    }

    public void setTriples(List<Integer[]> triples) {
        this.triples = triples;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TypeRelatedGraph subGraph = (TypeRelatedGraph) o;
        return Objects.equals(typeId, subGraph.typeId) &&
                Objects.equals(nodes, subGraph.nodes) &&
                Objects.equals(maxDeep, subGraph.maxDeep) &&
                Objects.equals(triples, subGraph.triples);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeId, nodes, triples,maxDeep);
    }

    @Override
    public String toString() {
        return "SubGraph{" +
                "typeId=" + typeId +
                ", nodes=" + nodes +
                ", triples=" + triples +
                ", maxDeep=" + maxDeep +
                '}';
    }

    public static void main(String[] args) throws Exception {
        DataBaseTools dataBaseTools = new DataBaseTools();
        Connection db = dataBaseTools.sqliteConect("C:\\bioportal.sqlite");
        //8980078
        Integer maxDeep = 10;
        Integer typeId = 8980078;
       // String filePath = "typeRelatedGraph"+typeId+".json";
        String filePath = "TRG_Ratio" + "0.001" + "typeId" + typeId;
        
        TypeRelatedGraph extractAndWirteTest = TypeRelatedGraph.extractTypeRelatedGraph(db,maxDeep,typeId,filePath,true,1);
        TypeRelatedGraph readTest = TypeRelatedGraph.readFromFile(filePath);
    }

}
