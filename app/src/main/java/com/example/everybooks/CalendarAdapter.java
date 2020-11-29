package com.example.everybooks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

public class CalendarAdapter extends BaseAdapter
{
    // todo static 수정하기
    static ArrayList<Book> readBookList = ReadBookAdapter.readBookList;

    @Override
    public int getCount() {
        return readBookList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return readBookList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

       final int index = position;
       final Context context = parent.getContext();

        // item_memo 레이아웃을 inflate 하여 convertView 참조 획득
        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_calendar_book, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate 된)으로부터 위젯에 대한 참조 획득
        ImageView imageView_img = convertView.findViewById(R.id.img);
        TextView textView_title = convertView.findViewById(R.id.title);
        TextView textView_writer = convertView.findViewById(R.id.writer);
        TextView textView_publisher = convertView.findViewById(R.id.publisher);
        TextView textView_publish_date = convertView.findViewById(R.id.publish_date);

        // 책 하나 얻기
        Book book = readBookList.get(position);

        // 뷰 요소에 나타내주기
        //imageView_img

        textView_title.setText(book.getTitle());
        textView_writer.setText(book.getWriter());
        textView_publisher.setText(book.getPublisher());
        textView_publish_date.setText(book.getPublishDate());

        return convertView;
    }
}