package com.gitium.core;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.bouncycastle.util.encoders.Base64;

public class SeedCreator {
    public static String newSeed(String username, String password) throws Exception {
        String json = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        String text = md5AndBase64(json) + md5AndBase64(reverse(json))
                + md5AndBase64(username + password + "GITIUMTERMINAL");
        StringBuilder builder = new StringBuilder();
        char[] chars = text.substring(0, 81).toCharArray();
        for (char c : chars) {
            if (c >= 'A' && c <= 'Z') {
                builder.append(c);
            } else {
                int offset = c % 27;
                if (offset == 26) {
                    builder.append('9');
                } else {
                    builder.append((char) ('A' + offset));
                }
            }
        }
        return builder.toString();
    }

    private static String md5(String text) throws NoSuchAlgorithmException {
        byte[] digest = MessageDigest.getInstance("MD5").digest(text.getBytes());
        StringBuilder builder = new StringBuilder();
        for (byte b : digest) {
            String hexString = Integer.toHexString(b & 0xff);
            if (hexString.length() < 2) {
                hexString = "0" + hexString;
            }
            builder.append(hexString);
        }
        return builder.toString();
    }

    private static String base64(String text) {
        return Base64.toBase64String(text.getBytes());
    }

    private static String md5AndBase64(String text) throws NoSuchAlgorithmException {
        return base64(md5(text));
    }

    private static String reverse(String text) {
        return new StringBuilder(text).reverse().toString();
    }
}