package com.thsst2;

//import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tbl_school:
        db.execSQL("CREATE TABLE "+TABLE_SCHOOL+
                "("+COL_SCHOOL_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
                +COL_SCHOOL_NAME+" VARCHAR NOT NULL,"+
                COL_SCHOOL_ADDRESS+" TEXT);");

        // Insert values for tbl_school:
        db.execSQL("INSERT INTO "+TABLE_SCHOOL+"("+COL_SCHOOL_NAME+", "+COL_SCHOOL_ADDRESS+")"+
                " VALUES ('De La Salle University - Laguna Campus', 'Brgy. Malamig, Binan, Laguna')," +
                "('Inchican Elementary School', 'Brgy. Inchican, Sta. Rosa, Laguna')," +
                "('Mary Help of Christians College', 'Brgy. Canlubang, Calamba City, Laguna')," +
                "('Don Bosco Canlubang', 'Brgy. Canlubang, Calamba City, Laguna')," +
                "('Don Bosco Children''s Center', 'Brgy. Mayapa, Calamba City, Laguna');");

        // Create tbl_session:
        db.execSQL("CREATE TABLE "+TABLE_SESSION+
                "("+COL_SESSION_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
                COL_SESSION_PASSWORD+" VARCHAR NOT NULL,"+
                COL_SESSION_SCHEDULE+" DATETIME NOT NULL,"+
                COL_SESSION_IS_EXPIRED+" BOOLEAN NOT NULL,"+
                COL_SCHOOL_ID+" INTEGER,"+
                "FOREIGN KEY("+COL_SCHOOL_ID+") REFERENCES "+TABLE_SCHOOL+"("+COL_SCHOOL_ID+"));");

        // Insert values for tbl_session:
        db.execSQL("INSERT INTO " + TABLE_SESSION + "(" + COL_SESSION_PASSWORD + ", " + COL_SESSION_SCHEDULE + "," +
                COL_SESSION_IS_EXPIRED + "," + COL_SCHOOL_ID + ")" +
                " VALUES ('fvM41]0:', '2016-02-29 09:00', 0, 3)," +
                "('8+kA_7%~', '2014-10-17 07:30', 0, 1)," +
                "('0v(SVFyG', '2018-01-11 08:15', 0, 5);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_SESSION);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_SCHOOL);
        onCreate(db);
    }

    public Cursor confirmSession(String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT col_school_name FROM "+TABLE_SCHOOL+" as School, "+TABLE_SESSION+" as Session "+
                "WHERE School.col_school_id = Session.col_school_id AND Session.col_session_password = '"+password+"'", null);

        return result;
    }

//    public boolean insertSchoolData(String school_name, String school_address) {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(COL_SCHOOL_NAME, school_name);
//        contentValues.put(COL_SCHOOL_ADDRESS, school_address);
//
//        long result = db.insert(TABLE_SCHOOL, null, contentValues);
//
//        if(result == -1)
//            return false;
//        else
//            return true;
//    }
}
