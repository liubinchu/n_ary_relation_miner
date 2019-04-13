package top.ericcliu.util;

import java.util.Objects;
import java.util.Set;

public class Seed implements Comparable {
    private Integer typeId;
    private double purity;
    private Integer nodeNums;
    private Set<Integer> commonEdges;

    public Seed(Integer typeId, double purity, Integer nodeNums, Set<Integer> commonEdges) {
        this.typeId = typeId;
        this.purity = purity;
        this.nodeNums = nodeNums;
        this.commonEdges = commonEdges;
    }

    public Seed() {
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public double getPurity() {
        return purity;
    }

    public void setPurity(double purity) {
        this.purity = purity;
    }

    public Integer getNodeNums() {
        return nodeNums;
    }

    public void setNodeNums(Integer nodeNums) {
        this.nodeNums = nodeNums;
    }

    public Set<Integer> getCommonEdges() {
        return commonEdges;
    }

    public void setCommonEdges(Set<Integer> commonEdges) {
        this.commonEdges = commonEdges;
    }

    @Override
    public String toString() {
        return "Seed{" +
                "typeId=" + typeId +
                ", purity=" + purity +
                ", nodeNums=" + nodeNums +
                ", commonEdges=" + commonEdges +
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
        Seed seed = (Seed) o;
        return Double.compare(seed.purity, purity) == 0 &&
                Objects.equals(typeId, seed.typeId) &&
                Objects.equals(nodeNums, seed.nodeNums) &&
                Objects.equals(commonEdges, seed.commonEdges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeId, purity, nodeNums, commonEdges);
    }

    @Override
    public int compareTo(Object o) {
        if(o ==null){
            throw new NullPointerException("所比较对象为空");
        }
        Seed seed = (Seed) o;

        if(seed.purity - this.purity<0) {
            return  -1;
        } else if(seed.purity == this.purity) {
            return 0;
        } else {
            return 1;
        }
    }
}
