package top.ericcliu.util;

import java.io.File;

/**
 * @author liubi
 * @date 2019-11-21 15:18
 **/
public class ResParseUtil {
    public static boolean mkdirs(String dirPath){
        File noDupDir = new File(dirPath);
        if (!noDupDir.exists()) {
            noDupDir.mkdirs();
        }
        return noDupDir.exists();
    }
}
