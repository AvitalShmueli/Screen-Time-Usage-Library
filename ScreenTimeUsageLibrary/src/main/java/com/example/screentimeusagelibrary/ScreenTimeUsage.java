package com.example.screentimeusagelibrary;

import static android.content.ContentValues.TAG;
import static com.example.screentimeusagelibrary.RecordsList.RECORDS_TABLE;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ScreenTimeUsage {
    private static ScreenTimeUsage instance = null;
    private long timeout, timeLimit, extraTime;
    private Handler mHandlerTimeLimit, mHandlerTimeout;
    private Runnable mRunnableTimeLimit, mRunnableTimeout;
    private MaterialTextView dialog_LBL_title;
    private MaterialTextView dialog_LBL_body;
    private String dialogTimeLimitBtnDismiss, dialogTimeLimitBtnMoreTime, dialogTimeLimitTitle, dialogTimeLimitBody;
    private String dialogTimeoutBtnDismiss, dialogTimeoutBtnMoreTime, dialogTimeoutTitle, dialogTimeoutBody;
    private long totalMilliseconds;
    private final String today;
    private RecordsList records;
    private Record todayRecord;
    private long initialTimestampMillis;
    private boolean isDialogVisible;
    private TimeLimitCallback timeLimitCallback;
    private final String myAppPackage;
    private boolean isDismissOrExitPressed;
    private final View dialogView;


    public static void init(Context context, long timeLimit_milliseconds, long timeout_milliseconds){
        synchronized (SharedPreferencesManager.class) {
            if (instance == null) {
                instance = new ScreenTimeUsage(context,timeLimit_milliseconds,timeout_milliseconds);
            }
        }
    }


    public static void init(Context context){
        synchronized (SharedPreferencesManager.class) {
            if (instance == null) {
                instance = new ScreenTimeUsage(context,5000,2000);
            }
        }
    }


    public static ScreenTimeUsage getInstance() {
        return instance;
    }


    private ScreenTimeUsage(Context context, long timeLimit_milliseconds, long timeout_milliseconds){
        timeLimit = timeLimit_milliseconds;
        timeout = timeout_milliseconds;
        isDialogVisible = false;
        isDismissOrExitPressed = false;
        extraTime = 10 * 60 * 1000; //default 10 minutes

        myAppPackage = context.getPackageName();
        initialTimestampMillis = System.currentTimeMillis();

        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_timeout, null);
        findViews(dialogView);

        SharedPreferencesManager.init(context);

        Date todayDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        today = dateFormat.format(todayDate);

        loadDataFromSP();
        initView(context, dialogView);
    }


    public ScreenTimeUsage setTimeLimitCallback(TimeLimitCallback timeLimitCallback) {
        this.timeLimitCallback = timeLimitCallback;
        return this;
    }


    private void loadDataFromSP() {
        records = new Gson().fromJson(SharedPreferencesManager.getInstance().getString(RECORDS_TABLE, ""), RecordsList.class);
        if(records == null) {
            records = new RecordsList();
            records.setListName("ScreenTimeUsage: " + myAppPackage);
        }
        else{
            todayRecord = records.getTodayRecord();
            if(todayRecord != null){
                totalMilliseconds = todayRecord.getUsageMilliseconds();
            }
            else totalMilliseconds = 0;
        }
        Log.d("TEST RECORDS_TABLE from SP", records.toString());
    }


    private void findViews(View view) {
        dialog_LBL_title = view.findViewById(R.id.dialog_LBL_title);
        dialog_LBL_body = view.findViewById(R.id.dialog_LBL_body);
    }


    private void initView(Context context, View view){
        dialogTimeLimitTitle = "Your time is up!!";
        dialogTimeLimitBody = "Your " + timeLimit/(60*1000) + " minutes of using the app are over.";
        dialogTimeLimitBtnDismiss = "Dismiss";
        dialogTimeLimitBtnMoreTime = "More time";

        dialogTimeoutTitle = "Are you still here?";
        dialogTimeoutBody = "Just checking in...";
        dialogTimeoutBtnDismiss = "Exit";
        dialogTimeoutBtnMoreTime = "Yes";

        if (totalMilliseconds >= timeLimit && !isDialogVisible)
            showTimeLimitDialog(context, dialogView);

        else{
            timeLimit -= totalMilliseconds;
            Log.d("TEST", "Remain time (ms): " + timeLimit);
        }

        mHandlerTimeLimit = new Handler(Looper.getMainLooper());
        mRunnableTimeLimit = () -> showTimeLimitDialog(context, view);
        startHandlerTimeLimit();

        mHandlerTimeout = new Handler(Looper.getMainLooper());
        mRunnableTimeout = () -> showTimeoutDialog(context, view);
        startHandlerTimeout();

    }


    private void showTimeLimitDialog(Context context, View view) {
        if (!isDismissOrExitPressed) {
            if (isDialogVisible) {
                stopHandlerTimeLimit();
                startHandlerTimeLimit();
                return;
            }

            if (view.getParent() != null)
                ((ViewGroup) view.getParent()).removeView(view);

            dialog_LBL_title.setText(dialogTimeLimitTitle);
            dialog_LBL_body.setText(dialogTimeLimitBody);
            AlertDialog alertDialog = new MaterialAlertDialogBuilder(context).setView(view)
                    .setPositiveButton(dialogTimeLimitBtnMoreTime, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            stopHandlerTimeLimit();
                            Log.d("TEST", "Dialog time limit | " + dialogTimeLimitBtnMoreTime + "remain time (ms): " + timeLimit);
                            updateUsageTime();
                            if (timeLimitCallback != null) {
                                timeLimitCallback.onUsageTimeUpdated(totalMilliseconds);
                            }
                            timeLimit = extraTime; // now the interval will be the extra time
                            startHandlerTimeLimit();
                            dialog.dismiss();
                        }
                    }).setNegativeButton(dialogTimeLimitBtnDismiss, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            stopHandlerTimeLimit();
                            stopHandlerTimeout();
                            Log.d("TEST", "Dialog time limit | " + dialogTimeLimitBtnDismiss);
                            updateUsageTime();
                            if (timeLimitCallback != null)
                                timeLimitCallback.onTimeEnds(totalMilliseconds);
                            isDismissOrExitPressed = true;
                            dialog.dismiss();
                        }
                    }).create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setOnShowListener(dialog -> isDialogVisible = true);// Set the flag to true when the dialog is shown
            alertDialog.setOnDismissListener(dialog -> isDialogVisible = false); // Reset the flag when the dialog is dismissed
            alertDialog.show();
        }
    }


    private void showTimeoutDialog(Context context, View view) {
        if (!isDismissOrExitPressed) {
            if (isDialogVisible) {
                stopHandlerTimeout();
                startHandlerTimeout();
                return;
            }

            if (view.getParent() != null)
                ((ViewGroup) view.getParent()).removeView(view);

            dialog_LBL_title.setText(dialogTimeoutTitle);
            dialog_LBL_body.setText(dialogTimeoutBody);
            AlertDialog alertDialog = new MaterialAlertDialogBuilder(context).setView(view)
                    .setPositiveButton(dialogTimeoutBtnMoreTime, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            stopHandlerTimeout();
                            Log.d("TEST", "Dialog timeout | " + dialogTimeoutBtnMoreTime + "remain time (ms): " + timeLimit);
                            updateUsageTime();
                            if (timeLimitCallback != null) {
                                timeLimitCallback.onUsageTimeUpdated(totalMilliseconds);
                            }
                            startHandlerTimeout();
                            dialog.dismiss();
                        }
                    }).setNegativeButton(dialogTimeoutBtnDismiss, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            stopHandlerTimeout();
                            stopHandlerTimeLimit();
                            Log.d("TEST", "Dialog timeout | " + dialogTimeoutBtnDismiss);
                            updateUsageTime();
                            if (timeLimitCallback != null)
                                timeLimitCallback.onTimeEnds(totalMilliseconds);
                            isDismissOrExitPressed = true;
                            dialog.dismiss();
                        }
                    }).create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setOnShowListener(dialog -> isDialogVisible = true);// Set the flag to true when the dialog is shown
            alertDialog.setOnDismissListener(dialog -> isDialogVisible = false); // Reset the flag when the dialog is dismissed
            alertDialog.show();
        }
    }


    private void startHandlerTimeLimit() {
        mHandlerTimeLimit.postDelayed(mRunnableTimeLimit, timeLimit);
    }


    private void stopHandlerTimeLimit(){
        mHandlerTimeLimit.removeCallbacks(mRunnableTimeLimit);
    }


    private void startHandlerTimeout() {
        mHandlerTimeout.postDelayed(mRunnableTimeout, timeout);
    }


    private void stopHandlerTimeout(){
        mHandlerTimeout.removeCallbacks(mRunnableTimeout);
    }


    public void onUserInteractionDetected(Context context) {
        stopHandlerTimeout();
        if(!isDismissOrExitPressed) {
            startHandlerTimeout();
            updateUsageTime();
            if(System.currentTimeMillis() - extraTime > initialTimestampMillis)
                showTimeLimitDialog(context, dialogView);
        }
    }


    public ScreenTimeUsage setTimeout(long milliseconds) {
        timeout = milliseconds;
        return this;
    }


    public ScreenTimeUsage setTimeLimit(long milliseconds) {
        timeLimit = milliseconds;
        return this;
    }


    public ScreenTimeUsage setExtraTime(long milliseconds) {
        this.extraTime = milliseconds;
        return this;
    }


    public ScreenTimeUsage setDialogTimeLimitButtonNameDismiss(String dialogBtnNameDismiss) {
        this.dialogTimeLimitBtnDismiss = dialogBtnNameDismiss;
        return this;
    }


    public ScreenTimeUsage setDialogTimeLimitButtonNameMoreTime(String dialogBtnNameMoreTime) {
        this.dialogTimeLimitBtnMoreTime = dialogBtnNameMoreTime;
        return this;
    }


    public ScreenTimeUsage setDialogTimeLimitTitle(String dialogTimeLimitTitle) {
        this.dialogTimeLimitTitle = dialogTimeLimitTitle;
        dialog_LBL_title.setText(dialogTimeLimitTitle);
        return this;
    }


    public ScreenTimeUsage setDialogTimeLimitBody(String dialogTimeLimitBody) {
        this.dialogTimeLimitBody = dialogTimeLimitBody;
        dialog_LBL_body.setText(dialogTimeLimitBody);
        return this;
    }


    public ScreenTimeUsage setDialogTimeoutButtonNameDismiss(String dialogBtnNameDismiss) {
        this.dialogTimeoutBtnDismiss = dialogBtnNameDismiss;
        return this;
    }

    public ScreenTimeUsage setDialogTimeoutButtonNameMoreTime(String dialogBtnNameMoreTime) {
        this.dialogTimeoutBtnMoreTime = dialogBtnNameMoreTime;
        return this;
    }


    public ScreenTimeUsage setDialogTimeoutTitle(String dialogTimeoutTitle) {
        this.dialogTimeoutTitle = dialogTimeoutTitle;
        dialog_LBL_title.setText(dialogTimeoutTitle);
        return this;
    }


    public ScreenTimeUsage setDialogTimeoutBody(String dialogTimeoutBody) {
        this.dialogTimeoutBody = dialogTimeoutBody;
        dialog_LBL_body.setText(dialogTimeoutBody);
        return this;
    }


    public void setInitialTimestampMillis(long initialTimestampMillis) {
        this.initialTimestampMillis = initialTimestampMillis;
    }


    public void updateUsageTime(){
        long currentTimestamp = System.currentTimeMillis();
        long delta = currentTimestamp - initialTimestampMillis;
        initialTimestampMillis = currentTimestamp;
        if(todayRecord == null){
            todayRecord = new Record().setDate(today).setUsageMilliseconds(delta);
            records.add(todayRecord);
        } else {
            records.get(records.getIndexOfToday()).addMillseconds(delta);
        }
        totalMilliseconds += delta;
        if (timeLimitCallback != null) {
            timeLimitCallback.onUsageTimeUpdated(totalMilliseconds);
        }
        Gson gson = new Gson();
        String recordsListAsJson = gson.toJson(records);
        SharedPreferencesManager.getInstance().putString(RECORDS_TABLE, recordsListAsJson);
        Log.d("TEST RECORDS_TABLE from SP", records.toString());
    }


    public long getDailyUsage(){
        if(todayRecord == null)
            return 0;
        return todayRecord.getUsageMilliseconds();
    }


    public long getWeeklyUsage(){
        return getUsageForPeriod(7);
    }


    public long getMonthlyUsage(){
        return getUsageForPeriod(30);
    }


    private long getUsageForPeriod(int days) {
        if (records == null || records.getRecordsArrayList() == null) return 0;

        long totalUsageForPeriod = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -days);
        Date cutoffDate = calendar.getTime();

        for (Record record : records.getRecordsArrayList()) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                Date recordDate = format.parse(record.getDate());
                if (recordDate != null && recordDate.after(cutoffDate)) {
                    totalUsageForPeriod += record.getUsageMilliseconds();
                }
            } catch (Exception e) {
                Log.e(TAG,"An unexpected error occurred during calculation of usage for period.");
            }
        }
        return totalUsageForPeriod;
    }


    public boolean isDismissOrExitPressed() {
        return isDismissOrExitPressed;
    }

}