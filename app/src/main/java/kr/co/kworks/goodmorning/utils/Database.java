package kr.co.kworks.goodmorning.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Locale;

public class Database extends SQLiteOpenHelper {
    private static final String DB_NAME = "forest_vehicle.db";
    private static final int DB_VERSION = 2;

    public Database() {
        super(GlobalApplication.getContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createWiseTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            if (oldVersion < 2) { // 1
                createDeviceInfoTable(sqLiteDatabase);
            }
        }
    }

    private void createWiseTable(SQLiteDatabase db) {
        String sql = String.format(Locale.KOREA,"CREATE TABLE IF NOT EXISTS %s(" +
            "%s TEXT PRIMARY KEY," +
            "%s TEXT NOT NULL);",
            Column.wise,
            Column.wise_column_text,
            Column.wise_column_name
        );
        db.execSQL(sql);
    }

    private void createDeviceInfoTable(SQLiteDatabase db) {
        String sql = String.format(Locale.KOREA, "CREATE TABLE IF NOT EXISTS %s(" +
            "%s TEXT," +
            "%s TEXT);",
            Column.device_info,
            Column.device_info_column_fcm_token,
            Column.device_info_column_tel
        );

        db.execSQL(sql);
    }

    public Cursor selectCursor(String tableName, String[] columns, String columnForWhereClause, String[] valueForWhereClause, String groupBy, String having, String orderBy, String limit) {
        Cursor c = getReadableDatabase().query(
            tableName,
            columns/* null == all column*/,
            columnForWhereClause /* column for where clause*/,
            valueForWhereClause/* value for where clause*/,
            groupBy,
            having,
            orderBy,
            limit);
        return c;
    }

    // 직접 작성하여 delete 하는 함수
    public long delete(String tableName, String whereClause, String[] whereArgs) {
        Log.d("db", "delete: " + tableName + " / " + whereClause + " " + whereArgs.toString());
        return getWritableDatabase().delete(tableName, whereClause, whereArgs); // success: bigger than 0, fail: 0
    }

    // ContentValue로 insert하는 함수
    public long insert(String tableName, ContentValues contentValues) {
        Log.d("db", "insert: " + tableName + " / " + contentValues.toString());
        return getWritableDatabase().insert(tableName, null, contentValues); // 성공 시 0보다 큰 정수, 실패 시 -1 반환
    }

    // 직접 작성하여 update하는 함수
    public int update(String tableName, ContentValues contentValue, String whereCluase, String[] whereArgs) {
        Log.d("db", "update: " + tableName + " / " + contentValue.toString());
        return getWritableDatabase().update(tableName, contentValue, whereCluase, whereArgs);
    }

    public int getCountOfTable(String tableName){
        Cursor cursor= getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + tableName , null);
        cursor.moveToNext();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    /**
     * Save pushToken
     * @param token
     * @return success > 0, fail == -1
     */
    public long setPushToken(String token) {
        int count = getCountOfTable(Column.device_info);
        if (count == 0) { // 새로 추가
            ContentValues contentValues = new ContentValues();
            contentValues.put(Column.device_info_column_fcm_token, token);
            return insert(Column.device_info, contentValues);
        } else { // 수정
            try (Cursor cursor = selectCursor(Column.device_info, null, null, null, null, null, null, "1")) {
                if (cursor.moveToNext()) {
                    String telNum  = cursor.getString(cursor.getColumnIndexOrThrow(Column.device_info_column_tel));
                    ContentValues cv = new ContentValues();
                    cv.put(Column.device_info_column_tel, telNum);
                    cv.put(Column.device_info_column_fcm_token, token);
                    return insert(Column.device_info, cv);
                }
            }

        }
        return -1;
    }

    /**
     * Save telNum
     * @param telNum
     * @return success > 0, fail == -1
     */
    public long setTelNum(String telNum) {
        int count = getCountOfTable(Column.device_info);
        if (count == 0) { // 새로 추가
            ContentValues contentValues = new ContentValues();
            contentValues.put(Column.device_info_column_tel, telNum);
            return insert(Column.device_info, contentValues);
        } else { // 수정
            Cursor cursor = selectCursor(Column.device_info, null, null, null, null, null, null, "1");
            if (cursor.moveToNext()) {
                String token  = cursor.getString(cursor.getColumnIndexOrThrow(Column.device_info_column_fcm_token));
                ContentValues cv = new ContentValues();
                cv.put(Column.device_info_column_fcm_token, token);
                cv.put(Column.device_info_column_tel, telNum);
                return insert(Column.device_info, cv);
            }
        }
        return -1;
    }

    public String getTelNum() {
        Cursor cursor = selectCursor(Column.device_info, null, null, null, null, null, null, "1");
        if (cursor.moveToNext()) {
            return cursor.getString(cursor.getColumnIndexOrThrow(Column.device_info_column_tel));
        }
        return null;
    }

    public String getFcmToken() {
        Cursor cursor = selectCursor(Column.device_info, null, null, null, null, null, null, "1");
        if (cursor.moveToNext()) {
            return cursor.getString(cursor.getColumnIndexOrThrow(Column.device_info_column_fcm_token));
        }
        return null;
    }

}
