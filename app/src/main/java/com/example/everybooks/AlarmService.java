package com.example.everybooks;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class AlarmService extends Service
{
    String TAG = "테스트";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "AlarmService 알람 서비스 ");

        // test
        SharedPreferences notiInfo = getSharedPreferences("notiInfo", MODE_PRIVATE);
        String alarmText = notiInfo.getString("alarmText", null);

        Intent alarmIntent = new Intent(getApplicationContext(), AlarmActivity.class);

        if(alarmText != null)
        {
            alarmIntent.putExtra("alarmText", alarmText);
        }

        startActivity(alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        return super.onStartCommand(intent, flags, startId);
    }

}
