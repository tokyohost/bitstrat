package com.bitstrat.utils;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/23 9:17
 * @Content
 */

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class RsaKeyFileUtil {

    // 保存公钥为 Base64 格式的 .txt 文件
    public static void savePublicKey(PublicKey publicKey, String filePath) throws IOException {
        byte[] encoded = publicKey.getEncoded();
        String base64Key = Base64.getEncoder().encodeToString(encoded);
        Files.write(Paths.get(filePath), base64Key.getBytes());
    }

    // 保存私钥为 Base64 格式的 .txt 文件
    public static void savePrivateKey(PrivateKey privateKey, String filePath) throws IOException {
        byte[] encoded = privateKey.getEncoded();
        String base64Key = Base64.getEncoder().encodeToString(encoded);
        Files.write(Paths.get(filePath), base64Key.getBytes());
    }

    // 从 .txt 文件读取并还原公钥
    public static PublicKey loadPublicKey(String filePath) throws Exception {
        String base64Key = new String(Files.readAllBytes(Paths.get(filePath)));
        byte[] decoded = Base64.getDecoder().decode(base64Key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    // 从 .txt 文件读取并还原私钥
    public static PrivateKey loadPrivateKey(String filePath) throws Exception {
        String base64Key = new String(Files.readAllBytes(Paths.get(filePath)));
        byte[] decoded = Base64.getDecoder().decode(base64Key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
}
