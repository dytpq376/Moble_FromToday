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
public class FoodService extends Service {
    public SharedPreferences dayKcal;
    public static final int FOOD_KCAL = 0;
    public static final int FOODMESSAGE = 0;
    public static final int INIT_DATA = 0;
    // Interface
    static FoodCallback FoodCallback;
    private MyBinder mMyBinder = new MyBinder();
    private AlarmSetData alarmSetData;
    //알람체크
    private Boolean alarmCheck = false;
    // 싱글톤 패턴
    private static FoodService foodService;
    /*    private StepService( ) {}*/

    public static FoodService getInstance() {
        if (foodService == null) {
            foodService = new FoodService();
        }
        return foodService;
    }


    /*
     * 바인드 클래스 생성
     * Activity => 클라이언트 역할
     * 서비스 => 서버 역할
     */
    class MyBinder extends Binder {
        FoodService getService() {
            return FoodService.this;
        }
    }

    public void setCallback(FoodCallback foodCallback) {
        Log.i("jenn", "setCallback : callback " + foodCallback );
        this.FoodCallback = foodCallback;
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

    // TODO :  생명주기 = onCreate() -> onStartCommand (서비스 처음 실행 시)
    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("geon", "StepService : onCreate");
    }

    // TODO : onStartCommand => 시작된서비스(콜백 메서드)
    // TODO : 서비스가 실행되고 있는 상태에서 또 서비스 시작을 할 경우 onStartCommand()함수를 탑
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        alarmCheck = true;
        Log.i("geon", "FoodService : onStartCommand");

        // Notification (알림창)
        initializeNotification();
        // TYPE_STEP_DETECTOR

        String param = intent.getStringExtra("SEND_INIT");
        Log.i("sung","param : "  + param);
        if(param != null){
            Log.i("sung","paramvalue:"+param);
            if(FoodCallback != null){
                FoodCallback.onFoodCallback(INIT_DATA);
                Log.i("sung2","param : "  + param);
            }
        }

//        dayKcal = getSharedPreferences("dayKcal",MODE_PRIVATE);
//        SharedPreferences.Editor editor = dayKcal.edit();
//        editor.remove("dayKcal");
//        editor.commit();
        doDayOfTheWeek();
        return START_STICKY;
    }

    // TODO : NotificationCompat.Builder 개체를 사용하여 콘텐츠와 채널을 설정
    public void initializeNotification() {
        // 알림창 생성, 채널 지정
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

    // TODO : 서비스를 종료 시켰을 경우
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


    //---------------------------------------------------------------------------------------------
    // TODO : Handler (걸음수 생성 및 초기화)
    //---------------------------------------------------------------------------------------------
    // Handler 선언 후 사용
    Handler FoodMessage = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Frag_Home home = new Frag_Home();
            switch (msg.what) {

                case FOOD_KCAL :

                    // ======================= 콜백으로 Frag_Home 에 전달 =======================
                    if (FoodCallback != null) {
                        Log.i("jenn", "callback != null 들어옴 ");
                        FoodCallback.onFoodCallback(foodService.FOODMESSAGE);
                    }
                    break;

            }
            super.handleMessage(msg);
        }
    };
    // 요일별 데이터 저장
    private void doDayOfTheWeek(){
        //달력 날자 받아오기
        Calendar calendar = Calendar.getInstance(); //캘린더 인스턴스 받아오기
        //Calendar.DAY_OF_WEEK로 오늘 요일을 받아온후 변수에 저장해준다 이 변수는 오늘 요일이다.
        int calendar_week = calendar.get(Calendar.DAY_OF_WEEK);
        dayKcal = getSharedPreferences("dayKcal",MODE_PRIVATE);
        SharedPreferences.Editor editor = dayKcal.edit();
        switch (calendar_week) {
            case 1:
                int sunday = dayKcal.getInt("dayKcal",0);
                editor.putInt("sunday",sunday);
                editor.apply();
                Log.i("Geon", "sundayKcal : " );
                if(alarmCheck == true) {
                    editor = dayKcal.edit();
                    editor.remove("monday");
                    editor.remove("tuesday");
                    editor.remove("wednesday");
                    editor.remove("thursday");
                    editor.remove("friday");
                    editor.remove("saturday");
                    editor.commit();
                }
                alarmCheck = false;
                break;
            case 2:
                int monday = dayKcal.getInt("dayKcal",0);
                editor.putInt("monday",monday);
                editor.apply();
                Log.i("Geon", "mondayKcal : " );
                break;
            case 3:
                int tuesday = dayKcal.getInt("dayKcal",0);
                editor.putInt("tuesday",tuesday);
                editor.apply();
                Log.i("Geon", "tuesdayKcal : " );
                break;
            case 4:
                int wednesday = dayKcal.getInt("dayKcal",0);
                editor.putInt("wednesday",wednesday);
                editor.apply();
                Log.i("Geon", "wednesdayKcal : " );
                break;
            case 5:
                int thursday = dayKcal.getInt("dayKcal",0);
                editor.putInt("thursday",thursday);
                editor.apply();
                Log.i("Geon", "thursdayKcal : "+thursday );
                break;
            case 6:
                int friday = dayKcal.getInt("dayKcal",0);
                editor.putInt("friday",friday);
                editor.apply();
                Log.i("Geon", "fridayKcal : ");
                break;
            case 7:
                int saturday = dayKcal.getInt("dayKcal",0);
                editor.putInt("saturday",saturday);
                editor.apply();
                Log.i("Geon", "saturdayKcal : " );
                break;
        }
    }
}