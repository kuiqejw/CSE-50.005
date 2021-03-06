/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package progass2;

/**
 *
 * @author Dell
 */

import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;
import java.io.*;

public class SecStore {
	private static final int PORT = 4321;
	private static final String privateKeyFile = "C:\\Users\\Dell\\Documents\\NetBeansProjects\\ProgAss2\\src\\progass2\\privateServer.der";
	private static final String myCert = "C:\\Users\\Dell\\Documents\\NetBeansProjects\\Aserver.crt";


	public static void main(String[] args) throws Exception {
		// Establish server connection
		ServerSocket serverSocket = new ServerSocket(PORT);
		
		System.out.println("Server started");

		// Retrieve private key from privateServer.der file to sign message
		Path path = Paths.get(privateKeyFile);
		byte[] privateKeyBytes = Files.readAllBytes(path);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PrivateKey myPrivateKey = keyFactory.generatePrivate(keySpec);

		// Create and initialize encryption cipher
		Cipher ecipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		ecipher.init(Cipher.ENCRYPT_MODE, myPrivateKey);

		// Prepare signed certificate required for authentication
		File cert = new File(myCert);
		byte[] certBytes = new byte[(int) (cert.length())];
		BufferedInputStream certInput = new BufferedInputStream(new FileInputStream(cert));
		certInput.read(certBytes,0,certBytes.length);
		String certStr = DatatypeConverter.printBase64Binary(certBytes);


		try {
			// Waiting for client to initialize connection
			Socket clientSocket = serverSocket.accept();
			System.out.println("Client connected...");
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),true);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(clientSocket.getInputStream()));
			BufferedReader serverIn = new BufferedReader(
					new InputStreamReader(System.in));
			
			// Waiting for client to make request
			System.out.println("Client: " + in.readLine());

			// Retrieve authentication message and sign message before sending to client
			String auMessage = in.readLine();
			System.out.println("Authentication message: " + auMessage);
			while (true){
			    String serverInput = "Hello, this is SecStore!";
			    if (serverInput.equals(auMessage)) {
                    break;
                } else {
                    System.out.println("Authentication message did not match. Please try again!");
                }
            }
			
			byte[] signedMessageBytes = ecipher.doFinal(auMessage.getBytes("UTF-16"));
			String signedMessage = DatatypeConverter.printBase64Binary(signedMessageBytes);

			// Send signed message to client
			out.println(signedMessage);
                        out.flush();
			System.out.println("Sent signed message to client.");

			// Waiting for client to make request
			System.out.println("Client: " + in.readLine());

			// Send signed certificate to the client
			out.println(certBytes.length);
			out.println(certStr);
                        out.flush();
			System.out.println("Sent signed certificate to client.");
			
//			// Wait for client's response
			System.out.println("Client: " + in.readLine()); 

			// Wait for client to send nonce
            String nonceString = in.readLine();
            byte[] nonce = new byte[32];
            nonce = DatatypeConverter.parseBase64Binary(nonceString);

			// Encrypt nonce
            byte[] encryptedNonce = ecipher.doFinal(nonce);
            out.println(DatatypeConverter.printBase64Binary(encryptedNonce));

            // Get decryption cipher with private RSA key
            Cipher dcipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            dcipher.init(Cipher.DECRYPT_MODE, myPrivateKey);

            // Wait for client response
            System.out.println("Client: " + in.readLine());
            
            // Authentication protocol done, starting file transfer
            System.out.println("\n---FILE TRANSFER---");
            
            String clientMessage = in.readLine();
            System.out.println(clientMessage);
            while (clientMessage.equals("Client is sending over file...")){
                // Client sends over file upload start time
                long startTime = Long.parseLong(in.readLine());

                // Client sends over file name
                String fileName = in.readLine();

                // Client sends filesize in bytes over
                int fileSize = Integer.parseInt(in.readLine());
                System.out.println("File size: " + fileSize + " bytes");
                byte[] encryptedBytes = new byte[fileSize];

                // Receives byteString
                String byteString = in.readLine();
                encryptedBytes = DatatypeConverter.parseBase64Binary(byteString);

                // Decrypt file in blocks of 128 bytes
                byte[] decryptedFileBytes = decryptFile(encryptedBytes, dcipher);

                // Write to file
//                FileOutputStream fileOutput = new FileOutputStream("testOutput.txt"); // for testing
                FileOutputStream fileOutput = new FileOutputStream(fileName);
                fileOutput.write(decryptedFileBytes, 0, decryptedFileBytes.length);
                fileOutput.close();

                // Display upload duration
                System.out.println("\nFile transfer complete!");
                System.out.println("Time taken: " + (System.currentTimeMillis() - startTime) + " milliseconds for " + fileName + " to be uploaded.");
                out.println("Upload completed");

                // Check client message
                clientMessage = in.readLine();
            }
            
            in.close();
            out.close();

		} catch (Exception e){
		}

		certInput.close();
		serverSocket.close();
	}

    public static byte[] decryptFile(byte[] encryptedData, Cipher dcipher) throws Exception{
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();

        int start = 0;
        int fileSize = encryptedData.length;
        while (start < fileSize) {
            byte[] tempBuff;
            /* RSA is a block cipher. No matter how long (or rather: short) the input,
             it will produce a 128 byte long output. That explains the 128. */
            if (fileSize - start >= 128) {
                tempBuff = dcipher.doFinal(encryptedData, start, 128);
            } else {
                tempBuff = dcipher.doFinal(encryptedData, start, fileSize - start);
            }
            byteOutput.write(tempBuff, 0, tempBuff.length);
            start += 128;
        }
        byte[] decryptedFileBytes = byteOutput.toByteArray();
        byteOutput.close();
        return decryptedFileBytes;
    }
}