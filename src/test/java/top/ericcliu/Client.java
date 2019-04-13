package top.ericcliu;

import java.io.File;
import java.io.IOException;

/**
 * gSpan频繁子图挖掘算法
 * @author lyq
 *
 */
public class Client {
    public static void main(String[] args) throws IOException {
        //测试数据文件地址

        // 通过新建文件获取当前地址
        File file = new File("");
        System.out.println(file.getAbsolutePath());
        System.out.println(file.getCanonicalPath());
        System.out.println(file.getPath());
        // 相对地址方式1:  但由于实际项目在打包后没有src目录 所以这种方法不常用
        String filePath = ".\\src\\test\\java\\top\\ericcliu\\input.txt";

        double minSupportRate = 0.2;

        GSpanTool tool = new GSpanTool(filePath, minSupportRate);
        tool.mine();
    }
}
