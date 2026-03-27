package kr.co.kworks.goodmorning.model.business_logic;

import android.content.ContentValues;

import kr.co.kworks.goodmorning.utils.CalendarHandler;
import kr.co.kworks.goodmorning.utils.Column;

public class Unlock {
    public String datetime;
    public int submit;

    public Unlock() {
        CalendarHandler calendarHandler = new CalendarHandler();
        datetime = calendarHandler.getCurrentDatetimeString();
        submit = 0;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(Column.unlock_datetime, datetime);
        cv.put(Column.unlock_submit, submit);
        return cv;
    }


}
