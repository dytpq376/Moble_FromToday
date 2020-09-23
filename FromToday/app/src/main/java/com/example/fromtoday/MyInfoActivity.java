package com.example.fromtoday;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;

public class MyInfoActivity extends AppCompatActivity {
    private ImageView profile;
    private TextView name;
    private TextView Age;
    private TextView gender;
    private TextView email;
    private TextView birthday;
    private Button button;
    private Button logout;
    private ImageView back;
    private ImageView imageUser;

    SharedPreferences currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myinfo);

        // 내 활동 Image
        imageUser = (ImageView)findViewById(R.id.imageUser);
        imageUser.setColorFilter(Color.parseColor(Colors.BLUE), PorterDuff.Mode.SRC_IN);

        profile = findViewById(R.id.profile);
        name = findViewById(R.id.name);
        Age = findViewById(R.id.age);
        gender = findViewById(R.id.gender);
        email = findViewById(R.id.email);
        birthday = findViewById(R.id.birthday);
        back = findViewById(R.id.back);
        button = findViewById(R.id.button);
        logout = findViewById(R.id.logout);
        prefCurrent();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    }
                });
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserManagement.getInstance()
                        .requestLogout(new LogoutResponseCallback() {
                            @Override
                            public void onCompleteLogout() {

                            }
                        });
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
            }
        });

    }
    private void prefCurrent() {
        currentUser = getSharedPreferences("currentUser", MODE_PRIVATE);
        String strProfile = currentUser.getString("profile", null);
        String strName = currentUser.getString("name", null);
        String strEmail = currentUser.getString("email", null);
        String strAge = currentUser.getString("age", null);
        String strGender = currentUser.getString("gender", null);
        String strBirthday = currentUser.getString("birthday", null);
        if (strProfile == null) {
            Glide.with(this).load(R.drawable.noneprofile).circleCrop().into(profile);
        } else {
            Glide.with(this).load(strProfile).circleCrop().into(profile);
        }
        name.setText(strName);
        Age.setText(strAge);
        gender.setText(strGender);
        email.setText(strEmail);
        birthday.setText(strBirthday);
    }
}



