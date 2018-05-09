package com.thsst2.activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.thsst2.processes.DBHelper;
import com.thsst2.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Type: Activity
 * CreateStudentRecord displays a form to be filled up
 * by the Test Administrator. The information will be saved
 * to the database.
 */
public class CreateStudentRecord extends AppCompatActivity implements OnItemSelectedListener {

    //Properties
    ArrayAdapter<String> monthAdapter;
    ArrayAdapter<Integer> dayAdapter;
    ArrayAdapter<Integer> yearAdapter;

    List<Integer> years;
    List<String> months;
    List<Integer> days;

    Spinner spinMonths;
    Spinner spinDays;
    Spinner spinYears;

    Calendar now;
    private int startYear;
    private int endYear;

    TextView txtEditCreate;
    EditText fname;
    EditText mname;
    EditText lname;
    EditText suffix;
    EditText age;
    EditText grade;
    EditText section;

    RadioGroup radioSexGroup;
    RadioButton radioSexButton;

    DBHelper database;

    int schoolID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_student_record);

        this.initComponents();
    }

    //This method creates the student record and uploads it to the database.
    public void onSubmit(View view) {
        String firstName = this.fname.getText().toString();
        String middleName = this.mname.getText().toString();
        String lastName = this.lname.getText().toString();
        String suffix = this.suffix.getText().toString();

        String rawAge = this.age.getText().toString();
        int year = Integer.parseInt(spinYears.getSelectedItem().toString());
        int day = Integer.parseInt(spinDays.getSelectedItem().toString());
        int month = spinMonths.getSelectedItemPosition()+1;

        int selectedId = this.radioSexGroup.getCheckedRadioButtonId();
        this.radioSexButton = (RadioButton) findViewById(selectedId);
        char sex = (radioSexButton.getText().toString().equals("Male") ? 'M' : 'F');

        String rawGrade = this.grade.getText().toString();
        String sec = this.section.getText().toString();

        if(isValid(firstName, lastName, rawAge, rawGrade, sec)) {
            int age = Integer.parseInt(rawAge);
            int gradeLevel = Integer.parseInt(rawGrade);

            boolean isInserted = this.database.insertStudentRecord(firstName, middleName, lastName, suffix, age, sex, year, day, month, gradeLevel, sec, this.schoolID);

            if(isInserted)
                Toast.makeText(this, "Student record added.", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Could not create record.", Toast.LENGTH_SHORT).show();

            int studentID = 0;
            Cursor res = this.database.getStudentID(firstName, middleName, lastName, suffix, age, this.schoolID);
            if(res.getCount() == 0 || res.getCount() > 1)
                Toast.makeText(this, "Student not found.", Toast.LENGTH_SHORT).show();
            else {
                while(res.moveToNext()) {
                    studentID = res.getInt(res.getColumnIndex("col_student_id"));
                }
            }

            Intent intent = new Intent(this, ChooseModule.class);
            intent.putExtra("SchoolID", this.schoolID);
            intent.putExtra("StudentID", studentID);

            startActivity(intent);
        }
        else {
            this.txtEditCreate.setBackgroundColor(Color.rgb(238,99,99));
            this.txtEditCreate.setText("Fill up the form completely.");
        }
    }

    private boolean isValid(String firstName, String lastName, String age, String gradeLevel, String sec) {
        boolean valid = true;

        if(firstName.equals("") || lastName.equals("") || age.equals("") || gradeLevel.equals("") || sec.equals(""))
            valid = false;

        return valid;

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();

        Toast.makeText(parent.getContext(), "Selected: "+item, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    //This method initializes the properties.
    private void initComponents() {
        // Get school ID from previous activity
        Intent intent = getIntent();
        this.schoolID = intent.getIntExtra("SchoolID", -1);

        // Instantiate DBHelper
        this.database = new DBHelper(this);

        // Instantiate spinners
        this.spinMonths = (Spinner) findViewById(R.id.spinMonth);
        this.spinDays = (Spinner) findViewById(R.id.spinDay);
        this.spinYears = (Spinner) findViewById(R.id.spinYear);

        // Instantiate spinMonths dataset
        this.months = new ArrayList<String>();
        this.months.add("January");
        this.months.add("February");
        this.months.add("March");
        this.months.add("April");
        this.months.add("May");
        this.months.add("June");
        this.months.add("July");
        this.months.add("August");
        this.months.add("September");
        this.months.add("October");
        this.months.add("November");
        this.months.add("December");

        // Instantiate spinDays dataset
        this.days = new ArrayList<Integer>();
        for (int i = 1; i <= 31; i++)
            this.days.add(i);

        // Instantiate spinYears dataset
        this.now = Calendar.getInstance();
        this.endYear = now.get(Calendar.YEAR);
        this.startYear = this.endYear - 100;
        this.years = new ArrayList<Integer>();
        for (int i = this.endYear; i >= this.startYear; i--)
            this.years.add(i);

        // Get form elements
        this.fname = (EditText) findViewById(R.id.txtFirstName);
        this.mname = (EditText) findViewById(R.id.txtMiddleName);
        this.lname = (EditText) findViewById(R.id.txtLastName);
        this.suffix = (EditText) findViewById(R.id.txtSuffix);
        this.age = (EditText) findViewById(R.id.txtAge);
        this.radioSexGroup = (RadioGroup) findViewById(R.id.radioSex);
        this.grade = (EditText) findViewById(R.id.txtGrade);
        this.section = (EditText) findViewById(R.id.txtSection);
        this.txtEditCreate = (TextView) findViewById(R.id.txtHeadingCreate);

        // Fill spinners with data
        monthAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinMonths.setAdapter(monthAdapter);

        dayAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinDays.setAdapter(dayAdapter);

        yearAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinYears.setAdapter(yearAdapter);
    }
}
