package com.thsst2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Created by gyra on 02/09/2018.
 */
public class ChooseModule extends AppCompatActivity {
    ImageView background;
    Button btnPSC;
    Button btnDAP;

    int studentID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_module);

        Intent intent = getIntent();
        this.studentID = intent.getIntExtra("StudentID", 0);

        this.initComponents();
    }

    public void initComponents() {
        this.background = (ImageView) findViewById(R.id.imgBGModule);
        this.background.setScaleType(ImageView.ScaleType.FIT_XY);

        this.btnPSC = (Button) findViewById(R.id.btnPSC);
        this.btnDAP = (Button) findViewById(R.id.btnDAP);
    }

    public void loadPSC(View view) {
        Intent intent = new Intent(this, PSCQuestions.class);
        intent.putExtra("StudentID", this.studentID);
        startActivity(intent);
    }
}
