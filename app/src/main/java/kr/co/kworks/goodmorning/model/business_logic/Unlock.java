package kr.co.kworks.goodmorning.model.business_logic;

import android.content.ContentValues;

import kr.co.kworks.goodmorning.utils.CalendarHandler;
import kr.co.kworks.goodmorning.utils.Column;

public class Unlock {
    public String datetime;
    public int type;
    public int submit;
    public String etc;

    public Unlock() {
        CalendarHandler calendarHandler = new CalendarHandler();
        datetime = calendarHandler.getCurrentDatetimeString();
        type = 1;
        submit = 0;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(Column.unlock_datetime, datetime);
        cv.put(Column.unlock_type, type);
        cv.put(Column.unlock_submit, submit);
        cv.put(Column.unlock_etc, etc);
        return cv;
    }


}
