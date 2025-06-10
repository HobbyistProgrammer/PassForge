package com.benton.passforge.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionUtils {

    private static final int ITERATIONS = 100_000;
    private static final int KEY_LENGTH = 256;
    private static final int IV_LENGTH = 16;

    private static final String ALOGRITHM = "AES/CBC/PKCS5Padding";

    public static SecretKey getSecretKeyFromPassword(char[] password,String salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, Base64.getDecoder().decode(salt), ITERATIONS, KEY_LENGTH);
        SecretKeyFactory fact = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] key = fact.generateSecret(spec).getEncoded();

        return new SecretKeySpec(key, "AES");
    }

    public static String encrypt(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALOGRITHM);
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] encrypted = cipher.doFinal(data.getBytes("UTF-8"));
        byte[] encryptedWithIv = new byte[iv.length + encrypted.length];

        System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
        System.arraycopy(encrypted, 0, encryptedWithIv, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }

    public static String decrypt(String encryptedData, SecretKey key) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        byte[] iv = new byte[IV_LENGTH];

        System.arraycopy(decoded, 0, iv, 0, IV_LENGTH);

        byte[] actualEncrypted = new byte[decoded.length - IV_LENGTH];

        System.arraycopy(decoded, IV_LENGTH, actualEncrypted, 0, actualEncrypted.length);

        Cipher cipher = Cipher.getInstance(ALOGRITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] original = cipher.doFinal(actualEncrypted);

        return new String(original, "UTF-8");
    }
}
