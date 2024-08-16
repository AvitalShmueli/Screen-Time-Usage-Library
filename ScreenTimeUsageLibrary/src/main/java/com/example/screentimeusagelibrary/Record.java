package com.example.screentimeusagelibrary;

public class Record {
    private String date;
    private long usageMilliseconds;

    public Record() {
    }


    public String getDate() {
        return date;
    }


    public Record setDate(String date) {
        this.date = date;
        return this;
    }


    public Record setUsageMilliseconds(long usageMilliseconds) {
        this.usageMilliseconds = usageMilliseconds;
        return this;
    }


    public Record addMillseconds(long milliseconds){
        this.usageMilliseconds += milliseconds;
        return this;
    }


    public long getUsageMilliseconds() {
        return usageMilliseconds;
    }


    public boolean equals(Record other){
        return other.getDate().equals(this.date);
    }


    @Override
    public String toString() {
        return "Record{" +
                "date='" + date + '\'' +
                ", usageMilliseconds=" + usageMilliseconds +
                '}';
    }
}
