package nslab21;
import javax.xml.bind.DatatypeConverter;
import javax.crypto.Cipher;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.*;


public class DigitalSignatureSolution {

    public static void main(String[] args) throws Exception {
//Read the text file and save to String data
            String fileName =  "C:\\Users\\ongajong\\Documents\\50.005\\NSLab21\\src\\nslab21\\smallSIze.txt";
            String data = "";
            String line;
            BufferedReader bufferedReader = new BufferedReader( new FileReader(fileName));
            while((line= bufferedReader.readLine())!=null){
                data = data +"\n" + line;
            }
            System.out.println("Original content: "+ data);

//TODO: generate a RSA keypair, initialize as 1024 bits, get public key and private key from this keypair.
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            KeyPair keyPair = keyGen.generateKeyPair();
            Key publicKey =  keyPair.getPublic();
            Key privateKey = keyPair.getPrivate();

//TODO: Calculate message digest, using MD5 hash function

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(data.getBytes());
//TODO: print the length of output digest byte[], compare the length of file smallSize.txt and largeSize.txt
            System.out.println("digest small length: "+ digest.length);
            System.out.println("digest small: " + digest);
            System.out.println("Small file length: "+ data.length());
            System.out.println("Message digest (MD5): " + DatatypeConverter.printBase64Binary(digest));
//TODO: Create RSA("RSA/ECB/PKCS1Padding") cipher object and initialize is as encrypt mode, use PRIVATE key.
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE,  privateKey);

//TODO: encrypt digest message
            byte[] cipherText = cipher.doFinal(new String(digest).getBytes());
            System.out.println("Signed bytes[] length: "+ cipherText.length);
            System.out.println(cipherText);
            String base64format = DatatypeConverter.printBase64Binary(cipherText);
//TODO: print the encrypted message (in base64format String using DatatypeConverter) 

            System.out.println("digital signature: " + base64format);
//TODO: Create RSA("RSA/ECB/PKCS1Padding") cipher object and initialize is as decrypt mode, use PUBLIC key.           
            Cipher dcipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            dcipher.init(Cipher.DECRYPT_MODE,  publicKey);
//TODO: decrypt message
            byte[] decryptedBytes = dcipher.doFinal(cipherText);
            String decipheredMessage = new String(decryptedBytes);
//TODO: print the decrypted message (in base64format String using DatatypeConverter), compare with origin digest 

            System.out.println("digital signature: " + DatatypeConverter.printBase64Binary(decryptedBytes));
            System.out.println( "origin digest length: " + decryptedBytes.length);
            System.out.println( "signed digest length: " + cipherText.length);
            if (decipheredMessage.equals(new String(digest))){
                System.out.println("Data pass");
            }else{
                System.out.println("Differet Byte found, Data corrupted");
            }


    }

}
