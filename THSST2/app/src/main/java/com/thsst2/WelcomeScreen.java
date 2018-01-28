package com.thsst2;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class WelcomeScreen extends AppCompatActivity {

    ImageView imgBackground;
    Button btnStart;
    EditText txtPassword;

    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        this.imgBackground = (ImageView) findViewById(R.id.imgBackground);
        this.imgBackground.setScaleType(ImageView.ScaleType.FIT_XY);

        this.btnStart = (Button) findViewById(R.id.btnStart);
        this.txtPassword = (EditText) findViewById(R.id.txtPassword);

        this.dbHelper = new DBHelper(this);
    }

    public void verifyPassword(View view) {
        Cursor result = this.dbHelper.confirmSession(this.txtPassword.getText().toString());
        if(result.getCount() == 0) {
            Toast.makeText(WelcomeScreen.this, "Invalid password.", Toast.LENGTH_SHORT).show();
        }
        else {
            StringBuffer buffer = new StringBuffer();
            while(result.moveToNext()) {
                buffer.append("School Name: "+result.getString(0));
            }

            Toast.makeText(WelcomeScreen.this, buffer, Toast.LENGTH_SHORT).show();
        }
    }
}
