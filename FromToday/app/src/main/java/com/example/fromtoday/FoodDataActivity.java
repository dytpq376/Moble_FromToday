package com.example.fromtoday;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class FoodDataActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {
    //    public static String getEmail() {
//        return email;
//    }
//
//    public static void setEmail(String email) {
//        FoodDataActivity.email = email;
//    }
//
//    public static String email;
    private String email;
    //리스트뷰
    private ListView DBListView;
    private ListView userListView;
    //추가
    private ImageView add;
    private ImageView useradd;
    private ImageView userdelete;
    private ImageView back;
    //editText
    private EditText editfood;
    private EditText editcalrorie;

    //scrollview
    private ScrollView userscroll;
    private ScrollView dbscroll;

    //내장 DB 사이즈
    final static int DB_SIZE = 50;
    //내장 DB
    public List<ResultDB> rtList;
    //데이터베이스 food data 를 담을 ArrayList
    public List<String> dbList = new ArrayList<String>();
    public List<String> userList = new ArrayList<>();

    ArrayList<String>UserFood = new ArrayList<>();
    ArrayList<String>UserCalrorie=new ArrayList<>();

    ArrayAdapter userAdapter;

    SharedPreferences pref;
    SharedPreferences currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("김성원","여기는onCreate");
        setContentView(R.layout.dblist);

        dbscroll=findViewById(R.id.dbscroll);
        userscroll=findViewById(R.id.userscroll);
        DBListView = findViewById(R.id.DBListView);
        editfood = findViewById(R.id.userfood);
        editcalrorie=findViewById(R.id.usercalrorie);
        userListView=findViewById(R.id.UserListView);
        useradd = findViewById(R.id.useradd);
        add = findViewById(R.id.add);
        userdelete=findViewById(R.id.userdelete);
        back=findViewById(R.id.back);

        //main currentUser 저장소
        currentUser = getSharedPreferences("currentUser",MODE_PRIVATE);
        email = currentUser.getString("email",null);

        //DBListView의 adapter 초기화
        setDBListViewAdapter();

        //UserListview의 adapter 초기화
        setUserListViewAdapter();

        //db listview, user listbiew scroll처리
        userListView.setOnTouchListener(this);
        DBListView.setOnTouchListener(this);

        useradd.setOnClickListener(this);
        add.setOnClickListener(this);
        userdelete.setOnClickListener(this);
        back.setOnClickListener(this);

        System.out.println("email value: "+email);
    }

    private void setDBListViewAdapter() {

        DataAdapter mDbHelper = new DataAdapter(getApplicationContext());
        mDbHelper.createDatabase();
        mDbHelper.open();

        // db에 있는 값들을 model을 적용해서 넣는다.
        rtList = mDbHelper.getTableData();

        // db 닫기
        mDbHelper.close();

        for (int i = 0; i < DB_SIZE; i++)
            dbList.add(rtList.get(i).getItem());

        //리스트뷰에 DB에서 받아온 dblist 지정
        ArrayAdapter dbAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, dbList);
        DBListView.setAdapter(dbAdapter);
    }

    //식단추가
    @Override
    public void onClick(View view) {
        Frag_Food food = new Frag_Food();
        switch (view.getId()) {
            case R.id.useradd:
                userFoodAdd();
                break;
            case R.id.add:
                menuAdd();
                break;
            case R.id.back:
                food.setMenu_Food(null);
                food.setUserFood(null);
                finish();
                break;
            case R.id.userdelete:
                delete();
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (view.getId()){
            case R.id.UserListView:
                userscroll.requestDisallowInterceptTouchEvent(true);
                break;
            case R.id.DBListView:
                dbscroll.requestDisallowInterceptTouchEvent(true);
                break;
        }
        return false;
    }

//    //DB초기화
//    private void initLoadDB() {
//
//        DataAdapter mDbHelper = new DataAdapter(getApplicationContext());
//        mDbHelper.createDatabase();
//        mDbHelper.open();
//
//        // db에 있는 값들을 model을 적용해서 넣는다.
//        rtList = mDbHelper.getTableData();
//
//        // db 닫기
//        mDbHelper.close();
//    }

    private void setUserListViewAdapter(){

        pref = getSharedPreferences(""+email, MODE_PRIVATE);

        userList.clear();
        UserFood.clear();
        UserCalrorie.clear();

        for(int i = 0; i < pref.getInt("count",0); i++) {
            UserFood.add( pref.getString("User_Food" + i, null));
            UserCalrorie.add( pref.getString("User_Calrorie" + i, null));
            userList.add(UserFood.get(i) + " " + UserCalrorie.get(i) + "kcal");
        }

        userAdapter = new ArrayAdapter
                (this, android.R.layout.simple_list_item_multiple_choice, userList);
        userAdapter.notifyDataSetChanged();
        userListView.setAdapter(userAdapter);
    }

    //사용자 식단추가 함수
    private void userFoodAdd() {
        String str = editfood.getText().toString();
        String str1 = editcalrorie.getText().toString();

        if(editfood.getText().length() == 0 || editcalrorie.getText().length() == 0)  {
            Toast.makeText(this, "입력하지 않은 값이 있습니다.", Toast.LENGTH_SHORT).show();
        }
        else {
            SharedPreferences.Editor editor = pref.edit();
            int i = pref.getInt("count", 0);
            editor.putString("User_Food" + i, str);
            editor.putString("User_Calrorie" + i, str1);
            editor.putInt("count", ++i);
            editor.commit();

            userList.add(str + " " + str1 + "kcal");
            UserFood.add(str);
            UserCalrorie.add(str1);

            userAdapter.notifyDataSetChanged();
            editfood.setText("");
            editcalrorie.setText("");
        }
    }
    //식단추가 함수
    private void menuAdd() {

        Frag_Food food = new Frag_Food();

        //Frag_Food에 전송할 array 객체 선언
        ArrayList<String> menu_Food = new ArrayList<>();
        ArrayList<String> menu_Calrorie = new ArrayList<>();
        ArrayList<String> Food = new ArrayList<>();
        ArrayList<String> calrorie = new ArrayList<>();

        //listview의 목록 갯수
        int len = DBListView.getCount();
        int len1 = userListView.getCount();
        //listview의 check 상태 (boolean)
        SparseBooleanArray checked = DBListView.getCheckedItemPositions();
        SparseBooleanArray checked1 = userListView.getCheckedItemPositions();
        //listview의 목록 갯수 만큼의 반복문
        for (int i = 0; i < len; i++) {
            //checkbox가 true && 내장db의 food이름과 listview의 목록이름 비교
            if (checked.get(i) && rtList.get(i).getItem().equals(dbList.get(i))) {
                menu_Food.add(dbList.get(i)+" ");
                menu_Calrorie.add(rtList.get(i).getCalorie());
            }
        }
        for(int i = 0; i < len1; i++) {
            if(checked1.get(i)) {
                Food.add(UserFood.get(i));
                calrorie.add(UserCalrorie.get(i));
            }
        }
        food.setMenu_Food(menu_Food);
        food.setMenu_Calrorie(menu_Calrorie);

        food.setUserFood(Food);
        food.setUserCalrorie(calrorie);

        setResult(RESULT_OK);
        finish();
    }
    private void delete() {
        SharedPreferences.Editor editor = pref.edit();
        SparseBooleanArray checkedItems = userListView.getCheckedItemPositions();

        int adapterCount = userAdapter.getCount();
        //pref 저장소에 int값 반환 defalult 시 0
        int count = pref.getInt("count",0);

        for (int i = 0; i < adapterCount; i++) {
            if (checkedItems.get(i) == true) {
                //삭제
                editor.remove("User_Food" + i);
                editor.remove("User_Calrorie"+i);
                //정렬
                for(int j = i ; j<count; j++){

                    editor.putString("User_Food" + j, pref.getString("User_Food" + (j+1), null));
                    editor.putString("User_Calrorie" + j,pref.getString("User_Calrorie" + (j+1),null));

                    editor.remove("User_Food" + (j+1));
                    editor.remove("User_Calrorie" + (j+1));
                }
                count--;
            }
        }

        editor.putInt("count", count);
        editor.commit();
        setUserListViewAdapter();
    }
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //Frag_Food food = new Frag_Food();
        Frag_Food.setMenu_Food(null);
        Frag_Food.setUserFood(null);
        finish();
    }
}