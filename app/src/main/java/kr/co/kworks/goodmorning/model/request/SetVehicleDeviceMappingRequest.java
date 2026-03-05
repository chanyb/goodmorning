package kr.co.kworks.goodmorning.model.request;

import com.google.gson.annotations.SerializedName;

public class SetVehicleDeviceMappingRequest {
    @SerializedName("device_code")
    public String deviceCode;
    @SerializedName("vehicle_no")
    public String vehicleNumber;
    @SerializedName("fow_se")
    public String force;
}
