package com.wasabilee.moments.Utils;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.wasabilee.moments.Data.JournalRemoteDataSource;
import com.wasabilee.moments.Data.JournalRepository;
import com.wasabilee.moments.ViewModel.EditViewModel;
import com.wasabilee.moments.ViewModel.ImageDetailViewModel;
import com.wasabilee.moments.ViewModel.MainViewModel;

public class ViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private static volatile ViewModelFactory INSTANCE;

    private final Application mApplication;

    private final JournalRepository mJournalRepository;

    public static ViewModelFactory getInstance(Application application) {
        if (INSTANCE == null) {
            synchronized (ViewModelFactory.class) {
                if (INSTANCE == null)
                    INSTANCE = new ViewModelFactory(application,
                        JournalRepository.getInstance(JournalRemoteDataSource.getInstance()));
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
        }
        else if (modelClass.isAssignableFrom(EditViewModel.class)) {
            //noinspection unchecked
            return (T) new EditViewModel(mApplication, mJournalRepository);
        }
        else if (modelClass.isAssignableFrom(MainViewModel.class)) {
            //noinspection unchecked
            return (T) new MainViewModel(mApplication, mJournalRepository);
        }

        return super.create(modelClass);
    }
}
