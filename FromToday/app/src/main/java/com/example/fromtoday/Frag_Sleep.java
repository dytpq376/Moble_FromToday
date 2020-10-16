package com.example.fromtoday;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;
import static android.app.Service.START_STICKY;
import static androidx.core.content.ContextCompat.getSystemService;
import static com.example.fromtoday.StepService.SEND_INIT;

public class Frag_Sleep extends Fragment implements View.OnClickListener{
    View view;

    private TextView textV;
    private TextView textA;
    private TextView tempText;
    private TextView humText;
    private TextView potText;
    private TextView tv1;
    private TextView tv2;

    private ImageView girl;
    private ImageView man;
    //    TextView textT;
    Button connectMenu;
    Button sendMenu;

    private Button btnMan;
    private Button btnGirl;


    private static final int REQUEST_ENABLE_BT = 10; // 블루투스 활성화 상태
    private BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터
    private Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋
    private BluetoothDevice bluetoothDevice; // 블루투스 디바이스
    private BluetoothSocket bluetoothSocket = null; // 블루투스 소켓
    private OutputStream outputStream = null; // 블루투스에 데이터를 출력하기 위한 출력 스트림
    private InputStream inputStream = null; // 블루투스에 데이터를 입력하기 위한 입력 스트림
    private Thread workerThread = null; // 문자열 수신에 사용되는 쓰레드
    private byte[] readBuffer; // 수신 된 문자열을 저장하기 위한 버퍼
    private int readBufferPosition; // 버퍼 내 문자 저장 위치
    private int pariedDeviceCount; // 페어링 된 Device 숫자 세기
    private double discomfortIndex;
    private double temp;
    private double hum;
    private double pot;
    private double [] chartvalue;
    private List<Double> pot_values = new ArrayList<>();
    private List<Double> temp_values = new ArrayList<>();
    private List<Double> hum_values = new ArrayList<>();

    // SensorManager(StepDetector)
    private SensorManager sensorManager;
    private Sensor stepDetectorSensor;
    private Sensor stepCountSensor;
    // BindService CallBack
    static StepCallback callback;

    String[] humarray = new String[12];
    String[] temparray = new String[12];
    String[] potarray = new String[12];

    ValueLineChart mCubicValueLineChart;
    ValueLineSeries series;
    SleepService sleepService;

//    private double temp = new ArrayList<>();
//    private double hum = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("sungwon","여기는 onCreateView");
        view = inflater.inflate(R.layout.frag_sleep, container, false);

        Log.d("view", "view");

        super.onCreate(savedInstanceState);

        textV = view.findViewById(R.id.textV);
        textA = view.findViewById(R.id.texta);
        tempText = view.findViewById(R.id.tempText);
        humText = view.findViewById(R.id.humText);
        potText = view.findViewById(R.id.potText);

        man = view.findViewById(R.id.Man);
        girl = view.findViewById(R.id.Girl);
//        textT = (TextView)view.findViewById(R.id.textT);

        btnMan= view.findViewById(R.id.btnMan);
        btnGirl = view.findViewById(R.id.btnGirl);
        connectMenu = view.findViewById(R.id.btnConnect);
        sendMenu = view.findViewById(R.id.btnSend);
        connectMenu.setOnClickListener(this);
        sendMenu.setOnClickListener(this);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // 기본 어댑터로 설정

        btnMan.setOnClickListener(this);
        btnGirl.setOnClickListener(this);

        // Line 그래프 선언
        mCubicValueLineChart = view.findViewById(R.id.cubiclinechart);
        series = new ValueLineSeries();

        if(hum != 0){
            setLayoutBySensorValue(hum, temp, pot);
            Log.e("hum", String.valueOf(hum));
            Log.e("temp", String.valueOf(temp));
            Log.e("pot", String.valueOf(pot));
        }
        chartvalue = new double[12];

        if (bluetoothAdapter == null) // 블루투스 지원하지 않을 때
        {
            //empty
        }
        else { // 디바이스가 블루투스를 지원 할 때
            if (bluetoothAdapter.isEnabled()) { // 블루투스가 활성화 상태 (기기에 블루투스가 켜져있음)
                //selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
            }
            else { // 블루투스가 비 활성화 상태 (기기에 블루투스가 꺼져있음)
                // 블루투스를 활성화 하기 위한 다이얼로그 출력
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                // 선택한 값이 onActivityResult 함수에서 콜백된다.
                startActivityForResult(intent, REQUEST_ENABLE_BT);
            }
        }


        Intent sleepIntent = new Intent(getActivity(), SleepService.class);
        getActivity().startService(sleepIntent);

        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("sungwon","여기는 onActivityResult");
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (requestCode == RESULT_OK) { // '사용'을 눌렀을 때
                    selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
                } else { // '취소'를 눌렀을 때
                    // 여기에 처리 할 코드를 작성하세요.
                }
                break;
        }
    }

    public void selectBluetoothDevice() {
        // 이미 페어링 되어있는 블루투스 기기를 찾습니다.
        devices = bluetoothAdapter.getBondedDevices();
        // 페어링 된 디바이스의 크기를 저장
        pariedDeviceCount = devices.size();
        // 페어링 되어있는 장치가 없는 경우
        if (pariedDeviceCount == 0) {
            // 페어링을 하기위한 함수 호출
        }
        // 페어링 되어있는 장치가 있는 경우
        else {
            // 디바이스를 선택하기 위한 다이얼로그 생성
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("블루투스 모듈과 연결하세요.");
            // 페어링 된 각각의 디바이스의 이름과 주소를 저장
            List<String> list = new ArrayList<>();
            // 모든 디바이스의 이름을 리스트에 추가
            for (BluetoothDevice bluetoothDevice : devices) {
                list.add(bluetoothDevice.getName());
            }
            list.add("나가기");
            // List를 CharSequence 배열로 변경
            final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);
            list.toArray(new CharSequence[list.size()]);

            // 해당 아이템을 눌렀을 때 호출 되는 이벤트 리스너
            builder.setItems(charSequences, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 해당 디바이스와 연결하는 함수 호출
                    connectDevice(charSequences[which].toString());
                }
            });

            // 뒤로가기 버튼 누를 때 창이 안닫히도록 설정
            builder.setCancelable(false);
            // 다이얼로그 생성
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }


    public void connectDevice(String deviceName) {
        // 페어링 된 디바이스들을 모두 탐색
        for (BluetoothDevice tempDevice : devices) {
            // 사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료
            if (deviceName.equals(tempDevice.getName())) {
                bluetoothDevice = tempDevice;
                break;
            }
        }
        // UUID 생성
        UUID uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        // SPP 통신
        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();
            // 블루투스 소켓 연결
            //imgConnect.setImageResource(R.drawable.connect); // 이미지뷰 초록색으로 변경
            // 데이터 송,수신 스트림
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();
            // 데이터 수신 함수 호출
            final Handler SleepMessage = new Handler();
            // 데이터를 수신하기 위한 버퍼를 생성
            readBufferPosition = 0;
            readBuffer = new byte[1024];
            // 데이터를 수신하기 위한 쓰레드 생성
            workerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            // 데이터를 수신했는지 확인합니다.
                            int byteAvailable = inputStream.available();
                            //데이터가 수신 된 경우
                            if (byteAvailable > 0) {
                                // 입력 스트림에서 바이트 단위로 읽어 옵니다.
                                byte[] bytes = new byte[byteAvailable];
                                inputStream.read(bytes);
                                // 입력 스트림 바이트를 한 바이트씩 읽어 옵니다.

                                for (int i = 0; i < byteAvailable; i++) {
                                    byte tempByte = bytes[i];
                                    // 개행문자를 기준으로 받음(한줄)
                                    if (tempByte == '\n') {
                                        // readBuffer 배열을 encodedBytes로 복사
                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                        // 인코딩 된 바이트 배열을 문자열로 변환
                                        final String text = new String(encodedBytes, StandardCharsets.US_ASCII);
                                        Log.d("text: ",text);
                                        readBufferPosition = 0;
                                        String[] textArray1 = text.split(" "); // 받아온 데이터를 공백을 기준으로 자름

                                        // 습도 온도 순으로 전송되게 하였으므로 공백을 기준으로 잘라 각각 값을 넣어준다
                                        hum = Double.parseDouble(textArray1[0]);
                                        temp = Double.parseDouble(textArray1[1]);
                                        pot = Double.parseDouble(textArray1[2]);
                                        Log.e("hum2", String.valueOf(hum));
                                        Log.e("temp2", String.valueOf(temp));
                                        Log.e("pot2", String.valueOf(pot));

//                                        setLayoutBySensorValue(hum,temp,pot);
//                                        humText.setText(humText.getText());
//                                        tempText.setText(tempText.getText());
//                                        potText.setText(potText.getText());
//
//                                        Log.d("hum: ", String.valueOf(hum));
//                                        Log.d("temp: ", String.valueOf(temp));
                                        if(view.getId()==R.id.btnSend) // 측정 버튼 누를 때
                                        {
                                            // 센서 값을 hum, temp에 저장 (hum과 temp의 값이 동일하게 들어간다.)
                                            setLayoutBySensorValue(hum,temp,pot);
                                            humText.setText(humText.getText());
                                            tempText.setText(tempText.getText());
                                            potText.setText(potText.getText());
                                            updateViewLinechart();

                                        }
                                        // eDouble(textArray[1]);
                                    } // 개행 문자가 아닐 경우
                                    else {
                                        readBuffer[readBufferPosition++] = tempByte;
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            // 1초마다 받아옴
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Log.e("InterruptedException", "쓰레드 종료!!!!!!!!!: ");
                            e.printStackTrace();
                        }
                    }
                }
            });
            workerThread.interrupt();
            workerThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    double calculateDiscomfortIndex(double _hum, double _temp,double _pot) {
        double _discomfortIndex;
        double index1 = 9 / 5 * _temp;
        double index2 = 1 - _hum;
        double index3 = index1 - 26;
        double index4 = 55 / 100 * index2;
        double index5 = index4 * index3;
        _discomfortIndex = _pot + index1 - index5 + 32;

        Log.d("DiscomfortIndex", String.valueOf(_discomfortIndex));
        return _discomfortIndex;

    }


    void setLayoutBySensorValue(double _hum, double _temp, double _pot) {
        discomfortIndex = calculateDiscomfortIndex( _hum, _temp, _pot);
        Log.d("discomfortIn", String.valueOf(discomfortIndex));

        textV.setText("수면적정지수 : " + discomfortIndex);

        Log.e("hum3", String.valueOf(_hum));
        Log.e("temp3", String.valueOf(_temp));
        Log.e("pot3", String.valueOf(_pot));

        pot_values.add(0d);
        potText.setText(_pot+"bpm");
        hum_values.add(0d);
        humText.setText(_hum + "%");
        temp_values.add(0d);
        tempText.setText(_temp + "°C");
        if (discomfortIndex < 128) // 불쾌지수 낮음
            textA.setText("전원 쾌적함을 느낍니다.");
        else if (discomfortIndex <= 134) // 불쾌지수 보통
            textA.setText("불쾌감을 나타내기 시작합니다");
        else if (discomfortIndex <= 139) // 불쾌지수 보통
            textA.setText("50%정도 불쾌감을 느낍니다.");
        else // 불쾌지수 매우나쁨
            textA.setText("전원 불쾌감을 느낍니다.");
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.btnConnect) // 다시 연결하고 싶을 때
        {
            //imgConnect.setImageResource(R.drawable.disconnect); // 이미지뷰 빨간색으로 변경
            if(bluetoothAdapter.isEnabled())
            { // 블루투스가 활성화 상태라면
                selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
//                updateViewLinechart();
            }
        }
        else if(view.getId()==R.id.btnSend) // 측정 버튼 누를 때
        {
            // 센서 값을 hum, temp에 저장 (hum과 temp의 값이 동일하게 들어간다.)

            setLayoutBySensorValue(hum, temp, pot);
            updateViewLinechart();
        }
        else if(view.getId()==R.id.btnMan){
            Log.e("btnMan", "btnManbtnMan" );
            man.setVisibility(View.VISIBLE);
            girl.setVisibility(View.INVISIBLE);
        }
        else if(view.getId()==R.id.btnGirl){
            Log.e("btnGirl", "btnGirlbtnGirl" );
            man.setVisibility(View.INVISIBLE);
            girl.setVisibility(View.VISIBLE);
        }
    }

    private void updateViewLinechart(){
        series.setColor(0xFF56B7F1);
        series.addPoint(new ValueLinePoint("12시", (float) discomfortIndex));
        series.addPoint(new ValueLinePoint("1시", (float)discomfortIndex));
        series.addPoint(new ValueLinePoint("2시", (float)discomfortIndex));
        series.addPoint(new ValueLinePoint("3시", (float)discomfortIndex));
        series.addPoint(new ValueLinePoint("4시", (float)discomfortIndex));
        series.addPoint(new ValueLinePoint("5시", (float)discomfortIndex));
        series.addPoint(new ValueLinePoint("6시", (float)discomfortIndex));
        series.addPoint(new ValueLinePoint("7시", (float)discomfortIndex));
        series.addPoint(new ValueLinePoint("8시", (float)discomfortIndex));
        series.addPoint(new ValueLinePoint("9시", (float)discomfortIndex));
        series.addPoint(new ValueLinePoint("10시", (float)discomfortIndex));
        series.addPoint(new ValueLinePoint("11시", (float)discomfortIndex));
        mCubicValueLineChart.addSeries(series);
        mCubicValueLineChart.startAnimation();
//        mCubicValueLineChart.setScaleY(30000);

    }

    @Override
    public void onResume() {
        // bindService 시작
        Intent sleepIntent = new Intent(getActivity(), SleepService.class);
        getActivity().bindService(sleepIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        super.onResume();
        Log.i("sungwon","여기는 onResume");



        TimerTask myTask = new TimerTask() {
            public void run() {
                Log.d("myTask", "run()");
                setLayoutBySensorValue(hum, temp, pot);
            }
        };
        Timer timer = new Timer();
        //timer.schedule(myTask, 5000);  // 5초후 실행하고 종료
        timer.schedule(myTask, 1000, 1000); // 5초후 첫실행, 3초마다 계속실행.

        updateViewLinechart();
    }

    private SleepCallback sleepCallback = new SleepCallback() {

        @Override
        public void onSleepCallback(int iNIT_DATA) {
            Log.d("sung",""+iNIT_DATA);

        }

        @Override
        public void onUnbindService() {
            /*Toast.makeText(getActivity(), "디스바인딩!!", Toast.LENGTH_SHORT).show();*/
        }
    };

    // 서비스 바인드를 담당하는 객체의 구현체
    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Log.i("jenn", "serviceConnection :onServiceConnected " );

            SleepService.MyBinder mb = (SleepService.MyBinder) service;
            sleepService = mb.getService();
            sleepService.setCallback(sleepCallback);

        }
        // 사실상 서비스가 킬되거나 아예 죽임 당했을 때만 호출된다고 보면 됨
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // stopService 또는 unBindService 때 호출되지 않음.
            Toast.makeText(getActivity(), "디스바인딩", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        Log.i("sungwon","여기는 onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("sungwon","여기는 onStop");
    }


//    public void onDestroyView(){
//        super.onDestroyView();
//        workerThread.interrupt();//쓰레드 종료 구문
//        Log.i("onDestroyView","여기는 onDestroyViewonDestroyViewonDestroyView");
//    }
}
