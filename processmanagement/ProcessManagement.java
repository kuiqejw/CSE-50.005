package processmanagement;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class ProcessManagement {

    //set the working directory
    private static final String currentDirectory = "/home/ongajong/Documents";
    //set the instructions file
    private static final String instructionSet = "graph-file2.txt";
    public static boolean allExecuted = false;

    public static void main(String[] args) {

        //Parse file and store inside ArrayList in ProcessGraph
        ParseFile.generateGraph(new File(currentDirectory + '/' + instructionSet));
        //print graph
        ProcessGraph.printGraph();
        //executeNodes based on their 'state' 
        while (!allExecuted) {
            executeNodes(ProcessGraph.nodes, new File(currentDirectory));
        }
        System.out.println("Summary of execution:");
        ProcessGraph.printGraph();
    }
    //check all node in ProcessGraph ArrayList is executed
    public static boolean isAllExecuted() {
        for (ProcessGraphNode node : ProcessGraph.nodes) {
            if (!node.isExecuted()) {
                allExecuted = false;
                return false;
            }
        }
        allExecuted = true;
        return true;
    }
    /**
    *@param nodes - ArrayList of nodes in the graph
    *@param currentDirectory = working directory
    */
    private static void executeNodes(ArrayList<ProcessGraphNode> nodes, File currentDirectory) {
        //temp list of threads
        ArrayList<ProcessThread> threads = new ArrayList<>();
        //while nodes have not been completely executed
        while (!allExecuted) {
            //remove thread if finished executing
            for (Iterator<ProcessThread> iterator = threads.iterator(); iterator.hasNext();) {
                ProcessThread thread = iterator.next();
                if (thread.getFlag()) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    iterator.remove();
                }

            }
            //check for nodes which are ready to be executed
            nodes.forEach((node) -> {
                node.checkStatus();
            });
            //start thread if node is Runnable
            for (ProcessGraphNode node : nodes) {
                if (node.isRunnable()) {
                    ProcessThread newThread = new ProcessThread(node, nodes, currentDirectory);
                    newThread.start();
                    // add thread to the temporary list for tracking
                    threads.add(newThread);
                    // set node as running
                    node.setRunning();
                }
            }
        }
    }
}

class ProcessThread extends Thread {

    private ArrayList<ProcessGraphNode> nodes;//set ref to list of nodes
    private ProcessGraphNode currentNode;
    private File currentDirectory;
    private boolean flag = false; //set to true after executing
    private ProcessBuilder pb;

    public ProcessThread(ProcessGraphNode currentNode, ArrayList<ProcessGraphNode> nodes, File currentDirectory) {
        this.currentNode = currentNode;
        this.nodes = nodes;
        this.currentDirectory = currentDirectory;
    }
    //func to handle grep, echo 
    private static String[] parser(String command) {
        String commad = command.split(" ")[0];
        if (commad.equalsIgnoreCase("echo") || commad.equalsIgnoreCase("grep")) {
            return command.split(" ", 2);
        }
        return command.split(" ");
    }

    public void run() {
        try {
            for (ProcessGraphNode node : ProcessGraph.nodes) {
                if (ProcessManagement.isAllExecuted()) {
                    //if all completed, return as you have nothing else to do
                    return;
                } else if ( !node.isExecuted() && node.isRunnable()) {
                    //prepare commands
                    String[] commandLine = node.getCommand().split(":");
                    //create ProcessBuilder
                    ProcessBuilder pb = new ProcessBuilder(Arrays.asList(commandLine));
                    //set working directory
                    pb.directory(new File("/home/ongajong/Documents"));
                    //check for stdin, redirect input if required
                    if (!node.getInputFile().getName().equalsIgnoreCase("stdin")) {
                        pb.command(node.getCommand().split(" "));
                        //proviso for ls
                        if (!pb.command().get(0).equals("ls")) {
                            //throw IOException
                            pb.redirectInput(node.getInputFile());
                        }
                    } else {
                        pb.command(parser(node.getCommand()));
                    }
                    //redirect output command
                    if (!node.getOutputFile().getName().equalsIgnoreCase("stdout")) {
                        pb.redirectOutput(node.getOutputFile());
                    } else {
                        pb.inheritIO();
                    }

                    System.out.println("Execute attempt: " + pb.command() );
                    //check if cat has multiple arguments rather than just one
                    //taken from "https://stackoverflow.com/questions/30610741/executing-cat-command-from-java-program-does-not-work-as-expected"
                    if (parser(node.getCommand())[0].equalsIgnoreCase("cat")) {
                        if (pb.command().size() > 1) {
                            pb = new ProcessBuilder(parser(node.getCommand()));
                            File combinedFile = new File(node.getOutputFile().toString());
                            pb.redirectOutput(combinedFile);
                            pb.redirectError(combinedFile);
                        }
                    }//start the process
                    Process p = pb.start();
                    p.waitFor();
                    // set the node to be executed once it has been executed
                    node.setExecuted();
                }
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("IOException or InterruptedException");
        }
    }

    /**
     * * GETTER METHODS **
     */
    public boolean getFlag() {
        return flag;
    }
}
