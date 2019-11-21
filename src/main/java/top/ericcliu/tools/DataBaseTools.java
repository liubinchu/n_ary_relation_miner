package top.ericcliu.tools;

import lombok.extern.log4j.Log4j2;

import java.sql.*;
import java.util.ArrayList;

/**
 * @author liubi
 * id <0 时
 * -24 < id < 0 其对应正数，为 types_strings 表的内容
 * id = -24 时 表示 replaced id
 */
@Log4j2
public class DataBaseTools {
    public Connection sqliteConect(String databasePath) {
        Connection db = null;
        try {
            db = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            db.setAutoCommit(false);
        } catch (Exception e) {
            log.error("Connect database failed , check database file path:" + databasePath);
            log.error(e.getMessage());
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

    public static boolean add_types_string(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            add_types_stringSQL(statement, "String,combine 2,9,13,14,19,20");
            connection.commit();
            add_types_stringSQL(statement, "Time,combine 3,10,22");
            connection.commit();
            add_types_stringSQL(statement, "Number,combine 4,6,7,8,12,15,18");
            connection.commit();
            add_types_stringSQL(statement, "Boolean,combine 5");
            connection.commit();
            add_types_stringSQL(statement, "Web URI, combine 11");
            connection.commit();
            add_types_stringSQL(statement, "Name, combine 16");
            connection.commit();
            add_types_stringSQL(statement, "ID, combine 17");
            connection.commit();
            statement.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean add_types_stringSQL(Statement statement, String content) throws SQLException {
        Integer nextIdtypes_string = statement.executeQuery("select max (id) from types_string").getInt(1) + 1;
        String s = "INSERT INTO types_string (id,type_string) VALUES (" + nextIdtypes_string + ", '" + content + "')";
        statement.executeUpdate(s);
        log.info(s);
        return true;
    }

    public static boolean addType_mapping(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            addType_mappingSQL(statement, "String", 24);
            connection.commit();
            addType_mappingSQL(statement, "Time", 25);
            connection.commit();
            addType_mappingSQL(statement, "Number", 26);
            connection.commit();
            addType_mappingSQL(statement, "Boolean", 27);
            connection.commit();
            addType_mappingSQL(statement, "Web URI", 28);
            connection.commit();
            addType_mappingSQL(statement, "Name", 29);
            connection.commit();
            addType_mappingSQL(statement, "ID", 30);
            connection.commit();
            statement.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean addType_mappingSQL(Statement statement, String content, Integer string_type_id) throws SQLException {
        Integer nextIdtypes_string = statement.executeQuery("select max (id) from mapping").getInt(1) + 1;
        String s = "INSERT INTO mapping (id,content,string_type_id) VALUES ("
                + nextIdtypes_string
                + ", '"
                + content
                + "',"
                + string_type_id
                + ")";
        statement.executeUpdate(s);
        log.info(s);
        return true;
    }

    public static boolean addType_types_node(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            addType_types_nodeSQL(statement, 16576049);
            connection.commit();
            addType_types_nodeSQL(statement, 16576050);
            connection.commit();
            addType_types_nodeSQL(statement, 16576051);
            connection.commit();
            addType_types_nodeSQL(statement, 16576052);
            connection.commit();
            addType_types_nodeSQL(statement, 16576053);
            connection.commit();
            addType_types_nodeSQL(statement, 16576054);
            connection.commit();
            addType_types_nodeSQL(statement, 16576055);
            connection.commit();
            statement.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean addType_types_nodeSQL(Statement statement, Integer types_id) throws SQLException {
        Integer nextIdtypes_string = statement.executeQuery("select max (id) from types_node").getInt(1) + 1;
        String s = "INSERT INTO types_node (id,types_id) VALUES ("
                + nextIdtypes_string
                + ", "
                + types_id
                + ")";
        statement.executeUpdate(s);
        log.info(s);
        return true;
    }

    private static ArrayList<Integer> selelctNodes(Connection connection, int[] nodes) throws Exception {
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
        ResultSet resultSet = statement.executeQuery(s.toString());

        ArrayList<Integer> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(resultSet.getInt("id"));
        }
        log.info(s.toString());
        log.info(result);
        return result;
    }

    public static boolean addNodeType_triples_all(Connection connection, Integer nodeId, Integer typeId) {
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

    private static boolean addNodeType_triples_allSQL(Statement statement, Integer nodeId, Integer typeId) throws SQLException {
        Integer nextId = statement.executeQuery("select max (id) from triples_all").getInt(1) + 1;
        String s = "INSERT INTO triples_all (id,subject_id,predicate_id,object_id) VALUES ("
                + nextId + ", "
                + nodeId + ", "
                + 4 + ", "
                + typeId
                + ")";
        statement.executeUpdate(s);
        log.info(s);
        return true;
    }

    public static boolean addNodeType_nodes_type(Connection connection, Integer nodeId, Integer typeId) {
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

    private static boolean addNodeType_nodes_typeSQL(Statement statement, Integer nodeId, Integer typeId) throws SQLException {
        Integer nextId = statement.executeQuery("select max (id) from nodes_type").getInt(1) + 1;
        String s = "INSERT INTO nodes_type (id,node_id,type_id) VALUES ("
                + nextId + ", "
                + nodeId + ", "
                + typeId
                + ")";
        statement.executeUpdate(s);
        log.info(s);
        return true;
    }

    /**
     * "2013-05-07T10:03:15Z"^^http://www.w3.org/2001/XMLSchema#dateTime 存在这样的数据 需要将其 拆分 得到标签
     *
     * @return
     */
    public static boolean seperateTriples(Connection readOnly, Connection readWrite) throws SQLException {
        try {
            add_types_string(readWrite);
            addType_mapping(readWrite);
            addType_types_node(readWrite);
            ArrayList<Integer> nodesString = selelctNodes(readOnly, new int[]{2, 9, 13, 14, 19, 20});
            for (Integer nodeString : nodesString) {
                addNodeType_triples_all(readWrite, nodeString, 16576049);
                addNodeType_nodes_type(readWrite, nodeString, 16576049);
            }
            ArrayList<Integer> nodesTime = selelctNodes(readOnly, new int[]{3, 10, 22});
            for (Integer nodeTime : nodesTime) {
                addNodeType_triples_all(readWrite, nodeTime, 16576050);
                addNodeType_nodes_type(readWrite, nodeTime, 16576050);
            }
            ArrayList<Integer> nodesNumber = selelctNodes(readOnly, new int[]{4, 6, 7, 8, 12, 15, 18});
            for (Integer nodeNumber : nodesNumber) {
                addNodeType_triples_all(readWrite, nodeNumber, 16576051);
                addNodeType_nodes_type(readWrite, nodeNumber, 16576051);
            }
            ArrayList<Integer> nodesBoolean = selelctNodes(readOnly, new int[]{5});
            for (Integer nodeBoolean : nodesBoolean) {
                addNodeType_triples_all(readWrite, nodeBoolean, 16576052);
                addNodeType_nodes_type(readWrite, nodeBoolean, 16576052);
            }
            ArrayList<Integer> nodesWebURI = selelctNodes(readOnly, new int[]{11});
            for (Integer nodeWebURI : nodesWebURI) {
                addNodeType_triples_all(readWrite, nodeWebURI, 16576053);
                addNodeType_nodes_type(readWrite, nodeWebURI, 16576053);
            }
            ArrayList<Integer> nodesName = selelctNodes(readOnly, new int[]{16});
            for (Integer nodeName : nodesName) {
                addNodeType_triples_all(readWrite, nodeName, 16576054);
                addNodeType_nodes_type(readWrite, nodeName, 16576054);
            }
            ArrayList<Integer> nodesID = selelctNodes(readOnly, new int[]{17});
            for (Integer nodeID : nodesID) {
                addNodeType_triples_all(readWrite, nodeID, 16576055);
                addNodeType_nodes_type(readWrite, nodeID, 16576055);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            readOnly.close();
            readWrite.close();
        }
    }


    public static void main(String[] args) throws SQLException {
        String dbPath = args[0];
        DataBaseTools dataBaseTools = new DataBaseTools();
        Connection db = dataBaseTools.sqliteConect(dbPath);
        //Connection db = dataBaseTools.sqliteConect("/home/lbc/disk/bioportal_sep.sqlite");
        // disk C  readonly for Java
        try {
            seperateTriples(db, db);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.commit();
            db.close();
        }
    }
}
