package kr.co.kworks.goodmorning.model.request;

import com.google.gson.annotations.SerializedName;

public class GetHelliListRequest {
    @SerializedName("vehicle_code")
    public String vehicleCode;
    @SerializedName("wgs84y")
    public String latitude;
    @SerializedName("wgs84x")
    public String longitude;


    public GetHelliListRequest(
        String vehicleCode,
        String latitude,
        String longitude
    ) {
        this.vehicleCode = vehicleCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
