package top.ericcliu.util;

import java.util.*;

/**
 * @author liubi
 * @date 2019-04-15 20:24
 **/
public class DFScodeInstance implements SaveToFile {
    /**
     * instances 中每个元素 为一个 instance
     * instance[nodeId] 表示 dfsCode 中 指定nodeId节点 在 数据中的实例节点
     * DFS code node ID 需要从0开始且连续
     */
    private ArrayList<int[]> instances = new ArrayList<>();
    private DFScode dfScode;

    public DFScodeInstance() {
    }

    public DFScodeInstance(DFScode dfScode) {
        this.dfScode = dfScode;
    }

    public DFScode getDfScode() {
        return dfScode;
    }

    public ArrayList<int[]> getInstances() {
        return instances;
    }

    /**
     * 向其中添加instance
     *
     * @param instance key: node id in corresponding DFS code
     *                 value: node id of DFS code embedding in data graph, value is the node id in data graph
     * @return
     */
    public boolean addInstance(DFScode dfScode, int[] instance) throws Exception {
        if (this.dfScode == null) {
            this.dfScode = dfScode;
        }
        if (!this.dfScode.equals(dfScode)) {
            throw new Exception("illegal DFS code");
        }
        if (instance.length != dfScode.getNodes().size()) {
            throw new Exception("illegal instance");
        }
        this.instances.add(instance);
        return true;
    }

    public int calMNI() throws Exception {
        if (this.dfScode == null && this.instances.size() == 0) {
            return 0;
            // 当前模式 在图中 不存在 实例
        }
        ArrayList<Set<Integer>> sets = new ArrayList<>(this.dfScode.getNodes().size());
        for (int i = 0; i < this.dfScode.getNodes().size(); i++) {
            sets.add(new HashSet<>());
        }

        for (int[] instance : this.instances) {
            for (int i = 0; i < this.dfScode.getNodes().size(); i++) {
                Set<Integer> set = sets.get(i);
                set.add(instance[i]);
                sets.set(i, set);
            }
        }
        int MNI = Integer.MAX_VALUE;
        for (int i = 0; i < this.dfScode.getNodes().size(); i++) {
            if (sets.get(i).size() < MNI) {
                MNI = sets.get(i).size();
            }
        }
        return MNI;
    }

    /**
     * 给定 DFS code 中的 node， 给出 实力层 的 instance node
     *
     * @param nodeId nodeId in DFS code
     * @return Map key instanceId, value instanceNodeId in Data Graph
     * @throws Exception
     */
    public Map<Integer, Integer> fetchInstanceNode(Integer nodeId) throws Exception {
        if (!this.dfScode.getNodes().contains(nodeId)) {
            throw new Exception("illeagl para");
        }
        Map<Integer, Integer> instanceNodeMap = new HashMap<>(this.instances.size());
        int index = 0;
        for (int[] instance : this.instances) {
            instanceNodeMap.put(index++, instance[nodeId]);
        }
        return instanceNodeMap;
    }

    public int calRootNodeNum() throws Exception {
        return this.fetchInstanceNode(0).entrySet().size();
    }

    public DFScodeInstance sample(double ratio, int upperBound, int bottomBound) throws Exception {
        int sampleNum = (int) (this.instances.size() * ratio);
        if (sampleNum > upperBound) {
            sampleNum = upperBound;
        }
        if (bottomBound > this.instances.size()) {
            bottomBound = this.instances.size();
        }
        if (sampleNum < bottomBound) {
            sampleNum = bottomBound;
        }

        DFScodeInstance sampled = new DFScodeInstance();
        sampled.dfScode = this.dfScode;

        Random random = new Random();
        Set<Integer> instanceIds = new HashSet<>(sampleNum);

        for (int i = 0; i < sampleNum; i++) {
            int nextInstanceId = random.nextInt(this.instances.size() - 1);
            while (instanceIds.contains(nextInstanceId)) {
                nextInstanceId = (nextInstanceId + 1) % sampleNum;
            }
            instanceIds.add(nextInstanceId);
        }

        for (int instanceId : instanceIds) {
            sampled.addInstance(this.dfScode, this.instances.get(instanceId));
        }
        return sampled;
    }

    public static void main(String[] args) throws Exception {
        DFScode dfScode = new DFScode(new GSpanEdge(1, 2, 1, 1, 1, 1));
        //dfScode.addEdge(new GSpanEdge(2, 3, 1, 2, 1, 1));
        DFScodeInstance DFScodeInstance = new DFScodeInstance();
        DFScodeInstance.addInstance(dfScode, new int[]{1, 3});
        DFScodeInstance.addInstance(dfScode, new int[]{2, 1});
        DFScodeInstance.addInstance(dfScode, new int[]{3, 2});
        DFScodeInstance.addInstance(dfScode, new int[]{1, 3});
        DFScodeInstance.sample(1, 1, 1).saveToFile("edgeInstanceTest.json", false);
        System.out.println(DFScodeInstance.calMNI());
    }
}
