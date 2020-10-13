package com.example.fromtoday;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    public ViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }


//    //실험 이중 스크롤뷰 어뎁터에 선언
//    viewPager.setOnTouchListener(new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//            parentscrollview.requestDisallowInterceptTouchEvent(true);
//            return false;
//        }
//    });

    //프래그 먼트 교체를 보여주는 곳
    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return Frag_Activity_Vp_Walk.walkInstance();
            case 1:
                return Frag_Activity_Vp_Run.runInstance();
            case 2:
                return Frag_Activity_Vp_Bike.bikeInstance();
            default:
                return null;

        }
    }

    //화면 갯수 명시
    @Override
    public int getCount() {
        return 3;
    }

    //산단의 탭 부분의 텍스트를 선언해주는 곳
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "걷기";
            case 1:
                return "뛰기";
            case 2:
                return "자전거";
            default:
                return null;
        }
    }
}
