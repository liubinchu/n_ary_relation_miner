package top.ericcliu.util;

import com.google.common.base.Objects;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * @author liubi
 * @date 2019-05-26 21:18
 * multi label gSpan Edge, 边上的节点 具有多个标签
 * 未实现 Comparable
 *
 **/
public class MLGSpanEdge <NodeType, EdgeType> {
        //implements  Comparable<MLGSpanEdge<NodeType, EdgeType>>, Serializable {
    /**
     * nodeId
     */
    private int nodeA, nodeB;
    /**
     * labels of node
     */
    private LinkedList<Integer> labelA, labelB;
    /**
     * edgeLabel ; direction of edge
     */
    private int edgeLabel, direction;

    public MLGSpanEdge(){
    }

    public MLGSpanEdge(int nodeA, int nodeB,
                       LinkedList<Integer> labelA,
                       LinkedList<Integer> labelB,
                       int edgeLabel, int direction) {
        this.nodeA = nodeA;
        this.nodeB = nodeB;
        this.labelA = labelA;
        this.labelB = labelB;
        this.edgeLabel = edgeLabel;
        this.direction = direction;
    }

    public MLGSpanEdge(GSpanEdge edge){
        this.nodeA = edge.getNodeA();
        this.nodeB = edge.getNodeB();
        this.labelA = new LinkedList<>();
        this.labelA.add(edge.getLabelA());
        this.labelB = new LinkedList<>();
        this.labelB.add(edge.getLabelB());
        this.edgeLabel = edge.getEdgeLabel();
        this.direction = edge.getDirection();
    }

    public MLGSpanEdge(MLGSpanEdge edge){
        this(edge.nodeA,edge.nodeB,
                new LinkedList<>(edge.labelA),
                new LinkedList<>(edge.labelB),
                edge.edgeLabel,edge.direction);
    }

    public void addLabelToNodeB(int edgeLabel){
        this.labelB.add(edgeLabel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MLGSpanEdge<?, ?> that = (MLGSpanEdge<?, ?>) o;
        return nodeA == that.nodeA &&
                nodeB == that.nodeB &&
                edgeLabel == that.edgeLabel &&
                direction == that.direction &&
                Objects.equal(labelA, that.labelA) &&
                Objects.equal(labelB, that.labelB);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nodeA, nodeB, labelA, labelB, edgeLabel, direction);
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

    public LinkedList<Integer> getLabelA() {
        return labelA;
    }

    public void setLabelA(LinkedList<Integer> labelA) {
        this.labelA = labelA;
    }

    public LinkedList<Integer> getLabelB() {
        return labelB;
    }

    public void setLabelB(LinkedList<Integer> labelB) {
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
        return "MLGSpanEdge{" +
                "nodeA=" + nodeA +
                ", nodeB=" + nodeB +
                ", labelA=" + labelA +
                ", labelB=" + labelB +
                ", edgeLabel=" + edgeLabel +
                ", direction=" + direction +
                '}';
    }
    /*    @Override
    public int compareTo(MLGSpanEdge<NodeType, EdgeType> o) {
        return 0;
    }*/
}
