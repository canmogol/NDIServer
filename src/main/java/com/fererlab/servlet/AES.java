package com.fererlab.servlet;


import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    public static String symmetricEncrypt(String value, String password) {
        String encryptedString;
        SecretKeySpec skeySpec;
        byte[] encryptValue = value.getBytes();
        Cipher cipher;
        try {
            skeySpec = new SecretKeySpec(password.getBytes(), "AES");
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            encryptedString = Base64.encodeBase64String(cipher.doFinal(encryptValue));
            return encryptedString;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String symmetricDecrypt(String value, String password) {
        Cipher cipher;
        String encryptedString;
        byte[] encryptValue = null;
        SecretKeySpec skeySpec;
        try {
            skeySpec = new SecretKeySpec(password.getBytes(), "AES");
            encryptValue = Base64.decodeBase64(value);
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            encryptedString = new String(cipher.doFinal(encryptValue));
            return encryptedString;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
