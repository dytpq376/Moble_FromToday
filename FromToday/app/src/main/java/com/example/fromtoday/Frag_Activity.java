package com.example.fromtoday;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;

import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;

import androidx.fragment.app.FragmentPagerAdapter;


public class Frag_Activity extends Fragment {

    private View view;

    //걷기,뛰기,자전거 타기 변수 지정
    private LinearLayout btn_walk, btn_run, btn_bike;

    //막대그래프 변수 선언입니다.
    private BarChart mBarChart;
    SharedPreferences runResult;

    //달력 요일을 넣어주기 위한 변수입니다.
    private int calendar_week;

    //각 요일에 넣은 칼로리 변수 입니다.
    private float chart_mondayKcal, chart_tuesdayKcal, chart_wednesdayKcal, chart_thursdayKcal, chart_fridayKcal, chart_saturdayKcal, chart_sundayKcal;

    //프리퍼런스에 저장된 데이터를 담는 변수 입니다
    String speed, kcal, time, distance, sumkcal;

    //요일별 칼로리 프리퍼런스 전연 선언입니다
    SharedPreferences week_Result;

    //viewpage-플래그 먼트
    private Fragment fragment1;
    private Fragment fragment2;

    //뷰테이저 어뎁터 가져오기
    private FragmentPagerAdapter fragmentPagerAdapter;

    //이중 스크롤뷰 선언
    // private ScrollView childscrollview;
    private NestedScrollView parentscrollview;

    private ViewPager viewPager;

    //DTO
    private DatabaseReference mPostReference;

    String resultactivity;

    Activity_DTO activity_dto = new Activity_DTO();
    private ArrayList<Double> AL_DTO_walk = new ArrayList<>();
    private ArrayList<Double> AL_DTO_run = new ArrayList<>();
    private ArrayList<Double> AL_DTO_bike = new ArrayList<>();

    //유저 이메일을 받기 위한 프리퍼런스 선언
    public SharedPreferences user_Value;

    //sunkcal
    double walk_sumkcal;
    double run_sumkcal;
    double bike_sumkcal;
    double total_sumkcal;

    //월요일 차트 데이터 초기화
    private boolean chartpermission = false;
    FirebaseDatabase mDatabase;
    DatabaseReference dataRef;

    //리사이클 뷰
    private NestedScrollView walk_scroll;
    private NestedScrollView run_scroll;
    private NestedScrollView bike_scroll;

    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;


    double sum_all;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_activity, container, false);

        //액티비티에서 받아온 거리,속력,시간,칼로리,총 칼로리 등을 가진 프리퍼런스의 객체를 만들어 준다.
        runResult = getActivity().getSharedPreferences("runResult", getActivity().MODE_PRIVATE);

        //걷기,뛰기,자전거 타기 아이디 값을 받아옵니다
        btn_walk = view.findViewById(R.id.linear_walk);
        btn_run = view.findViewById(R.id.linear_run);
        btn_bike = view.findViewById(R.id.linear_bike);

        viewPager = view.findViewById(R.id.view_pager);




        //스크롤 뷰
        parentscrollview = view.findViewById(R.id.parentscrollview);
        //childscrollview = view.findViewById(R.id.childscrollview);



        //주간 차트
        mBarChart = (BarChart) view.findViewById(R.id.tab1_chart_2);


        btn_walk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Activity_Walk.class);
                startActivity(intent);
//                btn_run.setEnabled(false);
//                btn_bike.setEnabled(false);
            }
        });

        btn_run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(getActivity(), Activity_Run.class);
                startActivity(intent2);
//                btn_walk.setEnabled(false);
//                btn_bike.setEnabled(false);
            }
        });

        btn_bike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent3 = new Intent(getActivity(), Activity_Bike.class);
                startActivity(intent3);
//                btn_walk.setEnabled(false);
//                btn_run.setEnabled(false);

            }
        });

        //수직 리스트는 안됨, 스평 이동은 실행됨
        //전제 스크롤 뷰가 기능을 안하면 기능함
        //포커스는 잡는데 뷰페이지 내부 리스트 뷰 는 못잡아서 나는 오류?
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e("childscrollview222", "childscrollviewchildscrollviewchildscrollviewchildscrollview222");
                parentscrollview.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        parentscrollview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e("parentscrollview", "parentscrollviewparentscrollview");
                return false;
            }
        });




        //뷰페이지 어뎀터 연결과 동작을 관리합니다
        ViewPager viewPager = view.findViewById(R.id.view_pager);
        fragmentPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        viewPager.setAdapter(fragmentPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        firebase();

        if (calendar_week == 1 && chartpermission == true) {//만약 차트 퍼미션이 참이면//초기화 하는 날에!
            user_Value = getActivity().getSharedPreferences("currentUser", MODE_PRIVATE);
            String strEmail = user_Value.getString("email", null);
            strEmail = strEmail.replaceAll("@", "").replaceAll("[.]", "");

            mDatabase = FirebaseDatabase.getInstance();
            dataRef = mDatabase.getReference("users");
            dataRef.child(strEmail).child("activity").removeValue();//파베의 액티비티 테이블을 날려버립니다.
            chartpermission = false;//다시 권한을 거짓을 주어서 이 구문이 월요일에 단 한번만 동작하게 합니다.
        }

        return view;
    }



    private void firebase() {
        Log.e("firebase", "firebase: firebase firebase");
        user_Value = getActivity().getSharedPreferences("currentUser", MODE_PRIVATE);
        String strEmail = user_Value.getString("email", null);
        strEmail = strEmail.replaceAll("@", "").replaceAll("[.]", "");
        mPostReference = FirebaseDatabase.getInstance().getReference();
        mPostReference.child("users").child(strEmail).child("activity").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //시작전 초기화
                AL_DTO_walk.clear();
                AL_DTO_run.clear();
                AL_DTO_bike.clear();
                walk_sumkcal = 0;
                run_sumkcal = 0;
                bike_sumkcal = 0;
                for (DataSnapshot activity_snapshot : dataSnapshot.getChildren()) {
                    Log.e("키1", activity_snapshot.getKey());
                    for (DataSnapshot snapshot : activity_snapshot.getChildren()) {
                        Log.e("키2", snapshot.getKey());//walk_activity 의 키값들을 가져온다.
                        Activity_DTO activity_dto = snapshot.getValue(Activity_DTO.class);

                        if (activity_snapshot.getKey() != null) {

                            if (activity_snapshot.getKey().equals("run_activity")) {
                                if (activity_dto.getRun_Week().equals(String.valueOf(calendar_week))) {//요일별 칼로리를 뽑아내는 로직
                                    AL_DTO_run.add(Double.parseDouble(activity_dto.getRun_kcal()));//만약 저장된 요일값과 오늘 요일이 같다면, kcal를 합산하여
                                    Log.e("AL_DTO_run", String.valueOf(AL_DTO_run.size()));//sumkcal를 계산하여, 저장한다.
                                }
                            }


                            if (activity_snapshot.getKey().equals("walk_activity")) {
                                if (activity_dto.getWalk_Week().equals(String.valueOf(calendar_week))) {//요일별 칼로리를 뽑아내는 로직
                                    AL_DTO_walk.add(Double.parseDouble(activity_dto.getWalk_kcal()));//만약 저장된 요일값과 오늘 요일이 같다면, kcal를 합산하여
                                    Log.e("AL_DTO_walk", String.valueOf(AL_DTO_walk.size()));//sumkcal를 계산하여, 저장한다.
                                }
                            }

                            if (activity_snapshot.getKey().equals("bike_activity")) {
                                if (activity_dto.getBike_Week().equals(String.valueOf(calendar_week))) {//요일별 칼로리를 뽑아내는 로직
                                    AL_DTO_bike.add(Double.parseDouble(activity_dto.getBike_kcal()));//만약 저장된 요일값과 오늘 요일이 같다면, kcal를 합산하여
                                    Log.e("AL_DTO_bike_size", String.valueOf(AL_DTO_bike.size()));//sumkcal를 계산하여, 저장한다.
                                }
                            }
                        }
                    }//for
                }//for

                for (int i = 0; i < AL_DTO_walk.size(); i++) {//오늘 요일의 칼로리 값을 뽑아 리스트에 저장후 전부 합산
                    Log.e("AL_DTO_walk", String.valueOf(AL_DTO_walk.get(i)));
                    walk_sumkcal += AL_DTO_walk.get(i);
                    Log.e("walk_sumkcal", String.format("%.1f", walk_sumkcal));
                }
                for (int i = 0; i < AL_DTO_run.size(); i++) {//오늘 요일의 칼로리 값을 뽑아 리스트에 저장후 전부 합산
                    Log.e("AL_DTO_run", String.valueOf(AL_DTO_run.get(i)));
                    run_sumkcal += AL_DTO_run.get(i);
                    Log.e("run_sumkcalrun_sumkcal", String.format("%.1f", run_sumkcal));
                }
                for (int i = 0; i < AL_DTO_bike.size(); i++) {//오늘 요일의 칼로리 값을 뽑아 리스트에 저장후 전부 합산
                    Log.e("AL_DTO_bike", String.valueOf(AL_DTO_bike.get(i)));
                    bike_sumkcal += AL_DTO_bike.get(i);
                    Log.e("bike_sumkcal", String.format("%.1f", bike_sumkcal));
                }
                setBarChart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("onCancelled", "onCancelledonCancelledonCancelled");
            }
        });
    }


    private void setBarChart() {
        Log.e("setBarChart", "setBarChartsetBarChart");
        Log.e("walk_sumkcal", String.valueOf(walk_sumkcal));
        Log.e("run_sumkcal", String.valueOf(run_sumkcal));
        Log.e("bike_sumkcal", String.valueOf(bike_sumkcal));
        total_sumkcal = walk_sumkcal + run_sumkcal + bike_sumkcal;


        //파이어베이스에 올라갈 일주일치 총 소모 칼로리
        sum_all = chart_sundayKcal + chart_mondayKcal + chart_tuesdayKcal + chart_wednesdayKcal + chart_thursdayKcal + chart_fridayKcal + chart_saturdayKcal ;

        Totalkcal();


        //달력 날자 받아오기
        Calendar calendar = Calendar.getInstance(); //캘린더 인스턴스 받아오기
        //Calendar.DAY_OF_WEEK로 오늘 요일을 받아온후 변수에 저장해준다 이 변수는 오늘 요일이다.
        calendar_week = calendar.get(Calendar.DAY_OF_WEEK);

        //받아온 calendar_week는 요일이 상수로 표현
        //일요일 = 1, 월요일 = 2, 화요일 = 3 ........... 토요일 = 7 이런 방식으로 받아온다.

        //프리퍼런스 값 받아오기
        runResult = getActivity().getSharedPreferences("runResult", MODE_PRIVATE);
        //프리퍼런스의 특정값을 받아와서 변수에 저장
        sumkcal = runResult.getString("sumkcal", null);

        //요일별 변수에 프리퍼 런스로 받아온 값을 저장해 준다.
        //요일에 오늘 운동한 칼로리 값이 요일변수에 저장된다.
        if (total_sumkcal != 0) {
            mBarChart.clearChart();
            switch (calendar_week) { //오늘 요일이 무엇이냐에 따라 case를 탄다
                case 1:
                    chart_sundayKcal = Float.parseFloat(String.valueOf(total_sumkcal));
                    Log.e("chart_sundayKcal", String.valueOf(chart_sundayKcal));
                    total_sumkcal = 0;

                    //차트 초기화 구문입니다.
                    //기본 차트 퍼미션은 거짓입니다.


                    break;
                case 2:
                    chart_mondayKcal = Float.parseFloat(String.valueOf(total_sumkcal));
                    Log.e("chart_mondayKcal", String.valueOf(total_sumkcal));
                    chartpermission = true;
                    total_sumkcal = 0;
                    break;
                case 3:
                    chart_tuesdayKcal = Float.parseFloat(String.valueOf(total_sumkcal));
                    Log.e("chart_tuesdayKcal", String.valueOf(total_sumkcal));
                    chartpermission = true;
                    total_sumkcal = 0;
                    break;
                case 4:
                    chart_wednesdayKcal = Float.parseFloat(String.valueOf(total_sumkcal));
                    Log.e("chart_wednesdayKcal", String.valueOf(total_sumkcal));
                    chartpermission = true;
                    total_sumkcal = 0;
                    break;
                case 5:
                    chart_thursdayKcal = Float.parseFloat(String.valueOf(total_sumkcal));
                    Log.e("chart_thursdayKcal", String.valueOf(total_sumkcal));
                    chartpermission = true;
                    total_sumkcal = 0;
                    break;
                case 6:
                    chart_fridayKcal = Float.parseFloat(String.valueOf(total_sumkcal));
                    Log.e("chart_fridayKcal", String.valueOf(total_sumkcal));
                    chartpermission = true;
                    total_sumkcal = 0;
                    break;
                case 7:
                    chart_saturdayKcal = Float.parseFloat(String.valueOf(total_sumkcal));
                    Log.e("chart_saturdayKcal", String.valueOf(total_sumkcal));
                    chartpermission = true;
                    total_sumkcal = 0;
                    break;
            }


            // BarChar 데이터 입력
            //저장된 요일변수를 넣어주어 차트에 데이터를 넣어준다.
            mBarChart.addBar(new BarModel("일", chart_sundayKcal, 0xFFCff0DA));
            mBarChart.addBar(new BarModel("월", chart_mondayKcal, 0xFF88DBA3));
            mBarChart.addBar(new BarModel("화", chart_tuesdayKcal, 0xFF90C695));
            mBarChart.addBar(new BarModel("수", chart_wednesdayKcal, 0xFF3B8686));
            mBarChart.addBar(new BarModel("목", chart_thursdayKcal, 0xFF3AC569));
            mBarChart.addBar(new BarModel("금", chart_fridayKcal, 0xFF3B8686));
            mBarChart.addBar(new BarModel("토", chart_saturdayKcal, 0xFFCFF09E));

            // BarChar 애니메이션 효과로 시작
            mBarChart.startAnimation();
        }
    }

    private void Totalkcal(){

        user_Value = getActivity().getSharedPreferences("currentUser", MODE_PRIVATE);
        String strEmail = user_Value.getString("email", null);
        strEmail = strEmail.replaceAll("@", "").replaceAll("[.]", "");
        mPostReference = FirebaseDatabase.getInstance().getReference();

//        Tkcal = new FirebasePost(total_sumkcal);
        mPostReference.child("users").child(strEmail).child("total_kcal").setValue(sum_all);


    }


    @Override
    public void onResume() {
        Log.e("onResume", "onResume: onResumeon Resume");
        super.onResume();
        firebase();
    }
}
