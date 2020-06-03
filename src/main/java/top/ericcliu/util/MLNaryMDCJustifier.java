package top.ericcliu.util;

import javafx.util.Pair;
import lombok.extern.log4j.Log4j2;
import top.ericcliu.ds.*;

import java.util.*;

/**
 * @author liubi
 * @date 2019-05-28 09:41
 **/
@Log4j2
public class MLNaryMDCJustifier {
    private MultiLabelGraph mlDFSCodeGraph;
    private MLDFScode mlDFSCode;

    public MLNaryMDCJustifier(MLDFScode mlDFSCode) throws Exception {
        this.mlDFSCode = mlDFSCode;
        this.mlDFSCodeGraph = new MultiLabelGraph(this.mlDFSCode);
    }

    public boolean justify() throws Exception {
        int edgeIndex = -1;
        // 标记待判断的DFS code 边 id
        MLDFScode minDFScode = new MLDFScode();
        MLDFScodeInstance minDFSCodeInstance = null;
        // 选取最小边
        GSpanEdge minEdge = null;
        Iterator<Map<DFScode, DFScodeInstance>> mapIt = this.mlDFSCodeGraph.getGraphEdge().values().iterator();
        while (mapIt.hasNext()) {
            for (Map.Entry<DFScode, DFScodeInstance> entry : mapIt.next().entrySet()) {
                ArrayList<GSpanEdge> edgeSeq = entry.getKey().getEdgeSeq();
                DFScodeInstance currentInstance = entry.getValue();
                if (edgeSeq.size() != 1) {
                    throw new Exception("dFSCodeGraph 初始化 存在问题");
                }
                GSpanEdge currentEdge = edgeSeq.get(0);
                if (minEdge == null || minEdge.compareTo(currentEdge) > 0) {
                    minEdge = currentEdge;
                    minDFSCodeInstance = new MLDFScodeInstance(currentInstance);
                }
            }
        }
        MLGSpanEdge minMLEdge = new MLGSpanEdge(minEdge);
        if (!compare(new Pair(false, minMLEdge), ++edgeIndex, minDFScode)) {
            // 生成的DFScode 更小， 给定dfs code不是最小DFScode
            return false;
        } else {
            minDFScode.addEdge(minMLEdge);
            // 向最小DFScode添加最小边，若当前多标签边标签已扩展完，则更新index
        }
        while (minDFScode.getTurn() < this.mlDFSCode.getTurn()) {
            //对最小DFS code 进行最右拓展
            ArrayList<Pair<Boolean, MLGSpanEdge>> childrenEdge = MultiLabelUtil.nAryRelationExtension(minDFScode,
                    Integer.MAX_VALUE, this.mlDFSCodeGraph);
            Map<Pair<Boolean, MLGSpanEdge>, MLDFScodeInstance> childrenEdgeInstanceMap = new HashMap<>(childrenEdge.size());
            Pair<Boolean, MLGSpanEdge> minMLEdgePair = null;
            Iterator<Pair<Boolean, MLGSpanEdge>> edgeIt = childrenEdge.iterator();
            while (edgeIt.hasNext()) {
                Pair<Boolean, MLGSpanEdge> childEdgePair = edgeIt.next();
                MLDFScodeInstance childInstace = MultiLabelUtil.subGraphIsomorphism(minDFScode,
                        minDFSCodeInstance, childEdgePair, this.mlDFSCodeGraph);
                childrenEdgeInstanceMap.put(childEdgePair, childInstace);
            }
            for (Map.Entry<Pair<Boolean, MLGSpanEdge>, MLDFScodeInstance> entry : childrenEdgeInstanceMap.entrySet()) {
                if (entry.getValue().calMMNI() > 0) {
                    if (minMLEdgePair == null || entry.getKey().getValue().compareTo(minMLEdgePair.getValue()) < 0) {
                        minMLEdgePair = entry.getKey();
                        minDFSCodeInstance = entry.getValue();
                    }
                }
            }
            if (minMLEdgePair == null) {
                log.error(this.mlDFSCodeGraph.graphName+":   childrenEdge size == 0, or all childInstace.getMNI() < 0, no valid childrenEdge");
                return false;
                //  应该不会出现这种情况 bug 待解决
            } else {
                if (minMLEdgePair.getKey()) {
                    // add label
                    if (!compare(minMLEdgePair, edgeIndex, minDFScode)) {
                        return false;
                    }
                    minDFScode.addLabel(minMLEdgePair.getValue());
                } else {
                    // add forward edge
                    if (!compare(minMLEdgePair, ++edgeIndex, minDFScode)) {
                        return false;
                    }
                    minDFScode.addEdge(minMLEdgePair.getValue());
                }
            }
        }
        return true;
    }

    /**
     * 判断给定的minEdgePair 和  index 指定的this.mlDFSCode 中的边的大小
     *
     * @param minEdgePair 待加入的边
     * @param index       index 指定的this.mlDFSCode 中的边
     * @param minDFScode  已经生成的DFScode
     * @return true: minEdgePair 是该步需要扩展的最小边
     * false: minEdgePair 不是该步需要扩展的最小边
     */
    private boolean compare(Pair<Boolean, MLGSpanEdge> minEdgePair, int index, MLDFScode minDFScode) throws Exception {
        MLGSpanEdge minEdge = minEdgePair.getValue();
        MLGSpanEdge dfScodeEdge = this.mlDFSCode.getEdgeSeq().get(index);
        if (minEdge.getLabelB().size() != 1) {
            throw new Exception("非法输入1");
        }
        if (minEdge.getNodeA() != dfScodeEdge.getNodeA()
                || minEdge.getNodeB() != dfScodeEdge.getNodeB()
                || minEdge.getDirection() != dfScodeEdge.getDirection()) {
            return false;
        }
        int minEdgeLabelB = (int) minEdge.getLabelB().getFirst();
        Set<Integer> dfScodeEdgeLabelB = new HashSet<>(dfScodeEdge.getLabelB());
        Set<Integer> minEdgeLabelA = new HashSet<>(minEdge.getLabelA());
        Set<Integer> dfScodeEdgeLabelA = new HashSet<>(dfScodeEdge.getLabelA());
        if (minEdgePair.getKey()) {
            // add label
            if (!minEdgeLabelA.equals(dfScodeEdgeLabelA)) {
                throw new Exception("非法输入2");
            }
            if (minEdge.getEdgeLabel() != dfScodeEdge.getEdgeLabel()) {
                return false;
            }
            int labelIndex = minDFScode.fetchNodeLabel(minDFScode.getMaxNodeId()).size();
            if (dfScodeEdge.getLabelB().size() <= labelIndex) {
                // 仍然可以增加一个标签，但是在this.mlDFSCode的对应节点上，没有该标签
                return false;
            } else {
                return dfScodeEdge.getLabelB().get(labelIndex).equals(minEdgeLabelB);
            }
        } else {
            // add a forward edge
            Set<Integer> temp = new HashSet<>(minEdgeLabelA);
            temp.removeAll(dfScodeEdgeLabelA);
            if (!temp.isEmpty()) {
                throw new Exception("非法输入3");
            }
            if (minDFScode != null && minDFScode.getEdgeSeq() != null && !minDFScode.getEdgeSeq().isEmpty()) {
                Set<Integer> lELB_GEN = new HashSet<>(minDFScode.getEdgeSeq().get(minDFScode.getEdgeSeq().size() - 1).getLabelB());
                // node B 's label of last edge in generated DFS code
                Set<Integer> lELB_DFS = new HashSet<>(this.mlDFSCode.getEdgeSeq().get(minDFScode.getEdgeSeq().size() - 1).getLabelB());
                // node B 's label of last edge in  DFS code needed justified
                lELB_DFS.removeAll(lELB_GEN);
                if (!lELB_DFS.isEmpty()) {
                    return false;
                    // 上一个节点 标签未扩展完全
                }
            }
            if (!minEdgeLabelA.equals(dfScodeEdgeLabelA)) {
                return false;
            }
            if (minEdge.getEdgeLabel() != dfScodeEdge.getEdgeLabel()) {
                return false;
            }
            return dfScodeEdge.getLabelB().getFirst().equals(minEdgeLabelB);
        }
    }
}