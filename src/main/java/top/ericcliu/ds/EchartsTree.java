package top.ericcliu.ds;

import lombok.Data;

/**
 * @author liubi
 * @date 2019-12-11 20:26
 **/
@Data
public class EchartsTree implements SaveToFile {
    private String relationNode;
    private Integer MNI = -1;
    private Double relatedRatio = -1.0;
    private Integer instanceNum = -1;
    /**
     * 不重复的根节点的个数
     */
    private Integer rootNodeNum = -1;
    /**
     * 数据图共具有n个不同的rootNode，该模式具有m个不同的rootNode
     * rootNodeRatio = m/n
     */
    private Double rootNodeRatio = -1.0;

    private EchartsTreeNode tree = null;

    // from MLDFScode to EchartsTree
    public EchartsTree(MLDFScode mldfScode, String databasePath, Integer relationId) throws Exception {
        if (mldfScode == null) {
            throw new Exception("DFScode is null");
        } else {
            this.MNI = mldfScode.getMNI();
            this.relatedRatio = mldfScode.getRelatedRatio();
            this.instanceNum = mldfScode.getInstanceNum();
            this.rootNodeNum = mldfScode.getRootNodeNum();
            this.rootNodeRatio = mldfScode.getRootNodeRatio();
            //this.relationNode
            //this.tree
            MLDFScodeTree mldfScodeTree = new MLDFScodeTree(mldfScode);
            this.tree = new EchartsTreeNode(mldfScodeTree.getRoot(), relationId, databasePath);
            /*            this.relationNode = this.tree.getName().nodeLabel;*/
            this.relationNode = this.tree.getName();
        }
    }
}
