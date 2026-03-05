package kr.co.kworks.goodmorning.model.business_logic;

import android.content.ContentValues;

import kr.co.kworks.goodmorning.utils.Column;

public class Azimuth {
    public String datetime;
    public String heading;

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(Column.azimuth_datetime, datetime);
        cv.put(Column.azimuth_heading, heading);
        return cv;
    }
}
