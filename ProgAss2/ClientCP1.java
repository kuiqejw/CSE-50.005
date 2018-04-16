package progAss2;

/**
 * Created by trying on 16/4/2018.
 */


import javax.crypto.Cipher;
        import java.io.*;
        import java.net.Socket;
        import java.nio.file.Files;
        import java.nio.file.Path;
        import java.nio.file.Paths;
        import java.security.*;
        import java.security.cert.CertificateFactory;
        import java.security.cert.X509Certificate;
        import java.util.Arrays;

public class ClientCP1 {

    private static final String SERVER_NAME = "localhost";
    private static final int SERVER_PORT = 1234;
    private static final String uploadFilePath = "D:\\50005real\\laura\\rr.txt";
    private static final String uploadFileName = "rr.txt";
    private static final String CACertFile = "D:\\50005real\\laura\\CA.crt.txt";

    public static void main(String[] args) {
        try {
            // create TCP socket for server at specified port
            Socket socket = new Socket(SERVER_NAME, SERVER_PORT);

            // channels for sending and receiving bytes
            OutputStream byteOut = socket.getOutputStream();
            InputStream byteIn = socket.getInputStream();

            // channels for sending and receiving plain text
            PrintWriter stringOut = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader stringIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // initiate conversation with server
            stringOut.println("CLIENT>> Hello SecStore, please prove your identity!");
            stringOut.flush();
            System.out.println("Sent to server: Hello SecStore, please prove your identity!");

            // wait for server to respond
            String firstResponse = stringIn.readLine();
            System.out.println(firstResponse);

            // send a nonce
            byte[] nonce = OutSideFunction.nonceGenerator();
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
            if (!Arrays.equals(digest, decryptedNonce)) {
                System.out.println("Identity verification unsuccessful, closing all connections");
                stringOut.println("CLIENT>> Bye!");
                byteOut.close();
                byteIn.close();
                stringOut.close();
                stringIn.close();
                socket.close();


            } else {
                System.out.println("Server's identity verified");
                stringOut.println("CLIENT>> Ready to upload file!");
                stringOut.flush();
                // **************** END OF AP ***************
                // start file transfer
                System.out.println("INITIALIZING FILE TRANSFER");

                // initial time mark
                Long startTime = System.currentTimeMillis();

                // create cipher object and initialize is as encrypt mode, use PUBLIC key.
                Cipher rsaCipherEncrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                rsaCipherEncrypt.init(Cipher.ENCRYPT_MODE, serverPublicKey);

                // start file transfer
                byte[] encryptedFile = OutSideFunction.encrypt(uploadFilePath, rsaCipherEncrypt);

                // send encrypted file
                stringOut.println(uploadFileName);
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
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}