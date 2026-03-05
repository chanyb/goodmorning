package kr.co.kworks.goodmorning.utils;

import static android.provider.ContactsContract.Directory.PACKAGE_NAME;

import android.content.Context;
import android.util.Base64;

import com.github.rtoshiro.secure.SecureSharedPreferences;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import kr.co.kworks.goodmorning.R;

public class PreferenceHandler {
    Context mContext;
    String MyPreference;

    public PreferenceHandler(Context mContext) {
        this.mContext = mContext;
        MyPreference = mContext.getString(R.string.str_preference);
    }

    public final static int PREFERENCE_INT_DEFAULT = -989899;
    public final static String PREF_KAKAO_AUTH_TOKEN = "kakao_auth_token";
    public final static String PREF_NAVER_AUTH_TOKEN = "naver_auth_token";
    public final static String PREF_KAKAO_USER_ID = "kakao_user_id";
    public final static String PREF_PUBLIC_KEY = "public_key_encoded";
    public final static String PREF_PRIVATE_KEY = "private_key_encoded";
    public final static String PREF_SERVER_PUBLIC_KEY = "server_public_key_encoded";
    public final static String PREF_LIVE_UPDATE = "live_update";
    public final static String PREF_USER_TOKEN = "user_fcm_token";
    public static final String PREF_FTP_HOST = "ftp_host";
    public static final String PREF_FTP_PORT = "ftp_port";
    public static final String PREF_FTP_USER = "ftp_user";
    public static final String PREF_FTP_PASS = "ftp_pass";
    public static final String PREF_FTP_SERVER_FOLDER = "ftp_server_folder";
    public static final String PREF_FTP_SERVER_TYPE = "ftp_server_type";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_PHONE_NUMBER = "user_phone_number";
    public static final String PREF_NEXT_UPLOAD_TIME = "next_upload_time";
    public static final String PREF_FCM_SUBSCRIBE = "fcm_subscribe";


    /**
     * SecureSharedPreference String 값 가져오기
     *
     * @param key
     * @return
     */
    public String getStringPreference(String key) {
        SecureSharedPreferences prefs = null;
        try {
            prefs = new SecureSharedPreferences(mContext, Encrypt(MyPreference, PACKAGE_NAME).replaceAll("\\r\\n|\\r|\\n", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prefs.getString(key, "");
    }

    /**
     * SecureSharedPreference String 값 저장하기
     *
     * @param key
     * @param sValue
     */
    public void setStringPreference(String key, String sValue) {
        SecureSharedPreferences prefs = null;
        try {
            prefs = new SecureSharedPreferences(mContext, Encrypt(MyPreference, PACKAGE_NAME).replaceAll("\\r\\n|\\r|\\n", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        SecureSharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, sValue);
        editor.commit();
        editor.apply();
    }

    /**
     * SecureSharedPreference Int 값 저장하기
     *
     * @param key
     * @param iValue
     */
    public void setIntegerPreference(String key, int iValue) {
        SecureSharedPreferences prefs = null;
        try {
            prefs = new SecureSharedPreferences(mContext, Encrypt(MyPreference, PACKAGE_NAME).replaceAll("\\r\\n|\\r|\\n", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        SecureSharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, iValue);
        editor.commit();
        editor.apply();
    }

    /**
     * SecureSharedPreference Long 값 저장하기
     *
     * @param key
     * @param lValue
     */
    public void setLongPreference(String key, long lValue) {
        SecureSharedPreferences prefs = null;
        try {
            prefs = new SecureSharedPreferences(mContext, Encrypt(MyPreference, PACKAGE_NAME).replaceAll("\\r\\n|\\r|\\n", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        SecureSharedPreferences.Editor editor = prefs.edit();
        editor.putLong(key, lValue);
        editor.commit();
        editor.apply();
    }

    /**
     * SecureSharedPreference int 값 가져오기
     *
     * @param key
     * @return
     */
    public int getIntegerPreference(String key) {
        SecureSharedPreferences prefs = null;
        try {
            prefs = new SecureSharedPreferences(mContext, Encrypt(MyPreference, PACKAGE_NAME).replaceAll("\\r\\n|\\r|\\n", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prefs.getInt(key, PREFERENCE_INT_DEFAULT);
    }

    /**
     * SecureSharedPreference long 값 가져오기
     *
     * @param key
     * @return
     */
    public long getLongPreference(String key, long defaultVal) {
        SecureSharedPreferences prefs = null;
        try {
            prefs = new SecureSharedPreferences(mContext, Encrypt(MyPreference, PACKAGE_NAME).replaceAll("\\r\\n|\\r|\\n", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prefs.getLong(key, defaultVal);
    }

    public String Encrypt(String text, String key) throws Exception {
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
