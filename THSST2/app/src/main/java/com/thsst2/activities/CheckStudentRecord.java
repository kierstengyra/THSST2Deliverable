package com.thsst2.activities;

/**
 * Type: Activity
 * CheckStudentRecord handles the screen for determining
 * the availability of the student record.
 * */

import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.thsst2.R;
import com.thsst2.processes.DBHelper;
import com.thsst2.processes.Field;
import com.thsst2.processes.FieldManager;
import com.thsst2.processes.FormManager;
import com.thsst2.processes.Question;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CheckStudentRecord extends AppCompatActivity {

    //Properties
    ImageView imgBackgroundMenu;
    int school_id;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_screen);

        Intent intent = getIntent();
        this.school_id = intent.getIntExtra("SchoolID", -1);

        this.dbHelper = new DBHelper(this);
        this.readQuestions();

        this.readCSV();

        this.imgBackgroundMenu = (ImageView) findViewById(R.id.imgBackgroundMenu);
        this.imgBackgroundMenu.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    //This method starts the CreateStudentRecord Activity.
    public void createRecord(View view) {
        Intent intent = new Intent(this, CreateStudentRecord.class);
        intent.putExtra("SchoolID", this.school_id);

        startActivity(intent);
    }

    //This method starts the ChooseStudentRecord Activity.
    public void selectRecord(View view) {
        Intent intent = new Intent(this, ChooseStudentRecord.class);
        intent.putExtra("SchoolID", this.school_id);

        startActivity(intent);
    }

    //TODO: Delete
    public void takePicture(View view) {

    }

    //This method retrieves the questions from the database
    //and creates an object for each.
    private void readQuestions() {
        Cursor result = this.dbHelper.getAllQuestions();

        if(result.getCount() == 0) {
            Log.e("CheckStudentRecord", "No questions found.");
        }
        else {
            while(result.moveToNext()) {
                int num = result.getInt(result.getColumnIndex("col_psc_id"));
                String question = result.getString(result.getColumnIndex("col_question_tag"));

                Question q = new Question(num, question);
                FormManager.getInstance().addQuestion(q);
            }
        }
    }

    //This method creates the Field objects.
    private void readCSV() {
        AssetManager assetManager = getAssets();
        String[] list;
        BufferedReader reader = null;

        try {
            list = assetManager.list("csv");
            String splitter = ",";

            if(list.length > 0) {
                for(String csvfile : list) {
                    InputStream is = assetManager.open("csv/"+csvfile);
                    InputStreamReader isr = new InputStreamReader(is);
                    String line = "";

                    if(csvfile.startsWith("Page") && csvfile.endsWith("csv")) {
                        FieldManager fm = new FieldManager();
                        reader = new BufferedReader(isr);

                        while((line = reader.readLine()) != null) {
                            String[] info = line.split(splitter);
                            Field field = new Field(Double.parseDouble(info[1]), Double.parseDouble(info[2]), Double.parseDouble(info[3]), Double.parseDouble(info[4]), Integer.parseInt(info[5]), Integer.parseInt(info[6]));
                            fm.addField(field);
                        }

                        FormManager.getInstance().addPage(fm);
                    }
                }
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        finally {
            if(reader != null) {
                try {
                    reader.close();
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
