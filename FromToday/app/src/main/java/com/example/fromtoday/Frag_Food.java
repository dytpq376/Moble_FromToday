package com.example.fromtoday;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
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
    private static final int MALE = 3200;
    private static final int FEMALE = 2600;

    //칼로리 계산
//    public int calrorieSum() {
//        int total = 0;
//        int keySetting = 0;
//        //receive hashmap 의 키값
//        Set set = Map_Breakfast.keySet();
//        Iterator iterator = set.iterator();
//        //receive에서 받아온 키 값을 저장해줄 key String 배열
//        String[] key = new String[2];
//        //hashmap 안에 null 값일 때 까지 iterator 인테페이스로 순차적 탐색
//        while (iterator.hasNext()) {
//            //key String 배열 초기화
//            key[keySetting] = (String) iterator.next();
//            keySetting++;
//        }
//        for (int i = 0; i < DB_Size; i++) {
//            String id = rtList.get(i).getId();
//            System.out.println(Map_Breakfast.keySet());
//            for (int j = 0; j < Map_Breakfast.size(); j++) {
//                if (id.equals(key[j])) {
//                    //칼로리 계산
//                    total += Integer.parseInt(rtList.get(i).getCalorie());
//                }
//            }
//        }
//        return total;
//    }


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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_food, container, false);
        //initLoadDB();


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

        currentUser = getActivity().getSharedPreferences("currentUser",getActivity().MODE_PRIVATE);

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
        //SharedPreferences.Editor editor = pref.edit();

//        if (getMenu_Food() == null || Menu_Food.size() == 0) {
//            System.out.println(getMenu_Food());
//        }
//        if(breakfast==null||pre.getString("breakfast",null)==null){
//           System.out.println("null");
//        }
//        else{
//            System.out.println("아침식사:"+breakfast);
//            morning.setText("다시짜기");
//            morningfood.setText("");
//            morningfood.append(pre.getString("breakfast",null)+pre.getString("user_Breakfast",null));
//            morningcalrorie.setText("아침식단 칼로리:"+pre.getString("brakfastCalrorie",null)+"kcal");
//
//    }
//        if(lunch_Menu==null||pre.getString("lunch_Menu",null)==null){
//            System.out.println("null");
//        }
//        else{
//            afternoon.setText("다시짜기");
//            afternoonfood.setText("");
//            afternoonfood.append(pre.getString("lunch_Menu",null)+pre.getString("user_Lunch",null));
//            afternoonclarorie.setText("점심식단 칼로리:"+pre.getString("lunch_MenuCalrorie",null)+"kcal");
//
//        }
//        if(dinner_Menu==null||pre.getString("dinner_Menu",null)==null){
//            System.out.println("null");
//        }
//        else{
//            dinner.setText("다시짜기");
//            dinnerfood.setText("");
//            dinnerfood.append(pre.getString("dinner_Menu",null)+pre.getString("user_Dinner",null));
//            dinnercalrorie.setText("저녁식단 칼로리:"+pre.getString("dinner_MenuCalrorie",null)+"kcal");
//
//        }

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
    }
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
    public int FoodCalrorieSum() {
        int sum=0;
        for(int i = 0 ; i<Menu_Food.size();i++) {
            sum += Integer.parseInt(getMenu_Calrorie().get(i));
        }
        sum += UserCalrorieSum();
        return sum;
    }
    public int UserCalrorieSum() {
        int total = 0;
        for(int i = 0 ; i<UserCalrorie.size(); i++) {
            total+=Integer.parseInt(getUserCalrorie().get(i));
        }
        return total;
    }
    private void setProgressBar() {
//        String gender = getArguments().getString("gender");
//        System.out.println(gender);

        if(gender.equals("male")){
            progress.setMax(MALE);
            tvbartext.setText(" / 3200 kcal");
            progress.setProgress(totalSum);
        }
        else if(gender.equals("female")){
            progress.setMax(FEMALE);
            tvbartext.setText(" / 2600 kcal");
            progress.setProgress(totalSum);
        }
    }
}