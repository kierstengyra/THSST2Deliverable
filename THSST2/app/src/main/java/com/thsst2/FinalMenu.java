package com.thsst2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by gyra on 02/13/2018.
 */
public class FinalMenu extends AppCompatActivity {

    ImageView imgBackgroundMenu;
    Button btnSwitch;
    Button btnNextStudent;
    Button btnExit;

    int studentID;
    int schoolID;
    ArrayList<Integer> pscAnswers;
    String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_menu);

        this.initComponents();

        Intent intent = getIntent();
        this.studentID = intent.getIntExtra("StudentID", -1);
        this.schoolID = intent.getIntExtra("SchoolID", -1);
        this.mode = intent.getStringExtra("Mode");
        this.pscAnswers = intent.getIntegerArrayListExtra("PSCAnswers");
    }

    public void switchMode(View view) {

    }

    public void nextStudent(View view) {
        Intent intent = new Intent(this, CheckStudentRecord.class);
        intent.putExtra("SchoolID", this.schoolID);
        startActivity(intent);
    }

    public void exit(View view) {

    }

    public void initComponents() {
        this.imgBackgroundMenu = (ImageView) findViewById(R.id.imgBackgroundMenu);
        this.imgBackgroundMenu.setScaleType(ImageView.ScaleType.FIT_XY);

        this.btnSwitch = (Button) findViewById(R.id.btnSwitch);
        this.btnNextStudent = (Button) findViewById(R.id.btnNextStudent);
        this.btnExit = (Button) findViewById(R.id.btnExit);

        this.pscAnswers = new ArrayList<Integer>();
    }
}
