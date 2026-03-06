package kr.co.kworks.goodmorning.utils;

import java.util.Locale;

import kr.co.kworks.goodmorning.R;

public class ApiConstants {

    private ApiConstants() {
    }

    public static final String SERVER_DOMAIN = GlobalApplication.getContext().getString(R.string.domain);
    public static final String SERVER_BASE_URL = "https://" + SERVER_DOMAIN;

    public static final String MAIN_URL = SERVER_BASE_URL + "/main/main.do";;
}
