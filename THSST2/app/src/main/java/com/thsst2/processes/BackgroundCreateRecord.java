package com.thsst2.processes;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.thsst2.activities.CreateStudentRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Type: Process
 * This class handles all transactions involving
 * the server (i.e. uploading, retrieving, etc.)
 */
public class BackgroundCreateRecord extends AsyncTask<String, String, String> {

    private Context ctx;
    private Connection conn1;

    BackgroundCreateRecord(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected String doInBackground(String... params) {
        int tmp = -1;
        String data ="";

        String firstName = params[0];
        String middleName = params[1];
        String lastName = params[2];
        String suffix = params[3];

        String sex = params[4];

        String month = params[5];
        String day = params[6];
        String year = params[7];

        String birthday = year+"-"+month+"-"+day;

        String age = params[8];

        String gradeLevel = params[9];
        String section = params[10];

        String school_id = "1";
        String section_id = "1";

        try {
            URL url = new URL("http://172.16.3.62/website2/create_student_record.php");
            String urlParams = "firstName="+firstName+"&middleName="+middleName+"&lastName="+lastName+"&suffix="+suffix+
                    "&sex="+sex+"&birthday="+birthday+"$age="+age+"&gradeLevel="+gradeLevel+"&section="+section+
                    "&school_id="+school_id+"$section_id="+section_id;

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Log.e("CreateRecord", "Open");
            conn.setDoOutput(true);
            Log.e("CreateRecord", "Connected");
            OutputStream os = conn.getOutputStream();
            os.write(urlParams.getBytes());
            Log.e("CreateRecord", "Write");
            os.flush();
            Log.e("CreateRecord", "Flushed");
            os.close();
            Log.e("CreateRecord", "Closed");
//            conn.connect();
//            InputStream is = conn.getInputStream();
//            Log.e("CreateRecord", "Read");
//
//            while((tmp=is.read()) != -1) {
//                data += (char) tmp;
//                Log.e("CreateRecord", data);
//            }
//
//            is.close();
//            Log.e("CreateRecord", "Closed2");
            conn.disconnect();
            Log.e("CreateRecord", "Disconnected");
        }
        catch(MalformedURLException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }

//        Log.e("CreateRecord", data);
        return data;
    }

    @Override
    protected void onPostExecute(String s) {
        String msg = "";
        if(s.equals(""))
            msg = "Data saved successfully";

        Toast.makeText(this.ctx, msg, Toast.LENGTH_LONG).show();

    }
}
