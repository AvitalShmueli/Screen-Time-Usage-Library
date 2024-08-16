package com.example.screentimeusagelibrary;

public interface TimeLimitCallback {
    void onTimeEnds(float usageMinutes);
    void onUsageTimeUpdated(float usageMinutes);
}
