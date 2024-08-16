
# Screen-Time-Usage-Library

Final project for Mobile Security course, in Afeka - the academic college  
of Engineering in Tel Aviv.

## Overview

A library for monitoring application usage time. Daily usage times are saved as logs in Shared Preferences. 
With the library's help, it will be possible to identify user inactivity (for example, that the application is open without interaction from the user) 
or overuse and limit time. In addition, there is a dialog that allows you to extend the usage time by X minutes or dismiss the app and lock other actions with a customized callback.

<div class="row">
    <img src="https://github.com/user-attachments/assets/8ef6a65a-61d7-44a0-90eb-ed4a27a3691d" alt="time_limit" style="height:500px;"/>
    <img src="https://github.com/user-attachments/assets/ea248006-3c54-4c61-8073-4bff99d39970" alt="usage_time_update" style="height:500px;"/>
    <img src="https://github.com/user-attachments/assets/d476a1d0-2352-4adf-9435-a5c2700dd316" alt="timeout" style="height:500px;"/>
    <img src="https://github.com/user-attachments/assets/c499b251-8ee7-46d5-a4a2-daef83d847d5" alt="after_dismiss" style="height:500px;"/>
 </div>


## Usage
#### ScreenTimeUsageLibrary Constructor:
The activity should extend MonitoredActivity. 
In OnCreate() you can modify the parameters of the ScreenTimeUsage. 
In order to change the time limit and the timeout interval you need to use the following functions before calling the super.onCreate(savedInstanceState)
```java
setTimeLimit_minutes(timeLimit_minutes);
setTimeout_minutes(timeout_minutes);
```

#### Methods & Other attributes
In order to adjust the parameters of ScreenTimeUsage, first call to getScreenTimeUsage().
You can adjust the texts in the title, body and buttons of the alert dialog using the following functions:
```java
setDialogTimeLimitTitle(String title)
setDialogTimeLimitBody(String body)
setDialogTimeLimitButtonNameDismiss(String btnName)
setDialogTimeLimitButtonNameMoreTime(String btnName)

setDialogTimeoutTitle(String title)
setDialogTimeoutBody(String body)
setDialogTimeoutButtonNameDismiss(String btnName)
setDialogTimeoutButtonNameMoreTime(String btnName)

setExtraTime(long milliseconds)
```

You can set the setTimeLimitCallback callback in order to define the actions that will occur when time ends.
```java
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
```

You can get aggregation of the usage records daily, weekly or monthly.
```java
getScreenTimeUsage().getDailyUsage()
getScreenTimeUsage().getWeeklyUsage()
getScreenTimeUsage().getMonthlyUsage()
```
