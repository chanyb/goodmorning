package kr.co.kworks.goodmorning.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Locale;

public class Database extends SQLiteOpenHelper {
    private static final String DB_NAME = "forest_vehicle.db";
    private static final int DB_VERSION = 1;

    public Database() {
        super(GlobalApplication.getContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createWiseTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

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

}
