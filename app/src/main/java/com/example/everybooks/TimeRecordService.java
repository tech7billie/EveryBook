package com.example.everybooks;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONObject;

public class TimeRecordService extends Service
{
    int bookId;
    String readTime;

    final String TAG = "테스트";

    int hour;
    int minute;
    int second;
    boolean isStart;

    int timeInSeconds;
    Thread timeThread;
    Intent intent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d(TAG, "TimeRecordService, onCreate()");

    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId )
    {
        NotificationCompat.Builder builder;

        if( Build.VERSION.SDK_INT >= 26 )
        {
            String CHANNEL_ID = "channel_id";
            NotificationChannel clsChannel = new NotificationChannel( CHANNEL_ID, "서비스 앱", NotificationManager.IMPORTANCE_DEFAULT );
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel( clsChannel );

            builder = new NotificationCompat.Builder(this, CHANNEL_ID );
        }
        else
        {
            builder = new NotificationCompat.Builder(this );
        }

        if(intent == null)
        {
            return Service.START_STICKY;
        }
        else
        {
            // 문자열의 형태로 저장되어있는 readTime 의 형식을 시, 분, 초로 나눈다.
            // 스레드를 사용해 1초당 1씩 초가 증가하도록 한다.

            readTime = intent.getStringExtra("readTime");
            bookId = intent.getIntExtra("bookId", -1);

            Log.d(TAG, "TimeRecordService 에서 받은 readTime" + readTime );

            String[] hourMinuteSecond = readTime.split(":");
            hour = Integer.parseInt(hourMinuteSecond[0]);
            minute = Integer.parseInt(hourMinuteSecond[1]);
            second = Integer.parseInt(hourMinuteSecond[2]);

            Log.d(TAG, "TimeRecordService, readTime 에서 시분초 int로 가져오기 : " + hour + minute + second);

            Log.d(TAG, "TimeRecordService, 스레드 실행 전 isStart : " + isStart);

            // 초 단위로 바꾸기
            timeInSeconds = hour * 60 * 60 + minute * 60 + second;

            Log.d(TAG, " 저장되어있던 초단위 시간 : " + timeInSeconds);

            // 1초 씩 증가하는 스레드 시작
            if(timeThread == null)
            {
                TimeRecordThread timeRecordThread = new TimeRecordThread();
                timeThread = new Thread(timeRecordThread);
                timeThread.start();
            }
        }

        intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 101, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        builder.setSmallIcon( R.drawable.ic_etc_black_24dp)
                .setContentTitle( "EveryBook" ).setContentText( "독서 시간을 기록중입니다 ! " )
                .setContentIntent( pendingIntent );

        // foreground 서비스로 실행한다.
        startForeground( 1, builder.build() );

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // 스레드가 생성되지 않은 상황에서는 stop 버튼을 눌러도 코드가 동작하지 않도록 하기 위해서
        // thread 가 null 이 아닐 때만 코드 동작하도록 한다.
        if (timeThread != null) {
            // stop 버튼 클릭하면 1초씩 증가하는 스레드를 멈추고 현재 독서 시간을 저장한다.
            timeThread.interrupt();

            SharedPreferences bookInfo = getSharedPreferences("bookInfo", MODE_PRIVATE);
            String bookListString = bookInfo.getString("bookList", null);

            try {
                JSONArray jsonArray = new JSONArray(bookListString);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    if (bookId == jsonObject.getInt("bookId"))
                    {
                        jsonObject.put("readTime", readTime);
                        Log.d(TAG, "TimeRecordService, onDestroy 후 저장할 readTime : " + readTime);
                    }
                }

                SharedPreferences.Editor editor = bookInfo.edit();
                editor.putString("bookList", jsonArray.toString());
                editor.commit();

                Log.d(TAG, "TimeRecordService, onDestroy 후 저장된 readTime : " + readTime);

            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }

   /* // 강제 종료 시점
    public void onTaskRemoved(Intent rootIntent)
    {
        // 스레드가 생성되지 않은 상황에서는 강제종료 시에도 코드가 동작하지 않도록 하기 위해서
        // thread 가 null 이 아닐 때만 코드 동작하도록 한다.
        if (timeThread != null) {
            // 1초씩 증가하는 스레드를 멈추고 현재 독서 시간을 저장한다.
            timeThread.interrupt();
            timeThread = null;

            SharedPreferences bookInfo = getSharedPreferences("bookInfo", MODE_PRIVATE);
            String bookListString = bookInfo.getString("bookList", null);

            try {
                JSONArray jsonArray = new JSONArray(bookListString);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    if (bookId == jsonObject.getInt("bookId"))
                    {
                        jsonObject.put("readTime", readTime);
                    }
                }

                SharedPreferences.Editor editor = bookInfo.edit();
                editor.putString("bookList", jsonArray.toString());
                editor.commit();

                Log.d(TAG, "TimeRecordService, 강제종료 후 저장된 readTime : " + readTime);

            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }*/


    /* @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 기존
        // 시스템에 의해 서비스가 자동으로 다시 시작될 수 있기 때문에 intent 객체 또한 null 값 일수 있다.
        // 그러므로 intent 객체가 null 인지 먼저 체크한 후 null 값일 경우 Service.START_STICKY 를 리턴한다.
        // 이 값을 반환하면 서비스가 비정상 종료되어도 시스템이 자동으로 재시작될 수 있다.
        if(intent == null)
        {
            return Service.START_STICKY;
        }
        else
        {

            // 문자열의 형태로 저장되어있는 readTime 의 형식을 시, 분, 초로 나눈다.
            // 스레드를 사용해 1초당 1씩 초가 증가하도록 한다.

            readTime = intent.getStringExtra("readTime");
            bookId = intent.getIntExtra("bookId", -1);

            Log.d(TAG, "TimeRecordService 에서 받은 readTime" + readTime );

            String[] hourMinuteSecond = readTime.split(":");
            hour = Integer.parseInt(hourMinuteSecond[0]);
            minute = Integer.parseInt(hourMinuteSecond[1]);
            second = Integer.parseInt(hourMinuteSecond[2]);

            Log.d(TAG, "TimeRecordService, readTime 에서 시분초 int로 가져오기 : " + hour + minute + second);

            Log.d(TAG, "TimeRecordService, 스레드 실행 전 isStart : " + isStart);

            // 초 단위로 바꾸기
            timeInSeconds = hour * 60 * 60 + minute * 60 + second;

            Log.d(TAG, " 저장되어있던 초단위 시간 : " + timeInSeconds);

            // 1초 씩 증가하는 스레드 시작
            if(timeThread == null)
            {
                TimeRecordThread timeRecordThread = new TimeRecordThread();
                timeThread = new Thread(timeRecordThread);
                timeThread.start();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }*/

  /* @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "TimeRecordService, onDestroy");

        // 스레드가 생성되지 않은 상황에서는 stop 버튼을 눌러도 코드가 동작하지 않도록 하기 위해서
        // thread 가 null 이 아닐 때만 코드 동작하도록 한다.
        if (timeThread != null) {
            // stop 버튼 클릭하면 1초씩 증가하는 스레드를 멈추고 현재 독서 시간을 저장한다.
            timeThread.interrupt();

            SharedPreferences bookInfo = getSharedPreferences("bookInfo", MODE_PRIVATE);
            String bookListString = bookInfo.getString("bookList", null);

            try {
                JSONArray jsonArray = new JSONArray(bookListString);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    if (bookId == jsonObject.getInt("bookId"))
                    {
                        jsonObject.put("readTime", readTime);
                        Log.d(TAG, "TimeRecordService, onDestroy 후 저장할 readTime : " + readTime);
                    }
                }

                SharedPreferences.Editor editor = bookInfo.edit();
                editor.putString("bookList", jsonArray.toString());
                editor.commit();

                Log.d(TAG, "TimeRecordService, onDestroy 후 저장된 readTime : " + readTime);

            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        this.stopSelf();
    }


    // 강제 종료 되는 시점
    public void onTaskRemoved(Intent rootIntent)
    {
        // 스레드가 생성되지 않은 상황에서는 강제종료 시에도 코드가 동작하지 않도록 하기 위해서
        // thread 가 null 이 아닐 때만 코드 동작하도록 한다.
        if (timeThread != null) {
            // 1초씩 증가하는 스레드를 멈추고 현재 독서 시간을 저장한다.
            timeThread.interrupt();
            timeThread = null;

            SharedPreferences bookInfo = getSharedPreferences("bookInfo", MODE_PRIVATE);
            String bookListString = bookInfo.getString("bookList", null);

            try {
                JSONArray jsonArray = new JSONArray(bookListString);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    if (bookId == jsonObject.getInt("bookId"))
                    {
                        jsonObject.put("readTime", readTime);
                    }
                }

                SharedPreferences.Editor editor = bookInfo.edit();
                editor.putString("bookList", jsonArray.toString());
                editor.commit();

                Log.d(TAG, "TimeRecordService, 강제종료 후 저장된 readTime : " + readTime);

            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }

        stopSelf(); //서비스 종료

    }*/

    // 타임핸들러 클래스
    Handler timeHandler = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            // 총 누적 초를 받아온 뒤
            timeInSeconds = msg.arg1;

            // 시, 분, 초에 맞게 계산한다.
            int seconds = timeInSeconds;
            int minutes = seconds / 60;
            seconds = seconds % 60;
            int hours = minutes / 60;
            minutes = minutes % 60;

            // 형식에 맞춰 문자열로 바꿔준다.
            readTime = "" + String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);

            // 전환한 화면에서보여주기 위해서 누적 독서시간을 나타내는 문자열과 어떤 책인지 알려주는 bookId 를 인텐트에 담아 TimeRecordActivity 로 화면을 전환한다.
            intent = new Intent(getApplicationContext(), TimeRecordActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("readTime",readTime);
            intent.putExtra("bookId", bookId);
            startActivity(intent);
        }
    };

    // 타임 스레드 클래스
    class TimeRecordThread implements Runnable {

        boolean running = false;

        public void run()
        {
            running = true;
            while (running)
            {
                // 총 누적시간에 1을 더한다.
                timeInSeconds += 1;
                Message message = timeHandler.obtainMessage();
                message.arg1 =  timeInSeconds;
                timeHandler.sendMessage(message);

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    return;
                }
            }
        }
    }

}
