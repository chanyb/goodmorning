package kr.co.kworks.goodmorning.model.response;

import com.google.gson.annotations.SerializedName;

public class Header {
    @SerializedName("resultcd")
    public String resultCode;

    @SerializedName("resultmsg")
    public String resultMessage;
}