package kr.co.kworks.goodmorning.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetVehicleListResponse {
    @SerializedName("vehicle_list")
    public List<VehicleInfo> vehicleList;


    public static class VehicleInfo {
        public String router_ssid;
        public String vehicle_no;
        public String gps_de;
        public String gps_time;
        public String wgs84x;
        public String wgs84y;
        public String speed;
        public String altitude;
        public String direction;
        public String real_gps_dt;
        public String weather_dt;
        public String wind_direction;
        public String wind_speed;
        public String weather_tp;
        public String weather_hd;
        public String live_url;
        public String stream_onoff;
    }
}
