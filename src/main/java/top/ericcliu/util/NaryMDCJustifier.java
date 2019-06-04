package top.ericcliu.util;

import top.ericcliu.ds.DFScode;
import top.ericcliu.ds.DFScodeInstance;
import top.ericcliu.ds.GSpanEdge;
import top.ericcliu.ds.MultiLabelGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author liubi
 * @date 2019-05-22 09:29
 **/
public class NaryMDCJustifier {
    private MultiLabelGraph dFSCodeGraph;
    private DFScode dFScode;
    private int maxDepth;

    public NaryMDCJustifier(DFScode dfScode, int maxDepth) throws Exception {
        this.dFScode = dfScode;
        this.dFSCodeGraph = new MultiLabelGraph(this.dFScode);
        this.maxDepth = maxDepth;
    }

    public boolean justify() throws Exception {
        int edgeIndex = 0;
        // 标记待判断的DFScode边 id
        int maxTurn = this.dFScode.getEdgeSeq().size();
        DFScode minDFScode = new DFScode();
        DFScodeInstance minDFSCodeInstance = null;

        // 选取最小边
        GSpanEdge minEdge = null;
        Iterator<Map<DFScode, DFScodeInstance>> mapIt = this.dFSCodeGraph.getGraphEdge().values().iterator();
        while (mapIt.hasNext()) {
            for (Map.Entry<DFScode, DFScodeInstance> entry : mapIt.next().entrySet()) {
                ArrayList<GSpanEdge> edgeSeq = entry.getKey().getEdgeSeq();
                DFScodeInstance currentInstance = entry.getValue();
                GSpanEdge currentEdge = null;
                if (edgeSeq.size() != 1) {
                    throw new Exception("dFSCodeGraph 初始化 存在问题");
                } else {
                    currentEdge = edgeSeq.get(0);
                }
                if (minEdge == null || minEdge.compareTo(currentEdge) > 0) {
                    minEdge = currentEdge;
                    minDFSCodeInstance = currentInstance;
                }
            }
        }
        if (minEdge.compareTo(dFScode.getEdgeSeq().get(edgeIndex++)) < 0) {
            // 生成的DFScode 更小， 给定dfs code不是最小DFScode
            return false;
        } else {
            minDFScode.addEdge(minEdge);
        }
        while (edgeIndex < maxTurn) {
            //对最小边进行最右拓展
            ArrayList<GSpanEdge> childrenEdge = SingleLabelUtil.nAryRelationExtension(minDFScode,this.dFSCodeGraph,
                    this.maxDepth);
            Map<GSpanEdge, DFScodeInstance> childrenEdgeInstanceMap = new HashMap<>(childrenEdge.size());
            Iterator<GSpanEdge> edgeIt = childrenEdge.iterator();
            while (edgeIt.hasNext()) {
                GSpanEdge childEdge = edgeIt.next();
                DFScodeInstance childInstace = SingleLabelUtil.subGraphIsomorphism(minDFScode, minDFSCodeInstance,
                        childEdge, false, this.dFSCodeGraph);
                childrenEdgeInstanceMap.put(childEdge, childInstace);
            }
            minEdge = null;
            for (Map.Entry<GSpanEdge, DFScodeInstance> entry : childrenEdgeInstanceMap.entrySet()) {
                if (entry.getValue().calMNI() > 0) {
                    if (minEdge == null || minEdge.compareTo(entry.getKey()) > 0) {
                        minEdge = entry.getKey();
                        minDFSCodeInstance = entry.getValue();
                    }
                }
            }
            if (minEdge == null) {
                throw new Exception("childrenEdge size == 0, or all childInstace.getMNI() < 0, no valid childrenEdge");
            } else if (minEdge.compareTo(dFScode.getEdgeSeq().get(edgeIndex++)) < 0) {
                // 生成的DFScode 更小， 给定dfs code不是最小DFScode
                return false;
            }
            minDFScode.addEdge(minEdge);
        }
        return true;
    }
}
