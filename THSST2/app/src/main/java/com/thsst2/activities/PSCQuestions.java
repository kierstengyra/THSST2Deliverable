package com.thsst2.activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.thsst2.processes.DBHelper;
import com.thsst2.R;
import com.thsst2.processes.DigitalFormManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Type: Activity
 * PSCQuestions displays the Digital Form.
 * */

public class PSCQuestions extends AppCompatActivity {

    //Properties
    ImageView btnHindi;
    ImageView btnMinsan;
    ImageView btnMadalas;
    Button btnSkip;

    TextView txtHindi;
    TextView txtMinsan;
    TextView txtMadalas;

    EditText txtRemarks;

    TextView txtQuestion;
    TextView txtQuestionNo;

    Button prevQuestion;
    Button nextQuestion;
    Button confirmTnx;

    DBHelper db;

    ImageView imgQuestion;
    RadioGroup radioChoices;
    ArrayList<Bitmap> pscDrawings;
    ArrayList<String> pscQuestions;
    ArrayList<Integer> pscAnswers;
    ArrayList<String> pscAnswersStr;
    ArrayList<String> pscRemarks;

    int questionCtr;
    int optionCtr;
    int selected;
    boolean isSelected;

    String studentName;
    String studentLastName;
    String schoolName;

    int studentID;
    int schoolID;

    boolean isBackPressed;
    boolean isChangedAnswer;
    boolean isChangedRemark;

    int prevAnswer;
    String prevRemark;

    int newAnswer;
    String newRemark;

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
        this.isSelected = false;
        this.isBackPressed = false;
        this.isChangedAnswer = false;
        this.isChangedRemark = false;

        this.prevAnswer = 0;
        this.prevRemark = "";
        this.newAnswer = 0;
        this.newRemark = "";

        this.btnHindi = (ImageView) findViewById(R.id.btnHindi);
        this.btnMinsan = (ImageView) findViewById(R.id.btnMinsan);
        this.btnMadalas = (ImageView) findViewById(R.id.btnMadalas);
        this.btnSkip = (Button) findViewById(R.id.btnSkip);

        this.txtHindi = (TextView) findViewById(R.id.txtHindi);
        this.txtMinsan = (TextView) findViewById(R.id.txtMinsan);
        this.txtMadalas = (TextView) findViewById(R.id.txtMadalas);

        this.txtRemarks = (EditText) findViewById(R.id.txtRemarks);

        this.txtQuestion = (TextView) findViewById(R.id.txtQuestion);
        this.txtQuestionNo = (TextView) findViewById(R.id.txtQuestionNo);

        this.prevQuestion = (Button) findViewById(R.id.btnPrevQ);
        this.nextQuestion = (Button) findViewById(R.id.btnNext);
        this.confirmTnx = (Button) findViewById(R.id.btnTnxOK);

        this.db = new DBHelper(this);
        this.getQuestionsAndDrawings();
        this.studentName = this.db.getStudentName(studentID);
        this.schoolName = this.db.getSchoolName(this.schoolID);
        this.studentLastName = this.db.getStudentLastName(studentID);

        this.pscDrawings = new ArrayList<Bitmap>();
        this.pscQuestions = new ArrayList<String>();
        this.pscAnswers = new ArrayList<Integer>();
        this.pscAnswersStr = new ArrayList<String>();
        this.pscRemarks = new ArrayList<String>();

        this.questionCtr = 0;
        this.optionCtr = 3;

        this.pscQuestions = DigitalFormManager.getInstance().getPscQuestions();
        this.pscDrawings = DigitalFormManager.getInstance().getPscDrawings();

        for(int i = 0; i < this.pscQuestions.size(); i++) {
            this.pscAnswers.add(-2);
            this.pscAnswersStr.add("");
            this.pscRemarks.add("");
        }

        this.txtRemarks.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                pscRemarks.set(questionCtr, editable.toString());
            }
        });

        this.btnHindi.setImageBitmap(pscDrawings.get(0));
        this.btnMinsan.setImageBitmap(pscDrawings.get(1));
        this.btnMinsan.setImageBitmap(pscDrawings.get(2));

        this.txtQuestion.setText(pscQuestions.get(0));
    }

    public void highlightAnswer(View view) {
        this.isSelected = true;

        if(this.btnSkip.isPressed()) {
            this.txtHindi.setBackgroundColor(Color.WHITE);
            this.txtMinsan.setBackgroundColor(Color.WHITE);
            this.txtMadalas.setBackgroundColor(Color.WHITE);
            this.btnSkip.setBackgroundColor(Color.rgb(23, 158, 154));
            this.btnSkip.setTextColor(Color.WHITE);

            this.pscAnswers.set(this.questionCtr, -1);
            this.pscAnswersStr.set(this.questionCtr, "Hindi ko masasagot");
        }

        if(this.btnHindi.isPressed() || this.txtHindi.isPressed()) {
            this.txtHindi.setBackgroundColor(Color.rgb(23, 158, 154));
            this.txtMinsan.setBackgroundColor(Color.WHITE);
            this.txtMadalas.setBackgroundColor(Color.WHITE);
            this.btnSkip.setBackgroundColor(Color.rgb(204, 204, 204));
            this.btnSkip.setTextColor(Color.rgb(23, 158, 154));

            this.pscAnswers.set(this.questionCtr, 0);
            this.pscAnswersStr.set(this.questionCtr, "Hindi nangyayari");
        }

        if(this.btnMinsan.isPressed() || this.txtMinsan.isPressed()) {
            this.txtHindi.setBackgroundColor(Color.WHITE);
            this.txtMinsan.setBackgroundColor(Color.rgb(23, 158, 154));
            this.txtMadalas.setBackgroundColor(Color.WHITE);
            this.btnSkip.setBackgroundColor(Color.rgb(204, 204, 204));
            this.btnSkip.setTextColor(Color.rgb(23, 158, 154));

            this.pscAnswers.set(this.questionCtr, 1);
            this.pscAnswersStr.set(this.questionCtr, "Minsan nangyayari");
        }

        if(this.btnMadalas.isPressed() || this.txtMadalas.isPressed()) {
            this.txtHindi.setBackgroundColor(Color.WHITE);
            this.txtMinsan.setBackgroundColor(Color.WHITE);
            this.txtMadalas.setBackgroundColor(Color.rgb(23, 158, 154));
            this.btnSkip.setBackgroundColor(Color.rgb(204, 204, 204));
            this.btnSkip.setTextColor(Color.rgb(23, 158, 154));

            this.pscAnswers.set(this.questionCtr, 2);
            this.pscAnswersStr.set(this.questionCtr, "Madalas nangyayari");
        }
    }

    public void getQuestionsAndDrawings() {
        Cursor questions = this.db.getAllQuestions();
        Cursor drawings = this.db.getAllPSCOptionDrawings();

        if(questions.getCount() == 0 || drawings.getCount() == 0)
            Toast.makeText(this, "Could not retrieve questions or drawings.", Toast.LENGTH_SHORT).show();
        else {
            while(questions.moveToNext())
                DigitalFormManager.getInstance().addQuestion(questions.getString(questions.getColumnIndex("col_question_tag")));

            while(drawings.moveToNext()) {
                byte[] imgarray = drawings.getBlob(drawings.getColumnIndex("col_pscoptions_img"));
                DigitalFormManager.getInstance().addDrawing(BitmapFactory.decodeByteArray(imgarray, 0, imgarray.length, null));
            }
        }
    }

    public void prevQuestion(View view) {
        this.isSelected = true;

        this.questionCtr--;
        this.optionCtr -= 3;

        if(this.questionCtr == 0)
            this.prevQuestion.setVisibility(View.INVISIBLE);

        this.btnHindi.setImageBitmap(this.pscDrawings.get(this.optionCtr));
        this.btnMinsan.setImageBitmap(this.pscDrawings.get(this.optionCtr+1));
        this.btnMadalas.setImageBitmap(this.pscDrawings.get(this.optionCtr+2));
        this.txtQuestion.setText(this.pscQuestions.get(this.questionCtr));

        this.txtRemarks.setText(this.pscRemarks.get(this.questionCtr));
        int prevAnswer = this.pscAnswers.get(this.questionCtr);

            switch(prevAnswer) {
            case 0: this.txtHindi.setBackgroundColor(Color.rgb(23, 158, 154));
                    this.txtMinsan.setBackgroundColor(Color.WHITE);
                    this.txtMadalas.setBackgroundColor(Color.WHITE);
                    this.btnSkip.setBackgroundColor(Color.rgb(204, 204, 204));
                    this.btnSkip.setTextColor(Color.rgb(23, 158, 154));
                    break;
            case 1: this.txtHindi.setBackgroundColor(Color.WHITE);
                    this.txtMinsan.setBackgroundColor(Color.rgb(23, 158, 154));
                    this.txtMadalas.setBackgroundColor(Color.WHITE);
                    this.btnSkip.setBackgroundColor(Color.rgb(204, 204, 204));
                    this.btnSkip.setTextColor(Color.rgb(23, 158, 154));
                    break;
            case 2: this.txtHindi.setBackgroundColor(Color.WHITE);
                    this.txtMinsan.setBackgroundColor(Color.WHITE);
                    this.txtMadalas.setBackgroundColor(Color.rgb(23, 158, 154));
                    this.btnSkip.setBackgroundColor(Color.rgb(204, 204, 204));
                    this.btnSkip.setTextColor(Color.rgb(23, 158, 154));
                    break;
            case -1: this.txtHindi.setBackgroundColor(Color.WHITE);
                    this.txtMinsan.setBackgroundColor(Color.WHITE);
                    this.txtMadalas.setBackgroundColor(Color.WHITE);
                    this.btnSkip.setBackgroundColor(Color.rgb(23, 158, 154));
                    this.btnSkip.setTextColor(Color.WHITE);
                    break;
            default: break;
        }

        this.txtQuestionNo.setText((this.questionCtr+1)+".");
    }

    //This method displays the next question.
    public void nextQuestion(View view) {
        if(this.isSelected) {
            if(this.questionCtr+1 < pscQuestions.size()) {
                if(this.optionCtr < pscDrawings.size()) {
                    this.prevQuestion.setVisibility(View.VISIBLE);
                    this.questionCtr++;

                    this.txtQuestionNo.setText((this.questionCtr+1)+".");

                    this.btnHindi.setImageBitmap(this.pscDrawings.get(this.optionCtr));
                    this.btnMinsan.setImageBitmap(this.pscDrawings.get(this.optionCtr+1));
                    this.btnMadalas.setImageBitmap(this.pscDrawings.get(this.optionCtr+2));
                    this.txtQuestion.setText(this.pscQuestions.get(this.questionCtr));
                    this.optionCtr += 3;

                    if(this.pscAnswers.get(this.questionCtr) > -2) {
                        int answer = this.pscAnswers.get(this.questionCtr);

                        switch(answer) {
                            case 0: this.txtHindi.setBackgroundColor(Color.rgb(23, 158, 154));
                                this.txtMinsan.setBackgroundColor(Color.WHITE);
                                this.txtMadalas.setBackgroundColor(Color.WHITE);
                                this.btnSkip.setBackgroundColor(Color.rgb(204, 204, 204));
                                this.btnSkip.setTextColor(Color.rgb(23, 158, 154));
                                break;
                            case 1: this.txtHindi.setBackgroundColor(Color.WHITE);
                                this.txtMinsan.setBackgroundColor(Color.rgb(23, 158, 154));
                                this.txtMadalas.setBackgroundColor(Color.WHITE);
                                this.btnSkip.setBackgroundColor(Color.rgb(204, 204, 204));
                                this.btnSkip.setTextColor(Color.rgb(23, 158, 154));
                                break;
                            case 2: this.txtHindi.setBackgroundColor(Color.WHITE);
                                this.txtMinsan.setBackgroundColor(Color.WHITE);
                                this.txtMadalas.setBackgroundColor(Color.rgb(23, 158, 154));
                                this.btnSkip.setBackgroundColor(Color.rgb(204, 204, 204));
                                this.btnSkip.setTextColor(Color.rgb(23, 158, 154));
                                break;
                            case -1: this.txtHindi.setBackgroundColor(Color.WHITE);
                                this.txtMinsan.setBackgroundColor(Color.WHITE);
                                this.txtMadalas.setBackgroundColor(Color.WHITE);
                                this.btnSkip.setBackgroundColor(Color.rgb(23, 158, 154));
                                this.btnSkip.setTextColor(Color.WHITE);
                                break;
                            default: break;
                        }

                        this.txtRemarks.setText(this.pscRemarks.get(this.questionCtr));
                    }
                    else {
                        this.isSelected = false;
                        this.txtRemarks.setText("");
                        this.txtRemarks.setHint("Maglagay ng paliwanag");
                        this.clearAnswers();
                    }
                }
            }
            else {
                    String answers = "";
                    int sum = 0;

                    for(int i = 0; i < pscAnswers.size(); i++) {
                        if(pscAnswers.get(i) > -1)
                            sum += pscAnswers.get(i);

                        switch(pscAnswers.get(i)) {
                            case 2: answers += "Madalas";
                                break;
                            case 1: answers += "Minsan";
                                break;
                            case 0: answers += "Hindi";
                                break;
                            case -1: answers += "Hindi masagot";
                                break;
                            default: break;
                        }

                        if(i < pscAnswers.size()-1)
                            answers += ",";
                    }

                    boolean result = this.db.insertAssessment(answers, sum, this.studentID);
                    if(result) {
                        this.selected = 0;
                        this.optionCtr = 3;
                        this.questionCtr = 1;

                        this.txtQuestionNo.setText("");
                        this.btnHindi.setVisibility(View.GONE);
                        this.btnMinsan.setVisibility(View.GONE);
                        this.btnMadalas.setVisibility(View.GONE);
                        this.btnSkip.setVisibility(View.GONE);

                        this.txtHindi.setVisibility(View.GONE);
                        this.txtMinsan.setVisibility(View.GONE);
                        this.txtMadalas.setVisibility(View.GONE);
                        this.txtRemarks.setVisibility(View.GONE);

                        this.prevQuestion.setVisibility(View.GONE);
                        this.nextQuestion.setVisibility(View.GONE);

                        this.confirmTnx.setVisibility(View.VISIBLE);

                        this.txtQuestion.setText("Maraming salamat sa pagsagot! Kung mayroon kayong mga katanungan,"+
                                "mangyari lamang na lumapit sa guro ng inyong anak o sa guidance counselor ng eskwelahan.");
                    }
                    else
                        Toast.makeText(this, "Could not insert PSC Answers", Toast.LENGTH_SHORT).show();
                }
            }
    }

    public void okTnx(View view) {
        Intent intent = new Intent(this, FinalMenu.class);
        intent.putExtra("StudentID", this.studentID);
        intent.putExtra("SchoolID", this.schoolID);
        intent.putExtra("PSCAnswers", this.pscAnswers);
        intent.putExtra("Mode", "DAP");

        startActivity(intent);
    }

    public void createFile() {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Results/"+this.schoolName);
            if(!root.exists()) {
                root.mkdirs();
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File resultsFile = new File(root, this.studentLastName+"_DIG_"+timeStamp+".txt");
            FileWriter writer = new FileWriter(resultsFile);

            writer.append(this.studentName+"\n\n");

            for(int i = 0; i < pscAnswersStr.size(); i++) {
                writer.append(this.pscQuestions.get(i)+"\n");
                writer.append(this.pscAnswersStr.get(i)+"\n\n");
            }

            writer.flush();
            writer.close();
        }
        catch(IOException e) {
           e.printStackTrace();
        }
    }

    public void clearAnswers() {
        this.txtHindi.setBackgroundColor(Color.WHITE);
        this.txtMinsan.setBackgroundColor(Color.WHITE);
        this.txtMadalas.setBackgroundColor(Color.WHITE);
        this.btnSkip.setBackgroundColor(Color.rgb(204, 204, 204));
        this.btnSkip.setTextColor(Color.rgb(23, 158, 154));

        this.selected = 0;
    }

}
