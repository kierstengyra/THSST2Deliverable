package com.thsst2.activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.thsst2.processes.DBHelper;
import com.thsst2.R;
import com.thsst2.processes.DigitalFormManager;

import java.util.ArrayList;

/**
 * Type: Activity
 * PSCQuestions displays the Digital Form.
 * */

public class PSCQuestions extends AppCompatActivity {

    //Properties
    ImageView btnHindi;
    ImageView btnMinsan;
    ImageView btnMadalas;

    TextView txtHindi;
    TextView txtMinsan;
    TextView txtMadalas;

    TextView txtQuestion;
    TextView txtQuestionNo;

    DBHelper db;

    ImageView imgQuestion;
    RadioGroup radioChoices;
    ArrayList<Bitmap> pscDrawings;
    ArrayList<String> pscQuestions;
    ArrayList<Integer> pscAnswers;
    int questionCtr;
    int optionCtr;
    int selected;

    int studentID;
    int schoolID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_psc);

        Intent intent = getIntent();
        this.studentID = intent.getIntExtra("StudentID", -1);
        this.schoolID = intent.getIntExtra("SchoolID", -1);

        this.initComponents();
        Toast.makeText(this, "Student ID: "+this.studentID+" | School ID: "+this.schoolID, Toast.LENGTH_SHORT).show();
    }

    //This method initializes the properties.
    public void initComponents() {
        this.selected = 0;

        this.btnHindi = (ImageView) findViewById(R.id.btnHindi);
        this.btnMinsan = (ImageView) findViewById(R.id.btnMinsan);
        this.btnMadalas = (ImageView) findViewById(R.id.btnMadalas);

        this.txtHindi = (TextView) findViewById(R.id.txtHindi);
        this.txtMinsan = (TextView) findViewById(R.id.txtMinsan);
        this.txtMadalas = (TextView) findViewById(R.id.txtMadalas);

        this.txtQuestion = (TextView) findViewById(R.id.txtQuestion);
        this.txtQuestionNo = (TextView) findViewById(R.id.txtQuestionNo);

        this.db = new DBHelper(this);
        this.pscDrawings = new ArrayList<Bitmap>();
        this.pscQuestions = new ArrayList<String>();
        this.pscAnswers = new ArrayList<Integer>();
        this.questionCtr = 1;
        this.optionCtr = 3;

        this.pscQuestions = DigitalFormManager.getInstance().getPscQuestions();
        this.pscDrawings = DigitalFormManager.getInstance().getPscDrawings();

        this.btnHindi.setImageBitmap(pscDrawings.get(0));
        this.btnMinsan.setImageBitmap(pscDrawings.get(1));
        this.btnMinsan.setImageBitmap(pscDrawings.get(2));

        this.txtQuestion.setText(pscQuestions.get(0));
    }

    public void highlightAnswer(View view) {
        if(this.btnHindi.isPressed() || this.txtHindi.isPressed()) {
            this.txtHindi.setBackgroundColor(Color.rgb(23, 158, 154));
            this.txtMinsan.setBackgroundColor(Color.WHITE);
            this.txtMadalas.setBackgroundColor(Color.WHITE);
            this.selected = 1;
        }

        if(this.btnMinsan.isPressed() || this.txtMinsan.isPressed()) {
            this.txtHindi.setBackgroundColor(Color.WHITE);
            this.txtMinsan.setBackgroundColor(Color.rgb(23, 158, 154));
            this.txtMadalas.setBackgroundColor(Color.WHITE);
            this.selected = 2;
        }

        if(this.btnMadalas.isPressed() || this.txtMadalas.isPressed()) {
            this.txtHindi.setBackgroundColor(Color.WHITE);
            this.txtMinsan.setBackgroundColor(Color.WHITE);
            this.txtMadalas.setBackgroundColor(Color.rgb(23, 158, 154));
            this.selected = 3;
        }
    }

    //This method displays the next question.
    public void nextQuestion(View view) {
        if(this.selected != 0) {
            if(this.questionCtr <= pscQuestions.size()) {
                switch(selected) {
                    case 3: pscAnswers.add(3);
                        break;
                    case 2: pscAnswers.add(2);
                        break;
                    case 1: pscAnswers.add(1);
                        break;
                }

                if(this.questionCtr != pscQuestions.size() && this.optionCtr != pscDrawings.size()) {
                    this.btnHindi.setImageBitmap(pscDrawings.get(this.optionCtr));
                    this.btnMinsan.setImageBitmap(pscDrawings.get(this.optionCtr+1));
                    this.btnMadalas.setImageBitmap(pscDrawings.get(this.optionCtr+2));
                    this.txtQuestion.setText(pscQuestions.get(this.questionCtr));
                    this.optionCtr += 3;
                    this.clearAnswers();
                }

                this.questionCtr++;
                this.txtQuestionNo.setText(this.questionCtr+".");
            }
            else {
                String answers = "";
                int sum = 0;

                for(int i = 0; i < pscAnswers.size(); i++) {
                    sum += pscAnswers.get(i);

                    switch(pscAnswers.get(i)) {
                        case 3: answers += "Madalas";
                            break;
                        case 2: answers += "Minsan";
                            break;
                        case 1: answers += "Hindi";
                            break;
                    }

                    if(i < pscAnswers.size()-1)
                        answers += ",";
                }

                Log.e("PSCQuestions", "Answers: "+answers);

                boolean result = this.db.insertAssessment(answers, sum, this.studentID);
                if(result) {
                    this.selected = 0;
                    this.optionCtr = 3;
                    this.questionCtr = 1;

//                    this.pscQuestions.clear();
//                    this.pscDrawings.clear();
//                    this.pscAnswers.clear();

                    Intent intent = new Intent(this, FinalMenu.class);
                    intent.putExtra("StudentID", this.studentID);
                    intent.putExtra("SchoolID", this.schoolID);
                    intent.putExtra("PSCAnswers", this.pscAnswers);
                    intent.putExtra("Mode", "DAP");

                    startActivity(intent);
                }
                else
                    Toast.makeText(this, "Could not insert PSC Answers", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void clearAnswers() {
        this.txtHindi.setBackgroundColor(Color.WHITE);
        this.txtMinsan.setBackgroundColor(Color.WHITE);
        this.txtMadalas.setBackgroundColor(Color.WHITE);

        this.selected = 0;
    }

}
