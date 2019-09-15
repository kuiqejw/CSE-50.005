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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;

public class MeanThread {
    MeanMultiThread[] part (int numOfThread, ArrayList<Integer> datalist){
        MeanMultiThread[] threads  = new MeanMultiThread[numOfThread];
        for(int i = 0; i < numOfThread; i++) {
			ArrayList<Integer> subArray = new ArrayList<Integer>();
			
			for(int j = 0; j < datalist.size() / numOfThread; j++) {
				subArray.add(datalist.get(j +  (i * datalist.size() / numOfThread)));
			}
			
			threads[i] = new MeanMultiThread(subArray);
		}
        return threads;
    }
    double allAroundMean(ArrayList<Double> tempMean, int numOfThreads) {
        double meanOutput = 0;
        for (Double temp : tempMean) {
            meanOutput += temp;
        }
        return meanOutput/numOfThreads;
}
    ArrayList<Integer> readExternalFile(String filePath) throws FileNotFoundException {
        Scanner inputScanner = new Scanner(new File(filePath));        
        ArrayList<Integer> arrayOutput = new ArrayList<>();
        while (inputScanner.hasNext()) {
            arrayOutput.add(inputScanner.nextInt());
        }

        return arrayOutput;
    }

	public static void main(String[] args) throws InterruptedException, FileNotFoundException {
		String filePath = "/home/ongajong/Documents/input.txt";
		MeanThread meanthread = new MeanThread();
		ArrayList<Integer> integers = meanthread.readExternalFile(filePath);
		int NumOfThread = 2048;
// For Command Line
//         String fileLocation = args[0];
//         N = Integer.valueOf(args[1]);
                // this way, you can pass number of threads as 
		     // a second command line argument at runtime.
		MeanMultiThread[] threads = meanthread.part(NumOfThread,integers);
		
		
		long startTime = System.currentTimeMillis();
		
		for(int i = 0; i < NumOfThread; i++) {
			threads[i].start();
		}
		
		double sum = 0;
		//Todo: show the N mean values
		for(int i = 0; i < NumOfThread; i++) {
			threads[i].join();
			System.out.println("Temporal mean value of thread " + i + " is " + threads[i].getMean());
			sum += threads[i].getMean();
		}
		
		long elapsed = System.currentTimeMillis() - startTime;
		
		System.out.println("The global mean value is " + (sum / NumOfThread));
		System.out.println("Elapsed time: " + (elapsed / 1000000) + "ms");
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
//        double meanOutput = 0;
//        int meanLength = list.size();
//        for (double values : list) {
//            meanOutput += values;
//        }
//        return meanOutput/meanLength;
        int size = list.size();
        double sum = 0;
        for (int i = 0; i< size;i++){
            sum += list.get(i);
        }
        double sublistMean = sum/(double) size;
        return sublistMean;
    }
    public void run() {
        // TODO: implement your actions here, e.g., computeMean(...)
        mean = computeMean(list);
    }
}