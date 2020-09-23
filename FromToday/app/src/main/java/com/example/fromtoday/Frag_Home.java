package com.example.fromtoday;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.dinuscxj.progressbar.CircleProgressBar;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.BarModel;

// 센서로부터의 값을 받아 올 수 있도록 class 에 SensorEventListener 를 implements 시켜준다.
// onSensorChanged()와 onAccuracyChanged() 함수를 override 할 수 있다.
public class Frag_Home extends Fragment {

    // 상수 선언
    public static final int MAX_STEP = 1000;

    // Xml 선언
    private View view;
    private ImageView imageChart, imageRun, imageMap, imageDrop_Up, imageTime;
    private ImageView imageLocation ,imageFire, imageTimer;
    private PieChart chartWalk;
    private BarChart mBarChart;
    private CircleProgressBar mCircleProgressBar;

    public TextView tvStepCount, tvMaxStep, tvMinute, tvSecond, tvKcal, tvKilometer;

    private StepService stepService;
    private Intent intent; //서비스 객체를 가지고 있는 인텐트 객체

    private int mbStep, mbKcal, mbMinute, mbSecond;
    private double mbKilometer;

    //서비스 내부로 Set 되어 스텝카운트의 변화와 Unbind 의 결과를 전달하는 콜백 객체의 구현체
    private StepCallback stepCallback = new StepCallback() {
        @Override
        public void onStepCallback(int step, int kcal, double kilometer, int minute, int second) {
            Log.i("jenn", "Frag_Home : onStepCallback  step: " + step);
            mbStep = step;
            mbKcal = kcal;
            mbKilometer = kilometer;
            mbMinute = minute;
            mbSecond = second;

            updateViewByReceivedData();
        }
        @Override
        public void onUnbindService() {
            /*Toast.makeText(getActivity(), "디스바인딩!!", Toast.LENGTH_SHORT).show();*/
        }
    };

    // 서비스 바인드를 담당하는 객체의 구현체
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Log.i("jenn", "serviceConnection :onServiceConnected " );

            Toast.makeText(getActivity(), "예스바인딩", Toast.LENGTH_SHORT).show();
            StepService.MyBinder mb = (StepService.MyBinder) service;
            stepService = mb.getService();
            stepService.setCallback(stepCallback);

            // 어플을 껐다켰을경우 stepService 에서 다시 값을 가져옴.
            mbStep = stepService.mStepDetector;
            mbKcal = stepService.kcal;
            mbKilometer = stepService.kilometer;
            mbMinute = stepService.minute;
            mbSecond = stepService.second;

            updateViewByReceivedData();
            setBarChart();
        }
        // 사실상 서비스가 킬되거나 아예 죽임 당했을 때만 호출된다고 보면 됨
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // stopService 또는 unBindService 때 호출되지 않음.
            Toast.makeText(getActivity(), "디스바인딩", Toast.LENGTH_SHORT).show();
        }
    };

    //---------------------------------------------------------------------------------------------
    // onCreateView
    //---------------------------------------------------------------------------------------------
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_home, container, false);

        // -----------------------------------------------------------------------------------------
        // PNG 파일 색상 바꾸기
        // 내 활동 Image
        imageChart = (ImageView)view.findViewById(R.id.imageChart);
        imageChart.setColorFilter(Color.parseColor(Colors.BLUE), PorterDuff.Mode.SRC_IN);
        // 운동기록 Image
        imageRun = (ImageView)view.findViewById(R.id.imageRun);
        imageRun.setColorFilter(Color.parseColor(Colors.GREEN), PorterDuff.Mode.SRC_IN);
        /*// Map Image
        imageMap = (ImageView)view.findViewById(R.id.imageMap);
        imageMap.setColorFilter(Color.parseColor(Colors.ORANGE), PorterDuff.Mode.SRC_IN);*/
        // Drop_Up Image
        imageDrop_Up = (ImageView)view.findViewById(R.id.imageDrop_up);
        imageDrop_Up.setColorFilter(Color.parseColor(Colors.GRAY), PorterDuff.Mode.SRC_IN);
        /*// 활동기록 Image
        imageTime = (ImageView)view.findViewById(R.id.imageWatch);
        imageTime.setColorFilter(Color.parseColor(Colors.YELLOW), PorterDuff.Mode.SRC_IN);*/
        // 활동기록 : 운동하기 Image
        imageLocation =(ImageView)view.findViewById(R.id.imageLocation);
        imageLocation.setColorFilter(Color.parseColor(Colors.COLOR_KM), PorterDuff.Mode.SRC_IN);
        // 활동기록 : 움직이기 Image
        imageFire = (ImageView)view.findViewById(R.id.imageFire);
        imageFire.setColorFilter(Color.parseColor(Colors.COLOR_KCAL), PorterDuff.Mode.SRC_IN);
        // 활동기록 : 올라가기 Image
        imageTimer = (ImageView)view.findViewById(R.id.imageTimer);
        imageTimer.setColorFilter(Color.parseColor(Colors.COLOR_FLOOR), PorterDuff.Mode.SRC_IN);
        // -----------------------------------------------------------------------------------------

        // TextView
        tvStepCount = (TextView)view.findViewById(R.id.tvStepCount);
        tvMaxStep = (TextView)view.findViewById(R.id.tvMaxStep);
        tvMinute = (TextView)view.findViewById(R.id.tvMinute);
        tvSecond = (TextView)view.findViewById(R.id.tvSecond);
        tvKcal = (TextView)view.findViewById(R.id.tvKcal);
        tvKilometer = (TextView)view.findViewById(R.id.tvKilometer);

        // stepService 클래스 객체 생성
        stepService = StepService.getInstance();

        // CircleProgressBar
        mCircleProgressBar = (CircleProgressBar)view.findViewById(R.id.days_graph);

        // BarChart
        mBarChart = (BarChart)view.findViewById(R.id.tab1_chart_2);

        updateViewByReceivedData();
        setBarChart();
/*
        // Graph
        chartWalk = (PieChart)view.findViewById(R.id.tab1_chart_1);
        setPieChart();
*/
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        intent = new Intent(getActivity(), StepService.class);
        getActivity().startService(intent);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        updateViewByReceivedData();
    }

    //---------------------------------------------------------------------------------------------
    // Function
    //---------------------------------------------------------------------------------------------
    private void setCircleProgressBar(){
        // CircleProgressBar 목표 걸음 수, 현재 걸음수 설정

        mCircleProgressBar.setMax(MAX_STEP);
        mCircleProgressBar.setProgress(stepService.mStepDetector);
        // CircleProgressBar 에 걸음 수 재입력
        mCircleProgressBar.setProgressFormatter((progress, max) -> {
            final String DEFAULT_PATTERN = " %d ";
            return String.format(DEFAULT_PATTERN, progress);
        });
    }

    private void updateViewByReceivedData(){
        // =========================================================================================
        // 1. CircleProgressBar 목표 걸음 수, 현재 걸음수 설정
        mCircleProgressBar.setMax(MAX_STEP);
        mCircleProgressBar.setProgress(mbStep);
        // CircleProgressBar 에 걸음 수 입력
        mCircleProgressBar.setProgressFormatter((progress, max) -> {
            final String DEFAULT_PATTERN = " %d ";
            return String.format(DEFAULT_PATTERN, progress);
        });
        // =========================================================================================
        // 2. TextView 에 걸음 수 입력
        tvStepCount.setText(String.valueOf(mbStep));
        tvMaxStep.setText(" / " + MAX_STEP + " 걸음");
        // =========================================================================================
        // 3. 거리, 칼로리, 시간 입력
        tvKilometer.setText(String.format("%.1f", mbKilometer));
        tvKcal.setText(String.valueOf(mbKcal));
        tvMinute.setText(String.valueOf(mbMinute));
        tvSecond.setText(String.valueOf(mbSecond));
    }

    // 파이 차트 설정
    private void setBarChart() {
/*        // 파이 차트 데이터 초기화
        chartWalk.clearChart();
        // 파이 차트에 데이터 추가
        chartWalk.addPieSlice(new PieModel(WalkCount + " / 6000 걸음", 100, Color.parseColor(Colors.GRAY)));
        chartWalk.addPieSlice(new PieModel("내 활동", 6, Color.parseColor(Colors.GRAY)));
        // 파이차트 애니메이션 시작
        chartWalk.startAnimation();*/
        // BarChar 초기화
        mBarChart.clearChart();
        // BarChar 데이터 입력
        mBarChart.addBar(new BarModel("월", 10f, 0xFFCff0DA));
        mBarChart.addBar(new BarModel("화", 2f, 0xFF88DBA3));
        mBarChart.addBar(new BarModel("수", 7f, 0xFF90C695));
        mBarChart.addBar(new BarModel("목", 5f, 0xFF3B8686));
        mBarChart.addBar(new BarModel("금", 8f, 0xFF3AC569));
        mBarChart.addBar(new BarModel("토", 3f, 0xFF3B8686));
        mBarChart.addBar(new BarModel("일", 7f, 0xFFCFF09E));
        // BarChar 애니메이션 효과로 시작
        mBarChart.startAnimation();
    }

}
