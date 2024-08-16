package com.example.screentimeusagelibrary;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RecordsList {
    public static final String RECORDS_TABLE = "RECORDS";

    private String listName = "";
    private ArrayList<Record> recordsArrayList = new ArrayList<>();

    public RecordsList() {
    }


    public String getListName() {
        return listName;
    }


    public RecordsList setListName(String listName) {
        this.listName = listName;
        return this;
    }


    public ArrayList<Record> getRecordsArrayList() {
        return recordsArrayList;
    }


    public RecordsList setRecordsArrayList(ArrayList<Record> recordsArrayList) {
        this.recordsArrayList = recordsArrayList;
        return this;
    }


    public RecordsList setRecordArrayList(ArrayList<Record> recordsArrayList) {
        this.recordsArrayList = recordsArrayList;
        return this;
    }


    public RecordsList add(Record record) {
        this.recordsArrayList.add(record);
        return this;
    }


    public int getSize(){
        return recordsArrayList.size();
    }


    public Record get(int position){
        return recordsArrayList.get(position);
    }


    public Record getTodayRecord() {
        int todayRecordIndex = getIndexOfToday();
        if(todayRecordIndex != -1)
            return recordsArrayList.get(todayRecordIndex);
        return null;
    }


    public int getIndexOfToday(){
       String today = getTodayDateFormatted();
        if(recordsArrayList != null) {
            for (int i = 0; i < recordsArrayList.size(); i++) {
                if (recordsArrayList.get(i).getDate().equals(today))
                    return i;
            }
            return -1;
        }
        return -1;
    }


    private String getTodayDateFormatted(){
        Date todayDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String today = dateFormat.format(todayDate);
        Log.d("TEST TIME RecordsList", today);
        return  today;
    }


    @Override
    public String toString() {
        return "RecordsList{" +
                "listName='" + listName + '\'' +
                ", recordsArrayList=" + recordsArrayList +
                '}';
    }
}
