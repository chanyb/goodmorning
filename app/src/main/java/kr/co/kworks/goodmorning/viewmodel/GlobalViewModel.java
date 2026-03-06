package kr.co.kworks.goodmorning.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import kr.co.kworks.goodmorning.model.repository.DeviceInfoRepository;
import kr.co.kworks.goodmorning.utils.Database;


@HiltViewModel
public class GlobalViewModel extends ViewModel {

    private final Executor io = Executors.newSingleThreadExecutor();

    public MutableLiveData<Event<String>> _webViewFragment, _popBackStack;
    public DeviceInfoRepository deviceInfoRepository;

    @Inject
    public GlobalViewModel(
        DeviceInfoRepository deviceInfoRepository
    ) {
        this.deviceInfoRepository = deviceInfoRepository;
        init();
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.toString();
    }

    public void init() {
        _popBackStack = new MutableLiveData<>();
        _webViewFragment = new MutableLiveData<>();
    }
}
