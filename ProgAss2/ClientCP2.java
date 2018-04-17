/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package prog2;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class ClientCP2 {

    private static final String serverAddress = "localhost";
    private static int port = 1234;
    private static final String uploadFilePath = "C:\\Users\\ongajong\\Documents\\50.005\\Prog2\\src\\prog2\\smallFile.txt";
    private static String filename = "small.txt";
    private static final String CACertFile = "C:\\Users\\ongajong\\Documents\\50.005\\Prog2\\src\\prog2\\CA.crt.txt";

    public static void main(String[] args) {
        Socket clientSocket;
        try {

            System.out.println("Establishing connection to server...");

            // Connect to server and get the input and output streams
            clientSocket = new Socket(serverAddress, port);
            // channels for sending and receiving bytes
            OutputStream byteOut = clientSocket.getOutputStream();
            InputStream byteIn = clientSocket.getInputStream();

            PrintWriter stringOut = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader stringIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
               //Wait for server to respond
            stringOut.println("CLIENT>> Hello SecStore, please prove your identity!");
            stringOut.flush();
            System.out.println("Sent to server: Hello SecStore, please prove your identity!");
                // wait for server to respond
            String helloSecStore = stringIn.readLine();
            System.out.println(helloSecStore);

            // send a nonce
            SecureRandom random = new SecureRandom();
            BigInteger placeHolder = new BigInteger(130, random);
//            System.out.println("That integer you are having " + placeHolder);
            byte[] nonce = placeHolder.toByteArray();
            if (helloSecStore.contains("this is SecStore")) {
                stringOut.println(Integer.toString(nonce.length));
                byteOut.write(nonce);
                byteOut.flush();
                System.out.println("Sent to server a fresh nonce");
            } else {
                OutSideFunction.closeConnections(byteOut, byteIn, stringOut, stringIn, clientSocket);
            }

            //System.out.println(nonce);
            // retrieve encrypted nonce from server
            String encryptedNonceLength = stringIn.readLine();
            System.out.println("encrypted Nonce length: " + encryptedNonceLength);
            byte[] encryptedNonce = new byte[Integer.parseInt(encryptedNonceLength)];
            OutSideFunction.readByte(encryptedNonce, byteIn);
            System.out.println("Received encrypted nonce from server");

            // ask for certificate
            stringOut.println("CLIENT>> Give me your certificate signed by CA");
            stringOut.flush();
            System.out.println("Sent to server: Give me your certificate signed by CA");

            // extract public key from CA certificate
            InputStream fis = new FileInputStream(CACertFile);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate caCert = (X509Certificate) certificateFactory.generateCertificate(fis);
            PublicKey caPublicKey = caCert.getPublicKey();
            System.out.println("CA public key extracted");

            // retrieve signed certificate from server
            String certByteArrayLength = stringIn.readLine();
            stringOut.println("CLIENT>> Ready to get certificate");
            stringOut.flush();
            byte[] certByteArray = new byte[Integer.parseInt(certByteArrayLength)];
            OutSideFunction.readByte(certByteArray, byteIn);
            System.out.println("Received certificate from server");

            // verifying signed certificate from server using CA public key
            System.out.println("Verifying certificate from server");
            InputStream certInputStream = new ByteArrayInputStream(certByteArray);
            X509Certificate signedCertificate = (X509Certificate) certificateFactory.generateCertificate(certInputStream);

            signedCertificate.checkValidity();
            signedCertificate.verify(caPublicKey);
            System.out.println("Signed certificate validity checked and verified");

            // extract public key from server's signed certificate
            PublicKey serverPublicKey = signedCertificate.getPublicKey();

            // create cipher object and initialize it as decrypt mode, using PUBLIC key.
            Cipher cipherDecrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipherDecrypt.init(Cipher.DECRYPT_MODE, serverPublicKey);

           // decrypt nonce
            byte[] decryptedNonce = cipherDecrypt.doFinal(encryptedNonce);
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(nonce);
            byte[] digest = md.digest();
            // handles connection after decrypting nonce.
            if (Arrays.equals(digest,decryptedNonce)) {
                System.out.println("Server's identity verified");
                stringOut.println("CLIENT>> Ready to upload file!");
                stringOut.flush();
            } else {
                System.out.println("Identity verification unsuccessful, closing all connections");
                stringOut.println("CLIENT>> Bye!");
                OutSideFunction.closeConnections(byteOut, byteIn, stringOut, stringIn, clientSocket);
            }

            System.out.println("Sending file...");
            // initial time mark
            Long startTime = System.currentTimeMillis();

            // create cipher object and initialize is as encrypt mode, use PUBLIC key.
            Cipher rsaCipherEncrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipherEncrypt.init(Cipher.ENCRYPT_MODE, serverPublicKey);

            // generate AES key
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey aesKey = keyGen.generateKey();

            // convert secret key to byte array
            byte[] aesKeyBytes = aesKey.getEncoded();

            // encrypt AES key
            byte[] encryptedAESKeyBytes = rsaCipherEncrypt.doFinal(aesKeyBytes);

            // create cipher object for file encryption
            Cipher aesEnCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            aesEnCipher.init(Cipher.ENCRYPT_MODE, aesKey);

            // get bytes of file needed for transfer
            File file = new File(uploadFilePath);
            byte[] fileBytes = new byte[(int) file.length()];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            bis.read(fileBytes, 0, fileBytes.length);

            // encrypt file with AES key
            byte[] encryptedFileBytes = aesEnCipher.doFinal(fileBytes);

            // send encrypted AES session key
            stringOut.println(encryptedAESKeyBytes.length);
            stringOut.flush();
            System.out.println(stringIn.readLine());
            byteOut.write(encryptedAESKeyBytes, 0, encryptedAESKeyBytes.length);
            byteOut.flush();
            System.out.println("Sent to server encrypted session key");

            // upload encrypted file to server
            stringOut.println(filename);
            stringOut.println(encryptedFileBytes.length);
            stringOut.flush();
            System.out.println(stringIn.readLine());
            byteOut.write(encryptedFileBytes, 0, encryptedFileBytes.length);
            byteOut.flush();
            System.out.println("Sent to server encrypted file");

            // confirmation of successful file upload
            System.out.println(stringIn.readLine());

            Long endTime = System.currentTimeMillis();
            System.out.println("Uploading time spent is: " + (endTime - startTime) + "ms");
            OutSideFunction.closeConnections(byteOut, byteIn, stringOut, stringIn, clientSocket);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
