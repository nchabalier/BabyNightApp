package com.example.nicolas.babyappfragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nicolas on 23/11/2016.
 */

public class SecondFragment extends Fragment {

    int[][] tables;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.second_frag, container, false);

        // Load tables
        tables = new int[3][24];
        for(int i=0; i<3; i++){
            tables[i] = MainActivity.loadArray(MainActivity.tableNames[i], super.getContext());
        }


        int[] sumns = new int[24];

        for(int j=0; j<24; j++) {
            sumns[j] = 0;
            for(int i=0; i<3; i++) {
                sumns[j]+=tables[i][j];
            }
        }


        LineChart lineChart = (LineChart) v.findViewById(R.id.lineChart);
        LineData lineData = new LineData();
        lineChart.setData(lineData);
        for(int k=0; k<MainActivity.NUMBER_OF_COLUMNS; k++) {
            List<Entry> entries = new ArrayList<Entry>();

            for (int i = 0; i < tables[k].length; i++) {

                float probability = (float)tables[k][i] / (float)sumns[i];
                // turn your data into Entry objects
                entries.add(new Entry(i, probability));
            }

            // The 24h value is the same that the 0h value
            float probability = (float)tables[k][0] / (float)sumns[0];
            entries.add(new Entry(24, probability));

            LineDataSet dataSet = new LineDataSet(entries, MainActivity.tableNames[k]); // add entries to dataset
            dataSet.setColor(MainActivity.tableColors.get(k));
            dataSet.setDrawValues(false);
            dataSet.disableDashedLine();
            dataSet.setColor(MainActivity.tableColors.get(k));
            dataSet.setDrawFilled(true);
            dataSet.setLineWidth(2f);
            dataSet.setFillColor(MainActivity.tableColors.get(k));
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

        return v;
    }

    public static SecondFragment newInstance(String text) {

        SecondFragment f = new SecondFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }
}
