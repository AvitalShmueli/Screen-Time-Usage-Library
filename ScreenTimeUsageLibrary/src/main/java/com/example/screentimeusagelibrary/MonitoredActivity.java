package com.example.screentimeusagelibrary;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class MonitoredActivity extends AppCompatActivity {

    private float timeLimitMinutes = -1, timeoutMinutes = -1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(timeLimitMinutes == -1)
            timeLimitMinutes = 2; // 2 minutes
        long timeLimit_milliseconds = (long) (timeLimitMinutes * 60 * 1000);

        if(timeoutMinutes == -1)
            timeoutMinutes = 0.5F; // 30 seconds
        long timeout_milliseconds = (long) (timeoutMinutes * 60 * 1000);

        ScreenTimeUsage.init(this, timeLimit_milliseconds, timeout_milliseconds);

    }


    public void setTimeLimit(float minutes) {
        this.timeLimitMinutes = minutes;
    }


    public void setTimeout(float minutes) {
        this.timeoutMinutes = minutes;
    }


    public ScreenTimeUsage getScreenTimeUsage() {
        return ScreenTimeUsage.getInstance();
    }


    @Override
    public void onUserInteraction() {
        long now = System.currentTimeMillis();
        Log.d("TEST - last interaction","last interaction: " + getDate(now));
        ScreenTimeUsage.getInstance().onUserInteractionDetected(this);
        super.onUserInteraction();
    }


    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        return DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();
    }


    @Override
    protected void onStop() {
        Log.d("TEST","STOP");
        ScreenTimeUsage.getInstance().updateUsageTime();
        super.onStop();
    }


    @Override
    protected void onRestart() {
        Log.d("TEST","RESTART");
        ScreenTimeUsage.getInstance().setInitialTimestampMillis(System.currentTimeMillis());
        ScreenTimeUsage.getInstance().updateUsageTime();
        super.onRestart();
    }
}

