package top.ericcliu.util;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author liubi
 * @date 2019-04-16 14:07
 **/
public class SeedEdge<T> {
    private T edgeLabel;
    private Set<T> nodeBLabelSet;



    public SeedEdge(T edgeLabel, Set<T> nodeBLabelSet) {
        this.edgeLabel = edgeLabel;
        this.nodeBLabelSet = nodeBLabelSet;
    }

    public SeedEdge() {
    }

    @Override
    public String toString() {
        return "SeedEdge{" +
                "edgeLabel=" + edgeLabel +
                ", nodeBLabelSet=" + nodeBLabelSet +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        // 重写 equal 方法 ， nodeBLabelSet 中只要存在一个相同，那么 都相同
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SeedEdge seedEdge = (SeedEdge) o;

        if (!Objects.equals(edgeLabel, seedEdge.edgeLabel)) {
            return false;
        }
        for(T nodeBlabel : nodeBLabelSet){
            if(seedEdge.nodeBLabelSet.contains(nodeBlabel)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = edgeLabel != null ? edgeLabel.hashCode() : 0;
        result = 31 * result + (nodeBLabelSet != null ? nodeBLabelSet.hashCode() : 0);
        return result;
    }

    public T getEdgeLabel() {
        return edgeLabel;
    }

    public void setEdgeLabel(T edgeLabel) {
        this.edgeLabel = edgeLabel;
    }

    public Set<T> getNodeBLabelSet() {
        return nodeBLabelSet;
    }

    public void setNodeBLabelSet(Set<T> nodeBLabelSet) {
        this.nodeBLabelSet = nodeBLabelSet;
    }
    public static void  main(String[] args){
        Set<Integer> nodeASet = new HashSet<>();
        nodeASet.add(1);
        nodeASet.add(2);
        Set<Integer> nodeBSet = new HashSet<>();
        nodeBSet.add(1);
        nodeBSet.add(3);
        Set<Integer> nodeCSet = new HashSet<>();
        nodeCSet.add(4);
        nodeCSet.add(3);
        SeedEdge seedEdge = new SeedEdge(1,nodeASet);
        SeedEdge seedEdge1 = new SeedEdge(1,nodeCSet);
        System.out.println(seedEdge.equals(seedEdge1));
    }
}
