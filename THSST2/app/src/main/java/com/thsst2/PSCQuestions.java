package com.thsst2;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Blob;
import java.util.ArrayList;

/**
 * Created by gyra on 02/10/2018.
 */
public class PSCQuestions extends AppCompatActivity {

    ImageView imgQuestion;
    TextView txtQuestion;

    DBHelper db;
    ArrayList<Bitmap> pscDrawings;
    ArrayList<String> pscQuestions;
    int questionCtr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_psc);

        this.initComponents();
    }

    public void initComponents() {
        this.imgQuestion = (ImageView) findViewById(R.id.imgQuestion);
        this.txtQuestion = (TextView) findViewById(R.id.txtQuestion);

        this.db = new DBHelper(this);
        this.pscDrawings = new ArrayList<Bitmap>();
        this.pscQuestions = new ArrayList<String>();
        this.questionCtr = 1;

        Cursor res = this.db.getAllPSCQuestions();
        if(res.getCount() == 0) {
            Toast.makeText(this, "Could not retrieve questions.", Toast.LENGTH_SHORT).show();
        }

        while (res.moveToNext()) {
            byte[] imgarray = res.getBlob(res.getColumnIndex("col_pscdraw_img"));
            pscDrawings.add(BitmapFactory.decodeByteArray(imgarray, 0, imgarray.length, null));
            pscQuestions.add(res.getString(res.getColumnIndex("col_question_tag")));
        }

        this.imgQuestion.setImageBitmap(pscDrawings.get(0));
        this.txtQuestion.setText(pscQuestions.get(0));
    }

    public void nextQuestion(View view) {
        if(this.questionCtr <= pscDrawings.size()) {
            this.imgQuestion.setImageBitmap(pscDrawings.get(this.questionCtr-1));
            this.txtQuestion.setText(pscQuestions.get(this.questionCtr-1));

            this.questionCtr++;
        }
    }
}
