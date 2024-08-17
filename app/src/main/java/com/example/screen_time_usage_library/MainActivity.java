package com.example.screen_time_usage_library;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatEditText;

import com.example.screentimeusagelibrary.MonitoredActivity;
import com.example.screentimeusagelibrary.TimeLimitCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.Locale;

public class MainActivity extends MonitoredActivity {
    private MaterialButton button;
    private MaterialTextView main_LBL_last_time;
    private AppCompatEditText editTextText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int minutes = 10;
        setTimeLimit(minutes);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        button.setOnClickListener(v -> {
            Toast.makeText(this,"This is a toast!",Toast.LENGTH_SHORT).show();
            editTextText.clearFocus();
        });

        long lastUseTimestamp = getScreenTimeUsage().getDailyUsage();

        String str = "Your daily usage\n  " + formatTime(lastUseTimestamp);
        main_LBL_last_time.setText(str);

        getScreenTimeUsage().setDialogTimeLimitTitle("Your time is up!!").
                setDialogTimeLimitBody("Your time up.\nPlease try again later.")
                .setTimeLimit(minutes*60*1000)
                .setExtraTime(2*60*1000); // extra 2 minutes

        getScreenTimeUsage().setTimeLimitCallback(new TimeLimitCallback() {
            @Override
            public void onTimeEnds(long milliseconds)  {
                String strTimeEnds = "TIME ENDS! "+formatTime(milliseconds)+"\nPlease come back tomorrow.";
                main_LBL_last_time.setText(strTimeEnds);
                button.setEnabled(false);
                editTextText.setEnabled(false);
            }

            @Override
            public void onUsageTimeUpdated(long milliseconds) {
                String str = "updated usage - tap to refresh\n" + formatTime(milliseconds);
                main_LBL_last_time.setText(str);
            }
        });

        Log.d("TEST - daily","daily " + getScreenTimeUsage().getDailyUsage());
        Log.d("TEST - weekly","weekly " + getScreenTimeUsage().getWeeklyUsage());
        Log.d("TEST - monthly","monthly " + getScreenTimeUsage().getMonthlyUsage());
    }


    void findViews(){
        button = findViewById(R.id.button);
        main_LBL_last_time = findViewById(R.id.main_LBL_last_time);
        editTextText = findViewById(R.id.editTextText);
    }


    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        long remainingMinutes = minutes % 60;
        long remainingSeconds = seconds % 60;

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, remainingMinutes, remainingSeconds);
    }

}