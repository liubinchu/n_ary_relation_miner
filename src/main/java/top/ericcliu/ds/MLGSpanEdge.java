package top.ericcliu.ds;

import com.google.common.base.Objects;

import java.util.*;

/**
 * @author liubi
 * @date 2019-05-26 21:18
 * multi label gSpan Edge, 边上的节点 具有多个标签
 * 未实现 Comparable
 **/
public class MLGSpanEdge<NodeType, EdgeType> {
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

    public MLGSpanEdge() {
    }

    public MLGSpanEdge(int nodeA, int nodeB,
                       LinkedList<Integer> labelA,
                       LinkedList<Integer> labelB,
                       int edgeLabel, int direction) {
        this.nodeA = nodeA;
        this.nodeB = nodeB;
        this.labelA = new LinkedList<>(labelA);
        this.labelB = new LinkedList<>(labelB);
        this.edgeLabel = edgeLabel;
        this.direction = direction;
    }

    public MLGSpanEdge(GSpanEdge edge) {
        this.nodeA = edge.getNodeA();
        this.nodeB = edge.getNodeB();
        this.labelA = new LinkedList<>();
        this.labelA.add(edge.getLabelA());
        this.labelB = new LinkedList<>();
        this.labelB.add(edge.getLabelB());
        this.edgeLabel = edge.getEdgeLabel();
        this.direction = edge.getDirection();
    }

    public MLGSpanEdge(MLGSpanEdge edge) {
        this(edge.nodeA, edge.nodeB,
                edge.labelA, edge.labelB,
                edge.edgeLabel, edge.direction);
    }

    public int calMNI(MultiLabelGraph dataGraph) {
        ArrayList<int[]> instances = new ArrayList<>();
        Set<Integer> possibleNodeA = null;
        for (int labela : labelA) {
            if (possibleNodeA == null) {
                possibleNodeA = new HashSet<>(dataGraph.queryNodesByLabel(labela));
            } else {
                possibleNodeA.retainAll(dataGraph.queryNodesByLabel(labela));
            }
        }
        Set<Integer> possibleNodeB = null;
        for (int labelb : labelB) {
            if (possibleNodeB == null) {
                possibleNodeB = new HashSet<>(dataGraph.queryNodesByLabel(labelb));
            } else {
                possibleNodeB.retainAll(dataGraph.queryNodesByLabel(labelb));
            }
        }
        for (int nodeA : possibleNodeA) {
            for (int nodeB : possibleNodeB) {
                if (dataGraph.getValueGraph().hasEdgeConnecting(nodeA, nodeB)
                        && dataGraph.getValueGraph().edgeValue(nodeA, nodeB).get().equals(this.edgeLabel)) {
                    instances.add(new int[]{nodeA, nodeB});
                }
            }
        }
        int MNI = Integer.MAX_VALUE;
        for (int i = 0; i < 2; i++) {
            Set<Integer> nodeSet = new HashSet<>(instances.size());
            for (int[] instance : instances) {
                nodeSet.add(instance[i]);
            }
            MNI = Math.min(MNI, nodeSet.size());
        }
        return MNI;
    }

    public void addLabelToNodeB(int edgeLabel) {
        this.labelB.add(edgeLabel);
    }

    /**
     * compares this edge with the given <code>other</code> one,
     *
     * @param other
     * @return <0: this < other
     * 0: this = other
     * >0: this > other
     * 实际上 后向边 应该 小于 前向边， 但是目前版本 前向边direction为0，导致后向边大于前向边
     * 后期只需修改 前向边的direction
     */
    public final int compareTo(final MLGSpanEdge<NodeType, EdgeType> other) throws Exception {
        if (this.nodeA == other.nodeA) {
            if (this.nodeB != other.nodeB) {
                return this.nodeB - other.nodeB;
            }
            if (this.direction != other.direction) {
                return other.direction - this.direction;
            }
            {
                // nodeA labels
                List<Integer> thisLabels = new ArrayList<>(this.getLabelA());
                List<Integer> otherLabels = new ArrayList<>(other.getLabelA());
                Collections.sort(thisLabels);
                Collections.sort(otherLabels);
                Iterator<Integer> thisIt = thisLabels.iterator();
                Iterator<Integer> otherIt = otherLabels.iterator();
                while (thisIt.hasNext() && otherIt.hasNext()) {
                    int thisLabel = thisIt.next();
                    int otherLabel = otherIt.next();
                    if (thisLabel != otherLabel) {
                        return thisLabel - otherLabel;
                    }
                }
                if (thisIt.hasNext()) {
                    // condition1 eg. this label [1,3,5] other label [1,3]; other should < this
                    return 1;
                } else if (otherIt.hasNext()) {
                    // condition2 eg. this label [1,3] other label [1,3,5]; this should < other
                    return -1;
                }
                // condition3 eg. this label [1,3,5] other label [1,3,5];  use other node label to justify
            }
            if (this.edgeLabel != other.edgeLabel) {
                return this.edgeLabel - other.edgeLabel;
            }
            {
                // nodeB labels
                List<Integer> thisLabels = new ArrayList<>(this.getLabelB());
                List<Integer> otherLabels = new ArrayList<>(other.getLabelB());
                Collections.sort(thisLabels);
                Collections.sort(otherLabels);
                Iterator<Integer> thisIt = thisLabels.iterator();
                Iterator<Integer> otherIt = otherLabels.iterator();
                while (thisIt.hasNext() && otherIt.hasNext()) {
                    int thisLabel = thisIt.next();
                    int otherLabel = otherIt.next();
                    if (thisLabel != otherLabel) {
                        return thisLabel - otherLabel;
                    }
                }
                if (thisIt.hasNext()) {
                    // condition1 eg. this label [1,3,5] other label [1,3]; other should < this
                    return 1;
                } else if (otherIt.hasNext()) {
                    // condition2 eg. this label [1,3] other label [1,3,5]; this should < other
                    return -1;
                } else {
                    // condition3 eg. this label [1,3,5] other label [1,3,5];  use other node label to justify
                    return 0;
                }
            }

        } else { // TODO: das laesst sich bestimmt noch irgendwie schoener
            // schreiben
            if (this.nodeA < this.nodeB) {
                // 前向边
                if (this.nodeB == other.nodeA) {
                    return -1;
                    // see paper
                } else {
                    if (other.nodeA > this.nodeA) {
                        if (other.nodeA > this.nodeB) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else {
                        if (this.nodeA >= other.nodeB) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                }
            } else if (other.nodeA < other.nodeB) {
                if (other.nodeB == this.nodeA) {
                    return 1;
                    // see paper
                } else {
                    if (other.nodeA > this.nodeA) {
                        if (other.nodeA >= this.nodeB) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else {
                        if (this.nodeA > other.nodeB) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                }
            } else { // compare two backwards edges with different nodeA
                return this.nodeA - other.nodeA;
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

    public static void main(String[] args) throws Exception {
        MultiLabelGraph graphSmall = new MultiLabelGraph(true);
        LinkedList<Integer> label01 = new LinkedList<>();
        label01.add(0);
        label01.add(1);
        LinkedList<Integer> label5 = new LinkedList<>();
        label5.add(5);
        LinkedList<Integer> label1 = new LinkedList<>();
        label1.add(1);
        LinkedList<Integer> label23 = new LinkedList<>();
        label23.add(2);
        label23.add(-24);
        LinkedList<Integer> label36 = new LinkedList<>();
        label36.add(-24);
        label36.add(6);
        LinkedList<Integer> label456 = new LinkedList<>();
        label456.add(4);
        label456.add(5);
        label456.add(6);
        MLGSpanEdge edge1 = new MLGSpanEdge(1,2,label23,label01,1,1);
        edge1.calMNI(graphSmall);
        MLGSpanEdge edge2 = new MLGSpanEdge(1,2,label23,label5,2,1);
        edge2.calMNI(graphSmall);
        MLGSpanEdge edge3 = new MLGSpanEdge(1,2,label36,label5,2,1);
        edge3.calMNI(graphSmall);
        MLGSpanEdge edge4 = new MLGSpanEdge(1,2,label36,label1,1,1);
        edge4.calMNI(graphSmall);
        MLGSpanEdge edge5 = new MLGSpanEdge(1,2,label23,label456,3,1);
        edge5.calMNI(graphSmall);
        MLGSpanEdge edge6 = new MLGSpanEdge(1,2,label36,label456,2,1);
        edge6.calMNI(graphSmall);
    }
}
