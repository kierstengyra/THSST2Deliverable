package com.thsst2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by gyra on 01/28/2018.
 */
public class CreateStudentRecord extends AppCompatActivity implements OnItemSelectedListener {

    ImageView imgBackgroundForm;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_student_record);

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
        this.imgBackgroundForm = (ImageView) findViewById(R.id.imgBackgroundForm);
        this.imgBackgroundForm.setScaleType(ImageView.ScaleType.FIT_XY);

        spinMonths = (Spinner) findViewById(R.id.spinMonth);
        spinDays = (Spinner) findViewById(R.id.spinDay);
        spinYears = (Spinner) findViewById(R.id.spinYear);

        months = new ArrayList<String>();
        months.add("January");
        months.add("February");
        months.add("March");
        months.add("April");
        months.add("May");
        months.add("June");
        months.add("July");
        months.add("August");
        months.add("September");
        months.add("October");
        months.add("November");
        months.add("December");

        days = new ArrayList<Integer>();
        for(int i = 1; i <= 31; i++)
            days.add(i);

        now = Calendar.getInstance();
        endYear = now.get(Calendar.YEAR);
        startYear = endYear - 100;
        years = new ArrayList<Integer>();
        for(int i = startYear; i <= endYear; i++)
            years.add(i);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();

        Toast.makeText(parent.getContext(), "Selected: "+item, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
