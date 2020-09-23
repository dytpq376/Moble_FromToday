package com.example.fromtoday;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;

/**
 * 포그라운드 서비스 : 알림창에 서비스가 실행 중인 것을 표시, 대신 시스템에 의해 강제로 종료되지않음
 * 백그라운드 서비스 : 사용자에게 보이지않는 백그라운드 작업수행. 시스템 리소스 부족시 강제 종료
 * 바인드 서비스 : 서비스와 서비스를 호출하는 앱 구성요소가 서버-클라이언트와 같은 형태로 상호작용
 * (따라서 여러 프로세스에서 같은 서비스에 바인딩하여 작업을 수행가능)
 */
public class StepService extends Service implements SensorEventListener {
    SharedPreferences step_Value;
    // 상수
    public static final double KCAL_PER_MINUTE = 4.5;
    public static final double KM_PER_MIN = 0.05;
    public static final double TIME_STOP_WAITING_STEP = 1.5;  // 걸음 간격 사이가 몇초 이상이면 정지
    public static final int STEP_DETECT = 1;                  // Handler(발걸음 발생)
    public static final int SEND_INIT = 2;

    public double interval_Step = 0;
    public double totalSeconds;
    public long prevTime = 0;
    public int minute, second;
    public int kcal = 0;
    public double kilometer = 0.0;

    // Interface
    static StepCallback callback;
    public int mStepDetector = 0;

    private SensorManager sensorManager;
    private Sensor stepDetectorSensor;
    private Sensor stepCountSensor;

    private MyBinder mMyBinder = new MyBinder();

    private AlarmSetData alarmSetData;

    // 싱글톤 패턴
    private static StepService singleStepService;
/*    private StepService( ) {}*/

    public static StepService getInstance() {
        if (singleStepService == null) {
            singleStepService = new StepService();
        }
        return singleStepService;
    }


    /*
     * 바인드 클래스 생성
     * Activity => 클라이언트 역할
     * 서비스 => 서버 역할
     */
    class MyBinder extends Binder {
        StepService getService() {
            return StepService.this;
        }
    }

    public void setCallback(StepCallback callback) {
        Log.i("jenn", "setCallback : callback " + callback );
        this.callback = callback;
    }

    /*
     * 바인드된 서비스(클라이언트-서버 인터페이스를 제공하여 구성요소와 서비스 간 상호작용 가능
     * 프로세스간 통신(IPC) 수행
     * 호출 메서드 : bindService()
     * 콜백 메서드 : onBind()
     * 구현 해야하는 클래스 : ServiceConnection
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("jenn", "StepService : onBind");
        return mMyBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("jenn", "StepService : onCreate");

        // sensorManager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // TYPE_STEP_DETECTOR
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (stepDetectorSensor == null) {
        } else {
            sensorManager.registerListener(this,
                    stepCountSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        // TYPE_STEP_COUNTER
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepCountSensor == null) {
        } else {
            sensorManager.registerListener(this,
                    stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        alarmSetData = new AlarmSetData();
        Init_Data();
    }

    // onStartCommand => 시작된서비스(콜백 메서드)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("jenn", "StepService : onStartCommand");

        // Notification (알림창)
        initializeNotification();
        // TYPE_STEP_DETECTOR
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (stepDetectorSensor == null) {
        } else {
            sensorManager.registerListener(this,
                    stepCountSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        // TYPE_STEP_COUNTER
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepCountSensor == null) {
        } else {
            sensorManager.registerListener(this,
                    stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);

        }
        // START_STICKY => 서비스 시작후 강제로 종료될 경우, 자동으로 서비스를 실행
        return START_STICKY;
    }

    public void initializeNotification() {
        /*
         * 알림창 생성, 채널 지정
         * NotificationCompat.Builder 개체를 사용하여 콘텐츠와 채널을 설정
         */
        NotificationCompat.Builder builder = new
                NotificationCompat.Builder(this, "1");

        builder.setSmallIcon(R.mipmap.ic_launcher);  //필수 (안해주면 에러)

        // 알림의 확장된 콘텐츠 영역에 텍스트를 표시(NotificationCompat.BigTextStyle)
        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText("설정을 보려면 누르세요.");
        style.setBigContentTitle(null);
        style.setSummaryText("서비스 동작중");

        builder.setContentTitle(null);   // 상태바 드래그시 보이는 타이틀
        builder.setContentText(null);    // 상태바 드래그시 보이는 서브타이틀
        builder.setOngoing(true);        // 사용자가 직접 못지우게 계속 실행하기

        builder.setStyle(style);         // 확장형 알람을 사용하기 위해 BigTextStyle style 전달
        builder.setWhen(0);              // 받은 시간 커스텀 (기본 시스템에서 제공)
        builder.setShowWhen(false);      // 알림 수신 시간 ( default => true, false => 숨김 )

        /*
         * PendingIntent 는 Intent 를 가지고 있는 클래스로
         * 가지고 있는 인텐트를 보류하고 특정 시점에작업을 요청하도록 하는 특징
         * 다른 프로세스에서 수행하므로 Notification 으로 Intent 수행 시 PendingIntent 의 사용 필수
         */
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);

        // 알람 표시
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(new
                    NotificationChannel("1", "undead_service",
                    NotificationManager.IMPORTANCE_LOW));
        }
        // id 값은 정의해야하는 각 알림의 고유한 int 값
        Notification notification = builder.build();
        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Calendar 객체를 사용하여 AlarmManager 사용
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 3);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this,
                0, intent, 0);

        // AlarmManager 를 통해 정해진 초 동안 핸드폰을 지속적으로 깨워서 강제 종료를 막음.
        AlarmManager alarmManager =
                (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
    }

    /*
     * 서비스에 바인드된 컴포넌트가 더이상 없을 경우 서비스는 소멸
     * onUnbind()메서드를 이용하여 서비스와 컴포넌트의 바인딩을 해제제
     */
/*

    @Override
    public boolean onUnbind(Intent intent) {
        unRegistManager();
        if (callback != null)
            callback.onUnbindService();
        return super.onUnbind(intent);
    }

    //혹시 모를 에러상황에 트라이 캐치
    public void unRegistManager() {
        try {
            sensorManager.unregisterListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

*/

    private void Init_Data() {
        alarmSetData.Init_Data(Hdl_StepDetect);
    }

    //---------------------------------------------------------------------------------------------
    // Handler
    //---------------------------------------------------------------------------------------------
    Handler Hdl_StepDetect = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Frag_Home home = new Frag_Home();
            switch (msg.what) {
                // 발걸음 센서 발생 ----------------------------------------------------------------
                case STEP_DETECT:
                    // 현재 시간 받아오기
                    long currTime = System.currentTimeMillis();
                    if (prevTime == 0) {
                        // 첫 발걸음이라면 현재시간 저장
                        prevTime = currTime;
                    } else {
                        // 현 발걸음 - 전 발걸음
                        interval_Step = ((currTime - prevTime) / 1000.0);
                    }
                    // 현재 발걸음시간을 전 발걸음시간에 저장
                    prevTime = currTime;

                    // (스텝 간격 < 멈춰있다는 기준시간) 경우에만 시간, 스텝 수 측정
                    if (interval_Step < TIME_STOP_WAITING_STEP) {
                        // ============================== 걸음수 증가 ==============================
                        mStepDetector++;
                        /*step_Value = getSharedPreferences("step_Value", MODE_PRIVATE);
                        SharedPreferences.Editor editor = step_Value.edit();
                        editor.putInt("stepCount", mStepDetector);
                        editor.commit();
                        int stepCount = step_Value.getInt("stepCount", 0);
                        System.out.println("stepCountValue:  " + stepCount);*/

                        // ==================== 활동 시간, 이동거리, 칼로리 측정 ====================

                        totalSeconds += interval_Step;
                        minute = (int) (totalSeconds / 60.0);
                        second = (int) (totalSeconds);
                        second = second % 60;
                        Log.e("스텝 디텍터", "" + mStepDetector);

                        kilometer = (minute * KM_PER_MIN);
                        kcal = (int) (minute * KCAL_PER_MINUTE);

                        // ======================= 콜백으로 Frag_Home 에 전달 =======================
                        if (callback != null)
                            callback.onStepCallback(mStepDetector, kcal, kilometer, minute, second);
                    }
                    break;

                case SEND_INIT :

                    Monday_step = mStepDetector;

                    mStepDetector = 0;
                    kcal = 0;
                    kilometer = 0;
                    minute = 0;
                    second = 0;

                    Log.i("jenn", "Hdl_StepDetect : SEND_INIT,  call back:" + callback);
                    Log.e("Monday_step : ", "" + Monday_step);
                    Log.e("mStepDetector : ", "" + mStepDetector);

                    // ======================= 콜백으로 Frag_Home 에 전달 =======================
                    if (callback != null) {
                    Log.i("jenn", "callback != null 들어옴 ");
                    callback.onStepCallback(mStepDetector, kcal, kilometer, minute, second);
                }
                    break;

            }
            super.handleMessage(msg);
        }
    };
int Monday_step;
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values[0] == 1.0f) {
                // ========================== Handler Message 전달 ==========================
                Hdl_StepDetect.sendEmptyMessage(STEP_DETECT);
            }
        } else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            // ========================== TYPE_STEP_COUNTER 확인용 ==========================
            Log.e("스텝 카운트", "" + event.values[0]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}