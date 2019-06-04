package top.ericcliu.ds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.Multimap;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author liubi
 * typeId: id of type in database
 * nodes: nodes id belonging to this type
 * triples: related triples with given maxDeep
 * maxDeep: steps when extract related triples
 * nodeLabels: labels of this id
 */
public class TypeRelatedGraph {
    private Integer typeId;
    private Set<Integer> nodes;
    private List<int[]> triples;
    private Integer maxDeep;
    private Multimap<Integer, Integer> nodeLabels;

    public static TypeRelatedGraph readFromFile(String filePath) throws Exception {
        File file = new File(filePath);
        TypeRelatedGraph typeRelatedGraph;
        if (file.exists()) {
            // 增加jackson 对google guava的支持
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new GuavaModule());
            typeRelatedGraph = mapper.readValue(file, TypeRelatedGraph.class);

        } else {
            throw new Exception("file does not exist");
        }
        return typeRelatedGraph;
    }

    public TypeRelatedGraph() {
    }

    public TypeRelatedGraph(TypeRelatedGraph s) {
        this.typeId = s.typeId;
        this.nodes = s.nodes;
        this.triples = s.triples;
        this.maxDeep = s.maxDeep;
        this.nodeLabels = s.nodeLabels;
    }

    public TypeRelatedGraph(Integer typeId, Set<Integer> nodes, List<int[]> triples, Integer maxDeep, Multimap nodeLabels) {
        this.typeId = typeId;
        this.nodes = nodes;
        //当前类型下的所有节点 ， 子图的起始节点
        this.triples = triples;
        // s p o
        this.maxDeep = maxDeep;
        // 子图拓展的最大步数
        this.nodeLabels = nodeLabels;
        // 子图中所有出现过的节点 (不限制于第一层节点) 的类型标签
    }

    public Multimap<Integer, Integer> getNodeLabels() {
        return nodeLabels;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public Set<Integer> getNodes() {
        return nodes;
    }

    public List<int[]> getTriples() {
        return triples;
    }

    public Integer getMaxDeep() {
        return maxDeep;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TypeRelatedGraph subGraph = (TypeRelatedGraph) o;
        return Objects.equals(typeId, subGraph.typeId) &&
                Objects.equals(nodes, subGraph.nodes) &&
                Objects.equals(maxDeep, subGraph.maxDeep) &&
                Objects.equals(triples, subGraph.triples);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeId, nodes, triples, maxDeep);
    }

    @Override
    public String toString() {
        return "SubGraph{" +
                "typeId=" + typeId +
                ", nodes=" + nodes +
                ", triples=" + triples +
                ", maxDeep=" + maxDeep +
                '}';
    }

    public static void main(String[] args) throws Exception {
        //8980078
        Integer maxDeep = 5;
        Integer typeId = 10894041;
        // String filePath = "typeRelatedGraph"+typeId+".json";
        //String filePath = "R_" + "1" + "T_" + typeId+"D_"+maxDeep+".json";
        String filePath = "P_0.9984126984126984R_1T_12330515.json";
        //SeedsCalculator.extractTypeRelatedGraph(maxDeep, typeId, filePath, 1);
        TypeRelatedGraph readTest = TypeRelatedGraph.readFromFile(filePath);
        System.out.println(readTest.getNodeLabels().get(10894041));
    }
}
