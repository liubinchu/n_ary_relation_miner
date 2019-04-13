package top.ericcliu.util;

import java.sql.*;
import java.util.ArrayList;

/**
 * @author liubi
 */
public class DataBaseTools {
    public Connection sqliteConect(String databasePath) {
        Connection db = null;
        try {
            db = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            db.setAutoCommit(false);
            System.out.println("Connect database successfully");
        } catch (Exception e) {
            System.out.println("Connect database failed , check database file path:" + databasePath);
            throw e;
        } finally {
            return db;
        }
    }

    public String printer(Connection db, Integer id) throws Exception {
        // this function used to print the actural string in database which is mapped to an id in memory
        if (id > 0 && id < 16576049) {
            Statement stmt = db.createStatement();
            String sql = "SELECT content FROM mapping WHERE id =" + id.toString();
            ResultSet res = stmt.executeQuery(sql);
            return res.getString("content");
        } else if (id < 0 && id > -24) {
            id = -id;
            Statement stmt = db.createStatement();
            String sql = "SELECT type_string FROM types_string WHERE id =" + id.toString();
            ResultSet res = stmt.executeQuery(sql);
            return res.getString("type_string");
        }
        return "illegal node id";
    }

    public boolean add_types_string(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            add_types_stringSQL(statement, "String,combine 2,9,13,14,19,20");
            add_types_stringSQL(statement, "Time,combine 3,10,22");
            add_types_stringSQL(statement, "Number,combine 4,6,7,8,12,15,18");
            add_types_stringSQL(statement, "Boolean,combine 5");
            add_types_stringSQL(statement, "Web URI, combine 11");
            add_types_stringSQL(statement, "Name, combine 16");
            add_types_stringSQL(statement, "ID, combine 17");
            statement.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean add_types_stringSQL(Statement statement, String content) throws SQLException {
        Integer nextIdtypes_string = statement.executeQuery("select max (id) from types_string").getInt(1) + 1;
        String s = "INSERT INTO types_string (id,type_string) VALUES (" + nextIdtypes_string + ", '" + content + "')";
        statement.executeUpdate(s);
        return true;
    }

    public boolean addType_mapping(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            addType_mappingSQL(statement, "String", 24);
            addType_mappingSQL(statement, "Time", 25);
            addType_mappingSQL(statement, "Number", 26);
            addType_mappingSQL(statement, "Boolean", 27);
            addType_mappingSQL(statement, "Web URI", 28);
            addType_mappingSQL(statement, "Name", 29);
            addType_mappingSQL(statement, "ID", 30);
            statement.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean addType_mappingSQL(Statement statement, String content, Integer string_type_id) throws SQLException {
        Integer nextIdtypes_string = statement.executeQuery("select max (id) from mapping").getInt(1) + 1;
        String s = "INSERT INTO mapping (id,content,string_type_id) VALUES ("
                + nextIdtypes_string
                + ", '"
                + content
                + "',"
                + string_type_id
                + ")";
        //System.out.println(s);
        statement.executeUpdate(s);
        return true;
    }

    public boolean addType_types_node(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            addType_types_nodeSQL(statement, 16576049);
            addType_types_nodeSQL(statement, 16576050);
            addType_types_nodeSQL(statement, 16576051);
            addType_types_nodeSQL(statement, 16576052);
            addType_types_nodeSQL(statement, 16576053);
            addType_types_nodeSQL(statement, 16576054);
            addType_types_nodeSQL(statement, 16576055);
            statement.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean addType_types_nodeSQL(Statement statement, Integer types_id) throws SQLException {
        Integer nextIdtypes_string = statement.executeQuery("select max (id) from types_node").getInt(1) + 1;
        String s = "INSERT INTO types_node (id,types_id) VALUES ("
                + nextIdtypes_string
                + ", "
                + types_id
                + ")";
        //System.out.println(s);
        statement.executeUpdate(s);
        return true;
    }

    private ArrayList<Integer> selelctNodes(Connection connection, int[] nodes) throws Exception {
        Statement statement = connection.createStatement();
        if (nodes == null || nodes.length == 0) {
            throw new Exception("nodes is empty or null");
        }
        StringBuilder s = new StringBuilder("SELECT id from mapping where string_type_id = ");
        s.append(nodes[0]);
        for (int i = 1; i < nodes.length; i++) {
            s.append(" or string_type_id =");
            s.append(nodes[i]);
        }
        s.append(";");
        System.out.println(s.toString());
        ResultSet resultSet = statement.executeQuery(s.toString());
        ArrayList<Integer> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(resultSet.getInt("id"));
        }
        System.out.println(result);
        return result;
    }

    public boolean addNodeType_triples_all(Connection connection, Integer nodeId, Integer typeId) {
        try {
            Statement statement = connection.createStatement();
            addNodeType_triples_allSQL(statement, nodeId, typeId);
            statement.close();
            connection.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean addNodeType_triples_allSQL(Statement statement, Integer nodeId, Integer typeId) throws SQLException {
        Integer nextId = statement.executeQuery("select max (id) from triples_all").getInt(1) + 1;
        String s = "INSERT INTO triples_all (id,subject_id,predicate_id,object_id) VALUES ("
                + nextId + ", "
                + nodeId + ", "
                + 4 + ", "
                + typeId
                + ")";
        System.out.println(s);
        statement.executeUpdate(s);
        return true;
    }

    public boolean addNodeType_nodes_type(Connection connection, Integer nodeId, Integer typeId) {
        try {
            Statement statement = connection.createStatement();
            addNodeType_nodes_typeSQL(statement, nodeId, typeId);
            statement.close();
            connection.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean addNodeType_nodes_typeSQL(Statement statement, Integer nodeId, Integer typeId) throws SQLException {
        Integer nextId = statement.executeQuery("select max (id) from nodes_type").getInt(1) + 1;
        String s = "INSERT INTO nodes_type (id,node_id,type_id) VALUES ("
                + nextId + ", "
                + nodeId + ", "
                + typeId
                + ")";
        System.out.println(s);
        statement.executeUpdate(s);
        return true;
    }

    /**
     * "2013-05-07T10:03:15Z"^^http://www.w3.org/2001/XMLSchema#dateTime 存在这样的数据 需要将其 拆分 得到标签
     * @return
     */
    public boolean seperateTriples(DataBaseTools dataBaseTools ,Connection readOnly, Connection readWrite) throws SQLException {
        try {
            dataBaseTools.add_types_string(readWrite);
            dataBaseTools.addType_mapping(readWrite);
            dataBaseTools.addType_types_node(readWrite);
            ArrayList<Integer> nodesString = dataBaseTools.selelctNodes(readOnly, new int[]{2, 9, 13, 14, 19, 20});
            for(Integer nodeString :nodesString){
                dataBaseTools.addNodeType_triples_all(readWrite, nodeString, 16576049);
                dataBaseTools.addNodeType_nodes_type(readWrite, nodeString, 16576049);
            }
            ArrayList<Integer> nodesTime = dataBaseTools.selelctNodes(readOnly, new int[]{3, 10, 22});
            for(Integer nodeTime :nodesTime){
                dataBaseTools.addNodeType_triples_all(readWrite, nodeTime, 16576050);
                dataBaseTools.addNodeType_nodes_type(readWrite, nodeTime, 16576050);
            }
            ArrayList<Integer> nodesNumber = dataBaseTools.selelctNodes(readOnly, new int[]{4,6,7,8,12,15,18});
            for(Integer nodeNumber :nodesNumber){
                dataBaseTools.addNodeType_triples_all(readWrite, nodeNumber, 16576051);
                dataBaseTools.addNodeType_nodes_type(readWrite, nodeNumber, 16576051);
            }
            ArrayList<Integer> nodesBoolean = dataBaseTools.selelctNodes(readOnly, new int[]{5});
            for(Integer nodeBoolean :nodesBoolean){
                dataBaseTools.addNodeType_triples_all(readWrite, nodeBoolean, 16576052);
                dataBaseTools.addNodeType_nodes_type(readWrite, nodeBoolean, 16576052);
            }
            ArrayList<Integer> nodesWebURI = dataBaseTools.selelctNodes(readOnly, new int[]{11});
            for(Integer nodeWebURI :nodesWebURI){
                dataBaseTools.addNodeType_triples_all(readWrite, nodeWebURI, 16576053);
                dataBaseTools.addNodeType_nodes_type(readWrite, nodeWebURI, 16576053);
            }
            ArrayList<Integer> nodesName = dataBaseTools.selelctNodes(readOnly, new int[]{16});
            for(Integer nodeName :nodesName){
                dataBaseTools.addNodeType_triples_all(readWrite, nodeName, 16576054);
                dataBaseTools.addNodeType_nodes_type(readWrite, nodeName, 16576054);
            }
            ArrayList<Integer> nodesID = dataBaseTools.selelctNodes(readOnly, new int[]{17});
            for(Integer nodeID :nodesID){
                dataBaseTools.addNodeType_triples_all(readWrite, nodeID, 16576055);
                dataBaseTools.addNodeType_nodes_type(readWrite, nodeID, 16576055);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return  false;
        } finally {
            readOnly.close();
            readWrite.close();
        }
    }


    public static void main(String[] args) throws SQLException {
        DataBaseTools dataBaseTools = new DataBaseTools();
        Connection dbC = dataBaseTools.sqliteConect("C:\\bioportal_full.sqlite");  // disk C  readonly for Java
        Connection dbD = dataBaseTools.sqliteConect("D:\\bioportal.sqlite");
        try {
            //dataBaseTools.add_types_string(dbC);
            //dataBaseTools.addType_mapping(dbC);
            // dataBaseTools.addType_types_node(dbC);
            ArrayList<Integer> nodesString = dataBaseTools.selelctNodes(dbC, new int[]{2, 9, 13, 14, 19, 20});
            for(Integer nodeString :nodesString){
                dataBaseTools.addNodeType_triples_all(dbD, nodeString, 16576049);
                dataBaseTools.addNodeType_nodes_type(dbD, nodeString, 16576049);
            }
            ArrayList<Integer> nodesTime = dataBaseTools.selelctNodes(dbC, new int[]{3, 10, 22});
            for(Integer nodeTime :nodesTime){
                dataBaseTools.addNodeType_triples_all(dbD, nodeTime, 16576050);
                dataBaseTools.addNodeType_nodes_type(dbD, nodeTime, 16576050);
            }
            ArrayList<Integer> nodesNumber = dataBaseTools.selelctNodes(dbC, new int[]{4,6,7,8,12,15,18});
            for(Integer nodeNumber :nodesNumber){
                dataBaseTools.addNodeType_triples_all(dbD, nodeNumber, 16576051);
                dataBaseTools.addNodeType_nodes_type(dbD, nodeNumber, 16576051);
            }
            ArrayList<Integer> nodesBoolean = dataBaseTools.selelctNodes(dbC, new int[]{5});
            for(Integer nodeBoolean :nodesBoolean){
                dataBaseTools.addNodeType_triples_all(dbD, nodeBoolean, 16576052);
                dataBaseTools.addNodeType_nodes_type(dbD, nodeBoolean, 16576052);
            }
            ArrayList<Integer> nodesWebURI = dataBaseTools.selelctNodes(dbC, new int[]{11});
            for(Integer nodeWebURI :nodesWebURI){
                dataBaseTools.addNodeType_triples_all(dbD, nodeWebURI, 16576053);
                dataBaseTools.addNodeType_nodes_type(dbD, nodeWebURI, 16576053);
            }
            ArrayList<Integer> nodesName = dataBaseTools.selelctNodes(dbC, new int[]{16});
            for(Integer nodeName :nodesName){
                dataBaseTools.addNodeType_triples_all(dbD, nodeName, 16576054);
                dataBaseTools.addNodeType_nodes_type(dbD, nodeName, 16576054);
            }
            ArrayList<Integer> nodesID = dataBaseTools.selelctNodes(dbC, new int[]{17});
            for(Integer nodeID :nodesID){
                dataBaseTools.addNodeType_triples_all(dbD, nodeID, 16576055);
                dataBaseTools.addNodeType_nodes_type(dbD, nodeID, 16576055);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbC.close();
            dbD.close();
        }
    }
}
