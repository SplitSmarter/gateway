package com.onewordmax.splitsmart.gateway.utils.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AESUtils {
    public String encryptAES(String data, String secret) {
        try {
//            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
//            SecretKeySpec skeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "AES");
//
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
//
//            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

//            return Base64.getEncoder().encodeToString(encrypted);
            return "Hello";
        } catch (Exception ex) {
            throw new RuntimeException("Error while encrypting JWT", ex);
        }
    }
}
