package com.example.fromtoday;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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

import static android.content.Context.MODE_PRIVATE;

// 센서로부터의 값을 받아 올 수 있도록 class 에 SensorEventListener 를 implements 시켜준다.
// onSensorChanged()와 onAccuracyChanged() 함수를 override 할 수 있다.
public class Frag_Home extends Fragment {
    // 상수 선언
    public static final int MAX_STEP = 1000;
    public static final int INIT_DATA = 0;

    // Xml 선언
    private View view;
    private ImageView imageChart, imageRun, imageMap, imageDrop_Up, imageTime;
    private ImageView imageLocation ,imageFire, imageTimer;
    private PieChart chartWalk;
    private BarChart mBarChart;
    private CircleProgressBar mCircleProgressBar;
    public TextView tvStepCount, tvMaxStep, tvMinute, tvSecond, tvKcal, tvKilometer;

    private StepService stepService;

    private int fmStep, fmKcal, fmMinute, fmSecond;
    private double fmKilometer;

    private int fmSundayKcal, fmMondayKcal, fmTuesdayKcal, fmWednesdayKcal;
    private int fmThursdayKcal, fmFridayKcal,  fmSaturdayKcal;

    //서비스 내부로 Set 되어 스텝카운트의 변화와 Unbind 의 결과를 전달하는 콜백 객체의 구현체
    private StepCallback stepCallback = new StepCallback() {
        @Override
        public void onStepCallback(int step, int kcal, double kilometer, int minute, int second) {
            Log.i("geon", "Frag_Home : onStepCallback  step: " + step);
            fmStep = step;
            fmKcal = kcal;
            fmKilometer = kilometer;
            fmMinute = minute;
            fmSecond = second;

            updateViewByReceivedData();
        }

        @Override
        public void onInitCallback(int sundayKcal, int mondayKcal, int tuesdayKcal, int wednesdayKcal,
                                   int thursdayKcal, int fridayKcal, int saturdayKcal) {
            Log.i("geon", "Frag_Home : onInitCallback  step");
            // 데이터초기화
            fmStep = INIT_DATA;
            fmKcal = INIT_DATA;
            fmKilometer = INIT_DATA;
            fmMinute = INIT_DATA;
            fmSecond = INIT_DATA;

            // 전달된 변수값을 전역변수에 저장
            fmSundayKcal = sundayKcal;
            fmMondayKcal = mondayKcal;
            fmTuesdayKcal = tuesdayKcal;
            fmWednesdayKcal = wednesdayKcal;
            fmThursdayKcal = thursdayKcal;
            fmFridayKcal = fridayKcal;
            fmSaturdayKcal = saturdayKcal;

            // UpdateView
            updateViewByReceivedData();
            updateViewByBarChart();
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

            Log.i("geon", "serviceConnection :onServiceConnected " );
            Toast.makeText(getActivity(), "예스바인딩", Toast.LENGTH_SHORT).show();
            //
            StepService.MyBinder mb = (StepService.MyBinder) service;
            stepService = mb.getService();
            stepService.setCallback(stepCallback);

            // 어플을 껐다켰을경우 stepService 에서 다시 값을 가져옴.
            fmStep = stepService.mStepDetector;
            fmKcal = stepService.kcal;
            fmKilometer = stepService.kilometer;
            fmMinute = stepService.minute;
            fmSecond = stepService.second;

            SharedPreferences weekKcal = getActivity().getSharedPreferences("weekKcal",
                    MODE_PRIVATE);

            fmSundayKcal = weekKcal.getInt("sundayKcal",0);
            fmMondayKcal = weekKcal.getInt("mondayKcal",0);
            fmTuesdayKcal = weekKcal.getInt("tuesdayKcal",0);
            fmWednesdayKcal = weekKcal.getInt("wednesdayKcal",0);
            fmThursdayKcal = weekKcal.getInt("thursdayKcal",0);
            fmFridayKcal = weekKcal.getInt("fridayKcal",0);
            fmSaturdayKcal = weekKcal.getInt("saturdayKcal",0);

            // UpdateView
            updateViewByReceivedData();
            updateViewByBarChart();
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
        // Drop_Up Image
        imageDrop_Up = (ImageView)view.findViewById(R.id.imageDrop_up);
        imageDrop_Up.setColorFilter(Color.parseColor(Colors.GRAY), PorterDuff.Mode.SRC_IN);
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
        // UpdateView
        updateViewByReceivedData();
        updateViewByBarChart();
        // Service 시작
        Intent intent = new Intent(getActivity(), StepService.class);
        getActivity().startService(intent);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // bindService 시작
        Intent intent = new Intent(getActivity(), StepService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        // UpdateView
        updateViewByReceivedData();
        updateViewByBarChart();
    }

    //---------------------------------------------------------------------------------------------
    // Function
    //---------------------------------------------------------------------------------------------
    private void updateViewByReceivedData(){
        // =========================================================================================
        // 1. CircleProgressBar 목표 걸음 수, 현재 걸음수 설정
        mCircleProgressBar.setMax(MAX_STEP);
        mCircleProgressBar.setProgress(fmStep);
        // CircleProgressBar 에 걸음 수 입력
        mCircleProgressBar.setProgressFormatter((progress, max) -> {
            final String DEFAULT_PATTERN = " %d ";
            return String.format(DEFAULT_PATTERN, progress);
        });
        // =========================================================================================
        // 2. TextView 에 걸음 수 입력
        tvStepCount.setText(String.valueOf(fmStep));
        tvMaxStep.setText(" / " + MAX_STEP + " 걸음");
        // =========================================================================================
        // 3. 거리, 칼로리, 시간 입력
        tvKilometer.setText(String.format("%.1f", fmKilometer));
        tvKcal.setText(String.valueOf(fmKcal));
        tvMinute.setText(String.valueOf(fmMinute));
        tvSecond.setText(String.valueOf(fmSecond));
    }

    // 파이 차트 설정
    private void updateViewByBarChart() {
        // BarChar 초기화
        mBarChart.clearChart();
        // BarChar 데이터 입력
        mBarChart.addBar(new BarModel("일", fmSundayKcal, 0xFFCff0DA));
        mBarChart.addBar(new BarModel("월", fmMondayKcal, 0xFF88DBA3));
        mBarChart.addBar(new BarModel("화", fmTuesdayKcal, 0xFF90C695));
        mBarChart.addBar(new BarModel("수", fmWednesdayKcal, 0xFF3B8686));
        mBarChart.addBar(new BarModel("목", fmThursdayKcal, 0xFF3AC569));
        mBarChart.addBar(new BarModel("금", fmFridayKcal, 0xFF3B8686));
        mBarChart.addBar(new BarModel("토", fmSaturdayKcal, 0xFFCFF09E));
        // BarChar 애니메이션 효과로 시작
        mBarChart.startAnimation();
    }

}
