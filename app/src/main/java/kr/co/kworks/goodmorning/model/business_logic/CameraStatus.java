package kr.co.kworks.goodmorning.model.business_logic;

import android.content.ContentValues;

import kr.co.kworks.goodmorning.utils.Column;

public class CameraStatus {
    public String datetime;
    public String pan;
    public String tilt;
    public String moveStart;
    public String moveEnd;

    public CameraStatus(String datetime) {
        this.datetime = datetime;
    }

    public CameraStatus() {
    }

    public CameraStatus(String datetime, String pan, String tilt, String moveStart, String moveEnd) {
        this.datetime = datetime;
        this.pan = pan;
        this.tilt = tilt;
        this.moveStart = moveStart;
        this.moveEnd = moveEnd;
    }

    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Column.camera_status_datetime, datetime);
        contentValues.put(Column.camera_status_pan, pan);
        contentValues.put(Column.camera_status_tilt, tilt);
        contentValues.put(Column.camera_status_move_start, moveStart);
        contentValues.put(Column.camera_status_move_end, moveEnd);
        return contentValues;
    }
}
