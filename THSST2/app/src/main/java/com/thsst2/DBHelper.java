package com.thsst2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by gyra on 01/28/2018.
 */
public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "wellbeingapp.db";

    public static final String TABLE_SESSION = "tbl_session";
    public static final String COL_SESSION_ID = "col_session_id";
    public static final String COL_SESSION_PASSWORD = "col_session_password";
    public static final String COL_SESSION_SCHEDULE = "col_session_schedule";
    public static final String COL_SESSION_IS_EXPIRED = "col_session_is_expired";

    public static final String TABLE_SCHOOL = "tbl_school";
    public static final String COL_SCHOOL_ID = "col_school_id";
    public static final String COL_SCHOOL_NAME = "col_school_name";
    public static final String COL_SCHOOL_ADDRESS = "col_school_address";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tbl_school
        db.execSQL("CREATE TABLE "+TABLE_SCHOOL+
                "("+COL_SCHOOL_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
                +COL_SCHOOL_NAME+" VARCHAR NOT NULL,"+
                COL_SCHOOL_ADDRESS+" TEXT);");

        // Create tbl_session
        db.execSQL("CREATE TABLE "+TABLE_SESSION+
                "("+COL_SESSION_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
                COL_SESSION_PASSWORD+" VARCHAR NOT NULL,"+
                COL_SESSION_SCHEDULE+" DATETIME NOT NULL,"+
                COL_SESSION_IS_EXPIRED+" BOOLEAN NOT NULL,"+
                COL_SCHOOL_ID+" INTEGER,"+
                "FOREIGN KEY("+COL_SCHOOL_ID+") REFERENCES "+TABLE_SCHOOL+"("+COL_SCHOOL_ID+"));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_SESSION);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_SCHOOL);
        onCreate(db);
    }
}
