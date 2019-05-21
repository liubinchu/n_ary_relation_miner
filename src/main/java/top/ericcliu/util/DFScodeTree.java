package top.ericcliu.util;

import org.apache.jena.base.Sys;
import sun.reflect.generics.tree.Tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liubi
 * @date 2019-05-20 19:25
 **/
public class DFScodeTree {
    private  TreeNode root = null;
    /**
     * key: node id in DFS code
     * value: node val in DFS code
     */
    private Map<Integer,TreeNode> nodeMap;

    /**
     * 需要满足 DFScode 中 第一个出现的edge 的第一个节点 为根节点
     * @param dfScode
     * @throws Exception
     */
    public DFScodeTree(DFScode dfScode) throws Exception {
        ArrayList<GSpanEdge> edgeSeq = dfScode.getEdgeSeq();
        if (edgeSeq!=null&&edgeSeq.size()!=0){
            for(GSpanEdge edge : edgeSeq){
                if(nodeMap==null||nodeMap.size()==0){
                    this.nodeMap = new HashMap<>();
                    this.root = new TreeNode(edge.getLabelA(),Integer.MAX_VALUE);
                    this.nodeMap.put(edge.getNodeA(),this.root);
                }
                try {
                    TreeNode parent = nodeMap.get(edge.getNodeA());
                    TreeNode current = new TreeNode(edge.getLabelB(),edge.getEdgeLabel());
                    parent.addChild(current);
                    this.nodeMap.put(edge.getNodeB(),current);
                }catch (NullPointerException e){
                    e.printStackTrace();
                    throw new Exception("illegal edge sequence");
                }
            }
        }
    }

    /**
     * 判断  treeA 是否是 treeB的 父模式
     * @param treeA
     * @param treeB
     * @return 1 equal, 0 parent -1 not parent(child/no relation)
     */
    public static int isParentOf(DFScodeTree treeA, DFScodeTree treeB){
        return TreeNode.isParent(treeA.root,treeB.root);
    }
    public static void main(String[] args) throws Exception {
        DFScode dfScode = new DFScode(new GSpanEdge(1, 2, 1, 1, 1, 1));
        //1
        dfScode.addEdge(new GSpanEdge(2, 3, 1, 2, 1, 1));
        //2
        dfScode.addEdge(new GSpanEdge(2, 4, 1, 3, 1, 1));
        //3
        dfScode.addEdge(new GSpanEdge(1, 5, 1, 3, 1, 1));
        //4
        //dfScode.addEdge(new GSpanEdge(5, 6, 3, 4, 1, 1));
        //5
        DFScodeTree dfScodeTree = new DFScodeTree(dfScode);

        DFScode dfScode1 = new DFScode(new GSpanEdge(1, 2, 1, 1, 1, 1));
        //1
        dfScode1.addEdge(new GSpanEdge(2, 3, 1, 2, 1, 1));
        //2
        dfScode1.addEdge(new GSpanEdge(2, 4, 1, 3, 1, 1));
        //3
        dfScode1.addEdge(new GSpanEdge(1, 5, 1, 3, 1, 1));
        //4
        dfScode1.addEdge(new GSpanEdge(5, 6, 3, 4, 1, 1));
        //5
        DFScodeTree dfScodeTree1 = new DFScodeTree(dfScode1);

        int f = DFScodeTree.isParentOf(dfScodeTree1,dfScodeTree);
        System.out.println(f);
    }
}
