package com.example.anuja.sleep_monitor;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayList;

public class SleepMonitorMainActivity extends Activity {

    private static SensorManager sensorMan;
    private static Sensor accelerometer;
    private static SensorEventListener mSensorEventListener;

    private float[] mGravity;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    private Button mStartStopMonitoring;
    private Button mShowHideLogs;
    private ListView mListView;
    private RadioGroup mRadioGroup;
    private RadioButton radioButton;
    private SleepLogAdapter mAdapter;

    private long mMinStillTimePeriod;
    private long mMinMovementTimeConsidered;
    private long mActualStill = 60*60*1000; // 1 hour
    private long mActualMove = 60*15*1000; //15 minutes
    private long mSampleStill = 1*60*1000; // 1 minute
    private long mSampleMove = 1*15*1000; //15 seconds

    private static final int QUEUE_LIMIT = 10;
    
    private long mPreviousTime = 0;
    private long mCurrentTime = 0;

    private String mPreviousTimeStr;
    private String mCurrentTimeStr;

    //StillTimePeriod is an object storing information about 
    //sleep time (no phone movement) >= predefined considered sleep duration
    private StillTimePeriod mPrevStillTime = null;
    private StillTimePeriod mCurrStillTime = null;

    private Queue<StillTimePeriod> mQueue = new LinkedList<StillTimePeriod>();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_monitor_main);

        mStartStopMonitoring = (Button) findViewById(R.id.start_stop_monitoring);
        mShowHideLogs = (Button) findViewById(R.id.show_sleep_log);
        mListView = (ListView) findViewById(R.id.sleep_log_list);
        mRadioGroup=(RadioGroup)findViewById(R.id.radioGroup);

        sensorMan = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        //Creating queue for storing only last 10 records of sleep
        Queue<StillTimePeriod> mQueue = new LinkedList<StillTimePeriod>();

        mShowHideLogs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mShowHideLogs.getText().equals(getApplicationContext().getResources().getString(R.string.show_logs))) {
                    mShowHideLogs.setText(getApplicationContext().getResources().getString(R.string.hide_logs));
                    mAdapter = new SleepLogAdapter(SleepMonitorMainActivity.this, getListFromQ());
                    mListView.setAdapter(mAdapter);
                    mListView.setVisibility(View.VISIBLE);
                } else if(mShowHideLogs.getText().equals(getApplicationContext().getResources().getString(R.string.hide_logs))) {
                    mShowHideLogs.setText(getApplicationContext().getResources().getString(R.string.show_logs));
                    mListView.setVisibility(View.GONE);
                }
            }
        });

        mStartStopMonitoring.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mStartStopMonitoring.getText().equals(getApplicationContext().getResources().getString(R.string.start))) {                  
                    mStartStopMonitoring.setText(getApplicationContext().getResources().getString(R.string.stop));
                    //resetting for new logs
                    clearQueue();
                    refreshList();
                    mPrevStillTime = null;
                    //get selected radio button value
                    int selectedId=mRadioGroup.getCheckedRadioButtonId();
                    radioButton=(RadioButton)findViewById(selectedId);
                    if(radioButton.getText().equals("Sample Values")) {
                        mMinStillTimePeriod = mSampleStill;
                        mMinMovementTimeConsidered = mSampleMove;
                    } else {
                        mMinStillTimePeriod = mActualStill;
                        mMinMovementTimeConsidered = mActualMove;
                    }

                    sensorMan.registerListener(mSensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
                    mPreviousTime = System.currentTimeMillis();
                    mPreviousTimeStr = getCurrentTime();
                } else if(mStartStopMonitoring.getText().equals(getApplicationContext().getResources().getString(R.string.stop))) {
                    mStartStopMonitoring.setText(getApplicationContext().getResources().getString(R.string.start));
                    enqueue();
                    refreshList();
                    sensorMan.unregisterListener(mSensorEventListener);
                }
            }
        });

        mSensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                    mGravity = event.values.clone();
                    float x = mGravity[0];
                    float y = mGravity[1];
                    float z = mGravity[2];
                    mAccelLast = mAccelCurrent;
                    mAccelCurrent = FloatMath.sqrt(x * x + y * y + z * z);
                    float delta = mAccelCurrent - mAccelLast;
                    mAccel = mAccel * 0.9f + delta;

                    if(mAccel > 3){
                        checkSleepTime();
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }

    public void onDestroy() {
        super.onDestroy();
        sensorMan.unregisterListener(mSensorEventListener);
    }

    private String getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        return ""+sdf.format(cal.getTime());
    } 

    private void checkSleepTime() {
        if(mPreviousTime != 0) {
            mCurrentTime = System.currentTimeMillis();
            mCurrentTimeStr = getCurrentTime();
            long diff = mCurrentTime - mPreviousTime;

            if(diff >= mMinStillTimePeriod) {
                if(mPrevStillTime == null) {
                    mPrevStillTime =  new StillTimePeriod(mPreviousTimeStr, mCurrentTimeStr, diff, mPreviousTime, mCurrentTime);
                } else {
                    mCurrStillTime =  new StillTimePeriod(mPreviousTimeStr, mCurrentTimeStr, diff, mPreviousTime, mCurrentTime);
                    if(mCurrStillTime.getStartTime() - mPrevStillTime.getEndTime()  <= mMinMovementTimeConsidered) {
                        long newDiff = mPrevStillTime.getStillPeriod() + mCurrStillTime.getStillPeriod() 
                            + mPrevStillTime.getEndTime() - mCurrStillTime.getStartTime();
                        mPrevStillTime = new StillTimePeriod(mPrevStillTime.getStartTimeStr(), 
                            mCurrStillTime.getEndTimeStr(), newDiff, mPrevStillTime.getStartTime(), 
                            mCurrStillTime.getEndTime());
                        
                        mCurrStillTime = null;
                    } else {
                        if(mQueue.size() == QUEUE_LIMIT)
                            mQueue.remove();
                        mQueue.add(mPrevStillTime);
                        refreshList();
                        mPrevStillTime = new StillTimePeriod(mPreviousTimeStr, mCurrentTimeStr, diff, mPreviousTime, mCurrentTime);
                    }
                }
            }
        }
        mPreviousTime = System.currentTimeMillis();
        mPreviousTimeStr = getCurrentTime();
    }

    private void enqueue() {
        if(mPrevStillTime != null) {
            if(mQueue.size() == QUEUE_LIMIT)
                mQueue.remove();
            mQueue.add(mPrevStillTime);
            refreshList();
        } else {
            mCurrentTime = System.currentTimeMillis();
            mCurrentTimeStr = getCurrentTime();
            long diff = mCurrentTime - mPreviousTime;
            if(diff >= mMinStillTimePeriod) {
                mPrevStillTime = new StillTimePeriod(mPreviousTimeStr, mCurrentTimeStr, diff, mPreviousTime, mCurrentTime);
                if(mQueue.size() == QUEUE_LIMIT)
                    mQueue.remove();
                mQueue.add(mPrevStillTime);
                refreshList();
            }
        }
    }

    private ArrayList<StillTimePeriod> getListFromQ() {
        ArrayList<StillTimePeriod> resultArray = new ArrayList<>();
        for(StillTimePeriod stp : mQueue){
            resultArray.add(stp);
        }
        return resultArray;
    }

    private void refreshList() {
        //notify data set changed
        if(mListView.getVisibility() == View.VISIBLE) {
            mAdapter = new SleepLogAdapter(SleepMonitorMainActivity.this, getListFromQ());
            mListView.setAdapter(mAdapter);
        }
    }

    private void clearQueue() {
        mQueue.clear();
    }
}
