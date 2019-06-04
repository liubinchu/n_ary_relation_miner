package top.ericcliu.util;

import com.opencsv.CSVReader;

import javax.annotation.Nonnull;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author liubi
 * @date 2019-05-31 16:32
 **/
public class Assignment {
    private static ArrayList<LinkedList<String>> assign(@Nonnull String csvFilePath, int currentLevel ,boolean balance) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(csvFilePath));
        ArrayList<LinkedList<String>> fileLists = new ArrayList<>(currentLevel);
        for (int i=0;i<currentLevel;i++){
            fileLists.add(new LinkedList<>());
        }

        String[] line ;
        int i=-1;
        while ((line = csvReader.readNext()) != null) {
            fileLists.get((++i)%currentLevel).add(line[0]);
        }
        int smallToBig = 1;
        // 1 true -1 false
        for (LinkedList<String> fileList : fileLists){
            Iterator<String> it;
            System.out.print("'");
            if(smallToBig>0){
                it  =fileList.descendingIterator();
            }else {
                it  =fileList.iterator();
            }
            if(balance){
                smallToBig*=-1;
            }
            while (it.hasNext()){
                System.out.print(it.next()+"' '");
            }
            System.out.println();
            System.out.println();
        }
        return fileLists;
    }
    public static void main(String[] args) throws IOException {
        ArrayList<LinkedList<String>> fileLists = assign(
                "D:\\D_10P\\fileListSortedBigToSmall.csv",
                6,
                false);
    }
}
