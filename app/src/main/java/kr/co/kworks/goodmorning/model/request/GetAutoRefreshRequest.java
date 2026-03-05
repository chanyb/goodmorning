package kr.co.kworks.goodmorning.model.request;

import com.google.gson.annotations.SerializedName;

public class GetAutoRefreshRequest {
    @SerializedName("refresh_token")
    public String refreshToken;
}
