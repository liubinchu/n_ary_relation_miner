package top.ericcliu.ds;

import top.ericcliu.tools.DataBaseTools;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author liubi
 * @date 2019-04-09 14:44
 **/
public class GSpanEdgeString implements Serializable {

    private final static int replacedTypeId  = -24;

    private Integer nodeA, nodeB;

    private String labelA, labelB;

    private String edgeLabel, direction;


    public GSpanEdgeString() {
    }

    public GSpanEdgeString(GSpanEdge gSpanEdge, String databasePath,Integer relationId){
        DataBaseTools dataBaseTools = new DataBaseTools();
        try {
            Connection db =dataBaseTools.sqliteConect(databasePath);
            this.nodeA = gSpanEdge.getNodeA();
            this.nodeB = gSpanEdge.getNodeB();
            if(gSpanEdge.getLabelA()==replacedTypeId){
                this.labelA = dataBaseTools.printer(db,relationId);
            }
            else {
                this.labelA = dataBaseTools.printer(db,gSpanEdge.getLabelA());
            }
            if(gSpanEdge.getLabelB()==replacedTypeId){
                this.labelB = dataBaseTools.printer(db,relationId);
            }
            else {
                this.labelB = dataBaseTools.printer(db,gSpanEdge.getLabelB());
            }
            this.edgeLabel =dataBaseTools.printer(db,gSpanEdge.getEdgeLabel());
            this.direction = String.valueOf(gSpanEdge.getDirection());
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public GSpanEdgeString(Integer nodeA, Integer nodeB, String labelA, String labelB, String edgeLabel, String direction) {
        this.nodeA = nodeA;
        this.nodeB = nodeB;
        this.labelA = labelA;
        this.labelB = labelB;
        this.edgeLabel = edgeLabel;
        this.direction = direction;
    }

    public Integer getNodeA() {
        return nodeA;
    }

    public Integer getNodeB() {
        return nodeB;
    }

    public String getLabelA() {
        return labelA;
    }

    public String getLabelB() {
        return labelB;
    }

    public String getEdgeLabel() {
        return edgeLabel;
    }

    public String getDirection() {
        return direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GSpanEdgeString that = (GSpanEdgeString) o;

        if (nodeA != null ? !nodeA.equals(that.nodeA) : that.nodeA != null) {
            return false;
        }
        if (nodeB != null ? !nodeB.equals(that.nodeB) : that.nodeB != null) {
            return false;
        }
        if (labelA != null ? !labelA.equals(that.labelA) : that.labelA != null) {
            return false;
        }
        if (labelB != null ? !labelB.equals(that.labelB) : that.labelB != null) {
            return false;
        }
        if (edgeLabel != null ? !edgeLabel.equals(that.edgeLabel) : that.edgeLabel != null) {
            return false;
        }
        return direction != null ? direction.equals(that.direction) : that.direction == null;
    }

    @Override
    public int hashCode() {
        int result = nodeA != null ? nodeA.hashCode() : 0;
        result = 31 * result + (nodeB != null ? nodeB.hashCode() : 0);
        result = 31 * result + (labelA != null ? labelA.hashCode() : 0);
        result = 31 * result + (labelB != null ? labelB.hashCode() : 0);
        result = 31 * result + (edgeLabel != null ? edgeLabel.hashCode() : 0);
        result = 31 * result + (direction != null ? direction.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GSpanEdgeString{" +
                "nodeA='" + nodeA + '\'' +
                ", nodeB='" + nodeB + '\'' +
                ", labelA='" + labelA + '\'' +
                ", labelB='" + labelB + '\'' +
                ", edgeLabel='" + edgeLabel + '\'' +
                ", direction='" + direction + '\'' +
                '}';
    }

    public static void main(String[] args) {
        LinkedList<Integer> linkedList = new LinkedList<>();
        for (int i = 0; i < 100; i++) {
            linkedList.add(i);
        }
        Iterator<Integer> itDesc = linkedList.descendingIterator();
        while (itDesc.hasNext()) {
            System.out.println(itDesc.next());
        }
        GSpanEdgeString edge1 = new GSpanEdgeString(1, 2, "3", "4", "5", "1");
        System.out.println(edge1.toString());
        GSpanEdgeString edge2 = new GSpanEdgeString(2, 3, "3", "4", "5", "1");
        System.out.println(edge2.toString());
    }
}
