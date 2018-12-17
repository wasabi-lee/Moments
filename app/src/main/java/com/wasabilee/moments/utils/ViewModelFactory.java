package com.wasabilee.moments.utils;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.wasabilee.moments.data.JournalLocalDataSource;
import com.wasabilee.moments.data.JournalRemoteDataSource;
import com.wasabilee.moments.data.JournalRepository;
import com.wasabilee.moments.viewmodel.DetailViewModel;
import com.wasabilee.moments.viewmodel.EditViewModel;
import com.wasabilee.moments.viewmodel.FullImageViewModel;
import com.wasabilee.moments.viewmodel.ImageDetailViewModel;
import com.wasabilee.moments.viewmodel.MainViewModel;

public class ViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private static volatile ViewModelFactory INSTANCE;

    private final Application mApplication;

    private final JournalRepository mJournalRepository;

    public static ViewModelFactory getInstance(Application application) {
        if (INSTANCE == null) {
            synchronized (ViewModelFactory.class) {
                if (INSTANCE == null)
                    INSTANCE = new ViewModelFactory(application,
                            JournalRepository.getInstance(JournalRemoteDataSource.getInstance(),
                                    JournalLocalDataSource.getInstance(application)));
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    private ViewModelFactory(Application application, JournalRepository repository) {
        mApplication = application;
        mJournalRepository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ImageDetailViewModel.class)) {
            //noinspection unchecked
            return (T) new ImageDetailViewModel(mApplication, mJournalRepository);
        } else if (modelClass.isAssignableFrom(EditViewModel.class)) {
            //noinspection unchecked
            return (T) new EditViewModel(mApplication, mJournalRepository);
        } else if (modelClass.isAssignableFrom(MainViewModel.class)) {
            //noinspection unchecked
            return (T) new MainViewModel(mApplication, mJournalRepository);
        } else if (modelClass.isAssignableFrom(DetailViewModel.class)) {
            //noinspection unchecked
            return (T) new DetailViewModel(mApplication, mJournalRepository);
        } else if (modelClass.isAssignableFrom(FullImageViewModel.class)) {
            //noinspection unchecked
            return (T) new FullImageViewModel(mApplication, mJournalRepository);
        }

        return super.create(modelClass);
    }
}
