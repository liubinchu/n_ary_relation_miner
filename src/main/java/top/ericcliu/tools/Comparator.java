package top.ericcliu.tools;

import com.opencsv.CSVReader;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author liubi
 * @date 2019-12-03 16:25
 **/
@Log4j2
public class Comparator {
    private static Set<String> compare(@NonNull String large, @NonNull String small) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(large));
        String[] line;
        Set<String> largeSet = new HashSet<>();
        while ((line = csvReader.readNext()) != null) {
            largeSet.add(line[0]);
        }
        System.out.println("largeSet.size():    "+largeSet.size());
        System.out.println(largeSet);
        csvReader = new CSVReader(new FileReader(small));
        Set<String> smallSet = new HashSet<>();
        while ((line = csvReader.readNext()) != null) {
            smallSet.add(line[0]);
        }
        System.out.println("smallSet.size():    "+smallSet.size());
        System.out.println(smallSet);
        largeSet.removeAll(smallSet);

        System.out.println("remainSet.size():    "+largeSet.size());
        System.out.println(largeSet);
        return largeSet;
    }

    public static void main(String[] args) throws IOException {
        compare("D:\\D_10P\\fileListSortedBigToSmall.csv","D:\\D_10P\\ML\\5\\finished.csv");
    }
}
