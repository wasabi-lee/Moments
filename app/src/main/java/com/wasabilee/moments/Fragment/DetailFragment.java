package com.wasabilee.moments.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wasabilee.moments.Activity.DetailActivity;
import com.wasabilee.moments.R;
import com.wasabilee.moments.ViewModel.DetailViewModel;
import com.wasabilee.moments.databinding.FragmentDayDetailBinding;
import com.wasabilee.moments.databinding.FragmentNightDetailBinding;

public class DetailFragment extends Fragment {

    private static final String TAG = DetailFragment.class.getSimpleName();

    public static final String DETAIL_FRAG_LAYOUT_RESOURCE_ARG_KEY = "detail_frag_layout_resource_key";

    private DetailViewModel mViewModel;
    private FragmentDayDetailBinding mDayBinding;
    private FragmentNightDetailBinding mNightBinding;


    public DetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        int layoutResource = R.layout.fragment_day_detail;
        if (getArguments() != null) {
            layoutResource = getArguments().getInt(DETAIL_FRAG_LAYOUT_RESOURCE_ARG_KEY, R.layout.fragment_day_detail);
        }

        View view = inflater.inflate(layoutResource, container, false);
        mViewModel = DetailActivity.obtainViewModel(getActivity());


        if (layoutResource == R.layout.fragment_day_detail) {
            if (mDayBinding == null) {
                mDayBinding = FragmentDayDetailBinding.bind(view);
                mDayBinding.setViewmodel(mViewModel);
            }
        } else {
            if (mNightBinding == null) {
                mNightBinding = FragmentNightDetailBinding.bind(view);
                mNightBinding.setViewmodel(mViewModel);
            }
        }
        setRetainInstance(false);

        return getRoot(layoutResource);
    }


    private View getRoot(int layoutResource) {
        if (layoutResource == R.layout.fragment_day_detail) {
            return mDayBinding.getRoot();
        }
        return mNightBinding.getRoot();
    }
}

