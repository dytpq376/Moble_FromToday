package com.example.fromtoday;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapter_run extends RecyclerView.Adapter<CustomAdapter_run.CustomViewHolder> {

    private ArrayList<Activity_DTO> arrayList;
    private Context context;

    private String WeekChange;

    public CustomAdapter_run(ArrayList<Activity_DTO> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.frag_activity_vp_run_item, parent, false);
        CustomViewHolder holder = new CustomViewHolder(view);//리스트 아이템에 뷰 홀더
        return holder;
    }



    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {//각 아이템에 매칭을 해준다.
        Log.e("arrayList.get(position", arrayList.get(position).getRun_Week());
        switch (arrayList.get(position).getRun_Week()) {
            case "1":
                WeekChange = "일요일";
                break;
            case "2":
                WeekChange = "월요일";
                break;
            case "3":
                WeekChange = "화요일";
                break;
            case "4":
                WeekChange = "수요일";
                break;
            case "5":
                WeekChange = "목요일";
                break;
            case "6":
                WeekChange = "금요일";
                break;
            case "7":
                WeekChange = "토1요일";
                break;
        }
        Log.e("WeekChange", WeekChange);
        holder.tv_week.setText(WeekChange);
        holder.tv_distance.setText(arrayList.get(position).getRun_distance());
        holder.tv_time.setText(arrayList.get(position).getRun_time());
        holder.tv_kcal.setText(arrayList.get(position).getRun_kcal());
    }


    @Override
    public int getItemCount() {
        //삼항 연산자
        return (arrayList != null ? arrayList.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView tv_week;
        TextView tv_distance;
        TextView tv_time;
        TextView tv_kcal;


        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tv_week = itemView.findViewById(R.id.tv_week);
            this.tv_distance = itemView.findViewById(R.id.tv_distance);
            this.tv_time = itemView.findViewById(R.id.tv_time);
            this.tv_kcal = itemView.findViewById(R.id.tv_kcal);
        }
    }
}