package kr.co.kworks.goodmorning.model.business_logic;

import android.content.ContentValues;

import androidx.annotation.NonNull;

import java.util.Locale;

import kr.co.kworks.goodmorning.utils.Column;

public class Token {
    public String accessToken;
    public String refreshToken;

    public Token(String _accessToken, String _refreshToken) {
        accessToken = _accessToken;
        refreshToken = _refreshToken;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(Column.token_access, accessToken);
        cv.put(Column.token_refresh, refreshToken);
        return cv;
    }

    @NonNull
    public String toString() {
        return String.format(Locale.KOREA, "[AccessToken] %s\n [RefreshToken] %s", accessToken, refreshToken);
    }
}
