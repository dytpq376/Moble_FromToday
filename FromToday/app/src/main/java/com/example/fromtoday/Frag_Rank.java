package com.example.fromtoday;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;

public class Frag_Rank extends Fragment {
    private View view;
    private ImageView profile;
    private TextView name;
    private TextView Age;
    private TextView gender;
    private TextView email;
    private TextView birthday;
    private Button button;
    private Button logout;
    private ImageView back;
    SharedPreferences currentUser;
    String user_Email;
    FirebaseDatabase mDatabase;
    DatabaseReference dataRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_rank, container, false);
        //setHasOptionsMenu(true);





        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("여기는 onResume");

    }




}
