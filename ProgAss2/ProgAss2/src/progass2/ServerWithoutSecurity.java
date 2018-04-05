package progass2;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerWithoutSecurity {

    public static void main(String[] args) {

        ServerSocket welcomeSocket = null;
        Socket connectionSocket = null;
        DataOutputStream toClient = null;
        DataInputStream fromClient = null;

        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedFileOutputStream = null;

        try {
            welcomeSocket = new ServerSocket(4321);
            connectionSocket = welcomeSocket.accept();//client socket
            //Reading and sending bytes
            fromClient = new DataInputStream(connectionSocket.getInputStream());
            toClient = new DataOutputStream(connectionSocket.getOutputStream());
            //reading and sending strings
            PrintWriter output = new PrintWriter(connectionSocket.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            while (!connectionSocket.isClosed()) {
                
             
                int packetType = fromClient.readInt();

                // If the packet is for transferring the filename
                if (packetType == 0) {

                    System.out.println("Receiving file...");

                    int numBytes = fromClient.readInt();
                    byte[] filename = new byte[numBytes];
                    fromClient.read(filename);

                    fileOutputStream = new FileOutputStream("C:\\Users\\Dell\\Documents\\NetBeansProjects\\rr.txt");
                    bufferedFileOutputStream = new BufferedOutputStream(fileOutputStream);

                    // If the packet is for transferring a chunk of the file
                } else if (packetType == 1) {

                    int numBytes = fromClient.readInt();
                    byte[] block = new byte[numBytes];
                    fromClient.read(block);

                    if (numBytes > 0) {
                        bufferedFileOutputStream.write(block, 0, numBytes);
                    }

                } else if (packetType == 2) {

                    System.out.println("Closing connection...");

                    if (bufferedFileOutputStream != null) {
                        bufferedFileOutputStream.close();
                    }
                    if (bufferedFileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    fromClient.close();
                    toClient.close();
                    connectionSocket.close();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
