package com.example.fromtoday;//package com.example.fromtoday;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import android.content.Intent;
//import android.os.Bundle;
//import android.provider.ContactsContract;
//import android.view.View;
//import android.widget.Button;
//import android.widget.Toast;
//
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.kakao.auth.ApiErrorCode;
//import com.kakao.auth.AuthType;
//import com.kakao.auth.ISessionCallback;
//import com.kakao.auth.Session;
//import com.kakao.network.ErrorResult;
//import com.kakao.usermgmt.UserManagement;
//import com.kakao.usermgmt.callback.LogoutResponseCallback;
//import com.kakao.usermgmt.callback.MeV2ResponseCallback;
//import com.kakao.usermgmt.response.MeV2Response;
//import com.kakao.usermgmt.response.model.Profile;
//import com.kakao.util.exception.KakaoException;
//
//public class Frag_Login extends Fragment {
//    private View view;
//    private Button btn_custom_login;
//    private Button btn_custom_login_out;
//    private SessionCallback sessionCallback = new SessionCallback();
//    Session session;
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        view = inflater.inflate(R.layout.frag_login, container, false);
//
//        btn_custom_login = (Button) view.findViewById(R.id.btn_custom_login);
//        btn_custom_login_out = (Button) view.findViewById(R.id.btn_custom_login_out);
//
//        session = Session.getCurrentSession();
//        session.addCallback(sessionCallback);
//
//        btn_custom_login.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                session.open(AuthType.KAKAO_LOGIN_ALL, Frag_Login.this);
//                Intent intent=new Intent(Frag_Login.this,Frag_People.class);
//                startActivity(intent);
//            }
//        });
//
//        btn_custom_login_out.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                UserManagement.getInstance()
//                        .requestLogout(new LogoutResponseCallback() {
//                            @Override
//                            public void onCompleteLogout() {
//                                Toast.makeText(Frag_Login.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//            }
//        });
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
//        // 세션 콜백 삭제
//        Session.getCurrentSession().removeCallback(sessionCallback);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        // 카카오톡|스토리 간편로그인 실행 결과를 받아서 SDK로 전달
//        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
//            return;
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }
//    public class SessionCallback implements ISessionCallback {
//        @Override
//        public void onSessionOpened() {
//            UserManagement.getInstance().me(new MeV2ResponseCallback() {
//                @Override
//                public void onFailure(ErrorResult errorResult) {
//                    int result = errorResult.getErrorCode();
//
//                    if(result == ApiErrorCode.CLIENT_ERROR_CODE) {
//                        Toast.makeText(getActivity(), "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
//                        getActivity().finish();
//                    } else {
//                        Toast.makeText(getActivity(),"로그인 도중 오류가 발생했습니다: "+errorResult.getErrorMessage(),Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                @Override
//                public void onSessionClosed(ErrorResult errorResult) {
//                    Toast.makeText(getActivity(),"세션이 닫혔습니다. 다시 시도해 주세요: "+errorResult.getErrorMessage(),Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void onSuccess(MeV2Response result) {
//                    Intent intent = new Intent(Frag_Login.this, Frag_People.class);
//                    intent.putExtra("name", result.getNickname());
//                    intent.putExtra("profile", result.getProfileImagePath());
//                    startActivity(intent);
//                    getActivity().finish();
//                }
//            });
//        }
//
//        @Override
//        public void onSessionOpenFailed(KakaoException e) {
//            Toast.makeText(getActivity(), "로그인 도중 오류가 발생했습니다. 인터넷 연결을 확인해주세요: "+e.toString(), Toast.LENGTH_SHORT).show();
//        }
//
//        return view;
//
//    }
//}
