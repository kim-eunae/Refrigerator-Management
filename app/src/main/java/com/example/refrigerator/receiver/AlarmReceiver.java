package com.example.refrigerator.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.refrigerator.service.AlarmService;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, AlarmService.class);
        serviceIntent.putExtra("alarm_no", intent.getIntExtra("alarm_no", 1));
        serviceIntent.putExtra("message", intent.getStringExtra("message"));

        // Oreo 버전 이후부터는 Background 에서 실행을 금지하기 때문에 Foreground 에서 실행해야 함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Foreground 로 서비스 실행
            context.startForegroundService(serviceIntent);
        } else {
            // Background 로 서비스 실행
            context.startService(serviceIntent);
        }
    }
}
