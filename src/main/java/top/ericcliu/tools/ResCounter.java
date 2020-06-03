package top.ericcliu.tools;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liubi
 * @date 2020-05-30 14:54
 **/
public class ResCounter {
    public static Map<Integer, Integer> countResNum(String filePath, int[] types) {
        Map<Integer, Integer> resCounter = new HashMap<>(types.length);
        for (int type : types) {
            resCounter.put(type, 0);
        }
        File dir = new File(filePath);
        if (dir.isFile()) {
            throw new IllegalArgumentException(dir.getAbsolutePath() + "must be a dir");
        }
        File[] secondDirs = dir.listFiles();
        for (File secondDir : secondDirs) {
            if (secondDir.isFile()) {
                throw new IllegalArgumentException(secondDir.getAbsolutePath() + "must be a dir");
            }
            int counter = 0;
            for (File file : secondDir.listFiles()) {
                if (file.isFile()) {
                    counter++;
                }
            }
            String[] info = secondDir.getName().split("_");
            if (info.length > 5) {
                int type = Integer.parseInt(info[4].replace(".json", ""));
                resCounter.put(type, counter);
            }
            //System.out.println(secondDir.getName() + "has " + counter + " files");
        }
        for (Map.Entry<Integer, Integer> entry : resCounter.entrySet()) {
            System.out.println(entry.getKey() + "," + entry.getValue());
        }
        return resCounter;
    }

    public static void main(String[] args) {
        int[] bioportalType = new int[]{8980122, 8980078, 8980358, 8980706, 10422374, 10667294, 173574, 12331100, 12331469, 12331846, 179645, 180194, 7603960, 14839557, 14963072, 12330515, 8980200, 8980201, 7748, 12330997, 10667099, 8980625, 8980977, 8980591, 11143323, 10667098, 12332304, 11143258, 8980339, 8980293, 8981142, 8980338, 8980377, 8980296, 8980289, 8980299, 9, 10422350, 12043923, 8981081, 8980448, 8980466, 10888581, 10918176, 10889932, 10667208, 13165860, 11143468, 11260, 5534745, 14839589, 5, 10214, 37670, 266425, 331209, 257944, 333365, 8980520, 8980136, 8980407, 8980583, 8980246, 8980506, 9677953, 10422343, 10422512, 10485791, 10667069, 10667197, 10667146, 5541267, 10667248, 10667051, 5542263, 5585078, 10667409, 5592154, 10667666, 10886385, 11143212, 12043942, 12043615, 12044644, 12332587, 12355066, 12372259, 6881082, 14840947, 12330475, 10485459, 331315, 12330641, 12330461, 14794918, 10667155, 10667105, 10541756, 10667096, 5609791, 11143158, 10667369, 10668901, 10667104, 10894041, 3532035, 3532034, 5613484, 10667118, 12330927, 176889, 231833, 3532036, 12330709, 12331342, 10667108, 10303, 12330765, 11104698, 12330964, 8981211, 12331008, 7103, 8673, 10680, 10892247, 8980196, 8981760, 10667643, 9678180, 8980233, 12332927, 11106417, 12029090, 12331897, 8980795, 10252, 10888740, 8980669, 326067, 12331000, 10566, 10422797, 10422796, 14839568, 10392321, 12029100, 10900746, 11143074, 11143296, 171780, 14795034, 13165529, 12331433, 13164561, 8980116, 11143198, 12330589, 13165240, 177988, 10892141, 8353, 148642, 7139, 4719, 7448, 7138, 14795019, 318727, 333477};
        int[] linkedMDBType = new int[]{823270, 1834459, 1061058, 1024790, 837861, 1062844, 1065017, 1023204, 653849, 818650, 815404, 1022438, 707526, 652204, 653191, 1062807, 1846914, 686747, 704535, 707525, 707664, 801142, 802104, 1856726, 1040141, 837600, 837392, 2001991, 2003516, 2006912, 1021634, 2047310, 2047588, 837819, 842408, 838517, 802569, 1062940, 836892, 686812, 776083, 2058314, 1113141, 2123428, 803434, 195607, 707741, 1851460, 1858106, 2003552};
/*
        String BioportalPath1 = "E:\\master_thesis_experinment\\Bioportal\\MLNaryRelation_Thresh_0.1D_3Related_Ratio_0.1";
        String BioportalPath2 = "E:\\master_thesis_experinment\\Bioportal\\MLNaryRelation_Thresh_0.2D_3Related_Ratio_0.1";
        String BioportalPath3 = "E:\\master_thesis_experinment\\Bioportal\\MLNaryRelation_Thresh_0.3D_3Related_Ratio_0.1";
        String BioportalPath4 = "E:\\master_thesis_experinment\\Bioportal\\MLNaryRelation_Thresh_0.4D_3Related_Ratio_0.1";
        String BioportalPath5 = "E:\\master_thesis_experinment\\Bioportal\\MLNaryRelation_Thresh_0.5D_3Related_Ratio_0.1";

        String LinkedMDBPath1 = "E:\\master_thesis_experinment\\LinkedMDB\\MLNaryRelation_Thresh_0.1D_3Related_Ratio_0.1";
        String LinkedMDBPath2 = "E:\\master_thesis_experinment\\LinkedMDB\\MLNaryRelation_Thresh_0.2D_3Related_Ratio_0.1";
        String LinkedMDBPath3 = "E:\\master_thesis_experinment\\LinkedMDB\\MLNaryRelation_Thresh_0.3D_3Related_Ratio_0.1";
        String LinkedMDBPath4 = "E:\\master_thesis_experinment\\LinkedMDB\\MLNaryRelation_Thresh_0.4D_3Related_Ratio_0.1";
        String LinkedMDBPath5 = "E:\\master_thesis_experinment\\LinkedMDB\\MLNaryRelation_Thresh_0.5D_3Related_Ratio_0.1";
*/
        String bioportalPath = args[0];
        Map<Integer, Integer> res = countResNum(bioportalPath, linkedMDBType);
    }
}
