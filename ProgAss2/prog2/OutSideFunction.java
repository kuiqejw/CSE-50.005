/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package prog2;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Cipher;

/**
 *
 * @author ongajong
 */
public class OutSideFunction {
    protected static byte[] encrypt(String fileName, Cipher rsaCipherEncrypt) throws Exception {
        Path filePath = Paths.get(fileName);
        byte[] fileData = Files.readAllBytes(filePath);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int count = 0;
        while (count < fileData.length) {
            byte[] placeHolder;
            if (fileData.length - count >= 117) {
                placeHolder = rsaCipherEncrypt.doFinal(fileData, count, 117);
            } else {
                placeHolder = rsaCipherEncrypt.doFinal(fileData, count, fileData.length - count);
            }
            byteArrayOutputStream.write(placeHolder, 0, placeHolder.length);
            count += 117;
        }
        byte[] encryptedFile = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();

        return encryptedFile;
    }
    protected static void decrypt(byte[] encryptedFile, Cipher rsaCipherDecrypt, String fileName) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int count = 0;
        while (count < encryptedFile.length) {
            byte[] placeHolder;
            if (encryptedFile.length - count >= 128) {
                placeHolder = rsaCipherDecrypt.doFinal(encryptedFile, count, 128);
            } else {
                placeHolder = rsaCipherDecrypt.doFinal(encryptedFile, count, encryptedFile.length - count);
            }
            byteArrayOutputStream.write(placeHolder, 0, placeHolder.length);
            count += 128;
        }
        byte[] decryptedFile = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();

        // create new file and write to file
        FileOutputStream fileOut = new FileOutputStream(fileName);
        fileOut.write(decryptedFile, 0, decryptedFile.length);
        System.out.println("File registered into system.");
    }
    protected static void readByte(byte[] byteArray, InputStream byteIn) throws Exception {
        int offset = 0;
        int numRead = 0;
        while (offset < byteArray.length && (numRead = byteIn.read(byteArray, offset, byteArray.length - offset)) >= 0) {
            offset += numRead;
        }
        if (offset < byteArray.length) {
            System.out.println("File reception incomplete!");
        }
    }
    protected static void closeConnections(OutputStream byteOut, InputStream byteIn, PrintWriter stringOut, BufferedReader stringIn, Socket socket) throws IOException {
        byteOut.close();
        byteIn.close();
        stringOut.close();
        stringIn.close();
        socket.close();
    }
    protected static byte[] nonceGenerator() throws NoSuchAlgorithmException {
        // create secure random number generator
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");

        // get 1024 random bytes
        byte[] nonce = new byte[64];
        secureRandom.nextBytes(nonce);
        return nonce;
    }
}
