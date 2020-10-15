package com.example.fromtoday;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

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

import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    //화면 상수 값 지정
    private static final int FRAGMENT_FOOD = 0;
    private static final int FRAGMENT_SLEEP = 1;
    private static final int FRAGMENT_HOME = 2;
    private static final int FRAGMENT_ACTIVITY = 3;
    private static final int FRAGMENT_RANK = 4;

    // 네비게이션 사용을 위한 선언
    private BottomNavigationView bottomNavigationView; // 바텀 네비게이션 뷰
    private FragmentManager fm;
    private FragmentTransaction ft;

    // 각 뷰에 값을 사용하기 위한 객체선언
    private Frag_Food fragFood;
    private Frag_Sleep fragSleep;
    private Frag_Home fragHome;
    private Frag_Activity fragActivity;
    private Frag_Rank fragRank;

    private LinearLayout info;
    private ImageView menu;
    public SharedPreferences user_Value;

    private BackPressedForFinish backPressedForFinish;

    //파이어베이스 선언
    private DatabaseReference mPostReference;
    private DatabaseReference databaseReference;
    FirebasePost post;

    public SharedPreferences text_result;

    //음성인식에 사용하는 문자들
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
                    case R.id.rank:
                        setFrag(FRAGMENT_RANK);
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
        fragRank = new Frag_Rank();
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
                ft.replace(R.id.main_frame, fragRank);
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
        user_Value = getSharedPreferences("currentUser",MODE_PRIVATE);
        SharedPreferences.Editor editor = user_Value.edit();
        String strName = user_Value.getString("name",null);
        String strProfile = user_Value.getString("profile",null);
        String strEmail = user_Value.getString("email",null);
        String strGender = user_Value.getString("gender",null);
        String strAge = user_Value.getString("age",null);
        String strBirth = user_Value.getString("birthday",null);

        mPostReference = FirebaseDatabase.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        strEmail = strEmail.replaceAll("@", "").replaceAll("[.]", "");

        String finalStrEmail = strEmail;

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Iterator<DataSnapshot> child = dataSnapshot.getChildren().iterator();
                //users의 모든 자식들의 key값과 value 값들을 iterator로 참조합니다.

                while(child.hasNext())
                {
                    //찾고자 하는 ID값은 key로 존재하는 값
                    if(child.next().getKey().equals(finalStrEmail))
                    {
                        return;
                    }

                }
                if(finalStrEmail != null) {

                    post = new FirebasePost(strProfile, strName, finalStrEmail, strGender, strAge, strBirth);
                    mPostReference.child("users").child(finalStrEmail).setValue(post);


                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getText(){

//            text_result = getSharedPreferences("result",MODE_PRIVATE);
//            SharedPreferences.Editor editor = text_result.edit();
//            String strResult = text_result.getString("result",null);
//            System.out.println("maintext : " + strResult);
//
//        if(strResult.equals(move)) {
//            setFrag(0);
//            if(strResult!= null) {
//                editor.remove("result");
//            }
//        }
//        else {
//            finish();
//        }
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


    //다이얼로그로 음성인식 실행
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
