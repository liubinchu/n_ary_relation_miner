package top.ericcliu.ds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

import java.io.File;
import java.io.FileWriter;

/**
 * @author liubi
 * @date 2019-04-15 21:16
 **/
interface  SaveToFile {
    default boolean saveToFile(String filePath, boolean isAppend) throws Exception {
        File file = new File(filePath);
        FileWriter fileWriter;
        if(file.exists()){
            fileWriter = new FileWriter(filePath,isAppend);
        }
        else {
            fileWriter = new FileWriter(filePath);
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new GuavaModule());
        try {
            mapper.writeValue(fileWriter,this);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
