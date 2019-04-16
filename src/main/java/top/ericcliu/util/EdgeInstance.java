package top.ericcliu.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * @author liubi
 * @date 2019-04-15 20:24
 **/
public class EdgeInstance implements SaveToFile{
    /**
     * Arraylist 中 每一个元素 为 一个 edge instance, 用数组表示
     * edge 有三个元素唯一标识  subject_label , predicate , object_label
     * 边的 instance 这三个元素都相同， 但是 subject， 和 object 不同
     */
    private ArrayList<int[]> instances = new ArrayList<>();
    private DFScode dfScode;

    public EdgeInstance() {
    }

    public EdgeInstance(DFScode dfScode) {
        this.dfScode = dfScode;
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

    public Integer calMNI() throws Exception {
        if(this.dfScode == null && this.instances.size()==0){
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
        Integer MNI = Integer.MAX_VALUE;
        for (int i = 0; i < this.dfScode.getNodes().size(); i++) {
            if (MNI > sets.get(i).size()) {
                MNI = sets.get(i).size();
            }
        }
        return MNI;
    }

    /**
     * 给定 DFS code 中的 node， 给出 实力层 的 instance node
     * @param nodeId   nodeId in DFS code
     * @return Map key instanceId, value instanceNodeId in Data Graph
     * @throws Exception
     */
    public Map<Integer,Integer> fetchInstanceNode(Integer nodeId) throws Exception {
        if(!this.dfScode.getNodes().contains(nodeId)){
            throw new Exception("illeagl para");
        }
        Map<Integer,Integer> instanceNodeMap = new HashMap<>(this.instances.size());
        int index = 0;
        for(int[]instance : this.instances){
            instanceNodeMap.put(index++,instance[nodeId]);
        }
        return instanceNodeMap;
    }
    public static void main(String[] args) throws Exception {
        DFScode dfScode = new DFScode(new GSpanEdge(1, 2, 1, 1, 1, 1));
        //dfScode.addEdge(new GSpanEdge(2, 3, 1, 2, 1, 1));
        EdgeInstance edgeInstance = new EdgeInstance();
        edgeInstance.addInstance(dfScode, new int[]{1, 3});
        edgeInstance.addInstance(dfScode, new int[]{2, 1});
        edgeInstance.addInstance(dfScode, new int[]{3, 2});
        edgeInstance.addInstance(dfScode, new int[]{1, 3});
        edgeInstance.saveToFile("edgeInstanceTest.json",false);
        System.out.println(edgeInstance.calMNI());
    }
}
