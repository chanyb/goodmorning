package kr.co.kworks.goodmorning.model.business_logic;

import android.content.ContentValues;

import kr.co.kworks.goodmorning.utils.Column;

public class Wise {
    public String text;
    public String name;

    public Wise() {
        text = "";
        name = "";
    }

    public Wise(String text, String name) {
        this.text = text;
        this.name = name;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(Column.wise_column_text, text);
        cv.put(Column.wise_column_name, name);
        return cv;
    }
}
