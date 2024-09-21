package com.member.exchange.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CheckSumUtil {
    public static String generateCheckSum(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 hashing algorithm is not available in the current environment. Please check the JDK configuration.", e);
        }
    }
}

