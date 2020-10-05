package com.example.fromtoday;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

public class Frag_People extends Fragment {
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
        view = inflater.inflate(R.layout.frag_people, container, false);
        //setHasOptionsMenu(true);

        profile = view.findViewById(R.id.profile);
        name = view.findViewById(R.id.name);
        Age = view.findViewById(R.id.age);
        gender=view.findViewById(R.id.gender);
        email=view.findViewById(R.id.email);
        birthday=view.findViewById(R.id.birthday);
        back = view.findViewById(R.id.back);
        button =view.findViewById(R.id.button);
        logout=view.findViewById(R.id.logout);

        currentUser = getActivity().getSharedPreferences("currentUser",getActivity().MODE_PRIVATE);

        String strProfile = currentUser.getString("profile",null);
        String strName = currentUser.getString("name",null);
        String strEmail = currentUser.getString("email",null);
        String strAge = currentUser.getString("age",null);
        String strGender = currentUser.getString("gender",null);
        String strBirthday = currentUser.getString("birthday",null);
        if(strProfile == null){
            Glide.with(this).load(R.drawable.noneprofile).circleCrop().into(profile);
        }
        else{
            Glide.with(this).load(strProfile).circleCrop().into(profile);
        }
//        if(getArguments()!=null) {
//            //bundle = getArguments();
//            String imageUrl = getArguments().getString("profile");
//            String Name = getArguments().getString("name");
//            String Email = getArguments().getString("email");
//            String age = getArguments().getString("age");
//            String Gender = getArguments().getString("gender");
//            String Birth = getArguments().getString("birthday");
//            if(imageUrl==null) {
//                //profile.setImageResource(R.drawable.noneprofile);
//                Glide.with(this).load(R.drawable.noneprofile).circleCrop().into(profile);
//            }
//            else {
//                Glide.with(this).load(imageUrl).circleCrop().into(profile);
//            }
            name.setText(strName);
            Age.setText(strAge);
            gender.setText(strGender);
            email.setText(strEmail);
            birthday.setText(strBirthday);
//        }
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
//        button.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//                UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback() {
//
//                    @Override
//                    public void onFailure(ErrorResult errorResult) {
//                        //회원탈퇴 실패 시 동작
//                    }
//
//                    @Override
//                    public void onSessionClosed(ErrorResult errorResult) {
//                        //세션이 닫혔을 시 동작.
//                    }
//
//                    @Override
//                    public void onNotSignedUp() {
//                        //가입되지 않은 계정이 회원탈퇴를 요구할 경우 동작.
//                    }
//
//                    @Override
//                    public void onSuccess(Long result) {
//                        //회원탈퇴 성공 시 동작.
//                    }
//                });
//            }
//        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserManagement.getInstance()
                        .requestLogout(new LogoutResponseCallback() {
                            @Override
                            // 로그아웃 완료시 호출되는 함수
                            public void onCompleteLogout() {
//                                Intent intent =new Intent(getActivity(), Login.class);
//                                startActivity(intent);
                                getActivity().finish();
                            }
                        });

            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogue();


            }
        });

        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("여기는 onResume");

    }

    private void dialogue() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("회원탈퇴").setMessage("탈퇴하시겠습니까?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback() {

                    @Override
                    public void onFailure(ErrorResult errorResult) {
                        //회원탈퇴 실패 시 동작
                    }

                    @Override
                    public void onSessionClosed(ErrorResult errorResult) {
                        //세션이 닫혔을 시 동작.
                    }

                    @Override
                    public void onNotSignedUp() {
                        //가입되지 않은 계정이 회원탈퇴를 요구할 경우 동작.
                    }

                    @Override
                    public void onSuccess(Long result) {
                        //회원탈퇴 성공 시 동작.
                        currentUser = getActivity().getSharedPreferences("currentUser",getActivity().MODE_PRIVATE);
                        String strEmail;
                        strEmail = currentUser.getString("email",null);
//                       Log.d("email",strEmail);
                        SharedPreferences pre = getActivity().getSharedPreferences(""+strEmail,getActivity().MODE_PRIVATE);

                        strEmail = strEmail.replaceAll("@", "").replaceAll("[.]", "");

                        mDatabase = FirebaseDatabase.getInstance();
                        dataRef = mDatabase.getReference("users");
                        dataRef.child(strEmail).removeValue();

                        SharedPreferences.Editor editor = pre.edit();
                        editor.clear().commit();

                        Intent intent =new Intent(getActivity(), Login.class);
                        startActivity(intent);
                        getActivity().finish();
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                Toast.makeText(getActivity(), "Cancel Click", Toast.LENGTH_SHORT).show();
            }
        });


        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


}
