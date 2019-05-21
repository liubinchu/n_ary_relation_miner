/*
 * Copyright (c) @ EriccLiu 2018.
 */

package top.ericcliu.util;

import javafx.util.Pair;

import java.util.*;

/**
 * @author liubi
 * @date 2018-12-04 21:30
 * 实现了排列数 组合数 排列 组合
 */
public class ArrangeCombination {
    /**
     * 计算阶乘数，即n! = n * (n-1) * ... * 2 * 1
     * @param n
     * @return
     */
    private static long factorial(int n) {
        return (n > 1) ? n * factorial(n - 1) : 1;
    }

    /**
     * 计算排列数，即A(n, m) = n!/(n-m)!
     * @param n
     * @param m
     * @return
     */
    public static long arrangement(int n, int m) {
        return (n >= m) ? factorial(n) / factorial(n - m) : 0;
    }

    /**
     * 计算组合数，即C(n, m) = n!/((n-m)! * m!)
     * @param n
     * @param m
     * @return
     */
    public static long combination(int n, int m) {
        return (n >= m) ? factorial(n) / factorial(n - m) / factorial(m) : 0;
    }
    /**
     * 排列选择（从列表中选择n个排列）
     * @param dataSet 待选择的元素集合
     * @param n 选择个数
     * @param print 是否打印排列数
     */
    public static <T> Set<List<T>> arrangementSelect(Set<T> dataSet, int n,boolean print) {
        if(print){
            System.out.println(String.format("A(%d, %d) = %d", dataSet.size(), n, arrangement(dataSet.size(), n)));
        }
        Set<List<T>> result = new HashSet<>();
        arrangementSelect(dataSet,new ArrayList<T>(),n,result);
        return  result;
    }

    /**
     * 排列选择
     * @param dataSet 待排列元素集合 dataSet 中 不存在重复集合
     * @param arrangement  一个排列
     * @param num 排列的元素个数 也即A（m，n） 中的n
     * @param result 结果集合
     */
    private static <T> void  arrangementSelect(Set<T> dataSet,List<T> arrangement,int num,Set<List<T>> result) {
        if (arrangement.size() >= num) {
            // 改排列生成完成, 保存结果
            result.add(arrangement);
        }
        else {
            // 递归选择下一个
            for(T element : dataSet){
                if(!arrangement.contains(element)){
                    // 排列结果不存在该项，才可选择
                    List<T> arrangementCopy = new ArrayList<>(arrangement);
                    arrangementCopy.add(element);
                    arrangementSelect(dataSet, arrangementCopy, num, result);
                }
            }
        }
    }

    /**
     *
     * @param dataSet 其中元素允许重复
     * @param arrangement
     * @param selectNum
     * @param result
     * @param <T>
     * @throws Exception
     */
    private <T> void  arrangement(LinkedList<T> dataSet ,List<T> arrangement, int selectNum,Set<List<T>> result) throws Exception {
        if(arrangement.size()> selectNum){
            throw new Exception("compute error");
        }
        else if(selectNum == 0){
            return;
        }
        else if(arrangement.size()==selectNum){
            result.add(arrangement);
        }
        else {
            for(T element : dataSet){
                List<T> arrangementCopy = new ArrayList<>(arrangement);
                arrangementCopy.add(element);
                LinkedList<T> dataSetCopy = new LinkedList<>(dataSet);
                dataSetCopy.remove(element);
                arrangement(dataSetCopy,arrangementCopy,selectNum,result);
            }
        }
    }
    /**
     * 组合选择（从列表中选择n个组合）
     * @param dataSet 待选列表
     * @param n 选择个数
     * @param print 是否输出组合数
     */
    public static <T> Set<Set<T>> combinationSelect(Set<T> dataSet, int n,boolean print) {
        if(print){
            System.out.println(String.format("C(%d, %d) = %d", dataSet.size(), n, combination(dataSet.size(), n)));
        }
        return  combinationSelect(dataSet,new HashSet<T>(),new HashSet<T>(),n,new HashSet<Set<T>>());
    }

    /**
     * 组合选择
     * @param dataSet 待组合元素集合
     * @param combination 一个组合
     * @param num 组合的元素个数 也即（m，n） 中的n
     * @param result 结果集合
     * @param selected 已经加入组合的元素列表
     */
    private static <T> Set<Set<T>>  combinationSelect(Set<T> dataSet,
                                                        Set<T> combination,
                                                        Set<T> selected,
                                                        int num,
                                                        Set<Set<T>> result) {
        if (combination.size() >= num) {
            // 该组合生成完成, 保存结果
            result.add(combination);
            return result;
        }
        // 递归选择下一个
        for(T element : dataSet){
            if(selected.contains(element)){
                break;
            }
            if(!combination.contains(element)){
                // 排列结果不存在该项，才可选择
                Set<T> combinationCopy = new HashSet<>(combination);
                combination.add(element);
                combinationSelect(dataSet, combination,selected,num, result);
                combination = combinationCopy;
            }
        }
        return result;
    }

    /**
     * 从set1 和 set2 中 各选一个元素 进行组合
     * @param set1
     * @param set2
     * @param <T>
     * @return
     */
    public static <T> Set<Pair<T,T>> binaryCombination (Collection<T> set1, Collection<T> set2){
        Set<Pair<T,T>> result = new HashSet<>();
        for(T element1 : set1){
            for(T element2 : set2){
                result.add(new Pair<>(element1,element2));
            }
        }
        return result;
    }

    public static void main(String[] args) {
        Set<Object> dataSet =new HashSet<>();
        dataSet.add("1");
        dataSet.add("2");
        dataSet.add("3");
        dataSet.add("4");
        Set<Object> dataSet2 =new HashSet<>();
        dataSet2.add("1");
        dataSet2.add("2");
        dataSet2.add("3");
        System.out.println(combinationSelect(dataSet ,4,true));
        System.out.println(combinationSelect(dataSet ,3,true));
        System.out.println(arrangementSelect(dataSet ,4,true));
        System.out.println(arrangementSelect(dataSet ,3,true));
        System.out.println(binaryCombination(dataSet,dataSet2));
    }
}
