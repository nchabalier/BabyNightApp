package com.example.nicolas.babyapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nicolas on 17/11/2016.
 */

public class SecondActivity extends Activity {

    String[] tableNames;
    int[][] tables;
    int[] colors = {Color.RED, Color.BLUE, Color.GREEN};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Intent intent = getIntent();

        tables = new int[3][24];

        Bundle extras = intent.getExtras();
        tableNames = extras.getStringArray("tableNames");

        for(int i=0; i<3; i++) {
            tables[i] = extras.getIntArray("tables_" + i);
        }

        int[] sumns = new int[24];

        for(int j=0; j<24; j++) {
            sumns[j] = 0;
            for(int i=0; i<3; i++) {
                sumns[j]+=tables[i][j];
            }
        }


        LineChart lineChart = (LineChart) findViewById(R.id.lineChart);
        LineData lineData = new LineData();
        lineChart.setData(lineData);
        for(int k=0; k<3; k++) {
            List<Entry> entries = new ArrayList<Entry>();

            for (int i = 0; i < tables[k].length; i++) {

                float probability = (float)tables[k][i] / (float)sumns[i];
                // turn your data into Entry objects
                entries.add(new Entry(i, probability));
            }

            // The 24h value is the same that the 0h value
            float probability = (float)tables[k][0] / (float)sumns[0];
            entries.add(new Entry(24, probability));

            LineDataSet dataSet = new LineDataSet(entries, tableNames[k]); // add entries to dataset
            dataSet.setColor(colors[k]);
            dataSet.setDrawValues(false);
            dataSet.disableDashedLine();
            dataSet.setColor(colors[k]);
            dataSet.setDrawFilled(true);
            dataSet.setLineWidth(2f);
            dataSet.setFillColor(colors[k]);
            dataSet.setFillAlpha(65);
            dataSet.setDrawCircles(false);

            //FIXME can be under 0 or upper 1 with cubic_bezier approximation
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);


            XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return (int)value +"h";
                }

                @Override
                public int getDecimalDigits() {
                    return 0;
                }
            });


            lineData.addDataSet(dataSet);

        }
        Description description = new Description();
        description.setText("Probability that each solution works");
        lineChart.setDescription(description);


        lineChart.invalidate(); // refresh

    }
}
