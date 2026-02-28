package com.bitstrat.utils;


import java.io.InputStream;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;
/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/23 9:26
 * @Content
 */

public class RsaKeyLoader {

    private static String readFromResources(String resourcePath) throws Exception {
        try (InputStream is = RsaKeyLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            return new String(is.readAllBytes());
        }
    }

    public static PublicKey loadPublicKey(String resourcePath) throws Exception {
        String base64Key = readFromResources(resourcePath);
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    public static PrivateKey loadPrivateKey(String resourcePath) throws Exception {
        String base64Key = readFromResources(resourcePath);
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }
}
