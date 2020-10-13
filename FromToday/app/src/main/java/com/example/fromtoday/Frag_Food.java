package com.example.fromtoday;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Frag_Food extends Fragment implements View.OnClickListener {
    //식단
    public static ArrayList<String> Menu_Food;

    public static ArrayList<String> getMenu_Food() {
        return Menu_Food;
    }

    public static void setMenu_Food(ArrayList<String> menu_Food) {
        Menu_Food = menu_Food;
    }

    //식단 칼로리
    public static ArrayList<String> Menu_Calrorie;

    public static ArrayList<String> getMenu_Calrorie() {
        return Menu_Calrorie;
    }

    public static void setMenu_Calrorie(ArrayList<String> menu_Calrorie) {
        Menu_Calrorie = menu_Calrorie;
    }

    //사용자 식단
    public static ArrayList<String> UserFood;

    public static ArrayList<String> getUserFood() {
        return UserFood;
    }

    public static void setUserFood(ArrayList<String> map_User) {
        UserFood = map_User;
    }

    //사용자 칼로리
    public static ArrayList<String> UserCalrorie;

    public static ArrayList<String> getUserCalrorie() {
        return UserCalrorie;
    }

    public static void setUserCalrorie(ArrayList<String> userCalrorie) {
        UserCalrorie = userCalrorie;
    }
    private static final int REQUESTCODE_MORNIGN = 1;
    private static final int REQUESTCODE_AFTERNOON = 2;
    private static final int REQUESTCODE_DINNER = 3;
    private static final int MALE = 2500;
    private static final int FEMALE = 2000;

    private View view;
    private ProgressBar progress;
    //식단 내용, 식단 값
    private TextView morningfood;
    private TextView morningcalrorie;
    private TextView afternoonfood;
    private TextView afternoonclarorie;
    private TextView dinnerfood;
    private TextView dinnercalrorie;
    private TextView totalcal;
    private TextView tvbartext;
    //button
    private CardView morning;
    private CardView afternoon;
    private CardView dinner;
    private TextView foodclear;

    private String breakfast;
    private String lunch_Menu;
    private String dinner_Menu;
    private String user_Breakfast;
    private String user_Lunch;
    private String user_Dinner;

    private String brakfastCalrorie;
    private String lunch_MenuCalrorie;
    private String dinner_MenuCalrorie;

    int morningKcal;
    int lunchKcal;
    int dinnerKcal;
    int totalSum = 0;

    String email;
    String  gender;

    SharedPreferences pre;
    SharedPreferences currentUser;
    public SharedPreferences dayKcal;
    FoodService foodService;

    private Boolean alarmCall = false;
    private BarChart mBarChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //사용할 xml id 선언
        view = inflater.inflate(R.layout.frag_food, container, false);
        morning = view.findViewById(R.id.morning);
        morningfood = view.findViewById(R.id.morningfood);
        morningcalrorie = view.findViewById(R.id.morningcalrorie);
        afternoon = view.findViewById(R.id.afternoon);
        afternoonfood = view.findViewById(R.id.afternoonfood);
        afternoonclarorie = view.findViewById(R.id.afternooncalrorie);
        dinner = view.findViewById(R.id.dinner);
        dinnerfood = view.findViewById(R.id.dinnerfood);
        dinnercalrorie = view.findViewById(R.id.dinnercalrorie);
        totalcal = view.findViewById(R.id.totalcal);
        foodclear = view.findViewById(R.id.foodclear);
        progress = view.findViewById(R.id.progress);
        tvbartext = view.findViewById(R.id.tvbartext);
        mBarChart = (BarChart)view.findViewById(R.id.tab1_chart_2);
        //Frag_Food에서 사용할 SharedPreferences 저장소
        currentUser = getActivity().getSharedPreferences("currentUser",getActivity().MODE_PRIVATE);
        dayKcal = getActivity().getSharedPreferences("dayKcal",getActivity().MODE_PRIVATE);

        //searchFood();
        if (getMenu_Food() == null || Menu_Food.size() == 0) {
            System.out.println(getMenu_Food());
        }

        //아침 식단짜기
        morning.setOnClickListener(this);
        //점심 식단짜기
        afternoon.setOnClickListener(this);
        //저녁 식단짜기
        dinner.setOnClickListener(this);
        //식단 초기화
        foodclear.setOnClickListener(this);



        return view;
    }
    //서비스 내부로 Set 되어 스텝카운트의 변화와 Unbind 의 결과를 전달하는 콜백 객체의 구현체
    private FoodCallback foodCallback = new FoodCallback() {
        //FoodService 에서 callback 을 받아 fragement 화면 초기화
        @Override
        public void onFoodCallback(int iNIT_DATA) {
            Log.d("sung",""+iNIT_DATA);
            //call back boolean 값
            alarmCall = true;
            //fragment 화면 초기화
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(Frag_Food.this).attach(Frag_Food.this).commit();
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

//            Toast.makeText(getActivity(), "예스바인딩", Toast.LENGTH_SHORT).show();
            FoodService.MyBinder mb = (FoodService.MyBinder) service;
            foodService = mb.getService();
            foodService.setCallback(foodCallback);

        }
        // 사실상 서비스가 킬되거나 아예 죽임 당했을 때만 호출된다고 보면 됨
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // stopService 또는 unBindService 때 호출되지 않음.
            Toast.makeText(getActivity(), "디스바인딩", Toast.LENGTH_SHORT).show();
        }
    };
    //Frag_Food onclick listener
    @Override
            public void onClick(View view) {
                int requestCode = 0;
                switch (view.getId()){
                    case R.id.morning:
                        requestCode = REQUESTCODE_MORNIGN;
                        break;

                    case R.id.afternoon:
                        requestCode = REQUESTCODE_AFTERNOON;
                        break;

                    case R.id.dinner:
                        requestCode = REQUESTCODE_DINNER;
                        break;

                    case R.id.foodclear:
                        clearFood();
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.detach(Frag_Food.this).attach(Frag_Food.this).commit();
                        return;

                    default:
                        return;
        }
        Intent intent = new Intent(getActivity(),FoodDataActivity.class);
        startActivityForResult(intent,requestCode);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("여기는 onResume","onResume");

        gender = currentUser.getString("gender",null);
        email = currentUser.getString("email",null);

        System.out.println("gender value:"+gender);
        System.out.println("gender value:"+email);

        searchFood();
        doDayOfTheWeek();
        setBarChart();


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUESTCODE_MORNIGN) {
            if(getMenu_Food() != null && getUserFood() != null){
                morningfood.setText("");
                breakfast = getMenu_Food().toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\,","");
                user_Breakfast = getUserFood().toString().replaceAll("\\[","").replaceAll("\\]","").replaceAll("\\,","")+" ";
                brakfastCalrorie = ""+FoodCalrorieSum();

                SharedPreferences.Editor editor = pre.edit();
                editor.putString("breakfast", breakfast);
                editor.putString("user_Breakfast", user_Breakfast);
                editor.putString("brakfastCalrorie", brakfastCalrorie);
                editor.commit();
            }
        } else if (requestCode == REQUESTCODE_AFTERNOON) {
            if(getMenu_Food() != null && getUserFood() != null){
                afternoonfood.setText("");
                lunch_Menu=getMenu_Food().toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\,","");
                user_Lunch=getUserFood().toString().replaceAll("\\[","").replaceAll("\\]","").replaceAll("\\,","")+" ";
                lunch_MenuCalrorie=""+FoodCalrorieSum();

                SharedPreferences.Editor editor=pre.edit();
                editor.putString("lunch_Menu",lunch_Menu);
                editor.putString("user_Lunch",user_Lunch);
                editor.putString("lunch_MenuCalrorie",lunch_MenuCalrorie);
                editor.commit();
            }
        } else {
            if(getMenu_Food() != null && getUserFood() != null) {
                dinnerfood.setText("");
                dinner_Menu=getMenu_Food().toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\,","");
                user_Dinner=getUserFood().toString().replaceAll("\\[","").replaceAll("\\]","").replaceAll("\\,","")+" ";
                dinner_MenuCalrorie=""+FoodCalrorieSum();

                SharedPreferences.Editor editor=pre.edit();
                editor.putString("dinner_Menu",dinner_Menu);
                editor.putString("user_Dinner",user_Dinner);
                editor.putString("dinner_MenuCalrorie",dinner_MenuCalrorie);
                editor.commit();
            }
        }
    }
    private void searchFood() {

        pre = getActivity().getSharedPreferences(""+email, getActivity().MODE_PRIVATE);

        morningfood.setText("");
        afternoonfood.setText("");
        dinnerfood.setText("");

        if(pre.getString("breakfast",null)!= null && pre.getString("user_Breakfast",null) != null){
            morningfood.append("   "+pre.getString("breakfast",null) + pre.getString("user_Breakfast",null));
            morningcalrorie.setText(pre.getString("brakfastCalrorie",null) + "kcal");
            morningKcal = Integer.parseInt(pre.getString("brakfastCalrorie",null));
        }

        if(pre.getString("lunch_Menu",null)!= null && pre.getString("user_Lunch",null) != null){
            afternoonfood.append("   "+pre.getString("lunch_Menu",null) + pre.getString("user_Lunch",null));
            afternoonclarorie.setText(pre.getString("lunch_MenuCalrorie",null) + "kcal");
            lunchKcal = Integer.parseInt(pre.getString("lunch_MenuCalrorie",null));
        }

        if(pre.getString("dinner_Menu",null) != null && pre.getString("user_Dinner",null) != null){
            dinnerfood.append("   "+pre.getString("dinner_Menu",null) + pre.getString("user_Dinner",null));
            dinnercalrorie.setText(pre.getString("dinner_MenuCalrorie",null) + "kcal");
            dinnerKcal = Integer.parseInt(pre.getString("dinner_MenuCalrorie",null));
        }
        totalSum = morningKcal + lunchKcal + dinnerKcal;
        totalcal.setText("" + totalSum);
        System.out.println("totalSum value:"+totalSum);
        setProgressBar();

        dayKcal = getActivity().getSharedPreferences("dayKcal",getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = dayKcal.edit();
        editor.putInt("dayKcal",totalSum);
        editor.commit();


    }
    //식단 초기화
    private void clearFood() {
        SharedPreferences.Editor editor = pre.edit();

        editor.remove("breakfast");
        editor.remove("user_Breakfast");
        editor.remove("brakfastCalrorie");

        editor.remove("lunch_Menu");
        editor.remove("user_Lunch");
        editor.remove("lunch_MenuCalrorie");

        editor.remove("dinner_Menu");
        editor.remove("user_Dinner");
        editor.remove("dinner_MenuCalrorie");
        editor.commit();

        morningfood.setText("");
        morningcalrorie.setText("");

        afternoonfood.setText("");
        afternoonclarorie.setText("");

        dinnerfood.setText("");
        dinnercalrorie.setText("");
        totalcal.setText("0");
        progress.setProgress(0);

        totalSum = 0;
        morningKcal = 0;
        lunchKcal = 0;
        dinnerKcal = 0;
    }
    //내장 db 식단 칼로리 계산
    public int FoodCalrorieSum() {
        int sum=0;
        for(int i = 0 ; i<Menu_Food.size();i++) {
            sum += Integer.parseInt(getMenu_Calrorie().get(i));
        }
        sum += UserCalrorieSum();
        return sum;
    }
    //사용자 추가 식단 칼로리 계산
    public int UserCalrorieSum() {
        int total = 0;
        for(int i = 0 ; i<UserCalrorie.size(); i++) {
            total+=Integer.parseInt(getUserCalrorie().get(i));
        }
        return total;
    }
    // 칼로리 프로그레스바 초기화
    private void setProgressBar() {

        if(gender.equals("male")){
            progress.setMax(MALE);
            tvbartext.setText(" / 2500 kcal");
            progress.setProgress(totalSum);
        }
        else if(gender.equals("female")){
            progress.setMax(FEMALE);
            tvbartext.setText(" / 2000 kcal");
            progress.setProgress(totalSum);
        }
    }
    // 날짜별 막대 그래프
    private void setBarChart() {
        dayKcal = getActivity().getSharedPreferences("dayKcal",getActivity().MODE_PRIVATE);

        if(mBarChart != null) {
            int monday = dayKcal.getInt("monday",0);
            int tuesday = dayKcal.getInt("tuesday",0);
            int wednesday = dayKcal.getInt("wednesday",0);
            int thursday = dayKcal.getInt("thursday",0);
            int friday = dayKcal.getInt("friday",0);
            int saturday = dayKcal.getInt("saturday",0);
            int sunday = dayKcal.getInt("sunday",0);
            Log.i("sung","monday"+monday);
            Log.i("sung","sunday"+sunday);
            //int mondayKcal = totalSum;
            //Log.d("setBarChart value:",""+mondayKcal);
            mBarChart.clearChart();
            // BarChar 데이터 입력
            mBarChart.addBar(new BarModel("일", sunday, 0xFFCff0DA));
            mBarChart.addBar(new BarModel("월", monday, 0xFF88DBA3));
            mBarChart.addBar(new BarModel("화", tuesday, 0xFF90C695));
            mBarChart.addBar(new BarModel("수", wednesday, 0xFF3B8686));
            mBarChart.addBar(new BarModel("목", thursday, 0xFF3AC569));
            mBarChart.addBar(new BarModel("금", friday, 0xFF3B8686));
            mBarChart.addBar(new BarModel("토", saturday, 0xFFCFF09E));
            // BarChar 애니메이션 효과로 시작
            mBarChart.startAnimation();

        }
    }
    // 요일별 데이터 저장
    private void doDayOfTheWeek(){
        //달력 날자 받아오기
        Calendar calendar = Calendar.getInstance(); //캘린더 인스턴스 받아오기
        //Calendar.DAY_OF_WEEK로 오늘 요일을 받아온후 변수에 저장해준다 이 변수는 오늘 요일이다.
        int calendar_week = calendar.get(Calendar.DAY_OF_WEEK);
        dayKcal = getActivity().getSharedPreferences("dayKcal",getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = dayKcal.edit();
        switch (calendar_week) {
            case 1:
                int sunday = dayKcal.getInt("dayKcal",0);
                editor.putInt("sunday",sunday);
                editor.apply();
                Log.i("Geon", "sundayKcal : " );
                if(alarmCall == true) {
                    editor = dayKcal.edit();
                    editor.remove("monday");
                    editor.remove("tuesday");
                    editor.remove("wednesday");
                    editor.remove("thursday");
                    editor.remove("friday");
                    editor.remove("saturday");
                    editor.commit();
                }
                alarmCall = false;
                break;
            case 2:
                int monday = dayKcal.getInt("dayKcal",0);
                editor.putInt("monday",monday);
                editor.apply();
                alarmCall = false;
                Log.i("Geon", "mondayKcal : "+monday );
                break;
            case 3:
                int tuesday = dayKcal.getInt("dayKcal",0);
                editor.putInt("tuesday",tuesday);
                editor.apply();
                alarmCall = false;
                Log.i("Geon", "tuesdayKcal : " );
                break;
            case 4:
                int wednesday = dayKcal.getInt("dayKcal",0);
                editor.putInt("wednesday",wednesday);
                editor.apply();
                alarmCall = false;
                Log.i("Geon", "wednesdayKcal : " );
                break;
            case 5:
                int thursday = dayKcal.getInt("dayKcal",0);
                editor.putInt("thursday",thursday);
                editor.apply();
                alarmCall = false;
                Log.i("Geon", "thursdayKcal : "+thursday );
                break;
            case 6:
                int friday = dayKcal.getInt("dayKcal",0);
                editor.putInt("friday",friday);
                editor.apply();
                alarmCall = false;
                Log.i("Geon", "fridayKcal : ");
                break;
            case 7:
                int saturday = dayKcal.getInt("dayKcal",0);
                editor.putInt("saturday",saturday);
                editor.apply();
                alarmCall = false;
                Log.i("Geon", "saturdayKcal : " );
                break;
        }
    }

}
