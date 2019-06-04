package top.ericcliu.ds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;

import java.io.File;
import java.util.ArrayList;

/**
 * @author liubi
 * @date 2019-05-31 09:35
 **/
public class MLDFScodeString implements SaveToFile {
    private String relationNode ;
    private int MNI = -1;
    private double relatedRatio = -1.0;
    private int instanceNum = -1;
    private ArrayList<MLGSpanEdgeString> edgeSeq = new ArrayList<>();
    private Boolean isNaryRelation = null;
    /**
     * 不重复的根节点的个数
     */
    private Integer rootNodeNum = -1;
    /**
     * 数据图共具有n个不同的rootNode，该模式具有m个不同的rootNode
     * rootNodeRatio = m/n
     */
    private Double rootNodeRatio = -1.0;

    public MLDFScodeString() {
    }

    public MLDFScodeString(MLDFScode mldfScode, String databasePath, Integer relationId) throws Exception {
        if (mldfScode == null) {
            throw new Exception("Multi Label DFScode is null");
        } else {
            this.MNI = mldfScode.getMNI();
            this.relatedRatio = mldfScode.getRelatedRatio();
            this.instanceNum = mldfScode.getInstanceNum();
            if(mldfScode.getEdgeSeq()!=null&&!mldfScode.getEdgeSeq().isEmpty()){
                for(MLGSpanEdge edge:mldfScode.getEdgeSeq()){
                    this.edgeSeq.add(new MLGSpanEdgeString(edge,databasePath,relationId));
                }
            }
            this.relationNode = this.edgeSeq.get(0).getLabelA().get(0);
        }
        this.rootNodeNum = mldfScode.getRootNodeNum();
        this.rootNodeRatio = mldfScode.getRootNodeRatio();
    }
    public static MLDFScodeString readFromFile(String filePath) throws Exception {
        File file = new File(filePath);
        if (file.exists()) {
            // 增加jackson 对google guava的支持
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(file, MLDFScodeString.class);
        } else {
            throw new Exception("file does not exist");
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
        MLDFScodeString that = (MLDFScodeString) o;
        return MNI == that.MNI &&
                Double.compare(that.relatedRatio, relatedRatio) == 0 &&
                instanceNum == that.instanceNum &&
                Objects.equal(relationNode, that.relationNode) &&
                Objects.equal(edgeSeq, that.edgeSeq) &&
                Objects.equal(rootNodeNum, that.rootNodeNum) &&
                Objects.equal(rootNodeRatio, that.rootNodeRatio);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(relationNode, MNI, relatedRatio, instanceNum, edgeSeq, rootNodeNum, rootNodeRatio);
    }

    @Override
    public String toString() {
        return "MLDFScodeString{" +
                "relationNode='" + relationNode + '\'' +
                ", MNI=" + MNI +
                ", relatedRatio=" + relatedRatio +
                ", instanceNum=" + instanceNum +
                ", edgeSeq=" + edgeSeq +
                ", rootNodeNum=" + rootNodeNum +
                ", rootNodeRatio=" + rootNodeRatio +
                '}';
    }

    public Boolean getNaryRelation() {
        return isNaryRelation;
    }

    public void setNaryRelation(Boolean naryRelation) {
        isNaryRelation = naryRelation;
    }

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

    public String getRelationNode() {
        return relationNode;
    }

    public void setRelationNode(String relationNode) {
        this.relationNode = relationNode;
    }

    public int getMNI() {
        return MNI;
    }

    public void setMNI(int MNI) {
        this.MNI = MNI;
    }

    public double getRelatedRatio() {
        return relatedRatio;
    }

    public void setRelatedRatio(double relatedRatio) {
        this.relatedRatio = relatedRatio;
    }

    public int getInstanceNum() {
        return instanceNum;
    }

    public void setInstanceNum(int instanceNum) {
        this.instanceNum = instanceNum;
    }

    public ArrayList<MLGSpanEdgeString> getEdgeSeq() {
        return edgeSeq;
    }

    public void setEdgeSeq(ArrayList<MLGSpanEdgeString> edgeSeq) {
        this.edgeSeq = edgeSeq;
    }

    public static void main(String[] args) throws Exception {
        MLDFScodeString mldfScodeString = MLDFScodeString.readFromFile("D:\\New folder\\noDup\\READRE_D_10P_0.7378246753246751R_1.0T_11260.jsonMNI_0.001Id_38.json");
        System.out.println(mldfScodeString.instanceNum);
    }
}
