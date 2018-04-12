package prog2;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.crypto.Cipher;

public class ClientWithoutSecurity {

    private static final String serverAddress = "localhost";
    private static int port = 1234;
    private static final String uploadFilePath = "C:\\Users\\ongajong\\Documents\\50.005\\Prog2\\src\\prog2\\smallFile.txt";
    private static String filename = "small.txt";
    private static final String CACertFile = "C:\\Users\\ongajong\\Documents\\50.005\\Prog2\\src\\prog2\\CA.crt.txt";

    public static void main(String[] args) {

        if (args.length > 0) {
            filename = args[0];
        }

        if (args.length > 1) {
            filename = args[1];
        }

        if (args.length > 2) {
            port = Integer.parseInt(args[2]);
        }

        int numBytes = 0;

        Socket clientSocket = null;

        DataOutputStream toServer = null;
        DataInputStream fromServer = null;

        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedFileInputStream = null;

        long timeStarted = System.nanoTime();

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
            String firstResponse = stringIn.readLine();
            System.out.println(firstResponse);

            // send a nonce
            byte[] nonce = generateNonce();
            if (firstResponse.contains("this is SecStore")) {
                stringOut.println(Integer.toString(nonce.length));
                byteOut.write(nonce);
                byteOut.flush();
                System.out.println("Sent to server a fresh nonce");
            }

            System.out.println(nonce);
            // retrieve encrypted nonce from server
            String encryptedNonceLength = stringIn.readLine();
            System.out.println("encrypted Nonce length: " + encryptedNonceLength);
            byte[] encryptedNonce = new byte[Integer.parseInt(encryptedNonceLength)];
            ServerWithoutSecurity.readByte(encryptedNonce, byteIn);
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
            ServerWithoutSecurity.readByte(certByteArray, byteIn);
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

            // handles connection after decrypting nonce.
            if (Arrays.equals(nonce, decryptedNonce)) {
                System.out.println("Server's identity verified");
                stringOut.println("CLIENT>> Ready to upload file!");
                stringOut.flush();
            } else {
                System.out.println("Identity verification unsuccessful, closing all connections");
                stringOut.println("CLIENT>> Bye!");
                byteOut.close();
                byteIn.close();
                stringOut.close();
                stringIn.close();
                clientSocket.close();
            }

            System.out.println("Sending file...");
            // initial time mark
            Long startTime = System.currentTimeMillis();

            // create cipher object and initialize is as encrypt mode, use PUBLIC key.
            Cipher rsaCipherEncrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipherEncrypt.init(Cipher.ENCRYPT_MODE, serverPublicKey);

            // start file transfer
            byte[] encryptedFile = ServerWithoutSecurity.encrypt(uploadFilePath, rsaCipherEncrypt);

            // send encrypted file
            stringOut.println(filename);
            stringOut.println(encryptedFile.length);
            System.out.println(stringIn.readLine());
            byteOut.write(encryptedFile, 0, encryptedFile.length);
            byteOut.flush();

            // confirmation of successful file upload
            System.out.println(stringIn.readLine());

            Long endTime = System.currentTimeMillis();
            System.out.println("Uploading time spent is: " + (endTime - startTime) + "ms");
                byteOut.close();
                byteIn.close();
                stringOut.close();
                stringIn.close();
                clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] generateNonce() throws NoSuchAlgorithmException {
        // create secure random number generator
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");

        // get 1024 random bytes
        byte[] nonce = new byte[64];
        secureRandom.nextBytes(nonce);
        return nonce;
    }
}
