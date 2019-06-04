package top.ericcliu.ds;

import top.ericcliu.tools.DataBaseTools;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author liubi
 * @date 2019-04-16 15:22
 **/
public class SeedString implements SaveToFile {
    private String typeId;
    private double purity;
    private Integer nodeNums;
    private Set<SeedEdge<String>> commonEdges;

    public SeedString() {
    }

    public String getTypeId() {
        return typeId;
    }

    public double getPurity() {
        return purity;
    }

    public Integer getNodeNums() {
        return nodeNums;
    }

    public Set<SeedEdge<String>> getCommonEdges() {
        return commonEdges;
    }

    public SeedString(Seed seed , String databasePath) {
        DataBaseTools dataBaseTools = new DataBaseTools();
        try {
            Connection db =dataBaseTools.sqliteConect(databasePath);
            this.typeId = dataBaseTools.printer(db,seed.getTypeId());
            this.purity = seed.getPurity();
            this.nodeNums = seed.getNodeNums();
            this.commonEdges = new HashSet<>(seed.getCommonEdges().size());
            for(SeedEdge<Integer> edge : seed.getCommonEdges()){
                Set<String> nodeLabelSetString = new HashSet<>(edge.getNodeBLabelSet().size());
                for(Integer label : edge.getNodeBLabelSet()){
                    nodeLabelSetString.add(dataBaseTools.printer(db,label));
                }
                String edgeLabel = dataBaseTools.printer(db,edge.getEdgeLabel());
                SeedEdge<String> edgeString = new SeedEdge<String>(edgeLabel,nodeLabelSetString);
                this.commonEdges.add(edgeString);
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
