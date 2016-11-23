package com.example.nicolas.babyapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;



public class MainActivity extends AppCompatActivity {

    ClientHandler clientHandler;
    ClientThread clientThread;
    TextView textViewState;
    TextView textViewRx;
    Button connectionButton;
    Button secondChartButton;
    Button alarmButton;

    String[] tableNames = {"Manger", "Lumiere", "Musique"};
    PieChart pieChart;
    int[][] tables;

    Vibrator vibrator;
    boolean onVibrate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pieChart = (PieChart) findViewById(R.id.pieChart);
        textViewRx = (TextView) findViewById(R.id.textViewRx);
        textViewState = (TextView) findViewById(R.id.textViewState);
        connectionButton = (Button) findViewById(R.id.connectionButton);
        secondChartButton = (Button) findViewById(R.id.secondChartButton);
        alarmButton = (Button) findViewById(R.id.alarmButton);

        clientHandler = new ClientHandler(this);
        connectionButton.setOnClickListener(buttonConnectOnClickListener);

        tables = new int[3][24];

        //----------------------------------PieChart---------------------------------------
        List<PieEntry> entries = new ArrayList<PieEntry>();
        entries.add(new PieEntry(18.5f, "Green"));
        entries.add(new PieEntry(26.7f, "Yellow"));
        entries.add(new PieEntry(24.0f, "Red"));
        entries.add(new PieEntry(29.8f, "Blue"));
        entries.add(new PieEntry(2.0f, "Pink"));

        PieDataSet set = new PieDataSet(entries, "Election Results");
        PieData data = new PieData(set);
        set.setColors(Color.parseColor("#4caf50"),  Color.parseColor("#ffeb3b"), Color.parseColor("#f44336"),  Color.parseColor("#03a9f4"),  Color.parseColor("#e91e63"));
        pieChart.setData(data);
        pieChart.invalidate(); // refresh
        //----------------------------------PieChart---------------------------------------

        for(int i=0; i<3; i++){

            tables[i] = loadArray(tableNames[i], getBaseContext());
        }

        updatePieChart();

        secondChartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent secondActivite = new Intent(MainActivity.this, SecondActivity.class);
                secondActivite.putExtra("tables", tables);
                secondActivite.putExtra("tableNames", tableNames);
                Bundle extras = new Bundle();

                //FIXME
                for(int i=0; i<3; i++) {
                    extras.putIntArray("tables_" + i, tables[i]);
                }
                //extras.putSerializable("tables", tables);
                extras.putStringArray("tableNames",tableNames);
                secondActivite.putExtras(extras);
                startActivity(secondActivite);
            }
        });


        //------Try to connect----
        final Handler handler = new Handler();
        final int delay = 10000; // Try to connect each 10 seconds
        handler.postDelayed(new Runnable(){
            public void run(){

                if(clientThread == null) {
                    //Connection on port 8000 and address 192.168.139.1
                    clientThread = new ClientThread(
                            "192.168.137.1",
                            8000,
                            clientHandler);
                    clientThread.start();
                }

                handler.postDelayed(this, delay);
            }
        }, delay);


        //TEST OF ALARM
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        alarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchAlarm();
            }
        });


    }

    public static int[] loadArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(arrayName, 0);
        int size = 24;
        int array[] = new int[size];
        for(int i=0;i<size;i++)
            array[i] = prefs.getInt(arrayName+ "_" + i,i);
        return array;
    }


    private void updatePieChart(){
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minutes = c.get(Calendar.MINUTE);

        //TODO change this to add minutes
        int hourCursor = hour;
        List<PieEntry> entries = new ArrayList<PieEntry>();

        //FIXME
        int numberOfTables = 3;

        int sumn = 0;
        for(int i=0; i<numberOfTables; i++) {
            sumn += tables[i][hourCursor];
        }


        if(sumn != 0) {
            for (int i = 0; i < numberOfTables; i++) {
                float probability = (float) (tables[i][hourCursor] * 100.0f / ((float) sumn));
                if(probability>=2.0f) {
                    entries.add(new PieEntry(probability, tableNames[i]));
                }
            }

            PieDataSet set = new PieDataSet(entries, "Election Results");
            PieData data = new PieData(set);
            set.setColors(Color.parseColor("#4caf50"), Color.parseColor("#ffeb3b"), Color.parseColor("#f44336"), Color.parseColor("#03a9f4"), Color.parseColor("#e91e63"));
            pieChart.setData(data);
            pieChart.invalidate(); // refresh
        }

    }

    public boolean saveArray(int[] array, String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(arrayName, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(arrayName +"_size", array.length);
        for(int i=0;i<array.length;i++)
            editor.putInt(arrayName + "_" + i, array[i]);
        return editor.commit();
    }

    View.OnClickListener buttonConnectOnClickListener =
            new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    //Connection on port 8000 and address 192.168.139.1
                    clientThread = new ClientThread(
                            "192.168.137.1",
                            8000,
                            clientHandler);
                    clientThread.start();
                }
            };

    private void updateState(String state){
        textViewState.setText(state);
    }

    // Traitement du message recu du raspberry
    private void updateRxMsg(String rxmsg){

        String[] parts = rxmsg.split(" ");

        switch (parts[0]){
            case "TABLE":
                textViewRx.setText("Update of table num: " + parts[1] + "\n");

                //Number of the table
                int numTable = Integer.parseInt(parts[1]);

                //Table of number of times a solution worked between [0h,24h]
                int[] table = convertMessageToArray(parts);

                tables[numTable] = table;
                saveArray(table, tableNames[numTable], getBaseContext());
                //FIXME
                if(numTable>=2) {
                    updatePieChart();
                }
                break;
            case "ALARM":
                switchAlarm();
                break;
            default:
                textViewRx.setText("Unknow message: " + rxmsg + "\n");
        }

    }

    private int[] convertMessageToArray(String[] parts) {
        int[] newArray = new int[24];
        for(int i=2; i<parts.length; i++){
            newArray[i-2] = Integer.parseInt(parts[i]);
        }
        return newArray;
    }

    private void clientEnd(){
        clientThread = null;
        textViewState.setText("clientEnd()");

    }

    //If no alarm running then run the alarm else stop ip
    private void switchAlarm(){
        if(!onVibrate) {
            long[] pattern = {0, 200, 200};
            vibrator.vibrate(pattern, 0);
            onVibrate = true;
        }else{
            vibrator.cancel();
            onVibrate = false;
        }
        /*Intent intent = new Intent(this, MyBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this.getApplicationContext(), 234324243, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),1000,
                pendingIntent);*/
    }

    public static class ClientHandler extends Handler {
        public static final int UPDATE_STATE = 0;
        public static final int UPDATE_MSG = 1;
        public static final int UPDATE_END = 2;
        private MainActivity parent;

        public ClientHandler(MainActivity parent) {
            super();
            this.parent = parent;
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case UPDATE_STATE:
                    parent.updateState((String)msg.obj);
                    break;
                case UPDATE_MSG:
                    parent.updateRxMsg((String)msg.obj);
                    break;
                case UPDATE_END:
                    parent.clientEnd();
                    break;
                default:
                    super.handleMessage(msg);
            }

        }

    }
}
