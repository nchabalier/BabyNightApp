package com.example.nicolas.babyappfragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends FragmentActivity {


    ClientHandler clientHandler;
    ClientThread clientThread;

    public static int NUMBER_OF_COLUMNS = 3;
    public static String[] tableNames = {"Manger", "Lumiere", "Musique"};
    public static List<Integer> tableColors = Arrays.asList(Color.parseColor("#4caf50"), Color.parseColor("#ffeb3b"), Color.parseColor("#f44336"), Color.parseColor("#03a9f4"), Color.parseColor("#e91e63"));
    //int[][] tables;


    Vibrator vibrator;
    boolean onVibrate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager pager = (ViewPager) findViewById(R.id.viewPager);
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));




        clientHandler = new MainActivity.ClientHandler(this);
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


        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            switch(pos) {

                case 0: return FirstFragment.newInstance("FirstFragment, Instance 1");
                case 1: return SecondFragment.newInstance("SecondFragment, Instance 1");
                case 2: return ThirdFragment.newInstance("ThirdFragment, Instance 1");
                case 3: return ThirdFragment.newInstance("ThirdFragment, Instance 2");
                case 4: return ThirdFragment.newInstance("ThirdFragment, Instance 3");
                default: return ThirdFragment.newInstance("ThirdFragment, Default");
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
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

    public static int[] loadArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(arrayName, 0);
        int size = 24;
        int array[] = new int[size];
        for(int i=0;i<size;i++)
            array[i] = prefs.getInt(arrayName+ "_" + i,i);
        return array;
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
        //textViewState.setText(state);
    }

    // Traitement du message recu du raspberry
    private void updateRxMsg(String rxmsg){

        String[] parts = rxmsg.split(" ");

        switch (parts[0]){
            case "TABLE":
                //textViewRx.setText("Update of table num: " + parts[1] + "\n");

                //Number of the table
                int numTable = Integer.parseInt(parts[1]);

                //Table of number of times a solution worked between [0h,24h]
                int[] table = convertMessageToArray(parts);

                //tables[numTable] = table;
                saveArray(table, tableNames[numTable], getBaseContext());

                break;
            case "ALARM":
                switchAlarm();
                break;
            default:
                //textViewRx.setText("Unknow message: " + rxmsg + "\n");
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
        //textViewState.setText("clientEnd()");

    }

    //If no alarm running then run the alarm else stop ip
    private void switchAlarm(){
        /*if(!onVibrate) {
            long[] pattern = {0, 200, 200};
            vibrator.vibrate(pattern, 0);
            onVibrate = true;
        }else{
            vibrator.cancel();
            onVibrate = false;
        }*/
        vibrator.vibrate(500);
    }

}
