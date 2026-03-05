package kr.co.kworks.goodmorning.model.repository;

import android.database.Cursor;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import kr.co.kworks.goodmorning.model.business_logic.Azimuth;
import kr.co.kworks.goodmorning.utils.CalendarHandler;
import kr.co.kworks.goodmorning.utils.Column;
import kr.co.kworks.goodmorning.utils.Database;

@Singleton
public class AzimuthRepository {
    private final Executor io = Executors.newSingleThreadExecutor();
    private CalendarHandler calendarHandler;
    private Database db;

    @Inject
    public AzimuthRepository() {
        calendarHandler = new CalendarHandler();
        db = new Database();
        removePastDataFromSqlite();
    }

    public boolean insertAzimuth(Azimuth azimuth) {
        return db.insert(Column.azimuth, azimuth.getContentValues()) > 0;
    }

    public boolean deleteAzimuth(Azimuth azimuth) {
        return db.delete(Column.azimuth, Column.azimuth_datetime+ "=?", new String[]{azimuth.datetime}) > 0;
    }

    public Azimuth selectProximateAzimuth(String datetime) {
        Cursor cursor = db.getReadableDatabase().rawQuery(String.format(Locale.KOREA, "SELECT * FROM %s ORDER BY ABS(CAST(%s AS INTEGER) - %s) LIMIT 1;", Column.azimuth, Column.azimuth_datetime, datetime), null);
        if (cursor.moveToNext()) {
            Azimuth azimuth = new Azimuth();
            azimuth.datetime = cursor.getString(cursor.getColumnIndexOrThrow(Column.azimuth_datetime));
            azimuth.heading = cursor.getString(cursor.getColumnIndexOrThrow(Column.azimuth_heading));
            return azimuth;
        }

        return null;
    }

    private void removePastDataFromSqlite() {
        Calendar before1hours = calendarHandler.getCalendarAfter(-1*60*60); // 한시간 전
        String datetimeString = calendarHandler.getDatetimeStringFromTimeMillis(before1hours.getTimeInMillis());
        db.delete(Column.azimuth, Column.azimuth_datetime+"<=?", new String[]{datetimeString});
    }

}
