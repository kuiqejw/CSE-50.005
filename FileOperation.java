/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lab4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class FileOperation {

    private static File currentDirectory = new File(System.getProperty("user.dir"));

    public static void main(String[] args) throws IOException {

        String commandLine;
        ArrayList<String> historyIndex = new ArrayList<String>();
        File file;
        File newpath;
        ArrayList<ArrayList<String>> history = new ArrayList<ArrayList<String>>();

        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            // read what the user entered
            System.out.print("jsh>");
            commandLine = console.readLine();
            // clear the space before and after the command line
            commandLine = commandLine.trim();

            // if the user entered a return, just loop again
            if (commandLine.equals("")) {
                continue;
            } // if exit or quit
            else if (commandLine.equalsIgnoreCase("exit") | commandLine.equalsIgnoreCase("quit")) {
                System.exit(0);
            }

            // check the command line, separate the words
            String[] commandStr = commandLine.split(" ");
            ArrayList<String> command = new ArrayList<String>();
            for (int i = 0; i < commandStr.length; i++) {
                command.add(commandStr[i]);
            }

            String action = command.get(0);
            if (action.equals("cd")) {
                history.add(0, command);
                historyIndex.add(Integer.toString(history.size() - 1));
                if (commandLine.equals("cd")) {
                    newpath = new File(System.getProperty("user.home"));
                } else if (command.get(1).equals("..")) {
                    file = new File(System.getProperty("user.dir"));
                    newpath = new File(file.getParent());//to use file as a temporary storage holder for the old path
                } else {
                    newpath = new File(currentDirectory.getAbsolutePath() + File.separator + command.get(1));
                }
                if (newpath.isDirectory()) {
                    currentDirectory = newpath;
                } else {
                    System.out.println("Directory not exist" + newpath);
                }
                continue;
            }//Qn 3 : history component:
            else if (command.get(0).trim().toLowerCase().equals("history")) {
                for (int i = 0; i < history.size(); i++) {
                    System.out.print(i + 1);
                    System.out.print(" ");
                    System.out.println(String.join(" ", history.get(i)));

                }
                continue;
                //call command from last called
            } else if (commandLine.matches("!!")) {
                if (history.size() == 0) {
                    System.out.println("No previous command found");
                    continue;
                } else {
                    command = history.get(history.size() - 1);
                    //System.out.print("got pwd");
                }
            } //call command from history index
            else if (historyIndex.contains(commandLine)) {
                command = history.get(Integer.parseInt(commandLine) - 1);
            } else if (commandLine.matches("-?\\d+")) {
                if (Integer.parseInt(commandLine) > historyIndex.size()) {
                    System.out.println("The number must be between 1 and " + (historyIndex.size()));
                    continue;
                }
            } // TODO: implement code to handle create here
            else if (action.equals("create")) {
                history.add(0, command);
                historyIndex.add(Integer.toString(history.size() - 1));
                // Getting file name...
                if (command.size() == 1) {
                    continue;
                }
                String fileName = command.get(1);
                Java_create(currentDirectory, fileName);
            } // TODO: implement code to handle delete here
            else if (action.equals("delete")) {
                history.add(0, command);
                historyIndex.add(Integer.toString(history.size() - 1));
                // Getting file name...
                if (command.size() == 1) {
                    continue;
                }
                String fileName = command.get(1);
                Java_delete(currentDirectory, fileName);
            } // TODO: implement code to handle display here
            else if (action.equals("display")) {
                history.add(0, command);
                historyIndex.add(Integer.toString(history.size() - 1));
                // Getting file name...
                if (command.size() == 1) {
                    continue;
                }
                String fileName = command.get(1);
                Java_cat(currentDirectory, fileName);
            } // TODO: implement code to handle list here
            else if (action.equals("list")) {
                history.add(0, command);
                historyIndex.add(Integer.toString(history.size() - 1));
                if (command.size() == 1) {
                    Java_ls(currentDirectory, null, null);
                } else if (command.size() == 2 && command.get(1).equals("property")) {
                    Java_ls(currentDirectory, command.get(1), null);
                } else if (command.size() == 3) {
                    Java_ls(currentDirectory, command.get(1), command.get(2));
                }

            } // TODO: implement code to handle find here
            else if (action.equals("find")) {
                history.add(0, command);
                historyIndex.add(Integer.toString(history.size() - 1));
                // Getting file name...
                if (command.size() == 1) {
                    continue;
                }
                String fileName = command.get(1);
                boolean found = Java_find(currentDirectory, fileName);
                if (!found) {
                    System.out.println("No such files in current directory and its subdirectories");
                }
            } // TODO: implement code to handle tree here
            else if (action.equals("tree")) {
                history.add(0, command);
                historyIndex.add(Integer.toString(history.size() - 1));
                int d;
                if (command.size() == 1) {
                    d = getDepth(currentDirectory, 1);
                    Java_tree(currentDirectory, d, "", d);
                } else if (command.size() == 2) {
                    d = Integer.parseInt(command.get(1));
                    Java_tree(currentDirectory, d, "", d);
                } else if (command.size() == 3) {
                    d = Integer.parseInt(command.get(1));
                    Java_tree(currentDirectory, d, command.get(2), d);
                }
            } else if (action.equals("depth")) {
                history.add(0, command);
                historyIndex.add(Integer.toString(history.size() - 1));
                System.out.println(getDepth(currentDirectory, 1));
            } else {
                //store history
                history.add(0, command);
                historyIndex.add(Integer.toString(history.size() - 1));
            }

            // other commands
            ProcessBuilder pBuilder = new ProcessBuilder(command);
            pBuilder.directory(currentDirectory);
            try {
                Process process = pBuilder.start();
                // obtain the input stream
                InputStream is = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);

                // read what is returned by the command
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }

                // close BufferedReader
                br.close();
            } // catch the IOexception and resume waiting for commands
            catch (IOException ex) {
//				System.out.println(ex);
                continue;
            }
        }
    }

    /**
     * Create a file
     *
     * @param dir - current working directory
     * @param command - name of the file to be created
     */
    public static void Java_create(File dir, String command) {
        // TODO: create a file
        try {
            File file = new File(dir, command);
            boolean exists = file.createNewFile();
            if (!exists) {
                System.out.println("File already exists: " + command);
            }
        } catch (IOException e) {
            System.out.println("I/O error occurred!");
        } catch (SecurityException e) {
            System.out.println("Permission to create file not granted: " + command);
        }
    }

    /**
     * Delete a file
     *
     * @param dir - current working directory
     * @param name - name of the file to be deleted
     */
    public static void Java_delete(File dir, String name) {
        // TODO: delete a file
        try {
            File file = new File(dir, name);
            boolean exists = file.delete();
            if (!exists) {
                System.out.println("File does not exist: " + name);
            }
        } catch (SecurityException e) {
            System.out.println("Permission to delete file not granted: " + name);
        }
    }

    /**
     * Display the file
     *
     * @param dir - current working directory
     * @param name - name of the file to be displayed
     */
    public static void Java_cat(File dir, String name) {
        // TODO: display a file
        File file = new File(dir, name);
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
            BufferedReader in = new BufferedReader(fileReader);
            String line;

            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
        } catch (FileNotFoundException e) {
            System.out.println("File does not exist: " + name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function to sort the file list
     *
     * @param list - file list to be sorted
     * @param sort_method - control the sort type
     * @return sorted list - the sorted file list
     */
    private static File[] sortFileList(File[] list, String sort_method) {
        // sort the file list based on sort_method
        // if sort based on name
        if (sort_method.equalsIgnoreCase("name")) {
            Arrays.sort(list, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return (f1.getName()).compareTo(f2.getName());
                }
            });
        } else if (sort_method.equalsIgnoreCase("size")) {
            Arrays.sort(list, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.valueOf(f1.length()).compareTo(f2.length());
                }
            });
        } else if (sort_method.equalsIgnoreCase("time")) {
            Arrays.sort(list, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                }
            });
        }
        return list;
    }

    /**
     * List the files under directory
     *
     * @param dir - current directory
     * @param display_method - control the list type
     * @param sort_method - control the sort type
     */
    public static void Java_ls(File dir, String display_method, String sort_method) {
        // TODO: list files
        File[] fileList = dir.listFiles();
        if (display_method == null) {
            for (File file : fileList) {
                System.out.println(file.getName());
            }
        } else if (sort_method == null) {
            for (File file : fileList) {
                System.out.format("%-15s Size: %-10s Last Modified: %-40s", file.getName(), file.length(), new Date(file.lastModified()));
                System.out.println();
            }
        } else {
            File[] sorted = sortFileList(fileList, sort_method);
            for (File file : sorted) {
                System.out.format("%-15s Size: %-10s Last Modified: %-40s", file.getName(), file.length(), new Date(file.lastModified()));
                System.out.println();
            }
        }
    }

    /**
     * Find files based on input string
     *
     * @param dir - current working directory
     * @param name - input string to find in file's name
     * @return flag - whether the input string is found in this directory and
     * its subdirectories
     */
    public static boolean Java_find(File dir, String name) {
        boolean flag = false;
        // TODO: find files
        File[] fileList = dir.listFiles();

        for (File file : fileList) {
            if (file.getName().endsWith(name)) {
                System.out.println(file.getAbsolutePath());
                flag = true;
            } else if (file.isDirectory()) {
                boolean tempFlag = Java_find(file, name);
                if (!flag) {
                    flag = tempFlag;
                }
            }
        }

        return flag;
    }

    /**
     * Print file structure under current directory in a tree structure
     *
     * @param dir - current working directory
     * @param depth - maximum sub-level file to be displayed
     * @param sort_method - control the sort type
     */
    public static void Java_tree(File dir, int depth, String sort_method, int initialDepth) {
        // TODO: print file tree
        File[] fileList = sortFileList(dir.listFiles(), sort_method);
        depth--;

        if (fileList != null && fileList.length != 0) {
            for (File file : fileList) {
                if (dir == currentDirectory) {
                    System.out.println(file.getName());
                } else {
                    String output = "|-" + file.getName();
                    for (int i = 0; i < (initialDepth - depth - 1); i++) {
                        output = "  " + output;
                    }
                    System.out.println(output);
                }

                if (depth > 0 && file.isDirectory()) {
                    Java_tree(file, depth, sort_method, initialDepth);
                }
            }
        }

    }

    // TODO: define other functions if necessary for the above functions
    public static int getDepth(File dir, int depth) {
        File[] fileList = dir.listFiles();
        ArrayList<Integer> subDirDepths = new ArrayList<>();
        boolean hasSubDir = false;

        if (fileList.length == 0) {
            return 1;
        }

        for (File file : fileList) {
            if (file.isDirectory()) {
                subDirDepths.add(getDepth(file, depth));
                hasSubDir = true;
            }
        }

        if (hasSubDir) {
            depth = Collections.max(subDirDepths) + 1;
        }

        return depth;
    }

}
