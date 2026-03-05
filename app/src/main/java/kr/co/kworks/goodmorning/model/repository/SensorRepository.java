package kr.co.kworks.goodmorning.model.repository;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import kr.co.kworks.goodmorning.model.business_logic.Cr350;
import kr.co.kworks.goodmorning.model.network.NetworkModule;
import kr.co.kworks.goodmorning.model.network.SensorRequestInterface;
import kr.co.kworks.goodmorning.model.response.SensorQueryResponse;
import kr.co.kworks.goodmorning.utils.CalendarHandler;
import kr.co.kworks.goodmorning.utils.Column;
import kr.co.kworks.goodmorning.utils.Database;
import kr.co.kworks.goodmorning.utils.Logger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class SensorRepository {
    private final Executor io = Executors.newSingleThreadExecutor();
    private SensorRequestInterface api;
    private CalendarHandler calendarHandler;
    private Database db;

    @Inject
    public SensorRepository(@NetworkModule.Sensor SensorRequestInterface sensorRequestInterface) {
        api = sensorRequestInterface;
        calendarHandler = new CalendarHandler();
        db = new Database();
        removePastDataFromSqlite();
    }

    public void getSensorDataFromServer() {
        Call<SensorQueryResponse> test = api.getSensorData();
        test.enqueue(new Callback<SensorQueryResponse>() {
            @Override
            public void onResponse(Call<SensorQueryResponse> call, Response<SensorQueryResponse> response) {
                if (response.isSuccessful()) {
                    try {
                        List<Float> values = response.body().getData().get(0).getVals();
                        ContentValues cv = new ContentValues();
                        String timestamp = response.body().getData().get(0).getTime(); // 2025-11-11T13:58:50
                        String datetime = String.format(Locale.KOREA, "%s%s%s%s%s%s", timestamp.substring(0, 4), timestamp.substring(5, 7), timestamp.substring(8, 10), timestamp.substring(11, 13), timestamp.substring(14,16), timestamp.substring(17,19));
                        cv.put(Column.sensor_datetime, datetime);
                        cv.put(Column.sensor_airtemp_avg, String.valueOf(values.get(0)));
                        cv.put(Column.sensor_airtemp_in_runavg, String.valueOf(values.get(1)));
                        cv.put(Column.sensor_ws_runavg, String.valueOf(values.get(2)));
                        cv.put(Column.sensor_wd_runavg, String.valueOf(values.get(3)));
                        cv.put(Column.sensor_wd_gust, String.valueOf(values.get(4)));
                        cv.put(Column.sensor_ws_gust, String.valueOf(values.get(5)));
                        cv.put(Column.sensor_air_pressure_avg, String.valueOf(values.get(6)));
                        cv.put(Column.sensor_rh_avg, String.valueOf(values.get(7)));
                        cv.put(Column.sensor_rh_runavg, String.valueOf(values.get(8)));
                        cv.put(Column.sensor_batt_volt_min, String.valueOf(values.get(9)));
                        db.insert(Column.sensor, cv);
                    } catch (Exception e) {
                        Logger.getInstance().error("SensorRepository-getSensorData-onResponse", e);
                    }

                }
            }

            @Override
            public void onFailure(Call<SensorQueryResponse> call, Throwable throwable) {
                Logger.getInstance().error("SensorRepository-getSensorData-onFailure", throwable);
            }
        });
    }

    public Cr350 getRecentSensorDataFromDB() {
        Cursor cursor = db.selectCursor(Column.sensor, Column.sensor_column_list, null, null, null, null,
            String.format(Locale.KOREA, "%s desc", Column.sensor_datetime), "1");

        if(cursor.moveToNext()) {
            Cr350 cr350 = new Cr350();
            cr350.datetime = cursor.getString(cursor.getColumnIndexOrThrow(Column.sensor_datetime));
            cr350.airtempAvg = cursor.getString(cursor.getColumnIndexOrThrow(Column.sensor_airtemp_avg));
            cr350.airtempInRunAvg = cursor.getString(cursor.getColumnIndexOrThrow(Column.sensor_airtemp_in_runavg));
            cr350.wsRunAvg = cursor.getString(cursor.getColumnIndexOrThrow(Column.sensor_ws_runavg));
            cr350.wdRunAvg = cursor.getString(cursor.getColumnIndexOrThrow(Column.sensor_wd_runavg));
            cr350.wdGust = cursor.getString(cursor.getColumnIndexOrThrow(Column.sensor_wd_gust));
            cr350.wsGust = cursor.getString(cursor.getColumnIndexOrThrow(Column.sensor_ws_gust));
            cr350.airPressureAvg = cursor.getString(cursor.getColumnIndexOrThrow(Column.sensor_air_pressure_avg));
            cr350.rhAvg = cursor.getString(cursor.getColumnIndexOrThrow(Column.sensor_rh_avg));
            cr350.rhRunAvg = cursor.getString(cursor.getColumnIndexOrThrow(Column.sensor_rh_runavg));
            cr350.battVoltMin = cursor.getString(cursor.getColumnIndexOrThrow(Column.sensor_batt_volt_min));
            return cr350;
        }


        return null;
    }

    private void removePastDataFromSqlite() {
        Calendar before10minutes = calendarHandler.getCalendarAfter(-1*60*10); // 10분 전
        String datetimeString = calendarHandler.getDatetimeStringFromTimeMillis(before10minutes.getTimeInMillis());
        db.delete(Column.sensor, Column.sensor_datetime+"<=?", new String[]{datetimeString});
    }

}
