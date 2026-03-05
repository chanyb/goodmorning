package kr.co.kworks.goodmorning.model.request;

import com.google.gson.annotations.SerializedName;

public class SetVehicleRecptnDataRequest {
    @SerializedName("vehicle_code")
    public String vehicleCode;
    @SerializedName("gps_de")
    public String date;
    @SerializedName("gps_time")
    public String time;
    @SerializedName("real_gps_dt")
    public String realGpsDatetime;
    @SerializedName("wgs84x")
    public String longitude;
    @SerializedName("wgs84y")
    public String latitude;
    @SerializedName("speed")
    public String speed;
    @SerializedName("altitude")
    public String altitude;
    @SerializedName("direction")
    public String direction;
    @SerializedName("router_ssid")
    public String routerSsid;
    @SerializedName("weather_dt")
    public String weatherDatetime;
    @SerializedName("wind_direction")
    public String windDirection;
    @SerializedName("wind_speed")
    public String windSpeed;
    @SerializedName("weather_tp")
    public String temperature;
    @SerializedName("weather_hd")
    public String humidity;

    public SetVehicleRecptnDataRequest () {
        weatherDatetime = "20000101090000";
        windSpeed = "-99.99";
        temperature = "-99.99";
        humidity = "-99.99";
        windDirection = "-99.99";
        latitude = "0.0";
        longitude = "0.0";
    }
}
