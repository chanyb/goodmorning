package kr.co.kworks.goodmorning.viewmodel;

import android.os.AsyncTask;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class IntroViewModel extends ViewModel {

    public MutableLiveData<Boolean> updateNeeded, updateNotNeed;
    public AsyncTask<Object, Object, Object> liveUpdateLogOutTask;
    public MutableLiveData<String> updateString;
    public MutableLiveData<Integer> downloadPercent;

    public IntroViewModel() {
        init();
    }

    public void init() {
        updateString = new MutableLiveData<>("");
        liveUpdateLogOutTask = null;
        updateNeeded = new MutableLiveData<>(false);
        downloadPercent = new MutableLiveData<>(0);
        updateNotNeed = new MutableLiveData<>(false);
    }
}
