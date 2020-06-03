package top.ericcliu.tools;

import lombok.extern.log4j.Log4j2;

/**
 * @author liubi
 * @date 2019-11-22 15:42
 **/
@Log4j2
public class DepthPrinter {
    public static void log(int depth,String fileName) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append(i + 1).append(" ");
        }
        System.out.println(fileName+":     DEPTH: " + sb.toString());
    }
}
