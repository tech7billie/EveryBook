package com.example.everybooks;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.everybooks.data.Notification;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class AddNotificationActivity extends AppCompatActivity
{
    // 뷰 요소 선언
    TextView textView_save;
    TextView textView_time;
    EditText notification_text;

    int notiId;
    int hour;
    int min;
    String[] days;

    String notiListString;

    JSONArray jsonArray;
    JSONObject jsonObject;

    SharedPreferences notiInfo;
    SharedPreferences.Editor editor;

    ArrayList<Notification> notiList;

    final String TAG = "테스트";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        // 화면 생성
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notification);

        // 뷰 요소 초기화
        textView_save = findViewById(R.id.save);
        textView_time = findViewById(R.id.time);
        notification_text = findViewById(R.id.notification_text);

        // 각 요소를 클릭하면 수행할 동작 지정해두기
        View.OnClickListener click = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.save:
                        // Save 클릭하면 사용자에게 입력받은 데이터를 알림 리스트에 저장한다.
                        notiList = new ArrayList<>();

                        notiInfo = getSharedPreferences("notiInfo", MODE_PRIVATE);
                        notiId = notiInfo.getInt("notiId", 0);

                        Notification noti = new Notification();
                        noti.setNotiId(notiId);
                        noti.setHour(hour);
                        noti.setMinute(min);
                        noti.setText(notification_text.getText().toString());
                        notiList.add(0, noti);

                        jsonObject = noti.toJSON();

                        notiListString  = notiInfo.getString("notiList", null);
                        editor = notiInfo.edit();

                        try
                        {
                            // 저장된 값이 있을 때
                            if(notiListString != null)
                            {
                                jsonArray = new JSONArray(notiListString);
                                //Log.d(TAG, "저장되어 있던 JsonArray 길이 : " + jsonArray.length());

                                jsonArray.put(jsonObject);

                                notiListString = jsonArray.toString();
                                editor.putString("notiList", notiListString);
                                editor.commit();

                                Log.d(TAG, "하나 추가한 뒤 JsonArray 길이 : " + jsonArray.length());

                            }
                            else
                            {
                                // 처음 저장할 때
                                jsonArray = new JSONArray();
                                jsonArray.put(jsonObject);

                                notiListString = jsonArray.toString();
                                editor.putString("notiList", notiListString);
                                editor.commit();
                                Log.d(TAG, "하나 추가한 뒤 JsonArray 길이 : " + jsonArray.length());
                            }

                        }
                        catch (Exception e)
                        {
                            System.out.println(e.toString());
                        }

                        // 알림을 구분하기 위해 저장된 알림의 notiId 가 겹치지 않도록 notiInfo 에 저장된 notiId의 값을 1 증가시킨다.
                        SharedPreferences.Editor editor = notiInfo.edit();
                        editor.putInt("notiId", notiId + 1);
                        editor.commit();

                        NotificationAdapter notificationAdapter = new NotificationAdapter(getApplicationContext(), notiList);
                        notificationAdapter.notifyDataSetChanged();

                        finish();

                        break;
                    case R.id.time :
                        // 시간 클릭하면 타임피커 다이얼로그가 등장해서 설정할 시간을 입력받는다.
                        dialogTimePicker();
                }
            }
        };

        // 각 요소가 클릭되면
        textView_save.setOnClickListener(click);
        textView_time.setOnClickListener(click);

        // 초기 시간을 현재 시간으로 보여준다.
        Calendar cal = Calendar.getInstance();
        hour = cal.HOUR_OF_DAY;
        min = cal.MINUTE;

        textView_time.setText(hour + ":" + min);

    }

    @Override
    protected void onResume()
    {
        super.onResume();

       /* // 저장되어있는 알림리스트 어댑터에 보내주기
        try
        {
            SharedPreferences notiInfo = getSharedPreferences("notiInfo", MODE_PRIVATE);
            String notiListString = notiInfo.getString("notiList", null);
            Log.d(TAG, "MainActivity, 저장되어있는 알림 목록 : " + notiListString);

            if(notiListString != null)
            {
                JSONArray jsonArray = new JSONArray(notiListString);

                // 가져온 jsonArray의 길이만큼 반복해서 jsonObject 를 가져오고, Book 객체에 담은 뒤 ArrayList<Book> 에 담는다.
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    Notification noti = new Notification();
                    noti.setNotiId(jsonObject.getInt("notiId"));
                    noti.setHour(jsonObject.getInt("hour"));
                    noti.setMinute(jsonObject.getInt("minute"));
                    noti.setText(jsonObject.getString("text"));

                    notiList.add(0, noti);

                }

                //어댑터에 보내기
                Log.d(TAG, "MainActivity, 어댑터에 보내는 알림.size : " + notiList.size());

                NotificationAdapter notificationAdapter = new NotificationAdapter(getApplicationContext(), notiList);
                notificationAdapter.notifyDataSetChanged();

            }

        }
        catch (Exception e)
        {
            System.out.println(e.toString());
        }
*/
    }

    private void dialogTimePicker(){
        TimePickerDialog.OnTimeSetListener mTimeSetListener =
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        textView_time.setText(hourOfDay +":"+minute);
                        hour = hourOfDay;
                        min = minute;
                    }
                };

        //현재 시간으로 타임피커 초기값을 설정한다.
        Calendar cal = Calendar.getInstance();
        TimePickerDialog alert = new TimePickerDialog(this,
                mTimeSetListener, cal.HOUR_OF_DAY, cal.MINUTE, true);
        alert.show();
    }


}
