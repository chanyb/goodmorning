package kr.co.kworks.goodmorning.model.request;

import com.google.gson.annotations.SerializedName;

public class UnlockRequest {
    @SerializedName("token")
    public String token;
    @SerializedName("unlock_type")
    public String type;
    @SerializedName("unlock_etc")
    public String etc;
}
