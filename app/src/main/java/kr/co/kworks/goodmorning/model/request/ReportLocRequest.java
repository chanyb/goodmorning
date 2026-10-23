package kr.co.kworks.goodmorning.model.request;

public class ReportLocRequest {
    public String token; // encapsulation
    public String loc_datetime;
    public String loc_lat; // encapsulation
    public String loc_lon; // encapsulation
    public float loc_acc;
    public String loc_etc;


    public ReportLocRequest() {
        loc_etc = "";
    }
}
