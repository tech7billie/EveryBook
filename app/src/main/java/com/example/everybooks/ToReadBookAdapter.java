package com.example.everybooks;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.everybooks.data.Book;
import com.example.everybooks.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class ToReadBookAdapter extends RecyclerView.Adapter<ToReadBookAdapter.BookViewHolder>
{
    private int position;
    Book book;
    Context context;
    final String TAG = "테스트";

    Intent intent;

    // static 지우면 안된다.
    static ArrayList<Book> toReadBookList = new ArrayList<>();

    public ToReadBookAdapter(){}

    public ToReadBookAdapter(Context context)
    {
        this.context = context;
    }

    public ToReadBookAdapter(Context context, ArrayList<Book> toReadBookList)
    {
        this.context = context;
        this.toReadBookList = toReadBookList;
    }

    public class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView_img;
        TextView textView_title;
        TextView textView_insert_date;

        BookViewHolder(View itemView)
        {
            super(itemView) ;

           // 뷰 요소 초기화
            imageView_img = itemView.findViewById(R.id.img);
            textView_title = itemView.findViewById(R.id.title);
            textView_insert_date = itemView.findViewById(R.id.insert_date);

            // 각각의 아이템을 클릭하면 책 아이디를 가지고 책 정보수정 화면으로 전환한다.
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {

                        book = getItem(position);
                        Intent intent = new Intent(v.getContext(), EditBookInfoActivity.class);
                        intent.putExtra("bookId", book.getBookId());
                        v.getContext().startActivity(intent);

                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION)
                    {
                        book = getItem(position);
                    }

                    //현재 년도, 월, 일을 책 등록일에 저장한다.
                    Calendar cal = Calendar.getInstance();
                    int year = cal.get ( cal.YEAR );
                    int month = cal.get ( cal.MONTH ) + 1 ;
                    int date = cal.get ( cal.DATE ) ;

                    String today = year + "." + month + "." + date;

                    //AlertDialog.Builder builder = new AlertDialog.Builder(ToReadBookAdapter.this);
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setMessage("독서를 시작할까요?");
                    builder.setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // 해당하는 책을 찾아서 state 를 reading으로 바꾸고 startDate 에 값을 입력한다.
                                SharedPreferences bookInfo = context.getSharedPreferences("bookInfo", Context.MODE_PRIVATE);
                                String bookListString= bookInfo.getString("bookList", null);


                                Log.d(TAG,"읽을 책 → 읽는 책 ToReadBookAdapter 에서 책 리스트 : " + bookListString);
                                //Log.d(TAG, "클릭한 책의 bookId : " + book.getBookId() );

                                try
                                {
                                    JSONArray jsonArray = new JSONArray(bookListString);
                                    for (int i = 0; i < jsonArray.length() ; i++)
                                    {
                                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                        if(book.getBookId() == jsonObject.getInt("bookId"))
                                        {
                                            jsonObject.put("state", "reading");
                                            jsonObject.put("startDate", today);
                                        }
                                    }

                                    SharedPreferences.Editor editor = bookInfo.edit();
                                    editor.putString("bookList", jsonArray.toString());
                                    editor.commit();

                                }
                                catch (Exception e)
                                {
                                    System.out.println(e.toString());
                                }

                                position = getAdapterPosition();
                                Book book = getItem(position);

                                ReadingBookAdapter readingBookAdapter = new ReadingBookAdapter();
                                readingBookAdapter.addItem(book);

                                removeItem(position);

                                dialog.dismiss();

                                //test
                                intent = new Intent(v.getContext(), MainActivity.class);
                                v.getContext().startActivity(intent);

                                Log.d(TAG, "읽을 책 어댑터에서 메인 액티비티로 전환 ");

                            }
                        });
                    builder.setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // 취소 클릭했을 때
                                Toast.makeText(v.getContext(), "취소" ,Toast.LENGTH_SHORT).show();
                            }
                        });

                    builder.show();
                    return true; // 롱클릭 이벤트 이후 클릭이벤트 발생하지 않도록 true 반환
                }
            });
        }
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴하는 메소드
    @Override
    public ToReadBookAdapter.BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_to_read_book, parent, false);

        return new BookViewHolder(view);
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시하는 메소드
    @Override
    public void onBindViewHolder(ToReadBookAdapter.BookViewHolder holder, int position) {

        //Log.d(TAG,"ToReadAdapter, toReadBookList.size : " + toReadBookList.size() );

        Book book = toReadBookList.get(position);

        // Log.d(TAG, "ToReadBookAdapter, 받아온 문자열 이미지 : " + book.getImg());
        // 문자열 이미지 비트맵으로 변환
        Util util = new Util();
        Bitmap bitmap = util.stringToBitmap(book.getImg());
        holder.imageView_img.setImageBitmap(bitmap);
        holder.textView_title.setText(book.getTitle());
        holder.textView_insert_date.setText(book.getInsertDate());

    }

    // getItemCount() - 전체 데이터 갯수 리턴하는 메소드
    @Override
    public int getItemCount() {
        return toReadBookList.size() ;
    }

    public int getPosition(int position)
    {
        return position;
    }

    public void setPosition(int position)
    {
        this.position = position;
    }

    // 아이템 추가 메소드
    public void addItem(Book book)
    {
        //현재 년도, 월, 일을 책 등록일에 저장한다.
        Calendar cal = Calendar.getInstance();

        int year = cal.get ( cal.YEAR );
        int month = cal.get ( cal.MONTH ) + 1 ;
        int date = cal.get ( cal.DATE ) ;

        String today = year + "." + month + "." + date;
        book.setInsertDate(today);
        book.setState("toRead");
        toReadBookList.add(0,book);
        notifyItemInserted(0);
    }

    // 아이템 삭제 메소드
    public void removeItem(int position)
    {
        toReadBookList.remove(position);
        notifyItemRemoved(position);
    }

    // 아이템 가져오는 메소드
    public Book getItem(int position) {
        return toReadBookList.get(position);
    }


}
