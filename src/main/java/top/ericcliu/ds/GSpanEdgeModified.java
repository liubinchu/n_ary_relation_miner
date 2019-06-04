package top.ericcliu.ds;

import com.google.common.base.Objects;

/**
 * @author liubi
 * @date 2019-06-04 12:43
 * 用以多标签最小DFScode判等
 * nodeA的标签不包含在判等之中
 **/
public class GSpanEdgeModified<NodeType, EdgeType>  {
    /**
     * nodeId
     */
    private int nodeA, nodeB;
    /**
     * label of node
     */
    private int labelA, labelB;
    /**
     * edgeLabel ; direction of edge
     */
    private int edgeLabel, direction;


    public GSpanEdgeModified(){
    }

    public GSpanEdgeModified(int nodeA, int nodeB, int labelA, int labelB, int edgeLabel, int direction) {
        this.nodeA = nodeA;
        this.nodeB = nodeB;
        this.labelA = labelA;
        this.labelB = labelB;
        this.edgeLabel = edgeLabel;
        this.direction = direction;
    }
    public GSpanEdgeModified(GSpanEdge gSpanEdge){
        this.nodeA = gSpanEdge.getNodeA();
        this.nodeB = gSpanEdge.getNodeB();
        this.labelA = gSpanEdge.getLabelA();
        this.labelB = gSpanEdge.getLabelB();
        this.direction = gSpanEdge.getDirection();
        this.edgeLabel = gSpanEdge.getEdgeLabel();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GSpanEdgeModified<?, ?> that = (GSpanEdgeModified<?, ?>) o;
        return nodeA == that.nodeA &&
                nodeB == that.nodeB &&
                labelB == that.labelB &&
                edgeLabel == that.edgeLabel &&
                direction == that.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nodeA, nodeB, labelB, edgeLabel, direction);
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

    public int getLabelA() {
        return labelA;
    }

    public void setLabelA(int labelA) {
        this.labelA = labelA;
    }

    public int getLabelB() {
        return labelB;
    }

    public void setLabelB(int labelB) {
        this.labelB = labelB;
    }

    public int getEdgeLabel() {
        return edgeLabel;
    }

    public void setEdgeLabel(int edgeLabel) {
        this.edgeLabel = edgeLabel;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        return "GSpanEdgeModified{" +
                "nodeA=" + nodeA +
                ", nodeB=" + nodeB +
                ", labelA=" + labelA +
                ", labelB=" + labelB +
                ", edgeLabel=" + edgeLabel +
                ", direction=" + direction +
                '}';
    }
    public static void main(String[] args){
        GSpanEdgeModified edge1 = new GSpanEdgeModified(new GSpanEdge(new GSpanEdgeModified(1,2,3,3,5,1)));
        System.out.println(edge1.toString());
        GSpanEdgeModified edge2 = new GSpanEdgeModified(new GSpanEdge(new GSpanEdgeModified(1,2,4,3,5,1)));
        System.out.println(edge2.toString());
        System.out.println(edge1.equals(edge2));
    }
}
