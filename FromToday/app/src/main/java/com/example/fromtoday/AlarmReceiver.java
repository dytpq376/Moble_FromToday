package com.example.fromtoday;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent in = new Intent(context, StepService.class);
            context.startForegroundService(in);
        }else {
            Intent in = new Intent(context, StepService.class);
            context.startService(in);
        }
    }
}