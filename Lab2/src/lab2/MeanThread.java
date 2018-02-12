/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lab2;

/**
 *
 * @author ongajong
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MeanThread {
    public ArrayList<MeanMultiThread> createThreads(int numOfThreads, ArrayList<ArrayList<Integer>> subArrays) {
        ArrayList<MeanMultiThread> threadArrayList = new ArrayList<>(numOfThreads);
        for (int i = 0; i < numOfThreads; i++) {
            MeanMultiThread thread = new MeanMultiThread(subArrays.get(i));
            threadArrayList.add(thread);
        }
        return threadArrayList;
    }

    ArrayList<Integer> readExternalFile(String filePath, int inputSize) throws FileNotFoundException {
        Scanner inputScanner = new Scanner(new File(filePath));

        int i = 0;
        
        ArrayList<Integer> arrayOutput = new ArrayList<>(inputSize);
        while (inputScanner.hasNext()) {
            arrayOutput.add(i,  inputScanner.nextInt());
            i++;
        }

        return arrayOutput;
    }

    ArrayList<ArrayList<Integer>> partitioningArray(ArrayList<Integer> originalArray, int inputSize, int numOfThreads) {
        int subArraySize = inputSize/numOfThreads;
        int startingIndex = 0;
        int endingIndex = subArraySize - 1;
        ArrayList<ArrayList<Integer>> outputArray = new ArrayList<>(numOfThreads);
       
        for (int i = 0; i < numOfThreads; i++) {
            ArrayList<Integer> splitArray = new ArrayList<>(originalArray.subList(startingIndex,endingIndex));
            outputArray.add(i,splitArray);
            startingIndex += subArraySize;
            endingIndex += subArraySize;
        }

        return outputArray;
    }

    

    double allAroundMean(ArrayList<Double> tempMean, int numOfThreads) {
        double meanOutput = 0;
        for (Double temp : tempMean) {
            meanOutput += temp;
        }
        return meanOutput/numOfThreads;
    }

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        MeanThread meanThread = new MeanThread();
        String filePath = "/home/ongajong/Documents/input.txt";
        int inputSize = 524288;
        ArrayList<Integer> originalArray = meanThread.readExternalFile(filePath,inputSize);

        // define number of threads
        int NumOfThread = 2048;

        // TODO: partition the array list into N subArrays, where N is the number of threads
        ArrayList<ArrayList<Integer>> subArrays = meanThread.partitioningArray(originalArray,inputSize,NumOfThread);

        // TODO: start recording time
        long startTime = System.currentTimeMillis();

        // TODO: create N threads and assign subArrays to the threads so that each thread computes mean of
        // its repective subarray. For example,
        ArrayList<MeanMultiThread> threadArrayList = meanThread.createThreads(NumOfThread,subArrays);

        //Tip: you can't create big number of threads in the above way. So, create an array list of threads.

        // TODO: start each thread to execute your computeMean() function defined under the run() method
        //so that the N mean values can be computed. for example,
        for (MeanMultiThread thread : threadArrayList) {
            thread.start();
        }

        for (MeanMultiThread thread : threadArrayList) {
            thread.join();
        }

        // TODO: show the N mean values
        System.out.println("Temporal mean value of thread n is ... ");
        for (MeanMultiThread thread : threadArrayList) {
            System.out.println(thread.getName() + ": " + thread.getMean());
        }

        // TODO: store the temporal mean values in a new array so that you can use that
        /// array to compute the global mean.
        ArrayList<Double> temporalMean = new ArrayList<>(NumOfThread);
        for (MeanMultiThread thread : threadArrayList) {
            temporalMean.add(thread.getMean());
        }

        // TODO: compute the global mean value from N mean values.
        double globalMean = meanThread.allAroundMean(temporalMean,NumOfThread);

        // TODO: stop recording time and compute the elapsed time
        long finalTime = System.currentTimeMillis();
        System.out.println("Time elapsed: " + (finalTime-startTime));

        System.out.println("The global mean value is ... ");
        System.out.println(globalMean);

    }
}
//Extend the Thread class
class MeanMultiThread extends Thread {
    private ArrayList<Integer> list;
    private double mean;
    MeanMultiThread(ArrayList<Integer> array) {
        list = array;
    }
    public double getMean() {
        return mean;
    }

    public double computeMean(ArrayList<Integer> list) {
        double meanOutput = 0;
        int meanLength = list.size();
        for (double values : list) {
            meanOutput += values;
        }
        return meanOutput/meanLength;
    }
    public void run() {
        // TODO: implement your actions here, e.g., computeMean(...)
        mean = computeMean(list);
    }
}