/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lab2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author ongajong
 */
public class MedianThread {
    static int N;
    static ArrayList<Integer> dataList;
    private static final String fileLocation = "/home/ongajong/Documents/input.txt";
    public static void main(String[] args) throws InterruptedException, FileNotFoundException{
        N = 1;


        // For Command Line
//         String fileLocation = args[0];
//         N = Integer.valueOf(args[1]);

        Scanner inFile = new Scanner(new File(fileLocation));

        dataList = new ArrayList<Integer>();
        try {
            while (inFile.hasNextInt()) {
                dataList.add(inFile.nextInt());
            }
        } finally {
            inFile.close();
        }


        // size of each sublist
        int numCountPart = dataList.size() / N;

        ArrayList<ArrayList<Integer>> listOfSublists = new ArrayList<>();

        for (int i = 0; i < N; i++){
            ArrayList<Integer> sublist = new ArrayList<>(dataList.subList(i*numCountPart, (i+1)*numCountPart) );
            listOfSublists.add(sublist);
        }

//        System.out.println("List of unsorted subarrays");
//        System.out.println(listOfSublists + "\n");

        // start recording time
        final long startTime = System.currentTimeMillis();

        // We have a large number of threads. So, we create an ArrayList of threads.
        ArrayList<MedianMultiThread> listOfThreads = new ArrayList<>();

        // A list to store sorted lists
        ArrayList<List<Integer>> sortedList = new ArrayList<>();


        // create N threads and assign sub-ArrayLists to the threads
        // so that each thread can sort its respective sub-arrayList.
        for (int i = 0; i < N; i++){
            MedianMultiThread thread = new MedianMultiThread(listOfSublists.get(i));
            listOfThreads.add(thread);
            thread.start();
            thread.join();
            sortedList.add(thread.getInternal());
        }

        List<Integer> sortedFullArray = mergeSortedArrays(sortedList);

        double median = computeMedian(sortedFullArray);
        // stop recording time and compute the elapsed time
        final long endTime = System.currentTimeMillis();
        long runningTime = endTime - startTime;

        System.out.println("The Median value is: " + String.valueOf(median));
        System.out.println("Running time is " + runningTime + " milliseconds\n");

    }

    // each thread in the input is already sorted
    public static ArrayList<Integer> mergeSortedArrays(ArrayList<List<Integer>> sortedList) {
        ArrayList<Integer> sortedFullArray = new ArrayList<>();
        ArrayList<Integer> cacheArray = new ArrayList<>();

        for (List<Integer> l : sortedList){
            cacheArray.add(l.get(0));
        }
        while (sortedFullArray.size() != dataList.size()){
            int min = findMin(cacheArray);
            sortedFullArray.add(min);
            int index = cacheArray.indexOf(min);
            cacheArray.remove(index);

            // update the appended value with the next smallest
            List<Integer> sublist = sortedList.get(index);
            if (sublist.size() != 1){
                sublist.remove(0);
                cacheArray.add(index, sublist.get(0));
            }
            // do not remove appended value if last element of sublist
            else if (sublist.size() == 1){
                cacheArray.add(index,999999);
            }
        }

        return sortedFullArray;
    }

    public static int findMin(ArrayList<Integer> list){
        int min = list.get(0);
        for (int i : list){
            min = min < i ? min : i;
        }
        return min;
    }

    // computeMedian is called when we get the final sorted ArrayList
    public static double computeMedian(List<Integer> inputArray) {
        int size = inputArray.size();
        // if the inputArray is even
        if (size % 2 == 0){
            double a = inputArray.get((size / 2) - 1);
            double b = inputArray.get(size / 2);
            double median = (a + b) / 2.0;
            return median;
        } else { // if the inputArray is odd
            double median = inputArray.get((size-1) / 2);
            return median;
        }
    }

}

class MedianMultiThread extends Thread {
    private ArrayList<Integer> list;
    private ArrayList<Integer> inpArray;

    public ArrayList<Integer> getInternal() {
        return inpArray;
    }

    MedianMultiThread(ArrayList<Integer> array) {
        list = array;
    }

    public void run() {
        mergeSort(list);
    }

    public void mergeSort(ArrayList<Integer> array) {
        int size = array.size();
        this.inpArray = array;
        doMergeSort(0, size-1);
    }

    public void doMergeSort(int lowerIndex, int higherIndex){
        if (lowerIndex < higherIndex){
            int middle = lowerIndex + (higherIndex - lowerIndex)/2;
            doMergeSort(lowerIndex,middle);
            doMergeSort(middle+1, higherIndex);
            mergeParts(lowerIndex, middle, higherIndex);
        }
    }

    public void mergeParts(int lowerIndex, int middle, int higherIndex){
        ArrayList<Integer> mergedSortedArray = new ArrayList<>();

        int i = lowerIndex;
        int j = middle + 1;
        int k = lowerIndex;
        while (i <= middle && j <= higherIndex){
            if (inpArray.get(i) <= inpArray.get(j)){
                mergedSortedArray.add(inpArray.get(i));
                i++;
            }
            else{
                mergedSortedArray.add(inpArray.get(j));
                j++;
            }
        }
        while (i <= middle){
            mergedSortedArray.add(inpArray.get(i));
            i++;
        }
        int iter = 0;
        while (iter < mergedSortedArray.size()){
            inpArray.set(k, mergedSortedArray.get(iter++));
            k++;
        }
    }

}