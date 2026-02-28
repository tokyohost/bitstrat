package com.bitstrat.utils;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/23 9:09
 * @Content
 */

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class HybridCryptoUtil {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // --- AES 加密 ---
    public static class AesResult {
        public String encryptedData;
        public String iv;
        public SecretKey aesKey;
        public String aesKeyString;
    }

    public static AesResult aesEncrypt(String plaintext) throws Exception {
        byte[] iv = SecureRandom.getInstanceStrong().generateSeed(12);
        SecretKey aesKey = KeyGenerator.getInstance("AES").generateKey();

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec);

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes("UTF-8"));

        AesResult result = new AesResult();
        result.encryptedData = Base64.getEncoder().encodeToString(ciphertext);
        result.iv = Base64.getEncoder().encodeToString(iv);
        result.aesKey = aesKey;
        return result;
    }

    public static String getIv() throws NoSuchAlgorithmException {
        byte[] iv = SecureRandom.getInstanceStrong().generateSeed(12);
        return Base64.getEncoder().encodeToString(iv);
    }
    public static String getAesKey(PublicKey publicKey) throws Exception {
        SecretKey aesKey = KeyGenerator.getInstance("AES").generateKey();
        return HybridCryptoUtil.rsaEncryptKey(aesKey, publicKey);
    }

    public static AesResult aesEncrypt(String plaintext,String iv,String encryptedAesKey,PrivateKey privateKey) throws Exception {
//        byte[] iv = SecureRandom.getInstanceStrong().generateSeed(12);
        byte[] decodeIv = Base64.getDecoder().decode(iv);

        SecretKey recoveredKey = HybridCryptoUtil.rsaDecryptKey(encryptedAesKey, privateKey);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, decodeIv);
        cipher.init(Cipher.ENCRYPT_MODE, recoveredKey, gcmSpec);

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes("UTF-8"));

        AesResult result = new AesResult();
        result.encryptedData = Base64.getEncoder().encodeToString(ciphertext);
        result.iv = iv;
        result.aesKey = recoveredKey;
        result.aesKeyString = encryptedAesKey;
        return result;
    }

    public static String aesDecrypt(String encryptedData, String ivBase64, SecretKey aesKey) throws Exception {
        byte[] iv = Base64.getDecoder().decode(ivBase64);
        byte[] ciphertext = Base64.getDecoder().decode(encryptedData);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec);

        byte[] plaintext = cipher.doFinal(ciphertext);
        return new String(plaintext, "UTF-8");
    }

    // --- RSA OAEP 加密 AES 密钥 ---
    public static String rsaEncryptKey(SecretKey aesKey, PublicKey rsaPublicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
        byte[] encryptedKey = cipher.doFinal(aesKey.getEncoded());
        return Base64.getEncoder().encodeToString(encryptedKey);
    }

    public static SecretKey rsaDecryptKey(String encryptedKeyBase64, PrivateKey rsaPrivateKey) throws Exception {
        byte[] encryptedKey = Base64.getDecoder().decode(encryptedKeyBase64);
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey);
        byte[] rawKey = cipher.doFinal(encryptedKey);
        return new SecretKeySpec(rawKey, "AES");
    }

    public static void main(String[] args) throws Exception {
        String path = "E:\\WorkSpace\\telecom\\RuoYi-Vue-Plus\\ruoyi-common\\bitstrat-common\\src\\main\\resources\\secretPath";

        // 1. 生成 RSA 密钥对（实际请使用已有密钥）
        PrivateKey privateKey = RsaKeyLoader.loadPrivateKey("secretPath/rsaPrivateKey.txt");
        PublicKey publicKey = RsaKeyLoader.loadPublicKey("secretPath/rsaPublicKey.txt");
//        KeyPair rsaKeyPair = kpg.generateKeyPair();
//        RsaKeyFileUtil.savePrivateKey(rsaKeyPair.getPrivate(), path+"\\rsaPrivateKey.txt");
//        RsaKeyFileUtil.savePublicKey(rsaKeyPair.getPublic(), path+"\\rsaPublicKey.txt");


// 2. 加密 API 信息
        String apiInfo = "api-key-secret-token-xyz";
        HybridCryptoUtil.AesResult aesResult = HybridCryptoUtil.aesEncrypt(apiInfo);
        String encryptedKey = HybridCryptoUtil.rsaEncryptKey(aesResult.aesKey, publicKey);

        System.out.println("Encrypted Data: " + aesResult.encryptedData);
        System.out.println("Encrypted AES Key: " + encryptedKey);
        System.out.println("IV: " + aesResult.iv);

// 3. 解密
        SecretKey recoveredKey = HybridCryptoUtil.rsaDecryptKey(encryptedKey, privateKey);
        String originalData = HybridCryptoUtil.aesDecrypt(aesResult.encryptedData, aesResult.iv, recoveredKey);

        System.out.println("Decrypted Data: " + originalData);

    }
}

