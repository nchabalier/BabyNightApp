package com.example.nicolas.babyappfragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Nicolas on 23/11/2016.
 */

public class FirstFragment extends Fragment {

    TextView textViewState;
    TextView textViewRx;
    Button alarmButton;

    PieChart pieChart;
    int[][] tables;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.first_frag, container, false);


        pieChart = (PieChart) v.findViewById(R.id.pieChart);
        textViewRx = (TextView) v.findViewById(R.id.textViewRx);
        textViewState = (TextView) v.findViewById(R.id.textViewState);
        alarmButton = (Button) v.findViewById(R.id.alarmButton);


        pieChart.setRotationEnabled(false);

        // Load tables
        tables = new int[3][24];
        for(int i=0; i<3; i++){
            tables[i] = MainActivity.loadArray(MainActivity.tableNames[i], super.getContext());
        }

        updatePieChart();


        return v;
    }

    public static FirstFragment newInstance(String text) {

        FirstFragment f = new FirstFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

     private void updatePieChart(){
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minutes = c.get(Calendar.MINUTE);

        //TODO change this to add minutes
        int hourCursor = hour;
        List<PieEntry> entries = new ArrayList<PieEntry>();

        int sumn = 0;
        for(int i=0; i<MainActivity.NUMBER_OF_COLUMNS; i++) {
            sumn += tables[i][hourCursor];
        }


        if(sumn != 0) {
            for (int i = 0; i<MainActivity.NUMBER_OF_COLUMNS; i++) {
                float probability = (float) (tables[i][hourCursor] * 100.0f / ((float) sumn));
                if(probability>=2.0f) {
                    entries.add(new PieEntry(probability, MainActivity.tableNames[i]));
                }
            }

            PieDataSet set = new PieDataSet(entries, "");
            PieData data = new PieData(set);
            set.setColors(MainActivity.tableColors);
            pieChart.setData(data);
            pieChart.invalidate(); // refresh
        }

    }
}
