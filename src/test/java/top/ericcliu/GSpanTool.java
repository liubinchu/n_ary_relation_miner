package top.ericcliu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * gSpan频繁子图挖掘算法工具类
 *
 * @author lyq
 *
 */
public class GSpanTool {
	/**
	 * data file format
	 * t represent new transactional graph
	 * v represent vertex
	 * e represent edge
	 * t # 0
	 * v 0 0
	 * v 1 1
	 * v 2 0
	 * v 3 0
	 * v 4 0
	 * v 5 1
	 * e 0 1 0
	 * e 1 2 0
	 * e 1 3 0
	 * e 2 4 0
	 * e 3 5 1
	 *顶点id 0,1,2,3,...
	 * 边 id 0,1,2,3,...
	 */
	public final String INPUT_NEW_GRAPH = "t";
	public final String INPUT_VERTICE = "v";
	public final String INPUT_EDGE = "e";
	/**
	 * Label标号的最大数量，包括点标号和边标号
	 */
	public final int LABEL_MAX = 10;
	/**
	 * 测试数据文件地址
	 */
	private String filePath;
	/**
	 * 最小支持度率
	 */
	private double minSupportRate;
	/**
	 * 最小支持度数，通过图总数与最小支持度率的乘积计算所得
	 */
	private int minSupportCount;
	/**
	 * 初始所有图的数据
	 */
	private ArrayList<GraphData> totalGraphDatas;
	/**
	 * 所有的图结构数据 所有的transactional graph
	 */
	private ArrayList<Graph> totalGraphs;
	/**
	 * 挖掘出的频繁子图
	 */
	private ArrayList<Graph> resultGraphs;
	/**
	 * 边的频率统计
	 */
	private EdgeFrequency ef;
	/**
	 * 节点标签的频率
	 * array index: label id
	 * array value: frequency
	 */
	private int[] freqNodeLabel;
	/**
	 * 边标签的频率
	 * array index: label id
	 * array value: frequency
	 */
	private int[] freqEdgeLabel;
	/**
	 * 重新标号之后的点的标号数
	 */
	private int newNodeLabelNum = 0;
	/**
	 * 重新标号后的边的标号数
	 */
	private int newEdgeLabelNum = 0;

	public GSpanTool(String filePath, double minSupportRate) throws IOException {
		this.filePath = filePath;
		this.minSupportRate = minSupportRate;
		readDataFile();
	}

	/**
	 * 从文件中读取数据
	 */
	private void readDataFile() throws IOException {
		ArrayList<String[]> dataArray = new ArrayList<>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(this.filePath));
			String str;
			String[] tempArray;
			while ((str = in.readLine()) != null) {
				tempArray = str.split(" ");
				dataArray.add(tempArray);
			}
			in.close();
			System.out.println("finishing reading input data file");
		} catch (IOException e) {
			System.err.println("error happens when reading input data file, please check file path: "+this.filePath);
			throw e;
		}
		calFrequency(dataArray);

		for (GraphData g : totalGraphDatas) {
			g.removeInFreqNodeAndEdge(freqNodeLabel, freqEdgeLabel,
					minSupportCount);
		}
	}

	/**
	 * 统计边和点的频度，并移除不频繁的点边，以标号作为统计的变量
	 * @param dataArray 原始数据
	 */
	private void calFrequency(ArrayList<String[]> dataArray) {
		int tempCount = 0;
		this.freqNodeLabel = new int[this.LABEL_MAX];
		this.freqEdgeLabel = new int[this.LABEL_MAX];

		// 做初始化操作 代表标号为i的节点目前的数量为0
		Arrays.fill(this.freqEdgeLabel, 0);
		Arrays.fill(this.freqNodeLabel, 0);

		GraphData gd = null;
		this.totalGraphDatas = new ArrayList<>();
		for (String[] array : dataArray) {
			if (array[0].equals(this.INPUT_NEW_GRAPH)) {
				// 加入上一个 transactional graph
				if (gd != null) {
					this.totalGraphDatas.add(gd);
				}
				// new a transactional graph
				gd = new GraphData();
			} else if (array[0].equals(this.INPUT_VERTICE)) {
				// 单个标签在一个事务图中出现的次数算作一次
				// 标签的频率表示 在多少个事务图中出现过
				// 对于single large graph 的情况 需要调整
				if (!gd.getNodeLabels().contains(Integer.parseInt(array[2]))) {
					this.freqNodeLabel[Integer.parseInt(array[2])]++;
				}
				gd.getNodeLabels().add(Integer.parseInt(array[2]));
				gd.getNodeVisibles().add(true);
			} else if (array[0].equals(INPUT_EDGE)) {
				if (!gd.getEdgeLabels().contains(Integer.parseInt(array[3]))) {
					this.freqEdgeLabel[Integer.parseInt(array[3])]++;
				}
				gd.getEdgeX().add(Integer.parseInt(array[1]));
				gd.getEdgeY().add(Integer.parseInt(array[2]));
				gd.getEdgeLabels().add(Integer.parseInt(array[3]));
				gd.getEdgeVisibles().add(true);
			}
		}
		// 把最后一块gd数据加入
		this.totalGraphDatas.add(gd);
		this.minSupportCount = (int) (this.minSupportRate * this.totalGraphDatas.size());
	}


	/**
	 * 根据标号频繁度进行排序并且重新标号
	 */
	private void sortAndReLabel() {
		int label1 = 0;
		int label2 = 0;
		int temp = 0;
		// 点排序名次
		int[] rankNodeLabels = new int[this.LABEL_MAX];
		// 边排序名次
		int[] rankEdgeLabels = new int[this.LABEL_MAX];
		// 标号对应排名
		int[] nodeLabel2Rank = new int[this.LABEL_MAX];
		int[] edgeLabel2Rank = new int[this.LABEL_MAX];

		for (int i = 0; i < this.LABEL_MAX; i++) {
			// 表示排名第i位的标号为i，[i]中的i表示排名
			rankNodeLabels[i] = i;
			rankEdgeLabels[i] = i;
		}

		for (int i = 0; i < this.freqNodeLabel.length - 1; i++) {
			int k = 0;
			label1 = rankNodeLabels[i];
			temp = label1;
			for (int j = i + 1; j < this.freqNodeLabel.length; j++) {
				label2 = rankNodeLabels[j];
				if (this.freqNodeLabel[temp] < this.freqNodeLabel[label2]) {
					// 进行标号的互换
					temp = label2;
					k = j;
				}
			}
			if (temp != label1) {
				// 进行i，k排名下的标号对调
				temp = rankNodeLabels[k];
				rankNodeLabels[k] = rankNodeLabels[i];
				rankNodeLabels[i] = temp;
			}
		}

		// 对边同样进行排序
		for (int i = 0; i < freqEdgeLabel.length - 1; i++) {
			int k = 0;
			label1 = rankEdgeLabels[i];
			temp = label1;
			for (int j = i + 1; j < freqEdgeLabel.length; j++) {
				label2 = rankEdgeLabels[j];

				if (freqEdgeLabel[temp] < freqEdgeLabel[label2]) {
					// 进行标号的互换
					temp = label2;
					k = j;
				}
			}

			if (temp != label1) {
				// 进行i，k排名下的标号对调
				temp = rankEdgeLabels[k];
				rankEdgeLabels[k] = rankEdgeLabels[i];
				rankEdgeLabels[i] = temp;
			}
		}

		// 将排名对标号转为标号对排名
		for (int i = 0; i < rankNodeLabels.length; i++) {
			nodeLabel2Rank[rankNodeLabels[i]] = i;
		}

		for (int i = 0; i < rankEdgeLabels.length; i++) {
			edgeLabel2Rank[rankEdgeLabels[i]] = i;
		}

		for (GraphData gd : totalGraphDatas) {
			gd.reLabelByRank(nodeLabel2Rank, edgeLabel2Rank);
		}

		// 根据排名找出小于支持度值的最大排名值
		for (int i = 0; i < rankNodeLabels.length; i++) {
			if (freqNodeLabel[rankNodeLabels[i]] > minSupportCount) {
				newNodeLabelNum = i;
			}
		}
		for (int i = 0; i < rankEdgeLabels.length; i++) {
			if (freqEdgeLabel[rankEdgeLabels[i]] > minSupportCount) {
				newEdgeLabelNum = i;
			}
		}
		//index比数量少1，所以要加回来
		newNodeLabelNum++;
		newEdgeLabelNum++;
	}

	/**
	 * 进行频繁子图的挖掘
	 */
	public void mine() {
		long startTime =  System.currentTimeMillis();
		Graph g;
		sortAndReLabel();

		resultGraphs = new ArrayList<>();
		totalGraphs = new ArrayList<>();
		// 通过图数据构造图结构
		for (GraphData gd : totalGraphDatas) {
			g = new Graph();
			g = g.constructGraph(gd);
			totalGraphs.add(g);
		}

		// 根据新的点边的标号数初始化边频繁度对象
		ef = new EdgeFrequency(newNodeLabelNum, newEdgeLabelNum);
		for (int i = 0; i < newNodeLabelNum; i++) {
			for (int j = 0; j < newEdgeLabelNum; j++) {
				for (int k = 0; k < newNodeLabelNum; k++) {
					for (Graph tempG : totalGraphs) {
						if (tempG.hasEdge(i, j, k)) {
							ef.edgeFreqCount[i][j][k]++;
						}
					}
				}
			}
		}

		Edge edge;
		GraphCode gc;
		for (int i = 0; i < newNodeLabelNum; i++) {
			for (int j = 0; j < newEdgeLabelNum; j++) {
				for (int k = 0; k < newNodeLabelNum; k++) {
					if (ef.edgeFreqCount[i][j][k] >= minSupportCount) {
						gc = new GraphCode();
						edge = new Edge(0, 1, i, j, k);
						gc.getEdgeSeq().add(edge);

						// 将含有此边的图id加入到gc中
						for (int y = 0; y < totalGraphs.size(); y++) {
							if (totalGraphs.get(y).hasEdge(i, j, k)) {
								gc.getGs().add(y);
							}
						}
						// 对某条满足阈值的边进行挖掘
						subMining(gc, 2);
					}
				}
			}
		}

		System.out.println("算法执行时间"+ (System.currentTimeMillis()-startTime) + "ms");
		printResultGraphInfo();
	}

	/**
	 * 进行频繁子图的挖掘
	 *
	 * @param gc
	 *            图编码
	 * @param next
	 *            图所含的点的个数
	 */
	public void subMining(GraphCode gc, int next) {
		Edge e;
		Graph graph = new Graph();
		int id1;
		int id2;

		for(int i=0; i<next; i++){
			graph.nodeLabels.add(-1);
			graph.edgeLabels.add(new ArrayList<Integer>());
			graph.edgeNexts.add(new ArrayList<Integer>());
		}

		// 首先根据图编码中的边五元组构造图
		for (int i = 0; i < gc.getEdgeSeq().size(); i++) {
			e = gc.getEdgeSeq().get(i);
			id1 = e.ix;
			id2 = e.iy;

			graph.nodeLabels.set(id1, e.x);
			graph.nodeLabels.set(id2, e.y);
			graph.edgeLabels.get(id1).add(e.a);
			graph.edgeLabels.get(id2).add(e.a);
			graph.edgeNexts.get(id1).add(id2);
			graph.edgeNexts.get(id2).add(id1);
		}

		DFSCodeTraveler dTraveler = new DFSCodeTraveler(gc.getEdgeSeq(), graph);
		dTraveler.traveler();
		if (!dTraveler.isMin) {
			return;
		}

		// 如果当前是最小编码则将此图加入到结果集中
		resultGraphs.add(graph);
		Edge e1;
		ArrayList<Integer> gIds;
		SubChildTraveler sct;
		ArrayList<Edge> edgeArray;
		// 添加潜在的孩子边，每条孩子边所属的图id
		HashMap<Edge, ArrayList<Integer>> edge2GId = new HashMap<>();
		for (int i = 0; i < gc.gs.size(); i++) {
			int id = gc.gs.get(i);

			// 在此结构的条件下，在多加一条边构成子图继续挖掘
			sct = new SubChildTraveler(gc.edgeSeq, totalGraphs.get(id));
			sct.traveler();
			edgeArray = sct.getResultChildEdge();

			// 做边id的更新
			for (Edge e2 : edgeArray) {
				if (!edge2GId.containsKey(e2)) {
					gIds = new ArrayList<>();
				} else {
					gIds = edge2GId.get(e2);
				}

				gIds.add(id);
				edge2GId.put(e2, gIds);
			}
		}

		for (Map.Entry entry : edge2GId.entrySet()) {
			e1 = (Edge) entry.getKey();
			gIds = (ArrayList<Integer>) entry.getValue();

			// 如果此边的频度大于最小支持度值，则继续挖掘
			if (gIds.size() < minSupportCount) {
				continue;
			}

			GraphCode nGc = new GraphCode();
			nGc.edgeSeq.addAll(gc.edgeSeq);
			// 在当前图中新加入一条边，构成新的子图进行挖掘
			nGc.edgeSeq.add(e1);
			nGc.gs.addAll(gIds);

			if (e1.iy == next) {
				// 如果边的点id设置是为当前最大值的时候，则开始寻找下一个点
				subMining(nGc, next + 1);
			} else {
				// 如果此点已经存在，则next值不变
				subMining(nGc, next);
			}
		}
	}

	/**
	 * 输出频繁子图结果信息
	 */
	public void printResultGraphInfo(){
		System.out.println(MessageFormat.format("挖掘出的频繁子图的个数为：{0}个", resultGraphs.size()));
		System.out.println(resultGraphs);

	}

}
