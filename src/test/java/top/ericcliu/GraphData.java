package top.ericcliu;

import java.util.ArrayList;

/**
 * 图的数据类
 *
 * @author lyq
 *
 */
public class GraphData {
	/**
	 * node's label
	 * List index: i  node id
	 * List value:  nodeLabels.get(i) label id
	 */
	private ArrayList<Integer> nodeLabels;
	/**
	 *节点是否可用,可能被移除
	 * List index: i  node id
	 * List value:  nodeVisibles.get(i) whether visible
	 */
	private ArrayList<Boolean> nodeVisibles;
	/**
	 * edge's label
	 * List index: i  edge id
	 * List value:  edgeLabels.get(i) label id
	 */
	private ArrayList<Integer> edgeLabels;
	/**
	 * 边的起始点
	 * List index: i  edge id
	 * List value:  edgeX.get(i) node id
	 */
	private ArrayList<Integer> edgeX;
	/**
	 * 边的终结点
	 * List index: i  edge id
	 * List value:  edgeY.get(i) node id
	 */
	private ArrayList<Integer> edgeY;
	/**
	 *边是否可用
	 * List index: i  edge id
	 * List value:  edgeVisibles.get(i) whether visible
	 */
	private ArrayList<Boolean> edgeVisibles;

	public GraphData() {
		this.nodeLabels = new ArrayList<>();
		this.nodeVisibles = new ArrayList<>();
		this.edgeLabels = new ArrayList<>();
		this.edgeX = new ArrayList<>();
		this.edgeY = new ArrayList<>();
		this.edgeVisibles = new ArrayList<>();
	}

	public ArrayList<Integer> getNodeLabels() {
		return this.nodeLabels;
	}

	public void setNodeLabels(ArrayList<Integer> nodeLabels) {
		this.nodeLabels = nodeLabels;
	}

	public ArrayList<Boolean> getNodeVisibles() {
		return nodeVisibles;
	}

	public void setNodeVisibles(ArrayList<Boolean> nodeVisibles) {
		this.nodeVisibles = nodeVisibles;
	}

	public ArrayList<Integer> getEdgeLabels() {
		return edgeLabels;
	}

	public void setEdgeLabels(ArrayList<Integer> edgeLabels) {
		this.edgeLabels = edgeLabels;
	}

	public ArrayList<Integer> getEdgeX() {
		return edgeX;
	}

	public void setEdgeX(ArrayList<Integer> edgeX) {
		this.edgeX = edgeX;
	}

	public ArrayList<Integer> getEdgeY() {
		return edgeY;
	}

	public void setEdgeY(ArrayList<Integer> edgeY) {
		this.edgeY = edgeY;
	}

	public ArrayList<Boolean> getEdgeVisibles() {
		return edgeVisibles;
	}

	public void setEdgeVisibles(ArrayList<Boolean> edgeVisibles) {
		this.edgeVisibles = edgeVisibles;
	}

	/**
	 * 根据点边频繁度移除图中不频繁的点边
	 *
	 * @param freqNodeLabel
	 *            点的频繁度统计
	 * @param freqEdgeLabel
	 *            边的频繁度统计
	 * @param minSupportCount
	 *            最小支持度计数
	 */
	public void removeInFreqNodeAndEdge(int[] freqNodeLabel, int[] freqEdgeLabel, int minSupportCount) {
		for (int i = 0; i < nodeLabels.size(); i++) {
			if (freqNodeLabel[nodeLabels.get(i)] < minSupportCount) {
				// 如果小于支持度计数，则此点不可用
				nodeVisibles.set(i, false);
			}
		}
		for (int i = 0; i < edgeLabels.size(); i++) {
			if (freqEdgeLabel[edgeLabels.get(i)] < minSupportCount) {
				// 如果小于支持度计数，则此边不可用
				edgeVisibles.set(i, false);
				continue;
			}
			// 如果此边的某个端的端点已经不可用了，则此边也不可用,x,y表示id号
			if (!nodeVisibles.get(edgeX.get(i)) || !nodeVisibles.get(edgeY.get(i))) {
				edgeVisibles.set(i, false);
			}
		}
	}

	/**
	 * 根据标号排序重新对满足条件的点边重新编号
	 *
	 * @param nodeLabel2Rank
	 *            点排名
	 * @param edgeLabel2Rank
	 *            边排名
	 */
	public void reLabelByRank(int[] nodeLabel2Rank, int[] edgeLabel2Rank) {
		int label = 0;
		int count = 0;
		int temp = 0;
		// 旧的id对新id的映射
		int[] oldId2New = new int[nodeLabels.size()];
		for (int i = 0; i < nodeLabels.size(); i++) {
			label = nodeLabels.get(i);
			// 如果当前点是可用的，将此id的排名作为此点新的id
			if (nodeVisibles.get(i)) {
				nodeLabels.set(i, nodeLabel2Rank[label]);
				oldId2New[i] = count;
				count++;
			}
		}

		for (int i = 0; i < edgeLabels.size(); i++) {
			label = edgeLabels.get(i);

			// 如果当前边是可用的，将此标号的排名号作为此点新的标号
			if (edgeVisibles.get(i)) {
				edgeLabels.set(i, edgeLabel2Rank[label]);

				// 对此点做x,y的id号替换
				temp = edgeX.get(i);
				edgeX.set(i, oldId2New[temp]);
				temp = edgeY.get(i);
				edgeY.set(i, oldId2New[temp]);
			}
		}
	}
}
