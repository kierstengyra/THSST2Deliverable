package com.thsst2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class WelcomeScreen extends AppCompatActivity {

    ImageView imgBackground;

    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        this.imgBackground = (ImageView) findViewById(R.id.imgBackground);
        this.imgBackground.setScaleType(ImageView.ScaleType.FIT_XY);

        this.dbHelper = new DBHelper(this);
    }
}
