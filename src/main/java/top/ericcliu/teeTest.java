package top.ericcliu;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.jena.base.Sys;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
        LinkedList<Integer> rightMostPath = new LinkedList<>();
        rightMostPath.add(1);
        rightMostPath.add(2);
        rightMostPath.add(3);
        rightMostPath.add(4);
        rightMostPath.add(5);
        rightMostPath.add(6);
        Iterator<Integer> descRMPit = rightMostPath.descendingIterator();

        System.out.println("rightMostPath.getLast():    "+rightMostPath.getLast());
        while (descRMPit.hasNext()){
            System.out.println(descRMPit.next());
        }

    }
}
