package kr.co.kworks.goodmorning.model.response;

import com.google.gson.annotations.SerializedName;

public class SysCommandResponse {
    public JsonData retData;

    public SysCommandResponse(JsonData retData) {
        this.retData = retData;
    }

    public static class JsonData {
        @SerializedName("System.Device.Temperature.Info")
        public CameraCommonResponse temperature;

    }
}