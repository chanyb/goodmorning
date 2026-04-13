package kr.co.kworks.goodmorning.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Locale;

import kr.co.kworks.goodmorning.model.business_logic.Unlock;

public class Database extends SQLiteOpenHelper {
    private static final String DB_NAME = "forest_vehicle.db";
    private static final int DB_VERSION = 5;

    public Database() {
        super(GlobalApplication.getContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createWiseTable(db);
        createDeviceInfoTable(db);
        createUser(db);
        createUnlock(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            if (oldVersion < 2) { // 1
                createDeviceInfoTable(sqLiteDatabase);
            }

            if (oldVersion < 3) {
                createUser(sqLiteDatabase);
            }

            if (oldVersion < 4) {
                createUnlock(sqLiteDatabase);
            }

            if (oldVersion < 5) {
                updateDeviceInfo(sqLiteDatabase);
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
            Column.device_info_app_token
        );

        db.execSQL(sql);
    }

    private void createUser(SQLiteDatabase db) {
        String sql = String.format(Locale.KOREA, "CREATE TABLE IF NOT EXISTS %s(" +
                "%s INTEGER DEFAULT 0);",
            Column.user,
            Column.user_is_login
        );

        db.execSQL(sql);
    }

    private void createUnlock(SQLiteDatabase db) {
        String sql = String.format(Locale.KOREA, "CREATE TABLE IF NOT EXISTS %s(" +
                "%s TEXT PRIMARY KEY," +
                "%s INTEGER DEFAULT 0);",
            Column.unlock,
            Column.unlock_datetime,
            Column.unlock_submit
        );

        db.execSQL(sql);
    }

    /**
     * DeviceInfo Table 의 Column 변경 device_info_column_tel -> device_info_app_token
     * DROP -> CREATE NEW TABLE
     * @param db
     */
    private void updateDeviceInfo(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            // 1. 새 테이블 생성
            db.execSQL(
                "CREATE TABLE device_info_new (" +
                    Column.device_info_column_fcm_token + " TEXT," +
                    Column.device_info_app_token + " TEXT" +
                    ");"
            );

            // 2. 데이터 복사
            db.execSQL(
                "INSERT INTO device_info_new (" +
                    Column.device_info_column_fcm_token + ", " + Column.device_info_column_fcm_token + ") " +
                    "SELECT " +
                    Column.device_info_column_fcm_token + ", " +
                    Column.device_info_column_tel +
                    " FROM " + Column.device_info
            );

            // 3. 기존 테이블 삭제
            db.execSQL("DROP TABLE " + Column.device_info);

            // 4. 이름 변경
            db.execSQL("ALTER TABLE device_info_new RENAME TO " + Column.device_info);

            db.setTransactionSuccessful();
        } catch(Exception e) {
            Logger.getInstance().error("upgradeDeviceInfoTable error", e);
        } finally {
            db.endTransaction();
        }
    }

    private void deleteDeviceInfo(SQLiteDatabase db) {
        db.execSQL("DROP TABLE " + Column.device_info);
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

    public long update(String tableName, ContentValues contentValues, ContentValues whereValues) {
        SQLiteDatabase db = getWritableDatabase();

        // WHERE 절 생성
        StringBuilder whereClause = new StringBuilder();
        String[] whereArgs = new String[whereValues.size()];

        int i = 0;
        for (String key : whereValues.keySet()) {
            if (i > 0) whereClause.append(" AND ");
            whereClause.append(key).append("=?");

            Object value = whereValues.get(key);
            whereArgs[i] = value == null ? null : String.valueOf(value);
            i++;
        }

        return db.update(tableName, contentValues, whereClause.toString(), whereArgs);
    }

    // 직접 작성하여 update하는 함수
    public int update(String tableName, ContentValues contentValue, String whereCluase, String[] whereArgs) {
        Log.d("db", "update: " + tableName + " / " + contentValue.toString());
        return getWritableDatabase().update(tableName, contentValue, whereCluase, whereArgs);
    }

    public int getCountOfTable(String tableName){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + tableName , null);
        int count = 0;
        if (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }

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
                    String appToken  = cursor.getString(cursor.getColumnIndexOrThrow(Column.device_info_app_token));
                    ContentValues cv = new ContentValues();
                    cv.put(Column.device_info_app_token, appToken);
                    cv.put(Column.device_info_column_fcm_token, token);

                    ContentValues whereCv = new ContentValues();
                    whereCv.put(Column.device_info_app_token, appToken);
                    return update(Column.device_info, cv, whereCv);
                }
            }

        }
        return -1;
    }

    /**
     * Save app token
     * @param appToken
     * @return success > 0, fail == -1
     */
    public long setAppToken(String appToken) {
        int count = getCountOfTable(Column.device_info);
        if (count == 0) { // 새로 추가
            ContentValues contentValues = new ContentValues();
            contentValues.put(Column.device_info_app_token, appToken);
            return insert(Column.device_info, contentValues);
        } else { // 수정
            try (Cursor cursor = selectCursor(Column.device_info, null, null, null, null, null, null, "1")) {
                if (cursor.moveToNext()) {
                    String token = cursor.getString(cursor.getColumnIndexOrThrow(Column.device_info_column_fcm_token));
                    ContentValues cv = new ContentValues();
                    cv.put(Column.device_info_column_fcm_token, token);
                    cv.put(Column.device_info_app_token, appToken);

                    ContentValues whereCv = new ContentValues();
                    whereCv.put(Column.device_info_column_fcm_token, token);
                    return update(Column.device_info, cv, whereCv);
                }
            }
        }
        return -1;
    }

    public String getAppToken() {
        try (Cursor cursor = selectCursor(Column.device_info, null, null, null, null, null, null, "1")) {
            if (cursor.moveToNext()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(Column.device_info_app_token));
            }
        }
        return null;
    }

    public String getFcmToken() {
        try (Cursor cursor = selectCursor(Column.device_info, null, null, null, null, null, null, "1")) {
            if (cursor.moveToNext()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(Column.device_info_column_fcm_token));
            }
        }
        return null;
    }

    public boolean isLogin() {
        try (Cursor cursor = selectCursor(Column.user, null, null, null, null, null, null, "1")) {
            if (cursor.moveToNext()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow(Column.user_is_login)) == 1;
            }
        }

        return false;
    }

    public void setLogin(boolean bool) {
        if (getCountOfTable(Column.user) == 0) {
            ContentValues cv = new ContentValues();
            cv.put(Column.user_is_login, bool ? 1:0);
            insert(Column.user, cv);
        } else {
            try (Cursor cursor = selectCursor(Column.user, null, null, null, null, null, null, "1")) {
                if (cursor.moveToNext()) {
                    int isLogin = cursor.getInt(cursor.getColumnIndexOrThrow(Column.user_is_login));

                    ContentValues cv = new ContentValues();
                    cv.put(Column.user_is_login, bool ? 1:0);

                    ContentValues whereCv = new ContentValues();
                    whereCv.put(Column.user_is_login, isLogin);
                    update(Column.user, cv, whereCv);
                }
            }
        }
    }

    public Unlock getUnlockInfo() {
        try (
            Cursor cursor = selectCursor(
            Column.unlock,
            null,
            Column.unlock_submit + "=?",
            new String[]{"0"},
            null,
            null,
            String.format(Locale.KOREA, "%s asc", Column.unlock_datetime),
            "1")
        ) {
            if (cursor.moveToNext()) {
                String datetime = cursor.getString(cursor.getColumnIndexOrThrow(Column.unlock_datetime));
                int submit = cursor.getInt(cursor.getColumnIndexOrThrow(Column.unlock_submit));

                Unlock unlock = new Unlock();
                unlock.datetime = datetime;
                unlock.submit = submit;

                return unlock;
            }
        }
        return null;
    }
}
