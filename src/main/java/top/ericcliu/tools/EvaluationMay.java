package top.ericcliu.tools;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author liubi
 * @date 2020-05-31 17:49
 **/
public class EvaluationMay {
    public static void evaluate(int[] allTypes, int[] humanTrue, String dirPath) {
        // init
        Map<Integer, int[]> matrix = new HashMap<>(allTypes.length);
        for (int type : allTypes) {
            matrix.put(type, new int[]{0, 0, 0, 0, 0, 0});
        }
        Set<Integer> humanTrueSet = new HashSet<>();
        for (int trueType : humanTrue) {
            if (matrix.containsKey(trueType)) {
                matrix.get(trueType)[0] = 1;
                humanTrueSet.add(trueType);
            }
        }

        Set<Integer>[] TP = new Set[]{new HashSet(), new HashSet(), new HashSet(), new HashSet(), new HashSet()};
        Set<Integer>[] TPAndFP = new Set[]{new HashSet(), new HashSet(), new HashSet(), new HashSet(), new HashSet()};
        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException(dir.getAbsolutePath() + "must be a directory");
        }
        for (File threshDir : dir.listFiles()) {
            if (!threshDir.isDirectory()) {
                throw new IllegalArgumentException(threshDir.getAbsolutePath() + "must be a directory");
            }
            for (File typeDir : threshDir.listFiles()) {
                if (!typeDir.isDirectory()) {
                    throw new IllegalArgumentException(typeDir.getAbsolutePath() + "must be a directory");
                }
                if (typeDir.getName().equals("noDup")) {
                    for (File res : typeDir.listFiles()) {
                        if (res.getName().startsWith("TREE")) {
                            continue;
                        }
                        String[] splitRes = res.getName().split("_");
                        int typeId = Integer.parseInt(
                                splitRes[5].replace(".json", ""));
                        int thresh = Integer.parseInt(
                                splitRes[7].replace("0.", "")
                                        .replace("Id", ""));
                        if (matrix.containsKey(typeId)) {
                            matrix.get(typeId)[thresh] = 1;
                            TPAndFP[thresh - 1].add(typeId);
                            if (humanTrueSet.contains(typeId)) {
                                TP[thresh - 1].add(typeId);
                            }
                        }
                    }
                }
            }
        }
        for (int type : allTypes) {
            StringBuilder labelsb = new StringBuilder();
            labelsb.append(type).append(",");
            for (int label : matrix.get(type)) {
                labelsb.append(label).append(",");
            }
            labelsb.deleteCharAt(labelsb.length() - 1);
            System.out.println(labelsb.toString());
        }
        StringBuilder Pre_sb = new StringBuilder();
        StringBuilder RE_sb = new StringBuilder();
        StringBuilder F1_sb = new StringBuilder();
        StringBuilder TP_sb = new StringBuilder();
        StringBuilder TPAndFP_sb = new StringBuilder();
        StringBuilder TPAndFN_sb = new StringBuilder();
        Pre_sb.append("Precision,");
        RE_sb.append("Recall,");
        F1_sb.append("F1,");
        for (int i = 0; i < 5; i++) {
            double precision = TP[i].size() / (double) TPAndFP[i].size();
            double recall = TP[i].size() / (double) humanTrue.length;
            double F1 = 2 * precision * recall / (precision + recall);
            Pre_sb.append(precision).append(",");
            RE_sb.append(recall).append(",");
            F1_sb.append(F1).append(",");
        }
        Pre_sb.deleteCharAt(Pre_sb.length() - 1).append("\n");
        RE_sb.deleteCharAt(RE_sb.length() - 1).append("\n");
        F1_sb.deleteCharAt(F1_sb.length() - 1).append("\n");
        System.out.println(Pre_sb.toString() + RE_sb.toString() + F1_sb.toString());
    }

    public static void main(String[] args) {
        int[] bioportalType = new int[]{14839557, 12043923, 8980706, 8980520, 8980358, 8980122, 8980078, 7603960, 173574, 10214, 11104698, 8980246, 12330475, 12330515, 12330997, 7748, 10894041, 8980625, 8980977, 8980339, 8980293, 8981142, 8980377, 8980338, 8980296, 8980289, 8980299, 8981081, 11143258, 8980466, 8980448, 9, 13165860, 7103, 10680, 10667208, 11260, 5534745, 11143468};
        // purity threshold 0.6
        int[] bioportalTrue = new int[]{14839557, 12043923, 8980706, 8980520, 8980358, 8980122, 8980078, 7603960, 173574, 10214, 12330515, 12330997, 7748, 8980625, 8980977, 8980339, 8980293, 8981142, 8980377, 8980338, 8980296, 8980289, 8980299, 8981081, 8980466, 8980448, 9, 13165860, 7103, 10667208, 11260, 5534745, 11143468};
        String bioportalResPath = "E:\\master_thesis_experinment\\Bioportal\\result";
        //evaluate(bioportalType, bioportalTrue,bioportalResPath );
        int[] linkedMDBType = new int[]{1021634, 836892, 704535, 652204, 837819, 842408, 802569, 2058314, 1113141};
        int[] linekdMDBTrue = new int[]{836892, 704535, 652204, 842408, 802569, 2058314, 1113141};
        String linkedMDBResPath = "E:\\master_thesis_experinment\\LinkedMDB\\res";
        evaluate(linkedMDBType, linekdMDBTrue,linkedMDBResPath );
    }
}
