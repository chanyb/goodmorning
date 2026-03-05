package kr.co.kworks.goodmorning.model.request;

import com.google.gson.annotations.SerializedName;

public class GetVehicleListRequest {
    @SerializedName("vehicle_code")
    public String vehicleCode;
    @SerializedName("wgs84y")
    public String latitude;
    @SerializedName("wgs84x")
    public String longitude;


    public GetVehicleListRequest(
        String vehicleCode,
        String latitude,
        String longitude
    ) {
        this.vehicleCode = vehicleCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
