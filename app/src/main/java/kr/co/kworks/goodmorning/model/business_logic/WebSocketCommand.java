package kr.co.kworks.goodmorning.model.business_logic;

import com.google.gson.annotations.SerializedName;

public class WebSocketCommand {
    @SerializedName("commandId")
    public String commandId;
    @SerializedName("cmd")
    public String command;
    @SerializedName("retCode")
    public String returnCode;
    @SerializedName("pan")
    public String pan;
    @SerializedName("tilt")
    public String tilt;
}
