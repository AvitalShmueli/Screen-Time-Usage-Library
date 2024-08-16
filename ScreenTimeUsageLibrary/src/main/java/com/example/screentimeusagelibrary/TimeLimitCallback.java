package com.example.screentimeusagelibrary;

public interface TimeLimitCallback {
    void onTimeEnds(long milliseconds);
    void onUsageTimeUpdated(long milliseconds);
}
