package kr.co.kworks.goodmorning.model.request;

import com.google.gson.annotations.SerializedName;

public class GetAuthRequest {
    @SerializedName("router_ssid")
    public String routerSsid;
}
