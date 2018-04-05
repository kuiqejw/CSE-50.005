/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package progass2;

import java.security.SecureRandom;

/**
 *
 * @author Dell
 */
public class Crypto {
    public static byte[] randomByteGenerator(int byteArrSize){
        SecureRandom random = new SecureRandom();
        byte[] randomByteArray = new byte[byteArrSize];
        random.nextBytes(randomByteArray);
        return randomByteArray;
    }
    
}
