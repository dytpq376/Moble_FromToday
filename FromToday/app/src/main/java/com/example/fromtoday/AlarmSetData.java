package com.example.fromtoday;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;
import android.widget.Toast;

import static android.content.Context.WIFI_SERVICE;

public class AlarmSetData extends BroadcastReceiver {

    Context context;
    //PowerManager.WakeLock 빈객체 선언한다.
    private static PowerManager.WakeLock sCpuWakeLock;
    private static WifiManager.WifiLock sWifiLock;
    private static ConnectivityManager manager;

    // MainActivity 에서 textView 를 변경하기위해 Handler 사용
    public Handler handler = null;


   SharedPreferences step_Value;
    // ===========================================================
    StepService stepService = StepService.getInstance();
//    StepService stepService = new StepService();

    public static final int SEND_INIT = 2;
    Intent alarmIntent;

    public void Init_Data(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        // 절전모드로 와이파이 꺼지는것을 방지
        WifiManager wifiManager = (WifiManager)context.getSystemService(WIFI_SERVICE);
        sWifiLock = wifiManager.createWifiLock("wifilock");
        sWifiLock.setReferenceCounted(true);
        sWifiLock.acquire();

        // 시스템에서 powermanager 받아옴
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        // 객체의 제어레벨 설정
        sCpuWakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE, "app:alarm");

        //acquire 함수를 실행하여 앱을 깨운다. cpu 를 획득한다
        sCpuWakeLock.acquire();
        // 콘텍스트 알람
        manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // ====================================== Data Set ========================================

        stepService.Hdl_StepDetect.sendEmptyMessage(stepService.SEND_INIT);

/*
        context.StepService(new Intent(context, StepService.class));
*/

        Toast.makeText(context,"알람 발생", Toast.LENGTH_SHORT).show();

        // ========================================================================================

        if(sWifiLock != null) {
            sWifiLock.release();
            sWifiLock = null;
        }
        if (sCpuWakeLock != null) {
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }
    }
}
