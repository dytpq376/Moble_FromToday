package com.example.fromtoday;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.widget.Toast;

import static android.content.Context.WIFI_SERVICE;

public class AlarmSetData extends BroadcastReceiver {

    //PowerManager.WakeLock 빈객체 선언한다.
    private static PowerManager.WakeLock sCpuWakeLock;
    private static WifiManager.WifiLock sWifiLock;
    private static ConnectivityManager manager;
    // food service
    private FoodService foodService = new FoodService();
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

        manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // ====================================== Data Set ========================================
        // StepService 에 초기화 메세지 전달
        Intent sIntent = new Intent(context, StepService.class);
        sIntent.putExtra("SEND_INIT","2");
        context.startService(sIntent);

        foodService.FoodMessage.sendEmptyMessage(foodService.FOOD_KCAL);
        Intent fIntent = new Intent(context, FoodService.class);
        fIntent.putExtra("SEND_INIT","2");
        context.startService(fIntent);

        Toast.makeText(context,"알람 발생", Toast.LENGTH_SHORT).show();

        // ========================================================================================
        // 와이파이 예외처리
        if(sWifiLock != null) {
            sWifiLock.release();
            sWifiLock = null;
        }
        // cpu 에 걸려있는 락 해제
        if (sCpuWakeLock != null) {
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }

    }
}


