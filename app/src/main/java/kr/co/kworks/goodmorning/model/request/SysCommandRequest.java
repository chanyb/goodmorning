package kr.co.kworks.goodmorning.model.request;

import com.google.gson.annotations.SerializedName;

public class SysCommandRequest {
    public JsonData jsonData;

    public SysCommandRequest(JsonData jsonData) {
        this.jsonData = jsonData;
    }

    public static class JsonData {
        public Data data;

        public JsonData(Data data) {
            this.data = data;
        }
    }

    public static class Data {
        @SerializedName("System.Device.Temperature.Info")
        public String systemDeviceTemperatureInfo;

        public Data() {
            this.systemDeviceTemperatureInfo = "";
        }
    }
}