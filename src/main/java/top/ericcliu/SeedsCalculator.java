package top.ericcliu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import top.ericcliu.util.DataBaseTools;
import top.ericcliu.util.Seed;
import top.ericcliu.util.TypeRelatedGraph;

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

    private static ArrayList<Integer> getAlltypes(Connection db) throws SQLException {
        Statement stmt = db.createStatement();
        String sql = "SELECT COUNT(*) FROM \"types_node\" ";
        ResultSet res = stmt.executeQuery(sql);
        Integer typesNum = res.getInt("COUNT(*)");
        System.out.println("types num : " + typesNum.toString());
        ArrayList<Integer> types = new ArrayList<>(typesNum);
        res.close();
        stmt.close();
        sql = "SELECT * FROM \"types_node\" ";
        res = stmt.executeQuery(sql);
        while (res.next()) {
            types.add(res.getInt("types_id"));
        }
        res.close();
        stmt.close();
        return types;
    }

    private static Map<Integer, Integer> getNodesOfType(Connection db, Integer typeId) throws SQLException {
        // firstElement = nodeId ;second element = edgeNums of this node , initial with -1 // means uncalculated
        Statement stmt = db.createStatement();
        String sql = "SELECT COUNT(DISTINCT node_id) FROM \"nodes_type\" WHERE type_id =" + typeId.toString();
        ResultSet res = stmt.executeQuery(sql);
        Integer typesNum = res.getInt("COUNT(DISTINCT node_id)");
        Map<Integer, Integer> nodes = new HashMap<Integer, Integer>(typesNum);
        res.close();
        stmt.close();

        sql = "SELECT DISTINCT node_id FROM \"nodes_type\" WHERE type_id =" + typeId.toString();
        res = stmt.executeQuery(sql);
        while (res.next()) {
            nodes.put(res.getInt("node_id"), -1);
        }
        res.close();
        stmt.close();
        return nodes;
    }

    private static Set<Integer> getEdgesOfNode(Connection db, Integer nodeId, Map<Integer, Integer> nodes, Set<Integer> commonEdges) throws SQLException {
        // 1 .get all edges of one node  2.calculate the union set of these edges from different edges
        Statement stmt = db.createStatement();
        String sql = "SELECT COUNT(DISTINCT predicate_id) FROM \"triples_all\" WHERE subject_id =" + nodeId.toString();
        ResultSet res = stmt.executeQuery(sql);
        Integer edgeNum = res.getInt("COUNT(DISTINCT predicate_id)");
        //nodes.replace(nodeId, -1, edgeNum);
        nodes.put(nodeId,edgeNum);
        Set<Integer> edges = new HashSet<>(edgeNum);
        res.close();
        sql = "SELECT DISTINCT predicate_id FROM \"triples_all\" WHERE subject_id =" + nodeId.toString();
        res = stmt.executeQuery(sql);
        while (res.next()) {
            edges.add(res.getInt("predicate_id"));
        }
        res.close();
        stmt.close();
        if (commonEdges == null) {
            // 公共边集为空，当前元素是该类型下计算的第一个元素
            commonEdges = new HashSet<>();
            commonEdges.addAll(edges);
            return commonEdges;
        } else {
            commonEdges.retainAll(edges);
            return commonEdges;
        }
    }

    private static ArrayList<Integer> getIDs(Connection db, String sql) throws SQLException {
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

    public static ArrayList<Seed> calculateSeeds(Connection db, String outputFilePath, boolean isSaveToFile) throws Exception {
        ArrayList<Integer> types = getAlltypes(db);
        // 元数据
        String idOfW3Sql = "SELECT id FROM mapping WHERE content LIKE \"http://www.w3.org/%\"";
        String idOfPurlTermsSql = "SELECT * FROM mapping WHERE content LIKE \"http://purl.org/dc/terms%\"";
        // useful
        String idOfTMOSql = "SELECT id FROM mapping WHERE content LIKE \"http://www.w3.org/2001/sw/hcls/ns/transmed/TMO%\"";
        ArrayList<Integer> idContainsW3 = getIDs(db, idOfW3Sql);
        ArrayList<Integer> idOfTMO = getIDs(db, idOfTMOSql);
        idContainsW3.removeAll(idOfTMO);
        ArrayList<Integer> idOfPurlTerms = getIDs(db, idOfPurlTermsSql);
        types.removeAll(idContainsW3);
        types.removeAll(idOfPurlTerms);
        ArrayList<Seed> seeds = new ArrayList<>(types.size());
        for (Integer typeId : types) {
            Map<Integer, Integer> nodes = getNodesOfType(db, typeId);
/*            if (nodes.size() < 10) {
                continue;
                //若该类型下 节点个数 小于10 那么不计算 / 不具有代表性
            }*/
            Set<Integer> commonEdges = null;
            for (Integer nodeId : nodes.keySet()) {
                commonEdges = getEdgesOfNode(db, nodeId, nodes, commonEdges);
            }
            String bio2rdfVocabularySql = "SELECT id FROM mapping WHERE content LIKE \"http://bio2rdf.org/bio2rdf_vocabulary:%\"";
            ArrayList<Integer> idContainsBio2RdfP = getIDs(db, bio2rdfVocabularySql);
            //  id whose content contains http://bio2rdf.org/bio2rdf_vocabulary
/*            commonEdges.removeAll(idOfPurlTerms);
            commonEdges.removeAll(idContainsBio2RdfP);
            commonEdges.removeAll(idContainsW3);
            commonEdges.remove(4);*/
            // remove http://www.w3.org/1999/02/22-rdf-syntax-ns#type
            if (commonEdges.size() < 1) {
                continue;
                // 若该类型下 commonEdges个数 小于1 没有公共边 那么不计算 / 不具有代表性
            }
            Integer nodesNums = nodes.size();
            //k
            double purity = 0;
            for (Integer nodeId : nodes.keySet()) {
                Double nodeEdgesNum = nodes.get(nodeId).doubleValue();
                // n_i
                purity += nodeEdgesNum / (commonEdges.size() + nodeEdgesNum);
            }
            purity /= nodesNums;
            seeds.add(new Seed(typeId, purity, nodesNums, commonEdges));
        }
        Collections.sort(seeds);
        if (isSaveToFile) {
            // 增加jackson 对google guava的支持
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new GuavaModule());

            File seedFile = new File(outputFilePath);
            if (seedFile.exists()) {
                throw new Exception("file already exist");
            } else if (seedFile.createNewFile()) {
                mapper.writeValue(seedFile, seeds);
            } else {
                throw new Exception("create file failed");
            }
        }
        return seeds;
    }

    public static void main(String[] args) throws Exception {
/*        double sampleRatio = Double.parseDouble(args[0]);*/
        double sampleRatio = 1;
        DataBaseTools dataBaseTools = new DataBaseTools();
        Connection db = dataBaseTools.sqliteConect("C:\\bioportal_full.sqlite");
/*        Connection db = dataBaseTools.sqliteConect("bioportal.sqlite");*/
        // cal seeds
        String outPutFileName = "seeds.json";
        ArrayList<Seed> seeds = calculateSeeds(db, outPutFileName, true);
        // extract typeRelatedGraph
/*        Integer maxDeep = 10;
        for (Seed seed : seeds) {
            String filePath = "P_"+seed.getPurity()+"R_" + sampleRatio + "T_" + seed.getTypeId();
            TypeRelatedGraph typeRelatedGraph = TypeRelatedGraph.extractTypeRelatedGraph(db, maxDeep, seed.getTypeId(), filePath, true, sampleRatio);
        }*/
        db.close();
    }
}


