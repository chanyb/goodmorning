package kr.co.kworks.goodmorning.model.business_logic;

public class Alert {
    public String title, body;

    public Alert() {}
    public Alert(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public Alert(String body) {
        this.title = "알림";
        this.body = body;
    }

}
