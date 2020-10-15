package com.example.fromtoday;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dinuscxj.progressbar.BuildConfig;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;


public class Activity_Map_Bike extends AppCompatActivity
        implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        PlacesListener {

    private static final String TAG = Activity_Run.class.getSimpleName();

    // 위치 업데이트를 가져오는 데 사용되는 서비스에 대한 참조.
    //서비스 클래스를 참조
    private LocationUpdatesService mService = null;

    //유저 이메일을 받기 위한 프리퍼런스 선언
    public SharedPreferences user_Value;

    SharedPreferences currentUser;

    // 서비스의 바인딩 상태를 추적하십시오.
    private boolean mBound = false;

    private GoogleMap mMap;
    private Marker currentMarker = null;

    //private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초


    // UI 요소.
    private Button mRequestLocationUpdatesButton;
    private Button mRemoveLocationUpdatesButton;
    private TextView tv_value, tv_speed, tv_distance, tv_time, tv_kcal;

    //DB저장용 데이터 변수 입니다.
    private double db_speed, db_distance, db_kcal;
    private int db_time;
    private double avgspeed;
    private double sumkcal;

    //DTO
    private DatabaseReference mPostReference;

    String resultactivity;

    Activity_DTO activity_dto = new Activity_DTO();

    //요일
    private int calendar_week;

    //서비스 객체를 가지고있는 인텐트 객체
    private Intent intent;

    //값 초기화 부분입니다.
    private double getdistance = 0;


    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    boolean needRequest = false;


    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소


    Location mCurrentLocatiion;
    LatLng currentPosition;


    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;

    SharedPreferences runResult;

    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요합니다.
    // (참고로 Toast에서는 Context가 필요했습니다.)
    List<Marker> previous_marker = null;


    // 서비스에 대한 연결 상태 모니터링,  예스 바인딩
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            System.out.println("이 구문이 보이면 바인딩 성공입니다!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mService.setCallback(speedCallback);
            mBound = true;
            //Toast.makeText(Activity_Map_Bike.this, "운동 시작 버튼을 눌러주세요", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
            Toast.makeText(Activity_Map_Bike.this, "디스바인딩", Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.frag_activity_bike);
        previous_marker = new ArrayList<Marker>();
        mLayout = findViewById(R.id.bike_main);
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        if (Utils.requestingLocationUpdates(this)) {
            System.out.println("oncreate에서 퍼미션을 체크합니다!!!!!!!!!!");
            if (!checkPermissions()) {
                requestPermissions();
            }
        }


        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.bike_map);
        mapFragment.getMapAsync(this);



    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "onMapReady :");

        mMap = googleMap;

        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation();


        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)

            startLocationUpdates(); // 3. 위치 업데이트 시작

        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions(Activity_Map_Bike.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }


        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        // 현재 오동작을 해서 주석처리

        //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                Log.d(TAG, "onMapClick :");
            }
        });
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Log.e("위도 경도", "locationCallback: locationCallbacklocationCallback" );
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);

                currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());


                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                        + " 경도:" + String.valueOf(location.getLongitude());

                Log.d(TAG, "onLocationResult : " + markerSnippet);


                //현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet);

                mCurrentLocatiion = location;
            }


        }

    };


    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        } else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);


            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }


            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mMap.setMyLocationEnabled(true);

        }

    }


    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart_run");
//
//        mRequestLocationUpdatesButton.setEnabled(true);
//        mRemoveLocationUpdatesButton.setEnabled(false);


        if (checkPermission()) {
            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            Log.e("mMap222", String.valueOf(mMap));
            if (mMap != null)
                mMap.setMyLocationEnabled(true);
        }

//        PreferenceManager.getDefaultSharedPreferences(this)
//                .registerOnSharedPreferenceChangeListener((SharedPreferences.OnSharedPreferenceChangeListener) this);

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


        //운동 종료 버튼을 클릭
        mRemoveLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                avgspeed = avgspeed / db_time;
                db_speed = Double.parseDouble(String.format("%.1f", avgspeed));

                //내장 디비 연결을 합니다.
                runResult = getSharedPreferences("runResult", MODE_PRIVATE);    // activityValue이름의 파일 생성
                SharedPreferences.Editor editor = runResult.edit(); //sharedPreferences를 제어할 editor를 선언
                editor.putString("speed", String.valueOf(db_speed)); // key,value 형식으로 저장
                editor.putString("distance", String.valueOf(db_distance)); // key,value 형식으로 저장
                editor.putString("time", String.valueOf(db_time)); // key,value 형식으로 저장
                editor.putString("kcal", String.valueOf(db_kcal)); // key,value 형식으로 저장
                editor.putString("sumkcal", String.valueOf(sumkcal));
                editor.commit();    //최종 커밋. 커밋을 해야 저장이 된다.

                mService.removeLocationUpdates(); //서비스 언바인드 부분


                //값이 확인용 출력
                String speedValue = runResult.getString("speed", null);
                String distanceValue = runResult.getString("distance", null);
                String timeValue = runResult.getString("time", null);
                String kcalValue = runResult.getString("kcal", null);
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
                activity_dto.run_distance = distanceValue;
                activity_dto.run_speed = speedValue;
                activity_dto.run_time = timeValue;
                activity_dto.run_kcal = kcalValue;
                activity_dto.run_Week = String.valueOf(calendar_week);

                //파베에 데이터 저장
                //데이터를 쌓아주고 싶으면 child뒤에 push를 사용한다.
                mPostReference.child("users").child(strEmail).child("activity").child("run_activity").push().setValue(activity_dto);

                Toast.makeText(Activity_Map_Bike.this, "DB에저장되었습니다.", Toast.LENGTH_SHORT).show();
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

                mRequestLocationUpdatesButton.setEnabled(true);
                mRemoveLocationUpdatesButton.setEnabled(false);


            }
        });

        // 활동(다시)이 해제될 때 버튼의 상태를 복원하십시오.
        //setButtonsState(Utils.requestingLocationUpdates(this));

        Log.e(TAG, "onStart: bindservice했음?");
        // 서비스에 바인딩하십시오. 서비스가 포그라운드 모드인 경우, 이 신호가 서비스에 전달됨
        // 이 활동이 전경에 있으므로 서비스는 전경에 있는 모드를 종료할 수 있다.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
        Log.e(TAG, "onStart: 이 구문이 보이면 onstart에서 bindservice를 실행한겁니다.");
    }

    private SpeedCallback speedCallback = new SpeedCallback() {
        @Override
        public void onLocationCallback(double speed, double distance, int time, double kcal) {
            Log.e("Geon", "speed : " + speed);

            //콜백을 통해 서비스에서 받아온 값들은 run액티비티에 뿌려줍니다.
            tv_speed.setText(":" + String.format("%.1f", speed));
            tv_distance.setText(":" + String.format("%.1f", distance));
            tv_time.setText(":" + (time / 1000));
            tv_kcal.setText(":" + String.format("%.1f", kcal) + "kcal");


            //여기서 avgspeed는 받아온 스피드 값을 다 더해준뒤
            //종료된 시점의 시간을 나누어 주어서 평균 속도로 표기한다.
            avgspeed += speed;

            //받은 값들을 db저장용 변수에 저장해 줍니다.
            db_speed = Double.parseDouble(String.format("%.1f", speed));
            db_distance = Double.parseDouble(String.format("%.1f", distance));
            db_kcal = Double.parseDouble(String.format("%.1f", kcal));
            db_time = Integer.parseInt(String.valueOf((time / 1000)));
            Log.e(TAG, "db_speed: 디비 저장 스피드는" + db_speed);
            Log.e(TAG, "db_speed: 디비 저장 거리는" + db_distance);
            Log.e(TAG, "db_speed: 디비 저장 시간는" + db_time);
            Log.e(TAG, "db_kcal: 디비 저장 칼로리는는" + db_kcal);
        }
    };

    @Override
    protected void onResume() {
        System.out.println("onResume 탔습니다!!1!!!!!!!!!");
        super.onResume();


    }

    @Override
    protected void onPause() {//언 레지스터 하는 과정 -
        System.out.println("onPause 탔습니다!!1!!!!!!!!!");
        super.onPause();
    }


    @Override
    protected void onStop() {
        if (mBound) {
            System.out.println("onStop 탔습니다!!1!!!!!!!!!");
            // 서비스에서 바인딩을 해제하십시오. 이는 서비스에 이 활동이 더 이상 필요하지 않음을 나타낸다.
            // 전경에, 서비스가 전경에 자신을 홍보함으로써 대응할 수 있다.
            // 서비스.
            unbindService(mServiceConnection);
            mBound = false;
        }
//        PreferenceManager.getDefaultSharedPreferences(this)
//                .unregisterOnSharedPreferenceChangeListener((SharedPreferences.OnSharedPreferenceChangeListener) this);

        super.onStop();

        if (mFusedLocationClient != null) {

            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    //        *필요한 권한의 현재 상태를 반환한다.
    private boolean checkPermissions() {// 권한을 체크합니다.
        Log.e("Geon", "speed : checkPermissions을 탔습니다!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }


    private void requestPermissions() {
        Log.e("Geon", "speed : requestPermissions 탔습니다!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
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
                            ActivityCompat.requestPermissions(Activity_Map_Bike.this,
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
            ActivityCompat.requestPermissions(Activity_Map_Bike.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }


    public String getCurrentAddress(LatLng latlng) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }


        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }


    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {


        if (currentMarker != null) currentMarker.remove();


        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);


        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);

    }


    public void setDefaultLocation() {


        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";


        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);

    }


    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        return false;

    }


    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        Log.i(TAG, "onRequestPermissionResult");
        if (permsRequestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grandResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grandResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                mService.requestLocationUpdates();
            } else {
                // Permission denied.
//                setButtonsState(false);
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

        // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates();
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();

                } else {
                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }

        }
    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Map_Bike.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음");


                        needRequest = true;

                        return;
                    }
                }

                break;
        }
    }

    @Override
    public void onPlacesFailure(PlacesException e) {

    }

    @Override
    public void onPlacesStart() {

    }

    @Override
    public void onPlacesSuccess(final List<Place> places) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Place place : places) {

                    LatLng latLng
                            = new LatLng(place.getLatitude()
                            , place.getLongitude());

                    String markerSnippet = getCurrentAddress(latLng);

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(place.getName());
                    markerOptions.snippet(markerSnippet);
                    Marker item = mMap.addMarker(markerOptions);
                    previous_marker.add(item);

                }

                //중복 마커 제거
                HashSet<Marker> hashSet = new HashSet<Marker>();
                hashSet.addAll(previous_marker);
                previous_marker.clear();
                previous_marker.addAll(hashSet);
                previous_marker.addAll(hashSet);

            }
        });
    }



    @Override
    public void onPlacesFinished() {

    }

    public void showPlaceInformation(LatLng location) {
        mMap.clear();//지도 클리어

        if (previous_marker != null)
            previous_marker.clear();//지역정보 마커 클리어

        new NRPlaces.Builder()
                .listener(Activity_Map_Bike.this)
                .key("AIzaSyAkURIuz7vD8AM8H2d1JnDhmZ7YwdxphLg")
                .latlng(location.latitude, location.longitude)//현재 위치
                .radius(2000) //500 미터 내에서 검색
                .type(PlaceType.GYM) //음식점
                .build()
                .execute();

    }


}