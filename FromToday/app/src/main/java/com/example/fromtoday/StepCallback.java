package com.example.fromtoday;

public interface StepCallback {
    // 서비스에서 스텝 변화시 액티비티로 전달하는 콜백 함수
    void onStepCallback(int step, int kcal, double kilometer, int minute, int second);
    // 서비스 언바인드 시 액티비티로 전달하는 콜백함수
    void onUnbindService();

}
