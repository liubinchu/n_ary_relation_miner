package top.ericcliu.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * @author liubi
 * @date 2019-01-19 20:15
 **/
public class DFScode implements Cloneable {
    /**
     * -1 未计算
     */
    private Integer rootNodeId = -1;
    private Integer MNI = -1;
    private Double relatedRatio = -1.0;
    private Integer instanceNum = -1;
    private Integer maxNodeId = -1;
    /**
     * 不重复的根节点的个数
     */
    private Integer rootNodeNum = -1;
    /**
     * 数据图共具有n个不同的rootNode，该模式具有m个不同的rootNode
     * rootNodeRatio = m/n
     */
    private Double rootNodeRatio = -1.0;

    /**
     * 边的集合，边的排序代表着边的添加次序
     */
    private ArrayList<GSpanEdge> edgeSeq;
    /**
     * key : nodes appeared in this DFS code, ie nodeId in DFScode, having no relation with dataGraph
     * value : node label of this node in DFS code
     */
    private Map<Integer, Integer> nodeLabelMap;


    public DFScode addEdge(GSpanEdge edge) throws CloneNotSupportedException {
        // 还需要 判断加入的边是否合法
        if (this.edgeSeq == null) {
            this.edgeSeq = new ArrayList<>();
        }
        if (this.nodeLabelMap == null) {
            this.nodeLabelMap = new HashMap<>();
        }

        this.edgeSeq.add(((GSpanEdge) edge.clone()));
        Integer nodeA = edge.getNodeA();
        Integer nodeB = edge.getNodeB();
        this.nodeLabelMap.put(nodeA, edge.getLabelA());
        this.nodeLabelMap.put(nodeB, edge.getLabelB());
        this.maxNodeId = nodeA > nodeB ? nodeA : nodeB;
        return this;
    }

    public DFScode(GSpanEdge edge) throws CloneNotSupportedException {
        this.edgeSeq = new ArrayList<>();
        this.nodeLabelMap = new HashMap<>();
        this.edgeSeq.add(((GSpanEdge) edge.clone()));
        Integer nodeA = edge.getNodeA();
        Integer nodeB = edge.getNodeB();
        this.nodeLabelMap.put(nodeA, edge.getLabelA());
        this.nodeLabelMap.put(nodeB, edge.getLabelB());
        this.maxNodeId = nodeA > nodeB ? nodeA : nodeB;
        this.rootNodeId = edge.getLabelA();
    }

    public DFScode(DFScodeJson dfScodeJson) {
        this.rootNodeId = dfScodeJson.getRootNodeId();
        this.MNI = dfScodeJson.getMNI();
        this.relatedRatio = dfScodeJson.getRelatedRatio();
        this.instanceNum = dfScodeJson.getInstanceNum();

        this.maxNodeId = dfScodeJson.getMaxNodeId();

        ObjectMapper mapper = new ObjectMapper();
        this.edgeSeq = new ArrayList<>(dfScodeJson.getEdgeSeq().size());
        for (Object object : dfScodeJson.getEdgeSeq()) {
            this.edgeSeq.add(mapper.convertValue(object, GSpanEdge.class));
        }
        this.nodeLabelMap = new TreeMap<>(dfScodeJson.getNodeLabelMap());
        this.rootNodeNum = dfScodeJson.getRootNodeNum();
        this.rootNodeRatio = dfScodeJson.getRootNodeRatio();
    }

    public DFScode() {
        this.edgeSeq = new ArrayList<>();
        this.nodeLabelMap = new HashMap<>();
    }

    public DFScode(ArrayList<GSpanEdge> edgeSeq) {
        this.edgeSeq = new ArrayList<>(edgeSeq);
        this.nodeLabelMap = new HashMap<>();

        int minNodeId = Integer.MAX_VALUE;
        for (GSpanEdge edge : edgeSeq) {
            int nodeA = edge.getNodeA();
            int nodeB = edge.getNodeB();
            this.nodeLabelMap.put(nodeA, edge.getLabelA());
            this.nodeLabelMap.put(nodeB, edge.getLabelB());
            this.maxNodeId = Math.max(this.maxNodeId, nodeA);
            this.maxNodeId = Math.max(this.maxNodeId, nodeB);
            minNodeId = Math.min(minNodeId, nodeA);
            minNodeId = Math.min(minNodeId, nodeB);
        }
        this.rootNodeId = fetchNodeLabel(minNodeId);
    }

    public DFScode (DFScode dfScode){
        this.rootNodeId = dfScode.rootNodeId;
        this.MNI = dfScode.MNI;
        this.relatedRatio = dfScode.relatedRatio;
        this.instanceNum = dfScode.instanceNum;
        this.maxNodeId = dfScode.maxNodeId;
        this.edgeSeq = new ArrayList<>(dfScode.edgeSeq.size());
        for (GSpanEdge edge : dfScode.edgeSeq) {
            this.edgeSeq.add(new GSpanEdge(edge));
        }
        this.nodeLabelMap = new HashMap<>(dfScode.nodeLabelMap.size());
        for (Map.Entry<Integer, Integer> entry : dfScode.nodeLabelMap.entrySet()) {
            this.nodeLabelMap.put(entry.getKey(), entry.getValue());
        }
        this.rootNodeNum = dfScode.getRootNodeNum();
        this.rootNodeRatio = dfScode.getRootNodeRatio();
    }

    /**
     * if this dfsCode is parent of  possibleChild return true else return false
     *
     * @param possibleChild
     * @return 1 equal, 0 parent -1 not parent(child/no relation)
     */
    public int isParentOf(DFScode possibleChild) throws Exception {
        if (possibleChild.getEdgeSeq().isEmpty() || this.getEdgeSeq().isEmpty()) {
            throw new Exception("illegal DFS code");
        } else if (possibleChild.getEdgeSeq().size() < this.getEdgeSeq().size()) {
            return -1;
        } else {
            return DFScodeTree.isParentOf(new DFScodeTree(this), new DFScodeTree(possibleChild));
        }
    }

    public boolean saveToFile(String filePath, boolean isAppend) throws Exception {
        File file = new File(filePath);
        FileWriter fileWriter;
        DFScodeJson dfScodeJson = new DFScodeJson(this);
        if (file.exists()) {
            fileWriter = new FileWriter(filePath, isAppend);
        } else {
            fileWriter = new FileWriter(filePath);
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(fileWriter, dfScodeJson);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static DFScode readFromFile(String filePath) throws Exception {
        File file = new File(filePath);
        DFScodeJson dfScodeJson;
        if (file.exists()) {
            // 增加jackson 对google guava的支持
            ObjectMapper mapper = new ObjectMapper();

            dfScodeJson = mapper.readValue(file, DFScodeJson.class);
        } else {
            throw new Exception("file does not exist");
        }
        return new DFScode(dfScodeJson);
    }

    public static void removeDupDumpReadable(String dirPath, String dataBasePath) throws Exception {
        //结果保存在第二级目录中
        try {
            File dirFile = new File(dirPath);
            File[] files;
            if (dirFile.isDirectory()) {
                files = dirFile.listFiles();
            } else {
                throw new Exception("dirPath must be a dir");
            }
            ArrayList<Pair<File, Map<Integer, DFScode>>> dfScodes = new ArrayList<>();
            for (File dir : files) {
                if (!dir.isDirectory()) {
                    continue;
                }
                File[] reFiles = dir.listFiles();
                Map<Integer, DFScode> currentMap = new HashMap<>();
                for (File reFile : reFiles) {
                    String fileName = reFile.getName();
                    if (fileName.length() >= 2 && fileName.charAt(0) == 'R' && fileName.charAt(1) == 'E' && reFile.length() > 1) {
                        Integer relationId = Integer.parseInt(fileName.split("Id_")[1].replace(".json", ""));
                        System.out.println(dir.getAbsolutePath() + File.separator + fileName);
                        DFScode dfScode = DFScode.readFromFile(dir.getAbsolutePath() + File.separator + fileName);
                        currentMap.put(relationId, dfScode);
                    }
                }
                dfScodes.add(new Pair<>(dir, currentMap));
            }
            for (Pair<File, Map<Integer, DFScode>> dFScodeOfFile : dfScodes) {
                File graphFile = dFScodeOfFile.getKey();
                Integer typeId = Integer.parseInt(graphFile.getName().split("T_|.json")[1]);
                Map<Integer, DFScode> map = dFScodeOfFile.getValue();
                if (map.isEmpty()) {
                    continue;
                } else if (map.size() == 1) {
                    new DFScodeString(map.get(1), dataBasePath, typeId).saveToFile(graphFile.getAbsolutePath() + File.separator + "READRE_" + graphFile.getName() + "Id_1.json", false
                    );
                    // id start from 1
                } else {
                    for (int i = 1; i < map.size() + 1; i++) {
                        boolean flag = true;
                        DFScode currentDFScode = map.get(i);
                        for (int j = 1; j < map.size() + 1; j++) {
                            if (i == j) {
                                continue;
                            }
                            DFScode nextDFScode = map.get(j);
                            int mode = currentDFScode.isParentOf(nextDFScode);
                            if (mode == 0 || (mode == 1 && currentDFScode.MNI < nextDFScode.MNI )) {
                                // mode == 0 || (mode == 1 && currentDFScode.MNI < nextDFScode.MNI && i>j )
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            new DFScodeString(currentDFScode, dataBasePath, typeId).saveToFile(graphFile.getAbsolutePath() + File.separator + "READRE_" + graphFile.getName() + "Id_" + i + ".json", false);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }


    public LinkedList<Integer> fetchRightMostPath() throws Exception {
        LinkedList<Integer> rightMostPath = new LinkedList<>();
        for (GSpanEdge edge : this.edgeSeq) {
            int nodeA = edge.getNodeA();
            int nodeB = edge.getNodeB();
            if (nodeA < nodeB) {
                //forward edge
                if (rightMostPath.size() == 0) {
                    rightMostPath.add(nodeA);
                    rightMostPath.add(nodeB);
                } else {
                    while (rightMostPath.getLast() != nodeA) {
                        rightMostPath.removeLast();
                    }
                    if (rightMostPath.size() == 0) {
                        throw new Exception("input error in DFScode, not connected graph");
                    }
                    rightMostPath.add(nodeB);
                }
            } else if (nodeA == nodeB) {
                throw new Exception("input error in GSpanEdge, nodeA == nodeB");
            }
            // backward edge 对于 最右路径没有 改变
            // 因此 DFScode生长时需要判断是否真的增长了
        }
        return rightMostPath;
    }
    /**
     * 获得DFScode中 节点的标签
     * @param nodeId DFScode 中 节点id
     * @return
     */
    public Integer fetchNodeLabel(Integer nodeId) {
        return this.nodeLabelMap.get(nodeId);
    }

    public Set<Integer> fetchNodes() {
        return nodeLabelMap.keySet();
    }

/*    private GSpanEdge minRightMostPathExtension(DFScode minDFSCode, Table<Integer, Integer, Set<GSpanEdge>> blocks) throws Exception {
        GSpanEdge minEdge = null;
        Set<GSpanEdge> childrenEdge = new LinkedHashSet<>();
        if (minDFSCode.edgeSeq.size() == 0) {
            for (Set<GSpanEdge> edges : blocks.values()) {
                childrenEdge.addAll(edges);
            }
        } else {
            LinkedList<Integer> rightMostPath = minDFSCode.getRightMostPath();
            Integer rightMostNode = rightMostPath.getLast();
            if (rightMostPath.size() == 0 || rightMostPath.size() == 1) {
                throw new Exception("right most path size is 0 or 1, ERROR");
            } else {
                // 1  not allow self loop
                // 0  allow self loop
                int leastRMPSize = 1;
                if (rightMostPath.size() > leastRMPSize) {
                    // backward extend, 最后1个节点，无需和最右节点组成新的边，且 不允许和最右节点组成后向边，构成self looped edge
                    ListIterator<Integer> rightMostPathIt = rightMostPath.listIterator();
                    int label1 = minDFSCode.getNodeLabel(rightMostNode);
                    while (rightMostPathIt.hasNext()) {
                        int node2 = rightMostPathIt.next();
                        int label2 = minDFSCode.getNodeLabel(node2);

                        Set<GSpanEdge> possibleChildren = new HashSet<>();
                        Set<GSpanEdge> set1 = blocks.get(label1, label2);
                        Set<GSpanEdge> set2 = blocks.get(label2, label1);
                        // 反转了
                        // 组成新边的时候 没有考虑反转
                        if (set1 != null) {
                            possibleChildren.addAll(set1);
                        }
                        if (set2 != null) {
                            possibleChildren.addAll(set2);
                        }
                        for (GSpanEdge possibleChild : possibleChildren) {
                            GSpanEdge possibleEdge = new GSpanEdge(rightMostNode, node2, label1, label2, possibleChild.getEdgeLabel(), 1);
                            GSpanEdge possibleEdgeReverse = new GSpanEdge(node2, rightMostNode, label2, label1, possibleChild.getEdgeLabel(), 1);
                            // 不允许有环
                            if (!minDFSCode.getEdgeSeq().contains(possibleEdge) && !minDFSCode.getEdgeSeq().contains(possibleEdgeReverse)) {
                                childrenEdge.add(possibleEdge);
                            }
                        }
                    }
                }
                // forward extend
                Iterator<Integer> descRMPit = rightMostPath.descendingIterator();
                while (descRMPit.hasNext()) {
                    Integer nodeInRMP = descRMPit.next();
                    Integer nodeInRMPLabel = minDFSCode.getNodeLabel(nodeInRMP);
                    Set<GSpanEdge> possibleChildren = new HashSet<>();
                    for (Set<GSpanEdge> edgesLabelB : blocks.row(nodeInRMPLabel).values()) {
                        possibleChildren.addAll(edgesLabelB);
                    }
                    for (GSpanEdge possibleChild : possibleChildren) {
                        int nodeId2 = minDFSCode.getMaxNodeId() + 1;
                        int nodeLabel2 = possibleChild.getLabelB();
                        int edgeLabel = possibleChild.getEdgeLabel();
                        GSpanEdge possibleEdge = new GSpanEdge(nodeInRMP, nodeId2, nodeInRMPLabel, nodeLabel2, edgeLabel, 1);
                        childrenEdge.add(possibleEdge);
                    }

                    possibleChildren = new HashSet<>();
                    for (Set<GSpanEdge> edgesLabelA : blocks.column(nodeInRMPLabel).values()) {
                        possibleChildren.addAll(edgesLabelA);
                    }
                    for (GSpanEdge possibleChild : possibleChildren) {
                        int nodeId2 = minDFSCode.getMaxNodeId() + 1;
                        int nodeLabel2 = possibleChild.getLabelA();
                        int edgeLabel = possibleChild.getEdgeLabel();
                        GSpanEdge possibleEdge = new GSpanEdge(nodeInRMP, nodeId2, nodeInRMPLabel, nodeLabel2, edgeLabel, 1);
                        childrenEdge.add(possibleEdge);
                    }
                }
            }
        }
        if (childrenEdge.size() == 0) {
            return null;
        } else {
            Iterator<GSpanEdge> childEdgeIt = childrenEdge.iterator();
            while (childEdgeIt.hasNext()) {
                GSpanEdge childEdge = childEdgeIt.next();
                if (minEdge == null || childEdge.compareTo(minEdge) < 0) {
                    minEdge = childEdge;
                }
            }
            return minEdge;
        }
    }*/



    public Integer getRootNodeNum() {
        return rootNodeNum;
    }

    public void setRootNodeNum(Integer rootNodeNum) {
        this.rootNodeNum = rootNodeNum;
    }

    public Double getRootNodeRatio() {
        return rootNodeRatio;
    }

    public void setRootNodeRatio(Double rootNodeRatio) {
        this.rootNodeRatio = rootNodeRatio;
    }

    public void setMaxNodeId(Integer maxNodeId) {
        this.maxNodeId = maxNodeId;
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

    public ArrayList<GSpanEdge> getEdgeSeq() {
        return edgeSeq;
    }

    public void setEdgeSeq(ArrayList<GSpanEdge> edgeSeq) {
        this.edgeSeq = edgeSeq;
    }



    public Integer getMaxNodeId() {
        return maxNodeId;
    }

    public Map<Integer, Integer> getNodeLabelMap() {
        return nodeLabelMap;
    }

    public void setNodeLabelMap(Map<Integer, Integer> nodeLabelMap) {
        this.nodeLabelMap = nodeLabelMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DFScode dfScode = (DFScode) o;
        return Objects.equals(edgeSeq, dfScode.edgeSeq);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edgeSeq);
    }

    @Override
    public String toString() {
        return "DFScode{" +
                "edgeSeq=" + edgeSeq +
                '}';
    }

    public static void main(String[] args) throws Exception {
        DFScode dfScode = new DFScode(new GSpanEdge(1, 2, 1, 1, 1, 1));
        //1
        dfScode.addEdge(new GSpanEdge(2, 3, 1, 2, 1, 1));
        //2
        dfScode.addEdge(new GSpanEdge(3, 1, 2, 1, 1, 1));
        //3
        dfScode.addEdge(new GSpanEdge(2, 4, 1, 3, 1, 1));
        //4
        dfScode.addEdge(new GSpanEdge(4, 1, 3, 1, 1, 1));
        //5
        dfScode.addEdge(new GSpanEdge(1, 5, 1, 3, 1, 1));
        //6
        dfScode.addEdge(new GSpanEdge(5, 6, 3, 4, 1, 1));
  /*        //7
        //dfScode.addEdge(new GSpanEdge(6, 1, 4, 1, 1, 1));
        //8
        //dfScode.addEdge(new GSpanEdge(5, 7, 3, 1, 1, 1));
        //9
        //dfScode.addEdge(new GSpanEdge(7, 8, 1, 2, 1, 1));
        //10
        DFScode dfScode1 = new DFScode(new GSpanEdge(1, 2, 1, 1, 1, 1));
        //1
        dfScode1.addEdge(new GSpanEdge(2, 3, 1, 2, 1, 1));
        //2
        dfScode1.addEdge(new GSpanEdge(3, 1, 2, 1, 1, 1));
        //3
        dfScode1.addEdge(new GSpanEdge(2, 4, 1, 3, 1, 1));
        //4
        dfScode1.addEdge(new GSpanEdge(4, 1, 3, 1, 1, 1));
        //5
        dfScode1.addEdge(new GSpanEdge(1, 5, 1, 3, 1, 1));
        //6
        dfScode1.addEdge(new GSpanEdge(5, 6, 3, 4, 1, 1));
        //7
        dfScode1.addEdge(new GSpanEdge(6, 1, 4, 1, 1, 1));
        //8
        DFScode dfScode2 = new DFScode(new GSpanEdge(1, 2, 1, 1, 1, 1));
        //1
        dfScode2.addEdge(new GSpanEdge(2, 3, 1, 2, 1, 1));
        //2
        dfScode2.addEdge(new GSpanEdge(3, 1, 2, 1, 1, 1));
        //3
        dfScode2.addEdge(new GSpanEdge(2, 4, 1, 3, 1, 1));
        //4
        dfScode2.addEdge(new GSpanEdge(4, 1, 3, 1, 1, 1));
        //5
        dfScode2.addEdge(new GSpanEdge(3, 5, 1, 3, 1, 1));
        //6
        dfScode2.addEdge(new GSpanEdge(5, 6, 3, 4, 1, 1));
        //7
        dfScode2.addEdge(new GSpanEdge(6, 1, 4, 1, 1, 1));
        //8
        System.out.println(dfScode.isParentOf(dfScode1));
        System.out.println(dfScode.isParentOf(dfScode));
        System.out.println(dfScode.isParentOf(dfScode2));
        GSpanEdge gSpanEdge1 = new GSpanEdge(6, 5, 3, 1, 1, 1);
        GSpanEdge gSpanEdge2 = new GSpanEdge(6, 1, 4, 1, 1, 1);
        System.out.println(gSpanEdge1.equals(gSpanEdge2));
                dfScode.saveToFile("test.json", true);
        dfScode1 = DFScode.readFromFile("test.json");

        String dirPath = "D:\\April9\\R_0.8\\P_all";*/
        //DFScode.removeDupDumpReadable(dirPath,"C:\\bioportal.sqlite",true);
/*        String dirPath = "/hdd/liubinchu/D_10/";
        DFScode.removeDupDumpReadable(dirPath,"/home/lbc/bioportal1.sqlite",false);*/
        String dirPath = "D:\\New folder (3)\\";
        DFScode.removeDupDumpReadable(dirPath, "C:\\bioportal1.sqlite");
    }
}
