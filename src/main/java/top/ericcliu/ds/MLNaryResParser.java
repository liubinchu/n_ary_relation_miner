package top.ericcliu.ds;

import lombok.extern.log4j.Log4j2;
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
/*    public static void parseAll(String dirPath, String dataBasePath) throws Exception {
        //结果保存在第二级目录中
        try {
            File dirFile = new File(dirPath);
            File[] files;
            if (dirFile.isDirectory()) {
                files = dirFile.listFiles();
            } else {
                throw new Exception("dirPath must be a dir");
            }
            String noDupDirPath = dirPath + File.separator + "noDup";
            File noDupDir = new File(noDupDirPath);
            if (!noDupDir.exists()) {
                noDupDir.mkdirs();
            }
            ArrayList<Pair<File, Map<Integer, MLDFScode>>> mlDFScodes = new ArrayList<>();
            for (File dir : files) {
                if (!dir.isDirectory()) {
                    continue;
                }
                File[] reFiles = dir.listFiles();
                Map<Integer, MLDFScode> currentMap = new HashMap<>();
                if (reFiles.length != 0) {
                    System.out.println("From" + reFiles[0]);
                    System.out.println("To" + reFiles[reFiles.length - 1]);
                }
                for (File reFile : reFiles) {
                    String fileName = reFile.getName();
                    if (fileName.length() >= 2 && fileName.charAt(0) == 'R' && fileName.charAt(1) == 'E' && reFile.length() > 1) {
                        int relationId = Integer.parseInt(fileName.split("Id_")[1].replace(".json", ""));
                        //System.out.println(dir.getAbsolutePath() + File.separator + fileName);
                        MLDFScode mldfScode = MLDFScode.readFromFile(dir.getAbsolutePath() + File.separator + fileName);
                        currentMap.put(relationId, mldfScode);
                    }
                }
                mlDFScodes.add(new Pair<>(dir, currentMap));
            }
            for (Pair<File, Map<Integer, MLDFScode>> dFScodeOfFile : mlDFScodes) {
                File graphFile = dFScodeOfFile.getKey();
                String[] split = graphFile.getName().split("T_|.json");
                int typeId;
                try {
                    typeId = Integer.parseInt(graphFile.getName().split("T_|.json")[1]);
                } catch (Exception e) {
                    System.out.println(graphFile.getName());
                    throw e;
                }

                Map<Integer, MLDFScode> map = dFScodeOfFile.getValue();
                if (map.isEmpty()) {
                    continue;
                } else if (map.size() == 1) {
                    //new MLDFScodeString(map.get(0), dataBasePath, typeId).saveToFile(graphFile.getAbsolutePath() + File.separator + "READRE_" + graphFile.getName() + "Id_1.json", false);
                    new MLDFScodeString(map.get(0), dataBasePath, typeId).saveToFile(noDupDirPath + File.separator + "READRE_" + graphFile.getName() + "Id_1.json", false);
                    System.out.println("GENERATE: " + noDupDirPath + File.separator + "READRE_" + graphFile.getName() + "Id_1.json");
                    // id start from 0
                } else {
                    for (int i = 0; i < map.size(); i++) {
                        boolean flag = true;
                        MLDFScode currentDFScode = map.get(i);
                        for (int j = 0; j < map.size(); j++) {
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
                            new MLDFScodeString(currentDFScode, dataBasePath, typeId).saveToFile(noDupDirPath + File.separator + "READRE_" + graphFile.getName() + "Id_" + i + ".json", false);
                            System.out.println("GENERATE: " + noDupDirPath + File.separator + "READRE_" + graphFile.getName() + "Id_" + i + ".json");
                            //new MLDFScodeString(currentDFScode, dataBasePath, typeId).saveToFile(graphFile.getAbsolutePath() + File.separator + "READRE_" + graphFile.getName() + "Id_" + i + ".json", false);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }*/

    public static void parseAll(String dirPath, String dataBasePath, String resDirPath) throws Exception {
        //结果保存在第二级目录中
        try {
            File dirFile = new File(dirPath);
            File[] files;
            if (!dirFile.isDirectory()) {
                log.error(" \"dirPath\" must be a dir :" + dirFile);
                return;
            }
            for (File secondDir : dirFile.listFiles()) {
                log.info("start: " + secondDir);
                parse(secondDir.getAbsolutePath(), dataBasePath, resDirPath);
                log.info("finish: " + secondDir);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
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
                log.error(dirFile.getName()+" is not a relation dir");
                log.error(e.getMessage());
                return;
            }
            if (!dirFile.isDirectory()) {
                log.error(" \"dirPath\" must be a dir :" + dirFile);
                return;
            }
            File[] reFiles = dirFile.listFiles();
            Map<Integer, MLDFScode> map = new HashMap<>(reFiles.length);
            for (File reFile : reFiles) {
                String fileName = reFile.getName();
                if (fileName.length() >= 2 && fileName.charAt(0) == 'R' && fileName.charAt(1) == 'E' && reFile.length() > 1) {
                    int relationId = Integer.parseInt(fileName.split("Id_")[1].replace(".json", ""));
                    MLDFScode mldfScode = MLDFScode.readFromFile(reFile.getAbsolutePath());
                    map.put(relationId, mldfScode);
                }
            }
            log.info("relation numbers: " + map.size());
            if (map.size() == 1) {
                new MLDFScodeString(map.get(0), dataBasePath, typeId).saveToFile(resDirPath + File.separator + "READRE_" + dirFile.getName() + "Id_0.json", false);
                log.info("GENERATE: " + resDirPath + File.separator + "READRE_" + dirFile.getName() + "Id_0.json");
                // id start from 1
            } else {
                for (int i = 0; i < map.size(); i++) {
                    boolean flag = true;
                    MLDFScode currentDFScode = map.get(i);
                    for (int j = 0; j < map.size(); j++) {
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
                        new MLDFScodeString(currentDFScode, dataBasePath, typeId).saveToFile(resDirPath + File.separator + "READRE_" + dirFile.getName() + "Id_" + i + ".json", false);
                        log.info("GENERATE: " + resDirPath + File.separator + "READRE_" + dirFile.getName() + "Id_" + i + ".json");
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        String dirPath = "D:\\OneDrive - Monash University\\WDS\\n_ary_relation_miner\\MLNaryRelation_Thresh_0.1D_10Related_Ratio_0.001";
/*        String dirPath = args[0];
        String dbPath = args[1];*/
        MLNaryResParser.parseAll(dirPath, "D:\\bioportal.sqlite", dirPath + File.separator + "noDup");
    }
}
