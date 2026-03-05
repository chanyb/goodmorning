package kr.co.kworks.goodmorning.utils;

import android.content.Context;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SecurityManager {
    private Context mContext;
    private PreferenceHandler preferenceHandler;
    public SecurityManager(Context context) {
        mContext = context;
        preferenceHandler = new PreferenceHandler(mContext);
    }
    public PublicKey getServerPublicKey() {
        String encodedServerPublicKey = preferenceHandler.getStringPreference(PreferenceHandler.PREF_SERVER_PUBLIC_KEY);
        byte[] decodedServerPublicKey = Base64.decode(encodedServerPublicKey, 0);
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(decodedServerPublicKey);
            return keyFactory.generatePublic(publicKeySpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public PublicKey getMyPublicKey() {
        String encodedServerPublicKey = preferenceHandler.getStringPreference(PreferenceHandler.PREF_PUBLIC_KEY);
        byte[] decodedServerPublicKey = Base64.decode(encodedServerPublicKey, 0);
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(decodedServerPublicKey);
            return keyFactory.generatePublic(publicKeySpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String encryptRSA(PublicKey publicKey, String message) {
        try {
            Cipher cipherEncrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipherEncrypt.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptMessageBytes = cipherEncrypt.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(encryptMessageBytes, 0);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String decryptRSA(String base64EncryptMessage) {
        Cipher cipherEncrypt = null;
        try {
            byte[] decodedPrivateKey = Base64.decode(preferenceHandler.getStringPreference(PreferenceHandler.PREF_PRIVATE_KEY),0);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(decodedPrivateKey);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            cipherEncrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipherEncrypt.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptMessageBytes = cipherEncrypt.doFinal(Base64.decode(base64EncryptMessage,0));
            return new String(decryptMessageBytes, StandardCharsets.UTF_8);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                 InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public String decryptAES(String text, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] keyBytes = new byte[16];
        byte[] b = key.getBytes("UTF-8");
        int len = b.length;
        if (len > keyBytes.length) len = keyBytes.length;
        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] results = cipher.doFinal(Base64.decode(text, 0));
        return new String(results, "UTF-8");
    }

    public String encryptAES(String text, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] keyBytes = new byte[16];
        byte[] b = key.getBytes("UTF-8");

        int len = b.length;

        if (len > keyBytes.length) len = keyBytes.length;

        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] results = cipher.doFinal(text.getBytes("UTF-8"));

        return Base64.encodeToString(results, 0);
    }
}
