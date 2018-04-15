/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package prog2;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author ongajong
 */
public class ServerCP2 {

    private static final int NTHREADS = 5;
    private static ExecutorService executorService = Executors.newFixedThreadPool(NTHREADS);
    private static ServerSocket serverSocket;
    private static final int port = 1234;
    private static final String privateKeyFile = "C:\\Users\\ongajong\\Documents\\50.005\\Prog2\\src\\prog2\\privateServer.der";
    private static final String signedCertificateFile = "C:\\Users\\ongajong\\Documents\\50.005\\Prog2\\src\\prog2\\server.crt";

    public static void main(String[] args) {

        ServerSocket welcomeSocket = null;
//        DataOutputStream toClient = null;
//        DataInputStream fromClient = null;

//        FileOutputStream fileOutputStream = null;
//        BufferedOutputStream bufferedFileOutputStream = null;
        try {
            welcomeSocket = new ServerSocket(port);

            while (true) {
                // then create TCP ClientSocket upon accepting incoming TCP request
                System.out.println("... expecting connection ...");
                final Socket connectionSocket = welcomeSocket.accept();
                System.out.println("... connection established...");

                // create threads to handle multiple client uploads
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OutputStream byteOut = connectionSocket.getOutputStream();
                            InputStream byteIn = connectionSocket.getInputStream();

                            // channels for sending and receiving plain text
                            PrintWriter stringOut = new PrintWriter(byteOut, true);
                            BufferedReader stringIn = new BufferedReader(new InputStreamReader(byteIn));
                            // wait for client to initiate conversation
                            System.out.println(stringIn.readLine());

                            // reply to client
                            stringOut.println("SERVER>> Hello, this is SecStore!");
                            stringOut.flush();
                            System.out.println("Sent to client: Hello, this is SecStore!");

                            // retrieve nonce from client
                            String nonceLength = stringIn.readLine();
                            byte[] nonce = new byte[Integer.parseInt(nonceLength)];
                            OutSideFunction.readByte(nonce, byteIn);
                            System.out.println("Received fresh nonce from client");

                            // load private key from .der
                            Path privateKeyPath = Paths.get(privateKeyFile);
                            byte[] privateKeyByteArray = Files.readAllBytes(privateKeyPath);
                            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyByteArray);
                            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

                            // Create cipher object and initialize is as encrypt mode, use PRIVATE key.
                            Cipher rsaCipherEncrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                            rsaCipherEncrypt.init(Cipher.ENCRYPT_MODE, privateKey);

                            
                            // encrypt nonce
                            byte[] encryptedNonce = rsaCipherEncrypt.doFinal(nonce);
                            stringOut.println(Integer.toString(encryptedNonce.length));
                            byteOut.write(encryptedNonce, 0, encryptedNonce.length);
                            byteOut.flush();
                            System.out.println("Sent encrypted nonce to Client");
                            // wait for client response
                            System.out.println(stringIn.readLine());

                            // send signed certificate
                            File certificateFile = new File(signedCertificateFile);
                            byte[] certByteArray = new byte[(int) certificateFile.length()];
                            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(certificateFile));
                            bis.read(certByteArray, 0, certByteArray.length);

                            stringOut.println(Integer.toString(certByteArray.length));
                            System.out.println(stringIn.readLine());
                            byteOut.write(certByteArray, 0, certByteArray.length);
                            byteOut.flush();
                            System.out.println("Sent to client certificate");

                            // receive messages from client
                            String clientResult = stringIn.readLine();
                            System.out.println(clientResult);
                            if (clientResult.contains("Bye!")) {
                                byteOut.close();
                                byteIn.close();
                                stringOut.close();
                                stringIn.close();
                                connectionSocket.close();
                            }
                            
                            System.out.println("File Transfer Initiated");
                            
                            
                            String encryptAESBytesLength = stringIn.readLine();
                            stringOut.println("Server>> Ready to receive encrypted session key");
                            stringOut.flush();

                            byte[] encryptedAESKeyBytes = new byte[Integer.parseInt(encryptAESBytesLength)];
                            OutSideFunction.readByte(encryptedAESKeyBytes, byteIn);
                            System.out.println("Received session key from client");

                            //get encrypted file from client
                            String filename = stringIn.readLine();
                            String encryptFileBytesLength = stringIn.readLine();
                            stringOut.println("Server>> Ready to receive encrypted file: ");
                            stringOut.flush();

                            byte[] encryptedFileBytes = new byte[Integer.parseInt(encryptFileBytesLength)];
                            OutSideFunction.readByte(encryptedFileBytes, byteIn);
                            //received encrypted file from client

                            //Use cipher object, initialize as decrypt mode
                            Cipher rsaCipherDecrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                            rsaCipherDecrypt.init(Cipher.DECRYPT_MODE, privateKey);
                            //decrypt to get AES key bytes

                            byte[] aesKeyByte = rsaCipherDecrypt.doFinal(encryptedAESKeyBytes);
                            System.out.println("AES KEY");
                            SecretKey aesKey = new SecretKeySpec(aesKeyByte, 0, aesKeyByte.length, "AES");

                            //decryption of file creates cipher object 
                            Cipher aesCipherDecrypt = Cipher.getInstance("AES/ECB/PKCS5Padding");
                            aesCipherDecrypt.init(Cipher.DECRYPT_MODE, aesKey);

                            //decrypt AES encrypted File
                            byte[] fileBytes = aesCipherDecrypt.doFinal(encryptedFileBytes);
                            System.out.println("File decrypted");

                            // create new file and write to file
                            FileOutputStream fileOut = new FileOutputStream(filename);
                            fileOut.write(fileBytes, 0, fileBytes.length);
                            System.out.println("File registered into system.");
                            
                            
                            // send confirmation of successful upload to client
                            stringOut.println("SERVER>> Upload file successful!");
                            stringOut.flush();
                            
                            byteOut.close();
                            System.out.println("");
                            byteIn.close();
                            stringOut.close();
                            stringIn.close();
                            connectionSocket.close();
                            System.out.println("This thread should be terminated");
                            executorService.shutdown();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                executorService.execute(task);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    

    
}
