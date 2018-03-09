/**
 * Created by jit_biswas on 2/1/2018.
 */
package processmanagement;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ParseFile {

    //this method generates a ProcessGraph and store in ProcessGraph Class
    public static ArrayList<ProcessGraphNode> generateGraph(File inputFile) {
        ArrayList<ProcessGraphNode> nodes = new ArrayList<>();
        try {
            Scanner fileIn = new Scanner(inputFile);
            int index = 0;
            ArrayList<Integer> edgeParents = new ArrayList<Integer>();
            ArrayList<Integer> edgeChildren = new ArrayList<Integer>();

            while (fileIn.hasNext()) {
                String line = fileIn.nextLine();
                String[] quatiles = line.split(":");
                if (quatiles.length != 4) {
                    System.out.println("Wrong input format!");
                    throw new Exception();
                }

                //add this node
                ProcessGraph.addNode(index);
                //handle Children
                if (!quatiles[1].equals("none")) {
                    String[] childrenStringArray = quatiles[1].split(" ");
                    int[] childrenId = new int[childrenStringArray.length];
                    for (int i = 0; i < childrenId.length; i++) {
                        childrenId[i] = Integer.parseInt(childrenStringArray[i]);
                        ProcessGraph.addNode(childrenId[i]);
                        edgeParents.add(index);
                        edgeChildren.add(childrenId[i]);
                    }
                }
                //setup command
                ProcessGraph.nodes.get(index).setCommand(quatiles[0]);
                //setup input
                ProcessGraph.nodes.get(index).setInputFile(new File(quatiles[2]));
                //setup output
                ProcessGraph.nodes.get(index).setOutputFile(new File(quatiles[3]));

                //mark initial runnable
                for (ProcessGraphNode node : ProcessGraph.nodes) {
                    if (node.getParents().isEmpty()) {
                        node.setRunnable();
                    }
                }

                index++;
            }

            for (int i = 0; i < edgeParents.size(); i++) {
                int p = edgeParents.get(i);
                int c = edgeChildren.get(i);
                ProcessGraph.nodes.get(p).addChild(ProcessGraph.nodes.get(c));
            }
            //setup parent (readjusted positioning of addition of parents. Previous position registered no parents
            for (ProcessGraphNode node : ProcessGraph.nodes) {
                for (ProcessGraphNode childNode : node.getChildren()) {
                    ProcessGraph.nodes.get(childNode.getNodeId()).addParent(ProcessGraph.nodes.get(node.getNodeId()));
                }
            }
            System.out.println("edgechildren: " + Arrays.toString(edgeChildren.toArray()));
            System.out.println("edgeparents: " + Arrays.toString(edgeParents.toArray()));

        } catch (Exception e) {
            System.out.println("File not found!");
            e.printStackTrace();
        }
        return ProcessGraph.nodes;
    }

}
