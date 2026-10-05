package com.onewordmax.splitsmart.gateway.utils.crypto;

import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class AESGCMUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final byte[] KEY = "01234567890123456789012345678901".getBytes(); // 32 bytes
//    private static final byte[] IV = "abcdefabcdef".getBytes(); // 12 bytes

    public static String encrypt(String plaintext, byte[] IV) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, IV);
        SecretKeySpec keySpec = new SecretKeySpec(KEY, "AES");

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes());

        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String ciphertextBase64, byte[] IV) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, IV);
        SecretKeySpec keySpec = new SecretKeySpec(KEY, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
        byte[] decoded = Base64.getDecoder().decode(ciphertextBase64);
        byte[] decrypted = cipher.doFinal(decoded);

        return new String(decrypted);
    }
}
