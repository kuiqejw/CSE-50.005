package nslab21;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import javax.crypto.*;
import javax.xml.bind.DatatypeConverter;
import sun.misc.BASE64Encoder;

public class DesSolution {

    public static void main(String[] args) throws Exception {
        String fileName = "C:\\Users\\ongajong\\Documents\\50.005\\NSLab21\\src\\nslab21\\largeSize.txt";
        String data = "";
        String line;
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        while ((line = bufferedReader.readLine()) != null) {
            data = data + "\n" + line;
        }
//        System.out.println("Original content: " + data);

//TODO: generate secret key using DES algorithm
        SecretKey key = KeyGenerator.getInstance("DES").generateKey();
//TODO: create cipher object, initialize the ciphers with the given key, choose encryption mode as DES
        Cipher ecipher = Cipher.getInstance("DES");
        ecipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] ciphertextsmall = data.getBytes();
////TODO: do encryption, by calling method Cipher.doFinal().
//        byte[] ciphertextEsmall = ecipher.doFinal(ciphertextsmall);
        byte[] encryptedBytes = ecipher.doFinal(ciphertextsmall);
//TODO: print the length of output encrypted byte[], compare the length of file smallSize.txt and largeSize.txt
        System.out.println("Encrypted small text: " + encryptedBytes.length);
        System.out.println("Small file: " + data.length());
//TODO: do format conversion. Turn the encrypted byte[] format into base64format String using DatatypeConverter
        String base64format = DatatypeConverter.printBase64Binary(encryptedBytes);
//TODO: print the encrypted message (in base64format String format)
        //System.out.println("Cipher bytes for Q 2: " + new String(encryptedBytes));
        System.out.println("Cipher text: " + base64format);
//TODO: create cipher object, initialize the ciphers with the given key, choose decryption mode as DES
        Cipher dcipher = Cipher.getInstance("DES");
        dcipher.init(Cipher.DECRYPT_MODE, key);
//TODO: do decryption, by calling method Cipher.doFinal().
        byte[] decryptedBytes = dcipher.doFinal(encryptedBytes);
//TODO: do format conversion. Convert the decrypted byte[] to String, using "String a = new String(byte_array);"
        String nsmall = new String(decryptedBytes);
        System.out.println("");
//TODO: print the decrypted String text and compare it with original text
        if (nsmall.equals(data)) {
            System.out.println("Data Pass");
        } else {
            System.out.println("Different Byte found, data corrupted");
        }
//        System.out.println("Decrypted text is: " + new String(decryptedBytes));

    }
}
