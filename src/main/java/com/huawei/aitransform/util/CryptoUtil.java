package com.huawei.aitransform.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES加解密工具类
 * 用于加密和解密数据库敏感信息
 */
public class CryptoUtil {
    
    /**
     * 默认密钥（建议通过环境变量或配置文件设置）
     * 实际使用时，建议通过环境变量 CRYPTO_KEY 设置密钥
     */
    private static final String DEFAULT_KEY = "HuaweiAITransform2024!@#$%^&*";
    
    /**
     * 加密算法
     */
    private static final String ALGORITHM = "AES";
    
    /**
     * 密钥长度（128位）
     */
    private static final int KEY_LENGTH = 128;
    
    /**
     * 获取加密密钥
     * 优先从环境变量获取，如果没有则使用默认密钥
     */
    private static String getKey() {
        String key = System.getenv("CRYPTO_KEY");
        if (key == null || key.isEmpty()) {
            key = System.getProperty("crypto.key");
        }
        if (key == null || key.isEmpty()) {
            key = DEFAULT_KEY;
        }
        return key;
    }
    
    /**
     * 生成密钥（用于生成新的加密密钥）
     * 
     * @return Base64编码的密钥
     */
    public static String generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_LENGTH, new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("生成密钥失败", e);
        }
    }
    
    /**
     * 从字符串生成密钥
     * 
     * @param keyStr 密钥字符串
     * @return SecretKeySpec对象
     */
    private static SecretKeySpec getSecretKeySpec(String keyStr) {
        // 确保密钥长度为16字节（128位）
        byte[] keyBytes = keyStr.getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[16];
        if (keyBytes.length >= 16) {
            System.arraycopy(keyBytes, 0, key, 0, 16);
        } else {
            System.arraycopy(keyBytes, 0, key, 0, keyBytes.length);
            // 如果密钥不足16字节，用0填充
            for (int i = keyBytes.length; i < 16; i++) {
                key[i] = 0;
            }
        }
        return new SecretKeySpec(key, ALGORITHM);
    }
    
    /**
     * 加密字符串
     * 
     * @param plainText 明文
     * @return Base64编码的密文
     */
    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        try {
            SecretKeySpec secretKey = getSecretKeySpec(getKey());
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }
    
    /**
     * 解密字符串
     * 
     * @param cipherText Base64编码的密文
     * @return 明文
     */
    public static String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        
        try {
            SecretKeySpec secretKey = getSecretKeySpec(getKey());
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 判断字符串是否为加密格式（以ENC(开头，)结尾）
     * 
     * @param text 待判断的字符串
     * @return 是否为加密格式
     */
    public static boolean isEncrypted(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.startsWith("ENC(") && text.endsWith(")");
    }
    
    /**
     * 提取加密内容（去除ENC()包装）
     * 
     * @param encryptedText 加密格式的字符串，如ENC(密文)
     * @return 密文部分
     */
    public static String extractEncryptedContent(String encryptedText) {
        if (isEncrypted(encryptedText)) {
            return encryptedText.substring(4, encryptedText.length() - 1);
        }
        return encryptedText;
    }
    
    /**
     * 解密配置值（如果是加密格式则解密，否则直接返回）
     * 
     * @param value 配置值
     * @return 解密后的值
     */
    public static String decryptIfNeeded(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        
        if (isEncrypted(value)) {
            String encryptedContent = extractEncryptedContent(value);
            return decrypt(encryptedContent);
        }
        
        return value;
    }
}

