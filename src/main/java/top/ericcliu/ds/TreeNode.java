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
 * @date 2019-05-20 19:25
 **/
public class TreeNode {
    /**
     * node label in DFScode
     */
    private int nodeVal;
    private int edgeVal;
    private Set<TreeNode> childern;

    public TreeNode(int nodeVal, int edgeVal) {
        this.nodeVal = nodeVal;
        this.edgeVal = edgeVal;
    }

    public boolean addChild(@Nonnull TreeNode child) {
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
    public static int isParent(TreeNode treeA, TreeNode treeB) {
        if (treeA == null && treeB == null) {
            return 1;
        } else if (treeA == null && treeB != null) {
            return 0;
        } else if (treeA != null && treeB == null) {
            return -1;
        } else if (treeA.equals(treeB)) {
            return 1;
        } else if (treeA.nodeVal != treeB.nodeVal || treeA.edgeVal != treeB.edgeVal) {
            return -1;
        } else if (treeA.childern == null && treeB.childern != null) {
            return 0;
        } else if (treeB.childern == null || treeA.childern.size() > treeB.childern.size()) {
            return -1;
        } else {
            //treeA.val.equals(treeB.val) && treeA.childern.size()<=treeB.childern.size()
            Set<List<TreeNode>> arrangesB = ArrangeCombination.arrangementSelect(treeB.childern, treeA.childern.size(), false);
            List<TreeNode> childrenA = new ArrayList<>(treeA.childern);
            for (List<TreeNode> arrange : arrangesB) {
                int mode = treeA.childern.size() == treeB.childern.size() ? 1 : 0;
                // treeA的childern 与 treeB的childern， 当前两两配对，且都满足isParent
                for (int i = 0; i < arrange.size(); i++) {
                    TreeNode childA = childrenA.get(i);
                    TreeNode childB = arrange.get(i);
                    mode = Math.min(TreeNode.isParent(childA, childB), mode);
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
        return "TreeNode{" +
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
        TreeNode node = (TreeNode) o;
        return Objects.equal(nodeVal, node.nodeVal) &&
                Objects.equal(edgeVal, node.edgeVal) &&
                Objects.equal(childern, node.childern);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nodeVal, edgeVal, childern);
    }

    public Integer getNodeVal() {
        return nodeVal;
    }

    public void setNodeVal(Integer nodeVal) {
        this.nodeVal = nodeVal;
    }

    public Integer getEdgeVal() {
        return edgeVal;
    }

    public void setEdgeVal(Integer edgeVal) {
        this.edgeVal = edgeVal;
    }

    public Set<TreeNode> getChildern() {
        return childern;
    }

    public void setChildern(Set<TreeNode> childern) {
        this.childern = childern;
    }
}
