package prog2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import javax.crypto.Cipher;
import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerWithoutSecurity {

    private static final int NTHREADS = 10;
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
                                //TODO: Calculate message digest, using MD5 hash function
                                MessageDigest md = MessageDigest.getInstance("MD5");
                                byte[] digest = md.digest(nonce);
                                System.out.println(nonce);
                                System.out.println(digest);
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
                                System.out.println("Sent to client encrypted nonce");

                                System.out.println("File Transfer Initiated");
                                //Use cipher object, initialize as decrypt mode
                                Cipher rsaCipherDecrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                                rsaCipherDecrypt.init(Cipher.DECRYPT_MODE, privateKey);

                                String fileName = stringIn.readLine();
                                String encryptedFileLength = stringIn.readLine();
                                stringOut.println("SERVER>> Ready to receive encrypted file");
                                stringOut.flush();
                                byte[] encryptedFile = new byte[Integer.parseInt(encryptedFileLength)];
                                OutSideFunction.readByte(encryptedFile, byteIn);
                                System.out.println("Received encrypted file from client");

                                OutSideFunction.decrypt(encryptedFile, rsaCipherDecrypt, fileName);

                                // send confirmation of successful upload to client
                                stringOut.println("SERVER>> Upload file successful!");
                                stringOut.flush();
                                byteOut.close();
                                byteIn.close();
                                stringOut.close();
                                stringIn.close();
                                connectionSocket.close();
                                System.out.println("This thread should be terminated");
                                System.exit(1);
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