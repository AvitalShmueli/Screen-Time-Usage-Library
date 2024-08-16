package com.example.screentimeusagelibrary;

public interface TimeLimitCallback {
    void onTimeEnds();
    void onUsageTimeUpdated(float usageMinutes);
}
