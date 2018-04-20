package com.wasabilee.moments.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    public static final int FARG_POSITION_DAY = 0;
    public static final int FARG_POSITION_NIGHT = 1;

    private final List<Fragment> mFragmentList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    public void addFragment(int position, Fragment fragment) {
        mFragmentList.add(position, fragment);
    }

    public Fragment getFragment(int position) {
        return mFragmentList.get(position);
    }

}
