package top.ericcliu;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

/**
 * @author liubi
 * @date 2019-04-10 21:22
 **/
public class teeTest {
    public static void main(String[] args) throws IOException {
        File dir = new File("dirTest/dirTest1");
        if(!dir.exists()){
            dir.mkdirs();
        }
        Integer integer = Math.toIntExact(10L);
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("dirTest/dirTest1"+File.separator+"file2");
        mapper.writeValue(file,integer);
    }
}
