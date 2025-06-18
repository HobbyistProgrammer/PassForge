package com.benton.passforge.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * @Author: Benton Le
 * @Description: This class uses PBKDF2 Password Hashing with Salt (PBKDF2WithHmacSHA256) to hash the users passwords.
 *               Each password is encrypted with a randomly generated salt.
 */
public class PasswordUtils {

    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 500_000;
    private static final int KEY_LENGTH = 256;

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];

        random.nextBytes(salt);

        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashPassword(char[] password, String salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, Base64.getDecoder().decode(salt), ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = factory.generateSecret(spec).getEncoded();

        return Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifyPassword(char[] password, String hash, String salt) throws Exception {
        String inputHash = hashPassword(password, salt);
        // return hash.equals(inputHash);
        // MessageDigest is used to prevent timing attacks and to stop info leaks from hashes.
        return MessageDigest.isEqual(inputHash.getBytes(StandardCharsets.UTF_8), hash.getBytes(StandardCharsets.UTF_8));
    }
}
