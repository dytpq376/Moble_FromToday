package com.example.fromtoday;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Iterator;

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
    public SharedPreferences user_Value;

    // AlarmManager
    private static final int REQUEST_CODE = 3333;
    private static final int ALARM_HOUR = 10;
    private static final int ALARM_MIN = 12;
    private static final int ALARM_SECOND = 30;

    AlarmManager alarmMgr;
    PendingIntent pendingIntent;
    private Calendar calendar;
    private Intent alarmIntent;
    public long calculateTime = 0;

    private BackPressedForFinish backPressedForFinish;

    private DatabaseReference mPostReference;
    private DatabaseReference databaseReference;
    FirebasePost post;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        info = findViewById(R.id.info);
        currentUser();

        // db에 저장할 시간과 분
        int getHourTimePicker = 0;
        int getMinuteTimePicker = 0;

        // 알람매니저 설정
        alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
        // Calendar 객체 생성
        calendar = Calendar.getInstance();
        // 알람리시버 intent 생성
        alarmIntent = new Intent(MainActivity.this, AlarmSetData.class);
        //alarmIntent.putExtra("step",stepCount);
        // DB 선언
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);

        // 23 이하, Old
        if (Build.VERSION.SDK_INT < 23) {
            calendar.set(Calendar.HOUR_OF_DAY, ALARM_HOUR);
            calendar.set(Calendar.MINUTE, ALARM_MIN);
            calendar.set(Calendar.SECOND, ALARM_SECOND);
        }
        // 23 이상, New
        else {
            // calendar 에 시간 셋팅
            calendar.set(Calendar.HOUR_OF_DAY, ALARM_HOUR);
            calendar.set(Calendar.MINUTE, ALARM_MIN);
            calendar.set(Calendar.SECOND, ALARM_SECOND);
        }

        SharedPreferences.Editor editor = pref.edit();
        // DB에 추가
        editor.putInt("set_hour", getHourTimePicker);
        editor.putInt("set_min", getMinuteTimePicker);

        editor.commit();

        calculateTime = calendar.getTimeInMillis();

        pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // 안드로이드 버젼에 따라 alarmMgr 설정
        // 23 미만
        if (Build.VERSION.SDK_INT < 23) {
            // 19 이상
            if (Build.VERSION.SDK_INT >= 19) {
                alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calculateTime, pendingIntent);
            }
            // 19 미만
            else {
                // 알람셋팅
                alarmMgr.set(AlarmManager.RTC_WAKEUP, calculateTime, pendingIntent);
            }
            // 23 이상
        } else {
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calculateTime, pendingIntent);
        }

        // TODO : 바텀 네비게이션, Fragment 연결
        bottomNavigationView = findViewById(R.id.bottom_Navigation);
        // 첫 로딩시 bottom_Navigation 에 원하는 아이콘이 지정되도록 설정
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // 선택된 메뉴 번호 전달
                switch (menuItem.getItemId()) {
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
                Intent intent = new Intent(getApplicationContext(), MyInfoActivity.class);
                startActivity(intent);
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
            case 0:
                ft.replace(R.id.main_frame, fragFood);
                ft.commit(); // 저장
                break;
            case 1:
                ft.replace(R.id.main_frame, fragSleep);
                ft.commit(); // 저장
                break;
            case 2:
                ft.replace(R.id.main_frame, fragHome);
                ft.commit(); // 저장
                break;
            case 3:
                ft.replace(R.id.main_frame, fragActivity);
                ft.commit(); // 저장
                break;
            case 4:
                //Login Activity 에서 넘어온 값 받기.
//                Intent intent =getIntent();
//
//                String strName=intent.getStringExtra("name");
//                String strImage = intent.getStringExtra("profile");
//                String strEmail=intent.getStringExtra("email");
//                String strGender=intent.getStringExtra("gender");
//                String strAge=intent.getStringExtra("age");
//                String strBirth=intent.getStringExtra("birthday");
//
//                Bundle bundle = new Bundle(6);
//                bundle.putString("profile",strImage);
//                bundle.putString("name",strName);
//                bundle.putString("birthday",strAge);
//                bundle.putString("email",strEmail);
//                bundle.putString("gender",strGender);
//                bundle.putString("age",strAge);
//                bundle.putString("birthday",strBirth);
//
//                Frag_People people = new Frag_People();
//                people.setArguments(bundle);
//                FragmentManager fm = getSupportFragmentManager();
//                FragmentTransaction ft = fm.beginTransaction();
//                ft.replace(R.id.main_frame,people).commit();
                ft.replace(R.id.main_frame, fragPeople);
                ft.commit();
                break;
        }


    }

    private void currentUser() {
//        Intent intent = getIntent();
//
//        String strName = intent.getStringExtra("name");
//        String strProfile = intent.getStringExtra("profile");
//        String strEmail = intent.getStringExtra("email");
//        String strGender = intent.getStringExtra("gender");
//        String strAge = intent.getStringExtra("age");
//        String strBirth = intent.getStringExtra("birthday");
//
//        user_Value = getSharedPreferences("currentUser",MODE_PRIVATE);
//        SharedPreferences.Editor editor = user_Value.edit();
//
//        editor.putString("name",strName);
//        editor.putString("profile",strProfile);
//        editor.putString("email",strEmail);
//        editor.putString("gender",strGender);
//        editor.putString("age",strAge);
//        editor.putString("birthday",strBirth);
//
//        editor.commit();
        user_Value = getSharedPreferences("currentUser", MODE_PRIVATE);
        SharedPreferences.Editor editor = user_Value.edit();
        String strName = user_Value.getString("name", null);
        String strProfile = user_Value.getString("profile", null);
        String strEmail = user_Value.getString("email", null);
        String strGender = user_Value.getString("gender", null);
        String strAge = user_Value.getString("age", null);
        String strBirth = user_Value.getString("birthday", null);

        mPostReference = FirebaseDatabase.getInstance().getReference();
        //유저 참조
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        strEmail = strEmail.replaceAll("@", "").replaceAll("[.]", "");

        String finalStrEmail = strEmail;

        //싱글 벨류 이벤트 한번만 실행
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //키값의 벨류를 검색
                //Iterator는 반복해서 모든 값을 서치
                //인덱스로 저장됨
                Iterator<DataSnapshot> child = dataSnapshot.getChildren().iterator();

                //users의 모든 자식들의 key값과 value 값들을 iterator로 참조합니다.
                while (child.hasNext()) {
                    //찾고자 하는 ID값은 key로 존재하는 값
                    if (child.next().getKey().equals(finalStrEmail)) {
                        return;
                    }
                }
                //널포인트 에러 체크
                if (finalStrEmail != null) {
                    post = new FirebasePost(strProfile, strName, finalStrEmail, strGender, strAge, strBirth);
                    mPostReference.child("users").child(finalStrEmail).setValue(post);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        int receiveCount = intent.getIntExtra("receiveCount", 0);
        System.out.println("receiveCount value:" + receiveCount);
    }

    @Override
    public void onBackPressed() {
        // BackPressedForFinish 클래시의 onBackPressed() 함수를 호출한다.
        backPressedForFinish.onBackPressed();
    }


}