package com.example.everybooks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.everybooks.data.Book;
import com.example.everybooks.data.Memo;
import com.example.everybooks.data.Notification;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class MainActivity extends AppCompatActivity
{
    // 로그인 여부 구별하는 변수
    static boolean isLogin = false;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    ToReadBookAdapter toReadBookAdapter;
    ReadingBookAdapter readingBookAdapter;
    ReadBookAdapter readBookAdapter;
    AllMemoAdapter allMemoAdapter;
    NotificationAdapter notificationAdapter;

    ArrayList<Book> toReadBookList;
    ArrayList<Book> readingBookList;
    ArrayList<Book> readBookList;
    ArrayList<Memo> allMemoList;
    ArrayList<Notification> notiList;

    // fragment 뷰들
    private HomeFragment homeFragment;
    private RecordFragment recordFragment;
    private EtcFragment etcFragment;
    private ProfileLogoutFragment profileLogoutFragment;
    private ProfileFragment profileFragment;
    
    // 뷰 요소 선언
    private BottomNavigationView bottomNavigationView;// 바텀 네비게이션 뷰. 하단 메뉴바
    private Toast toast;

    // 상수 선언
    final int HOME = 0;
    final int RECORD = 1;
    final int ETC = 2;
    final int PROFILE = 3;

    final String TAG = "테스트";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 화면 생성
        setContentView(R.layout.activity_bottom_navi);

        // 뷰 요소 초기화
        bottomNavigationView = findViewById(R.id.bottomNavi);

        // 프래그먼트 객체 생성
        homeFragment = new HomeFragment();
        recordFragment = new RecordFragment();
        etcFragment = new EtcFragment();
        profileLogoutFragment = new ProfileLogoutFragment();
        profileFragment = new ProfileFragment();

        // 첫 프래그먼트 화면을 무엇으로 지정해줄 것인지 선택
        setFragment(HOME);

    }

    @Override
    protected void onResume()
    {
        super.onResume();

        toReadBookList = new ArrayList<>();
        readingBookList = new ArrayList<>();
        readBookList = new ArrayList<>();
        allMemoList = new ArrayList<>();
        notiList = new ArrayList<>();

        // 하단 네비 바 아이템 클릭하면 해당하는 프래그먼트로 변경
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                switch (menuItem.getItemId())
                {
                    case R.id.action_home :
                        setFragment(HOME);
                        break;
                    case R.id.action_record :
                        setFragment(RECORD);
                        break;
                    case R.id.action_etc :
                        setFragment(ETC);
                        break;
                    case R.id.action_profile :
                        setFragment(PROFILE);
                        break;
                }
                return true;
            }
        });

        // 저장되어있는 책 리스트를 리사이클러뷰에서 보여줄 수 있도록 ArrayList<Book> 형태로 변환해서 어댑터에 보내준다.
        try
        {
            // bookInfo 파일에서 bookList 를 문자열의 형태로 가져온다.
            // 문자열의 형태를 ArrayList<Book> 로 변환하기 위해서 JsonArray 형태로 변환한다.
            SharedPreferences bookInfo = getSharedPreferences("bookInfo", MODE_PRIVATE);
            String bookListString = bookInfo.getString("bookList", null);
            Log.d(TAG, "MainActivity, 저장되어있는 책 목록 : " + bookListString);

            if(bookListString != null)
            {
                JSONArray jsonArray = new JSONArray(bookListString);

                // JsonArray 의 길이만큼 반복해서 jsonObject 를 가져오고, Book 객체에 담은 뒤 ArrayList<Book> 에 담는다.
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    int bookId = jsonObject.getInt("bookId");
                    String img = jsonObject.getString("img");
                    String title = jsonObject.getString("title");
                    String writer = jsonObject.getString("writer");
                    String publisher = jsonObject.getString("publisher");
                    String publishDate = jsonObject.getString("publishDate");
                    String insertDate = jsonObject.getString("insertDate");
                    String startDate = jsonObject.getString("startDate");
                    String endDate = jsonObject.getString("endDate");
                    String state = jsonObject.getString("state");
                    String readTime = jsonObject.getString("readTime");
                    int starNum = jsonObject.getInt("starNum");

                    Book book = new Book();
                    book.setBookId(bookId);
                    book.setImg(img);
                    book.setTitle(title);
                    book.setWriter(writer);
                    book.setPublisher(publisher);
                    book.setPublishDate(publishDate);
                    book.setInsertDate(insertDate);
                    book.setStartDate(startDate);
                    book.setEndDate(endDate);
                    book.setState(state);
                    book.setReadTime(readTime);
                    book.setStarNum(starNum);

                    // book 객체의 state 속성에 따라 책의 상태를 구분해서 적절한 ArrayList 에 담는다.
                    if(book.getState().equals("toRead"))
                    {
                        toReadBookList.add(0, book);
                    }
                    else if(book.getState().equals("reading"))
                    {
                        readingBookList.add(0, book);
                    }
                    else if(book.getState().equals("read"))
                    {
                        readBookList.add(0, book);
                    }

                }

                // 책의 상태에 따라 구분해서 담은 책리스트를 적절한 어댑터에 보낸다.
                Log.d(TAG, "MainActivity, 어댑터에 보내는 읽을책.size : " + toReadBookList.size());
                Log.d(TAG, "MainActivity, 어댑터에 보내는 읽는책.size : " + readingBookList.size());
                Log.d(TAG, "MainActivity, 어댑터에 보내는 읽은책.size : " + readBookList.size());

                toReadBookAdapter = new ToReadBookAdapter(getApplicationContext(), toReadBookList);
                toReadBookAdapter.notifyDataSetChanged();
                readingBookAdapter = new ReadingBookAdapter(getApplicationContext(), readingBookList);
                readingBookAdapter.notifyDataSetChanged();
                readBookAdapter = new ReadBookAdapter(getApplicationContext(), readBookList);
                readBookAdapter.notifyDataSetChanged();
            }
        }
        catch (JSONException e)
        {
            System.out.println(e.toString());
        }


        // 저장되어있는 메모 리스트를 리사이클러뷰에서 보여줄 수 있도록 ArrayList<Memo> 형태로 변환해서 어댑터에 보내준다.
        try
        {
            // memoInfo 파일에서 memoList 를 문자열의 형태로 가져온다.
            // 문자열의 형태를 ArrayList<Memo> 로 변환하기 위해서 JsonArray 형태로 변환한다.
            SharedPreferences memoInfo = getSharedPreferences("memoInfo", MODE_PRIVATE);
            String memoListString = memoInfo.getString("memoList", null);
            Log.d(TAG, "MainActivity, 저장되어있는 메모 목록 : " + memoListString);

            if(memoListString != null)
            {
                JSONArray jsonArray = new JSONArray(memoListString);

                // 가져온 jsonArray의 길이만큼 반복해서 jsonObject 를 가져오고, Memo 객체에 담은 뒤 ArrayList<Memo> 에 담는다.
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    Memo memo = new Memo();
                    memo.setMemoId(jsonObject.getInt("memoId"));
                    memo.setBookId(jsonObject.getInt("bookId"));
                    memo.setMemoText(jsonObject.getString("memoText"));
                    memo.setMemoDate(jsonObject.getString("memoDate"));

                    allMemoList.add(0, memo);

                }

                // 어댑터에 보내기
                Log.d(TAG, "MainActivity, 어댑터에 보내는 메모.size : " + allMemoList.size());

                allMemoAdapter = new AllMemoAdapter(getApplicationContext(), allMemoList);
                allMemoAdapter.notifyDataSetChanged();
            }

        }
        catch (JSONException e)
        {
            System.out.println(e.toString());
        }



        /*// 저장되어있는 알림 리스트를 리스트뷰에서 보여줄 수 있도록 ArrayList<Notification> 형태로 변환해서 어댑터에 보내준다.
        try
        {
            SharedPreferences notiInfo = getSharedPreferences("notiInfo", MODE_PRIVATE);
            String notiListString = notiInfo.getString("notiList", null);
            Log.d(TAG, "MainActivity, 저장되어있는 알림 목록 : " + notiListString);

            if(notiListString != null)
            {
                JSONArray jsonArray = new JSONArray(notiListString);

                // 가져온 jsonArray의 길이만큼 반복해서 jsonObject 를 가져오고, Notification 객체에 담은 뒤 ArrayList<Notification> 에 담는다.
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

                Log.d(TAG, "MainActivity, 어댑터에 보내는 알림.size : " + notiList.size());

                notificationAdapter = new NotificationAdapter(getApplicationContext(), notiList);
                notificationAdapter.notifyDataSetChanged();
            }

        }
        catch (JSONException e)
        {
            System.out.println(e.toString());
        }*/
    }

    // 프레그먼트 교체가 일어나는 메소드
    private void setFragment(int fragmentNum)
    {
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        switch (fragmentNum)
        {
            case HOME :
                // HOME 클릭하면 HOME 프래그먼트로 전환
                fragmentTransaction.replace(R.id.main_frame, homeFragment);
                fragmentTransaction.commit();

                break;

            case RECORD :
                // RECORD 클릭하면 RECORD 프래그먼트로 전환
                fragmentTransaction.replace(R.id.main_frame, recordFragment);
                fragmentTransaction.commit();
                break;

            case ETC:
                // ETC 클릭하면 ETC 프래그먼트로 전환
                fragmentTransaction.replace(R.id.main_frame, etcFragment);
                fragmentTransaction.commit();
                break;

            case PROFILE :
                // PROFILE 클릭하면 PROFILE 프래그먼트로 전환

                if(MainActivity.isLogin == true)// 로그인된 상태라면 로그인 된 프로필 창으로 화면 전환
                {
                    fragmentTransaction.replace(R.id.main_frame, profileFragment);
                    fragmentTransaction.commit();
                }
                else    // 로그인 되지 않은 상태라면 로그아웃 상태의 프로필 창으로 화면 전환
                {
                    fragmentTransaction.replace(R.id.main_frame, profileLogoutFragment);
                    fragmentTransaction.commit();
                }
            break;
        }
    }

    // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;

    @Override
    public void onBackPressed()
    {
        // 기존 뒤로가기 버튼의 기능을 막기위해 주석처리한다.
        // super.onBackPressed();

        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지났으면 Toast Show
        // 2000 milliseconds = 2 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2000)
        {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
        // 현재 표시된 Toast 취소
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000)
        {
            finish();
            toast.cancel();
        }
    }

    // 새로고침
    public void refresh()
    {
        toReadBookAdapter.notifyDataSetChanged();
    }

}