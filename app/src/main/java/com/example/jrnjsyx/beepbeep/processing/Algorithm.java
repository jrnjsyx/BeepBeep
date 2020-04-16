package com.example.jrnjsyx.beepbeep.processing;

import com.example.jrnjsyx.beepbeep.utils.FlagVar2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by cc on 2017/11/29.
 */

public class Algorithm {



    public static int getMedian(List<Integer> nums){
        if(nums.size() == 0){
            return 0;
        }
        List<Integer> ns = new ArrayList<Integer>();
        ns.addAll(nums);
        Collections.sort(ns);
        return ns.get((ns.size()-1)/2);
    }

    /**
     *
     * @param numbers
     * @param low
     * @param high
     * @return
     */
    public  static <T extends Comparable<T>>int getMiddle(T[] numbers, int low,int high)
    {
        T temp = numbers[low];
        while(low < high)
        {
            while(low < high && numbers[high].compareTo(temp) >= 0)
            {
                high--;
            }
            numbers[low] = numbers[high];
            while(low < high && numbers[low].compareTo(temp) < 0)
            {
                low++;
            }
            numbers[high] = numbers[low] ;
        }
        numbers[low] = temp ;
        return low ;
    }




    /**
     * quick sort with designated upper and lower bound of an array
     * @param numbers: vectors
     * @param low: low index of vecotors
     * @param high: high index of vectors
     */
    public  static <T extends Comparable<T>> void quickSort(T[] numbers,int low,int high)
    {
        if(low < high)
        {
            int middle = getMiddle(numbers,low,high);
            quickSort(numbers, low, middle-1);
            quickSort(numbers, middle+1, high);
        }
    }

    public static void quickSort(float[] numbers,int low,int high){
        Float[] data = new Float[numbers.length];
        for(int i=0;i<data.length;i++){
            data[i] = numbers[i];
        }
        quickSort(data,low,high);
        for(int i=0;i<data.length;i++){
            numbers[i] = data[i];
        }
    }

    public static void quickSort(int[] numbers,int low,int high){
        Integer[] data = new Integer[numbers.length];
        for(int i=0;i<data.length;i++){
            data[i] = numbers[i];
        }
        quickSort(data,low,high);
        for(int i=0;i<data.length;i++){
            numbers[i] = data[i];
        }
    }

    /**
     * get both the max vlaue and its corresponding index
     * @param s - input array in float format
     * @param low - low index of the array that to be searched
     * @param high - high index of the array that to be searched
     * @return class IndexMaxVarInfo that contains both the max value and its index in the array
     */
    public static IndexMaxVarInfo getMaxInfo(float s[], int low, int high){
        IndexMaxVarInfo indexMaxVarInfo = new IndexMaxVarInfo();
        indexMaxVarInfo.index = low;
        indexMaxVarInfo.fitVal = s[low];
        for(int i = low; i < high; i++){
            if(s[i] > indexMaxVarInfo.fitVal){
                indexMaxVarInfo.fitVal = s[i];
                indexMaxVarInfo.index = i;
            }
        }
        return indexMaxVarInfo;
    }

    /**
     * to get the max value of the short array
     * @param s - samples in short format
     * @param low - low index
     * @param high - high index
     * @return both the max value and its corresponding index
     */
    public static IndexMaxVarInfo getMaxInfo(short s[], int low, int high){
        IndexMaxVarInfo indexMaxVarInfo = new IndexMaxVarInfo();
        indexMaxVarInfo.index = low;
        indexMaxVarInfo.fitVal = s[low];
        for(int i = low; i < high; i++){
            if(s[i] > indexMaxVarInfo.fitVal){
                indexMaxVarInfo.fitVal = s[i];
                indexMaxVarInfo.index = i;
            }
        }
        return indexMaxVarInfo;
    }

    public static float meanValue(float[] s, int low, int high){
        float sum = 0;
        for(int i = low ; i < high ; i++){
            sum += s[(i+s.length)%s.length];
        }
        sum /= (high - low + 1);
        return sum;
    }

    public static short meanValue(short[] s, int low, int high){
        long sum = 0;
        for(int i = low ; i < high ; i++){
            sum += s[i];
        }
        sum /= (high - low + 1);
        return (short) sum;
    }



    public static int findNearestPosOnBase(List<Integer> positions, Integer base){

        int index = 0;
        int min = Integer.MAX_VALUE;
        for(int i=0;i<positions.size();i++){
            int val = Math.abs(positions.get(i)-base);
            if(val < min){
                min = val;
                index = i;
            }
        }
        return positions.get(index);
    }

    public static int moveIntoRange(int val, int low, int step){
        int res = val;
        while (res < low){
            res += step;
        }
        while (res >= low+step){
            res -= step;
        }

        return res;

    }

    //(y-y0)*(x-x0)=a  return [x0,y0,a]
    public static double[] threePointDeterminationCurve(double[] xs,double[] ys){
        if(xs.length != 3 || ys.length != 3){
            throw new RuntimeException("输入的并非三点的数据。");
        }
        if((xs[1]-xs[0])*(xs[2]-xs[1]) <= 0 || (ys[1]-ys[0])*(ys[2]-ys[1]) <= 0){
            throw new RuntimeException("输入的数据必须单增或者单减。");
        }
        double a11 = ys[1]-ys[0];
        double a12 = xs[1]-xs[0];
        double a21 = ys[2]-ys[0];
        double a22 = xs[2]-xs[0];
        double b1 = xs[1]*ys[1]-xs[0]*ys[0];
        double b2 = xs[2]*ys[2]-xs[0]*ys[0];
        double x0 = (b1*a22-b2*a12)/(a11*a22-a21*a12);
        double y0 = (a11*b2-a21*b1)/(a11*a22-a21*a12);
        double a = (xs[1]-x0)*(ys[1]-y0);
        double[] ret = new double[3];
        ret[0] = x0;
        ret[1] = y0;
        ret[2] = a;
        return ret;

    }


    public static float[] fftBandPassFilter(float[] fft, int lowF, int highF, int spaceF){
        int len = fft.length/2;
        float[] retFFT = new float[fft.length];


        int low = (lowF-spaceF) * len / FlagVar2.Fs;
        int high = (highF+spaceF) * len / FlagVar2.Fs;
        if(low>0 && low<high && high<len/2) {
            int low2 = len-high;
            int high2 = len-low;
            for (int i=0;i<2*len;i++){
                if(i>=2*low && i<=2*high+1){
                    retFFT[i] = fft[i];
                }else if(i>=2*low2 && i<=2*high2+1){
                    retFFT[i] = fft[i];
                }else{
                    retFFT[i] = 0;
                }
            }
            return retFFT;
        }else{
            System.out.println("fftlen:"+len+"  low:"+low+"  high:"+high);
            throw new RuntimeException("输入数据非法。");
        }
    }
}
