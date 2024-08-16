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
    private long timeout, timeLimit, extraTime;
    private final Context context;
    private Handler mHandlerTimeLimit, mHandlerTimeout;
    private Runnable mRunnableTimeLimit, mRunnableTimeout;
    private MaterialTextView dialog_LBL_title;
    private MaterialTextView dialog_LBL_body;
    private String dialogTimeLimitBtnDismiss, dialogTimeLimitBtnMoreTime, dialogTimeLimitTitle, dialogTimeLimitBody;
    private String dialogTimeoutBtnDismiss, dialogTimeoutBtnMoreTime, dialogTimeoutTitle, dialogTimeoutBody;
    private float totalMinutes;
    private final String today;
    private RecordsList records;
    private Record todayRecord;
    private long initialTimestampMillis;
    private boolean isDialogVisible;
    private TimeLimitCallback timeLimitCallback;
    private final String myAppPackage;
    private boolean isDismissOrExitPressed;
    private View dialogView;

    public ScreenTimeUsage(Context context){
        this(context,5000,2000);
    }

    public ScreenTimeUsage(Context context, long timeLimit_milliseconds, long timeout_milliseconds){
        this.context = context;
        timeLimit = timeLimit_milliseconds;
        timeout = timeout_milliseconds;
        isDialogVisible = false;
        isDismissOrExitPressed = false;
        extraTime = 10 * 60 * 1000; //10 minutes

        Log.d("TEST", "initial time limit = "+timeLimit);

        myAppPackage = context.getPackageName();
        initialTimestampMillis = System.currentTimeMillis();

        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_timeout, null);
        findViews(dialogView);

        SharedPreferencesManager.init(context);

        Date todayDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        today = dateFormat.format(todayDate);
        Log.d("TEST TIME", today);

        loadDataFromSP();
        initView(dialogView);

        if (totalMinutes * 60 * 1000 >= timeLimit && !isDialogVisible)
            showTimeLimitDialog(dialogView);
    }


    public ScreenTimeUsage setTimeLimitCallback(TimeLimitCallback timeLimitCallback) {
        this.timeLimitCallback = timeLimitCallback;
        return this;
    }


    private void loadDataFromSP() {
        records = new Gson().fromJson(SharedPreferencesManager.getInstance().getString(RECORDS_TABLE, ""), RecordsList.class);
        if(records == null) {
            records = new RecordsList();
            records.setListName("ScreenTimeUsage - " + myAppPackage);
        }
        else{
            todayRecord = records.getTodayRecord();
            if(todayRecord != null){
                totalMinutes = todayRecord.getUsageMinutes();
            }
            else totalMinutes = 0;
        }
        Log.d("TEST RECORDS_TABLE from SP", records.toString());
    }


    private void findViews(View view) {
        dialog_LBL_title = view.findViewById(R.id.dialog_LBL_title);
        dialog_LBL_body = view.findViewById(R.id.dialog_LBL_body);
    }


    private void initView(View view){
        dialogTimeLimitTitle = "Your time is up!!";
        dialogTimeLimitBody = "Your " + timeLimit/(60*1000) + " minutes of using the app are over.";
        dialogTimeLimitBtnDismiss = "Dismiss";
        dialogTimeLimitBtnMoreTime = "More time";

        dialogTimeoutTitle = "Are you still here?";
        dialogTimeoutBody = "Just checking in...";
        dialogTimeoutBtnDismiss = "Exit";
        dialogTimeoutBtnMoreTime = "Yes";

        Log.d("TEST", "after init view time limit = "+timeLimit);
        mHandlerTimeLimit = new Handler(Looper.getMainLooper());
        mRunnableTimeLimit = () -> showTimeLimitDialog(view);
        startHandlerTimeLimit();

        mHandlerTimeout = new Handler(Looper.getMainLooper());
        mRunnableTimeout = () -> showTimeoutDialog(view);
        startHandlerTimeout();

    }


    private void showTimeLimitDialog(View view){
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
                        Log.d("TEST", "Dialog time limit more time");
                        updateUsageMinutes();
                        if (timeLimitCallback != null) {
                            timeLimitCallback.onUsageTimeUpdated(totalMinutes);
                        }
                        timeLimit = extraTime; // now the interval will be the extra time
                        Log.d("TEST","extra time added (ms)= "+extraTime+" | new limit (ms)= " + timeLimit);
                        startHandlerTimeLimit();
                        dialog.dismiss();
                    }
                }).setNegativeButton(dialogTimeLimitBtnDismiss, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        stopHandlerTimeLimit();
                        stopHandlerTimeout();
                        Log.d("TEST", "Dialog time limit dismiss");
                        updateUsageMinutes();
                        if(timeLimitCallback != null)
                            timeLimitCallback.onTimeEnds();
                        isDismissOrExitPressed = true;
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.setOnShowListener(dialog -> isDialogVisible = true);// Set the flag to true when the dialog is shown
        alertDialog.setOnDismissListener(dialog -> isDialogVisible = false); // Reset the flag when the dialog is dismissed
        alertDialog.show();
    }


    private void showTimeoutDialog(View view){
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
                        Log.d("TEST", "Dialog timeout more time");
                        updateUsageMinutes();
                        if (timeLimitCallback != null) {
                            timeLimitCallback.onUsageTimeUpdated(totalMinutes);
                        }
                        startHandlerTimeout();
                        dialog.dismiss();
                    }
                }).setNegativeButton(dialogTimeoutBtnDismiss, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        stopHandlerTimeout();
                        stopHandlerTimeLimit();
                        Log.d("Exit", "Dialog timeout dismiss");
                        updateUsageMinutes();
                        if(timeLimitCallback != null)
                            timeLimitCallback.onTimeEnds();
                        isDismissOrExitPressed = true;
                        dialog.dismiss();
                    }
                }).create();

        alertDialog.setOnShowListener(dialog -> isDialogVisible = true);// Set the flag to true when the dialog is shown
        alertDialog.setOnDismissListener(dialog -> isDialogVisible = false); // Reset the flag when the dialog is dismissed
        alertDialog.show();
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


    public void onUserInteractionDetected() {
        stopHandlerTimeout();
        if(!isDismissOrExitPressed) {
            startHandlerTimeout();
            updateUsageMinutes();
            //checkTimeLimit();
            if(System.currentTimeMillis() - extraTime > initialTimestampMillis)
                showTimeLimitDialog(dialogView);
        }
    }


    public ScreenTimeUsage setTimeout(long milliseconds) {
        timeout = milliseconds;
        return this;
    }


    public ScreenTimeUsage setTimeLimit(long milliseconds) {
        timeLimit = milliseconds;
        Log.d("TEST", "set time limit = "+timeLimit);
        //checkTimeLimit();
        return this;
    }


    public ScreenTimeUsage setExtraTime(long milliseconds) {
        this.extraTime = milliseconds;
        return this;
    }


    /*
    private boolean checkTimeLimit() {
        if (totalMinutes * 60 * 1000 >= timeLimit && !isDialogVisible) {
            showTimeLimitDialog(dialogView);
            return false;
        }
        return true;
    }*/


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


    public void updateUsageMinutes(){
        long currentTimestamp = System.currentTimeMillis();
        long delta = currentTimestamp - initialTimestampMillis;
        float deltaMin = (float) delta /(60*1000);
        initialTimestampMillis = currentTimestamp;
        if(todayRecord == null){
            todayRecord = new Record().setDate(today).setUsageMinutes(deltaMin);
            records.add(todayRecord);
        } else {
            records.get(records.getIndexOfToday()).addMinutes(deltaMin);
        }
        totalMinutes += deltaMin;
        if (timeLimitCallback != null) {
            timeLimitCallback.onUsageTimeUpdated(totalMinutes);
        }
        Gson gson = new Gson();
        String recordsListAsJson = gson.toJson(records);
        SharedPreferencesManager.getInstance().putString(RECORDS_TABLE, recordsListAsJson);
        Log.d("TEST RECORDS_TABLE from SP", records.toString());
    }


    public float getDailyUsage(){
        return todayRecord.getUsageMinutes();
    }


    public float getWeeklyUsage(){
        return  getUsageForPeriod(7);
    }


    public float getMonthlyUsage(){
        return getUsageForPeriod(30);
    }


    private float getUsageForPeriod(int days) {
        if (records == null || records.getRecordsArrayList() == null) return 0;

        float totalUsageForPeriod = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -days);
        Date cutoffDate = calendar.getTime();

        for (Record record : records.getRecordsArrayList()) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                Date recordDate = format.parse(record.getDate());
                if (recordDate != null && recordDate.after(cutoffDate)) {
                    totalUsageForPeriod += record.getUsageMinutes();
                }
            } catch (Exception e) {
                Log.e(TAG,"An unexpected error occurred during calculation of usage for period.");
            }
        }
        return totalUsageForPeriod;
    }

}
