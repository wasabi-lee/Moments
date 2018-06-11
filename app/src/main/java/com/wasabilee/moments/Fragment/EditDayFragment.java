package com.wasabilee.moments.Fragment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wasabilee.moments.Activity.EditActivity;
import com.wasabilee.moments.Activity.ImageDetailActivity;
import com.wasabilee.moments.ViewModel.EditViewModel;
import com.wasabilee.moments.R;
import com.wasabilee.moments.Utils.SnackbarUtils;
import com.wasabilee.moments.databinding.FragmentEditDayBinding;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;


public class EditDayFragment extends Fragment implements DatePickerDialog.OnDateSetListener, View.OnClickListener {

    private static final String TAG = EditDayFragment.class.getSimpleName();

    public static final String JOURNAL_TIME_IDENTIFIER_DAY = "day_journal";

    @BindView(R.id.edit_day_date_text)
    TextView mDateText;
    @BindView(R.id.edit_day_journal_image)
    ImageView mImageView;
    @BindView(R.id.edit_day_journal_no_image_text)
    TextView mNoImageText;

    private EditViewModel mViewModel;
    private FragmentEditDayBinding mBinding;

    public EditDayFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_day, container, false);
        ButterKnife.bind(this, view);

        if (mBinding == null) {
            mBinding = FragmentEditDayBinding.bind(view);
        }

        mViewModel = EditActivity.obtainViewModel(getActivity());
        mBinding.setViewmodel(mViewModel);

        setRetainInstance(false);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupCalendar();
        setupImageView();
        setupActivityTransitionObservers();
    }

    private void setupCalendar() {
        mDateText.setOnClickListener(v -> showDatePicker());
    }


    private void setupActivityTransitionObservers() {
        mViewModel.getDayActivityChangeLiveData().observe(this, activityNavigator -> {
            if (activityNavigator != null)
                switch (activityNavigator) {
                    case NEW_IMAGE:
                        toCropImageActivity();
                        break;
                }
        });

        mViewModel.getDayImageDetailOpeningEvent().observe(this, this::toImageDetailActivity);
    }

    private void showDatePicker() {
        Date currentDate = mViewModel.getDate();
        DatePickerFragment newFrag = new DatePickerFragment();
        newFrag.setmDate(currentDate);
        newFrag.setmListener(this);
        try {
            newFrag.show(getActivity().getSupportFragmentManager(), "datePicker");
        } catch (Exception e) {
            showSnackbar(getString(R.string.unexpected_error));
            e.printStackTrace();
        }
    }

    private void setupImageView() {
        mImageView.setOnClickListener(this);
        mNoImageText.setOnClickListener(this);
    }

    private void toCropImageActivity() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(getContext(), EditDayFragment.this);
    }

    private void toImageDetailActivity(Uri uri) {
        Intent intent = new Intent(getContext(), ImageDetailActivity.class);
        intent.putExtra(ImageDetailActivity.IMAGE_SOURCE_EXTRA_KEY, uri.toString());
        intent.putExtra(ImageDetailActivity.IDENTIFIER_EXTRA_KEY, JOURNAL_TIME_IDENTIFIER_DAY);
        startActivityForResult(intent, EditActivity.IMAGE_DETAIL_REQUEST_CODE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_day_journal_no_image_text:
                mViewModel.toCropImageActivity(JOURNAL_TIME_IDENTIFIER_DAY);
                break;
            case R.id.edit_day_journal_image:
                mViewModel.toImageDetailActivity(JOURNAL_TIME_IDENTIFIER_DAY);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mViewModel.handleActivityResult(requestCode, resultCode, data, JOURNAL_TIME_IDENTIFIER_DAY);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mViewModel.onDateSet(year, month, dayOfMonth);
    }


    private void showSnackbar(String s) {
        Log.d(TAG, "showSnackbar: ");
        SnackbarUtils.showSnackbar(getActivity().findViewById(android.R.id.content), s);
    }

}
