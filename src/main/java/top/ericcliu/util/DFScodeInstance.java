package top.ericcliu.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author liubi
 * @date 2019-01-20 19:19
 **/
public class DFScodeInstance implements  Cloneable{
    /**
     * row key : instance id
     * column key : node id of DFS code
     * value: DFScode 对应embedding中相对应的data graph node id
     */
    private Table<Integer,Integer,Integer> instances;

    private void setInstances(Table<Integer, Integer, Integer> instances) {
        this.instances = instances;
    }

    public DFScodeInstance() {
        this.instances = HashBasedTable.create();
    }

    public boolean saveToFile(String filePath, boolean isAppend) throws Exception {
        File file = new File(filePath);
        FileWriter fileWriter;
        if(file.exists()){
            fileWriter = new FileWriter(filePath,isAppend);
        }
        else {
            fileWriter = new FileWriter(filePath);
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new GuavaModule());
        try {
            mapper.writeValue(fileWriter,this);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向其中添加instance
     * @param instance
     *          key: node id in corresponding DFS code
     *          value: node id of DFS code embedding in data graph, value is the node id in data graph
     * @return
     */
    public boolean addInstance(DFScode dfScode,Map<Integer,Integer> instance){
        Integer rowId =  this.instances.rowKeySet().size();
        for(Integer nodeId : instance.keySet()){
            if(!dfScode.getNodes().contains(nodeId)){
                System.err.println("Do not have this node in DFS code");
                return false;
            }
            else {
                this.instances.put(rowId,nodeId,instance.get(nodeId));
            }
        }
        return true;
    }

    public int getMNI(){
        int mni;
        if(this.instances.columnKeySet().size()==0){
            mni = -1;
        }
        else {
            mni = Integer.MAX_VALUE;
            for(Integer nodeId : this.instances.columnKeySet()){
                Set<Integer> dataNodes = new HashSet<>();
                Collection<Integer> dataNodesDup = this.instances.column(nodeId).values();
                for(Integer dataNode : dataNodesDup){
                    dataNodes.add(dataNode);
                }
                if(dataNodes.size()<mni){
                    mni = dataNodes.size();
                }
            }
        }
        return mni;
    }

    public Table<Integer, Integer, Integer> getInstances() {
        return instances;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DFScodeInstance that = (DFScodeInstance) o;

        return instances != null ? instances.equals(that.instances) : that.instances == null;
    }

    @Override
    public int hashCode() {
        return instances != null ? instances.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "DFScodeInstance{" +
                "instances=" + instances +
                '}';
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        DFScodeInstance cloned = (DFScodeInstance) super.clone();
        cloned.instances = HashBasedTable.create(this.instances);
        return cloned;
    }

    public static void main(String[]args) throws Exception {
        DFScodeInstance dfScodeInstance1= new DFScodeInstance();
        DFScodeInstance dfScodeInstance2 = new DFScodeInstance();
        System.out.println(dfScodeInstance1.getInstances());
        if(dfScodeInstance1.getInstances() != dfScodeInstance2.getInstances()){
            System.out.println("deep copy");
        }
        Table<Integer,Integer,Integer> instances = HashBasedTable.create();
        instances.put(0,0,1);
        instances.put(0,1,4);
        instances.put(1,0,2);
        instances.put(1,1,5);
        instances.put(2,0,3);
        instances.put(2,1,6);
        System.out.println(instances.column(0).values());
        dfScodeInstance1.setInstances(instances);
        dfScodeInstance1.saveToFile("dfScodeInstance1.json",false);


    }
}
