package com.thsst2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
//import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by gyra on 01/28/2018.
 */
public class CreateStudentRecord extends AppCompatActivity implements OnItemSelectedListener {
    public static final String DATABASE_NAME = "wellbeingapp.db";
//    ImageView imgBackgroundForm;

    ArrayAdapter<String> monthAdapter;
    List<String> months;
    Spinner spinMonths;

    ArrayAdapter<Integer> dayAdapter;
    List<Integer> days;
    Spinner spinDays;

    ArrayAdapter<Integer> yearAdapter;
    List<Integer> years;
    Spinner spinYears;
    Calendar now;
    private int startYear;
    private int endYear;

    EditText fname;
    EditText mname;
    EditText lname;
    EditText suffix;
    EditText age;
    RadioGroup radioSexGroup;
    RadioButton radioSexButton;

    DBHelper database;
    int schoolID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_student_record);

        Intent intent = getIntent();
        this.schoolID = intent.getIntExtra("SchoolID", 0);

        this.database = new DBHelper(this);
//        this.database.

        this.initComponents();

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

    private void initComponents() {
//        this.imgBackgroundForm = (ImageView) findViewById(R.id.imgBackgroundForm);
//        this.imgBackgroundForm.setScaleType(ImageView.ScaleType.FIT_XY);

        this.spinMonths = (Spinner) findViewById(R.id.spinMonth);
        this.spinDays = (Spinner) findViewById(R.id.spinDay);
        this.spinYears = (Spinner) findViewById(R.id.spinYear);

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

        this.days = new ArrayList<Integer>();
        for (int i = 1; i <= 31; i++)
            this.days.add(i);

        this.now = Calendar.getInstance();
        this.endYear = now.get(Calendar.YEAR);
        this.startYear = this.endYear - 100;
        this.years = new ArrayList<Integer>();
        for (int i = this.endYear; i >= this.startYear; i--)
            this.years.add(i);

        this.fname = (EditText) findViewById(R.id.txtFirstName);
        this.mname = (EditText) findViewById(R.id.txtMiddleName);
        this.lname = (EditText) findViewById(R.id.txtLastName);
        this.suffix = (EditText) findViewById(R.id.txtSuffix);
        this.age = (EditText) findViewById(R.id.txtAge);
        this.radioSexGroup = (RadioGroup) findViewById(R.id.radioSex);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();

        Toast.makeText(parent.getContext(), "Selected: "+item, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void onSubmit(View view) {
        String firstName = this.fname.getText().toString();
        String middleName = this.mname.getText().toString();
        String lastName = this.lname.getText().toString();
        String suffix = this.suffix.getText().toString();
        int age = Integer.parseInt(this.age.getText().toString());
        int year = Integer.parseInt(spinYears.getSelectedItem().toString());
        int day = Integer.parseInt(spinDays.getSelectedItem().toString());
        int month = spinMonths.getSelectedItemPosition()+1;

        int selectedId = this.radioSexGroup.getCheckedRadioButtonId();
        this.radioSexButton = (RadioButton) findViewById(selectedId);
        char sex = (radioSexButton.getText().toString().equals("Male") ? 'M' : 'F');

        Log.e("CreateStudentRecord", firstName+middleName+lastName+suffix);
        Log.e("CreateStudentRecord", age+"|"+year+"|"+day+"|"+month);
        Log.e("CreateStudentRecord", this.schoolID+"");

        if(this.database == null)
            Log.e("CreateStudentRecord", "DB NULL");

        boolean isInserted = this.database.insertStudentRecord(firstName, middleName, lastName, suffix, age, sex, year, day, month, this.schoolID);

        if(isInserted)
            Toast.makeText(this, "Student Record Added", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
    }
}
