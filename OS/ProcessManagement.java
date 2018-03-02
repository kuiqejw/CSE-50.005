/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package OS;

/**
 * Created by jit_biswas on 2/1/2018.
 */
import java.io.*;
import java.util.ArrayList;

public class ProcessManagement {

    //set the working directory
    private static File currentDirectory = new File("");
    //set the instructions file
    private static File instructionSet = new File("test2.txt");
    public static Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {

        /*-------------------1. Parse the text file--------------------- */
        // Define variables to store the details extacted from file parsing
        // (ArrayList is used to minimize index out of bound error for fixed-siezed array)
        ArrayList<ArrayList<String>> listOfProcesses = new ArrayList<ArrayList<String>>();


        /* content of the process under each index
		0 program name and arguments;
		1 list Of children's id;
		2 input file;
		3 output file; */
        // Actual file parsing process
        try {
            // FileReader reads the text file in the first argument
            FileReader fileReader = new FileReader("C:\\Users\\Dell\\Documents\\NetBeansProjects\\FileOperation\\src\\OS\\test2.txt");
            // Always wrap FileReader in BufferedReader
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            try {
                // Read the next line in the text file
                String line = bufferedReader.readLine();
                // Repeat reading lines until the end of the file
                while (line != null) {
                    // Create a buffer to store processDetails
                    ArrayList<String> processDetails = new ArrayList<String>();
                    // Parsing information to the Arraylist processDetails
                    for (String component : line.split(":")) {
                        // Add the information to processDetails
                        processDetails.add(component);
                    }
                    // Store the processDetails into listOfProcesses
                    listOfProcesses.add(processDetails);
                    // Update the line definition to the next line to prevent infinite loop
                    line = bufferedReader.readLine();
                }

            } catch (IOException e) {
                System.out.println(e);
            }

        } catch (FileNotFoundException e) {
            System.out.println(e);
        }

        /*-------------------2. Store the inter-node relationship information--------------------- */
        // Create a buffer list of children under each parent(s)
        ArrayList<String[]> listOfChildren = new ArrayList<String[]>();
        for (ArrayList<String> process : listOfProcesses) {
            listOfChildren.add(process.get(1).split(" "));
        }

        /*----Control Dependency----*/
        // Create a list of parent(s) for each child node -- control dependencies
        // if a parent node has no child, its list of children will be empty
        ArrayList<ArrayList<Integer>> listOfParents = new ArrayList<ArrayList<Integer>>();

        // Initiate the listOfParents to prevent ArrayIndexOutOfBound error in later steps
        for (int i = 0; i < listOfProcesses.size(); i++) {
            listOfParents.add(new ArrayList<Integer>());
        }

        // Temporary variable for parent index
        int parentIndex;
        // Temporary variable for child index
        int childIndex;

        // Add the parent indices to each child
        for (String[] children : listOfChildren) {
            parentIndex = listOfChildren.indexOf(children);
            for (String oneChild : children) {
                if (!oneChild.equals("none")) {
                    childIndex = Integer.parseInt(oneChild);
                    listOfParents.get(childIndex).add(parentIndex);
                }
            }
        }


        /*----Data Dependency----*/
        // Add to the listOfParents the nodes that supplies input files for each child node -- data dependencies
        // Create a buffer string for input file name
        String inputFile;
        // Create a buffer string for output file name
        String outputFile;
        // Create a buffer string for command name
        String commandFile;

        /* There are two cases for data dependency:
		1. when the input of child node is an output file from the parent node
		2. when the arguments for child node command contains output file from the parent node*/
        // Add the data/file-providing parent indices to each child
        for (ArrayList<String> process : listOfProcesses) {
            childIndex = listOfProcesses.indexOf(process);
            // toLowerCase method is used based on assumption that file names are not case sensitive
            inputFile = process.get(2).toLowerCase();
            commandFile = process.get(0).toLowerCase();

            for (ArrayList<String> anotherProcess : listOfProcesses) {
                // 1. when the input of child node is an output file from the parent node
                parentIndex = listOfProcesses.indexOf(anotherProcess);
                outputFile = anotherProcess.get(3).toLowerCase();
                if (inputFile.equals(outputFile)) {
                    // add if it does not already exist
                    if (!listOfParents.get(childIndex).contains(parentIndex)) {
                        listOfParents.get(childIndex).add(parentIndex);
                    }
                }

                // 2. when the arguments for child node command contains output file from the parent node
                if (commandFile.contains(outputFile)) {
                    // add if it does not already exist
                    if (!listOfParents.get(childIndex).contains(parentIndex)) {
                        listOfParents.get(childIndex).add(parentIndex);
                    }
                }
            }
        }

        /*-------------------3. Execute the processes in the correct order--------------------- */
        // Create a list of process builders, one for each process
        ArrayList<ProcessBuilder> processBuliderList = new ArrayList<ProcessBuilder>();
        // Also, create a list of processes
        // Use ArrayList of ArrayList, instead of simply ArrayList<Process>, for the subsequent checking step
        ArrayList<ArrayList<Process>> processList = new ArrayList<ArrayList<Process>>();
        // Initiate the ArrayList of ArrayList
        for (int i = 0; i < listOfProcesses.size(); i++) {
            processList.add(new ArrayList<Process>());
        }

        // Create a list shows the running status for each process
        ArrayList<String> listOfStatus = new ArrayList<String>();
        // Initiate the all status to ineligible
        for (int i = 0; i < listOfProcesses.size(); i++) {
            listOfStatus.add("ineligible");
        }

        // Get current directory
        File currentDirectory = new File(System.getProperty("user.dir"));

        // Initiate a list of processBuilders with specific command, input, and output
        for (ArrayList<String> process : listOfProcesses) {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command(process.get(0).split(" "));
            pb.directory(currentDirectory);
            // redirect input
            if (!process.get(2).equals("stdin")) {
                pb.redirectInput(new File(process.get(2)));
            }
            // redirect output
            if (!process.get(3).equals("stdout")) {
                pb.redirectOutput(new File(process.get(3)));
            }
            processBuliderList.add(pb);
        }

        // Starting processes
        for (int idx = 0; idx < listOfParents.size(); idx++) {
            // If a process has no parent, mark the status as ready
            if (listOfParents.get(idx).isEmpty()) {
                listOfStatus.set(idx, "ready");
            }
        }

        // Check whether all programs have finished
        while (!isAllProcessesFinished(listOfStatus)) {
            for (int idx = 0; idx < listOfProcesses.size(); idx++) {

                // Case 1: if the status is ready
                if (listOfStatus.get(idx).equals("ready")) {
                    try {
                        // start the process
                        Process p = processBuliderList.get(idx).start();
                        // Set the status to running
                        listOfStatus.set(idx, "running");
                        //Add process p to the list at position idx
                        processList.get(idx).add(p);
                    } catch (IOException e) {
                        System.out.println(e);
                    }
                } // Case 2: if the status is running
                else if (listOfStatus.get(idx).equals("running")) {
                    try {
                        // use waitFor method to check whether process has finished
                        processList.get(idx).get(0).waitFor();
                    } catch (InterruptedException e) {
                        System.out.println("The process is still running");
                    }
                    // If a process has finished, mark the status as finished
                    listOfStatus.set(idx, "finished");
                } // Case 3: if the status is ineligible
                else if (listOfStatus.get(idx).equals("ineligible")) {
                    for (Integer parentInteger : listOfParents.get(idx)) {
                        // If all parents are finished, mark the child program as ready
                        if (!listOfStatus.get(parentInteger).equals("finished")) {
                            // Break if any of the parent has not finished yet
                            break;
                        }
                        // If all parents have finished, mark the statusof child as ready
                        listOfStatus.set(idx, "ready");
                    }
                }
            }

        }

        System.out.println("Program finished, thanks for using!");

    }

    // Create a method to check if all processes have finished
    public static boolean isAllProcessesFinished(ArrayList<String> listOfStatus) {
        for (String status : listOfStatus) {
            if (!status.equals("finished")) {
                return false;
            }
        }
        return true;
    }

}
