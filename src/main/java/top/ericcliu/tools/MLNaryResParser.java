package top.ericcliu.tools;

import lombok.extern.log4j.Log4j2;
import top.ericcliu.ds.EchartsTree;
import top.ericcliu.ds.MLDFScode;
import top.ericcliu.ds.MLDFScodeString;
import top.ericcliu.util.ResParseUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liubi
 * @date 2019-11-21 14:51
 **/
@Log4j2
public class MLNaryResParser {

    public static void parseAll(String dirPath, String dataBasePath, String resDirPath) throws Exception {
        //结果保存在第二级目录中
        try {
            File dirFile = new File(dirPath);
            if (!dirFile.isDirectory()) {
                System.out.println(" \"dirPath\" must be a dir :" + dirFile);
                return;
            }
            for (File secondDir : dirFile.listFiles()) {
                System.out.println("start: " + secondDir);
                parse(secondDir.getAbsolutePath(), dataBasePath, resDirPath);
                System.out.println("finish: " + secondDir);
                System.gc();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
    }

    public static void parse(String dirPath, String dataBasePath, String resDirPath) throws Exception {
        //结果保存在第一级目录中
        try {
            ResParseUtil.mkdirs(resDirPath);
            File dirFile = new File(dirPath);
            int typeId;
            try {
                typeId = Integer.parseInt(dirFile.getName().split("T_|.json")[1]);
            } catch (Exception e) {
                System.out.println(dirFile.getName() + " is not a relation dir");
                System.out.println(e.getMessage());
                return;
            }
            if (!dirFile.isDirectory()) {
                System.out.println(" \"dirPath\" must be a dir :" + dirFile);
                return;
            }
            File[] reFiles = dirFile.listFiles();
            System.out.println("relation numbers: " + reFiles.length);
            Map<Integer, MLDFScode> map = new HashMap<>(reFiles.length);
            int minRelationId = Integer.MAX_VALUE;
            int maxRelationId = Integer.MIN_VALUE;
            for (File reFile : reFiles) {
                String fileName = reFile.getName();
                if (fileName.length() >= 2 && fileName.charAt(0) == 'R' && fileName.charAt(1) == 'E' && reFile.length() > 1) {
                    int relationId = Integer.parseInt(fileName.split("Id_")[1].replace(".json", ""));
                    if (relationId > reFiles.length - 100) {
                        // 仅取relationId最大的100个
                        minRelationId = Math.min(minRelationId, relationId);
                        maxRelationId = Math.max(maxRelationId, relationId);
                        MLDFScode mldfScode = MLDFScode.readFromFile(reFile.getAbsolutePath());
                        map.put(relationId, mldfScode);
                    }
                }
            }

            new EchartsTree(map.get(maxRelationId), dataBasePath, typeId).saveToFile(resDirPath + File.separator + "TREE_" + dirFile.getName() + "Id_" + maxRelationId + ".json", false);
            new MLDFScodeString(map.get(maxRelationId), dataBasePath, typeId).saveToFile(resDirPath + File.separator + "READRE_" + dirFile.getName() + "Id_" + maxRelationId + ".json", false);
            System.out.println("GENERATE: " + resDirPath + File.separator + "READRE_" + dirFile.getName() + "Id_" + maxRelationId + ".json");



 /*           if (map.size() == 1) {
                new MLDFScodeString(map.get(0), dataBasePath, typeId).saveToFile(resDirPath + File.separator + "READRE_" + dirFile.getName() + "Id_0.json", false);
                new EchartsTree(map.get(0), dataBasePath, typeId).saveToFile(resDirPath + File.separator + "TREE_" + dirFile.getName() + "Id_0.json", false);
                System.out.println("GENERATE: " + resDirPath + File.separator + "READRE_" + dirFile.getName() + "Id_0.json");
                // id start from 1
            } else {
                for (int i = minRelationId; i < map.size(); i++) {
                    boolean flag = true;
                    MLDFScode currentDFScode = map.get(i);
                    for (int j = minRelationId; j < map.size(); j++) {
                        if (i == j) {
                            continue;
                        }
                        MLDFScode nextDFScode = map.get(j);
                        int mode = currentDFScode.isParentOf(nextDFScode);
                        if (mode == 0 || (mode == 1 && currentDFScode.getMNI() < nextDFScode.getMNI())) {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        new EchartsTree(currentDFScode, dataBasePath, typeId).saveToFile(resDirPath + File.separator + "TREE_" + dirFile.getName() + "Id_" + i + ".json", false);
                        new MLDFScodeString(currentDFScode, dataBasePath, typeId).saveToFile(resDirPath + File.separator + "READRE_" + dirFile.getName() + "Id_" + i + ".json", false);
                        System.out.println("GENERATE: " + resDirPath + File.separator + "READRE_" + dirFile.getName() + "Id_" + i + ".json");
                    }
                }
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
       String LinekedMDBdirPath1 = "E:\\master_thesis_experinment\\LinkedMDB\\MLNaryRelation_Thresh_0.1D_3Related_Ratio_0.1";
        String LinekedMDBdirPath2 = "E:\\master_thesis_experinment\\LinkedMDB\\MLNaryRelation_Thresh_0.2D_3Related_Ratio_0.1";
        String LinekedMDBdirPath3 = "E:\\master_thesis_experinment\\LinkedMDB\\MLNaryRelation_Thresh_0.3D_3Related_Ratio_0.1";
        String LinekedMDBdirPath4 = "E:\\master_thesis_experinment\\LinkedMDB\\MLNaryRelation_Thresh_0.4D_3Related_Ratio_0.1";
        String LinekedMDBdirPath5 = "E:\\master_thesis_experinment\\LinkedMDB\\MLNaryRelation_Thresh_0.5D_3Related_Ratio_0.1";

        String bioportaldirPath1 = "E:\\master_thesis_experinment\\Bioportal\\MLNaryRelation_Thresh_0.1D_3Related_Ratio_0.1";
        String bioportaldirPath2 = "E:\\master_thesis_experinment\\Bioportal\\MLNaryRelation_Thresh_0.2D_3Related_Ratio_0.1";
        String bioportaldirPath3 = "E:\\master_thesis_experinment\\Bioportal\\MLNaryRelation_Thresh_0.3D_3Related_Ratio_0.1";
        String bioportaldirPath4 = "E:\\master_thesis_experinment\\Bioportal\\MLNaryRelation_Thresh_0.4D_3Related_Ratio_0.1";
        String bioportaldirPath5 = "E:\\master_thesis_experinment\\Bioportal\\MLNaryRelation_Thresh_0.5D_3Related_Ratio_0.1";

        String linkedMDB = "E:\\linkedmdb.sqlite";
        String bioportalDB = "E:\\bioportal.sqlite";

       /* MLNaryResParser.parseAll(LinekedMDBdirPath1, linkedMDB, LinekedMDBdirPath1 + File.separator + "noDup");
        MLNaryResParser.parseAll(LinekedMDBdirPath2, linkedMDB, LinekedMDBdirPath2 + File.separator + "noDup");
        MLNaryResParser.parseAll(LinekedMDBdirPath4, linkedMDB, LinekedMDBdirPath4 + File.separator + "noDup");
        MLNaryResParser.parseAll(LinekedMDBdirPath5, linkedMDB, LinekedMDBdirPath5 + File.separator + "noDup");
      */
        //MLNaryResParser.parseAll(bioportaldirPath5, bioportalDB, bioportaldirPath5 + File.separator + "noDup");

        String dirPath = args[0];
        String dbPath = args[1];
        MLNaryResParser.parseAll(dirPath, dbPath, dirPath + File.separator + "noDup");
    }
}
