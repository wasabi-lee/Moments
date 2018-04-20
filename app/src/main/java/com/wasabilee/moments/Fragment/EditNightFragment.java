package com.wasabilee.moments.Fragment;

import android.app.DatePickerDialog;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wasabilee.moments.Activitiy.EditActivity;
import com.wasabilee.moments.Activitiy.ImageDetailActivity;
import com.wasabilee.moments.Utils.ActivityNavigator;
import com.wasabilee.moments.ViewModel.EditViewModel;
import com.wasabilee.moments.R;
import com.wasabilee.moments.Utils.SnackbarUtils;
import com.wasabilee.moments.databinding.FragmentEditNightBinding;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;


public class EditNightFragment extends Fragment implements DatePickerDialog.OnDateSetListener, View.OnClickListener {

    private static final String TAG = EditNightFragment.class.getSimpleName();

    private static final int NIGHT_IMAGE_DETAIL_REQUEST_CODE = 23;
    public static final String JOURNAL_TIME_IDENTIFIER_NIGHT = "night_journal";


    @BindView(R.id.edit_night_date_text)
    TextView mDateText;
    @BindView(R.id.edit_night_journal_image)
    ImageView mImageView;
    @BindView(R.id.edit_night_journal_no_image_text)
    TextView mNoImageText;

    private EditViewModel mViewModel;
    private FragmentEditNightBinding mBinding;

    public EditNightFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_night, container, false);
        ButterKnife.bind(this, view);

        if (mBinding == null) {
            mBinding = FragmentEditNightBinding.bind(view);
        }

        mViewModel = EditActivity.obtainViewModel(getActivity());
        mBinding.setViewmodel(mViewModel);

        setHasOptionsMenu(true);
        setRetainInstance(false);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel.start();
        setupCalendar();
        setupImageView();
        setupSnackbar();
        setupActivityTransitionObservers();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_save:
                //TODO Save edits
                 mViewModel.saveJournal();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupCalendar() {
        mDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
    }

    private void setupSnackbar() {
        mViewModel.getSnackbarText().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                showSnackbar(getString(integer));
            }
        });
    }

    private void setupActivityTransitionObservers() {
        mViewModel.getNightActivityChangeLiveData().observe(this, new Observer<ActivityNavigator>() {
            @Override
            public void onChanged(@Nullable ActivityNavigator activityNavigator) {
                if (activityNavigator != null)
                switch (activityNavigator) {
                    case NEW_IMAGE:
                        toCropImageActivity();
                        break;
                }
            }
        });

        mViewModel.getNightImageDetailOpeningEvent().observe(this, new Observer<Uri>() {
            @Override
            public void onChanged(@Nullable Uri uri) {
                toImageDetailActivity(uri);
            }
        });
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


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mViewModel.onDateSet(year, month, dayOfMonth);
    }


    private void setupImageView() {
        mImageView.setOnClickListener(this);
        mNoImageText.setOnClickListener(this);
    }
    private void toCropImageActivity() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(getContext(), EditNightFragment.this);
    }

    private void toImageDetailActivity(Uri uri) {
        Intent intent = new Intent(getContext(), ImageDetailActivity.class);
        intent.putExtra(ImageDetailActivity.IMAGE_URI_EXTRA_KEY, uri.toString());
        startActivityForResult(intent, NIGHT_IMAGE_DETAIL_REQUEST_CODE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_night_journal_no_image_text:
                mViewModel.toCropImageActivity(JOURNAL_TIME_IDENTIFIER_NIGHT);
                break;
            case R.id.edit_night_journal_image:
                mViewModel.toImageDetailActivity(JOURNAL_TIME_IDENTIFIER_NIGHT);
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mViewModel.handleActivityResult(requestCode, resultCode, data, JOURNAL_TIME_IDENTIFIER_NIGHT);
    }

    private void showSnackbar(String s) {
        SnackbarUtils.showSnackbar(getActivity().findViewById(android.R.id.content), s);
    }

}
