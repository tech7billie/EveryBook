package com.example.everybooks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class HomeFragment extends Fragment
{
    Intent intent;

    // 뷰 요소 선언
    private View view;

    Button button_to_read;
    Button button_reading;
    Button button_read;
    androidx.appcompat.widget.SearchView searchView_search;
    Spinner spinner_order;
    ImageView imageView_mic;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        // 화면 생성
        view = inflater.inflate(R.layout.fragment_home, container, false);

        //처음 childFragment 지정
        getChildFragmentManager().beginTransaction().add(R.id.home_frame, new ToReadFragment()).commit();

        // 뷰 요소 초기화
        button_to_read = view.findViewById(R.id.btn_to_read);
        button_reading =  view.findViewById(R.id.btn_reading);
        button_read = view.findViewById(R.id.btn_read);
        searchView_search = view.findViewById(R.id.search);
        spinner_order = view.findViewById(R.id.spinner_order);
        imageView_mic = view.findViewById(R.id.mic);

        return view;

    }//end onCreateView


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        // 각각의 버튼을 클릭하면 해당하는 리스트를 보여줄 수 있도록
        // to_read 클릭 이벤트
        button_to_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getChildFragmentManager().beginTransaction().replace(R.id.home_frame, new ToReadFragment()).commit();
            }
        });


        // reading 클릭 이벤트
        button_reading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getChildFragmentManager().beginTransaction().replace(R.id.home_frame, new ReadingFragment()).commit();
            }
        });

        // read 클릭 이벤트
        button_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getChildFragmentManager().beginTransaction().replace(R.id.home_frame, new ReadFragment()).commit();

            }
        });

        // 검색창 엔터 이벤트 등록
        // 검색창에 검색할 키워드를 입력하고 엔터를 누르면 인텐트에 키워드를 담아 검색 결과화면으로 이동한다.
        searchView_search.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                intent = new Intent(getContext(), SearchBookActivity.class);
                intent.putExtra("keyword", query);
                startActivity(intent);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();



    }
}