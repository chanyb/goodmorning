package kr.co.kworks.goodmorning.model.business_logic;

public class Confirm {
    public String title, body, leftBtnName, rightBtnName;

    public Confirm() {
        this.title = "알림";
        this.body = "";
        this.leftBtnName = "취소";
        this.rightBtnName = "확인";
    }
    public Confirm(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public Confirm(String body) {
        this.title = "알림";
        this.body = body;
    }

}
