package top.ericcliu.ds;

import com.google.common.base.Objects;
import top.ericcliu.util.ArrangeCombination;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author liubi
 * @date 2019-05-31 13:39
 **/
public class MLTreeNode {
    /**
     * node labels in DFScode
     */
    private Set<Integer> nodeVal;
    private int edgeVal;
    private Set<MLTreeNode> childern;

    public MLTreeNode(@Nonnull List<Integer> nodeVal, @Nonnull Integer edgeVal) {
        this.nodeVal = new HashSet<>(nodeVal);
        this.edgeVal = edgeVal;
    }

    public boolean addChild(@Nonnull MLTreeNode child) {
        if (this.childern == null) {
            this.childern = new HashSet<>();
        }
        if (this.childern.contains(child)) {
            return false;
        } else {
            return this.childern.add(child);
        }
    }

    /**
     * 判断  treeA 是否是 treeB 的 父模式
     * treeA 与 treeB 完全相同时也认为是父模式
     *
     * @param treeA
     * @param treeB
     * @return 1 equal, 0 parent -1 not parent(child/no relation)
     */
    public static int isParent(MLTreeNode treeA, MLTreeNode treeB) {
        if (treeA == null && treeB == null) {
            return 1;
        } else if (treeA == null && treeB != null) {
            return 0;
        } else if (treeA != null && treeB == null) {
            return -1;
        } else if (treeA.equals(treeB)) {
            return 1;
        } else if (treeA.edgeVal != treeB.edgeVal) {
            return -1;
        }
        HashSet<Integer> reduntent = new HashSet<>(treeA.nodeVal);
        reduntent.removeAll(treeB.nodeVal);
        if (!reduntent.isEmpty()) {
            // treeB.nodeVal 是 treeA.nodeVal 的子集，因此
            return -1;
        } else if (treeA.childern == null && treeB.childern != null) {
            return 0;
        } else if (treeB.childern == null || treeA.childern.size() > treeB.childern.size()) {
            return -1;
        } else {
            //treeA.val.equals(treeB.val) && treeA.childern.size()<=treeB.childern.size()
            Set<List<MLTreeNode>> arrangesB = ArrangeCombination.arrangementSelect(treeB.childern, treeA.childern.size(), false);
            List<MLTreeNode> childrenA = new ArrayList<>(treeA.childern);
            for (List<MLTreeNode> arrange : arrangesB) {
                int mode = treeA.childern.size() == treeB.childern.size() ? 1 : 0;
                // treeA的childern 与 treeB的childern， 当前两两配对，且都满足isParent
                for (int i = 0; i < arrange.size(); i++) {
                    MLTreeNode childA = childrenA.get(i);
                    MLTreeNode childB = arrange.get(i);
                    mode = Math.min(MLTreeNode.isParent(childA, childB), mode);
                }
                if (mode != -1) {
                    return mode;
                }
            }
            return -1;
        }
    }

    @Override
    public String toString() {
        return "MLTreeNode{" +
                "nodeVal=" + nodeVal +
                ", edgeVal=" + edgeVal +
                ", childern=" + childern +
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
        MLTreeNode that = (MLTreeNode) o;
        return Objects.equal(edgeVal, that.edgeVal) &&
                Objects.equal(nodeVal, that.nodeVal) &&
                Objects.equal(childern, that.childern);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nodeVal, edgeVal, childern);
    }

    public Set<Integer> getNodeVal() {
        return nodeVal;
    }

    public void setNodeVal(Set<Integer> nodeVal) {
        this.nodeVal = nodeVal;
    }

    public int getEdgeVal() {
        return edgeVal;
    }

    public void setEdgeVal(int edgeVal) {
        this.edgeVal = edgeVal;
    }

    public Set<MLTreeNode> getChildern() {
        return childern;
    }

    public void setChildern(Set<MLTreeNode> childern) {
        this.childern = childern;
    }
}
