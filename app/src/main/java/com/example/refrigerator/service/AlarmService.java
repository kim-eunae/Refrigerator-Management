package com.example.refrigerator.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.refrigerator.IntroActivity;
import com.example.refrigerator.R;
import com.example.refrigerator.util.Constants;
import com.example.refrigerator.util.SharedPreferencesUtils;

public class AlarmService extends Service {
    private static final String TAG = AlarmService.class.getSimpleName();

    public AlarmService() {}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Oreo(26) 버전 이후 부터 필요한 항목
        // 상태 바에 알림
        // 알림의 id는 0이 아니어야합니다.

        int alarmNo = intent.getIntExtra("alarm_no", 1);

        // 식품 + 유통기한
        String message = intent.getStringExtra("message");
        if (TextUtils.isEmpty(message)) {
            message = "?의 생명이 ?일 남았습니다!";
        }

        Log.d(TAG, "alarmNo:" + alarmNo);
        Log.d(TAG, "message:" + message);

        // 알림을 클릭하면 IntroActivity 호출
        Intent appIntent = new Intent(this, IntroActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, alarmNo, appIntent, PendingIntent.FLAG_ONE_SHOT);

        // 채널 ID
        String channelId = "Refrigerator";

        // 기본 사운드 사용
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // 알림 Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_food_apple_24_white)
                .setContentTitle("유통기한 임박")
                .setContentText(message)
                .setSound(soundUri)
                .setAutoCancel(true)
                //.setOngoing(true)       // 사용자가 지우지 못하게 막기
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Oreo(26) 버전 이후 버전부터는 channel 이 필요함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 채널 생성
            NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
            if (channel == null) {
                channel = new NotificationChannel(channelId, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }

            // 알림 표시
            startForeground(alarmNo, builder.build());
            stopForeground(false);      // false 해야지 알림 선택시 알림이 지워짐 (시간이 지나면 자동으로 알림이 삭제됨)
        } else {
            // 알림 표시
            notificationManager.notify(alarmNo, builder.build());
        }

        // 진동 여부
        boolean vibration = SharedPreferencesUtils.getInstance(this).get(Constants.SharedPreferencesName.ALARM_VIBRATION, false);
        // 진동이면
        if (vibration) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(500);
            }
        }

        // 시스템에 의해 강제 종료되어도 Service 가 재시작 하지 않음
        return START_NOT_STICKY;
    }
}
