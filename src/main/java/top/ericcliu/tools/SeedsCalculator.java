package top.ericcliu.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import top.ericcliu.ds.Seed;
import top.ericcliu.ds.SeedEdge;
import top.ericcliu.ds.SeedString;
import top.ericcliu.ds.TypeRelatedGraph;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * @author liubi
 */
public class SeedsCalculator {
    private static Set<Integer> subMetaData = new HashSet<>();
    private static Set<Integer> predMetaData = new HashSet<>();
    private static Set<Integer> objMetaData = new HashSet<>();
    private final static String DATA_BASE_PATH = "/home/lbc/bioportal1.sqlite";
    //private static String DATA_BASE_PATH = "C:\\bioportal1.sqlite";
    private final static String OUT_PUT_FILE_NAME = "seeds1"+".json";
    private static Connection db = new DataBaseTools().sqliteConect(DATA_BASE_PATH);

   static {
        try {
             // 需要去除的类型 ie 主语
            subMetaData.addAll(getIDs("SELECT id FROM mapping WHERE content LIKE \"http://www.w3.org/%\""));
            subMetaData.removeAll(getIDs("SELECT id FROM mapping WHERE content LIKE \"http://www.w3.org/2001/sw/hcls/ns/transmed/TMO%\""));
            subMetaData.addAll(getIDs("SELECT id FROM mapping WHERE content LIKE \"http://purl.org/dc/terms%\""));
            subMetaData.addAll(getIDs("SELECT id FROM mapping WHERE content like \"http://bio2rdf.org%esource\""));
            subMetaData.addAll(getIDs("select id from mapping where content like \"%ataset\""));
            subMetaData.addAll(getIDs("select id from mapping where content like \"%identifier\""));
            subMetaData.addAll(getIDs("select id from mapping where content like \"%identifiers.org\""));
            subMetaData.addAll(getIDs("select id from mapping where content like \"%namespace\""));
            subMetaData.addAll(getIDs("select id from mapping where content like \"http://bio2rdf.org/bio2rdf_vocabulary:%\""));
            subMetaData.addAll(getIDs("select id from mapping where content like \"%comment\""));
            subMetaData.removeAll(getIDs("select id from mapping where content like \"http://proteomeontology.org%comment\""));
            subMetaData.removeAll(getIDs("select id from mapping where content like \"http://semantic-dicom.org%comment\""));
            subMetaData.addAll(getIDs("select id from mapping where content like \"%isDefinedBy\""));
            subMetaData.addAll(getIDs("select id from mapping where content like \"%label\""));
            subMetaData.addAll(getIDs("select id from mapping where content like \"%created_by\""));
            subMetaData.addAll(getIDs("select id from mapping where content like \"%creation_date\""));
            subMetaData.addAll(getIDs("select id from mapping where content like \"%sameAs\""));
            subMetaData.addAll(getIDs("select id from mapping where content like \"%description\""));
            System.out.println("finish getting subMetaData");
            predMetaData.addAll(getIDs("SELECT id FROM mapping WHERE content = \"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\""));
            predMetaData.addAll(getIDs("SELECT id FROM mapping WHERE content = \"http://www.w3.org/2000/01/rdf-schema#subClassOf\""));
            predMetaData.addAll(getIDs("SELECT id FROM mapping where content like \"%Dataset\""));
            predMetaData.addAll(getIDs("SELECT id FROM mapping where content like \"http://www.w3.org/2002/07/%Class\""));
            predMetaData.addAll(getIDs("SELECT id FROM mapping where content like \"%sameAs\""));
            predMetaData.addAll(getIDs("SELECT id FROM mapping where content like \"http://www.w3.org/2002/07/owl#Thing\""));
            predMetaData.addAll(getIDs("SELECT id FROM mapping where content like \"http://www.w3.org/2000/01/rdf-schema#label\""));
            System.out.println("finish getting predMetaData");
            objMetaData.addAll(getIDs("SELECT * FROM mapping WHERE content = \"http://bio2rdf.org/bio2rdf_vocabulary:namespace\""));
            objMetaData.addAll(getIDs("SELECT id FROM mapping where content like \"http://bio2rdf.org%Resource\""));
            objMetaData.addAll(getIDs("SELECT id FROM mapping where content like \"%Dataset\""));
            objMetaData.addAll(getIDs("SELECT id FROM mapping where content like \"http://who.int/icd#%DefinitionTerm\" or content like \"http://who.int/icd#ClamlReference\" or content like \"http://who.int/icd#TitleTerm\" or content like \"http://who.int/icd#InclusionTerm\""));
            objMetaData.addAll(getIDs("SELECT id FROM mapping where content like \"%Dataset\""));
            System.out.println("finish getting objMetaData");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<Integer> getAlltypes() throws SQLException {
        Statement stmt = db.createStatement();
        String sql = "SELECT COUNT(*) FROM \"types_node\" ";
        ResultSet res = stmt.executeQuery(sql);
        int typesNum = res.getInt("COUNT(*)");
        System.out.println("types num : " + typesNum);
        ArrayList<Integer> types = new ArrayList<>(typesNum);
        res.close();
        stmt.close();
        sql = "SELECT types_id FROM \"types_node\" ";
        res = stmt.executeQuery(sql);
        while (res.next()) {
            types.add(res.getInt("types_id"));
        }
        res.close();
        stmt.close();
        return types;
    }

    private static Map<Integer, Integer> getNodesOfType(Integer typeId) throws SQLException {
        // firstElement = nodeId ;second element = edgeNums of this node , initial with -1 // means uncalculated
        Statement stmt = db.createStatement();
        ResultSet res = stmt.executeQuery("SELECT COUNT(DISTINCT node_id) FROM \"nodes_type\" WHERE type_id =" + typeId.toString());
        int typesNum = res.getInt("COUNT(DISTINCT node_id)");
        Map<Integer, Integer> nodes = new HashMap<>(typesNum);
        res.close();
        stmt.close();
        res = stmt.executeQuery("SELECT DISTINCT node_id FROM \"nodes_type\" WHERE type_id =" + typeId.toString());
        while (res.next()) {
            nodes.put(res.getInt("node_id"), -1);
        }
        res.close();
        stmt.close();
        return nodes;
    }

    private static Set<SeedEdge> getEdgesOfNode(int nodeId,
                                                Map<Integer, Integer> nodes,
                                                Set<SeedEdge> commonEdges) throws SQLException {
        // 1 .get all edges of one node  2.calculate the union set of these edges from different edges
        Set<SeedEdge> edges = new HashSet<>();
        Statement stmt0 = db.createStatement();
        Statement stmt1 = db.createStatement();
        ResultSet res1;
        ResultSet res0 = stmt0.executeQuery("SELECT DISTINCT * FROM \"triples_all\" WHERE subject_id =" + nodeId);
        while (res0.next()) {
            int edgeLabel = res0.getInt("predicate_id");
            if (predMetaData.contains(edgeLabel)) {
                continue;
            }
            int nodeBId = res0.getInt("object_id");
            res1 = stmt1.executeQuery("SELECT DISTINCT type_id FROM \"nodes_type\" WHERE node_id = " + nodeBId);
            Set<Integer> nodeBLabel = new HashSet<>();
            boolean haveType = false;
            while (res1.next()) {
                haveType = true;
                int objLabel = res1.getInt("type_id");
                if (objMetaData.contains(objLabel)) {
                    continue;
                }
                nodeBLabel.add(objLabel);
            }
            res1.close();
            stmt1.close();
            if (!haveType) {
                // 该node为 literal
                res1 = stmt1.executeQuery("SELECT string_type_id FROM \"mapping\" WHERE id =" + nodeBId);
                while (res1.next()) {
                    nodeBLabel.add(-res1.getInt("string_type_id"));
                }
                res1.close();
                stmt1.close();
            }
            if (nodeBLabel.isEmpty()) {
                continue;
            }
            edges.add(new SeedEdge(edgeLabel, nodeBLabel));
        }
        nodes.put(nodeId, edges.size());
        res0.close();
        stmt0.close();
        if (commonEdges == null) {
            // 公共边集为空，当前元素是该类型下计算的第一个元素
            commonEdges = new HashSet<>();
            commonEdges.addAll(edges);
        } else {
            commonEdges.retainAll(edges);
        }
        return commonEdges;
    }

    private static ArrayList<Integer> getIDs(String sql) throws SQLException {
        Statement stmt = db.createStatement();
        ResultSet res = stmt.executeQuery(sql);
        ArrayList<Integer> result = new ArrayList<>();
        while (res.next()) {
            result.add(res.getInt("id"));
        }
        stmt.close();
        res.close();
        return result;
    }

    public static ArrayList<Seed> calculateSeeds(boolean isSaveToFile, boolean isSaveReadable, String dataBaseFile) throws Exception {
        ArrayList<Integer> types = getAlltypes();
        System.out.println(types.size());
        types.removeAll(subMetaData);
        System.out.println(types.size());
        System.out.println("finish get All types ");
        ArrayList<Seed> seeds = new ArrayList<>(types.size());
        for (int typeId : types) {
            Map<Integer, Integer> nodes = getNodesOfType(typeId);
            System.out.println("finish get Nodes Of Type " + typeId);
            if (nodes.size() < 10) {
                // 若该类型下 节点个数 小于 10 那么不计算 ,不具有代表性
                continue;
            }
            Set<SeedEdge> commonEdges = null;
            for (Integer nodeId : nodes.keySet()) {
                commonEdges = getEdgesOfNode(nodeId, nodes, commonEdges);
            }
            System.out.println("finish cal commonEdges of " + typeId);
            if (commonEdges == null || commonEdges.isEmpty()) {
                continue;
                // 若该类型下 commonEdges个数 小于1 没有公共边 那么不计算 / 不具有代表性
            }

            // 计算 purity
            int nodesNums = nodes.size();
            double purity = 0;
            for (int nodeId : nodes.keySet()) {
                double nodeEdgesNum = nodes.get(nodeId).doubleValue();
                purity += nodeEdgesNum / (commonEdges.size() + nodeEdgesNum);
            }
            purity /= nodesNums;
            purity = (1 - purity) * 2;
            seeds.add(new Seed(typeId, purity, nodesNums, commonEdges));
        }

        Collections.sort(seeds);
        if (isSaveToFile) {
            // 增加jackson 对google guava的支持
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new GuavaModule());

            File seedFile = new File(OUT_PUT_FILE_NAME);
            if (seedFile.exists()) {
                throw new Exception("file already exist");
            } else if (seedFile.createNewFile()) {
                mapper.writeValue(seedFile, seeds);
            } else {
                throw new Exception("create file failed");
            }
        }
        if (isSaveReadable) {
            ArrayList<SeedString> seedStrings = new ArrayList<>(seeds.size());
            for (Seed seed : seeds) {
                seedStrings.add(new SeedString(seed, dataBaseFile));
            }
            // 增加jackson 对google guava的支持
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new GuavaModule());

            File seedFile = new File("READ_" + OUT_PUT_FILE_NAME);
            if (seedFile.exists()) {
                throw new Exception("file already exist");
            } else if (seedFile.createNewFile()) {
                mapper.writeValue(seedFile, seedStrings);
            } else {
                throw new Exception("create file failed");
            }
        }
        return seeds;
    }

    private static List<int[]> generateTriples(int nodeId,
                                               Integer deep,
                                               List<int[]> triples,
                                               Set<Integer> appearedSub,
                                               int maxDeep,
                                               Multimap<Integer, Integer> nodeLabels
    ) throws SQLException {
        // deep controls the deep of recursion ie. the steps when generate triples from one node
        // 使用前 应在函数外 声明一个set<multimap> nodeLabels 用以记录 node 的label
        if (deep > maxDeep) {
            return triples;
        }
        if (triples == null || appearedSub==null) {
            appearedSub = new HashSet<>();
            triples = new ArrayList<>();
        }
        System.out.println("triples.size(): " + triples.size());
        System.out.println("appearedSub.size(): " + appearedSub.size());
        // 查找 第一层节点label label 加入nodeLabels
        Statement stmt = db.createStatement();
        Statement stmt1 = db.createStatement();
        ResultSet res;
        ResultSet res1;
        if (deep.equals(1)) {
            stmt = db.createStatement();
            res = stmt.executeQuery("SELECT type_id FROM \"nodes_type\" WHERE node_id =" + nodeId);
            while (res.next()) {
                nodeLabels.put(nodeId, res.getInt("type_id"));
            }
            res.close();
            stmt.close();
        }
        // 查找 triple List<Integer[]> triples
        res = stmt.executeQuery("SELECT * FROM \"triples_all\" WHERE subject_id =" + nodeId);
        Set<Integer> objects = new HashSet<>();

        while (res.next()) {
            int[] triple = new int[3];
            triple[0] = res.getInt("subject_id");
            triple[1] = res.getInt("predicate_id");
            triple[2] = res.getInt("object_id");
            if (predMetaData.contains(triple[1])) {
                continue;
            }
            Set<Integer> objLabelSet = new HashSet<>();
            res1 = stmt1.executeQuery("SELECT type_id FROM \"nodes_type\" WHERE node_id =" + triple[2]);
            boolean haveType = false;
            while (res1.next()) {
                haveType = true;
                objLabelSet.add(res1.getInt("type_id"));
            }
            res1.close();
            stmt1.close();
            if (!haveType) {
                // 该node为 literal
                res1 = stmt1.executeQuery("SELECT string_type_id FROM \"mapping\" WHERE id =" + triple[2]);
                while (res1.next()) {
                    objLabelSet.add(-res1.getInt("string_type_id"));
                }
            }
            res1.close();
            stmt1.close();
            objLabelSet.removeAll(objMetaData);
            if (objLabelSet.isEmpty()) {
                continue;
            }
            for (int objLabel : objLabelSet) {
                nodeLabels.put(triple[2], objLabel);
            }
            objects.add(triple[2]);
            triples.add(triple);
            appearedSub.add(triple[0]);
        }
        res.close();
        stmt.close();
        for (int object : objects) {
            if (appearedSub.contains(object)) { continue; }
            generateTriples(object, deep + 1, triples, appearedSub, maxDeep, nodeLabels);
        }
        return triples;
    }

    /**
     * 从给定类型 typeID 下 的所有节点 nodeId ，向外搜索n步 生成同类型相关的子图、
     * 存储 typeId / type下的所有 nodeId / id形式的三元组
     *
     * @param maxDeep
     * @param typeId
     * @return
     * @throws SQLException
     */

    public static void extractTypeRelatedGraph(int maxDeep, int typeId,
                                               String filePath,
                                               double sampleRatio) throws Exception {

        // typeId 从 Seed 中得到 ，经过metadata 去除
        List<Integer> originNodes = new ArrayList<>(getNodesOfType(typeId).keySet());
        int sampleNum = (int) (originNodes.size() * sampleRatio);
        Random random = new Random();
        Set<Integer> nodeIndexes = new HashSet<>(sampleNum);
        for (int i = 0; i < sampleNum; i++) {
            int nextNodeIndex = random.nextInt(originNodes.size() - 1);
            while (nodeIndexes.contains(nextNodeIndex)) {
                nextNodeIndex = (nextNodeIndex + 1) % sampleNum;
            }
            nodeIndexes.add(nextNodeIndex);
        }
        Set<Integer> nodes = new HashSet<>(sampleNum);
        for (int index : nodeIndexes) {
            nodes.add(originNodes.get(index));
        }
        List<int[]> triples =  new ArrayList<>();
        Set<Integer> appearedNodes = null;
        Multimap<Integer, Integer> nodeLabels = TreeMultimap.create();
        for (int nodeId : nodes) {
            // 使用前 应在函数外 声明一个set<multimap> nodeLabels 用以记录 node 的label
            triples.addAll(generateTriples(nodeId, 1, triples, appearedNodes, maxDeep, nodeLabels));
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new GuavaModule());
        // 增加jackson 对google guava的支持
        File resultFile = new File(filePath);
        if (resultFile.createNewFile()) {
            mapper.writeValue(resultFile, new TypeRelatedGraph(typeId, nodes, triples, maxDeep, nodeLabels));
        } else {
            throw new Exception("file already exist");
        }
    }

    public static void main(String[] args) throws Exception {
       ArrayList<Seed> seeds = calculateSeeds(true, true, DATA_BASE_PATH);
        double sampleRatio = 1;
        int maxDeep = Integer.parseInt(args[0]);
        for (Seed seed : seeds) {
            String filePath = "D1_" + maxDeep + "P_" + seed.getPurity() + "R_" + sampleRatio + "T_" + seed.getTypeId() + ".json";
            SeedsCalculator.extractTypeRelatedGraph(maxDeep, seed.getTypeId(), filePath, sampleRatio);
            System.out.println("finish " + filePath + " at " + new Date());
        }
        db.close();
    }
}


