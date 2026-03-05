package kr.co.kworks.goodmorning.utils;

import java.util.Locale;

import kr.co.kworks.goodmorning.R;

public class ApiConstants {

    private ApiConstants() {
    }

    public static final String USER_TEST = "http://192.168.0.43:9001" + "/user/posttest";

    public static final String INTEGRATE_SERVER_DOMAIN = GlobalApplication.getContext().getString(R.string.INTEGRATE_SERVER_DOMAIN);
    public static final String INTEGRATE_SERVER_URL = "https://" + INTEGRATE_SERVER_DOMAIN;

    public static final String STREAM_DOMAIN = GlobalApplication.getContext().getString(R.string.STREAM_DOMAIN);

    public static final String WONWOO_CAMERA_IP = "192.168.1.2";
    public static final String WONWOO_VIDEO_URL = String.format(Locale.KOREA, "rtsp://%s:%s@%s:554/%s", "root", "kworks0001819!!", WONWOO_CAMERA_IP, "AVStream1_1");
    public static final String UBITRON_CAMERA_IP = "192.168.1.20";
    public static final String UBITRON_VIDEO_URL = String.format(Locale.KOREA, "rtsp://%s:%s@%s:554/%s", "admin", "kworks0001819!!", UBITRON_CAMERA_IP, "D1-IPCamera");
    public static final String CAMERA_MAIN_URL = "http://" + WONWOO_CAMERA_IP;
    public static final String FFMPEG_RELAY_URL = String.format(Locale.KOREA, "rtsp://%s:%s@%s:554/%s", "root", "kworks0001819!!", WONWOO_CAMERA_IP, "AVStream1_2");

    public static final String LOGIN_URL = INTEGRATE_SERVER_URL + "/app/login/loginPage.do";

    public static final String SENSOR_IP = "192.168.1.40";
    public static final String SENSOR_URL = "http://" + SENSOR_IP;

    public static final String SOFTWARE_UPDATE_SERVER_DOMAIN = "https://apk-link14.kworks.co.kr";

    public static final String OPEN_API_DOMAIN = "http://fd.forest.go.kr";

    public static final String EMPTY_IP = "192.168.1.253";
}
