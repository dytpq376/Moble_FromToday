package com.example.fromtoday;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    //화면 상수 값 지정
    private static final int FRAGMENT_FOOD = 0;
    private static final int FRAGMENT_SLEEP = 1;
    private static final int FRAGMENT_HOME = 2;
    private static final int FRAGMENT_ACTIVITY = 3;
    private static final int FRAGMENT_PEOPLE = 4;

    // 네비게이션 사용을 위한 선언
    private BottomNavigationView bottomNavigationView; // 바텀 네비게이션 뷰
    private FragmentManager fm;
    private FragmentTransaction ft;

    // 각 뷰에 값을 사용하기 위한 객체선언
    private Frag_Food fragFood;
    private Frag_Sleep fragSleep;
    private Frag_Home fragHome;
    private Frag_Activity fragActivity;
    private Frag_People fragPeople;

    private ImageView info;
    private ImageView menu;
    private Stt stt;
    public SharedPreferences user_Value;

    private BackPressedForFinish backPressedForFinish;

    private DatabaseReference mPostReference;
    FirebasePost post;

    public SharedPreferences text_result;

    String food = "식단";
    String home = "홈";
    String main = "메인";
    String sleep = "수면";
    String activity = "운동";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        info = findViewById(R.id.info);
        menu = findViewById(R.id.menu);
        currentUser();

        // TODO : 바텀 네비게이션, Fragment 연결
        bottomNavigationView = findViewById(R.id.bottom_Navigation);
        // 첫 로딩시 bottom_Navigation 에 원하는 아이콘이 지정되도록 설정
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // 선택된 메뉴 번호 전달
                switch (menuItem.getItemId()){
                    case R.id.food:
                        setFrag(FRAGMENT_FOOD);
                        break;
                    case R.id.sleep:
                        setFrag(FRAGMENT_SLEEP);
                        break;
                    case R.id.home:
                        setFrag(FRAGMENT_HOME);
                        break;
                    case R.id.activity:
                        setFrag(FRAGMENT_ACTIVITY);
                        break;
                    case R.id.people:
                        setFrag(FRAGMENT_PEOPLE);
                        break;
                }
                return true;
            }
        });
        // 각 뷰에 값을 사용하기 위한 객체생성
        fragFood = new Frag_Food();
        fragSleep = new Frag_Sleep();
        fragHome = new Frag_Home();
        fragActivity = new Frag_Activity();
        fragPeople = new Frag_People();
        //foodDataActivity=new FoodDataActivity();
        // 첫 프래그먼트 화면을 무엇으로 지정해줄 것인지
        setFrag(FRAGMENT_HOME);


        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), GoogleMapActivity.class);
                startActivity(intent);
            }
        });

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogue();
            }
        });

        // BackPressedForFinish 객체를 생성한다.
        backPressedForFinish = new BackPressedForFinish(this);

    }

    // 프래그먼트 교체가 일어나는 실행문
    public void setFrag(int n) {
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        switch (n) {
            case 0 :
                ft.replace(R.id.main_frame, fragFood);
                ft.commit(); // 저장
                break;
            case 1 :
                ft.replace(R.id.main_frame, fragSleep);
                ft.commit(); // 저장
                break;
            case 2 :
                ft.replace(R.id.main_frame, fragHome);
                ft.commit(); // 저장
                break;
            case 3 :
                ft.replace(R.id.main_frame, fragActivity);
                ft.commit(); // 저장
                break;
            case 4 :
                ft.replace(R.id.main_frame, fragPeople);
                ft.commit();
                break;
        }


    }

    private void currentUser() {

        user_Value = getSharedPreferences("currentUser",MODE_PRIVATE);
        SharedPreferences.Editor editor = user_Value.edit();
        String strName = user_Value.getString("name",null);
        String strProfile = user_Value.getString("profile",null);
        String strEmail = user_Value.getString("email",null);
        String strGender = user_Value.getString("gender",null);
        String strAge = user_Value.getString("age",null);
        String strBirth = user_Value.getString("birthday",null);

        mPostReference = FirebaseDatabase.getInstance().getReference();

        strEmail = strEmail.replaceAll("@", "").replaceAll("[.]", "");
        if(strEmail != null) {

            post = new FirebasePost(strProfile, strName, strEmail, strGender, strAge, strBirth);
            mPostReference.child("users").child(strEmail).setValue(post);

        }
    }

    private void getText(){

        String text_result;
        Intent intent = getIntent();
        text_result = intent.getStringExtra("result");
        Log.e("qqqqqqqqqqqqqqqqqq","qqqqqqqqqqqqqqqqq"+text_result);


        if(text_result != null) {
            if(text_result.contains(food) == true) {
                setFrag(0);
                bottomNavigationView.setSelectedItemId(R.id.food);
            }
            if(text_result.contains(sleep) == true) {
                setFrag(1);
                bottomNavigationView.setSelectedItemId(R.id.sleep);
            }
            if(text_result.contains(home)||text_result.contains(main) == true){
                setFrag(2);
                bottomNavigationView.setSelectedItemId(R.id.home);
            }
            if(text_result.contains(activity) == true) {
                setFrag(0);
                bottomNavigationView.setSelectedItemId(R.id.activity);
            }

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        int receiveCount = intent.getIntExtra("receiveCount",0);
        System.out.println("receiveCount value:"+receiveCount);
        getText();
    }

    @Override
    public void onBackPressed() {
        // BackPressedForFinish 클래시의 onBackPressed() 함수를 호출한다.
        backPressedForFinish.onBackPressed();
    }


    private void dialogue(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("음성인식").setMessage("실행하시겠습니까?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), Stt.class);
                startActivity(intent);



            }

        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                Toast.makeText(getApplicationContext(), "Cancel Click", Toast.LENGTH_SHORT).show();
            }
        });


        AlertDialog alertDialog = builder.create();
        alertDialog.show();

//        text_result = getSharedPreferences("result",MODE_PRIVATE);
//
//        String strResult = text_result.getString("result",null);
//
//        Log.d("Maintext",strResult);

    }


}
