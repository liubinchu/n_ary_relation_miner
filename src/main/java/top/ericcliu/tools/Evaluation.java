package top.ericcliu.tools;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import javafx.util.Pair;
import top.ericcliu.ds.MLDFScode;
import top.ericcliu.ds.MLDFScodeString;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author liubi
 * @date 2019-06-02 14:59
 **/
public class Evaluation {
    private static ArrayList<Pair<String, Boolean>> standard = new ArrayList<>();
    private static int relationNum = 0;
    private static int totalInstanceNum = 0;
    static {
        standard.add(new Pair<>("READRE_D_10P_1.0R_1.0T_5585078.jsonMNI_0.1Id_2.json",false));
        standard.add(new Pair<>("READRE_D_10P_1.0R_1.0T_8980506.jsonMNI_0.1Id_3.json",true));
    }

    /**
     *
     * @param filePath
     * @return List<Map<String,MLDFScodeString>> List 中 包含不同评估者的答案，Map的key是 文件名，value 是包含答案的DFScode
     * @throws Exception
     */
    private static List<Map<String, MLDFScodeString>> readJudgementFromFile(String filePath) throws Exception {
        File dir = new File(filePath);
        if(!dir.exists()||!dir.isDirectory()){
            throw new IllegalArgumentException("illegal file path ");
        }
        File[] judgeDirs = dir.listFiles();
        // 设置评估者的个数
        List<Map<String,MLDFScodeString>> judgements = new ArrayList<>(judgeDirs.length);
        for(File judgeDir: judgeDirs){
            if(!judgeDir.isDirectory()){
                throw new IllegalArgumentException("illegal file path ");
            }
            Map<String,MLDFScodeString> judgement = new HashMap<>(judgeDir.listFiles().length);
            judgements.add(judgement);
            File[] judgementFiles = judgeDir.listFiles();
            for(File judgementFile : judgementFiles){
                if(!judgementFile.isFile()){
                    throw new IllegalArgumentException("illegal file path ");
                }
                MLDFScodeString dfsCode = MLDFScodeString.readFromFile(judgementFile.getAbsolutePath());
                judgement.put(judgementFile.getName(),dfsCode);
                relationNum++;
                totalInstanceNum+=dfsCode.getInstanceNum();
            }
        }
        relationNum/=judgements.size();
        totalInstanceNum/=judgements.size();
        return judgements ;
    }

    private static double[] calJudgeConfidence(List<Map<String,MLDFScodeString>> judgements) throws Exception {
        int judgeNum = judgements.size();
        int[] judgeConfidence = new int[judgeNum];
        int confidenceSum = 0;
        for(int i=0;i<judgeNum;i++){
            Map<String,MLDFScodeString> judgement = judgements.get(i);
            int confidence = 0;
            for(Pair<String,Boolean> answer: standard){
                if(judgement.get(answer.getKey()).getNaryRelation().equals(answer.getValue())){
                    // 评估者答对预留的黄金问题
                    confidence++;
                }
            }
            judgeConfidence[i] = confidence;
            confidenceSum+=confidence;
        }
        double [] confidence = new double[judgeNum];
        for(int i=0;i<judgeNum;i++){
            confidence[i] = (double)judgeConfidence[i]/(double)confidenceSum;
        }
        return confidence;
    }

    /**
     * 根据confidence 对不同评估者评估结果 加权 >=0.5 true <=0.5 false
     * @param judgements
     * @param conficence
     */
    private static Map<String,MLDFScodeString> refineJudgements(List<Map<String,MLDFScodeString>> judgements, double[] conficence ){
        Map<String,MLDFScodeString> judgementsRefined = new HashMap<>(judgements.get(0));
        int judgeNum = judgements.size();
        for(Map.Entry<String,MLDFScodeString> judgement : judgementsRefined.entrySet()){
            double res = 0;
            for (int i=0;i<judgeNum;i++){
                if(judgements.get(i).get(judgement.getKey()).getNaryRelation()){
                    res += conficence[i];
                }
            }
            judgement.getValue().setNaryRelation(res>=0.5);
        }
        return judgementsRefined;
    }

    private static  Map<Integer,double[]> countByArity(Map<String,MLDFScodeString> refinedJudgements){
        // can alse re returned
        Multimap<Integer,MLDFScodeString> judgementsByArity = MultimapBuilder.hashKeys().hashSetValues().build();
        Map<Integer,double[]> statistics = new HashMap<>();
        for(Map.Entry<String,MLDFScodeString> judgement : refinedJudgements.entrySet()){
            MLDFScodeString dfsCode = judgement.getValue();
            int arity = dfsCode.getEdgeSeq().size()+1;
            judgementsByArity.put(arity,dfsCode);
            double[] statistic ;
            // precison; relationNum; average instanceNum per relation; total instanceNum; Relate Ratio
            if (!statistics.containsKey(arity)){
                statistic = new double[5];
            }else {
                statistic = statistics.get(arity);
            }
            if(dfsCode.getNaryRelation()){ statistic[0]++; }
            statistic[1]++;
            statistic[2]+=dfsCode.getInstanceNum();
            statistic[3]+=dfsCode.getInstanceNum();
            statistic[4]+=dfsCode.getRelatedRatio();
            statistics.put(arity,statistic);
        }

        for(Map.Entry<Integer,double[]> statisticArity : statistics.entrySet()){
            double [] statistic = statisticArity.getValue();
            statistic[0] = (double)statistic[0] / (double) statistic[1];
            statistic[2] = (double)statistic[2] / (double) statistic[1];
            statistic[4] = (double)statistic[4] / (double) statistic[1];
        }

        for (Map.Entry<Integer,double[]> statistic : statistics.entrySet()){
            System.out.println("Arity:"+new DecimalFormat("#").format(statistic.getKey())
                    +"  Relation Quantity:"+new DecimalFormat("#").format(statistic.getValue()[1])
                    +"  Average Instance Quantity per Relation:"+new DecimalFormat("#.0000").format(statistic.getValue()[2])
                    +"  Total Instance Quantity:"+new DecimalFormat("000.0000").format(statistic.getValue()[3])
                    +"  Average Related Ratio:"+new DecimalFormat("00.0000%").format(statistic.getValue()[4])
                    +"  Precision:"+new DecimalFormat("00.0000%").format(statistic.getValue()[0]));
        }
        return statistics;
    }

    private static  Map<Pair<Double,Double>,double[]> countByRelatedRatio(Map<String,MLDFScodeString> refinedJudgements, double[]ranges ){
        // range : (0,ranges[0]] (ranges[0],ranges[1]], ... ... , (ranges[n-1],1)
       Arrays.sort(ranges);
       if(ranges==null||ranges[0]<=0||ranges[ranges.length-1]>=1){
           throw new IllegalArgumentException("illegal range arguments");
       }
       // can alse re returned
        Multimap<Pair<Double,Double>,MLDFScodeString> judgementsByRelatedRatio = MultimapBuilder.hashKeys().hashSetValues().build();
        Map<Pair<Double,Double>,double[]> statistics = new HashMap<>();
        for(Map.Entry<String,MLDFScodeString> judgement : refinedJudgements.entrySet()){
            MLDFScodeString dfsCode = judgement.getValue();
            Pair<Double,Double> range = Evaluation.decideRange(dfsCode.getRelatedRatio(),ranges);
            judgementsByRelatedRatio.put(range,dfsCode);
            double[] statistic ;
            // precison; relationNum; average instanceNum per relation; total instanceNum
            if (!statistics.containsKey(range)){
                statistic = new double[5];
            }else {
                statistic = statistics.get(range);
            }
            if(dfsCode.getNaryRelation()){ statistic[0]++; }
            statistic[1]++;
            statistic[2]+=dfsCode.getInstanceNum();
            statistic[3]+=dfsCode.getInstanceNum();
            statistic[4]+=dfsCode.getRelatedRatio();
            statistics.put(range,statistic);
        }
        for(Map.Entry<Pair<Double,Double>,double[]> statisticRelatedRatio : statistics.entrySet()){
            double [] statistic = statisticRelatedRatio.getValue();
            statistic[0] = (double)statistic[0] / (double) statistic[1];
            statistic[2] = (double)statistic[2] / (double) statistic[1];
            statistic[4] = (double)statistic[4] / (double) statistic[1];
        }

       for (Map.Entry<Pair<Double,Double>,double[]> statisticRelatedRatio : statistics.entrySet()){
           System.out.println("Range:("+new DecimalFormat("0.000").format(statisticRelatedRatio.getKey().getKey())
                   +","+new DecimalFormat("0.000").format(statisticRelatedRatio.getKey().getValue())+"]"
                   +"  Relation Quantity:"+new DecimalFormat("#").format(statisticRelatedRatio.getValue()[1])
                   +"  Average Instance Quantity per Relation:"+new DecimalFormat("#.0000").format(statisticRelatedRatio.getValue()[2])
                   +"  Total Instance Quantity:"+new DecimalFormat("000.0000").format(statisticRelatedRatio.getValue()[3])
                   +"  Average Related Ratio:"+new DecimalFormat("00.0000%").format(statisticRelatedRatio.getValue()[4])
                   +"  Precision:"+new DecimalFormat("00.0000%").format(statisticRelatedRatio.getValue()[0]));
       }

        return statistics;
    }

    /**
     * 针对每一个不同的type, 统计related ratio 和 arity 之间的关系
     */
    private static void evaluateRelatedRatio(String dirPath,@Nonnull String resultDirName) throws Exception {
        double[] range = new double[]{0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9};
        File dirFile = new File(dirPath);
        File[] typeDirs;
        if (dirFile.isDirectory()) {
            typeDirs = dirFile.listFiles();
        } else {
            throw new Exception("dirPath must be a dir");
        }
        File resultDir = new File(dirPath+File.separator+resultDirName);
        if(!resultDir.exists()){
            resultDir.mkdirs();
        }
        for (File typeDir : typeDirs){
            evaluateRelatedRatioCore(typeDir,range,resultDir);
        }
    }
    private static Pair<Double,Double> decideRange(double value, double[] ranges){
        int upperBoundIndex = ranges.length-1;
        for (int i=0;i<ranges.length;i++){
            upperBoundIndex = i;
            if(ranges[i]>=value){
                break;
            }
        }
        double lowerBound, upperBound;
        if(upperBoundIndex==0){
            lowerBound = 0;
            upperBound = ranges[upperBoundIndex];
        }else if(ranges[upperBoundIndex]<value){
            lowerBound = ranges[upperBoundIndex];
            upperBound = 1;
        }else {
            lowerBound = ranges[upperBoundIndex-1];
            upperBound = ranges[upperBoundIndex];
        }
        Pair<Double,Double> range = new Pair<>(lowerBound,upperBound);
        return range;
    }
    private static Map<Integer,double[]> evaluateRelatedRatioCore(File typeDir,double[] ranges,File resultDir) throws Exception {
        if (!typeDir.isDirectory()) { return null; }
        File[] dfsCodeFiles = typeDir.listFiles();
        // can else returned
        Map<Integer,double[]> statistics = new HashMap<>();
        for (File dfsCodeFile : dfsCodeFiles){
            if(dfsCodeFile.getName().startsWith("IN_")){
                continue;
            }
            MLDFScode dfsCode = MLDFScode.readFromFile(dfsCodeFile.getAbsolutePath());
            int arity = dfsCode.getEdgeSeq().size()+1;
            double[] statistic ;
            // relationNum; Relate Ratio
            if (!statistics.containsKey(arity)){
                statistic = new double[2];
            }else {
                statistic = statistics.get(arity);
            }
            {
                //仅计算 所有，包括正反例
                statistic[0]++;
                statistic[1]+=dfsCode.getRelatedRatio();
            }
            statistics.put(arity,statistic);
        }
        if(statistics.size()>1){
            // arity 种类太少，不具有拟合的价值
            try{
                File resultFile = new File(resultDir.getAbsolutePath()+File.separator+typeDir.getName()+".csv");
                Writer writer = new FileWriter(resultFile);
                for(Map.Entry<Integer,double[]> statisticArity : statistics.entrySet()){
                    double [] statistic = statisticArity.getValue();
                    statistic[1] = (double)statistic[1] / (double) statistic[0];
                    writer.write(statisticArity.getKey()+","+(int)statisticArity.getValue()[0]+","+statisticArity.getValue()[1]+"\n");
                }
                writer.flush();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return statistics;
    }
    public static void main(String[] args) throws Exception {
/*        List<Map<String,MLDFScodeString>> judgements = Evaluation.readJudgementFromFile("D:\\judgement");
        double[] conficence = calJudgeConfidence(judgements);
        Map<String,MLDFScodeString> refinedJudgements = Evaluation.refineJudgements(judgements,conficence);
        System.out.println("quantity of relation:   "+relationNum);
        System.out.println("quantity of total instance:   "+totalInstanceNum);
        Map<Integer,double[]> statisticsArity =  Evaluation.countByArity(refinedJudgements);
        double[] ranges = new double[]{0.1,0.5,0.9};
        Map<Pair<Double,Double>,double[]> statisticsRelatedRatio = Evaluation.countByRelatedRatio(refinedJudgements,ranges);*/
        Evaluation.evaluateRelatedRatio("D:\\D_10P\\result_Thresh_0.3D_10related_ratio_1.0E-5",
                "result");
    }
}
