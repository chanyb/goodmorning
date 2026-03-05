package kr.co.kworks.goodmorning.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetHelliListResponse {
    @SerializedName("heli_list")
    public List<HelliInfo> helliList;


    public static class HelliInfo {
        public String gps_de;
        public String gps_time;
        public String wgs84x;
        public String wgs84y;
        public String heli_code;
        public String heli_nm;
        public String heli_callnum;
        public String heli_dept_nm;
        public String speed;
        public String altitude;
        public String direction;
        public String live_url;
        public String stream_onoff;
    }
}
