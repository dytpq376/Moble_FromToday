/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.fromtoday;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.util.Date;

/**
 * A bound and started service that is promoted to a foreground service when location updates have
 * been requested and all clients unbind.
 * <p>
 * For apps running in the background on "O" devices, location is computed only once every 10
 * minutes and delivered batched every 30 minutes. This restriction applies even to apps
 * targeting "N" or lower which are run on "O" devices.
 * <p>
 * This sample show how to use a long-running service for location updates. When an activity is
 * bound to this service, frequent location updates are permitted. When the activity is removed
 * from the foreground, the service promotes itself to a foreground service, and location updates
 * continue. When the activity comes back to the foreground, the foreground service stops, and the
 * notification associated with that service is removed.
 */
public class LocationUpdatesService extends Service {
    SharedPreferences runResult;
//    private static double getdistance = 0;

    private static final String PACKAGE_NAME =
            "com.google.android.gms.location.sample.locationupdatesforegroundservice";

    private static final String TAG = LocationUpdatesService.class.getSimpleName();

    /**
     * The name of the channel for notifications.
     */
    private static final String CHANNEL_ID = "channel_01";

    static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

    static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";

    //바인더의 객체 생성
    private final IBinder mBinder = new LocalBinder();

    /**
     * 위치 업데이트에 대해 원하는 간격. 정확하지 않다. 업데이트는 다소 빈번할 수 있다.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;//업데이트 주기 입니당


    /**
     * 활성 위치 업데이트의 가장 빠른 속도. 업데이트 빈도는 이보다 더 높을 수 없음
     * 이 값보다.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS;

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 12345678;

    /**
     * 바인딩된 활동이 정말로 사라졌는지 여부를 확인할 때 사용되며, 바인딩된 활동이 결합되지 않았는지 확인할 때 사용
     * 방향 변경. 전자가 다음을 수행하는 경우에만 포그라운드 서비스 알림을 생성함
     * 장소.
     */
    private boolean mChangingConfiguration = false;

    private NotificationManager mNotificationManager;

    /**
     * 다음에서 사용하는 매개 변수 포함 {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;

    /**
     * Fused Location Provider API에 대한 액세스 제공
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * 위치 변경에 대한 콜백
     */
    private LocationCallback mLocationCallback;

    private Handler mServiceHandler;

    /**
     * 현재 위치.
     */
    private Location mLocation;

    //-----------------------------------------------------------------
    //센서값을 담아줄 변수를 지정합니다.
    public double speed;
    public double distance;
    public int time;
    public double kcal;
    private double sumkcal;


    public LocationUpdatesService() {
    }

    //센서값을 받아서 콜백 시켜주는 함수를 생성 합니다
    private SpeedCallback callback;

    public void setCallback(SpeedCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate() {
        System.out.println("서비스의 oncreate를 탔습니다!!!!!!!!!!!!");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "tjqltmdml onStartCommand 탔습니다!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false);

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            removeLocationUpdates();
            stopSelf();
        }
        // Tells the system to not try to recreate the service after it has been killed.
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // 클라이언트(이 샘플의 경우 MainActivity)가 전면에 나타날 때 호출됨
        // 및 이 서비스와 바인딩. 그 서비스는 전면 서비스에서 중단되어야 한다.
        // 그럴 때.
        Log.i(TAG, "in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
// 클라이언트(이 샘플의 경우 MainActivity)가 포그라운드에 돌아오면 호출됨
// 그리고 이 서비스로 다시 한 번 바인딩. 서비스가 전경이 되는 것을 멈춰야 한다.
// 그럴 때 서비스.
        Log.i(TAG, "in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");

        // 마지막 클라이언트(이 샘플의 경우 MainActivity)의 바인딩이 해제되면 호출됨
        // 서비스. MainActivity의 구성 변경으로 인해 이 메서드를 호출하는 경우
        // 아무 것도 하지 않는다. 그렇지 않으면 우리는 이 서비스를 포그라운드 서비스로 만든다.
        if (!mChangingConfiguration && Utils.requestingLocationUpdates(this)) {
            Log.i(TAG, "Starting foreground service");

            startForeground(NOTIFICATION_ID, getNotification());
        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 위치 업데이트 요청 이 샘플에서는 단지
     *
     * @link Security예외
     */
    public void requestLocationUpdates() {
        System.out.println("서비스의 requestLocationUpdates 탔습니다!!!!!!!!!!!!!!");
        Log.i(TAG, "Requesting location updates");
        Utils.setRequestingLocationUpdates(this, true);
        startService(new Intent(getApplicationContext(), LocationUpdatesService.class));
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, false);
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    /**
     * 위치 업데이트 제거 이 샘플에서는 단지
     * {@link SecurityException}.
     */
    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            Utils.setRequestingLocationUpdates(this, false);
            //언바인딩 후 초기화
            speed = 0;
            distance = 0;
            time = 0;
            stopSelf();
        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, true);
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private Notification getNotification() {
        System.out.println("Notification을 탔습니다!!!!!!!!!!!!!!!!");
        Intent intent = new Intent(this, LocationUpdatesService.class);

        CharSequence text = LocationUpdatesService.getLocationText(mLocation);

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // The PendingIntent to launch activity.
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Activity_Map_Run.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
//                .addAction(R.drawable.run, getString(R.string.launch_activity),//스트링 스타일로 특정 문자열을 찍어줍니다.
//                        activityPendingIntent)
//                .addAction(R.drawable.run, getString(R.string.remove_location_updates),//스트링 스타일로 특정 문자열을 찍어 줍니다.
//                        servicePendingIntent)
                .setContentText(text)
                .setContentTitle(LocationUpdatesService.getLocationTitle(this))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.안드로이드 오레오 버전이상미면 실행
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }
        runResult = getSharedPreferences("sumkcal", MODE_PRIVATE);

        return builder.build();
    }

    private void getLastLocation() {
        System.out.println("getLastLocation를 탔습니다!!!!!!!!!!!!!!!!!!!!!!");
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void onNewLocation(Location location) {
        Log.i(TAG, "New location: " + location);
        System.out.println("onNewLocation를 탔습니다!!!!!!!!!!!!!!!!!!!");

        mLocation = location;

        if (location != null) {
            System.out.println("센서 값을 변수에 저장해 주었습니다!!!!!!!!");
            speed = location.getSpeed();
            distance += location.getSpeed();
            time += UPDATE_INTERVAL_IN_MILLISECONDS;

            //걷기 로직
            if (speed != 0 && speed < 2) {
                if (1 >= speed) {
                    kcal = 0.1;
                } else if (speed >= 1.1 && 1.3 >= speed) {
                    kcal = 0.5;
                } else if (speed >= 1.4 && 1.5 >= speed) {
                    kcal = 0.6;
                } else if (speed >= 1.6 && 1.7 >= speed) {
                    kcal = 0.7;
                } else {
                    kcal = 0.7;
                }
                sumkcal += kcal;
            }

            //달리기 로직
            if (speed != 0 && speed >= 2 && speed < 6.4) {
                if (2 >= speed) {
                    kcal = 0.1;
                } else if (speed >= 2.1 && 2.9 >= speed) {
                    kcal = 0.3;
                } else if (speed >= 3 && 3.5 >= speed) {
                    kcal = 0.2;
                } else if (speed >= 3.6 && 4.6 >= speed) {
                    kcal = 0.24;
                } else if (speed >= 4.7 && 5.7 >= speed) {
                    kcal = 0.3;
                } else if (speed >= 5.8 && 6.2 >= speed) {
                    kcal = 0.34;
                } else {
                    kcal = 0.38;
                }
                sumkcal += kcal;
            }

            //자전거 로직
            if (speed != 0) {
                if (1 >= speed && speed < 2) {
                    kcal = 0.1;
                } else if (speed >= 1.1 && 1.3 >= speed) {
                    kcal = 0.5;
                } else if (speed >= 1.4 && 1.5 >= speed) {
                    kcal = 0.6;
                } else if (speed >= 1.6 && 1.7 >= speed) {
                    kcal = 0.7;
                } else {
                    kcal = 0.7;
                }
                sumkcal += kcal;
            }
        }

        if (callback != null) {
            System.out.println("콜백에 값을 넣어줬습니다!!!!!!!!");
            callback.onLocationCallback(speed, distance, time, sumkcal);
        }

        // 방송을 듣고 있는 모든 사람에게 새 위치에 대해 알리십시오.
        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_LOCATION, location);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        // 포그라운드 서비스로 실행 중인 경우 알림 내용 업데이트
        if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
        }
    }

    /**
     * 위치 요청 매개 변수를 설정하십시오.
     */
    private void createLocationRequest() {
        System.out.println("createLocationRequest 를 탔습니다!!!!!!!!!!!!!!!!!!!!!");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    /**
     * 클라이언트 바인더에 사용되는 클래스. 본 서비스는 본 서비스와 동일한 프로세스에서 실행되므로
     * 고객, IPC는 취급하지 않아도 된다.
     */
    public class LocalBinder extends Binder {
        LocationUpdatesService getService() {
            return LocationUpdatesService.this;
        }
    }

    /**
     * 포드라운드 서비스인 경우 true를 반환한다.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }


    static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "( 현재 속도는" + String.format("%.1f", location.getSpeed()) + "m/s )"; //값 찍어주기
    }


//      static으로 값 넘기지 않고 바인드로 값으로 엄기는 방식으로 값을 넘김니다.
//    static String getLocationTextSpeed(Location location) {
//        return location == null ? "Unknown location" :
//                "(" + location.getSpeed() + ")";//값 찍어주기
//    }
//
//    static String getLocationTextDistance(Location location) {
//        return location == null ? "Unknown location" :
//                "(" + location.getSpeed() + ")";//값 찍어주기
//
//    }

    static String getLocationTitle(Context context) {//노티피케이션에서 시간을 보여줍니다.
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }

}
