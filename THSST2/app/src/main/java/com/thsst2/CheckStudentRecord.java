package com.thsst2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by gyra on 01/28/2018.
 */
public class CheckStudentRecord extends AppCompatActivity {

    ImageView imgBackgroundMenu;
    int school_id;

    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_screen);

        Intent intent = getIntent();
        this.school_id = intent.getIntExtra("SchoolID", 0);

        Toast.makeText(this, "School ID: "+this.school_id, Toast.LENGTH_SHORT).show();

        this.imgBackgroundMenu = (ImageView) findViewById(R.id.imgBackgroundMenu);
        this.imgBackgroundMenu.setScaleType(ImageView.ScaleType.FIT_XY);
    }
}
