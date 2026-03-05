package kr.co.kworks.goodmorning.model.response;

import com.google.gson.annotations.SerializedName;

public class SetVehicleDeviceMappingResponseBody {
    @SerializedName("vehicle_code")
    public String vehicleCode;
    @SerializedName("vehicle_no")
    public String vehicleNumber;
}
