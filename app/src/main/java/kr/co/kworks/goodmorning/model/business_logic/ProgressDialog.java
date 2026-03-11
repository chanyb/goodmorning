package kr.co.kworks.goodmorning.model.business_logic;

public class ProgressDialog {
    public String message;
    public int type; // 1:Circular, 2: Bar
    public int progress;


    public ProgressDialog(){
        this.type = 1;
        this.progress = -1;
        this.message = "잠시만 기다려주십시오.\n통신중에 있습니다.";
    }
    public ProgressDialog(int type, String message, int progress){
        this.type = type;
        this.message = message;
        this.progress = progress;
    }
}
