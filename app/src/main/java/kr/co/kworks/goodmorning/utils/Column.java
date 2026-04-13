package kr.co.kworks.goodmorning.utils;

public class Column {
    private Column() {
    }

    /* 명언 */
    public static String wise = "wise";
    public static String wise_column_text = "text";
    public static String wise_column_name = "name";
    public static String[] wise_column_list = new String[] {
        wise_column_text, wise_column_name
    };

    /* 디바이스 정보 */
    public static String device_info = "device_info";
    public static String device_info_column_tel = "device_tel_num";
    public static String device_info_app_token = "device_app_token";
    public static String device_info_column_fcm_token = "device_fcm_token";
    public static String[] device_info_column_list = new String[] {
        device_info_column_tel, device_info_column_fcm_token
    };

    /* User */
    public static String user = "user";
    public static String user_is_login = "user_is_login";

    /* unlock */
    public static String unlock = "unlock";
    public static String unlock_datetime = "unlock_datetime";
    public static String unlock_submit = "unlock_submit";


}
