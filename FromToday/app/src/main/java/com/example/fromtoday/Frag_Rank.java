package com.example.fromtoday;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Frag_Rank extends Fragment {
    private View view;

//    private FirebaseDatabase mDatabase;
//    DatabaseReference dataRef;
    private ListView listView;
    private ListView listView2;

    ArrayAdapter<String> arrayAdapter;
    ArrayAdapter<String> rank_num;
    static ArrayList<String> arrayIndex =  new ArrayList<String>();
    static ArrayList<String> arrayData = new ArrayList<String>();
    static ArrayList<String> rankNum = new ArrayList<String>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_rank, container, false);

        arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
        rank_num = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1);
        listView = (ListView) view.findViewById(R.id.db_list_view);
        listView2 = (ListView) view.findViewById(R.id.rank_view);


        rankNum.clear();
        rankNum.add("1");
        rankNum.add("2");
        rankNum.add("3");
        rankNum.add("4");
        rankNum.add("5");
        rankNum.add("6");
        rankNum.add("7");
        rankNum.add("8");
        rankNum.add("9");
        rankNum.add("10");
        rank_num.addAll(rankNum);

        listView2.setAdapter(rank_num);
        listView.setAdapter(arrayAdapter);

        getFirebaseDatabase();

        return view;

    }

    public void getFirebaseDatabase(){
//        mDatabase = FirebaseDatabase.getInstance();
//        dataRef = mDatabase.getReference("users");

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Log.e("getFirebaseDatabase", "key: " + dataSnapshot.getChildrenCount());

                arrayData.clear();
                arrayIndex.clear();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    String key = postSnapshot.getKey();
                    FirebasePost get = postSnapshot.getValue(FirebasePost.class);
                    if(get.total_kcal == null){
                        get.total_kcal = Double.valueOf(0);
                    }
                    String[] info = {get.name, String.valueOf(get.total_kcal)};
                    String Result = setTextLength(info[0],10) + "          "+setTextLength(info[1],10)  ;

                    arrayData.add(0, Result);
                    arrayIndex.add(key);
//                    Log.d("getFirebaseDatabase", "key: " + key);
//                    Log.d("getFirebaseDatabase", "info: " + info[0] + info[1] + info[2]);
                }

                arrayAdapter.clear();
                arrayAdapter.addAll(arrayData);
                arrayAdapter.notifyDataSetChanged();

//                Log.e("aaaaaaaaa", String.valueOf(arrayData));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("getFirebaseDatabase","loadPost:onCancelled", databaseError.toException());
            }
        };
        Query query = FirebaseDatabase.getInstance().getReference().child("users").orderByChild("total_kcal").limitToLast(10);
        query.addValueEventListener(eventListener);



    }



    public String setTextLength(String text, int length){
        if(text.length()<length){
            int gap = length - text.length();
            for (int i=0; i<gap; i++){
                text = text + " ";
            }
        }
        return text;
    }

    @Override
    public void onResume() {
        super.onResume();
//        System.out.println("여기는 onResume");
        getFirebaseDatabase();

    }


}
