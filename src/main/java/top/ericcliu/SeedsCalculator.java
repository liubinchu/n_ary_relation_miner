package top.ericcliu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import top.ericcliu.util.*;

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

    private static Set<SeedEdge> getEdgesOfNode(Connection db,
                                                Integer nodeId,
                                                Map<Integer, Integer> nodes,
                                                Set<SeedEdge> commonEdges,
                                                Set<Integer> predicateRemove,
                                                Set<Integer> objLabelRemove) throws SQLException {
        // 1 .get all edges of one node  2.calculate the union set of these edges from different edges
        Set<SeedEdge> edges = new HashSet<>();
        Statement stmt0 = db.createStatement();
        ResultSet res0 = stmt0.executeQuery("SELECT DISTINCT * FROM \"triples_all\" WHERE subject_id =" + nodeId.toString());
        while (res0.next()) {
            Integer edgeLabel = res0.getInt("predicate_id");
            if(predicateRemove.contains(edgeLabel)){
                continue;
            }
            Integer nodeBId = res0.getInt("object_id");
            Statement stmt1 = db.createStatement();
            ResultSet res1 = stmt1.executeQuery("SELECT DISTINCT type_id FROM \"nodes_type\" WHERE node_id = "+ nodeBId.toString());
            Set<Integer> nodeBLabel = new HashSet<>();
            boolean haveType = false;
            while (res1.next()){
                haveType = true;
                Integer objLabel = res1.getInt("type_id");
                if(objLabelRemove.contains(objLabel)){
                    res1.close();
                    stmt1.close();
                    continue;
                }
                nodeBLabel.add(objLabel);
            }
            if (!haveType) {
                // 该node为 literal
                res1 = stmt1.executeQuery("SELECT string_type_id FROM \"mapping\" WHERE id =" + nodeBId.toString());
                while (res1.next()) {
                    nodeBLabel.add(-res1.getInt("string_type_id"));
                }
            }
            if(nodeBLabel.isEmpty()){
                res1.close();
                stmt1.close();
                continue;
            }
            res1.close();
            stmt1.close();
            edges.add(new SeedEdge(edgeLabel,nodeBLabel));
        }
        nodes.put(nodeId,edges.size());
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

    public static ArrayList<Seed> calculateSeeds(Connection db, String outputFilePath, boolean isSaveToFile, boolean isSaveReadable, String dataBaseFile) throws Exception {
        ArrayList<Integer> types = getAlltypes(db);
        // 需要去除的类型 ie 主语
        //ArrayList<Integer> idContainsBio2RdfP = getIDs(db, "SELECT id FROM mapping WHERE content LIKE \"http://bio2rdf.org/bio2rdf_vocabulary:%\"");
        ArrayList<Integer> idContainsW3 = getIDs(db, "SELECT id FROM mapping WHERE content LIKE \"http://www.w3.org/%\"");
        ArrayList<Integer> idOfTMO = getIDs(db, "SELECT id FROM mapping WHERE content LIKE \"http://www.w3.org/2001/sw/hcls/ns/transmed/TMO%\"");
        idContainsW3.removeAll(idOfTMO);
        ArrayList<Integer> idOfPurlTerms = getIDs(db, "SELECT id FROM mapping WHERE content LIKE \"http://purl.org/dc/terms%\"");
        types.removeAll(getIDs(db, "SELECT id FROM mapping WHERE content = \"http://bio2rdf.org/zfin_vocabulary:Resource\""));
        types.removeAll(getIDs(db, "select id from mapping where content like \"%ataset\""));
        types.removeAll(getIDs(db, "select id from mapping where content like \"%identifier\""));
        types.removeAll(getIDs(db, "select id from mapping where content like \"%identifiers.org\""));
        types.removeAll(getIDs(db, "select id from mapping where content like \"%namespace\""));
        types.removeAll(getIDs(db, "select id from mapping where content like \"http://bio2rdf.org/bio2rdf_vocabulary:%\""));
        ArrayList<Integer> comment = getIDs(db, "select id from mapping where content like \"%comment\"");
        comment.removeAll(getIDs(db, "select id from mapping where content like \"http://proteomeontology.org%comment\""));
        comment.removeAll(getIDs(db, "select id from mapping where content like \"http://semantic-dicom.org%comment\""));
        types.removeAll(comment);
        types.removeAll(getIDs(db, "select id from mapping where content like \"%isDefinedBy\""));
        types.removeAll(getIDs(db, "select id from mapping where content like \"%label\""));
        types.removeAll(getIDs(db, "select id from mapping where content like \"%created_by\""));
        types.removeAll(getIDs(db, "select id from mapping where content like \"%creation_date\""));
        types.removeAll(getIDs(db, "select id from mapping where content like \"%sameAs\""));
        types.removeAll(getIDs(db, "select id from mapping where content like \"%description\""));
        types.removeAll(idContainsW3);
        types.removeAll(idOfPurlTerms);

        Set<Integer> predicateRemove = new HashSet<>(getIDs(db, "SELECT id FROM mapping WHERE content = \"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\""));
        predicateRemove.addAll(getIDs(db, "SELECT id FROM mapping WHERE content = \"http://www.w3.org/2000/01/rdf-schema#subClassOf\""));
        predicateRemove.addAll(getIDs(db, "SELECT id FROM mapping where content like \"%Dataset\""));
        predicateRemove.addAll(getIDs(db, "SELECT id FROM mapping where content like \"http://www.w3.org/2002/07/%Class\""));
        predicateRemove.addAll(getIDs(db, "SELECT id FROM mapping where content like \"%sameAs\""));
        predicateRemove.addAll(getIDs(db, "SELECT id FROM mapping where content like \"http://www.w3.org/2002/07/owl#Thing\""));

        Set<Integer> objLabelRemove = new HashSet<>(getIDs(db, "SELECT * FROM mapping WHERE content = \"http://bio2rdf.org/bio2rdf_vocabulary:namespace\""));
        objLabelRemove.addAll(getIDs(db, "SELECT id FROM mapping where content like \"http://bio2rdf.org%Resource\""));
        objLabelRemove.addAll(getIDs(db, "SELECT id FROM mapping where content like \"%Dataset\""));
        objLabelRemove.addAll(getIDs(db, "SELECT id FROM mapping where content like \"http://who.int/icd#%DefinitionTerm\" or content like \"http://who.int/icd#ClamlReference\" or content like \"http://who.int/icd#TitleTerm\" or content like \"http://who.int/icd#InclusionTerm\""));
        objLabelRemove.addAll(getIDs(db, "SELECT id FROM mapping where content like \"%Dataset\""));

        ArrayList<Seed> seeds = new ArrayList<>(types.size());
        for (Integer typeId : types) {
            Map<Integer, Integer> nodes = getNodesOfType(db, typeId);
            if (nodes.size() < 3) { continue; }
            // 若该类型下 节点个数 小于10 那么不计算 ,不具有代表性

            Set<SeedEdge> commonEdges = null;
            for (Integer nodeId : nodes.keySet()) {
                commonEdges = getEdgesOfNode(db, nodeId, nodes, commonEdges,predicateRemove,objLabelRemove);
            }

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
            purity = (1-purity)*2;
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
        if(isSaveReadable){
            ArrayList<SeedString> seedStrings = new ArrayList<>(seeds.size());
            for(Seed seed :seeds){
                seedStrings.add(new SeedString(seed,dataBaseFile));
            }
            // 增加jackson 对google guava的支持
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new GuavaModule());

            File seedFile = new File("READ_"+outputFilePath);
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

    public static void main(String[] args) throws Exception {
/*        double sampleRatio = Double.parseDouble(args[0]);*/
        double sampleRatio = 1;
        DataBaseTools dataBaseTools = new DataBaseTools();
        String dataBaseFile = "C:\\bioportal_full.sqlite";
        Connection db = dataBaseTools.sqliteConect(dataBaseFile);
/*        Connection db = dataBaseTools.sqliteConect("bioportal.sqlite");*/
        //cal seeds
        String outPutFileName = "seeds1.json";
        ArrayList<Seed> seeds = calculateSeeds(db, outPutFileName, false,true,dataBaseFile);
        //extract typeRelatedGraph
/*        Integer maxDeep = 10;
        for (Seed seed : seeds) {
            String filePath = "P_"+seed.getPurity()+"R_" + sampleRatio + "T_" + seed.getTypeId();
            TypeRelatedGraph typeRelatedGraph = TypeRelatedGraph.extractTypeRelatedGraph(db, maxDeep, seed.getTypeId(), filePath, true, sampleRatio);
        }*/
        db.close();
    }
}


