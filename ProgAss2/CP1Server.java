package progAss2;

/**
 * Created by trying on 16/4/2018.
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author ongajong
 */
public class CP1Server {
    private static final int NTHREADS = 5;
    private static final Executor exec = Executors.newFixedThreadPool(NTHREADS);
    private static ServerSocket serverSocket;
    private static final int port = 1234;
    private static final String privateKeyFile = "D:\\50005real\\laura\\privateServer.der";
    private static final String signedCertificateFile = "D:\\50005real\\laura\\server.crt";

    private static void handshake(Socket socket)throws Exception{
        OutputStream byteOutput = socket.getOutputStream();
        InputStream byteInput = socket.getInputStream();

        PrintWriter stringOut = new PrintWriter(byteOutput, true);
        BufferedReader stringIn = new BufferedReader(new InputStreamReader(byteInput));

        System.out.println(stringIn.readLine());

        // reply to client
        stringOut.println("SERVER>> Hello, this is SecStore!");
        stringOut.flush();
        System.out.println("Sent to welcome message to client: SERVER>> Hello, this is SecStore!");

        // retrieve nonce from client
        String nonceLength = stringIn.readLine();
        byte[] nonce = new byte[Integer.parseInt(nonceLength)];
        OutSideFunction.readByte(nonce, byteInput);
        System.out.println("Received nonce from client");

        // load private key
        Path privateKeyPath = Paths.get(privateKeyFile);
        byte[] privateKeyByteArray = Files.readAllBytes(privateKeyPath);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyByteArray);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        // Create cipher object and initialize is as encrypt mode, use PRIVATE key.
        Cipher rsaCipherEncrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipherEncrypt.init(Cipher.ENCRYPT_MODE, privateKey);

        //hash nounce
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(nonce);
        byte[] digest = md.digest();

        // encrypt nonce
        byte[] encryptedNonce = rsaCipherEncrypt.doFinal(digest);
        stringOut.println(Integer.toString(encryptedNonce.length));
        byteOutput.write(encryptedNonce, 0, encryptedNonce.length);
        byteOutput.flush();
        System.out.println("Sent encrypted nonce to Client");

        // wait for client response
        String SecondResponse = stringIn.readLine();
        System.out.println(SecondResponse);
        if (SecondResponse.contains("your certificate")) {
            // send signed certificate
            File certificateFile = new File(signedCertificateFile);
            byte[] certByteArray = new byte[(int) certificateFile.length()];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(certificateFile));
            bis.read(certByteArray, 0, certByteArray.length);

            stringOut.println(Integer.toString(certByteArray.length));
            System.out.println(stringIn.readLine());
            byteOutput.write(certByteArray, 0, certByteArray.length);
            byteOutput.flush();
            System.out.println("Sent certificate to client");
        }

        // receive messages from client, "Bye" if verify failed
        String clientResult = stringIn.readLine();
        System.out.println(clientResult);
        if (clientResult.contains("Bye!")) {
            byteOutput.close();
            byteInput.close();
            stringOut.close();
            stringIn.close();
            socket.close();
            return;
        }

        // start file transfer, read file name
        System.out.println("File Transfer Initiated");
        //get encrypted file from client
        String filename = stringIn.readLine();
        // read file length
        String encryptFileBytesLength = stringIn.readLine();
        stringOut.println("Server>> Ready to receive encrypted file: ");
        stringOut.flush();

        byte[] encryptedFileBytes = new byte[Integer.parseInt(encryptFileBytesLength)];
        OutSideFunction.readByte(encryptedFileBytes, byteInput);
        //received encrypted file from client

        //Use cipher object, initialize as decrypt mode
        Cipher rsaCipherDecrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipherDecrypt.init(Cipher.DECRYPT_MODE, privateKey);

        //decrypt RSA encrypted File
        OutSideFunction.decrypt(encryptedFileBytes,rsaCipherDecrypt,filename);

        // send confirmation of successful upload to client
        stringOut.println("SERVER>> Upload file successful!");
        stringOut.flush();

        byteOutput.close();
        System.out.println("");
        byteInput.close();
        stringOut.close();
        stringIn.close();
        socket.close();
        System.out.println("This thread should be terminated");

    }

    public static void main(String[] args) {

        int port = 1234;
        if (args.length > 0) port = Integer.parseInt(args[0]);

        ServerSocket welcomeSocket = null;

        try {
            welcomeSocket = new ServerSocket(port);
            while (true){
                System.out.println("Waiting for connection");
                final Socket connectionSocket = welcomeSocket.accept();
                System.out.println("Accepted connection");
                Runnable task = new Runnable () {
                    public void run() {
                        try {
                            handshake(connectionSocket);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                exec.execute(task);
            }
        } catch (Exception e) {e.printStackTrace();}

    }

}