package com.example.fromtoday;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;


public class Frag_Activity_Vp_Run extends Fragment {
    private View view;


    //요일
    private int calendar_week;

    //리사이클 뷰
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Activity_DTO> arrayList;

    //DTO
    private DatabaseReference mPostReference;

    String resultactivity;

    Activity_DTO activity_dto = new Activity_DTO();

    //유저 이메일을 받기 위한 프리퍼런스 선언
    public SharedPreferences user_Value;

    private ArrayList<Activity_DTO> result_run = new ArrayList<>();

    //싱글톤 어디서든 객체를 생성 가능
    public static Frag_Activity_Vp_Run runInstance(){
        Frag_Activity_Vp_Run frag_activity_vp_run = new Frag_Activity_Vp_Run();
        return frag_activity_vp_run;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_activity_vp_walk, container, false);

        //리사이클뷰 설정
        recyclerView = view.findViewById(R.id.recyclerview);// 아디 연결
        recyclerView.setHasFixedSize(true);//리사이클뷰 기존 성능 강화
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>();// dto객체를 담을 리스트..어뎀터 쪽으로
        mPostReference = FirebaseDatabase.getInstance().getReference();


        //프리퍼런스
        user_Value = getActivity().getSharedPreferences("currentUser", MODE_PRIVATE);
        String strEmail = user_Value.getString("email", null);
        strEmail = strEmail.replaceAll("@", "").replaceAll("[.]", "");

        //캘린더 객체
        Calendar calendar = Calendar.getInstance();
        calendar_week = calendar.get(Calendar.DAY_OF_WEEK);


        mPostReference.child("users").child(strEmail).child("activity").child("run_activity").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                result_run.clear();//dto초기화

                // double sumkcal = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {//데이터 리스트 추출
                    Log.e("키2", snapshot.getKey());//walk_activity 의 키값들을 가져온다.
                    Activity_DTO activity_dto = snapshot.getValue(Activity_DTO.class);//dto의 객체를 담는다.
                    result_run.add(activity_dto);//담은 데이터들을 배열 리스트에 넣고 리사이클러뷰에 보낼 준비를 한다.
                }
                adapter.notifyDataSetChanged();//리스트 저장 및 새로고침
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //에러발생시
                Log.e("onCancelled", "onCancelled: " );
            }
        });

        adapter = new CustomAdapter_run(result_run, getActivity());
        recyclerView.setAdapter(adapter);//리사이클러뷰에 어뎁터 연결

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}