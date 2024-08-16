package com.example.screentimeusagelibrary;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class MonitoredActivity extends AppCompatActivity {

    private ScreenTimeUsage screenTimeUsage;
    private float timeLimit_minutes = -1,timeout_minutes = -1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(timeLimit_minutes == -1)
            timeLimit_minutes = 2; // 2 minutes
        long timeLimit_milliseconds = (long) (timeLimit_minutes * 60 * 1000);

        if(timeout_minutes == -1)
            timeout_minutes = 0.5F; // 30 seconds
        long timeout_milliseconds = (long) (timeout_minutes * 60 * 1000);

        screenTimeUsage = new ScreenTimeUsage(this, timeLimit_milliseconds, timeout_milliseconds);

    }


    public void setTimeLimit(float timeLimit_minutes) {
        this.timeLimit_minutes = timeLimit_minutes;
    }


    public void setTimeout(float timeout_minutes) {
        this.timeout_minutes = timeout_minutes;
    }


    public ScreenTimeUsage getScreenTimeUsage() {
        return screenTimeUsage;
    }


    @Override
    public void onUserInteraction() {
        long now = System.currentTimeMillis();
        Log.d("TEST - last interaction","last interaction: " + getDate(now));
        screenTimeUsage.onUserInteractionDetected();
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
        screenTimeUsage.updateUsageMinutes();
        super.onStop();
    }


    @Override
    protected void onRestart() {
        Log.d("TEST","RESTART");
        screenTimeUsage.setInitialTimestampMillis(System.currentTimeMillis());
        screenTimeUsage.updateUsageMinutes();
        super.onRestart();
    }
}

