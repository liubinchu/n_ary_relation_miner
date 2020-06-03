package top.ericcliu.ds;

import java.util.*;

/**
 * @author liubi
 * @date 2019-04-15 20:24
 **/
public class MLDFScodeInstance implements SaveToFile {
    /**
     * instances 中每个元素 为一个 instance
     * instance[nodeId] 表示 dfsCode 中 指定nodeId节点 在 数据中的实例节点
     * DFS code node ID 需要从0开始且连续
     */
    private ArrayList<int[]> instances = new ArrayList<>();
    private MLDFScode mldfScode;

    public MLDFScodeInstance() {
    }

    public MLDFScodeInstance(MLDFScode mldfScode) {
        this.mldfScode = mldfScode;
    }

    public MLDFScodeInstance(DFScodeInstance dfScodeInstance) {
        this.mldfScode = new MLDFScode(dfScodeInstance.getDfScode());
        this.instances = new ArrayList<>(dfScodeInstance.getInstances());
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
    public boolean addInstance(MLDFScode mldfScode, int[] instance) throws Exception {
        if (this.mldfScode == null) {
            this.mldfScode = mldfScode;
        }
        if (!this.mldfScode.equals(mldfScode)) {
            throw new Exception("illegal Multi label DFS code");
        }
        int size = mldfScode.fetchNodes().size();
        if (instance.length != mldfScode.fetchNodes().size()) {
            throw new Exception("illegal instance");
        }
        this.instances.add(instance);
        return true;
    }

    public int calMMNI() throws Exception {
        if (this.mldfScode == null && this.instances.size() == 0) {
            return 0;
            // 当前模式 在图中 不存在 实例
        }
        int MMNI = Integer.MAX_VALUE;
        for (int i = 0; i < this.mldfScode.fetchNodes().size(); i++) {
            Set<Integer> nodeSet = new HashSet<>();
            for (int[] instance : this.instances) {
                nodeSet.add(instance[i]);
            }
            MMNI = Math.min(MMNI, nodeSet.size());
        }
        return MMNI;
    }

    /**
     * 给定 DFS code 中的 node， 给出 实力层 的 instance node
     *
     * @param nodeId nodeId in DFS code
     * @return Map key instanceId, value instanceNodeId in Data Graph
     * @throws Exception
     */
    public Map<Integer, Integer> fetchInstanceNode(Integer nodeId) throws Exception {
        if (!this.mldfScode.fetchNodes().contains(nodeId)) {
            throw new Exception("illeagl para");
        }
        Map<Integer, Integer> instanceNodeMap = new HashMap<>(this.instances.size());
        int index = 0;
        for (int[] instance : this.instances) {
            instanceNodeMap.put(index++, instance[nodeId]);
        }
        return instanceNodeMap;
    }

    public int calNMNI() throws Exception {
        if (this.mldfScode == null && this.instances.isEmpty()) {
            return 0;
            // 当前模式 在图中 不存在 实例
        } else {

           // return this.fetchInstanceNode(0).entrySet().size();
            int NMNI = Integer.MAX_VALUE;
            Set<Integer> nodeSet = new HashSet<>();
            for (int[] instance : this.instances) {
                nodeSet.add(instance[0]);
            }
            NMNI = Math.min(NMNI, nodeSet.size());

            return NMNI;
        }


}

    public MLDFScodeInstance sample(double ratio, int upperBound, int bottomBound) throws Exception {
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

        MLDFScodeInstance sampled = new MLDFScodeInstance();
        sampled.mldfScode = this.mldfScode;

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
            sampled.addInstance(this.mldfScode, this.instances.get(instanceId));
        }
        return sampled;
    }

    public static void main(String[] args) throws Exception {
        LinkedList<Integer> label1 = new LinkedList<>();
        label1.add(1);
        LinkedList<Integer> label2 = new LinkedList<>();
        label2.add(2);
        MLDFScode dfScode = new MLDFScode(new MLGSpanEdge(0, 1, label1, label1, 1, 1));
        dfScode = dfScode.addEdge(new MLGSpanEdge(1, 2, label1, label1, 1, 1));
        MLDFScodeInstance DFScodeInstance = new MLDFScodeInstance();
        DFScodeInstance.addInstance(dfScode, new int[]{1, 3, 1});
        DFScodeInstance.addInstance(dfScode, new int[]{2, 1, 2});
        DFScodeInstance.addInstance(dfScode, new int[]{3, 2, 4});
        DFScodeInstance.addInstance(dfScode, new int[]{1, 4, 4});

        MLDFScodeInstance newInstance = DFScodeInstance.sample(1, 2, 2);
        System.out.println(DFScodeInstance.fetchInstanceNode(1));
    }
}
