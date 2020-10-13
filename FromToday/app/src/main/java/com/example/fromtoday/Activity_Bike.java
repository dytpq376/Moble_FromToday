/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.fromtoday;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.dinuscxj.progressbar.BuildConfig;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

/**
 이 샘플의 유일한 활동.
 *
 * 참고: 사용자는 위치와 관련하여 "Q"에 다음 세 가지 옵션이 있다.
 * <>
 * 항상 허용.
 * app 사용 중, 즉 app가 포그라운드일 때 허용(</li)
 * 위치 전혀 허용하지 않음
 * </>
 * 이 앱은 사용자가 탐색할 때 포그라운드 서비스(통지에 표시)를 생성하기 때문에
 * 앱에서 벗어나 "사용 중" 위치만 있으면 된다. 즉, 부탁할 필요가 없다.
 * 항상 위치(매니페스트에 추가 권한이 필요함)
 *
 * 또한 "Q"는 개발자가 매니페스트에 포그라운드 서비스 유형을 지정해야 함(이 경우)
 * case, "위치").
 *
 * 참고: Foreground Services의 경우 "P"는 매니페스트에서 추가 권한이 필요하다. 확인해주세요
 * 자세한 내용은 프로젝트 매니페스트를 참조하십시오.
 *
 * 참고: "O" 장치에서 백그라운드에서 실행되는 앱의 경우(targetSdkVersion에 관계 없음)
 * 위치는 앱이 전면에 없을 때 요청된 것보다 적게 계산할 수 있다.
 * 포그라운드 서비스를 사용하는 앱 - 삭제할 수 없는 앱 표시
 * 통지 - 이전과 같이 백그라운드 위치 제한을 무시하고 위치 업데이트를 요청할 수 있음
 *
 * 이 샘플은 위치 업데이트에 장기 실행 바인딩 및 시작 서비스를 사용한다. 서비스는
 * 이 활동의 전경 상태 인식(이 활동의 유일한 바인딩된 클라이언트)
 * 이 샘플. 위치 업데이트를 요청한 후 활동이 중단되면
 * 서비스가 전경서비스로 홍보하고 위치 업데이트를 계속 수신함
 * 활동이 다시 전경에 돌아오면 전경에 서비스가 중지되고
 * 해당 전경 서비스와 관련된 통지가 제거됨.
 *
 * Foreground 서비스 알림이 표시되는 동안 사용자는 이 서비스를 시작할 수
 * 통지로부터의 활동. 사용자는 또한 위치 업데이트를 에서 직접 제거할 수 있다.
 * 통지. 이렇게 하면 통지가 해제되고 서비스가 중지된다.
 */
public class
Activity_Bike extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = Activity_Run.class.getSimpleName();

    // 런타임 권한을 확인하는 데 사용됨.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // roadcastReceiver는 서비스에서 브로드캐스트를 호출한다.
//    private MyReceiver myReceiver;

    // 위치 업데이트를 가져오는 데 사용되는 서비스에 대한 참조.
    //서비스 클래스를 참조
    private LocationUpdatesService mService = null;

    // 서비스의 바인딩 상태를 추적하십시오.
    private boolean mBound = false;

    //프리퍼런스
    public SharedPreferences user_Value;

    // UI 요소.
    private Button mRequestLocationUpdatesButton;
    private Button mRemoveLocationUpdatesButton;
    private TextView tv_kcal,tv_speed,tv_distance,tv_time;

    //DB저장용 데이터 변수 입니다.
    private double db_speed, db_distance, db_kcal;
    private int db_time;
    private double avgspeed;
    private double sumkcal;

    //DTO
    private DatabaseReference mPostReference;

    String resultactivity;

    Activity_DTO activity_dto= new Activity_DTO();

    //요일
    private int calendar_week;

    //서비스 객체를 가지고있는 인텐트 객체
    private Intent intent;

    //값 초기화 부분입니다.
    private double getdistance =0;


    // 서비스에 대한 연결 상태 모니터링,  예스 바인딩
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            System.out.println("이 구문이 보이면 바인딩 성공입니다!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mService.setCallback(speedCallback);
            mBound = true;
            Toast.makeText(Activity_Bike.this, "예스 로케이션 서비스 바인딩", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
            Toast.makeText(Activity_Bike.this, "디스바인딩", Toast.LENGTH_SHORT).show();
        }
    };

    SharedPreferences bikeResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("run부분의 oncreate를 탔습니다!!!!!!!!!");
        super.onCreate(savedInstanceState);
//        myReceiver = new MyReceiver();
        setContentView(R.layout.frag_activity_bike);

        // 설정으로 이동하여 사용자가 권한을 취소하지 않았는지 확인하십시오.
        if (Utils.requestingLocationUpdates(this)) {
            System.out.println("oncreate에서 퍼미션을 체크합니다!!!!!!!!!!");
            if (!checkPermissions()) {
                requestPermissions();
            }
        }
    }

    @Override
    protected void onStart() {
        System.out.println("onstart를 탔습니다!!1!!!!!!!!!");
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        mRequestLocationUpdatesButton = (Button) findViewById(R.id.request_location_updates_button);
        mRemoveLocationUpdatesButton = (Button) findViewById(R.id.remove_location_updates_button);

        tv_speed = findViewById(R.id.tvspeed);
        tv_distance = findViewById(R.id.tvdistance);
        tv_time = findViewById(R.id.tvtime);
        tv_kcal = findViewById(R.id.tv_kcal);


        mRequestLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("운동 시작 버튼을 탔습니다 탔습니다!!!!!!!!!!!");
                if (checkPermissions()) {
                    requestPermissions();
                    intent = new Intent(getApplicationContext(), LocationUpdatesService.class);
                    startService(intent);
                    bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
                } else {
                    System.out.println("로케이션 서비스에서 업데이트 합니다 null입니까?????????");
                    System.out.println("로케이션 서비스에서 업데이트 합니다 null이 아닙니다!!!!!");
                    mService.requestLocationUpdates();
                }
            }
        });


        //운동 종료 버튼을 눌렀습니다.
        mRemoveLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                avgspeed = avgspeed / db_time;
                db_speed = Double.parseDouble(String.format("%.1f", avgspeed));

                //내장 db에 저장 쉐어드프리퍼런스를 사용하여 값을
                bikeResult = getSharedPreferences("bikeResult", MODE_PRIVATE);    // activityValue이름의 파일 생성
                SharedPreferences.Editor editor = bikeResult.edit(); //sharedPreferences를 제어할 editor를 선언
                editor.putString("speed", String.valueOf(db_speed)); // key,value 형식으로 저장
                editor.putString("distance", String.valueOf(db_distance)); // key,value 형식으로 저장
                editor.putString("time", String.valueOf(db_time)); // key,value 형식으로 저장
                editor.putString("kcal", String.valueOf(db_kcal)); // key,value 형식으로 저장
                editor.putString("sumkcal",String.valueOf(sumkcal));
                editor.commit();    //최종 커밋. 커밋을 해야 저장이 된다.

                String speedValue = bikeResult.getString("speed", null);
                String distanceValue = bikeResult.getString("distance", null);
                String timeValue = bikeResult.getString("time", null);
                String kcalValue = bikeResult.getString("kcal",null);
                System.out.println("speedValue======:" + speedValue);
                System.out.println("distanceValue======:" + distanceValue);
                System.out.println("timeValue======:" + timeValue);
                System.out.println("kcalValue======:" + kcalValue);

                //파베
                user_Value = getSharedPreferences("currentUser", MODE_PRIVATE);
                //SharedPreferences.Editor editor = user_Value.edit();
                String strEmail = user_Value.getString("email", null);

                mPostReference = FirebaseDatabase.getInstance().getReference();

                strEmail = strEmail.replaceAll("@", "").replaceAll("[.]", "");
                Calendar calendar = Calendar.getInstance();
                calendar_week = calendar.get(Calendar.DAY_OF_WEEK);

                //파베 저장용 데이터 지정
                activity_dto.bike_distance = distanceValue;
                activity_dto.bike_speed = speedValue;
                activity_dto.bike_time = timeValue;
                activity_dto.bike_kcal = kcalValue;
                activity_dto.bike_Week = String.valueOf(calendar_week);

                //파베에 데이터 저장
                //데이터를 쌓아주고 싶으면 child뒤에 push를 사용한다.
                mPostReference.child("users").child(strEmail).child("activity").child("bike_activity").push().setValue(activity_dto);

                Toast.makeText(Activity_Bike.this, "DB에저장되었습니다.", Toast.LENGTH_SHORT).show();
                //값 초기화 부분

                tv_speed.setText("0.0");
                tv_distance.setText("0.0");
                tv_time.setText("0");
                tv_kcal.setText("0 kcal");

                avgspeed = 0;
                //skcal = 0;
                db_speed = 0;
                db_distance = 0;
                db_time = 0;

                mService.removeLocationUpdates();
            }
        });

        // 활동(다시)이 해제될 때 버튼의 상태를 복원하십시오.
        setButtonsState(Utils.requestingLocationUpdates(this));

        Log.e(TAG, "onStart: bindservice했음?" );
        // 서비스에 바인딩하십시오. 서비스가 포그라운드 모드인 경우, 이 신호가 서비스에 전달됨
        // 이 활동이 전경에 있으므로 서비스는 전경에 있는 모드를 종료할 수 있다.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
        Log.e(TAG, "onStart: 이 구문이 보이면 onstart에서 bindservice를 실행한겁니다." );
    }

    //--------------------------------------------------
    //콜백 하는 부분입니다
    private SpeedCallback speedCallback = new SpeedCallback() {
        @Override
        public void onLocationCallback(double speed, double distance, int time, double sumkcal) {
            Log.e("Geon", "speed : " + speed);

            //콜백을 통해 서비스에서 받아온 값들은 run액티비티에 뿌려줍니다.
            tv_speed.setText(":"+String.format("%.1f",speed));
            tv_distance.setText(":"+String.format("%.1f",distance));
            tv_time.setText(":"+(time/1000));
            tv_kcal.setText(":"+ String.format("%.1f", sumkcal));

            //여기서 avgspeed는 받아온 스피드 값을 다 더해준뒤
            //종료된 시점의 시간을 나누어 주어서 평균 속도로 표기한다.
            avgspeed += speed;

            //받은 값들을 db저장용 변수에 저장해 줍니다.
            db_speed = Double.parseDouble(String.format("%.1f",speed));
            db_distance = Double.parseDouble(String.format("%.1f",distance));
            db_time = Integer.parseInt(String.valueOf((time/1000)));
            db_kcal = db_distance = Double.parseDouble(String.format("%.1f", sumkcal));

            Log.e(TAG, "db_speed: 디비 저장 스피드는"+db_speed );
            Log.e(TAG, "db_speed: 디비 저장 거리는"+db_distance );
            Log.e(TAG, "db_speed: 디비 저장 시간는"+db_time );
            Log.e(TAG, "db_kcal: 디비 저장 칼로리는는" + db_kcal);
        }
    };

    @Override

    protected void onResume() {
        System.out.println("onResume 탔습니다!!1!!!!!!!!!");
        super.onResume();
//        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
//                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));

    }

    @Override
    protected void onPause() {//언 레지스터 하는 과정 -
        System.out.println("onPause 탔습니다!!1!!!!!!!!!");
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {// 바인드 해제
        if (mBound) {
            System.out.println("onStop 탔습니다!!1!!!!!!!!!");
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            // 서비스에서 바인딩을 해제하십시오. 이는 서비스에 이 활동이 더 이상 필요하지 않음을 나타낸다.
            // 전경에, 서비스가 전경에 자신을 홍보함으로써 대응할 수 있다.
            // 서비스.
            unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    /**
     *필요한 권한의 현재 상태를 반환한다.
     */
    private boolean checkPermissions() {// 권한을 체크합니다.
        Log.e("Geon", "speed : checkPermissions을 탔습니다!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
        return  PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        Log.e("Geon", "speed : requestPermissions 탔습니다!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        // 사용자에게 추가적인 근거를 제시하십시오. 사용자가 다음 작업을 거부하면
        // 이전에 요청했지만 "다시 묻지 않음" 확인란은 선택하지 않았다.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.activity_main),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(Activity_Bike.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(Activity_Bike.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     *권한 요청이 완료되면 콜백이 수신됨
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                mService.requestLocationUpdates();
            } else {
                // Permission denied.
                setButtonsState(false);
                Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }

//    /**
//     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
//     * {@link LocationUpdatesService}에서 보낸 브로드캐스트용 수신기
//     */
//    private class MyReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
////            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
////            if (location != null) {
////                tv_value.setText(LocationUpdatesService.getLocationText(location));
////
////                double getSpeed = Double.parseDouble(String.format("%.1f", location.getSpeed()));
////                tv_speed.setText(":"+getSpeed);
////
////                getdistance += getSpeed;
////                System.out.println(getdistance);
////                tv_distance.setText(":"+String.format("%.1f",getdistance));
////
////                Toast.makeText(MainActivity.this, Utils.getLocationText(location),
////                        Toast.LENGTH_SHORT).show();
////            }
//        }
//    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Update the buttons state depending on whether location updates are being requested.
        // 위치 업데이트 요청 여부에 따라 버튼 상태 업데이트
        if (s.equals(Utils.KEY_REQUESTING_LOCATION_UPDATES)) {
            setButtonsState(sharedPreferences.getBoolean(Utils.KEY_REQUESTING_LOCATION_UPDATES,
                    false));
        }
    }

    private void setButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            mRequestLocationUpdatesButton.setEnabled(false);
            mRemoveLocationUpdatesButton.setEnabled(true);
        } else {

            mRequestLocationUpdatesButton.setEnabled(true);
            mRemoveLocationUpdatesButton.setEnabled(false);

        }
    }

    //막대 그래프 부분 코딩입니다

}
