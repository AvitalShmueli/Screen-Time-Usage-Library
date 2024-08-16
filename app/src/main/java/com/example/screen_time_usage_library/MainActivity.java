package com.example.screen_time_usage_library;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.screentimeusagelibrary.MonitoredActivity;
import com.example.screentimeusagelibrary.TimeLimitCallback;

import java.text.DecimalFormat;

public class MainActivity extends MonitoredActivity {
    private Button button;
    private TextView main_LBL_last_time;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int minutes = 2;
        setTimeLimit_minutes(minutes);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        button.setOnClickListener(v -> Toast.makeText(this,"This is a toast!",Toast.LENGTH_SHORT).show());

        getScreenTimeUsage().setDialogTimeLimitTitle("Your time is up!!").
                setDialogTimeLimitBody("Your time up.\nPlease try again later.")
                .setTimeLimit(minutes*60*1000)
                .setExtraTime(1*60*1000); // half an hour

        getScreenTimeUsage().setTimeLimitCallback(new TimeLimitCallback() {
            @Override
            public void onTimeEnds()  {
                String strTimeEnds = "TIME ENDS!\nPlease come back tomorrow.";
                main_LBL_last_time.setText(strTimeEnds);
            }

            @Override
            public void onUsageTimeUpdated(float usageMinutes) {
                String str = "updated\n"+df.format(usageMinutes)+" min";
                main_LBL_last_time.setText(str);
            }
        });

        float lastUseTimestamp = getScreenTimeUsage().getDailyUsage();
        String str = "HELLO\n"+df.format(lastUseTimestamp)+" min";
        main_LBL_last_time.setText(str);

        Log.d("TEST - daily","daily " + getScreenTimeUsage().getDailyUsage());
        Log.d("TEST - weekly","weekly " + getScreenTimeUsage().getWeeklyUsage());
        Log.d("TEST - monthly","monthly " + getScreenTimeUsage().getMonthlyUsage());
    }

    void findViews(){
        button = findViewById(R.id.button);
        main_LBL_last_time = findViewById(R.id.main_LBL_last_time);
    }

}