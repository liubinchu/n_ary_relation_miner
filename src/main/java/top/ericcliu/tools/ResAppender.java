package top.ericcliu.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import top.ericcliu.ds.CombinedRes;
import top.ericcliu.ds.MLDFScodeString;
import top.ericcliu.ds.SeedString;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liubi
 * @date 2020-06-02 18:19
 **/
public class ResAppender {
    private final static String DATA_BASE_PATH = "E:\\linkedmdb.sqlite";
    //private final static String DATA_BASE_PATH = "E:\\bioportal.sqlite";

    public static void combineRes(ArrayList<SeedString> seeds, String resPath, int[] humanTrue) throws Exception {
        Map<Integer, SeedString> seedMap = new HashMap<>(seeds.size());
        Map<Integer, CombinedRes> combineResMap = new HashMap<>(humanTrue.length);
        // init seedMap
        for (SeedString seed : seeds) {
            seedMap.put(Integer.parseInt(seed.getTypeId()), seed);
        }

        for (int type : humanTrue) {
            combineResMap.put(type, new CombinedRes(seedMap.get(type)));
        }
        File dir = new File(resPath);
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException(dir.getAbsolutePath() + "must be a directory");
        }
        for (File threshDir : dir.listFiles()) {
            if (!threshDir.isDirectory()) {
                continue;
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
                        MLDFScodeString resInFile = MLDFScodeString.readFromFile(res.getAbsolutePath());
               /*         BufferedReader reader = new BufferedReader(new FileReader(res));
                        String line;
                        StringBuilder resFileStringsb = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            resFileStringsb.append(line).append(File.separator);
                        }*/
                        if (combineResMap.containsKey(typeId)) {
                            combineResMap.get(typeId).setThresholdRes(thresh,resInFile);
       /*                     combineResMap.get(typeId)[thresh - 1] = "\"" + thresh + " :\"" +
                                    resFileStringsb.toString();*/
                        }
                       // reader.close();
                    }
                }
            }
        }
        File combineResFile = new File(resPath + "\\combineRes.json");
   /*     if(!combineResFile.createNewFile()){
            throw new FileExistsException("create file failed");
        }*/

        // 增加jackson 对google guava的支持
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new GuavaModule());
        if (combineResFile.exists()) {
            throw new Exception("file already exist");
        } else if (combineResFile.createNewFile()) {
            mapper.writeValue(combineResFile, combineResMap);
        } else {
            throw new Exception("create file failed");
        }
/*        BufferedWriter writer = new BufferedWriter(new FileWriter(combineResFile));
        for (int typeId : combineResMap.keySet()) {
            if (!seedMap.containsKey(typeId)) {
                continue;
            }
            String seedString = seedMap.get(typeId).toJsonString();
            writer.write(seedString);
            for (int i = 0; i < 5; i++) {
                writer.write(combineResMap.get(typeId)[i]);
            }
            writer.flush();
        }
        writer.close();*/
    }

    public static void main(String[] args) throws Exception {
/*        int[] bioportalType = new int[]{14839557, 12043923, 8980706, 8980520, 8980358, 8980122, 8980078, 7603960, 173574, 10214, 11104698, 8980246, 12330475, 12330515, 12330997, 7748, 10894041, 8980625, 8980977, 8980339, 8980293, 8981142, 8980377, 8980338, 8980296, 8980289, 8980299, 8981081, 11143258, 8980466, 8980448, 9, 13165860, 7103, 10680, 10667208, 11260, 5534745, 11143468};
        // purity threshold 0.6
        int[] bioportalTrue = new int[]{14839557, 12043923, 8980706, 8980520, 8980358, 8980122, 8980078, 7603960, 173574, 10214, 12330515, 12330997, 7748, 8980625, 8980977, 8980339, 8980293, 8981142, 8980377, 8980338, 8980296, 8980289, 8980299, 8981081, 8980466, 8980448, 9, 13165860, 7103, 10667208, 11260, 5534745, 11143468};
        String bioportalResPath = "E:\\master_thesis_experinment\\Bioportal\\result";
        ArrayList<SeedString> bioSeeds = SeedsCalculator.calculateSeeds(false, false, false, DATA_BASE_PATH);
        combineRes(bioSeeds, bioportalResPath, bioportalTrue);*/

        int[] linkedMDBType = new int[]{1021634, 836892, 704535, 652204, 837819, 842408, 802569, 2058314, 1113141};
        int[] linekdMDBTrue = new int[]{836892, 704535, 652204, 842408, 802569, 2058314, 1113141};
        String linkedMDBResPath = "E:\\master_thesis_experinment\\LinkedMDB\\res";
        ArrayList<SeedString> linkedMDBSeeds = SeedsCalculator.calculateSeeds(false, false,
                false, DATA_BASE_PATH);
        combineRes(linkedMDBSeeds, linkedMDBResPath, linekdMDBTrue);

    }
}
