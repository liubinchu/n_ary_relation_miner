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
        //int[] bioportalType = new int[]{14839557, 12043923, 8980706, 8980520, 8980358, 8980122, 8980078, 7603960, 173574, 10214, 11104698, 8980246, 12330475, 12330515, 12330997, 7748, 10894041, 8980625, 8980977, 8980339, 8980293, 8981142, 8980377, 8980338, 8980296, 8980289, 8980299, 8981081, 11143258, 8980466, 8980448, 9, 13165860, 7103, 10680, 10667208, 11260, 5534745, 11143468};
        // purity threshold 0.6
        int[] bioportalType = new int[]{14839557, 12043923, 11104698, 8980706, 8980520, 8980358, 8980246, 8980122, 8980078, 7603960, 173574, 10214, 12330475, 12330515, 12330997, 7748, 10894041, 8980625, 8980977, 8980339, 8980293, 8981142, 8980377, 8980338, 8980296, 8980289, 8980299, 8981081, 11143258, 8980466, 8980448, 9, 13165860, 7103, 10680, 10667208, 11260, 5534745, 11143468, 11106417, 10566, 10892141, 14795034, 14839589, 13165529, 13164561, 13165240, 12330589, 14963072, 14840947, 14839568, 14795019, 14794918, 12372259, 12355066, 12332927, 12332587, 12332304, 12331897, 12331846, 12331469, 12331433, 12331342, 12331100, 12331008, 12331000, 12330964, 12330927, 12330765, 12330709, 12330641, 12330461, 12044644, 12043942, 12043615, 12029100, 12029090, 11143323, 11143296, 11143212, 11143198, 11143158, 11143074, 10918176, 10900746, 10892247, 10889932, 10888740, 10888581, 10886385, 10668901, 10667666, 10667643, 10667409, 10667369, 10667294, 10667248, 10667197, 10667155, 10667146, 10667118, 10667108, 10667105, 10667104, 10667099, 10667098, 10667096, 10667069, 10667051, 10541756, 10485791, 10485459, 10422797, 10422796, 10422512, 10422374, 10422350, 10422343, 10392321, 9678180, 9677953, 8981760, 8981211, 8980795, 8980669, 8980591, 8980583, 8980506, 8980407, 8980233, 8980201, 8980200, 8980196, 8980136, 8980116, 6881082, 5613484, 5609791, 5592154, 5585078, 5542263, 5541267, 3532036, 3532035, 3532034, 333477, 333365, 331315, 331209, 326067, 318727, 266425, 257944, 231833, 180194, 179645, 177988, 176889, 171780, 148642, 37670, 10303, 10252, 8673, 8353, 7448, 7139, 7138, 4719, 5};
        int[] bioportalTrue = new int[]{14839557, 12043923, 8980706, 8980520, 8980358, 8980122, 8980078, 7603960, 173574, 10214, 12330515, 12330997, 7748, 8980625, 8980977, 8980339, 8980293, 8981142, 8980377, 8980338, 8980296, 8980289, 8980299, 8981081, 8980466, 8980448, 9, 13165860, 7103, 10667208, 11260, 5534745, 11143468};
        String bioportalResPath = "E:\\master_thesis_experinment\\Bioportal\\result";
        //evaluate(bioportalType, bioportalTrue, bioportalResPath);
        int[] linkedMDBType = new int[]{1021634, 836892, 704535, 652204, 837819, 842408, 802569, 2058314, 1113141};
        //int[] linkedMDBType = new int[]{1021634, 836892, 704535, 652204, 837819, 842408, 802569, 2058314, 1113141, 803434, 195607, 2123428, 1858106, 2047588, 2047310, 2006912, 2003552, 2003516, 2001991, 1856726, 1851460, 1846914, 1834459, 1065017, 1062940, 1062844, 1062807, 1061058, 1040141, 1024790, 1023204, 1022438, 838517, 837861, 837600, 837392, 823270, 818650, 815404, 802104, 801142, 776083, 707741, 707664, 707526, 707525, 686812, 686747, 653849, 653191};
        int[] linekdMDBTrue = new int[]{836892, 704535, 652204, 842408, 802569, 2058314, 1113141};
        String linkedMDBResPath = "E:\\master_thesis_experinment\\LinkedMDB\\res";
        evaluate(linkedMDBType, linekdMDBTrue,linkedMDBResPath );
    }
}
