package kr.co.kworks.goodmorning.model.response;

import com.google.gson.annotations.SerializedName;

public class GetAuthBody {
    @SerializedName("access_token")
    public String accessToken;

    @SerializedName("refresh_token")
    public String refreshToken;

    @SerializedName("vehicle_code")
    public String vehicleCode;

    @SerializedName("vehicle_no")
    public String vehicleNumber;
}
