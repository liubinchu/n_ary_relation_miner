package top.ericcliu.ds;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import top.ericcliu.util.DBCache;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * @author liubi
 * @date 2019-12-11 20:27
 **/
@Data
@Log4j2
public class EchartsTreeNode {

    @JsonIgnore
    private  String labels;
    // 多标签 取每个标签最后一部分
    @JsonIgnore
    private  String edgeLabel;
    // edge label
    private String name;
    // name = edgeLabel + labels



    private Set<EchartsTreeNode> children;

    private final static int replacedTypeId = -24;

    private EchartsTreeNode(String labels, String edgeLabel) {

/*        this.name = new EdgeNodeLabel(edgeLabel, labels);*/
            this.name = edgeLabel + System.getProperty("line.separator") + labels;
    }

    public EchartsTreeNode(MLTreeNode mlTreeNode, int typeId, String databasePath) {
        StringBuilder labelsb = new StringBuilder();
        for (int nodeLabel : mlTreeNode.getNodeVal()) {
            if (nodeLabel == replacedTypeId) {
                nodeLabel = typeId;
            }
            try {
                labelsb.append(DBCache.getLast(nodeLabel, databasePath)).append(System.getProperty("line.separator"));
            } catch (ExecutionException e) {
                labelsb.append(nodeLabel);
                log.error(nodeLabel);
                log.error(e.getMessage());
            }
        }
        this.labels = labelsb.toString();

        if (Integer.MAX_VALUE != mlTreeNode.getEdgeVal()) {
            try {
                this.edgeLabel = DBCache.getLast(mlTreeNode.getEdgeVal(), databasePath);
            } catch (ExecutionException e) {
                this.edgeLabel = String.valueOf(mlTreeNode.getEdgeVal());
                log.error(this.edgeLabel);
                log.error(e.getMessage());
            }
            this.name = this.edgeLabel + System.getProperty("line.separator") + this.labels;
        }

        this.name = this.edgeLabel + System.getProperty("line.separator") + this.labels;

        if (mlTreeNode.getChildern() != null && !mlTreeNode.getChildern().isEmpty()) {
            if (this.children == null) {
                this.children = new HashSet<>();
            }
            for (MLTreeNode mlChild : mlTreeNode.getChildern()) {
                this.children.add(new EchartsTreeNode(mlChild, typeId, databasePath));
            }
        }
    }

    public boolean addChild(@Nonnull EchartsTreeNode child) {
        if (this.children == null) {
            this.children = new HashSet<>();
        }
        if (this.children.contains(child)) {
            return false;
        } else {
            return this.children.add(child);
        }
    }
}
