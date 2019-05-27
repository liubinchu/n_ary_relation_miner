package top.ericcliu;

import javafx.util.Pair;
import top.ericcliu.util.*;

import java.util.*;

/**
 * @author liubi
 * @date 2019-05-27 10:10
 **/
public class MLNAryRelationMiner {
    private MultiLabelGraph dataGraph;
    private Double threshold;
    /**
     * 支持度 用以判断 1. 是否作为 父模式扩展（MNI）2. 是否作为频繁模式输出（instance num）
     */
    private Integer support;
    /**
     * 模式扩展的最大深度 <= maxDepth
     */
    private int maxDepth;
    private Double relatedRatio;
    private int resultSize = 0;

    public MLNAryRelationMiner(MultiLabelGraph dataGraph, double thresh, int maxDepth, double relatedRatio) throws Exception {
        this.dataGraph = dataGraph;
        this.threshold = thresh;
        this.support = Math.max(2, ((Double) (threshold * this.dataGraph.getTypeRelatedNum())).intValue());
        this.maxDepth = maxDepth;
        this.relatedRatio = relatedRatio;
        //清洗不频繁的边
        for (Integer labelA : this.dataGraph.getGraphEdge().rowKeySet()) {
            for (Integer labelB : this.dataGraph.getGraphEdge().columnKeySet()) {
                Map<DFScode, DFScodeInstance> map = this.dataGraph.getGraphEdge().get(labelA, labelB);
                if (map != null) {
                    boolean changed = false;
                    Iterator<Map.Entry<DFScode, DFScodeInstance>> it = map.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<DFScode, DFScodeInstance> entry = it.next();
                        if (entry.getValue().calMNI() < this.support) {
                            it.remove();
                            changed = true;
                        }
                    }
                    if (changed) {
                        this.dataGraph.getGraphEdge().put(labelA, labelB, map);
                    }
                }
            }
        }
    }

    /**
     * 模式拓展原则，在gSpan最右拓展的基础上, 进行修改
     * 去除后向扩展，具有前向扩展的深度限制
     * 1. 首先尝试在最右节点上 扩展一个标签
     * 2. 其次尝试在最右路径上 前向扩展
     * @param parent
     * @return ArrayList<Pair<Boolean,MLGSpanEdge>>:
     * true: 在最右节点上添加新的标签
     * false: 在最右路径上扩展边
     * @throws Exception
     */
    public ArrayList<Pair<Boolean,MLGSpanEdge>> nAryRelationExtension(MLDFScode parent) throws Exception {
        ArrayList<Pair<Boolean,MLGSpanEdge>> childrenEdge = new ArrayList<>();
        LinkedList<Integer> RMP = parent.getRightMostPath();
        if (RMP.size() == 0 || RMP.size() == 1) {
            throw new Exception("right most path size is 0 or 1, ERROR");
        }
        if (this.maxDepth < 1) {
            throw new Exception("maxDepth must > 0, ERROR");
        }
        int maxSizeRMP = this.maxDepth + 1;
        boolean extendOnNode = true;
        if (RMP.size() >= maxSizeRMP) {
            // 超过最大深度,则跳过最右节点
            extendOnNode = false;
        }
        // extend a new label on right most node
        Iterator<Integer> descRMPit = RMP.descendingIterator();
        int RMNode = descRMPit.next(); // last Edge end node
        int RMNodeF =descRMPit.next(); // last Edge start node
        Set<Integer> RMNodeLabels = new HashSet<>(parent.getNodeLabel(RMNode));
        LinkedList<Integer> RMNodeFLabels = parent.getNodeLabel(RMNodeF);
        MLGSpanEdge lastEdge = parent.getEdgeSeq().get(parent.getEdgeSeq().size()-1);
        int edgeLabel = lastEdge.getEdgeLabel();
        assert parent.getNodeLabel(RMNode).equals(lastEdge.getLabelB()) : "最右节点 没有出现在 最后一个边上";
        assert RMNodeFLabels.equals(lastEdge.getLabelA()): "最右节点的签一个节点 没有出现在 最后一个边上";

        Set<DFScode> children = new HashSet<>();
        // 所有标签能够拓展出的边
        for(int RMNodeFLabel :RMNodeFLabels){
            Set<DFScode> childrenTemp = new HashSet<>();
            for (Map<DFScode,DFScodeInstance> map : this.dataGraph.getGraphEdge().row(RMNodeFLabel).values()){
                // 单个其实节点标签相同
                for(DFScode dfScode : map.keySet()){
                    GSpanEdge edge = dfScode.getEdgeSeq().get(0);
                    if(edge.getEdgeLabel()==edgeLabel
                    && !RMNodeLabels.contains(edge.getLabelB())){
                        // 边标签相同,且 最右节点上 不包含 新扩展的标签
                        childrenTemp.add(dfScode);
                    }
                }
                if(children.isEmpty()){
                    children.addAll(childrenTemp);
                }
                else {
                    children.retainAll(childrenTemp);
                }
            }
        }
        for (DFScode child : children) {
            if (child.getEdgeSeq().size() != 1) {
                throw new Exception("wrong edge");
            }
            LinkedList<Integer> RMNodeNewLabel = new LinkedList<>();
            RMNodeNewLabel.add(child.getEdgeSeq().get(0).getLabelB());
            int newEdgeLabel = child.getEdgeSeq().get(0).getEdgeLabel();
            assert newEdgeLabel==edgeLabel:"wrong edge label";
            childrenEdge.add(new Pair<>(false,
                    new MLGSpanEdge<>(RMNodeF,RMNode,RMNodeFLabels,RMNodeNewLabel,newEdgeLabel,0)));
        }

        // forward extend
        descRMPit = RMP.descendingIterator();
        while (descRMPit.hasNext()) {
            int RMPNode = descRMPit.next();
            LinkedList<Integer> RMPNodeLabels = parent.getNodeLabel(RMPNode);
            if (!extendOnNode) {
                // 跳过最右节点
                extendOnNode = true;
                continue;
            }
            // 多标签，所有标签都能够扩展出的边
            children = new HashSet<>();
            for(Integer RMPNodeLabel : RMPNodeLabels){
                Set<DFScode> childrenTemp = new HashSet<>();
                // 单个标签能够扩展出的边
                for (Map<DFScode, DFScodeInstance> map : this.dataGraph.getGraphEdge().row(RMPNodeLabel).values()) {
                    childrenTemp.addAll(map.keySet());
                }
                if (children.isEmpty()){
                    children.addAll(childrenTemp);
                }
                else {
                    children.retainAll(childrenTemp);
                }
            }
            for (DFScode child : children) {
                if (child.getEdgeSeq().size() != 1) {
                    throw new Exception("wrong edge");
                }
                int node2 = parent.getMaxNodeId() + 1;
                LinkedList<Integer> node2Labels = new LinkedList<>();
                node2Labels.add(child.getEdgeSeq().get(0).getLabelB());
                int newEdgeLabel = child.getEdgeSeq().get(0).getEdgeLabel();
                childrenEdge.add(new Pair<>(false,
                        new MLGSpanEdge<>(RMPNode,node2,RMPNodeLabels,node2Labels,newEdgeLabel,0)));
            }
        }
        return childrenEdge;
    }


}
