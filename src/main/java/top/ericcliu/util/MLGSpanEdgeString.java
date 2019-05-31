package top.ericcliu.util;

import com.google.common.base.Objects;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author liubi
 * @date 2019-05-31 09:50
 **/
public class MLGSpanEdgeString {

    private final static int replacedTypeId = -24;

    private int nodeA, nodeB, direction;

    private List<String> labelA = new LinkedList<>();

    private List<String> labelB = new LinkedList<>();

    private String edgeLabel;

    public MLGSpanEdgeString() {
    }

    public MLGSpanEdgeString(MLGSpanEdge mlgSpanEdge, String databasePath, Integer relationId) throws Exception {
        this.nodeA = mlgSpanEdge.getNodeA();

        this.nodeB = mlgSpanEdge.getNodeB();

        this.direction = mlgSpanEdge.getDirection();

        DataBaseTools dataBaseTools = new DataBaseTools();
        Connection db = dataBaseTools.sqliteConect(databasePath);
        for (Object label : mlgSpanEdge.getLabelA()) {
            int labela = (int) label;
            if (labela == replacedTypeId) {
                labela = relationId;
            }
            this.labelA.add(dataBaseTools.printer(db, labela));
        }

        for (Object label : mlgSpanEdge.getLabelB()) {
            int labelb = (int) label;
            if (labelb == replacedTypeId) {
                labelb = relationId;
            }
            this.labelB.add(dataBaseTools.printer(db, labelb));
        }

        this.edgeLabel = dataBaseTools.printer(db, mlgSpanEdge.getEdgeLabel());
    }

    @Override
    public String toString() {
        return "MLGSpanEdgeString{" +
                "nodeA=" + nodeA +
                ", nodeB=" + nodeB +
                ", direction=" + direction +
                ", labelA=" + labelA +
                ", labelB=" + labelB +
                ", edgeLabel='" + edgeLabel + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MLGSpanEdgeString that = (MLGSpanEdgeString) o;
        return nodeA == that.nodeA &&
                nodeB == that.nodeB &&
                direction == that.direction &&
                Objects.equal(labelA, that.labelA) &&
                Objects.equal(labelB, that.labelB) &&
                Objects.equal(edgeLabel, that.edgeLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nodeA, nodeB, direction, labelA, labelB, edgeLabel);
    }

    public static int getReplacedTypeId() {
        return replacedTypeId;
    }

    public int getNodeA() {
        return nodeA;
    }

    public void setNodeA(int nodeA) {
        this.nodeA = nodeA;
    }

    public int getNodeB() {
        return nodeB;
    }

    public void setNodeB(int nodeB) {
        this.nodeB = nodeB;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public List<String> getLabelA() {
        return labelA;
    }

    public void setLabelA(List<String> labelA) {
        this.labelA = labelA;
    }

    public List<String> getLabelB() {
        return labelB;
    }

    public void setLabelB(List<String> labelB) {
        this.labelB = labelB;
    }

    public String getEdgeLabel() {
        return edgeLabel;
    }

    public void setEdgeLabel(String edgeLabel) {
        this.edgeLabel = edgeLabel;
    }
}
