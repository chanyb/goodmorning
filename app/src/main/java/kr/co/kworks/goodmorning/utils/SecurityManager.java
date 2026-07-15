package kr.co.kworks.goodmorning.utils;

import android.content.Context;
import android.util.Base64;


import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import kr.co.kworks.goodmorning.R;

public class SecurityManager {
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String ALGORITHM = "AES";

    private Context mContext;
    private String key;
    public SecurityManager(Context context) {
        mContext = context;
        key = context.getString(R.string.aes_key);
    }

    public String decryptAES(String text) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        byte[] keyBytes = new byte[16];
        byte[] b = key.getBytes("UTF-8");
        int len = b.length;
        if (len > keyBytes.length) len = keyBytes.length;
        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] results = cipher.doFinal(Base64.decode(text, 0));
        return new String(results, "UTF-8");
    }

    public String encryptAES(String text) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        byte[] keyBytes = new byte[16];
        byte[] b = key.getBytes("UTF-8");

        int len = b.length;

        if (len > keyBytes.length) len = keyBytes.length;

        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] results = cipher.doFinal(text.getBytes("UTF-8"));

        return Base64.encodeToString(results, 0);
    }

    /**
     * AES-256 암호화
     * @param str
     * @return
     * @throws Exception
     */
    public String encAES(String str) throws Exception {
        SecretKeySpec keySpec = getKeySpec();

        // CBC에서는 매번 새로운 16바이트 IV 사용
        byte[] iv = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] encrypted = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));

        // 저장/전송 편의를 위해 IV + 암호문 합쳐서 Base64
        byte[] ivAndCipher = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, ivAndCipher, 0, iv.length);
        System.arraycopy(encrypted, 0, ivAndCipher, iv.length, encrypted.length);

        return java.util.Base64.getEncoder().encodeToString(ivAndCipher);
    }

    /**
     * AES-256 복호화
     * @param enStr
     * @return
     * @throws Exception
     */
    public String decAES(String enStr) throws Exception {
        SecretKeySpec keySpec = getKeySpec();

        byte[] ivAndCipher = java.util.Base64.getDecoder().decode(enStr);

        if (ivAndCipher.length < 17) {
            throw new IllegalArgumentException("잘못된 암호문 형식입니다.");
        }

        byte[] iv = Arrays.copyOfRange(ivAndCipher, 0, 16);
        byte[] cipherBytes = Arrays.copyOfRange(ivAndCipher, 16, ivAndCipher.length);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] decrypted = cipher.doFinal(cipherBytes);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    private SecretKeySpec getKeySpec() {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("AES-256 키는 32바이트여야 합니다.");
        }
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

}
