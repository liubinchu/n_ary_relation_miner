package top.ericcliu.ds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author liubi
 * @date 2019-05-31 10:37
 **/
public class MLDFScodeTree {
    private  MLTreeNode root = null;
    /**
     * key: node id in DFS code
     * value: node val in DFS code
     */
    private Map<Integer,MLTreeNode> nodeMap;

    /**
     * 需要满足 DFScode 中 第一个出现的edge 的第一个节点 为根节点
     * @param mldfScode
     * @throws Exception
     */
    public MLDFScodeTree(MLDFScode mldfScode) throws Exception {
        ArrayList<MLGSpanEdge> edgeSeq = mldfScode.getEdgeSeq();
        if (edgeSeq!=null&&edgeSeq.size()!=0){
            for(MLGSpanEdge edge : edgeSeq){
                if(nodeMap==null||nodeMap.size()==0){
                    this.nodeMap = new HashMap<>();
                    this.root = new MLTreeNode(edge.getLabelA(),Integer.MAX_VALUE);
                    this.nodeMap.put(edge.getNodeA(),this.root);
                }
                try {
                    MLTreeNode parent = nodeMap.get(edge.getNodeA());
                    MLTreeNode current = new MLTreeNode(edge.getLabelB(),edge.getEdgeLabel());
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
    public static int isParentOf(MLDFScodeTree treeA, MLDFScodeTree treeB){
        return MLTreeNode.isParent(treeA.root,treeB.root);
    }

    public static void main(String[] args) throws Exception {
        LinkedList<Integer> label1 = new LinkedList<>();
        label1.add(1);
        LinkedList<Integer> label2 = new LinkedList<>();
        label2.add(2);
        LinkedList<Integer> label3 = new LinkedList<>();
        label3.add(3);
        LinkedList<Integer> label4 = new LinkedList<>();
        label4.add(4);

        MLDFScode dfScode = new MLDFScode(new MLGSpanEdge(1, 2, label1, label1, 1, 1));
        //1
        dfScode.addLabel(new MLGSpanEdge(1, 2, label1, label2, 1, 1));

        dfScode.addEdge(new MLGSpanEdge(2, 3, label1, label2, 1, 1));
        //2
        dfScode.addEdge(new MLGSpanEdge(2, 4, label1, label3, 1, 1));
        //3
        dfScode.addEdge(new MLGSpanEdge(1, 5, label1, label3, 1, 1));
        //4
        //dfScode.addEdge(new GSpanEdge(5, 6, 3, 4, 1, 1));
        //5
        MLDFScodeTree dfScodeTree = new MLDFScodeTree(dfScode);

        MLDFScode dfScode1 = new MLDFScode(new MLGSpanEdge(1, 2, label1, label1, 1, 1));
        //1
        dfScode1.addEdge(new MLGSpanEdge(2, 3, label1, label2, 1, 1));
        //2
        dfScode1.addEdge(new MLGSpanEdge(2, 4, label1, label3, 1, 1));
        //3
        dfScode1.addEdge(new MLGSpanEdge(1, 5, label1, label3, 1, 1));
        //4
        dfScode1.addEdge(new MLGSpanEdge(5, 6, label3, label4, 1, 1));
        //5
        MLDFScodeTree dfScodeTree1 = new MLDFScodeTree(dfScode1);

        int f = MLDFScodeTree.isParentOf(dfScodeTree,dfScodeTree1);

        System.out.println(f);
    }
}
