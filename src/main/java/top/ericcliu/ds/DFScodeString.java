package top.ericcliu.ds;

import com.google.common.base.Objects;

import java.util.ArrayList;

/**
 * @author liubi
 * @date 2019-04-09 14:51
 **/
public class DFScodeString implements Cloneable , SaveToFile {
    private String relationNode ;
    private Integer MNI = -1;
    private Double relatedRatio = -1.0;
    private Integer instanceNum = -1;
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
    private ArrayList<GSpanEdgeString> edgeSeq = new ArrayList<>();
    /**
     * key : nodes appeared in this DFS code, ie nodeId in DFScode, having no relation with dataGraph
     * value : node label of this node in DFS code
     */

    public DFScodeString() {
    }
    public DFScodeString(DFScode dfScode,String databasePath,Integer relationId) throws Exception {
        if(dfScode==null){
            throw new Exception("DFScode is null");
        }
        else {
            this.MNI = dfScode.getMNI();
            this.relatedRatio = dfScode.getRelatedRatio();
            this.instanceNum = dfScode.getInstanceNum();
            if(dfScode.getEdgeSeq()!=null&&!dfScode.getEdgeSeq().isEmpty()){
                for(GSpanEdge edge : dfScode.getEdgeSeq()){
                    this.edgeSeq.add(new GSpanEdgeString(edge,databasePath,relationId));
                }
            }
            this.relationNode = this.edgeSeq.get(0).getLabelA();
            this.rootNodeNum = dfScode.getRootNodeNum();
            this.relatedRatio = dfScode.getRelatedRatio();
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
        return Objects.equal(relationNode, that.relationNode) &&
                Objects.equal(MNI, that.MNI) &&
                Objects.equal(relatedRatio, that.relatedRatio) &&
                Objects.equal(instanceNum, that.instanceNum) &&
                Objects.equal(rootNodeNum, that.rootNodeNum) &&
                Objects.equal(rootNodeRatio, that.rootNodeRatio) &&
                Objects.equal(edgeSeq, that.edgeSeq);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(relationNode, MNI, relatedRatio, instanceNum, rootNodeNum, rootNodeRatio, edgeSeq);
    }

    @Override
    public String toString() {
        return "DFScodeString{" +
                "relationNode='" + relationNode + '\'' +
                ", MNI=" + MNI +
                ", relatedRatio=" + relatedRatio +
                ", instanceNum=" + instanceNum +
                ", rootNodeNum=" + rootNodeNum +
                ", rootNodeRatio=" + rootNodeRatio +
                ", edgeSeq=" + edgeSeq +
                '}';
    }

    public ArrayList<GSpanEdgeString> getEdgeSeq() {
        return edgeSeq;
    }

    public void setEdgeSeq(ArrayList<GSpanEdgeString> edgeSeq) {
        this.edgeSeq = edgeSeq;
    }

    public String getRelationNode() {
        return relationNode;
    }

    public void setRelationNode(String relationNode) {
        this.relationNode = relationNode;
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

    public static void main(String[] args)  {
    }
}
