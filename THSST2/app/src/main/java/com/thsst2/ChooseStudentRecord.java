package com.thsst2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by gyra on 02/04/2018.
 */
public class ChooseStudentRecord extends AppCompatActivity {

    DBHelper database;
    int schoolID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_student_record);

        this.initComponents();
    }

    private void initComponents() {
        Intent intent = getIntent();
        this.schoolID = intent.getIntExtra("SchoolID", 0);
    }
}
