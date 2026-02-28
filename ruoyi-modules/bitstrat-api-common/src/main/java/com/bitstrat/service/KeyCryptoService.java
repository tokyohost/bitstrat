package com.bitstrat.service;

import com.bitstrat.domain.CoinsApi;
import com.bitstrat.domain.vo.CoinsApiVo;
import com.bitstrat.utils.HybridCryptoUtil;
import com.bitstrat.utils.RsaKeyLoader;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/23 9:29
 * @Content
 */

@Slf4j
@Component
public class KeyCryptoService {
    private static PrivateKey privateKey;
    private static PublicKey publicKey;
    public KeyCryptoService() throws Exception {
        /**
         * 加载密钥
         */
        privateKey = RsaKeyLoader.loadPrivateKey("secretPath/rsaPrivateKey.txt");
        publicKey = RsaKeyLoader.loadPublicKey("secretPath/rsaPublicKey.txt");

    }

    /**
     * 解密
     * @param encryptedData 待解密数据
     * @param iv iv
     * @param aseKey 加密aeskey
     * @return
     * @throws Exception
     */
    public String decrypt(String encryptedData,String iv,String aseKey) throws Exception {
        log.debug("Decrypted Data: " + encryptedData);
        log.debug("Decrypted AES Key: " + aseKey);
        log.debug("IV: " + iv);
        SecretKey recoveredKey = HybridCryptoUtil.rsaDecryptKey(aseKey, privateKey);
        return HybridCryptoUtil.aesDecrypt(encryptedData, iv, recoveredKey);
    }
    /**
     * 加密
     * @param data 待加密数据
     * @param iv iv
     * @param aseKey 加密aeskey
     * @return
     * @throws Exception
     */
    public  String encrypt(String data,String iv,String aseKey) throws Exception {

        log.debug("Encrypted Data: " + data);
        log.debug("Encrypted AES Key: " + aseKey);
        log.debug("IV: " + iv);
        HybridCryptoUtil.AesResult aesResult = HybridCryptoUtil.aesEncrypt(data, iv, aseKey, privateKey);
        return aesResult.encryptedData;
    }

    /**
     * 创建AES key
     * @return
     * @throws Exception
     */
    public String generateAesKey() throws Exception {
        return HybridCryptoUtil.getAesKey(publicKey);
    }
    /**
     * 创建IV
     * @return
     * @throws Exception
     */
    public String generateIv() throws Exception {
        return HybridCryptoUtil.getIv();
    }

    @SneakyThrows
    public CoinsApiVo decryptApi(CoinsApiVo api) {
        if(StringUtils.isNotEmpty(api.getIv())
            && StringUtils.isNotEmpty(api.getAesKey())){
            //已加密，开始解密
            //key 不加密
            if (StringUtils.isNotEmpty(api.getApiSecurity())) {
                String decryptApiSecurity = this.decrypt(api.getApiSecurity(), api.getIv(), api.getAesKey());
                api.setApiSecurity(decryptApiSecurity);
            }
            if (StringUtils.isNotEmpty(api.getPassphrase())) {
                String decryptPassphrase = this.decrypt(api.getPassphrase(), api.getIv(), api.getAesKey());
                api.setPassphrase(decryptPassphrase);
            }
            api.setIv(null);
            api.setAesKey(null);
//            api.setPassphrase(null);
        }
        return api;
    }

    /**
     * 解密api
     * @param api
     * @return
     */
    @SneakyThrows
    public CoinsApi decryptApi(CoinsApi api) {
        if(StringUtils.isNotEmpty(api.getIv())
            && StringUtils.isNotEmpty(api.getAesKey())){
            //已加密，开始解密

//            String decryptApiKey = this.decrypt(api.getApiKey(), api.getIv(), api.getAesKey());
            if (StringUtils.isNotEmpty(api.getApiSecurity())) {
                String decryptApiSecurity = this.decrypt(api.getApiSecurity(), api.getIv(), api.getAesKey());
                api.setApiSecurity(decryptApiSecurity);
            }
            if (StringUtils.isNotEmpty(api.getPassphrase())) {
                String decryptPassphrase = this.decrypt(api.getPassphrase(), api.getIv(), api.getAesKey());
                api.setPassphrase(decryptPassphrase);
            }
//            api.setApiKey(decryptApiKey);
            api.setIv(null);
            api.setAesKey(null);
//            api.setPassphrase(null);
        }
        return api;
    }

    /**
     * 加密api
     * @param api
     */
    @SneakyThrows
    public CoinsApi encryptApi(CoinsApi api) {
        if(StringUtils.isNotEmpty(api.getIv())
            && StringUtils.isNotEmpty(api.getAesKey())){
            //已有加密凭据，直接加密
        }else{
            //没有加密凭据，生成加密凭据
            String iv = HybridCryptoUtil.getIv();
            String aesKey = HybridCryptoUtil.getAesKey(publicKey);
            api.setIv(iv);
            api.setAesKey(aesKey);
        }
        //key 不加密
//        String encryptApiKey = this.encrypt(api.getApiKey(), api.getIv(), api.getAesKey());
        if (StringUtils.isNotEmpty(api.getApiSecurity())) {
            String encryptApiSecurity = this.encrypt(api.getApiSecurity(), api.getIv(), api.getAesKey());
            api.setApiSecurity(encryptApiSecurity);
        }
        if (StringUtils.isNotEmpty(api.getPassphrase())) {
            String encryptPassphrase = this.encrypt(api.getPassphrase(), api.getIv(), api.getAesKey());
            api.setPassphrase(encryptPassphrase);
        }
//        api.setApiKey(encryptApiKey);


        return api;

    }


//    public static void main(String[] args) throws Exception {
//        privateKey = RsaKeyLoader.loadPrivateKey("secretPath/rsaPrivateKey.txt");
//        publicKey = RsaKeyLoader.loadPublicKey("secretPath/rsaPublicKey.txt");
//
//        String iv = HybridCryptoUtil.getIv();
//        String aesKey = HybridCryptoUtil.getAesKey(publicKey);
//        String data = "123";
//        String encrypted = encrypt(data, iv, aesKey);
//        log.info("Encrypted: " + encrypted);
//        String decrypted = decrypt(encrypted, iv, aesKey);
//        log.info("Decrypted: " + decrypted);
//
//    }

}
