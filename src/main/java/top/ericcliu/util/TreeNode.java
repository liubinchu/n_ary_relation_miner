package top.ericcliu.util;

import com.google.common.base.Objects;

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
    private Integer val;
    private Set<TreeNode> childern;

    public TreeNode(Integer val) {
        this.val = val;

    }

    public boolean addChild(TreeNode child) {
        if(this.childern==null){
            this.childern = new HashSet<>();
        }
        if(this.childern.contains(child)){
            return false;
        }
        else {
            this.childern.add(child);
            return true;
        }
    }

    /**
     * 判断  treeA 是否是 treeB的 父模式
     * treeA 与 treeB 完全相同时也认为是父模式
     * @param treeA
     * @param treeB
     * @return
     */
    public static boolean isParent(TreeNode treeA, TreeNode treeB){
        if(treeA == null){
            return true;
        }
        else if(treeB == null || !treeA.val.equals(treeB.val) ){
            return false;
        }
        else if(treeA.childern==null){
            return true;
        }
        else if(treeB.childern==null || treeA.childern.size()>treeB.childern.size()){
            return false;
        }
        else {
            //treeA.val.equals(treeB.val) && treeA.childern.size()==treeB.childern.size()
            Set<List<TreeNode>> arrangesB = ArrangeCombination.arrangementSelect(treeB.childern,treeA.childern.size(),false);
            List<TreeNode> childrenA = new ArrayList<>(treeA.childern);
            for(List<TreeNode> arrange : arrangesB){
                boolean isParent = true;
                // treeA的childern 与 treeB的childern， 当前两两配对，且都满足isParent
                for(int i=0;i<arrange.size();i++){
                    TreeNode childA = childrenA.get(i);
                    TreeNode childB = arrange.get(i);
                    if(!TreeNode.isParent(childA,childB)){
                        isParent = false;
                        break;
                    }
                }
                if(isParent){
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public String toString() {
        return "TreeNode{" +
                "val=" + val +
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
        return Objects.equal(val, node.val) &&
                Objects.equal(childern, node.childern);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(val, childern);
    }

    public Integer getVal() {
        return val;
    }

    public void setVal(Integer val) {
        this.val = val;
    }

    public Set<TreeNode> getChildern() {
        return childern;
    }

    public void setChildern(Set<TreeNode> childern) {
        this.childern = childern;
    }
}
