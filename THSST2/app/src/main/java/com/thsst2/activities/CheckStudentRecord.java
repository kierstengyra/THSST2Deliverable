package com.thsst2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.thsst2.R;
import com.thsst2.processes.Field;
import com.thsst2.processes.FieldManager;
import com.thsst2.processes.Question;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CheckStudentRecord extends AppCompatActivity {

    ImageView imgBackgroundMenu;
    int school_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_screen);

        Intent intent = getIntent();
        this.school_id = intent.getIntExtra("SchoolID", -1);

        this.readCSV("Page1.csv");
        this.readCSV("Page2.csv");

        this.imgBackgroundMenu = (ImageView) findViewById(R.id.imgBackgroundMenu);
        this.imgBackgroundMenu.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    public void createRecord(View view) {
        Intent intent = new Intent(this, CreateStudentRecord.class);
        intent.putExtra("SchoolID", this.school_id);

        startActivity(intent);
    }

    public void selectRecord(View view) {
        Intent intent = new Intent(this, ChooseStudentRecord.class);
        intent.putExtra("SchoolID", this.school_id);

        startActivity(intent);
    }

    public void takePicture(View view) {

    }

    private void readCSV(String filename) {
        try {
            InputStreamReader is = new InputStreamReader(getAssets().open(filename));
            BufferedReader reader = new BufferedReader(is);
            BufferedReader reader2 = new BufferedReader(is);

            String line;
            String splitter = ",";
            int i = 0;

            while((line = reader.readLine()) != null) {
                String[] info = line.split(splitter);

                if((i+1)%3 == 0) {
                    Question q = new Question(Double.parseDouble(info[5]));
                    FieldManager.getInstance().addQuestion(q);
                }

                i++;
            }

            line = "";
            while((line = reader2.readLine()) != null) {
                String[] info = line.split(splitter);

                Field field = new Field(Double.parseDouble(info[1]),
                        Double.parseDouble(info[2]),
                        Double.parseDouble(info[3]),
                        Double.parseDouble(info[4]),
                        Integer.parseInt(info[5]),
                        Integer.parseInt(info[6]));

                FieldManager.getInstance().addField(field);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
}
