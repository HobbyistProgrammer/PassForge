package com.benton.passforge.model;

import com.benton.passforge.controller.MainController;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.locks.Lock;

public class LockoutManager {

    private static final Path APP_DATA_FOLDER = Paths.get(System.getProperty("user.home"), ".passforge");
    private static final Path DATABASE_FILE = APP_DATA_FOLDER.resolve("passforge.db");
    private static final Path LOCKOUT_FILE = APP_DATA_FOLDER.resolve("auth_lockout.dat");
    private static final Path FLAG_FILE = APP_DATA_FOLDER.resolve("user_registration.flag");

    public static class LockoutData {
        public int failedAttempts;
        public long lockoutEndTime;

        public LockoutData(int failedAttempts, long lockoutEndTime) {
            this.failedAttempts = failedAttempts;
            this.lockoutEndTime = lockoutEndTime;
            // System.out.println(LOCKOUT_FILE);
        }

        public String toJson() { return String.format("{\"failedAttempts\":%d,\"lockoutEndTime\":%d}", failedAttempts, lockoutEndTime); }

        public static LockoutData fromJson(String json) {
            try {
                json = json.replaceAll("[{}\"]", "");
                String[] parts = json.split(",");
                int failed = 0;
                long end = 0;

                for (String part : parts) {
                    String[] keyVal = part.split(":");
                    if (keyVal[0].trim().equals("failedAttempts")) {
                        failed = Integer.parseInt(keyVal[1].trim());
                    } else if (keyVal[0].trim().equals("lockoutEndTime")) {
                        end = Long.parseLong(keyVal[1].trim());
                    }
                }

                return new LockoutData(failed, end);

            } catch (Exception e) {
                e.printStackTrace();
                return new LockoutData(0, 0);
            }
        }
    }

    private static String getPrimaryMacAddress() {
        try {
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();

            while (networks.hasMoreElements()) {
                NetworkInterface network = networks.nextElement();

                // Skip virtual or loopback interfaces
                if (network.isLoopback() || network.isVirtual() || !network.isUp()) {
                    continue;
                }

                byte[] mac = network.getHardwareAddress();
                if (mac != null && mac.length > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    return sb.toString(); // Return the first valid MAC
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return "UNKNOWN";
    }

    private static SecretKey createKeyFromDevice() throws Exception {
        String mac = getPrimaryMacAddress();
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] hash = sha.digest(mac.getBytes(StandardCharsets.UTF_8));

        return new SecretKeySpec(Arrays.copyOf(hash, 16), "AES");
    }

    public static LockoutData load() {
        try {
            Files.createDirectories(APP_DATA_FOLDER);

            boolean lockoutFileExists = Files.exists(LOCKOUT_FILE);
            boolean flagFileExists = Files.exists(FLAG_FILE);
            boolean dbFileExists = Files.exists(DATABASE_FILE);

            if (!lockoutFileExists && flagFileExists) {
                return new LockoutData(5, System.currentTimeMillis() + 900_000);
            }

            if (!flagFileExists && !lockoutFileExists && dbFileExists) {
                Files.createFile(FLAG_FILE);

                return new LockoutData(5, System.currentTimeMillis() + 900_000);
            }

            if (!flagFileExists && !dbFileExists) {
                return new LockoutData(0, 0);
            }

            SecretKey key = createKeyFromDevice();
            byte[] encrypted = Files.readAllBytes(LOCKOUT_FILE);
            Cipher cipher = Cipher.getInstance("AES");

            cipher.init(Cipher.DECRYPT_MODE, key);
            // System.out.println("Device ID used for key: " + createKeyFromDevice());
            byte[] decrypted = cipher.doFinal(encrypted);

            // System.out.println("Loading: returning decryted");
            String json = new String(decrypted, StandardCharsets.UTF_8);
            // System.out.println("Decrypted JSON: " + json);

            return LockoutData.fromJson(json);
        } catch (Exception e) {
            e.printStackTrace();
            return new LockoutData(5,System.currentTimeMillis() + 900_000);
        }
    }

    public static void save(LockoutData data) {
        try {
            Files.createDirectories(APP_DATA_FOLDER);

            SecretKey key = createKeyFromDevice();
            Cipher cipher = Cipher.getInstance("AES");

            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encrypted = cipher.doFinal(data.toJson().getBytes(StandardCharsets.UTF_8));

            System.out.println("Saving: " + data.failedAttempts + ", " + data.lockoutEndTime);

            Files.write(LOCKOUT_FILE, encrypted, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
