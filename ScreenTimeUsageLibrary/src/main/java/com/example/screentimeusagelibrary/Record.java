package com.example.screentimeusagelibrary;

public class Record {
    private String date;
    private float usageMinutes;

    public Record() {
    }


    public String getDate() {
        return date;
    }


    public Record setDate(String date) {
        this.date = date;
        return this;
    }


    public Record setUsageMinutes(float usageMinutes) {
        this.usageMinutes = usageMinutes;
        return this;
    }


    public Record addMinutes(float min){
        this.usageMinutes += min;
        return this;
    }


    public float getUsageMinutes() {
        return usageMinutes;
    }


    public boolean equals(Record other){
        return other.getDate().equals(this.date);
    }


    @Override
    public String toString() {
        return "Record{" +
                "date='" + date + '\'' +
                ", usageMinutes=" + usageMinutes +
                '}';
    }
}
