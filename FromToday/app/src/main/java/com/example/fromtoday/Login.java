package com.example.fromtoday;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.kakao.auth.ApiErrorCode;
import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Login extends AppCompatActivity {

    private Button btn_custom_login;
    private Button btn_custom_login_out;
    private ImageView today;
    private SessionCallback sessionCallback = new SessionCallback();
    Session session;
    public SharedPreferences user_Value;


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_login2);
        today=findViewById(R.id.today);
        today.setVisibility(View.VISIBLE);
        btn_custom_login = (Button) findViewById(R.id.btn_custom_login);
        btn_custom_login_out = (Button) findViewById(R.id.btn_custom_login_out);

        session = Session.getCurrentSession();
        session.addCallback(sessionCallback);


        //session.open(AuthType.KAKAO_LOGIN_ALL,Login.this);

        //Session.getCurrentSession().isOpened();

        btn_custom_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                session.open(AuthType.KAKAO_LOGIN_ALL, Login.this);
                //로그인 status 확인
                if(Session.getCurrentSession().isOpened()) {

                }
                else{
                    Toast.makeText(Login.this, "로그인해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_custom_login_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Login.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                UserManagement.getInstance()
                        .requestLogout(new LogoutResponseCallback() {
                            @Override
                            public void onCompleteLogout() {

                            }
                        });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 세션 콜백 삭제
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // 카카오톡|스토리 간편로그인 실행 결과를 받아서 SDK로 전달
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    public class SessionCallback implements ISessionCallback {
        @Override
        public void onSessionOpened() {
            UserManagement.getInstance().me(new MeV2ResponseCallback() {
                @Override
                public void onFailure(ErrorResult errorResult) {
                    int result = errorResult.getErrorCode();

                    if(result == ApiErrorCode.CLIENT_ERROR_CODE) {
                        Toast.makeText(getApplicationContext(), "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(),"로그인 도중 오류가 발생했습니다: "+errorResult.getErrorMessage(),Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    Toast.makeText(getApplicationContext(),"세션이 닫혔습니다. 다시 시도해 주세요: "+errorResult.getErrorMessage(),Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(MeV2Response result) {
//                    Frag_People people = new Frag_People();
//                    Bundle bundle =new Bundle();
//                    bundle.putString("profile",result.getProfileImagePath());
//                    bundle.putString("name",result.getNickname());
//                    people.setArguments(bundle);

                    user_Value = getSharedPreferences("currentUser",MODE_PRIVATE);
                    SharedPreferences.Editor editor = user_Value.edit();
                    editor.putString("name",result.getNickname());
                    editor.putString("profile",result.getProfileImagePath());
                    editor.putString("email",result.getKakaoAccount().getEmail());
                    editor.putString("age",result.getKakaoAccount().getAgeRange().getValue());
                    editor.putString("birthday",result.getKakaoAccount().getBirthday());
                    editor.putString("gender",result.getKakaoAccount().getGender().getValue());
                    editor.commit();

                    Intent intent = new Intent(Login.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }

        @Override
        public void onSessionOpenFailed(KakaoException e) {
            Toast.makeText(getApplicationContext(), "로그인 도중 오류가 발생했습니다. 인터넷 연결을 확인해주세요: "+e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}