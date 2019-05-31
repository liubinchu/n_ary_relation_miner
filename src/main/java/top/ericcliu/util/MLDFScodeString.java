package top.ericcliu.util;

import com.google.common.base.Objects;

import java.util.ArrayList;

/**
 * @author liubi
 * @date 2019-05-31 09:35
 **/
public class MLDFScodeString implements SaveToFile {
    private int rootNodeId = -1;
    private int MNI = -1;
    private double relatedRatio = -1.0;
    private int instanceNum = -1;
    private ArrayList<MLGSpanEdgeString> edgeSeq = new ArrayList<>();

    public MLDFScodeString() {
    }

    public MLDFScodeString(MLDFScode mldfScode, String databasePath, Integer relationId) throws Exception {
        if (mldfScode == null) {
            throw new Exception("Multi Label DFScode is null");
        } else {
            this.rootNodeId = mldfScode.getRootNodeId();
            this.MNI = mldfScode.getMNI();
            this.relatedRatio = mldfScode.getRelatedRatio();
            this.instanceNum = mldfScode.getInstanceNum();
            if(mldfScode.getEdgeSeq()!=null&&!mldfScode.getEdgeSeq().isEmpty()){
                for(MLGSpanEdge edge:mldfScode.getEdgeSeq()){
                    this.edgeSeq.add(new MLGSpanEdgeString(edge,databasePath,relationId));
                }
            }
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
        return rootNodeId == that.rootNodeId &&
                MNI == that.MNI &&
                Double.compare(that.relatedRatio, relatedRatio) == 0 &&
                instanceNum == that.instanceNum &&
                Objects.equal(edgeSeq, that.edgeSeq);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(rootNodeId, MNI, relatedRatio, instanceNum, edgeSeq);
    }

    @Override
    public String toString() {
        return "MLDFScodeString{" +
                "rootNodeId=" + rootNodeId +
                ", MNI=" + MNI +
                ", relatedRatio=" + relatedRatio +
                ", instanceNum=" + instanceNum +
                ", edgeSeq=" + edgeSeq +
                '}';
    }

    public int getRootNodeId() {
        return rootNodeId;
    }

    public void setRootNodeId(int rootNodeId) {
        this.rootNodeId = rootNodeId;
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
}
